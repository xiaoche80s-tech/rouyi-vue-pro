-- =============================================
-- OpsHub Step 19: 政策看板模块
-- 纯 DML（菜单按钮权限 + 角色关联）
-- 租户ID: 123
-- =============================================

-- 按钮权限 - 政策看板（新增 CRUD 按钮）
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, deleted) VALUES
(6302, '创建政策', 'dealer:policy:create', 3, 3, 6004, '', '', '', NULL, 0, true, true, true, 0),
(6303, '更新政策', 'dealer:policy:update', 3, 4, 6004, '', '', '', NULL, 0, true, true, true, 0),
(6304, '删除政策', 'dealer:policy:delete', 3, 5, 6004, '', '', '', NULL, 0, true, true, true, 0);

-- 角色-菜单关联
INSERT INTO system_role_menu (id, role_id, menu_id, tenant_id)
SELECT (SELECT COALESCE(MAX(id),0) FROM system_role_menu) + row_number() OVER (), role_id, menu_id, 123 FROM (
  -- brand_admin (157): 全部 3 个按钮
  SELECT 157 AS role_id, 6302 AS menu_id
  UNION ALL SELECT 157, 6303
  UNION ALL SELECT 157, 6304
  UNION ALL
  -- service_executor (159): 全部 3 个按钮
  SELECT 159, 6302
  UNION ALL SELECT 159, 6303
  UNION ALL SELECT 159, 6304
) t
WHERE NOT EXISTS (
  SELECT 1 FROM system_role_menu rm
  WHERE rm.role_id = t.role_id AND rm.menu_id = t.menu_id AND rm.tenant_id = 123
);

-- 同步序列值
SELECT setval('system_menu_seq',      (SELECT COALESCE(MAX(id), 0) FROM system_menu));
SELECT setval('system_role_menu_seq', (SELECT COALESCE(MAX(id), 0) FROM system_role_menu));
