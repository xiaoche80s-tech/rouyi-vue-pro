# 使用 REQUIRES_NEW 事务隔离 OpsHub 与 BPM 的调用

_来源：32a9ec7 → 7451d2e 提交周期内记录的编码计划——内容为规划时意图，实现可能滞后或有出入。_

**状态：** accepted

## 背景
OpsHub 服务在 @Transactional 方法中调用 BPM 服务的 @Transactional 方法时，若 BPM 调用失败抛出异常，会导致外层事务被标记为 rollback-only。即使 OpsHub 层 catch 了异常，提交时仍会抛出 UnexpectedRollbackException，掩盖真实错误并导致业务回滚。

## 决策驱动
- 事务一致性边界
- 错误可观测性
- 业务容错性

## 备选方案
- **使用 TransactionTemplate (PROPAGATION_REQUIRES_NEW) 隔离调用** — 优点：BPM 调用失败仅回滚其自身事务，不影响 OpsHub 主业务事务；可捕获真实异常日志；缺点：代码复杂度略有增加，需手动管理事务模板
- **保持默认 REQUIRED 传播并在外层捕获** _（已否决）_ — 优点：代码简洁；缺点：无法避免 UnexpectedRollbackException，导致主业务意外回滚或错误信息丢失

## 决策
在 CsTaskServiceImpl 中注入 PlatformTransactionManager，使用 TransactionTemplate 以 PROPAGATION_REQUIRES_NEW 方式调用 bpmTaskService 的相关方法（转派、审批、拒绝等），并将异常记录为 error 日志而不中断主流程。

## 影响
解耦了 OpsHub 与 BPM 的事务边界，提高了系统的健壮性。BPM 同步失败不再导致工单状态变更回滚，但需依赖日志进行问题排查。