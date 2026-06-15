-- DML 汇总文件
-- ================================================
-- 分支: feature/step1-权限功能设计
-- 描述: 所有 DML 数据变更汇总
-- ================================================

-- ====== Step3 签约进度模块 (2026-06-15) ======

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

-- 20 条测试数据 ops_signing_contract (ID 1-20, 覆盖4种合同类型×3经销商×跨年份)

-- ====== Step4 订单模块 (2026-06-15) ======

UPDATE system_menu SET component = 'opshub/order/index' WHERE id = 6006;

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, deleted) VALUES
(6065, '更新进度', 'dealer:order:update', 3, 6, 6006, '', '', '', NULL, 0, true, true, true, 0);

INSERT INTO system_role_menu (id, role_id, menu_id, tenant_id) SELECT (SELECT COALESCE(MAX(id),0) FROM system_role_menu) + row_number() OVER (), role_id, menu_id, 123 FROM (SELECT 157 AS role_id, 6065 AS menu_id UNION ALL SELECT 159, 6065) t;

-- 25 条订单数据 + 14 条产品明细 + 25 条时间线 + 4 条付款记录 + 4 条开票记录 + 9 条物流轨迹
-- 详见: db/branches/feature_step4-订单模块/feature_step4-订单模块_dml.sql
