-- =============================================
-- Step 8 — 咨询服务集成 DML
-- 功能：为基础数据模块补充「咨询」权限按钮
--       + 补充客服会话「发起咨询」权限（cs-consult:create）
-- 日期：2026-06-17
-- =============================================

-- =============================================
-- 一、新增按钮权限
-- 6074：基础数据 — 咨询（parent_id=6007）
-- 6099：客服会话 — 发起咨询（parent_id=6008）
-- =============================================

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, deleted) VALUES
(6074, '基础数据咨询', 'dealer:basedata:consult',   3, 5, 6007, '', '', '', NULL, 0, true, true, true, 0),
(6099, '发起咨询',     'dealer:cs-consult:create', 3, 6, 6008, '', '', '', NULL, 0, true, true, true, 0);

-- =============================================
-- 二、角色-菜单关联
-- 6074 → brand_admin (157) / service_executor (159) / dealer (160)
-- 6099 → brand_admin (157) / service_executor (159) / dealer (160)
-- =============================================

INSERT INTO system_role_menu (id, role_id, menu_id, tenant_id)
SELECT (SELECT COALESCE(MAX(id), 0) FROM system_role_menu) + row_number() OVER (), role_id, menu_id, 123 FROM (
  -- 6074: basedata:consult
  SELECT 157 AS role_id, 6074 AS menu_id
  UNION ALL SELECT 159, 6074
  UNION ALL SELECT 160, 6074
  -- 6099: cs-consult:create
  UNION ALL SELECT 157, 6099
  UNION ALL SELECT 159, 6099
  UNION ALL SELECT 160, 6099
) t;

-- =============================================
-- 三、同步序列值（防止后续应用插入时主键冲突）
-- =============================================

SELECT setval('system_menu_seq',      (SELECT COALESCE(MAX(id), 0) FROM system_menu));
SELECT setval('system_role_menu_seq', (SELECT COALESCE(MAX(id), 0) FROM system_role_menu));
