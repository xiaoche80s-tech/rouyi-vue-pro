-- =============================================
-- OpsHub Step 13: Excel 批量导入（独立页面）
-- 数据库: PostgreSQL
-- 包含: DML（新增菜单 + 按钮权限 + 角色-菜单关联）
-- 租户ID: 123
-- =============================================

-- =============================================
-- 一、新增菜单 — 数据批量导入
-- parent_id = 6000（OpsHub 经销商运营中心）
-- path = excel-import
-- component = opshub/excelImport/index
-- =============================================

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, deleted) VALUES
-- 菜单（type=2，visible=true）
(6300, '数据批量导入', '', 2, 90, 6000, 'excel-import', 'ep:upload-filled', 'opshub/excelImport/index', 'ExcelImport', 0, true, true, true, 0),

-- 按钮权限（type=3）
(6301, '下载模板与导入', 'opshub:excel-import:import', 3, 1, 6300, '', '', '', NULL, 0, true, true, true, 0);

-- =============================================
-- 二、角色-菜单关联
--
--   brand_admin(157)      ✅ 分配 — 仅管理员可导入
--   brand_sales(158)      ❌ 不分配
--   service_executor(159) ❌ 不分配
--   dealer(160)           ❌ 不分配
-- =============================================

INSERT INTO system_role_menu (id, role_id, menu_id, tenant_id)
SELECT (SELECT COALESCE(MAX(id),0) FROM system_role_menu) + row_number() OVER (), role_id, menu_id, 123 FROM (
  -- 仅 brand_admin(157) 分配菜单(6300) + 按钮(6301)
  SELECT 157 AS role_id, 6300 AS menu_id
  UNION ALL
  SELECT 157, 6301
) t;

-- =============================================
-- 三、同步序列值（防止应用插入时主键冲突）
-- =============================================

SELECT setval('system_menu_seq',      (SELECT COALESCE(MAX(id), 0) FROM system_menu));
SELECT setval('system_role_menu_seq', (SELECT COALESCE(MAX(id), 0) FROM system_role_menu));
