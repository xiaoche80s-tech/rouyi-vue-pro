-- 执行时间: 2026-06-19 20:11:04
-- 分支: feature/step1-权限功能设计
-- 操作说明: 新增内置角色「流程管理员」(process_admin) + OpsHub/BPM 菜单授权

-- 一、角色数据（已存在，更新 code 字段）
UPDATE system_role SET code = 'process_admin', sort = 50, data_scope = 1, data_scope_dept_ids = '', status = 0, type = 1, remark = '业务角色-流程转派' WHERE id = 161 AND tenant_id = 123;

-- 二、OpsHub 菜单授权 (role_id=161, tenant_id=123)
INSERT INTO system_role_menu (id, role_id, menu_id, tenant_id)
SELECT (SELECT COALESCE(MAX(id),0) FROM system_role_menu) + row_number() OVER (), role_id, menu_id, 123 FROM (
  SELECT 161 AS role_id, 6000 AS menu_id
  UNION ALL SELECT 161, 6008
  UNION ALL SELECT 161, 6080
  UNION ALL SELECT 161, 6097
  UNION ALL SELECT 161, 6219
) t;

-- 三、BPM 菜单授权 (role_id=161, tenant_id=123)
INSERT INTO system_role_menu (id, role_id, menu_id, tenant_id)
SELECT (SELECT COALESCE(MAX(id),0) FROM system_role_menu) + row_number() OVER (), role_id, menu_id, 123 FROM (
  SELECT 161 AS role_id, 1185 AS menu_id
  UNION ALL SELECT 161, 1200
  UNION ALL SELECT 161, 2724
  UNION ALL SELECT 161, 2725
  UNION ALL SELECT 161, 1222
) t;

-- 四、同步序列值
SELECT setval('system_role_seq', (SELECT COALESCE(MAX(id), 0) FROM system_role));
SELECT setval('system_role_menu_seq', (SELECT COALESCE(MAX(id), 0) FROM system_role_menu));
