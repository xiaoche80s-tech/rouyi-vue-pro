-- =============================================
-- OpsHub Step 19: 政策看板模块 - 年度字段迁移
-- 用途：为已存在的 indicator / achievement 表追加 target_year 字段
-- 注意：已在 PostgreSQL 环境执行过，MySQL 环境请按实际年份调整默认值
-- =============================================

-- 指标表新增年度列
ALTER TABLE ops_dealer_policy_indicator ADD COLUMN target_year INT NOT NULL DEFAULT 2026 COMMENT '年度';
CREATE INDEX idx_dpi_target_year ON ops_dealer_policy_indicator (target_year);

-- 达成明细表新增年度列
ALTER TABLE ops_dealer_policy_achievement ADD COLUMN target_year INT NOT NULL DEFAULT 2026 COMMENT '年度';
CREATE INDEX idx_dpa_target_year ON ops_dealer_policy_achievement (target_year);

-- 回填历史数据（示例：全部填 2026；实际请按业务数据所属年度调整）
UPDATE ops_dealer_policy_indicator SET target_year = 2026 WHERE target_year = 2026;
UPDATE ops_dealer_policy_achievement SET target_year = 2026 WHERE target_year = 2026;

-- 取消默认值，强制以后显式写入
ALTER TABLE ops_dealer_policy_indicator ALTER COLUMN target_year DROP DEFAULT;
ALTER TABLE ops_dealer_policy_achievement ALTER COLUMN target_year DROP DEFAULT;
