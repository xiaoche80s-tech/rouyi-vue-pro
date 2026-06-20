# 可行性分析：使用 BPM 执行监听器/任务监听器变更 op_cs_task 状态

## Context

用户希望了解当前使用的 BPM 工作流能否通过 Flowable 的**执行监听器 (ExecutionListener)** 和**任务监听器 (TaskListener)** 来变更 `op_cs_task` 表的 `status` 字段。

## 结论：技术上完全可行，但现有机制已更优

---

## 一、现有 4 层事件链（已验证）

当前 `op_cs_task` 与 BPM 的集成已有完整的 4 层事件架构：

| 层级 | 机制 | 代表类 | 当前用途 |
|------|------|--------|---------|
| **L1** Flowable 全局事件 | `AbstractFlowableEngineEventListener` | `CsTaskBpmAssignedListener` | TASK_ASSIGNED → 同步 assignee_id + PENDING→IN_PROGRESS |
| **L2** BPMN 级监听器 | `ExecutionListener` / `TaskListener` | `BpmUserTaskListener`、`BpmCallActivityListener` | BPM 模块内部使用（HTTP 回调、子流程） |
| **L3** Spring ApplicationEvent | `BpmProcessInstanceStatusEventListener` | `CsTaskStatusListener` | APPROVE→DELIVERED/CLOSED, REJECT→REJECTED, CANCEL→CLOSED |
| **L4** 业务自定义事件 | `CsTaskStatusChangeEvent` | 各业务模块监听器 | 下游模块（签约进度等）响应工单状态变更 |

**关键调用链：**
```
Flowable 引擎 → BpmProcessInstanceEventListener (L1)
  → processProcessInstanceCompleted()
    → BpmProcessInstanceStatusEvent (L3 发布)
      → CsTaskStatusListener.onEvent() (L3 消费)
        → updateCsTaskStatusByBpm() → 更新 DB
          → CsTaskStatusChangeEvent (L4 发布)
```

---

## 二、三种 BPMN 级监听器实现方式

### 方式 A：Java 类 (class)

```java
// 不注册为 Spring Bean，由 Flowable 反射实例化
public class CsTaskStatusClassListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        // 缺陷：无法 @Resource 注入 Spring Bean，需手动从 ApplicationContext 获取
    }
}
```
- BPMN 配置：`class` = `cn.iocoder.yudao.module.opshub...CsTaskStatusClassListener`
- **不推荐**：无法注入 Spring Bean，无法使用声明式事务

### 方式 B：委托表达式 (delegateExpression) - 推荐

```java
@Component("csTaskStatusDelegateListener")
@Slf4j
public class CsTaskStatusDelegateListener implements TaskListener {
    @Resource
    private CsTaskMapper csTaskMapper;

    @Override
    public void notify(DelegateTask delegateTask) {
        String businessKey = delegateTask.getExecution().getProcessInstanceBusinessKey();
        Long csTaskId = Long.parseLong(businessKey);
        // 可直接注入 Spring Bean，正常操作 DB
    }
}
```
- BPMN 配置：`delegateExpression` = `${csTaskStatusDelegateListener}`
- **可行**：支持 Spring Bean 注入，运行在 Flowable 引擎事务中

### 方式 C：表达式 (expression)

```java
@Component("csTaskStatusHandler")
public class CsTaskStatusHandler {
    @Resource private CsTaskService csTaskService;
    public void onTaskComplete(DelegateTask task) { ... }
}
```
- BPMN 配置：`expression` = `${csTaskStatusHandler.onTaskComplete(task)}`
- **可行但脆弱**：表达式语法错误只能运行时发现

---

## 三、与现有机制对比

| 维度 | 现有 L3 (`CsTaskStatusListener`) | **BPMN 级监听器** |
|------|------|------|
| 触发粒度 | 流程实例级 (APPROVE/REJECT/CANCEL) | **节点级**（精确到某个 UserTask 的 complete） |
| 注册方式 | `@Component` 自动注册 | 需在 BPMN 流程图中配置 + 重新部署 |
| 流程定义耦合 | 不耦合 | **强耦合**（改规则需改 BPMN 并重新部署） |
| 事务行为 | Spring Event 事务 | Flowable 引擎事务 |
| 通知链 | 自动触发 WebSocket + 站内信 + L4 事件 | 需手动处理，否则绕过通知链 |
| 幂等保护 | 已有 (`updateCsTaskStatusByBpm` 内置) | 需自行实现 |
| 可测试性 | 纯 Mockito 单元测试 | 需 Flowable 集成测试 |

---

## 四、使用 BPMN 级监听器的注意事项

1. **事务陷阱**：TaskListener 运行在 Flowable 引擎事务中，若在此事务内发送 WebSocket，事务回滚时消息已无法撤回。应参考 `CsTaskBpmAssignedListener` 使用 `TransactionSynchronizationManager.registerSynchronization(afterCompletion)` 模式。

2. **绕过通知链**：直接操作 `csTaskMapper` 会绕过 `updateCsTaskStatusByBpm()` 中的 WebSocket 通知、站内信和 `CsTaskStatusChangeEvent` 发布，导致下游模块收不到事件。

3. **重复处理**：现有 `CsTaskStatusListener` 已处理 APPROVE/REJECT/CANCEL，新增 BPMN 监听器可能导致同一事件被处理两次。

4. **流程重部署**：每次修改监听器逻辑，需要修改 BPMN 流程定义并重新发布流程。

---

## 五、建议

**维持现有 L1 + L3 + L4 三层架构，不引入 BPMN 级监听器来变更 op_cs_task 状态。**

理由：
- `CsTaskStatusListener` (L3) + `CsTaskBpmAssignedListener` (L1) + `updateCsTaskStatusByBpm()` 状态机已精确覆盖工单全生命周期
- 新增状态推进需求应优先扩展现有 `updateCsTaskStatusByBpm()` 方法

**BPMN 级监听器的合理使用场景**（供未来参考）：
- 特定中间节点完成时触发**独立于工单状态**的业务动作（如外部系统回调）
- 利用 TaskListener 的 `timeout` 事件实现 SLA 超时自动升级
- 在 ServiceTask 节点执行自动化操作（使用 `JavaDelegate`）
