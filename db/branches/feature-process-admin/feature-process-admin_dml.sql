-- =============================================
-- 新增内置角色：流程管理员 (process_admin)
-- 数据库: PostgreSQL
-- 包含: DML（角色数据 + 角色-菜单关联）
-- 租户ID: 123
-- 角色ID: 161
-- 用途: 在 BPM 和 OpsHub 模块中拥有管理员级转派权限
-- =============================================

-- =============================================
-- 一、角色数据（UPSERT 兼容已存在/已删除记录）
-- =============================================

INSERT INTO system_role (id, name, code, sort, data_scope, data_scope_dept_ids, status, type, remark, tenant_id, deleted)
VALUES (161, '流程管理员', 'process_admin', 50, 1, '', 0, 1, '业务角色-流程转派', 123, 0)
ON CONFLICT (id) DO UPDATE SET
  code = EXCLUDED.code, name = EXCLUDED.name, sort = EXCLUDED.sort,
  data_scope = EXCLUDED.data_scope, status = EXCLUDED.status,
  type = EXCLUDED.type, remark = EXCLUDED.remark, deleted = 0;

-- =============================================
-- 二、OpsHub 菜单授权 (role_id=161, tenant_id=123)
--
--   6000  聚院通（一级目录）
--   6008  客户服务（菜单入口）
--   6080  查看工单（service:task:query）
--   6097  转单（dealer:cs-task:transfer）
--   6219  工单详情（隐藏路由，visible=false）
-- =============================================

INSERT INTO system_role_menu (id, role_id, menu_id, tenant_id)
SELECT (SELECT COALESCE(MAX(id),0) FROM system_role_menu) + row_number() OVER (), role_id, menu_id, 123 FROM (
  SELECT 161 AS role_id, 6000 AS menu_id
  UNION ALL SELECT 161, 6008
  UNION ALL SELECT 161, 6080
  UNION ALL SELECT 161, 6097
  UNION ALL SELECT 161, 6219
) t
WHERE NOT EXISTS (
  SELECT 1 FROM system_role_menu rm
  WHERE rm.role_id = t.role_id AND rm.menu_id = t.menu_id AND rm.tenant_id = 123
);

-- =============================================
-- 三、BPM 菜单授权 (role_id=161, tenant_id=123)
--
--   1185  工作流程（一级目录）
--   1200  审批中心（菜单）
--   2724  流程任务（管理员视图 bpm/task/manager/index）
--   2725  bpm:task:manager-query（管理员查询权限）
--   1222  bpm:task:update（任务操作按钮，含转派）
-- =============================================

INSERT INTO system_role_menu (id, role_id, menu_id, tenant_id)
SELECT (SELECT COALESCE(MAX(id),0) FROM system_role_menu) + row_number() OVER (), role_id, menu_id, 123 FROM (
  SELECT 161 AS role_id, 1185 AS menu_id
  UNION ALL SELECT 161, 1200
  UNION ALL SELECT 161, 2724
  UNION ALL SELECT 161, 2725
  UNION ALL SELECT 161, 1222
) t
WHERE NOT EXISTS (
  SELECT 1 FROM system_role_menu rm
  WHERE rm.role_id = t.role_id AND rm.menu_id = t.menu_id AND rm.tenant_id = 123
);

-- =============================================
-- 四、同步序列值（防止应用插入时主键冲突）
-- =============================================

SELECT setval('system_role_seq',      (SELECT COALESCE(MAX(id), 0) FROM system_role));
SELECT setval('system_role_menu_seq', (SELECT COALESCE(MAX(id), 0) FROM system_role_menu));
