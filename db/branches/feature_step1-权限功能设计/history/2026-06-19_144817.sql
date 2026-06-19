-- 执行时间: 2026-06-19 14:48:17
-- 分支: feature/step1-权限功能设计
-- 操作说明: OpsHub Step 13 Excel批量导入 - 新增菜单 + 按钮权限 + brand_admin角色授权

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, deleted) VALUES
(6300, '数据批量导入', '', 2, 90, 6000, 'excel-import', 'ep:upload-filled', 'opshub/excelImport/index', 'ExcelImport', 0, true, true, true, 0),
(6301, '下载模板与导入', 'opshub:excel-import:import', 3, 1, 6300, '', '', '', NULL, 0, true, true, true, 0);

INSERT INTO system_role_menu (id, role_id, menu_id, tenant_id)
SELECT (SELECT COALESCE(MAX(id),0) FROM system_role_menu) + row_number() OVER (), role_id, menu_id, 123 FROM (
  SELECT 157 AS role_id, 6300 AS menu_id
  UNION ALL
  SELECT 157, 6301
) t;

SELECT setval('system_menu_seq', (SELECT COALESCE(MAX(id), 0) FROM system_menu));
SELECT setval('system_role_menu_seq', (SELECT COALESCE(MAX(id), 0) FROM system_role_menu));
