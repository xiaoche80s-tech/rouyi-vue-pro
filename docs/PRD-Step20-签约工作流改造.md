# PRD-Step20-操作请求工作流

## 概述

新建统一的"操作请求"模块，采用单一 BPM 工作流承载 4 种业务类型（申请签约、申请付款、申请开票、申请退货），本期先实现"申请签约"类型，将签约进度中的"去签署"功能改为 BPM 驱动。

## 变更背景

1. 当前"去签署"是直接修改 `status`/`subStatus` 字段，没有流程引擎参与，无法支持审批意见、流程跟踪、退回等能力
2. 业务上存在 4 类操作请求（签约/付款/开票/退货），它们的审批模式高度相似（经销商发起 → 内部审批），适合用统一工作流 + 按类型渲染不同表单

## 架构设计

### 统一模型（主表 + 子表）

- 主表 `ops_op_request`：所有操作请求共用的通用字段
- 子表按 `request_type` 拆分，每种类型独立的流程子表，存储差异化表单数据：
  - `ops_op_request_signing` — 签约请求子表（本期实现）
  - `ops_op_request_payment` — 付款请求子表（后续扩展）
  - `ops_op_request_invoice` — 开票请求子表（后续扩展）
  - `ops_op_request_return` — 退货请求子表（后续扩展）
- 主表**不存储** `source_id`、`form_data`，业务关联由子表维护

### 统一状态机

```
WAITING（待处理）→ IN_PROGRESS（处理中）→ DELIVERED（已交付）→ CLOSED（已验收）
                                                        ↘ REJECTED（已退回，可重新处理）
                                                        ↘ CANCEL（已取消）
```

| 状态 | 含义 | 触发者 |
|------|------|--------|
| WAITING | 已发起，等待执行员接单 | 经销商发起时自动设置 |
| IN_PROGRESS | 执行员已接单，正在处理中 | 执行员接单/开始操作 |
| DELIVERED | 执行员处理完毕，已提交结果 | 执行员提交处理结果（Service 层直接设置） |
| CLOSED | 经销商验收通过，流程结束 | BPM 回调 APPROVE |
| REJECTED | 经销商验收不通过，退回执行员重新处理 | BPM 回调 REJECT |
| CANCEL | 流程已取消 | BPM 回调 CANCEL |

### 单一 BPM 流程

- 流程 Key：`ops-op-request`
- 流程变量 `requestType` 用于网关路由（后续扩展时不同类型可走不同审批人）
- 本期仅实现 `signing` 类型
- BPM 三节点链路：发起人节点（经销商）→ 办理人节点（执行员）→ 审批节点（经销商验收）→ 结束

### 与签约合同的关系

- `ops_signing_contract` 表**不新增字段**
- 经销商在签约进度页点击"去签署" → 调用 `POST /opshub/op-request/create`（type=signing）→ 创建主表 + 签约子表记录 → 发起 BPM
- BPM 验收通过（CLOSED）→ 回调自动将合同 `status` 改为 `SIGNED`
- **删除原 `/opshub/signing/sign` 和 `/opshub/signing/upload-sign-proof` 接口**，统一走操作请求 API

## 变更内容

### 1. 新建操作请求数据表与 DO（P0）

**新建文件**：
- `yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/dal/dataobject/oprequest/OpRequestDO.java`

**设计逻辑**：
- 主表 `ops_op_request`，继承 `TenantBaseDO`
- 字段设计：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| request_no | String | 请求编号（自动生成，如 OP-2026-001） |
| request_type | OpRequestTypeEnum | 类型：signing/payment/invoice/return |
| request_type_name | String | 类型中文名 |
| dealer_id | Long | 经销商 ID |
| dealer_code | String | 经销商编码 |
| request_status | String | 状态：waiting/in_progress/delivered/closed/rejected/cancel |
| process_instance_id | String | BPM 流程实例 ID |
| assignee_id | Long | 当前处理人（执行员）ID |
| remark | String | 备注 |

> 注意：主表不包含 `source_id`、`source_code`、`form_data`，业务关联和差异化表单由各类型子表维护。

### 2. 签约子表 DO 与 Mapper（P0）

**新建文件**：
- `yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/dal/dataobject/oprequest/OpRequestSigningDO.java`
- `yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/dal/mysql/oprequest/OpRequestSigningMapper.java`

**设计逻辑**：
- 表名 `ops_op_request_signing`，继承 `TenantBaseDO`
- 字段设计：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| request_id | Long | 关联 `ops_op_request.id` |
| contract_id | Long | 关联 `ops_signing_contract.id` |
| contract_code | String | 合同编码 |
| contract_name | String | 合同名称 |

> 盖章文件通过通用附件表（`ops_cs_attachment`）存储，复用现有附件服务。

### 3. 主表 Mapper（P0）

**新建文件**：
- `yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/dal/mysql/oprequest/OpRequestMapper.java`

**设计逻辑**：
- 主表 Mapper：继承 `BaseMapperX<OpRequestDO>`，提供分页查询 + `selectMaxSeq`
- 子表 Mapper：继承 `BaseMapperX<OpRequestSigningDO>`

### 4. 操作请求枚举（P0）

**新建文件**：
- `yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/enums/OpRequestTypeEnum.java`
- `yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/enums/OpRequestStatusEnum.java`

**设计逻辑**：
```java
// OpRequestTypeEnum
SIGNING("signing", "申请签约"),
PAYMENT("payment", "申请付款"),
INVOICE("invoice", "申请开票"),
RETURN("return", "申请退货");

// OpRequestStatusEnum
WAITING("waiting", "待处理"),
IN_PROGRESS("in_progress", "处理中"),
DELIVERED("delivered", "已交付"),
CLOSED("closed", "已验收"),
REJECTED("rejected", "已退回"),
CANCEL("cancel", "已取消");
```

### 5. 操作请求 Service（P0）

**新建文件**：
- `OpRequestService.java` — 接口
- `OpRequestServiceImpl.java` — 实现（编排主表 + BPM，委托策略处理子表）

**设计逻辑**：
- `PROCESS_KEY = "ops-op-request"`
- `createOpRequest(OpRequestCreateReqVO)` — 创建请求 + 写入子表 + 发起 BPM
  1. 生成请求编号，插入主表（request_status=waiting，processInstanceId=null）
  2. 委托策略 `handler.onCreate(request, vo)` 写入子表
  3. 调用 `BpmProcessInstanceApi.createProcessInstance()`，`businessKey` = 请求 ID
  4. 回写 `processInstanceId`
  5. 委托策略 `handler.onStart(request)` 执行发起后处理（如 signing: 合同 subStatus→signing）
- `getOpRequestPage(OpRequestPageReqVO)` — 分页查询
- `getOpRequest(Long id)` — 详情（主表 + 委托策略 `handler.getDetail()` 联合返回）
- `submitResult(OpRequestSubmitResultReqVO)` — 执行员提交处理结果
  1. 校验 status=WAITING 或 IN_PROGRESS
  2. 委托策略 `handler.onSubmitResult(request, vo)` 更新子表（如保存 signProofUrl）
  3. 推动 BPM 执行员节点审批通过（流程流转到验收节点）
  4. Service 直接设置 status=DELIVERED（业务层驱动，非 BPM 回调）
- `verifyRequest(OpRequestVerifyReqVO)` — 经销商验收
  1. 校验 status=DELIVERED
  2. passed=true → 推动 BPM 验收节点审批通过（流程结束）
  3. passed=false → 推动 BPM 退回（流程结束）
- `updateOpRequestStatusByBpm(Long id, Integer bpmStatus)` — BPM 回调（仅处理终态）
  - APPROVE → status=CLOSED，委托策略 `handler.onClosed(request)` 执行后处理
  - REJECT → status=REJECTED
  - CANCEL → status=CANCEL

### 5.1 策略模式：OpRequestTypeHandler（P0）

**新建文件**：
- `service/oprequest/handler/OpRequestTypeHandler.java` — 策略接口
- `service/oprequest/handler/SigningRequestHandler.java` — 签约类型策略（本期实现）

**策略接口定义**：
```java
public interface OpRequestTypeHandler {
    /** 支持的请求类型 */
    String getRequestType();
    
    /** 创建时写入子表 */
    void onCreate(OpRequestDO request, OpRequestCreateReqVO vo);
    
    /** 发起后处理（如更新合同 subStatus） */
    void onStart(OpRequestDO request);
    
    /** 执行员提交处理结果时更新子表 */
    void onSubmitResult(OpRequestDO request, OpRequestSubmitResultReqVO vo);
    
    /** 流程终态 APPROVE 后处理（如合同→SIGNED） */
    void onClosed(OpRequestDO request);
    
    /** 获取详情时填充子表数据到 RespVO */
    void fillDetail(OpRequestDO request, OpRequestRespVO respVO);
}
```

**策略注册与路由**：
```java
// OpRequestServiceImpl 中通过 Spring 自动注入所有策略 Bean
private final Map<String, OpRequestTypeHandler> handlerMap;

public OpRequestServiceImpl(List<OpRequestTypeHandler> handlers) {
    this.handlerMap = handlers.stream()
        .collect(Collectors.toMap(OpRequestTypeHandler::getRequestType, Function.identity()));
}

private OpRequestTypeHandler getHandler(String requestType) {
    OpRequestTypeHandler handler = handlerMap.get(requestType);
    if (handler == null) throw exception(OP_REQUEST_TYPE_NOT_SUPPORTED);
    return handler;
}
```

**SigningRequestHandler 实现要点**：
- `onCreate`：写入 `ops_op_request_signing` 子表（contractId、contractCode、contractName）
- `onStart`：更新合同 `subStatus` → `signing`
- `onSubmitResult`：通过附件服务上传盖章文件到 `ops_cs_attachment`，关联合同编码
- `onClosed`：更新合同 status→SIGNED、signDate=今天、清空 subStatus
- `fillDetail`：查询子表填充到 RespVO

### 6. 操作请求 Controller（P0）

**新建文件**：
- `yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/controller/admin/oprequest/OpRequestController.java`

**设计逻辑**：

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 创建操作请求 | `POST` | `/opshub/op-request/create` | `dealer:op-request:create` | 经销商发起，写主表+子表，发起 BPM |
| 操作请求分页 | `GET` | `/opshub/op-request/page` | `dealer:op-request:query` | 分页查询 |
| 操作请求详情 | `GET` | `/opshub/op-request/get` | `dealer:op-request:query` | 主表+子表联合返回 |
| 提交处理结果 | `POST` | `/opshub/op-request/submit-result` | `dealer:op-request:process` | 执行员提交（如上传盖章文件），推动 BPM → DELIVERED |
| 经销商验收 | `POST` | `/opshub/op-request/verify` | `dealer:op-request:verify` | 经销商验收通过/拒绝，推动 BPM → CLOSED/REJECTED |

### 7. BPM 状态回调监听器（P0）

**新建文件**：
- `yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/service/oprequest/listener/OpRequestBpmStatusListener.java`

**设计逻辑**：
- 继承 `BpmProcessInstanceStatusEventListener`
- 监听 `processDefinitionKey = "ops-op-request"`
- 回调 → `opRequestService.updateOpRequestStatusByBpm(id, status)`

```java
@Override
protected String getProcessDefinitionKey() {
    return OpRequestServiceImpl.PROCESS_KEY;
}

@Override
protected void onEvent(BpmProcessInstanceStatusEvent event) {
    Long requestId = Long.parseLong(event.getBusinessKey());
    opRequestService.updateOpRequestStatusByBpm(requestId, event.getStatus());
}
```

### 8. 签约模块改造：删除旧接口（P0）

**改动文件**：
- `SigningContractController.java` — 删除 `/sign` 和 `/upload-sign-proof` 接口
- `SigningContractService.java` — 删除 `signContract` 和 `uploadSignProof` 方法声明
- `SigningContractServiceImpl.java` — 删除 `signContract` 和 `uploadSignProof` 实现

**设计逻辑**：
- 签署发起和盖章文件上传全部通过操作请求统一入口
- 签约合同的状态更新由 BPM 回调自动完成（`updateOpRequestStatusByBpm` 内部调用 `SigningContractMapper`）
- 签约进度页的"去签署"按钮改为调用 `POST /opshub/op-request/create`
- "上传盖章文件"按钮改为调用 `POST /opshub/op-request/submit-result`

### 9. 操作请求 VO（P0）

**新建文件**：
- `OpRequestCreateReqVO.java` — requestType(OpRequestTypeEnum)、dealerId、dealerCode、remark + 子表字段（signing: contractId）
- `OpRequestPageReqVO.java` — 继承 PageParam，requestType(OpRequestTypeEnum)、requestStatus、dealerCode、keyword
- `OpRequestRespVO.java` — 主表全字段 + requestType(OpRequestTypeEnum) + 子表字段平铺
- `OpRequestSubmitResultReqVO.java` — id + 子表处理结果字段（signing: 附件上传，复用现有附件组件）
- `OpRequestVerifyReqVO.java` — id + passed(Boolean) + rejectReason

### 10. 前端 — 统一操作请求页面组件（P0）

**新建文件**：
- `src/views/opshub/oprequest/components/CreateOpRequestModal.vue` — 统一创建弹窗，根据 requestType 动态渲染不同子表单
- `src/views/opshub/oprequest/components/ProcessOpRequestModal.vue` — 统一处理弹窗，执行员提交处理结果
- `src/views/opshub/oprequest/components/VerifyOpRequestModal.vue` — 统一验收弹窗，经销商验收通过/拒绝
- `src/views/opshub/oprequest/components/forms/SigningForm.vue` — 签约类型子表单（本期实现）
- `src/views/opshub/oprequest/components/forms/PaymentForm.vue` — 付款类型子表单（后续扩展）
- `src/views/opshub/oprequest/components/forms/InvoiceForm.vue` — 开票类型子表单（后续扩展）
- `src/views/opshub/oprequest/components/forms/ReturnForm.vue` — 退货类型子表单（后续扩展）

**改动文件**：
- `src/api/opshub/oprequest/index.ts` — 新增操作请求 API
- `src/api/opshub/signing/index.ts` — 删除 `signContract`、`uploadSignProof`，改用 op-request API
- `src/views/opshub/signing/index.vue` — 引入统一弹窗组件，删除旧弹窗引用
- `src/views/opshub/signing/components/ContractTable.vue` — 增加"验收""流程详情"按钮

**设计逻辑**：
- 统一组件通过 `requestType` prop 控制渲染哪个子表单
- `CreateOpRequestModal` 打开时传入 `requestType`，动态加载对应的 `forms/XxxForm.vue`
- `ProcessOpRequestModal` 根据 requestType 渲染不同的处理表单字段（如签约显示盖章文件上传）
- `VerifyOpRequestModal` 所有类型共用，只需 passed + rejectReason
- 签约进度页引入这三个统一组件，传入 `requestType="signing"`，嵌入感无差别

```
<CreateOpRequestModal ref="createModalRef" @success="getList" />
<ProcessOpRequestModal ref="processModalRef" @success="getList" />
<VerifyOpRequestModal ref="verifyModalRef" @success="getList" />
```

**已删除前端组件**：
- `SignRequestModal.vue` — 被 `CreateOpRequestModal` 替代
- `UploadSignProofModal.vue` — 被 `ProcessOpRequestModal` 替代

## 页面元素规格

### 签约进度列表页 — 操作列（变更）

| 按钮 | 显示条件 | 变更说明 |
|------|---------|----------|
| 去签署 | `status=unsigned && subStatus=pending` | 改为 `POST /opshub/op-request/create`（type=signing），进入 WAITING |
| 上传盖章文件 | `status=unsigned && subStatus=signing` | 改为 `POST /opshub/op-request/submit-result`，执行员提交处理结果 |
| 验收 | 关联操作请求 status=DELIVERED | **新增**，经销商验收通过/拒绝 |
| 流程详情 | 关联操作请求 processInstanceId 存在 | **新增**，跳转 BPM 审批详情页 |

### 确认签署弹窗（SignRequestModal 变更）

#### Form 表单（输入项）

| 字段名 | 元素类型 | 数据类型 | 取值范围 | 必填 | 读写 | 说明 |
|--------|----------|----------|----------|------|------|------|
| contractCode | text | String | 自动回填 | — | 只读 | 合同编码 |
| contractName | text | String | 自动回填 | — | 只读 | 合同名称 |
| contractTypeName | text | String | 自动回填 | — | 只读 | 合同类型 |
| remark | textarea | String | max=500 | 否 | 可写 | 签署备注 |

## API 接口定义

### 接口 1：创建操作请求

| 项 | 值 |
|------|------|
| 方法 | `POST` |
| 路径 | `/opshub/op-request/create` |
| 权限 | `dealer:op-request:create` |
| 说明 | 创建操作请求 + 写入子表 + 发起 BPM |

**请求参数**（Body JSON）：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| requestType | OpRequestTypeEnum | 是 | 类型：signing/payment/invoice/return |
| dealerId | Long | 是 | 经销商 ID |
| dealerCode | String | 否 | 经销商编码 |
| contractId | Long | 条件必填 | 合同 ID（requestType=signing 时必填） |
| remark | String | 否 | 备注，max=500 |

> 后续扩展 payment/invoice/return 时，在此 VO 中增加对应的条件必填字段。

**响应**（`CommonResult<Long>`）：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| data | Long | 新建操作请求 ID |

### 接口 2：操作请求分页

| 项 | 值 |
|------|------|
| 方法 | `GET` |
| 路径 | `/opshub/op-request/page` |
| 权限 | `dealer:op-request:query` |
| 说明 | 分页查询操作请求 |

**请求参数**（Query）：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNo | Integer | 是 | 页码，默认 1 |
| pageSize | Integer | 是 | 每页条数，默认 10 |
| requestType | OpRequestTypeEnum | 否 | 类型筛选 |
| requestStatus | String | 否 | 状态筛选 |
| dealerCode | String | 否 | 经销商编码 |
| keyword | String | 否 | 请求编号/编码模糊搜索 |

**响应**（`CommonResult<PageResult<OpRequestRespVO>>`）：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| data.list | List | 操作请求列表 |
| data.total | Long | 总条数 |

### 接口 3：操作请求详情

| 项 | 值 |
|------|------|
| 方法 | `GET` |
| 路径 | `/opshub/op-request/get` |
| 权限 | `dealer:op-request:query` |

**请求参数**（Query）：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 操作请求 ID |

**响应**（`CommonResult<OpRequestRespVO>`）

### 接口 4：提交处理结果

| 项 | 值 |
|------|------|
| 方法 | `POST` |
| 路径 | `/opshub/op-request/submit-result` |
| 权限 | `dealer:op-request:process` |
| 说明 | 执行员提交处理结果，推动 BPM → DELIVERED |

**请求参数**（Body JSON）：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 操作请求 ID |
| attachments | List | 条件必填 | 盖章文件附件（requestType=signing 时必填，复用现有附件上传组件） |

**响应**（`CommonResult<Boolean>`）

### 接口 5：经销商验收

| 项 | 值 |
|------|------|
| 方法 | `POST` |
| 路径 | `/opshub/op-request/verify` |
| 权限 | `dealer:op-request:verify` |
| 说明 | 经销商验收通过/拒绝 |

**请求参数**（Body JSON）：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 操作请求 ID |
| passed | Boolean | 是 | true=验收通过，false=验收不通过 |
| rejectReason | String | 否 | 不通过时的原因 |

**响应**（`CommonResult<Boolean>`）

### 已删除接口

| 接口 | 原路径 | 替代方案 |
|------|---------|----------|
| 经销商发起签署 | `POST /opshub/signing/sign` | 改用 `POST /opshub/op-request/create`（type=signing） |
| 执行员上传盖章文件 | `POST /opshub/signing/upload-sign-proof` | 改用 `POST /opshub/op-request/submit-result` |

## DDL 变更

**文件路径**：`db/branches/feature_step20-操作请求工作流/feature_step20-操作请求工作流_ddl.sql`

```sql
-- 主表
CREATE TABLE ops_op_request (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_no      VARCHAR(32) NOT NULL COMMENT '请求编号（自动生成）',
    request_type    VARCHAR(32) NOT NULL COMMENT '类型：signing/payment/invoice/return',
    request_type_name VARCHAR(64) COMMENT '类型中文名',
    dealer_id       BIGINT NOT NULL COMMENT '经销商ID',
    dealer_code     VARCHAR(32) COMMENT '经销商编码',
    request_status  VARCHAR(16) NOT NULL DEFAULT 'waiting' COMMENT '状态：waiting/in_progress/delivered/closed/rejected/cancel',
    process_instance_id VARCHAR(64) COMMENT 'BPM流程实例ID',
    assignee_id     BIGINT COMMENT '当前处理人（执行员）ID',
    remark          VARCHAR(500) COMMENT '备注',
    tenant_id       BIGINT NOT NULL DEFAULT 0,
    creator         VARCHAR(64) DEFAULT '',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater         VARCHAR(64) DEFAULT '',
    update_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         BIT(1) NOT NULL DEFAULT 0
) COMMENT '操作请求主表';

CREATE INDEX idx_op_request_type ON ops_op_request(request_type);
CREATE INDEX idx_op_request_dealer ON ops_op_request(dealer_code);
CREATE INDEX idx_op_request_status ON ops_op_request(request_status);

-- 签约子表
CREATE TABLE ops_op_request_signing (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id      BIGINT NOT NULL COMMENT '关联 ops_op_request.id',
    contract_id     BIGINT NOT NULL COMMENT '关联 ops_signing_contract.id',
    contract_code   VARCHAR(64) COMMENT '合同编码',
    contract_name   VARCHAR(128) COMMENT '合同名称',
    tenant_id       BIGINT NOT NULL DEFAULT 0,
    creator         VARCHAR(64) DEFAULT '',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater         VARCHAR(64) DEFAULT '',
    update_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         BIT(1) NOT NULL DEFAULT 0
) COMMENT '操作请求-签约子表';

CREATE INDEX idx_op_req_signing_request ON ops_op_request_signing(request_id);
CREATE INDEX idx_op_req_signing_contract ON ops_op_request_signing(contract_id);
```

## DML 变更

需要在 BPM 流程定义管理后台配置流程：
- 流程 Key：`ops-op-request`
- 流程名称：`操作请求审批`
- 流程设计：发起人节点（经销商）→ 办理人节点（执行员处理）→ 审批节点（经销商验收）→ 结束节点
- 流程变量：`requestType` 用于后续网关路由扩展

菜单权限 DML：

```sql
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, deleted) VALUES
(6050, '操作请求管理', '',                2, 20, 6000, 'op-request', 'ep:document-checked', 'opshub/oprequest/index', 'OpshubOpRequest', 0, true, true, true, 0),
(6051, '创建操作请求', 'dealer:op-request:create', 3, 1, 6050, '', '', '', NULL, 0, true, true, true, 0),
(6052, '查询操作请求', 'dealer:op-request:query',  3, 2, 6050, '', '', '', NULL, 0, true, true, true, 0),
(6053, '处理操作请求', 'dealer:op-request:process', 3, 3, 6050, '', '', '', NULL, 0, true, true, true, 0),
(6054, '验收操作请求', 'dealer:op-request:verify',  3, 4, 6050, '', '', '', NULL, 0, true, true, true, 0);
```

## 涉及文件清单

### 后端（yudao-module-opshub）
| 文件 | 操作 |
|------|------|
| `dal/dataobject/oprequest/OpRequestDO.java` | 新增 |
| `dal/dataobject/oprequest/OpRequestSigningDO.java` | 新增 |
| `dal/mysql/oprequest/OpRequestMapper.java` | 新增 |
| `dal/mysql/oprequest/OpRequestSigningMapper.java` | 新增 |
| `enums/OpRequestTypeEnum.java` | 新增 |
| `enums/OpRequestStatusEnum.java` | 新增 |
| `service/oprequest/OpRequestService.java` | 新增 |
| `service/oprequest/impl/OpRequestServiceImpl.java` | 新增 |
| `service/oprequest/handler/OpRequestTypeHandler.java` | 新增：策略接口 |
| `service/oprequest/handler/SigningRequestHandler.java` | 新增：签约类型策略实现 |
| `service/oprequest/listener/OpRequestBpmStatusListener.java` | 新增 |
| `controller/admin/oprequest/OpRequestController.java` | 新增 |
| `controller/admin/oprequest/vo/OpRequestCreateReqVO.java` | 新增 |
| `controller/admin/oprequest/vo/OpRequestPageReqVO.java` | 新增 |
| `controller/admin/oprequest/vo/OpRequestRespVO.java` | 新增 |
| `controller/admin/oprequest/vo/OpRequestSubmitResultReqVO.java` | 新增 |
| `controller/admin/oprequest/vo/OpRequestVerifyReqVO.java` | 新增 |
| `controller/admin/signing/SigningContractController.java` | 修改：删除 `/sign` 和 `/upload-sign-proof` 接口 |
| `service/signing/SigningContractService.java` | 修改：删除 `signContract`、`uploadSignProof` 方法 |
| `service/signing/impl/SigningContractServiceImpl.java` | 修改：删除 `signContract`、`uploadSignProof` 实现 |
| `enums/ErrorCodeConstants.java` | 修改：新增操作请求相关错误码 |

### 前端（yudao-ui/yudao-ui-admin-vue3）
| 文件 | 操作 |
|------|------|
| `src/api/opshub/oprequest/index.ts` | 新增：操作请求 API |
| `src/views/opshub/oprequest/components/CreateOpRequestModal.vue` | 新增：统一创建弹窗 |
| `src/views/opshub/oprequest/components/ProcessOpRequestModal.vue` | 新增：统一处理弹窗 |
| `src/views/opshub/oprequest/components/VerifyOpRequestModal.vue` | 新增：统一验收弹窗 |
| `src/views/opshub/oprequest/components/forms/SigningForm.vue` | 新增：签约子表单 |
| `src/views/opshub/signing/index.vue` | 修改：引入统一弹窗组件，删除旧弹窗引用 |
| `src/views/opshub/signing/components/ContractTable.vue` | 修改：增加"验收""流程详情"按钮 |
| `src/views/opshub/signing/components/SignRequestModal.vue` | 删除：被 CreateOpRequestModal 替代 |
| `src/views/opshub/signing/components/UploadSignProofModal.vue` | 删除：被 ProcessOpRequestModal 替代 |
| `src/api/opshub/signing/index.ts` | 修改：删除 signContract/uploadSignProof |

### DDL/DML
| 文件 | 操作 |
|------|------|
| `db/branches/feature_step20-操作请求工作流/feature_step20-操作请求工作流_ddl.sql` | 新建 |
| `db/branches/feature_step20-操作请求工作流/feature_step20-操作请求工作流_dml.sql` | 新建 |

## 已知限制

1. BPM 流程定义 `ops-op-request` 需在管理后台手动配置
2. 本期仅实现 `signing` 类型及其子表，其余 3 类子表在后续 Step 中扩展
3. 各类型子表的字段在后续实现时定义

## 剩余待做（P2/P3）

| 优先级 | 任务 | 说明 |
|-------|------|------|
| P1 | 申请付款 | 新建 `ops_op_request_payment` 子表 + `PaymentRequestHandler` 策略实现 |
| P1 | 申请开票 | 新建 `ops_op_request_invoice` 子表 + `InvoiceRequestHandler` 策略实现 |
| P1 | 申请退货 | 新建 `ops_op_request_return` 子表 + `ReturnRequestHandler` 策略实现 |
| P2 | 操作请求列表页 | 独立的 op-request 管理页面（本期仅在签约页嵌入） |
| P2 | 操作请求通知 | BPM 回调后发送 WebSocket/站内信通知审批人 |
| P3 | 批量操作 | 批量签署/付款等走 BPM |
