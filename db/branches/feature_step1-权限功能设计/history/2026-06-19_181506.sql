-- 执行时间: 2026-06-19 18:15:06
-- 分支: feature/step1-权限功能设计
-- 操作说明: 修正 TASK-20260619-003 工单 assignee_id 错误数据
--           syncBpmAssignee 在创建工单时取到了 StartUserNode（发起人）的 assignee，
--           导致 assignee_id=202（李代理A）而非正确执行人 205（陈执行A）。
--           BPM 当前运行任务节点为"执行员提交处理方案"，assignee=205（陈执行A）。

UPDATE ops_cs_task
SET assignee_id = 205,
    update_time = NOW()
WHERE task_no = 'TASK-20260619-003'
  AND deleted = 0;
