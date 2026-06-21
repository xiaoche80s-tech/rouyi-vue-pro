-- =============================================
-- OpsHub Step 20: 操作请求工作流
-- 数据库: PostgreSQL
-- 包含: DML（菜单按钮 + 角色关联 + 删除旧权限）
-- 租户ID: 123
-- =============================================

-- =============================================
-- 一、新增操作请求按钮权限
-- 菜单 ID 段: 6310-6313（parent: 6003 签约进度）
-- =============================================

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, deleted) VALUES
(6310, '创建操作请求', 'dealer:op-request:create',  3, 10, 6003, '', '', '', NULL, 0, true, true, true, 0),
(6311, '查询操作请求', 'dealer:op-request:query',   3, 11, 6003, '', '', '', NULL, 0, true, true, true, 0),
(6312, '处理操作请求', 'dealer:op-request:process', 3, 12, 6003, '', '', '', NULL, 0, true, true, true, 0),
(6313, '验收操作请求', 'dealer:op-request:verify',  3, 13, 6003, '', '', '', NULL, 0, true, true, true, 0);

-- 操作请求详情页（隐藏菜单，用于 BPM 表单查看地址）
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, deleted)
VALUES (6314, '操作请求详情', '', 2, 99, 6000, 'signing/op-request-detail', '', 'opshub/oprequest/detail', '', 0, false, true, true, 0);

-- =============================================
-- 二、角色-菜单关联
--
--   brand_admin(157)      ✅ create/query/verify — 管理员可发起、查看、验收
--   brand_sales(158)      ✅ query              — 仅查看
--   service_executor(159) ✅ query/process       — 执行员可查看、处理
--   dealer(160)           ✅ create/query/verify  — 经销商可发起、查看、验收
-- =============================================

INSERT INTO system_role_menu (id, role_id, menu_id, tenant_id, deleted, creator, create_time, updater, update_time)
SELECT (SELECT COALESCE(MAX(id),0) FROM system_role_menu) + row_number() OVER (), role_id, menu_id, 123, 0, '1', NOW(), '1', NOW() FROM (
  SELECT 157 AS role_id, unnest(ARRAY[6310, 6311, 6313]) AS menu_id
  UNION ALL
  SELECT 158, unnest(ARRAY[6311])
  UNION ALL
  SELECT 159, unnest(ARRAY[6311, 6312])
  UNION ALL
  SELECT 160, unnest(ARRAY[6310, 6311, 6313])
) t;

-- =============================================
-- 三、删除旧签约按钮权限
-- 6038 = 上传盖章文件(dealer:signing:upload-proof) — 已被 op-request:process 替代
-- =============================================

-- 删除角色-菜单关联
DELETE FROM system_role_menu WHERE menu_id = 6038 AND tenant_id = 123;

-- 软删除菜单项
UPDATE system_menu SET deleted = 1 WHERE id = 6038;

-- =============================================
-- 四、同步序列值
-- =============================================

SELECT setval('system_menu_seq',      (SELECT COALESCE(MAX(id), 0) FROM system_menu));
SELECT setval('system_role_menu_seq', (SELECT COALESCE(MAX(id), 0) FROM system_role_menu));
