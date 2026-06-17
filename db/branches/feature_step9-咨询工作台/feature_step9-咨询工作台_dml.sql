-- =============================================
-- OpsHub Step 9: 咨询工作台（IM 聊天窗口风格独立页面）
-- 数据库: PostgreSQL
-- 包含: DML（新增菜单 + 角色-菜单关联）
-- 租户ID: 123
-- =============================================

-- =============================================
-- 一、新增菜单 — 咨询工作台
-- parent_id = 6000（OpsHub 经销商运营中心）
-- path = cs-workbench
-- component = opshub/csWorkbench/index
-- 权限复用 dealer:cs-consult:query
-- =============================================

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, deleted) VALUES
(6100, '咨询工作台', 'dealer:cs-consult:query', 2, 80, 6000, 'cs-workbench', 'ep:chat-line-round', 'opshub/csWorkbench/index', 'CsWorkbench', 0, true, true, true, 0);

-- =============================================
-- 二、角色-菜单关联
--
--   brand_admin(157)      ✅ 分配 — 管理员可访问咨询工作台
--   brand_sales(158)      ❌ 不分配 — 无客服操作权限
--   service_executor(159) ✅ 分配 — 执行员主要使用者
--   dealer(160)           ✅ 分配 — 经销商可查看历史
-- =============================================

INSERT INTO system_role_menu (id, role_id, menu_id, tenant_id)
SELECT (SELECT COALESCE(MAX(id),0) FROM system_role_menu) + row_number() OVER (), role_id, menu_id, 123 FROM (
  SELECT 157 AS role_id, 6100 AS menu_id
  UNION ALL
  SELECT 159, 6100
  UNION ALL
  SELECT 160, 6100
) t;

-- =============================================
-- 三、同步序列值（防止应用插入时主键冲突）
-- =============================================

SELECT setval('system_menu_seq',      (SELECT COALESCE(MAX(id), 0) FROM system_menu));
SELECT setval('system_role_menu_seq', (SELECT COALESCE(MAX(id), 0) FROM system_role_menu));
