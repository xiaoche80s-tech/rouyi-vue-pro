-- =============================================
-- Feature: 将转单接口迁移至 BPM /bpm/task/transfer
-- 目的: 确保持有 dealer:cs-task:transfer 的角色同时拥有 bpm:task:update
-- 数据库: PostgreSQL
-- 租户ID: 123
-- =============================================

-- 将 bpm:task:update (menu_id=1222) 授予所有持有 dealer:cs-task:transfer (menu_id=6097) 的角色
INSERT INTO system_role_menu (id, role_id, menu_id, tenant_id)
SELECT (SELECT COALESCE(MAX(id),0) FROM system_role_menu) + row_number() OVER (), t.role_id, 1222, 123
FROM (
  SELECT DISTINCT rm.role_id
  FROM system_role_menu rm
  WHERE rm.menu_id = 6097 AND rm.tenant_id = 123
) t
WHERE NOT EXISTS (
  SELECT 1 FROM system_role_menu rm2
  WHERE rm2.role_id = t.role_id AND rm2.menu_id = 1222 AND rm2.tenant_id = 123
);

-- 同步序列值
SELECT setval('system_role_menu_seq', (SELECT COALESCE(MAX(id), 0) FROM system_role_menu));
