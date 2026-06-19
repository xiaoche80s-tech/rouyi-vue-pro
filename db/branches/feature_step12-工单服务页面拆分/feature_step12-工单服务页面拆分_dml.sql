-- =============================================
-- OpsHub Step 12: 工单服务页面拆分
-- 数据库: PostgreSQL
-- 包含: DML（新增菜单 + 角色-菜单关联变更）
-- 租户ID: 123
--
-- 背景: 将"客户服务"(6008) 按角色拆分为两个页面入口：
--   - 客户服务 (6008) → 执行员/管理员使用（保持不变）
--   - 工单服务 (6200) → 经销商使用（新增）
-- =============================================

-- =============================================
-- 一、新增菜单 — 工单服务 (6200) + 按钮权限 (6201-6218) + 隐藏详情路由 (6219)
-- =============================================

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, deleted) VALUES
-- 工单服务主菜单（与 6008 客户服务平级）
(6200, '工单服务', '', 2, 81, 6000, 'workorder-service', 'ep:tickets', 'opshub/workorder-service/index', NULL, 0, true, true, true, 0),

-- 工单按钮（与 6080-6085 对应，permission 完全一致）
(6201, '查看工单',     'dealer:cs-task:query',     3, 1,  6200, '', '', '', NULL, 0, true, true, true, 0),
(6202, '创建任务',     'dealer:cs-task:create',    3, 2,  6200, '', '', '', NULL, 0, true, true, true, 0),
(6203, '接单',         'dealer:cs-task:accept',    3, 3,  6200, '', '', '', NULL, 0, true, true, true, 0),
(6204, '提交结果',     'dealer:cs-task:deliver',   3, 4,  6200, '', '', '', NULL, 0, true, true, true, 0),
(6205, '验收',         'dealer:cs-task:verify',    3, 5,  6200, '', '', '', NULL, 0, true, true, true, 0),
(6206, '催办',         'dealer:cs-task:urge',      3, 6,  6200, '', '', '', NULL, 0, true, true, true, 0),

-- 咨询按钮（与 6086-6089 对应）
(6207, '查看咨询',     'dealer:cs-consult:query',  3, 7,  6200, '', '', '', NULL, 0, true, true, true, 0),
(6208, '回复咨询',     'dealer:cs-consult:reply',  3, 8,  6200, '', '', '', NULL, 0, true, true, true, 0),
(6209, '关闭咨询',     'dealer:cs-consult:close',  3, 9,  6200, '', '', '', NULL, 0, true, true, true, 0),
(6210, '完成处理',     'dealer:cs-consult:complete', 3, 10, 6200, '', '', '', NULL, 0, true, true, true, 0),

-- 操作请求按钮（与 6090-6094 对应）
(6211, '查看操作请求', 'dealer:cs-opreq:query',    3, 11, 6200, '', '', '', NULL, 0, true, true, true, 0),
(6212, '发起请求',     'dealer:cs-opreq:create',   3, 12, 6200, '', '', '', NULL, 0, true, true, true, 0),
(6213, '接单(操作)',   'dealer:cs-opreq:accept',   3, 13, 6200, '', '', '', NULL, 0, true, true, true, 0),
(6214, '提交结果(操作)', 'dealer:cs-opreq:submit', 3, 14, 6200, '', '', '', NULL, 0, true, true, true, 0),
(6215, '验收(操作)',   'dealer:cs-opreq:verify',   3, 15, 6200, '', '', '', NULL, 0, true, true, true, 0),

-- 扩展按钮（与 6097/6098/6099 对应）
(6216, '转单',         'dealer:cs-task:transfer',   3, 16, 6200, '', '', '', NULL, 0, true, true, true, 0),
(6217, '重新处理',     'dealer:cs-task:reprocess',  3, 17, 6200, '', '', '', NULL, 0, true, true, true, 0),
(6218, '发起咨询',     'dealer:cs-consult:create',  3, 18, 6200, '', '', '', NULL, 0, true, true, true, 0),

-- 隐藏菜单：工单详情路由（visible=false，不在侧边栏显示但注册为路由）
(6219, '工单详情',     '',                          2, 99, 6000, 'workorder-service/task-detail', '', 'opshub/customerservice/task-detail', NULL, 0, false, true, true, 0);

-- =============================================
-- 二、角色-菜单关联变更
--
--   brand_admin(157):       保持 6008 + 新增 6219（工单详情路由）
--   brand_sales(158):       无变更
--   service_executor(159):  保持 6008 + 新增 6219（工单详情路由）
--   dealer(160):            移除 6008 + 新增 6200 及部分按钮 + 新增 6219
-- =============================================

-- 2.1 移除 dealer(160) 对 6008（客户服务）及其按钮的授权
DELETE FROM system_role_menu
WHERE role_id = 160
  AND menu_id IN (6008, 6080, 6081, 6084, 6086, 6088, 6090, 6091, 6094, 6099)
  AND tenant_id = 123;

-- 2.2 为 brand_admin(157) 新增 6219（工单详情路由）
INSERT INTO system_role_menu (id, role_id, menu_id, tenant_id)
SELECT (SELECT COALESCE(MAX(id),0) FROM system_role_menu) + 1, 157, 6219, 123
WHERE NOT EXISTS (
  SELECT 1 FROM system_role_menu WHERE role_id = 157 AND menu_id = 6219 AND tenant_id = 123
);

-- 2.3 为 service_executor(159) 新增 6219（工单详情路由）
INSERT INTO system_role_menu (id, role_id, menu_id, tenant_id)
SELECT (SELECT COALESCE(MAX(id),0) FROM system_role_menu) + 1, 159, 6219, 123
WHERE NOT EXISTS (
  SELECT 1 FROM system_role_menu WHERE role_id = 159 AND menu_id = 6219 AND tenant_id = 123
);

-- 2.4 为 dealer(160) 新增 6200（工单服务）+ 授权按钮子集 + 6219（工单详情路由）
--
-- dealer 在 6200 下授权的按钮子集（14 条）:
--   6200(工单服务主菜单)
--   6201(查看工单), 6202(创建任务), 6203(接单), 6205(验收), 6206(催办)
--   6207(查看咨询), 6209(关闭咨询), 6218(发起咨询)
--   6211(查看操作请求), 6212(发起请求), 6215(验收操作)
--   6219(工单详情路由)
INSERT INTO system_role_menu (id, role_id, menu_id, tenant_id)
SELECT (SELECT COALESCE(MAX(id),0) FROM system_role_menu) + row_number() OVER (), role_id, menu_id, 123 FROM (
  SELECT 160 AS role_id, unnest(ARRAY[
    6200,
    6201, 6202, 6203, 6205, 6206,
    6207, 6209, 6218,
    6211, 6212, 6215,
    6219
  ]) AS menu_id
) t;

-- =============================================
-- 三、同步序列值（防止应用插入时主键冲突）
-- =============================================

SELECT setval('system_menu_seq',      (SELECT COALESCE(MAX(id), 0) FROM system_menu));
SELECT setval('system_role_menu_seq', (SELECT COALESCE(MAX(id), 0) FROM system_role_menu));
