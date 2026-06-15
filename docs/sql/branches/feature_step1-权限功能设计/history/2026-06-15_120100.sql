-- 执行时间: 2026-06-15 12:01:00
-- 分支: feature/step1-权限功能设计
-- 操作说明: Step3 签约进度模块 - 菜单更新 + 按钮权限 + 角色关联 + 测试数据

UPDATE system_menu SET component = 'opshub/signing/index' WHERE id = 6003;

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, deleted) VALUES
(6036, '创建合同',     'dealer:signing:create',      3, 7, 6003, '', '', '', NULL, 0, true, true, true, 0),
(6037, '编辑合同',     'dealer:signing:update',      3, 8, 6003, '', '', '', NULL, 0, true, true, true, 0),
(6038, '上传盖章文件', 'dealer:signing:upload-proof', 3, 9, 6003, '', '', '', NULL, 0, true, true, true, 0);

INSERT INTO system_role_menu (id, role_id, menu_id, tenant_id)
SELECT (SELECT COALESCE(MAX(id),0) FROM system_role_menu) + row_number() OVER (), role_id, menu_id, 123 FROM (
  SELECT 157 AS role_id, unnest(ARRAY[6036, 6037]) AS menu_id
  UNION ALL
  SELECT 159, unnest(ARRAY[6038])
) t;

-- 20 条测试数据 ops_signing_contract (ID 1-20)
