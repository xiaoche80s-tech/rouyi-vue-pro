# OpRequest BPM TaskListener 实现计划

## Context

当前 `ops-op-request` 流程缺少对 BPM 任务级别事件的监听：
- `OpRequestBpmStatusListener` 仅处理流程实例终态回调（APPROVE/REJECT/CANCEL）
- `assigneeId` 字段从未被 BPM 事件更新
- 状态从 WAITING → IN_PROGRESS 的推进没有自动触发

需要添加一个 Flowable 原生 `TaskListener`（委托表达式方式），在 BPMN 流程图中配置到执行员用户任务节点上，实现：
1. `assignment` 事件 → 同步 `assigneeId` + 状态从 WAITING → IN_PROGRESS
2. `complete` 事件 → 可选的流程状态变更处理

## 参考实现

- `CsTaskBpmAssignedListener` — 使用 `FlowableEventListener` + `TASK_ASSIGNED` 全局监听（对比方案）
- `DemoDelegateExpressionTaskListener` — `TaskListener` + `@Component` 委托表达式方式（采用方案）
- `BpmUserTaskListener` — BPM 模块内置的通用任务监听器（参考结构）

## 实现方案

### Task 1: 创建 OpRequestBpmTaskListener

**新建文件**：`yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/service/oprequest/listener/OpRequestBpmTaskListener.java`

关键设计：
- `@Component("opRequestBpmTaskListener")` — Spring Bean 名称用于 BPMN 委托表达式 `${opRequestBpmTaskListener}`
- 实现 `org.flowable.engine.delegate.TaskListener` 接口
- `notify(DelegateTask)` 方法中：
  1. 通过 `RuntimeService` 查询 `ProcessInstance`，校验 `processDefinitionKey = "ops-op-request"`
  2. 解析 `businessKey` 为 `requestId`
  3. 根据 `delegateTask.getEventName()` 分发：
     - `assignment` → `TransactionSynchronization.afterCompletion` 中同步 `assigneeId` + WAITING→IN_PROGRESS
     - `complete` → 日志记录/扩展点
- 使用 `@Lazy` 注入依赖避免循环依赖
- 对 `assignee` 判空，防止 `NumberFormatException`

### Task 2: BPMN 流程图配置说明

在 BPM 管理后台的 `ops-op-request` 流程定义中，给**执行员用户任务节点**添加任务监听器：

| 事件 | 值类型 | 委托表达式 | 说明 |
|------|--------|-----------|------|
| `assignment` | `delegateExpression` | `${opRequestBpmTaskListener}` | 任务分配时同步 assigneeId + 状态 |
| `complete` | `delegateExpression` | `${opRequestBpmTaskListener}` | 任务完成时扩展处理 |

> 需要在 BPMN 设计器中手动配置，或通过流程监听器模板功能配置。配置后必须**重新发布流程**。

### Task 3: 编译验证

```bash
mvn compile -pl yudao-module-opshub
```

## 涉及文件

| 文件 | 操作 |
|------|------|
| `yudao-module-opshub/.../oprequest/listener/OpRequestBpmTaskListener.java` | **新建** |

## 验证方式

| 验证项 | 操作 | 预期 |
|--------|------|------|
| 编译通过 | `mvn compile -pl yudao-module-opshub` | 无错误 |
| 流程发起 → 执行员被分配 | 创建操作请求，BPM 流转到执行员节点 | `ops_op_request.assignee_id` 被更新，`request_status` 从 `waiting` → `in_progress` |
| 流程完成 | 执行员提交处理结果 | 状态正常流转，无异常日志 |

## 风险与注意事项

| 风险 | 缓解措施 |
|------|---------|
| BPMN 流程图未重新发布 | 新建监听器后必须重新发布 `ops-op-request` 流程才能生效 |
| 事务时序 | 使用 `afterCompletion` 确保 BPM 事务已提交，避免数据不一致 |
| assignee 为空 | 判空处理，防止 `NumberFormatException` |
