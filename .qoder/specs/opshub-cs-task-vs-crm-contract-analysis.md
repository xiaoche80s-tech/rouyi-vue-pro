# ops_cs_task 按 CRM 合同审批模式改造 -- 对比分析

## Context

本文档对比 `ops_cs_task`（客服工单）与 `CrmContractServiceImpl`（CRM 合同审批）两种 BPM 集成模式的架构差异，逐项分析如果将 ops_cs_task 按 CRM 轻量模式改造，涉及哪些改造点、难度、风险、以及对现有功能的影响。

**核心结论：工单业务远比合同审批复杂（多步骤状态机 + 多角色操作 + 处理人同步），CRM 轻量模式无法直接套用。8 个改造点中仅 1 个可安全实施。**

---

## 一、两套模式的核心差异

| 维度 | CRM 合同审批（轻量模式） | ops_cs_task（深度模式） |
|------|------------------------|----------------------|
| BPM 启动时机 | 显式提交 `submitContract()` | 创建即启动 `createCsTask()` |
| 状态复杂度 | 线性单向：DRAFT→PROCESS→APPROVE/REJECT/CANCEL | 多步骤+回退：PENDING→IN_PROGRESS→DELIVERED→CLOSED，+ REJECTED→IN_PROGRESS |
| BPM APPROVE 处理 | 1:1 映射（`CrmAuditStatusUtils`） | 条件分支（IN_PROGRESS+APPROVE→DELIVERED，DELIVERED+APPROVE→CLOSED） |
| BPM 任务操作 | Service 不直接操作 BPM 任务 | 深度操作：`approveTask()` / `rejectTask()` / `transferTask()` |
| Flowable 事件监听 | 无 | `CsTaskBpmAssignedListener` 监听 TASK_ASSIGNED |
| 处理人同步 | 无需求 | BPM assignee ↔ 工单 assigneeId 双向同步 |
| 待办/已办查询 | 按 auditStatus 状态过滤 | BPM 驱动：`bpmTaskService.getTodoProcessInstanceIds()` 反查 |
| 业务事件 | 无自有事件 | `CsTaskStatusChangeEvent` 按 category 过滤，供下游模块监听 |

### 为什么两种模式不同

- **CRM 合同**是"单次审批"场景：提交 → 审批人在 BPM 系统操作 → 结果回调。业务系统被动等待。
- **ops_cs_task**是"多步骤协作"场景：接单 → 处理 → 交付审批 → 验收审批 → 关闭，含退回重做。业务操作主动驱动 BPM。

---

## 二、改造点清单

### 改造点 1：BPM 启动时机 -- 从"创建即启动"改为"显式提交"

| | 当前实现 | 目标实现 |
|---|---------|---------|
| 代码 | `CsTaskServiceImpl.createCsTask()` L109-122 | `CrmContractServiceImpl.submitContract()` L292-308 |
| 行为 | 创建工单后立即调用 `processInstanceApi.createProcessInstance()` | 创建时不涉及 BPM，用户手动提交才启动 |

**改造内容：**
- `createCsTask()` 移除 BPM 调用，状态设为新增的 DRAFT
- 新增 `submitCsTask()` 方法，校验 DRAFT 状态后才启动 BPM
- 前端增加"提交"按钮

**难度：中 | 风险：高 | 不推荐**
- 经销商提单 = 提交是合理语义，拆成两步增加操作步骤
- PENDING 状态语义需要重新定义
- BPM 流程变量传递时机改变，需同步修改 BPMN 流程定义

---

### 改造点 2：移除 Service 层对 BPM 任务的直接操作

| | 当前实现 | 目标实现 |
|---|---------|---------|
| 代码 | `approveCurrentBpmTask()` L651-663, `rejectCurrentBpmTask()` L668-680 | CRM 无对应方法 |
| 依赖 | `BpmTaskService.approveTask()` / `rejectTask()` | CRM 不依赖 `BpmTaskService` |

**改造内容：**
- 移除 `approveCurrentBpmTask()` 和 `rejectCurrentBpmTask()`
- `submitForApproval()`、`verifyTask()`、`cancelTask()` 不再主动推动 BPM

**难度：低 | 风险：极高 | 不可行**
- `submitForApproval()` 不推动 BPM → 流程停在当前节点，经销商永远收不到验收通知
- `verifyTask()` 不推动 BPM → 验收操作无法传导到流程引擎
- `cancelTask()` 不取消 BPM → 流程实例泄漏

---

### 改造点 3：BPM 回调状态映射 -- 从条件分支改为 1:1 映射

| | 当前实现 | 目标实现 |
|---|---------|---------|
| 代码 | `updateCsTaskStatusByBpm()` L529-566 | `CrmContractServiceImpl.updateContractAuditStatus()` L312-325 |
| 逻辑 | APPROVE + IN_PROGRESS → DELIVERED; APPROVE + DELIVERED → CLOSED | APPROVE → APPROVE（1:1） |

**改造内容：**
- 将条件分支改为 1:1 映射
- 创建 `CsTaskAuditStatusUtils` 工具类

**难度：低 | 风险：高 | 不可行**
- CRM 合同只有一次审批（DRAFT→PROCESS→APPROVE），工单有两个审批节点（执行员交付 + 经销商验收）
- 1:1 映射无法区分"交付审批通过"和"验收审批通过"

---

### 改造点 4：移除 Flowable TASK_ASSIGNED 事件监听器

| | 当前实现 | 目标实现 |
|---|---------|---------|
| 代码 | `CsTaskBpmAssignedListener.java`（219 行） | CRM 无对应组件 |
| 功能 | 同步 assignee + PENDING→IN_PROGRESS 自动推进 + 接单/转单通知 |

**改造内容：**
- 删除 `CsTaskBpmAssignedListener.java`
- 处理人同步逻辑找替代方案

**难度：低 | 风险：极高 | 不推荐**
- 丧失自动接单（BPM 分配 → 自动 PENDING→IN_PROGRESS）
- 丧失转单后 BPM assignee 自动同步
- 丧失接单/转单统一通知

---

### 改造点 5：待办/已办查询 -- 从 BPM 驱动改为状态过滤

| | 当前实现 | 目标实现 |
|---|---------|---------|
| 代码 | `applyTabFilter()` L455-509, `getTabCounts()` L568-601 | `CrmContractMapper.selectPage()` 按 auditStatus 过滤 |
| 方式 | `bpmTaskService.getTodoProcessInstanceIds()` 反查 | 直接按 status 字段分页 |

**改造内容：**
- `applyTabFilter()` 中 assignee/all 的 pending/done 分支改为 status 过滤
- `getTabCounts()` 移除 `bpmTaskService` 调用

**难度：低 | 风险：中 | 部分可行**
- 语义差异：BPM 待办包含候选任务（未 assign 的），状态过滤无法覆盖
- 抢单模式可通过已有 claimable tab 覆盖
- 如接受"待办 = 分配给我的处理中工单"简化语义，可实施

---

### 改造点 6：状态枚举简化 -- 移除回退路径

| | 当前实现 | 目标实现 |
|---|---------|---------|
| 代码 | `CsTaskStatusEnum`（5 种状态 + 回退） | `CrmAuditStatusEnum`（5 种状态，单向） |

**改造内容：**
- 移除 REJECTED(4) 或移除 REJECTED→IN_PROGRESS 回退路径
- 移除 `reprocessTask()` 方法

**难度：低 | 风险：高 | 不可行**
- 退回重做是工单核心需求（验收不通过 → 修改 → 重新提交）
- CRM 合同无此场景（合同审批不通过就是终态）

---

### 改造点 7：Listener 回调简化

| | 当前实现 | 目标实现 |
|---|---------|---------|
| 代码 | `CsTaskStatusListener.java`（36 行）调用条件状态机 | `CrmContractStatusListener.java`（28 行）调用 1:1 映射 |

**改造内容：**
- 回调方法仅做状态写入，不做条件判断

**难度：低 | 风险：取决于改造点 3 | 不可行**

---

### 改造点 8：移除自有事件体系

| | 当前实现 | 目标实现 |
|---|---------|---------|
| 代码 | `CsTaskStatusChangeEvent`（60 行）、`CsTaskStatusChangeEventListener`（55 行） | CRM 无对应 |
| 用途 | 各业务模块按 category 过滤监听工单状态变更 |

**改造内容：**
- 删除事件类和抽象监听器
- 移除 `publishStatusChangeEvent()` 调用

**难度：低 | 风险：低 | 可行但不必要**
- 当前无生产环境实现类，但保留有利于未来扩展（如签约工单验收后更新签约进度）

---

## 三、改造可行性总结

| # | 改造点 | 难度 | 风险 | 可行性 | 建议 |
|---|--------|------|------|--------|------|
| 1 | BPM 启动改为显式提交 | 中 | 高 | **不推荐** | 增加操作步骤，违背提单语义 |
| 2 | 移除 Service 直接操作 BPM | 低 | 极高 | **不可行** | BPM 流程无法推进，核心功能瘫痪 |
| 3 | BPM 回调改为 1:1 映射 | 低 | 高 | **不可行** | 多步骤状态机无法适配 |
| 4 | 移除 TASK_ASSIGNED 监听器 | 低 | 极高 | **不推荐** | 丧失处理人自动同步 |
| 5 | 待办查询改为状态过滤 | 低 | 中 | **部分可行** | 可简化，但丧失候选任务可见性 |
| 6 | 移除回退路径 | 低 | 高 | **不可行** | 退回重做是核心需求 |
| 7 | Listener 回调简化 | 低 | 高 | **不可行** | 依赖改造点 3 |
| 8 | 移除自有事件体系 | 低 | 低 | **可行但不必要** | 保留有利于扩展 |

---

## 四、结论

### ops_cs_task 当前架构是合理的

当前深度 BPM 集成模式针对工单业务场景设计，每个"复杂点"都对应具体业务需求：

| 复杂点 | 业务需求 |
|--------|----------|
| 创建即启动 BPM | 经销商提单后执行员立即可见 |
| Service 直接操作 BPM | 业务操作（交付/验收）驱动流程前进 |
| 条件分支状态映射 | 两次 APPROVE 分别推进到不同状态 |
| TASK_ASSIGNED 监听 | 自动接单 + 处理人同步 + 通知 |
| BPM 驱动待办查询 | 精确反映 BPM 候选任务 |
| 回退路径 | 验收不通过后重新处理 |

### 唯一可安全实施的改造

**改造点 5（待办查询简化）**可独立实施，降低对 `BpmTaskService` 的耦合度，但需接受简化语义。

---

## 五、关键文件索引

| 文件 | 行数 | 角色 |
|------|------|------|
| `opshub/.../service/cs/impl/CsTaskServiceImpl.java` | 721 | 工单核心逻辑 |
| `opshub/.../service/cs/listener/CsTaskStatusListener.java` | 36 | BPM 流程状态回调 |
| `opshub/.../service/cs/listener/CsTaskBpmAssignedListener.java` | 219 | Flowable 任务分配监听 |
| `opshub/.../service/cs/event/CsTaskStatusChangeEvent.java` | 60 | 工单状态变更事件 |
| `opshub/.../service/cs/event/CsTaskStatusChangeEventListener.java` | 55 | 按 category 过滤监听器 |
| `opshub/.../enums/CsTaskStatusEnum.java` | 41 | 工单状态枚举 |
| `crm/.../service/contract/CrmContractServiceImpl.java` | 417 | CRM 合同参考实现 |
| `crm/.../service/contract/listener/CrmContractStatusListener.java` | 28 | CRM 合同 BPM 回调 |
| `crm/.../util/CrmAuditStatusUtils.java` | 28 | BPM→CRM 状态映射 |
