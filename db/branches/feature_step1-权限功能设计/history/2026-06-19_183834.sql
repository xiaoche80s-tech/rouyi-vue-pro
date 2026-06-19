-- 执行时间: 2026-06-19 18:38:34
-- 分支: feature/step1-权限功能设计
-- 操作说明: 修正 TASK-20260619-007 工单 assignee_id 和 status 错误数据
--           syncBpmAssignee 在创建工单时执行，但 BPM 引擎尚未完成 StartUserNode 自动流转，
--           过滤掉 StartUserNode 后未找到下一节点，导致 assignee_id=null、status=PENDING。
--           BPM 当前运行任务节点为"执行员提交处理方案"，assignee=205（陈执行A）。

UPDATE ops_cs_task
SET assignee_id = 205,
    status = 1,
    accept_time = NOW(),
    update_time = NOW()
WHERE task_no = 'TASK-20260619-007'
  AND deleted = 0;
