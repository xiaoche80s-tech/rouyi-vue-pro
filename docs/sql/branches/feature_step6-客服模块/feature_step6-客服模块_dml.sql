-- =============================================
-- OpsHub Step 6: 客户服务模块 - 站内信模板 DML
-- 数据库: PostgreSQL
-- 包含: 6 条客服工单站内信模板
-- =============================================

INSERT INTO system_notify_template (id, name, code, nickname, content, type, params, status, remark, creator, create_time, updater, update_time, deleted) VALUES
(200, '工单创建通知', 'cs-task-created', '系统', '您有新的工单 {taskNo} 待处理', 2, '["taskNo"]', 0, '客服工单创建时通知处理人', '1', CURRENT_TIMESTAMP, '1', CURRENT_TIMESTAMP, 0),
(201, '工单接单通知', 'cs-task-accepted', '系统', '工单 {taskNo} 已被接单', 2, '["taskNo"]', 0, '客服工单被接单时通知提单人', '1', CURRENT_TIMESTAMP, '1', CURRENT_TIMESTAMP, 0),
(202, '工单转单通知', 'cs-task-transferred', '系统', '有新的工单 {taskNo} 转交给您', 2, '["taskNo"]', 0, '客服工单转单时通知新处理人', '1', CURRENT_TIMESTAMP, '1', CURRENT_TIMESTAMP, 0),
(203, '工单交付通知', 'cs-task-delivered', '系统', '工单 {taskNo} 已交付，请验收', 2, '["taskNo"]', 0, '客服工单交付时通知提单人', '1', CURRENT_TIMESTAMP, '1', CURRENT_TIMESTAMP, 0),
(204, '工单验收通过', 'cs-task-verified', '系统', '工单 {taskNo} 验收通过', 2, '["taskNo"]', 0, '客服工单验收通过时通知处理人', '1', CURRENT_TIMESTAMP, '1', CURRENT_TIMESTAMP, 0),
(205, '工单退回通知', 'cs-task-rejected', '系统', '工单 {taskNo} 验收不通过，已退回', 2, '["taskNo"]', 0, '客服工单验收不通过时通知处理人', '1', CURRENT_TIMESTAMP, '1', CURRENT_TIMESTAMP, 0);

-- 更新序列值
SELECT setval('system_notify_template_seq', GREATEST((SELECT MAX(id) FROM system_notify_template), 205));
