-- =============================================
-- BugFix: 工单转单/催单功能修复
-- 催办站内信模板
-- =============================================

-- 催办工单站内信模板
INSERT INTO system_notify_template (name, code, nickname, content, type, params, status, remark, creator, create_time, updater, update_time, deleted)
VALUES ('工单-催办通知', 'cs-task-urging', '系统通知', '工单 {taskNo} 被催办，请尽快处理', 1, '["taskNo"]', 0, '催办工单时通知处理人', '1', NOW(), '1', NOW(), 0);
