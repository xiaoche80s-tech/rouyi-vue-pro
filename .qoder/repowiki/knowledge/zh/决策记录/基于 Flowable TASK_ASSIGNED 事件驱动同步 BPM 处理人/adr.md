# 基于 Flowable TASK_ASSIGNED 事件驱动同步 BPM 处理人

_来源：a7aeacb → 32a9ec7 提交周期内记录的编码计划——内容为规划时意图，实现可能滞后或有出入。_

**状态：** accepted

## 背景
在创建客服工单（createCsTask）后，原方案通过事务后回调主动查询 BPM 任务以同步处理人。由于 BPM 引擎的 StartUserNode 自动完成发生在异步事务中，存在时序竞争，导致查询时下一节点尚未生成，造成 assignee_id 为空。用户明确拒绝了 Thread.sleep 重试方案，要求更可靠的实现。

## 决策驱动
- 消除时序竞争导致的空指针/数据不一致
- 避免使用 Thread.sleep 等不稳定的轮询/等待机制
- 保持 BPM 模块零侵入，利用 Spring 自动注册机制扩展监听

## 备选方案
- **事务后主动拉取 + Thread.sleep 重试** _（已否决）_ — 优点：实现简单，无需新增监听器类；缺点：引入不可控的延迟，性能差，且仍可能因极端负载失败，被用户明确拒绝
- **Flowable TASK_ASSIGNED 事件监听器** — 优点：由引擎在分配处理人后立即触发，时序准确；通过 afterCompletion 确保数据落盘；零改动 BPM 核心模块；缺点：需新增监听器组件，逻辑从主动拉取变为被动响应

## 决策
在 opshub 模块创建 CsTaskBpmAssignedListener 实现 FlowableEventListener，监听 TASK_ASSIGNED 事件。在事件回调中通过 TransactionSynchronizationManager.registerSynchronization 注册 afterCompletion 回调，异步更新 ops_cs_task 表的 assignee_id 和状态。移除 CsTaskServiceImpl 中 createCsTask 后的主动同步调用。

## 影响
彻底解决了工单创建初期处理人为空的问题；系统对 BPM 引擎的状态变化响应更实时；但增加了事件监听器的维护成本，且需确保监听器中的数据库操作不会阻塞 BPM 主事务。