-- =============================================
-- OpsHub Step 3: 签约进度模块
-- 数据库: PostgreSQL
-- 包含: DML（菜单更新 + 新增按钮权限 + 角色-菜单关联 + 测试数据）
-- 租户ID: 123
-- =============================================

-- =============================================
-- 一、菜单更新
-- =============================================

-- 更新签约进度菜单的 component
UPDATE system_menu SET component = 'opshub/signing/index' WHERE id = 6003;

-- =============================================
-- 二、新增按钮权限
-- =============================================

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, deleted) VALUES
(6036, '创建合同',     'dealer:signing:create',      3, 7, 6003, '', '', '', NULL, 0, true, true, true, 0),
(6037, '编辑合同',     'dealer:signing:update',      3, 8, 6003, '', '', '', NULL, 0, true, true, true, 0),
(6038, '上传盖章文件', 'dealer:signing:upload-proof', 3, 9, 6003, '', '', '', NULL, 0, true, true, true, 0);

-- =============================================
-- 三、角色-菜单关联（新增按钮）
-- =============================================

INSERT INTO system_role_menu (id, role_id, menu_id, tenant_id)
SELECT (SELECT COALESCE(MAX(id),0) FROM system_role_menu) + row_number() OVER (), role_id, menu_id, 123 FROM (
  -- brand_admin (157): 创建合同 + 编辑合同
  SELECT 157 AS role_id, unnest(ARRAY[6036, 6037]) AS menu_id
  UNION ALL
  -- service_executor (159): 上传盖章文件
  SELECT 159, unnest(ARRAY[6038])
) t;

-- =============================================
-- 四、测试数据（ops_signing_contract）
-- 经销商: 1001(D001), 1002(D002), 1003(D003)
-- =============================================

INSERT INTO ops_signing_contract (
    id, dealer_id, dealer_code, product_line_code, contract_type, contract_type_name,
    contract_code, contract_name, status, sub_status, issued_date, sign_date,
    summary, policy_analysis, indicators, file_ids, sign_proof_url, remark,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES
-- 主合同 (main)
(1, 101, 'D001', 'PL01', 'main', '主合同', 'MC-2024-001', '2024年度主合同-经销商A', 'signed', NULL,
 '2024-01-15', '2024-02-10', '2024年度经销商A主合同', NULL, NULL, NULL, NULL, '首年签约',
 '1', '2024-01-15 10:00:00', '1', '2024-02-10 15:00:00', 0, 123),

(2, 102, 'D002', 'PL02', 'main', '主合同', 'MC-2024-002', '2024年度主合同-经销商B', 'signed', NULL,
 '2024-01-20', '2024-03-05', '2024年度经销商B主合同', NULL, NULL, NULL, NULL, NULL,
 '1', '2024-01-20 10:00:00', '1', '2024-03-05 15:00:00', 0, 123),

(3, 101, 'D001', 'PL01', 'main', '主合同', 'MC-2025-001', '2025年度主合同-经销商A', 'signed', NULL,
 '2025-01-10', '2025-02-20', '2025年度经销商A续签主合同', NULL, NULL, NULL, NULL, '续签',
 '1', '2025-01-10 10:00:00', '1', '2025-02-20 15:00:00', 0, 123),

(4, 103, 'D003', 'PL03', 'main', '主合同', 'MC-2025-002', '2025年度主合同-经销商C', 'unsigned', 'pending',
 '2025-03-01', NULL, '2025年度经销商C主合同', NULL, NULL, NULL, NULL, '新经销商',
 '1', '2025-03-01 10:00:00', '1', '2025-03-01 10:00:00', 0, 123),

(5, 102, 'D002', 'PL02', 'main', '主合同', 'MC-2026-001', '2026年度主合同-经销商B', 'unsigned', 'signing',
 '2026-01-08', NULL, '2026年度经销商B主合同续签', NULL, NULL, NULL, NULL, '签署中',
 '1', '2026-01-08 10:00:00', '1', '2026-01-08 10:00:00', 0, 123),

-- 政策合同 (policy)
(6, 101, 'D001', 'PL01', 'policy', '政策合同', 'POL-2024-001', '2024年Q1政策合同-经销商A', 'signed', NULL,
 '2024-02-01', '2024-02-28', '2024年第一季度政策合同', '本季度重点推进骨科关节产品线销售',
 '[{"indicatorName":"骨科关节销量","targetValue":500,"unit":"件","quarter":1,"achievedValue":320,"achieveRate":64.0}]',
 NULL, NULL, 'Q1政策',
 '1', '2024-02-01 10:00:00', '1', '2024-02-28 15:00:00', 0, 123),

(7, 102, 'D002', 'PL02', 'policy', '政策合同', 'POL-2024-002', '2024年Q2政策合同-经销商B', 'signed', NULL,
 '2024-04-01', '2024-04-25', '2024年第二季度政策合同', '脊柱产品线推广计划',
 '[{"indicatorName":"脊柱产品销量","targetValue":300,"unit":"件","quarter":2,"achievedValue":280,"achieveRate":93.3}]',
 NULL, NULL, NULL,
 '1', '2024-04-01 10:00:00', '1', '2024-04-25 15:00:00', 0, 123),

(8, 101, 'D001', 'PL01', 'policy', '政策合同', 'POL-2025-001', '2025年Q1政策合同-经销商A', 'unsigned', 'pending',
 '2025-02-01', NULL, '2025年第一季度政策合同', '年度新品推广政策',
 '[{"indicatorName":"新品销量","targetValue":200,"unit":"件","quarter":1,"achievedValue":0,"achieveRate":0}]',
 NULL, NULL, '待签署',
 '1', '2025-02-01 10:00:00', '1', '2025-02-01 10:00:00', 0, 123),

(9, 103, 'D003', 'PL03', 'policy', '政策合同', 'POL-2025-002', '2025年Q2政策合同-经销商C', 'unsigned', 'signing',
 '2025-05-01', NULL, '2025年第二季度政策合同', '经销商C专属推广政策', NULL, NULL, NULL, '签署中',
 '1', '2025-05-01 10:00:00', '1', '2025-05-01 10:00:00', 0, 123),

(10, 102, 'D002', 'PL02', 'policy', '政策合同', 'POL-2026-001', '2026年Q1政策合同-经销商B', 'unsigned', 'pending',
 '2026-01-15', NULL, '2026年第一季度政策合同', NULL, NULL, NULL, NULL, NULL,
 '1', '2026-01-15 10:00:00', '1', '2026-01-15 10:00:00', 0, 123),

-- 补充协议 (supplement)
(11, 101, 'D001', 'PL01', 'supplement', '补充协议', 'SA-2024-001', '主合同补充协议-经销商A', 'signed', NULL,
 '2024-03-15', '2024-04-01', '主合同价格调整补充协议', NULL, NULL, NULL, NULL, '价格调整',
 '1', '2024-03-15 10:00:00', '1', '2024-04-01 15:00:00', 0, 123),

(12, 102, 'D002', 'PL02', 'supplement', '补充协议', 'SA-2024-002', '配送条款补充协议-经销商B', 'signed', NULL,
 '2024-06-01', '2024-06-20', '配送方式和结算条款补充', NULL, NULL, NULL, NULL, NULL,
 '1', '2024-06-01 10:00:00', '1', '2024-06-20 15:00:00', 0, 123),

(13, 101, 'D001', 'PL01', 'supplement', '补充协议', 'SA-2025-001', '2025年返利补充协议-经销商A', 'unsigned', 'pending',
 '2025-03-10', NULL, '年度返利政策补充协议', NULL, NULL, NULL, NULL, '待签署',
 '1', '2025-03-10 10:00:00', '1', '2025-03-10 10:00:00', 0, 123),

(14, 103, 'D003', 'PL03', 'supplement', '补充协议', 'SA-2025-002', '试用期补充协议-经销商C', 'unsigned', 'signing',
 '2025-04-15', NULL, '新经销商试用期特殊条款', NULL, NULL, NULL, NULL, '签署中',
 '1', '2025-04-15 10:00:00', '1', '2025-04-15 10:00:00', 0, 123),

-- 终止协议 (termination)
(15, 101, 'D001', 'PL01', 'termination', '终止协议', 'TA-2024-001', '产品线终止协议-经销商A', 'signed', NULL,
 '2024-09-01', '2024-09-15', '终止PL01产品线合作', NULL, NULL, NULL, NULL, '产品线调整',
 '1', '2024-09-01 10:00:00', '1', '2024-09-15 15:00:00', 0, 123),

(16, 103, 'D003', 'PL03', 'termination', '终止协议', 'TA-2025-001', '2025年终止协议-经销商C', 'unsigned', 'pending',
 '2025-06-01', NULL, '终止经销商C部分产品线合作', NULL, NULL, NULL, NULL, '待确认',
 '1', '2025-06-01 10:00:00', '1', '2025-06-01 10:00:00', 0, 123),

-- 更多2026年数据（趋势图验证）
(17, 101, 'D001', 'PL01', 'main', '主合同', 'MC-2026-002', '2026年度主合同-经销商A', 'signed', NULL,
 '2026-02-01', '2026-02-28', '2026年度经销商A主合同', NULL, NULL, NULL, NULL, NULL,
 '1', '2026-02-01 10:00:00', '1', '2026-02-28 15:00:00', 0, 123),

(18, 103, 'D003', 'PL03', 'policy', '政策合同', 'POL-2026-002', '2026年Q1政策合同-经销商C', 'signed', NULL,
 '2026-03-01', '2026-03-20', '2026年第一季度政策合同-经销商C', NULL, NULL, NULL, NULL, NULL,
 '1', '2026-03-01 10:00:00', '1', '2026-03-20 15:00:00', 0, 123),

(19, 102, 'D002', 'PL02', 'supplement', '补充协议', 'SA-2026-001', '2026年物流补充协议-经销商B', 'unsigned', 'pending',
 '2026-04-01', NULL, '物流配送条款补充协议', NULL, NULL, NULL, NULL, NULL,
 '1', '2026-04-01 10:00:00', '1', '2026-04-01 10:00:00', 0, 123),

(20, 101, 'D001', 'PL01', 'termination', '终止协议', 'TA-2026-001', '2026年终止协议-经销商A', 'unsigned', 'signing',
 '2026-05-01', NULL, '终止部分产品线合作', NULL, NULL, NULL, NULL, NULL,
 '1', '2026-05-01 10:00:00', '1', '2026-05-01 10:00:00', 0, 123);
