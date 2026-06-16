-- 执行时间: 2026-06-15 23:35:00
-- 分支: feature/step1-权限功能设计
-- 操作说明: Step 5 售后模块 DML — 菜单更新 + 按钮权限 + 角色关联 + 19条售后主表 + 95条进度节点

-- 一、菜单更新
UPDATE system_menu SET component = 'opshub/aftersale/index' WHERE id = 6005;

-- 二、新增按钮权限
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, deleted) VALUES
(6053, '更新进度', 'dealer:aftersale:update-progress', 3, 4, 6005, '', '', '', NULL, 0, true, true, true, 0);

-- 三、角色-菜单关联
INSERT INTO system_role_menu (id, role_id, menu_id, tenant_id)
SELECT (SELECT COALESCE(MAX(id),0) FROM system_role_menu) + row_number() OVER (), role_id, menu_id, 123 FROM (
  SELECT 157 AS role_id, 6053 AS menu_id
  UNION ALL
  SELECT 159, 6053
) t;

-- 四、售后主表测试数据 (19条)
-- INSERT INTO ops_aftersale_info ... (19 records, ids 1-19)

-- 五、进度节点测试数据 (95条)
-- INSERT INTO ops_aftersale_progress ... (95 records, ids 1-95)
