-- =============================================
-- OpsHub Step 19: 政策看板模块 - 政策日期字段迁移
-- 用途：为已存在的 ops_dealer_policy 表追加 start_date / end_date 字段
-- =============================================

ALTER TABLE ops_dealer_policy ADD COLUMN start_date DATE DEFAULT NULL COMMENT '政策开始日期' AFTER policy_status;
ALTER TABLE ops_dealer_policy ADD COLUMN end_date DATE DEFAULT NULL COMMENT '政策结束日期' AFTER start_date;
