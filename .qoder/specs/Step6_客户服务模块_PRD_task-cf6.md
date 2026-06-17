# Step 6 — 客户服务模块实施 PRD

> **版本**: V1.0 | **日期**: 2026-06-16  
> **文档性质**: 分阶段实施 PRD — Step 6（客户服务模块）  
> **前置文档**: `docs/PRD-Step5-售后模块.md`（Step 5）、`docs/PRD-Step4-订单模块.md`（Step 4）、`docs/PRD-Step3-签约进度模块.md`（Step 3）、`docs/PRD-Step2-基础数据模块.md`（Step 2）、`docs/PRD-Step1-角色菜单经销商产品线.md`（Step 1）  
> **输入来源**: PRD V2.0 客户服务模块（第九章） + PRD V1.0 模块六 + 用户确认决策 + 现有代码审计

---

## 一、Step 6 目标

在 Step 1-5 基础设施与业务模块之上，实现**客户服务模块**的工单增强 + 操作请求 + 通用附件三大能力：

| 目标 | 说明 |
|------|------|
| 工单增强 | 现有 `ops_cs_task` 表新增 4 个业务字段（productLineCode/productLineName/sourceModule/dealerName），补齐 DDL 脚本，增强分页查询筛选维度 |
| 操作请求管理 | 新建 `ops_cs_opreq` 表，支持 5 种操作类型（签署/验款/开票/退货/盖章）的发起→接单→提交→验收完整工作流 |
| 通用附件表 | 新建 `ops_cs_attachment` 表，统一存储系统所有模块的附件（工单凭证、操作请求附件等） |
| 4 状态操作请求 | PENDING(0)/IN_PROGRESS(1)/DELIVERED(2)/CLOSED(3) — 无退回状态 |
| 编码规则 | 操作请求编号 `OPR-{YYYYMMDD}-{seq}`（如 `OPR-20260616-001`） |
| 数据权限 | `ops_cs_task` 和 `ops_cs_opreq` 均注册 dealer_code + product_line_code 到 `DealerDataPermissionRule` |
| 权限标识统一 | 工单 `dealer:cs-task:*`、咨询 `dealer:cs-consult:*`（预留）、操作请求 `dealer:cs-opreq:*` |

**本阶段不包含**：咨询队列和在线聊天（权限预留但功能不实现）、操作请求状态同步到其他模块、BPM 审批流程集成（操作请求采用简单工作流）。

---

## 二、数据模型

### 2.1 工单主表增强 — ops_cs_task（ALTER TABLE）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|:---:|------|
| `dealer_name` | varchar(100) | N | 经销商名称（冗余存储） |
| `product_line_code` | varchar(50) | N | 产品线编码（数据权限用） |
| `product_line_name` | varchar(100) | N | 产品线名称（冗余存储） |
| `source_module` | varchar(20) | N | 来源模块：`aftersale`/`order`/`signing`/`basedata`/`manual` |

新增索引：`idx_ops_cs_task_dealer_code`(dealer_code)、`idx_ops_cs_task_pl_code`(product_line_code)、`idx_ops_cs_task_status`(status)

### 2.2 操作请求主表 — ops_cs_opreq（CREATE TABLE）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|:---:|------|
| `id` | int8 | PK | 主键（序列 `ops_cs_opreq_seq`） |
| `opreq_code` | varchar(30) | Y | 操作请求编号（唯一，`OPR-20260616-001`） |
| `op_type` | varchar(20) | Y | 操作类型：`sign`/`payment`/`invoice`/`return`/`stamp` |
| `status` | integer | Y | 状态：0=待处理 1=处理中 2=等待验收 3=已完成 |
| `dealer_code` | varchar(50) | Y | 经销商编码（数据权限用） |
| `dealer_name` | varchar(100) | Y | 经销商名称 |
| `product_line_code` | varchar(50) | N | 产品线编码（可空） |
| `product_line_name` | varchar(100) | N | 产品线名称 |
| `source_module` | varchar(20) | Y | 来源模块：`signing`/`order`/`aftersale`/`basedata` |
| `source_id` | int8 | N | 来源业务 ID |
| `source_code` | varchar(50) | N | 来源业务编号（展示用） |
| `content` | varchar(500) | Y | 请求内容描述 |
| `creator_user_id` | int8 | Y | 发起人用户 ID |
| `assignee_id` | int8 | N | 当前处理人用户 ID |
| `accept_time` | timestamp | N | 接单时间 |
| `submit_time` | timestamp | N | 提交时间 |
| `submit_remark` | varchar(500) | N | 处理留言 |
| `verify_time` | timestamp | N | 验收时间 |
| `completed_time` | timestamp | N | 完成时间 |
| `remark` | varchar(500) | N | 备注 |
| 标准字段 | | | creator, create_time, updater, update_time, deleted, tenant_id |

索引：`uk_ops_cs_opreq_code`(opreq_code UNIQUE)、`idx_ops_cs_opreq_dealer_code`、`idx_ops_cs_opreq_pl_code`、`idx_ops_cs_opreq_status`、`idx_ops_cs_opreq_type`、`idx_ops_cs_opreq_source`(source_module, source_id)、`idx_ops_cs_opreq_creator`、`idx_ops_cs_opreq_assignee`

### 2.3 通用附件表 — ops_cs_attachment（CREATE TABLE）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|:---:|------|
| `id` | int8 | PK | 主键（序列 `ops_cs_attachment_seq`） |
| `module` | varchar(20) | Y | 关联模块：`task`/`opreq`/`basedata`/`signing`/`order`/`aftersale` |
| `business_id` | int8 | Y | 业务 ID |
| `business_code` | varchar(50) | N | 业务编号（冗余存储） |
| `file_name` | varchar(200) | Y | 原始文件名 |
| `file_url` | varchar(500) | Y | 文件 URL |
| `file_size` | int8 | N | 文件大小（字节） |
| `file_type` | varchar(100) | N | MIME 类型 |
| `remark` | varchar(500) | N | 备注 |
| 标准字段 | | | creator, create_time, updater, update_time, deleted, tenant_id |

索引：`idx_ops_cs_attachment_module_biz`(module, business_id)、`idx_ops_cs_attachment_biz_code`(business_code)

### 2.4 操作请求状态流转

```
PENDING(0) → IN_PROGRESS(1) → DELIVERED(2) → CLOSED(3)
  待处理       处理中          等待验收         已完成
```

操作请求无退回状态，退回需求通过工单系统处理。

### 2.5 枚举定义

```java
// CsOpReqTypeEnum
SIGN("sign", "签署完成"), PAYMENT("payment", "付款完成"), INVOICE("invoice", "开票完成"),
RETURN("return", "退货完成"), STAMP("stamp", "盖章完成");

// CsOpReqStatusEnum
PENDING(0, "待处理"), IN_PROGRESS(1, "处理中"), DELIVERED(2, "等待验收"), CLOSED(3, "已完成");

// CsAttachmentModuleEnum
TASK("task", "工单"), OPREQ("opreq", "操作请求"), BASEDATA("basedata", "基础数据"),
SIGNING("signing", "签约进度"), ORDER("order", "订单"), AFTERSALE("aftersale", "售后");

// CsSourceModuleEnum
AFTERSALE("aftersale", "售后"), ORDER("order", "订单"), SIGNING("signing", "签约进度"),
BASEDATA("basedata", "基础数据"), MANUAL("manual", "手动创建");
```

### 2.6 数据可见性规则（工单 + 操作请求共用）

在 DealerDataPermissionRule（dealer_code + product_line_code）基础之上，Service 层按角色追加 **用户级可见性过滤**：

| 角色 | 工单 (`ops_cs_task`) 可见范围 | 操作请求 (`ops_cs_opreq`) 可见范围 |
|------|------|------|
| **品牌管理员** (`brand_admin`) | 全部（仅受 dealer_code 数据权限约束） | 全部 |
| **品牌销售员** (`brand_sales`) | 全部（仅受 dealer_code 数据权限约束） | 全部 |
| **经销商代理人** (`dealer`) | 仅自己提交的：`creator_user_id = 当前用户ID` | 仅自己发起的：`creator_user_id = 当前用户ID` |
| **服务单执行员** (`service_executor`) | 未接单的 + 自己是处理人的：`status = PENDING(0) OR assignee_id = 当前用户ID` | 未接单的 + 自己是处理人的：`status = PENDING(0) OR assignee_id = 当前用户ID` |

**SQL 逻辑（工单表示例）**：
```sql
-- 管理员/销售员：无额外过滤（仅 DealerDataPermissionRule 生效）
SELECT * FROM ops_cs_task WHERE ... 

-- 经销商代理人：
SELECT * FROM ops_cs_task WHERE ... AND creator_user_id = #{currentUserId}

-- 执行员：
SELECT * FROM ops_cs_task WHERE ... AND (status = 0 OR assignee_id = #{currentUserId})
```

**实现位置**：在 `CsTaskServiceImpl.getCsTaskPage()` 和 `CsOpReqServiceImpl.getOpReqPage()` 中，根据当前用户角色动态注入过滤条件（通过 PageReqVO 传递 `viewScope` 参数）。

> **与 DealerDataPermissionRule 的关系**：用户级可见性过滤在 Service 层执行，DealerDataPermissionRule 在 SQL 拦截器层执行，两者 **AND 叠加**。即执行员既受产品线数据权限约束，又只能看到未接单 + 自己的工单。

### 2.7 设计决策

| 决策项 | 选择 | 理由 |
|--------|------|------|
| 操作请求 4 状态（无退回） | 退回通过工单系统处理 | 用户确认 |
| 操作请求不走 BPM | 简单工作流 | 操作场景简单（发起→接单→提交→验收） |
| 附件统一表 | `ops_cs_attachment` 全局复用 | 用户确认：后续所有模块附件统一存储 |
| product_line_code 可空 | 部分操作不涉及产品线 | 如盖章、数据请求 |
| 权限标识分三组 | `dealer:cs-task:*` / `dealer:cs-consult:*` / `dealer:cs-opreq:*` | 用户确认 |
| 用户级可见性在 Service 层 | 不在 DataPermission 层 | DataPermission 是 dealer_code 维度，用户级过滤是 userId 维度，两层独立叠加更清晰 |
| 执行员看未接单工单 | `status=PENDING OR assignee_id=me` | 执行员需要看到待接单工单才能接单 |

---

## 三、后端实现

### Task 1: 工单增强 — 修改现有代码

**修改文件**：

| # | 文件 | 修改内容 |
|---|------|---------|
| 1 | `CsTaskDO.java` | 新增 dealerName, productLineCode, productLineName, sourceModule 字段 |
| 2 | `CsTaskRespVO.java` | 新增对应响应字段 |
| 3 | `CsTaskPageReqVO.java` | 新增 productLineCode, sourceModule 筛选；**新增 viewScope 字段（由 Service 层按角色自动填充）** |
| 4 | `CsTaskCreateReqVO.java` | 新增 4 个字段 |
| 5 | `CsTaskMapper.java` | selectPage 增加新字段筛选条件 |
| 6 | `CsTaskServiceImpl.java` | createCsTask 补充新字段赋值；**getCsTaskPage 增加角色可见性过滤**（见 2.6） |

### Task 2: 操作请求 — 新建完整 CRUD

**新增文件（13 个）**：

| # | 文件路径 | 说明 |
|---|---------|------|
| 1 | `enums/CsOpReqTypeEnum.java` | 操作类型枚举 |
| 2 | `enums/CsOpReqStatusEnum.java` | 状态枚举 |
| 3 | `enums/CsAttachmentModuleEnum.java` | 附件模块枚举 |
| 4 | `enums/CsSourceModuleEnum.java` | 来源模块枚举 |
| 5 | `dal/dataobject/cs/CsOpReqDO.java` | 操作请求 DO |
| 6 | `dal/mysql/cs/CsOpReqMapper.java` | Mapper |
| 7 | `controller/admin/cs/vo/CsOpReqPageReqVO.java` | 分页请求 VO（含 viewScope 字段，由 Service 按角色填充） |
| 8 | `controller/admin/cs/vo/CsOpReqRespVO.java` | 响应 VO |
| 9 | `controller/admin/cs/vo/CsOpReqCreateReqVO.java` | 创建请求 VO |
| 10 | `controller/admin/cs/vo/CsOpReqSubmitReqVO.java` | 提交请求 VO |
| 11 | `service/cs/CsOpReqService.java` | Service 接口 |
| 12 | `service/cs/impl/CsOpReqServiceImpl.java` | Service 实现 |
| 13 | `controller/admin/cs/CsOpReqController.java` | REST Controller |

**REST API**：

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/opshub/cs-opreq/page` | `dealer:cs-opreq:query` | 分页查询 |
| GET | `/opshub/cs-opreq/get?id=` | `dealer:cs-opreq:query` | 详情（含附件） |
| POST | `/opshub/cs-opreq/create` | `dealer:cs-opreq:create` | 创建请求 |
| POST | `/opshub/cs-opreq/accept?id=` | `dealer:cs-opreq:accept` | 执行员接单 |
| POST | `/opshub/cs-opreq/submit` | `dealer:cs-opreq:submit` | 提交（附件+留言） |
| POST | `/opshub/cs-opreq/verify?id=` | `dealer:cs-opreq:verify` | 经销商验收 |

**Service 核心逻辑**：

- **创建**：校验类型+经销商→生成编号→填充冗余字段→状态=PENDING→通知处理人
- **接单**：校验 PENDING→status=IN_PROGRESS, assigneeId=当前用户→通知发起人
- **提交**：校验 IN_PROGRESS+是处理人+有附件→status=DELIVERED→通知发起人验收
- **验收**：校验 DELIVERED+是发起人→status=CLOSED, completedTime=now()→通知处理人
- **分页查询**：按角色注入可见性过滤（见 2.6），经销商只看自己发起的，执行员看未接单+自己的

### Task 3: 通用附件 — 新建完整 CRUD

**新增文件（7 个）**：

| # | 文件路径 | 说明 |
|---|---------|------|
| 1 | `dal/dataobject/cs/CsAttachmentDO.java` | 附件 DO |
| 2 | `dal/mysql/cs/CsAttachmentMapper.java` | Mapper |
| 3 | `controller/admin/cs/vo/CsAttachmentRespVO.java` | 响应 VO |
| 4 | `controller/admin/cs/vo/CsAttachmentUploadReqVO.java` | 上传请求 VO |
| 5 | `service/cs/CsAttachmentService.java` | Service 接口 |
| 6 | `service/cs/impl/CsAttachmentServiceImpl.java` | Service 实现 |
| 7 | `controller/admin/cs/CsAttachmentController.java` | REST Controller |

**REST API**：

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/opshub/cs-attachment/upload` | 按模块动态校验 | 上传附件 |
| GET | `/opshub/cs-attachment/list?module=&businessId=` | 按模块动态校验 | 查询附件列表 |
| DELETE | `/opshub/cs-attachment/delete?id=` | 按模块动态校验 | 删除附件 |

### Task 4: 基础设施修改

| # | 文件 | 修改内容 |
|---|------|---------|
| 1 | `ErrorCodeConstants.java` | 新增 11 个错误码（1-050-008-010~021） |
| 2 | `OpshubDataPermissionConfiguration.java` | 注册 `ops_cs_task` 和 `ops_cs_opreq` |
| 3 | `CsWebSocketService` 相关 | 新增操作请求通知类型 |

### Task 5: 错误码

```java
// 操作请求 1-050-008-0xx
CS_OPREQ_NOT_EXISTS       = new ErrorCode(1_050_008_010, "操作请求不存在");
CS_OPREQ_NOT_PENDING      = new ErrorCode(1_050_008_011, "仅待处理状态可接单");
CS_OPREQ_NOT_IN_PROGRESS  = new ErrorCode(1_050_008_012, "仅处理中状态可提交");
CS_OPREQ_NOT_DELIVERED    = new ErrorCode(1_050_008_013, "仅等待验收状态可验收");
CS_OPREQ_ALREADY_CLOSED   = new ErrorCode(1_050_008_014, "操作请求已完成");
CS_OPREQ_NOT_ASSIGNEE     = new ErrorCode(1_050_008_015, "非当前处理人");
CS_OPREQ_CODE_DUPLICATE   = new ErrorCode(1_050_008_016, "编号重复");
CS_OPREQ_SOURCE_NOT_EXISTS= new ErrorCode(1_050_008_017, "来源业务不存在");
// 附件
CS_ATTACHMENT_NOT_EXISTS  = new ErrorCode(1_050_008_020, "附件不存在");
CS_ATTACHMENT_UPLOAD_FAIL = new ErrorCode(1_050_008_021, "附件上传失败");
```

---

## 四、前端实现

### Task 6: 页面重构为 Tabs 布局

将 `customerservice/index.vue` 重构为 `el-tabs` 双 Tab：
- Tab 1: 工单管理（现有内容，增强产品线筛选+列）
- Tab 2: 操作请求（全新）

### Task 7: 操作请求前端组件

**新增文件（10 个）**：

| # | 文件 | 说明 |
|---|------|------|
| 1 | `api/opshub/csOpReq/index.ts` | API 定义 |
| 2 | `api/opshub/csAttachment/index.ts` | 附件 API |
| 3 | `views/opshub/customerservice/components/OpReqTab.vue` | 操作请求 Tab |
| 4 | `views/opshub/customerservice/components/OpReqFilterBar.vue` | 筛选栏 |
| 5 | `views/opshub/customerservice/components/OpReqTable.vue` | 列表表格 |
| 6 | `views/opshub/customerservice/components/OpReqDetailModal.vue` | 详情弹窗 |
| 7 | `views/opshub/customerservice/components/OpReqCreateModal.vue` | 创建弹窗 |
| 8 | `views/opshub/customerservice/components/OpReqSubmitModal.vue` | 提交弹窗 |
| 9 | `views/opshub/customerservice/components/AttachmentUploader.vue` | 附件上传组件 |
| 10 | `views/opshub/customerservice/components/AttachmentList.vue` | 附件列表组件 |

**页面布局**：
```
┌─────────────────────────────────────────────────────┐
│ [📋 工单管理]  [📝 操作请求]                           │
├─────────────────────────────────────────────────────┤
│ === 操作请求 Tab ===                                  │
│ [状态▼(多选)] [类型▼(多选)] [搜索编号/内容] [重置]       │
│ [发起请求]                                            │
│ 编号|类型|经销商|关联内容|产品线|状态|创建时间|用时|操作   │
│ 操作列：[接单(执行员)] [提交(执行员)] [验收(经销商)] [详情] │
└─────────────────────────────────────────────────────┘
```

---

## 五、角色权限矩阵

### Task 8: 权限标识统一

UPDATE `system_menu` id 6080-6094 的 permission 字段：
- `service:task:*` → `dealer:cs-task:*`
- `service:consult:*` → `dealer:cs-consult:*`
- `service:opreq:*` → `dealer:cs-opreq:*`

新增菜单按钮 id=6097(转单)、6098(重新处理)。

### 按钮级权限

**工单**：query/create/accept/deliver/transfer/verify/reprocess/urge（8 个权限）
**操作请求**：query/create/accept/submit/verify（5 个权限）

---

## 六、SQL 脚本

### Task 9: DDL + DML

**DDL**（`db/branches/feature_step6-客户服务模块/feature_step6-客户服务模块_ddl.sql`）：
- `ops_cs_task` 的完整 CREATE TABLE（补齐之前缺失的 DDL）+ ALTER TABLE 新增 4 字段
- `ops_cs_opreq` CREATE TABLE + 序列 + 索引
- `ops_cs_attachment` CREATE TABLE + 序列 + 索引

**DML**（`db/branches/feature_step6-客户服务模块/feature_step6-客户服务模块_dml.sql`）：
- 权限标识统一 UPDATE（15 条）
- 新增菜单按钮（2 条）+ 角色-菜单关联（4 条）
- 菜单 component 更新
- 测试数据：15-25 条操作请求 + 5-10 条工单增强数据
- 站内信模板（4 条）

---

## 七、验证方式

| 验证项 | 方法 |
|--------|------|
| 表创建 | DDL 后确认 ops_cs_opreq、ops_cs_attachment 存在，ops_cs_task 有 4 个新字段 |
| 权限统一 | `SELECT id, permission FROM system_menu WHERE id BETWEEN 6080 AND 6098` |
| 操作请求工作流 | create→accept→submit→verify 完整链路 |
| 附件 | upload→list→delete 完整链路 |
| 数据权限 | dealer/执行员分别查询验证过滤；经销商只看自己提交的；执行员看未接单+自己的；管理员看全部 |
| 角色可见性 | 分别用 4 种角色登录 `/opshub/cs-task/page` 和 `/opshub/cs-opreq/page`，验证返回数据集符合 2.6 规则 |
| 编译 | `mvn clean compile -pl yudao-module-opshub` |
| 前端 | `pnpm dev` 双 Tab 页面正常渲染 |

---

## 八、后续阶段预留

| 功能 | 预留阶段 |
|------|--------|
| 咨询队列 + 在线聊天 | 后续 Step |
| 操作请求状态同步到源模块 | 后续 Step |
| 操作请求 BPM 集成 | 后续 Step |
| SLA 自动监控 + 超时告警 | 后续 Step |
| 批量接单/批量验收 | 后续 Step |