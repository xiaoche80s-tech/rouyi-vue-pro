---
kind: design
name: 使用 Flowable BPM 引擎驱动工单待办/已办查询
source: session
category: adr
---

# 使用 Flowable BPM 引擎驱动工单待办/已办查询

_来源：7451d2e → 073df14 提交周期内记录的编码计划——内容为规划时意图，实现可能滞后或有出入。_

**状态：** accepted

## 背景
原有的工单待办/已办查询完全依赖数据库 `status` 字段进行硬编码过滤，导致业务数据状态与 BPM 流程实际状态脱节。为确保执行员看到的任务列表与 Flowable 引擎中的任务分配严格一致，需要重构查询逻辑，以 BPM 引擎为唯一真实来源。

## 决策驱动
- 状态一致性：确保业务视图与 BPM 引擎状态实时同步
- 架构解耦：将任务分配逻辑从业务表状态管理中剥离
- 权限复用：利用现有的 DealerDataPermissionRule 叠加 BPM 过滤结果

## 备选方案
- **基于数据库 status 字段过滤（原有方案）** _（已否决）_ — 优点：实现简单，无需额外查询开销；缺点：与 BPM 流程状态脱节，存在数据不一致风险，无法反映复杂的流程路由状态
- **BPM 驱动的反查模式（选定方案）** — 优点：以 Flowable 引擎为权威数据源，保证待办/已办列表的绝对准确；通过 processInstanceId IN (...) 反查业务表，兼容现有数据权限规则；缺点：每次查询需额外调用 Flowable API 获取 ID 集合；标签计数与列表可能存在短暂不一致（计数仍用 status，列表用 BPM）

## 决策
在 `BpmTaskService` 中新增 `getTodoProcessInstanceIds` 和 `getDoneProcessInstanceIds` 接口，直接查询 Flowable 引擎获取当前用户的 `processInstanceId` 集合。在 `CsTaskServiceImpl` 的 `applyTabFilter` 中，针对 pending/done 标签，先调用 BPM 服务获取 ID 列表，再通过 `WHERE process_instance_id IN (...)` 过滤 `ops_cs_task` 表。为空时构造永假条件以避免全表扫描。

## 影响
待办/已办列表的准确性得到保障，但引入了对 Flowable 服务的依赖和额外的查询开销。标签计数（count）与列表（page）采用不同策略（前者用 status，后者用 BPM），可能导致 UI 上短暂的数字不一致，需在回调到达后自动修正。历史无 processInstanceId 的数据仅在 'all' 标签可见。