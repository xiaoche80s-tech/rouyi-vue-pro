-- =============================================
-- OpsHub Step 6: 客户服务模块
-- 数据库: PostgreSQL
-- 包含: DML（权限统一 + 新增按钮 + 角色-菜单关联 + 菜单更新 + 测试数据 + 站内信模板）
-- 租户ID: 123
-- =============================================

-- =============================================
-- 一、权限标识统一 — service:* → dealer:cs-*
-- Step 1 菜单 id 6080-6094 的 permission 字段统一为 dealer:cs-* 前缀
-- =============================================

-- 工单按钮权限标识统一
UPDATE system_menu SET permission = 'dealer:cs-task:query'    WHERE id = 6080;
UPDATE system_menu SET permission = 'dealer:cs-task:create'   WHERE id = 6081;
UPDATE system_menu SET permission = 'dealer:cs-task:accept'   WHERE id = 6082;
UPDATE system_menu SET permission = 'dealer:cs-task:deliver'  WHERE id = 6083;
UPDATE system_menu SET permission = 'dealer:cs-task:verify'   WHERE id = 6084;
UPDATE system_menu SET permission = 'dealer:cs-task:urge'     WHERE id = 6085;

-- 咨询按钮权限标识统一（预留，功能不实现）
UPDATE system_menu SET permission = 'dealer:cs-consult:query'   WHERE id = 6086;
UPDATE system_menu SET permission = 'dealer:cs-consult:reply'   WHERE id = 6087;
UPDATE system_menu SET permission = 'dealer:cs-consult:close'   WHERE id = 6088;
UPDATE system_menu SET permission = 'dealer:cs-consult:complete' WHERE id = 6089;

-- 操作请求按钮权限标识统一
UPDATE system_menu SET permission = 'dealer:cs-opreq:query'   WHERE id = 6090;
UPDATE system_menu SET permission = 'dealer:cs-opreq:create'  WHERE id = 6091;
UPDATE system_menu SET permission = 'dealer:cs-opreq:accept'  WHERE id = 6092;
UPDATE system_menu SET permission = 'dealer:cs-opreq:submit'  WHERE id = 6093;
UPDATE system_menu SET permission = 'dealer:cs-opreq:verify'  WHERE id = 6094;

-- =============================================
-- 二、新增按钮权限
-- =============================================

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, deleted) VALUES
(6097, '转单',         'dealer:cs-task:transfer',   3, 16, 6008, '', '', '', NULL, 0, true, true, true, 0),
(6098, '重新处理',     'dealer:cs-task:reprocess',  3, 17, 6008, '', '', '', NULL, 0, true, true, true, 0);

-- =============================================
-- 三、菜单 component 更新
-- =============================================

-- 更新客户服务菜单的 component
UPDATE system_menu SET component = 'opshub/customerservice/index' WHERE id = 6008;

-- =============================================
-- 四、角色-菜单关联（新增按钮 6097=转单、6098=重新处理）
--
--   brand_admin(157)      ✅ 分配 — 管理员可转单/重新处理
--   brand_sales(158)      ❌ 不分配 — 仅查看权限
--   service_executor(159) ✅ 分配 — 执行员可转单/重新处理
--   dealer(160)           ❌ 不分配 — 经销商无此操作权限
--
-- 同时补齐 dealer(160) 缺失的工单按钮：
--   6081(创建工单)、6082(接单-执行员权限dealer不需要)、6083(交付-执行员权限)
--   6085(催办)、6090(查看操作请求)、6091(发起请求)、6094(验收操作)
-- =============================================

INSERT INTO system_role_menu (id, role_id, menu_id, tenant_id)
SELECT (SELECT COALESCE(MAX(id),0) FROM system_role_menu) + row_number() OVER (), role_id, menu_id, 123 FROM (
  -- brand_admin(157): 新增 6097 + 6098
  SELECT 157 AS role_id, unnest(ARRAY[6097, 6098]) AS menu_id
  UNION ALL
  -- service_executor(159): 新增 6097 + 6098
  SELECT 159, unnest(ARRAY[6097, 6098])
) t;

-- =============================================
-- 五、测试数据 — 操作请求 (ops_cs_opreq)
-- 经销商: D001(华康), D002(康源), D003(明德)
-- 产品线: PL01(骨科), PL02(心内科), PL03(外科), PL04(神经外科)
-- 操作类型: sign/invoice/stamp/aftersale
-- 状态: 0=待处理 1=处理中 2=等待验收 3=已完成
-- =============================================

INSERT INTO ops_cs_opreq (
    id, opreq_code, op_type, status, dealer_code, dealer_name, product_line_code, product_line_name,
    source_module, source_id, source_code, content,
    creator_user_id, assignee_id, accept_time, submit_time, submit_remark, verify_time, completed_time, remark,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES
-- ===== PENDING (待处理) =====
(1, 'OPR-20260616-001', 'sign', 0, 'D001', '华康医疗器械有限公司', 'PL01', '骨科',
 'signing', 101, 'CON-2026-0101-001', '请完成合同 CON-2026-0101-001 的签署归档',
 200, NULL, NULL, NULL, NULL, NULL, NULL, '经销商发起签署请求',
 '200', '2026-06-16 09:00:00', '200', '2026-06-16 09:00:00', 0, 123),

(2, 'OPR-20260616-002', 'aftersale', 0, 'D002', '北京康源医疗科技有限公司', 'PL02', '心内科',
 'aftersale', 201, 'SO-2026-0210-001', '售后工单处理：设备安装调试问题反馈',
 201, NULL, NULL, NULL, NULL, NULL, NULL, '经销商发起售后请求',
 '201', '2026-06-16 09:30:00', '201', '2026-06-16 09:30:00', 0, 123),

(3, 'OPR-20260616-003', 'invoice', 0, 'D003', '上海明德医疗器械有限公司', 'PL03', '外科',
 'order', 202, 'ORD-2026-0225-001', '订单 ORD-2026-0225-001 申请开票，金额 ¥120,000',
 202, NULL, NULL, NULL, NULL, NULL, NULL, '经销商发起开票请求',
 '202', '2026-06-16 10:00:00', '202', '2026-06-16 10:00:00', 0, 123),

(4, 'OPR-20260616-004', 'aftersale', 0, 'D001', '华康医疗器械有限公司', 'PL01', '骨科',
 'aftersale', 1, 'SO20260615-001', '售后退货确认：物流跟踪异常',
 200, NULL, NULL, NULL, NULL, NULL, NULL, '经销商发起售后退货确认',
 '200', '2026-06-16 10:30:00', '200', '2026-06-16 10:30:00', 0, 123),

(5, 'OPR-20260616-005', 'stamp', 0, 'D002', '北京康源医疗科技有限公司', NULL, NULL,
 'basedata', NULL, NULL, '申请资质文件盖章：医疗器械经营许可证副本',
 201, NULL, NULL, NULL, NULL, NULL, NULL, '经销商发起盖章请求',
 '201', '2026-06-16 11:00:00', '201', '2026-06-16 11:00:00', 0, 123),

-- ===== IN_PROGRESS (处理中) =====
(6, 'OPR-20260615-006', 'sign', 1, 'D003', '上海明德医疗器械有限公司', 'PL03', '外科',
 'signing', 102, 'CON-2026-0225-001', '合同 CON-2026-0225-001 签署归档处理',
 202, 100, '2026-06-15 10:00:00', NULL, NULL, NULL, NULL, '执行员已接单处理中',
 '202', '2026-06-15 09:00:00', '100', '2026-06-15 10:00:00', 0, 123),

(7, 'OPR-20260615-007', 'aftersale', 1, 'D001', '华康医疗器械有限公司', 'PL01', '骨科',
 'aftersale', 203, 'SO-2026-0115-001', '售后退货处理：产品批次质量问题',
 200, 100, '2026-06-15 11:00:00', NULL, NULL, NULL, NULL, '执行员已接单',
 '200', '2026-06-15 10:00:00', '100', '2026-06-15 11:00:00', 0, 123),

(8, 'OPR-20260615-008', 'invoice', 1, 'D002', '北京康源医疗科技有限公司', 'PL02', '心内科',
 'order', 204, 'ORD-2026-0210-002', '订单 ORD-2026-0210-002 开票处理',
 201, 101, '2026-06-15 14:00:00', NULL, NULL, NULL, NULL, '执行员已接单处理中',
 '201', '2026-06-15 13:00:00', '101', '2026-06-15 14:00:00', 0, 123),

(9, 'OPR-20260614-009', 'stamp', 1, 'D003', '上海明德医疗器械有限公司', NULL, NULL,
 'basedata', NULL, NULL, '申请营业执照副本盖章',
 202, 100, '2026-06-14 15:00:00', NULL, NULL, NULL, NULL, '执行员正在处理盖章',
 '202', '2026-06-14 14:00:00', '100', '2026-06-14 15:00:00', 0, 123),

(10, 'OPR-20260614-010', 'aftersale', 1, 'D001', '华康医疗器械有限公司', 'PL02', '心内科',
 'aftersale', 7, 'SO20260615-011', '售后换货处理：发错产品需换货',
 200, 101, '2026-06-14 16:00:00', NULL, NULL, NULL, NULL, '执行员处理售后中',
 '200', '2026-06-14 15:00:00', '101', '2026-06-14 16:00:00', 0, 123),

-- ===== DELIVERED (等待验收) =====
(11, 'OPR-20260614-011', 'sign', 2, 'D002', '北京康源医疗科技有限公司', 'PL02', '心内科',
 'signing', 103, 'CON-2026-0210-001', '合同 CON-2026-0210-001 签署完成待验收',
 201, 100, '2026-06-13 10:00:00', '2026-06-14 09:00:00', '签署文件已归档，请验收', NULL, NULL, NULL,
 '201', '2026-06-13 09:00:00', '100', '2026-06-14 09:00:00', 0, 123),

(12, 'OPR-20260613-012', 'aftersale', 2, 'D001', '华康医疗器械有限公司', 'PL01', '骨科',
 'aftersale', 205, 'SO-2026-0115-002', '售后换货处理：规格不符需换货',
 200, 100, '2026-06-12 10:00:00', '2026-06-13 14:00:00', '换货物流已安排', NULL, NULL, NULL,
 '200', '2026-06-12 09:00:00', '100', '2026-06-13 14:00:00', 0, 123),

(13, 'OPR-20260613-013', 'invoice', 2, 'D003', '上海明德医疗器械有限公司', 'PL03', '外科',
 'order', 206, 'ORD-2026-0225-002', '开票完成待验收',
 202, 101, '2026-06-12 14:00:00', '2026-06-13 16:00:00', '发票已开具，发票号 INV-20260613-001', NULL, NULL, NULL,
 '202', '2026-06-12 13:00:00', '101', '2026-06-13 16:00:00', 0, 123),

(14, 'OPR-20260612-014', 'aftersale', 2, 'D002', '北京康源医疗科技有限公司', 'PL03', '外科',
 'aftersale', 8, 'SO20260615-012', '售后退货处理完成待验收',
 201, 100, '2026-06-11 10:00:00', '2026-06-12 15:00:00', '退货物流已确认', NULL, NULL, NULL,
 '201', '2026-06-11 09:00:00', '100', '2026-06-12 15:00:00', 0, 123),

(15, 'OPR-20260612-015', 'stamp', 2, 'D001', '华康医疗器械有限公司', NULL, NULL,
 'basedata', NULL, NULL, '资质文件盖章完成待验收',
 200, 101, '2026-06-11 14:00:00', '2026-06-12 16:00:00', '盖章完成，文件已寄出', NULL, NULL, NULL,
 '200', '2026-06-11 13:00:00', '101', '2026-06-12 16:00:00', 0, 123),

-- ===== CLOSED (已完成) =====
(16, 'OPR-20260610-016', 'sign', 3, 'D001', '华康医疗器械有限公司', 'PL01', '骨科',
 'signing', 104, 'CON-2026-0115-001', '合同签署完成',
 200, 100, '2026-06-09 10:00:00', '2026-06-10 09:00:00', '签署完成', '2026-06-10 14:00:00', '2026-06-10 14:00:00', NULL,
 '200', '2026-06-09 09:00:00', '100', '2026-06-10 14:00:00', 0, 123),

(17, 'OPR-20260610-017', 'aftersale', 3, 'D003', '上海明德医疗器械有限公司', 'PL03', '外科',
 'aftersale', 207, 'SO-2026-0225-003', '售后维修处理：设备故障维修确认',
 202, 101, '2026-06-09 14:00:00', '2026-06-10 10:00:00', '维修已完成', '2026-06-10 16:00:00', '2026-06-10 16:00:00', NULL,
 '202', '2026-06-09 13:00:00', '101', '2026-06-10 16:00:00', 0, 123),

(18, 'OPR-20260608-018', 'invoice', 3, 'D002', '北京康源医疗科技有限公司', 'PL02', '心内科',
 'order', 208, 'ORD-2026-0210-003', '开票完成',
 201, 100, '2026-06-07 10:00:00', '2026-06-08 09:00:00', '发票已开具', '2026-06-08 14:00:00', '2026-06-08 14:00:00', NULL,
 '201', '2026-06-07 09:00:00', '100', '2026-06-08 14:00:00', 0, 123),

(19, 'OPR-20260608-019', 'aftersale', 3, 'D001', '华康医疗器械有限公司', 'PL01', '骨科',
 'aftersale', 13, 'SO20260615-014', '售后维修完成：设备已修复验收通过',
 200, 101, '2026-06-07 14:00:00', '2026-06-08 10:00:00', '维修完成', '2026-06-08 16:00:00', '2026-06-08 16:00:00', NULL,
 '200', '2026-06-07 13:00:00', '101', '2026-06-08 16:00:00', 0, 123),

(20, 'OPR-20260605-020', 'stamp', 3, 'D003', '上海明德医疗器械有限公司', NULL, NULL,
 'basedata', NULL, NULL, '营业执照盖章完成',
 202, 100, '2026-06-04 10:00:00', '2026-06-05 09:00:00', '盖章完成', '2026-06-05 14:00:00', '2026-06-05 14:00:00', NULL,
 '202', '2026-06-04 09:00:00', '100', '2026-06-05 14:00:00', 0, 123);

-- =============================================
-- 六、测试数据 — 工单增强字段更新（更新已有工单数据增加新字段值）
-- 注：仅在 ops_cs_task 表已有数据时执行 UPDATE
-- =============================================

-- 为现有工单补充 dealer_name / product_line_code / product_line_name / source_module
-- 假设现有工单 ID 1-5，如果不存在则跳过
UPDATE ops_cs_task SET dealer_name = '华康医疗器械有限公司', product_line_code = 'PL01', product_line_name = '骨科', source_module = 'manual' WHERE dealer_code = 'D001' AND dealer_name IS NULL;
UPDATE ops_cs_task SET dealer_name = '北京康源医疗科技有限公司', product_line_code = 'PL02', product_line_name = '心内科', source_module = 'manual' WHERE dealer_code = 'D002' AND dealer_name IS NULL;
UPDATE ops_cs_task SET dealer_name = '上海明德医疗器械有限公司', product_line_code = 'PL03', product_line_name = '外科', source_module = 'manual' WHERE dealer_code = 'D003' AND dealer_name IS NULL;

-- 插入新工单测试数据（含增强字段）
INSERT INTO ops_cs_task (
    id, task_no, content, urgency, status, creator_user_id, assignee_id, category, sla_deadline,
    dealer_code, dealer_name, product_line_code, product_line_name, source_module,
    remark, accept_time, deliver_time, verify_time, reject_reason, process_instance_id,
    creator, create_time, updater, update_time, deleted, tenant_id
) VALUES
(100, 'TASK-20260616-001', '合同 CON-2026-0101-001 签署进度延迟，请加快处理', 1, 0, 200, NULL, 0, '2026-06-18 18:00:00',
 'D001', '华康医疗器械有限公司', 'PL01', '骨科', 'signing',
 '签约进度模块自动创建', NULL, NULL, NULL, NULL, NULL,
 '200', '2026-06-16 09:00:00', '200', '2026-06-16 09:00:00', 0, 123),

(101, 'TASK-20260616-002', '订单 ORD-2026-0210-001 付款审批异常', 0, 1, 201, 100, 3, '2026-06-17 12:00:00',
 'D002', '北京康源医疗科技有限公司', 'PL02', '心内科', 'order',
 '紧急：付款审批流程卡住', '2026-06-16 09:30:00', NULL, NULL, NULL, NULL,
 '201', '2026-06-16 09:15:00', '100', '2026-06-16 09:30:00', 0, 123),

(102, 'TASK-20260616-003', '售后退货单 SO20260615-001 物流跟踪异常', 1, 2, 200, 101, 2, '2026-06-19 18:00:00',
 'D001', '华康医疗器械有限公司', 'PL01', '骨科', 'aftersale',
 '退货物流3天未更新', '2026-06-15 10:00:00', '2026-06-16 14:00:00', NULL, NULL, NULL,
 '200', '2026-06-15 09:00:00', '101', '2026-06-16 14:00:00', 0, 123),

(103, 'TASK-20260616-004', '基础数据文件下载权限问题', 2, 3, 202, 100, 4, '2026-06-20 18:00:00',
 'D003', '上海明德医疗器械有限公司', 'PL03', '外科', 'basedata',
 '文件权限已修复', '2026-06-14 10:00:00', '2026-06-15 09:00:00', '2026-06-15 14:00:00', NULL, NULL,
 '202', '2026-06-14 09:00:00', '100', '2026-06-15 14:00:00', 0, 123),

(104, 'TASK-20260616-005', '产品线 PL04 价格表更新请求', 2, 0, 201, NULL, 5, '2026-06-22 18:00:00',
 'D002', '北京康源医疗科技有限公司', 'PL04', '神经外科', 'manual',
 '手动创建：产品线价格表更新', NULL, NULL, NULL, NULL, NULL,
 '201', '2026-06-16 11:00:00', '201', '2026-06-16 11:00:00', 0, 123),

(105, 'TASK-20260616-006', '合同 CON-2026-0225-001 盖章流程催促', 1, 4, 202, 101, 0, '2026-06-18 12:00:00',
 'D003', '上海明德医疗器械有限公司', 'PL03', '外科', 'signing',
 '盖章退回：文件信息不完整', '2026-06-15 14:00:00', '2026-06-16 09:00:00', '2026-06-16 15:00:00', '文件信息不完整，请补充附件', NULL,
 '202', '2026-06-15 13:00:00', '101', '2026-06-16 15:00:00', 0, 123);

-- =============================================
-- 七、站内信模板 — 操作请求通知（4 条）
-- 注：system_notify_template 表结构参考芋道框架标准模板
-- =============================================

INSERT INTO system_notify_template (name, code, nickname, content, params, status, remark, creator, create_time, updater, update_time, deleted, tenant_id) VALUES
('操作请求-创建通知',   'cs-opreq-created',   '系统通知', '您有新的操作请求待处理，编号：{opreqCode}，类型：{opType}',     '["opreqCode","opType"]',     0, '操作请求创建后通知处理人', '1', '2026-06-16 00:00:00', '1', '2026-06-16 00:00:00', 0, 123),
('操作请求-接单通知',   'cs-opreq-accepted',  '系统通知', '操作请求 {opreqCode} 已被接单，处理人正在处理中',                  '["opreqCode"]',              0, '执行员接单后通知发起人',   '1', '2026-06-16 00:00:00', '1', '2026-06-16 00:00:00', 0, 123),
('操作请求-提交通知',   'cs-opreq-submitted', '系统通知', '操作请求 {opreqCode} 已提交结果，请验收',                           '["opreqCode"]',              0, '执行员提交后通知发起人验收', '1', '2026-06-16 00:00:00', '1', '2026-06-16 00:00:00', 0, 123),
('操作请求-验收通知',   'cs-opreq-verified',  '系统通知', '操作请求 {opreqCode} 已验收完成',                                   '["opreqCode"]',              0, '经销商验收后通知处理人',   '1', '2026-06-16 00:00:00', '1', '2026-06-16 00:00:00', 0, 123);

-- =============================================
-- 八、同步序列值（防止应用插入时主键冲突）
-- DML 手动指定了 id，必须推进对应序列
-- =============================================

SELECT setval('system_menu_seq',             (SELECT COALESCE(MAX(id), 0) FROM system_menu));
SELECT setval('system_role_menu_seq',        (SELECT COALESCE(MAX(id), 0) FROM system_role_menu));
SELECT setval('system_notify_template_seq',  (SELECT COALESCE(MAX(id), 0) FROM system_notify_template));
SELECT setval('ops_cs_opreq_seq',           (SELECT COALESCE(MAX(id), 0) FROM ops_cs_opreq));
SELECT setval('ops_cs_task_seq',            (SELECT COALESCE(MAX(id), 0) FROM ops_cs_task));
