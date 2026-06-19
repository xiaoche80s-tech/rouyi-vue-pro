-- =============================================
-- OpsHub Step 11: 工单流程优化
-- 数据库: PostgreSQL
-- 包含: DDL（唯一索引）
--   ops_cs_task.task_no 添加部分唯一索引（仅对未删除行生效）
--   防止工单号生成并发竞态导致重复
-- =============================================

-- 为工单编号添加部分唯一索引，确保并发场景下不会生成重复工单号
-- 仅对 deleted = 0 的行生效（逻辑删除的行不参与唯一约束检查）
CREATE UNIQUE INDEX uk_ops_cs_task_no ON ops_cs_task (task_no) WHERE deleted = 0;
