# Task-Step20-操作请求工作流

> PRD 来源：`docs/PRD-Step20-签约工作流改造.md`

## Task 1：数据库建表（DDL/DML）

**文件**：
- `db/branches/feature_step20-操作请求工作流/feature_step20-操作请求工作流_ddl.sql`（新建）
- `db/branches/feature_step20-操作请求工作流/feature_step20-操作请求工作流_dml.sql`（新建）

**实施内容**：
1. 创建 `ops_op_request` 主表（含 tenant_id、creator、create_time、updater、update_time、deleted）
2. 创建索引：`idx_op_request_type`、`idx_op_request_dealer`、`idx_op_request_status`
3. 创建 `ops_op_request_signing` 子表
4. 创建索引：`idx_op_req_signing_request`、`idx_op_req_signing_contract`
5. 插入菜单权限 DML（menu_id: 6050-6054）

**验证**：DDL 在开发数据库执行成功，无报错。

---

## Task 2：DO + Mapper + 枚举

**新建文件**（基础包 `cn.iocoder.yudao.module.opshub`）：
- `dal/dataobject/oprequest/OpRequestDO.java`
- `dal/dataobject/oprequest/OpRequestSigningDO.java`
- `dal/mysql/oprequest/OpRequestMapper.java`
- `dal/mysql/oprequest/OpRequestSigningMapper.java`
- `enums/OpRequestTypeEnum.java`
- `enums/OpRequestStatusEnum.java`
- `enums/ErrorCodeConstants.java`（修改：新增 OP_REQUEST_NOT_EXISTS、OP_REQUEST_TYPE_NOT_SUPPORTED、OP_REQUEST_STATUS_INVALID）

**实施要点**：
- OpRequestDO：继承 TenantBaseDO，字段：id, requestNo, requestType(OpRequestTypeEnum), requestTypeName, dealerId, dealerCode, requestStatus, processInstanceId, assigneeId, remark
- OpRequestSigningDO：继承 TenantBaseDO，字段：id, requestId, contractId, contractCode, contractName
- OpRequestMapper：继承 BaseMapperX，提供 selectPage 分页查询
- OpRequestSigningMapper：继承 BaseMapperX，提供 selectByRequestId
- OpRequestStatusEnum：waiting/in_progress/delivered/closed/rejected/cancel
- OpRequestTypeEnum：signing/payment/invoice/return

**验证**：`mvn compile -pl yudao-module-opshub` 编译通过。

---

## Task 3：策略模式 — Handler 接口 + SigningRequestHandler

**新建文件**：
- `service/oprequest/handler/OpRequestTypeHandler.java`（策略接口）
- `service/oprequest/handler/SigningRequestHandler.java`（签约类型策略实现）

**策略接口方法**：
```java
public interface OpRequestTypeHandler {
    String getRequestType();
    void onCreate(OpRequestDO request, OpRequestCreateReqVO vo);
    void onStart(OpRequestDO request);
    void onSubmitResult(OpRequestDO request, OpRequestSubmitResultReqVO vo);
    void onClosed(OpRequestDO request);
    void fillDetail(OpRequestDO request, OpRequestRespVO respVO);
}
```

**SigningRequestHandler 实现**：
- `onCreate`：插入 ops_op_request_signing 子表（contractId、contractCode、contractName）
- `onStart`：更新签约合同 subStatus→signing
- `onSubmitResult`：通过附件服务保存盖章文件到 ops_cs_attachment
- `onClosed`：更新签约合同 status→SIGNED、signDate=今天、清空 subStatus
- `fillDetail`：查询子表填充到 RespVO

**验证**：`mvn compile -pl yudao-module-opshub` 编译通过。

---

## Task 4：Service + BPM Listener

**新建文件**：
- `service/oprequest/OpRequestService.java`（接口）
- `service/oprequest/impl/OpRequestServiceImpl.java`（实现）
- `service/oprequest/listener/OpRequestBpmStatusListener.java`（BPM 回调）

**OpRequestServiceImpl 方法**：
- `PROCESS_KEY = "ops-op-request"`
- `createOpRequest`：生成编号 → 插主表 → handler.onCreate → createProcessInstance → 回写 processInstanceId → handler.onStart
- `getOpRequestPage`：分页查询
- `getOpRequest`：详情（主表 + handler.fillDetail）
- `submitResult`：校验状态 → handler.onSubmitResult → approveCurrentBpmTask → 设 requestStatus=DELIVERED
- `verifyRequest`：校验 DELIVERED → passed=true 则 approveCurrentBpmTask，passed=false 则 rejectCurrentBpmTask
- `updateOpRequestStatusByBpm`：APPROVE→CLOSED+handler.onClosed, REJECT→REJECTED, CANCEL→CANCEL

**策略路由**：
```java
private final Map<String, OpRequestTypeHandler> handlerMap;
// 构造函数通过 Spring 注入 List<OpRequestTypeHandler> 并 collect toMap
```

**OpRequestBpmStatusListener**：
- 继承 BpmProcessInstanceStatusEventListener
- getProcessDefinitionKey() → "ops-op-request"
- onEvent → opRequestService.updateOpRequestStatusByBpm(id, bpmStatus)

**验证**：`mvn compile -pl yudao-module-opshub` 编译通过。

---

## Task 5：Controller + VO

**新建文件**：
- `controller/admin/oprequest/OpRequestController.java`
- `controller/admin/oprequest/vo/OpRequestCreateReqVO.java`
- `controller/admin/oprequest/vo/OpRequestPageReqVO.java`
- `controller/admin/oprequest/vo/OpRequestRespVO.java`
- `controller/admin/oprequest/vo/OpRequestSubmitResultReqVO.java`
- `controller/admin/oprequest/vo/OpRequestVerifyReqVO.java`

**接口清单**：
| 方法 | 路径 | 权限 |
|------|------|------|
| POST | /opshub/op-request/create | dealer:op-request:create |
| GET | /opshub/op-request/page | dealer:op-request:query |
| GET | /opshub/op-request/get | dealer:op-request:query |
| POST | /opshub/op-request/submit-result | dealer:op-request:process |
| POST | /opshub/op-request/verify | dealer:op-request:verify |

**验证**：`mvn compile -pl yudao-module-opshub` 编译通过；启动后 Swagger 可见新接口。

---

## Task 6：删除签约旧接口

**改动文件**：
- `controller/admin/signing/SigningContractController.java` — 删除 `signContract` 和 `uploadSignProof` 方法
- `service/signing/SigningContractService.java` — 删除方法声明
- `service/signing/impl/SigningContractServiceImpl.java` — 删除方法实现

**注意**：仅删除签署发起和盖章文件上传，保留其他接口（如列表查询、详情等）。

**验证**：`mvn compile -pl yudao-module-opshub` 编译通过；确认无其他代码引用被删方法。

---

## Task 7：后端编译验证

**命令**：
```bash
mvn clean compile -pl yudao-module-opshub -am
```

**验证项**：
- 编译无报错
- 新增的 Bean 能被 Spring 正常注入
- 无循环依赖问题

---

## Task 8：前端 — API 层 + 统一组件

**新建文件**：
- `src/api/opshub/oprequest/index.ts` — 操作请求 API（createOpRequest, getOpRequestPage, getOpRequest, submitResult, verifyRequest）
- `src/views/opshub/oprequest/components/CreateOpRequestModal.vue` — 统一创建弹窗
- `src/views/opshub/oprequest/components/ProcessOpRequestModal.vue` — 统一处理弹窗
- `src/views/opshub/oprequest/components/VerifyOpRequestModal.vue` — 统一验收弹窗
- `src/views/opshub/oprequest/components/forms/SigningForm.vue` — 签约子表单

**组件设计**：
- CreateOpRequestModal：接收 requestType prop，动态渲染 forms/ 下对应子表单
- ProcessOpRequestModal：根据 requestType 渲染不同处理表单（签约：附件上传）
- VerifyOpRequestModal：通用，只需 passed + rejectReason

**验证**：`pnpm dev` 启动无报错。

---

## Task 9：前端 — 签约进度页改造

**改动文件**：
- `src/views/opshub/signing/index.vue` — 引入统一弹窗组件，删除旧弹窗引用
- `src/views/opshub/signing/components/ContractTable.vue` — 操作列改造
- `src/api/opshub/signing/index.ts` — 删除 signContract/uploadSignProof

**删除文件**：
- `src/views/opshub/signing/components/SignRequestModal.vue`
- `src/views/opshub/signing/components/UploadSignProofModal.vue`

**ContractTable 操作列变更**：
| 按钮 | 条件 | 动作 |
|------|------|------|
| 去签署 | status=unsigned && subStatus=pending | 打开 CreateOpRequestModal(type=signing) |
| 上传盖章文件 | status=unsigned && subStatus=signing | 打开 ProcessOpRequestModal |
| 验收 | 关联操作请求 requestStatus=delivered | 打开 VerifyOpRequestModal |
| 流程详情 | processInstanceId 存在 | 跳转 BPM 审批详情页 |

**验证**：页面功能与 PRD 页面元素规格一致，按钮显示/隐藏逻辑正确。

---

## Task 10：端到端验证

**测试场景**（签约类型完整流程）：
1. 经销商在签约进度页点击"去签署" → 确认弹窗 → 创建操作请求（requestStatus=waiting）
2. 执行员接单 → 点击"上传盖章文件" → 上传附件 → 提交（requestStatus=delivered）
3. 经销商收到通知 → 点击"验收" → 通过 → 合同自动变为 SIGNED
4. 检查 BPM 流程详情页可正常查看流转记录
5. 验收拒绝场景：经销商拒绝 → requestStatus=rejected → 合同保持原状

---

## 实施顺序

```
Task 1 (DDL/DML)
  ↓
Task 2 (DO/Mapper/Enum) → 编译验证
  ↓
Task 3 (策略接口+SigningHandler) → 编译验证
  ↓
Task 4 (Service+Listener) → 编译验证
  ↓
Task 5 (Controller+VO) → 编译验证
  ↓
Task 6 (删除旧接口) → Task 7 (全量编译验证)
  ↓
Task 8 (前端API+组件) → dev 启动验证
  ↓
Task 9 (签约页改造) → dev 启动验证
  ↓
Task 10 (端到端验证)
```
