-- =============================================
-- OpsHub Step 1: 经销商管理 SaaS 基础设施层
-- 数据库: PostgreSQL
-- 包含: 纯 DML（角色 + 菜单 + 角色-菜单关联）
-- 租户ID: 123
-- =============================================

-- =============================================
-- 一、角色数据（4 条，tenant_id = 123）
-- =============================================

INSERT INTO system_role (id, name, code, sort, data_scope, data_scope_dept_ids, status, type, remark, tenant_id) VALUES
(157, '品牌管理员',   'brand_admin',      10, 1, '', 0, 1, '业务角色',     123),
(158, '品牌销售员',   'brand_sales',      20, 1, '', 0, 1, '业务角色-只读', 123),
(159, '服务单执行员', 'service_executor', 30, 1, '', 0, 1, '业务角色',     123),
(160, '经销商',       'dealer',           40, 1, '', 0, 1, '业务角色',     123);

-- =============================================
-- 二、菜单数据（1 目录 + 9 菜单 + 43 按钮 = 53 条）
-- 注意：system_menu 无 tenant_id 列（全局共享）
-- =============================================

-- 一级目录
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, deleted) VALUES
(6000, '经销商管理 SaaS', '', 1, 5, 0, '/dealer', 'ep:office-building', NULL, NULL, 0, true, true, true, 0);

-- 二级菜单
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, deleted) VALUES
(6001, '经销商管理', '', 2, 10, 6000, 'dealer-mgmt',      'ep:user',          'opshub/dealer/index',      NULL, 0, true, true, true, 0),
(6002, '产品线管理', '', 2, 20, 6000, 'product-line-mgmt', 'ep:goods',         'opshub/productLine/index', NULL, 0, true, true, true, 0),
(6003, '签约进度',   '', 2, 30, 6000, 'signing',           'ep:document',      '',                         NULL, 0, true, true, true, 0),
(6004, '政策看板',   '', 2, 40, 6000, 'policy',            'ep:data-analysis', '',                         NULL, 0, true, true, true, 0),
(6005, '售后模块',   '', 2, 50, 6000, 'aftersale',         'ep:service',       '',                         NULL, 0, true, true, true, 0),
(6006, '订单模块',   '', 2, 60, 6000, 'order',             'ep:shopping-cart', '',                         NULL, 0, true, true, true, 0),
(6007, '基础数据',   '', 2, 70, 6000, 'basedata',          'ep:files',         '',                         NULL, 0, true, true, true, 0),
(6008, '客户服务',   '', 2, 80, 6000, 'service',           'ep:chat-dot-round','',                         NULL, 0, true, true, true, 0),
(6009, '授权管理',   '', 2, 25, 6000, 'scope-mgmt',        'ep:key',           'opshub/scope/index',       NULL, 0, true, true, true, 0);

-- 按钮权限 - 经销商管理
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, deleted) VALUES
(6010, '查看经销商列表', 'dealer:mgmt:query',       3, 1, 6001, '', '', '', NULL, 0, true, true, true, 0),
(6011, '新增经销商',     'dealer:mgmt:create',      3, 2, 6001, '', '', '', NULL, 0, true, true, true, 0),
(6012, '编辑经销商',     'dealer:mgmt:update',      3, 3, 6001, '', '', '', NULL, 0, true, true, true, 0),
(6013, '删除经销商',     'dealer:mgmt:delete',      3, 4, 6001, '', '', '', NULL, 0, true, true, true, 0),
(6014, '绑定产品线(已废弃)', 'dealer:mgmt:bindproduct', 3, 5, 6001, '', '', '', NULL, 0, true, true, true, 0),
(6015, '绑定用户(已废弃)',   'dealer:mgmt:binduser',    3, 6, 6001, '', '', '', NULL, 0, true, true, true, 0);

-- 按钮权限 - 产品线管理
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, deleted) VALUES
(6020, '查看产品线', 'dealer:productline:query',  3, 1, 6002, '', '', '', NULL, 0, true, true, true, 0),
(6021, '新增产品线', 'dealer:productline:create', 3, 2, 6002, '', '', '', NULL, 0, true, true, true, 0),
(6022, '编辑产品线', 'dealer:productline:update', 3, 3, 6002, '', '', '', NULL, 0, true, true, true, 0),
(6023, '删除产品线', 'dealer:productline:delete', 3, 4, 6002, '', '', '', NULL, 0, true, true, true, 0),
(6024, '绑定经销商', 'dealer:productline:binddealer', 3, 5, 6002, '', '', '', NULL, 0, true, true, true, 0);

-- 按钮权限 - 签约进度
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, deleted) VALUES
(6030, '查看签约', 'dealer:signing:query',   3, 1, 6003, '', '', '', NULL, 0, true, true, true, 0),
(6031, '发起签署', 'dealer:signing:sign',    3, 2, 6003, '', '', '', NULL, 0, true, true, true, 0),
(6032, '咨询',     'dealer:signing:consult', 3, 3, 6003, '', '', '', NULL, 0, true, true, true, 0),
(6033, '导入',     'dealer:signing:import',  3, 4, 6003, '', '', '', NULL, 0, true, true, true, 0),
(6034, '申请盖章', 'dealer:signing:stamp',   3, 5, 6003, '', '', '', NULL, 0, true, true, true, 0),
(6035, '导出',     'dealer:signing:export',  3, 6, 6003, '', '', '', NULL, 0, true, true, true, 0);

-- 按钮权限 - 政策看板
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, deleted) VALUES
(6040, '查看政策', 'dealer:policy:query',   3, 1, 6004, '', '', '', NULL, 0, true, true, true, 0),
(6041, '咨询',     'dealer:policy:consult', 3, 2, 6004, '', '', '', NULL, 0, true, true, true, 0);

-- 按钮权限 - 售后模块
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, deleted) VALUES
(6050, '查看售后', 'dealer:aftersale:query',   3, 1, 6005, '', '', '', NULL, 0, true, true, true, 0),
(6051, '咨询',     'dealer:aftersale:consult', 3, 2, 6005, '', '', '', NULL, 0, true, true, true, 0),
(6052, '发起退货', 'dealer:aftersale:return',  3, 3, 6005, '', '', '', NULL, 0, true, true, true, 0);

-- 按钮权限 - 订单模块
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, deleted) VALUES
(6060, '查看订单', 'dealer:order:query',   3, 1, 6006, '', '', '', NULL, 0, true, true, true, 0),
(6061, '申请付款', 'dealer:order:pay',     3, 2, 6006, '', '', '', NULL, 0, true, true, true, 0),
(6062, '申请开票', 'dealer:order:invoice', 3, 3, 6006, '', '', '', NULL, 0, true, true, true, 0),
(6063, '申请退货', 'dealer:order:return',  3, 4, 6006, '', '', '', NULL, 0, true, true, true, 0),
(6064, '咨询',     'dealer:order:consult', 3, 5, 6006, '', '', '', NULL, 0, true, true, true, 0);

-- 按钮权限 - 基础数据
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, deleted) VALUES
(6070, '查看基础数据', 'dealer:basedata:query',     3, 1, 6007, '', '', '', NULL, 0, true, true, true, 0),
(6071, 'AI解读',       'dealer:basedata:ai',        3, 2, 6007, '', '', '', NULL, 0, true, true, true, 0),
(6072, '下载',         'dealer:basedata:download',  3, 3, 6007, '', '', '', NULL, 0, true, true, true, 0),
(6073, '申请盖章',     'dealer:basedata:stamp',     3, 4, 6007, '', '', '', NULL, 0, true, true, true, 0);

-- 按钮权限 - 客户服务
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, deleted) VALUES
(6080, '查看工单',       'service:task:query',    3, 1,  6008, '', '', '', NULL, 0, true, true, true, 0),
(6081, '创建任务',       'service:task:create',   3, 2,  6008, '', '', '', NULL, 0, true, true, true, 0),
(6082, '接单',           'service:task:accept',   3, 3,  6008, '', '', '', NULL, 0, true, true, true, 0),
(6083, '提交结果',       'service:task:submit',   3, 4,  6008, '', '', '', NULL, 0, true, true, true, 0),
(6084, '验收',           'service:task:verify',   3, 5,  6008, '', '', '', NULL, 0, true, true, true, 0),
(6085, '催办',           'service:task:urge',     3, 6,  6008, '', '', '', NULL, 0, true, true, true, 0),
(6086, '查看咨询',       'service:consult:query', 3, 7,  6008, '', '', '', NULL, 0, true, true, true, 0),
(6087, '回复咨询',       'service:consult:reply', 3, 8,  6008, '', '', '', NULL, 0, true, true, true, 0),
(6088, '关闭咨询',       'service:consult:close', 3, 9,  6008, '', '', '', NULL, 0, true, true, true, 0),
(6089, '完成处理',       'service:consult:complete', 3, 10, 6008, '', '', '', NULL, 0, true, true, true, 0),
(6090, '查看操作请求',   'service:opreq:query',   3, 11, 6008, '', '', '', NULL, 0, true, true, true, 0),
(6091, '发起请求',       'service:opreq:create',  3, 12, 6008, '', '', '', NULL, 0, true, true, true, 0),
(6092, '接单(操作)',     'service:opreq:accept',  3, 13, 6008, '', '', '', NULL, 0, true, true, true, 0),
(6093, '提交结果(操作)', 'service:opreq:submit',  3, 14, 6008, '', '', '', NULL, 0, true, true, true, 0),
(6094, '验收(操作)',     'service:opreq:verify',  3, 15, 6008, '', '', '', NULL, 0, true, true, true, 0);

-- 按钮权限 - 授权管理
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, deleted) VALUES
(6095, '查看授权', 'dealer:scope:query',  3, 1, 6009, '', '', '', NULL, 0, true, true, true, 0),
(6096, '分配授权', 'dealer:scope:assign', 3, 2, 6009, '', '', '', NULL, 0, true, true, true, 0);

-- =============================================
-- 三、角色-菜单关联（tenant_id = 123）
-- 注意：system_role_menu 有 id 列（bigint, NOT NULL, 无默认值）
--       使用 row_number() 从当前最大 id+1 开始自动生成
-- =============================================

INSERT INTO system_role_menu (id, role_id, menu_id, tenant_id)
SELECT (SELECT COALESCE(MAX(id),0) FROM system_role_menu) + row_number() OVER (), role_id, menu_id, 123 FROM (
  -- brand_admin (157): 全部菜单 + 全部按钮（53条）
  SELECT 157 AS role_id, unnest(ARRAY[
    6000,6001,6002,6003,6004,6005,6006,6007,6008,6009,
    6010,6011,6012,6013,6014,6015,
    6020,6021,6022,6023,6024,
    6030,6031,6032,6033,6034,6035,
    6040,6041,
    6050,6051,6052,
    6060,6061,6062,6063,6064,
    6070,6071,6072,6073,
    6080,6081,6082,6083,6084,6085,6086,6087,6088,6089,
    6090,6091,6092,6093,6094,
    6095,6096
  ]) AS menu_id
  UNION ALL
  -- brand_sales (158): 只读菜单（19条）
  SELECT 158, unnest(ARRAY[
    6000,6001,6002,6003,6004,6005,6006,6007,6009,
    6010,6020,6024,6030,6040,6050,6060,6070,6095
  ])
  UNION ALL
  -- service_executor (159): 9个菜单 + 按钮（49条）
  SELECT 159, unnest(ARRAY[
    6000,6001,6002,6003,6004,6005,6006,6007,6008,6009,
    6010,6020,
    6030,6031,6032,6033,6034,6035,
    6040,6041,
    6050,6051,6052,
    6060,6061,6062,6063,6064,
    6070,6071,6072,6073,
    6080,6081,6082,6083,6084,6085,6086,6087,6088,6089,
    6090,6091,6092,6093,6094,
    6095
  ])
  UNION ALL
  -- dealer (160): 9个菜单 + 按钮（38条）
  SELECT 160, unnest(ARRAY[
    6000,6001,6002,6003,6004,6005,6006,6007,6008,6009,
    6010,6020,
    6030,6031,6032,6034,
    6040,
    6050,6051,6052,
    6060,6061,6062,6063,6064,
    6070,6071,6072,6073,
    6080,6084,6086,6088,
    6090,6091,6094,
    6095
  ])
) t;

-- =============================================
-- 四、同步序列值（防止应用插入时主键冲突）
-- DML 手动指定了 id，必须推进对应序列
-- =============================================

SELECT setval('system_role_seq',      (SELECT COALESCE(MAX(id), 0) FROM system_role));
SELECT setval('system_menu_seq',      (SELECT COALESCE(MAX(id), 0) FROM system_menu));
SELECT setval('system_role_menu_seq', (SELECT COALESCE(MAX(id), 0) FROM system_role_menu));
