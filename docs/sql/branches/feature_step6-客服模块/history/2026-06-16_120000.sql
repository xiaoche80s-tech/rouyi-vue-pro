-- 执行时间: 2026-06-16 12:00:00
-- 分支: feature_step6-客服模块
-- 操作说明: Step 6 客户服务模块完整 DDL + DML 执行（工单增强 + 操作请求表 + 附件表 + 权限统一 + 测试数据 + 站内信模板）

-- ===== DDL 部分 =====

-- 1. ops_cs_task 补齐 Step 6 新增字段
ALTER TABLE ops_cs_task ADD COLUMN IF NOT EXISTS dealer_name VARCHAR(100);
ALTER TABLE ops_cs_task ADD COLUMN IF NOT EXISTS product_line_code VARCHAR(50);
ALTER TABLE ops_cs_task ADD COLUMN IF NOT EXISTS product_line_name VARCHAR(100);
ALTER TABLE ops_cs_task ADD COLUMN IF NOT EXISTS source_module VARCHAR(20);
CREATE INDEX IF NOT EXISTS idx_ops_cs_task_pl_code ON ops_cs_task (product_line_code);
ALTER TABLE ops_cs_task ALTER COLUMN assignee_id DROP NOT NULL;
COMMENT ON COLUMN ops_cs_task.dealer_name IS '经销商名称（冗余存储）';
COMMENT ON COLUMN ops_cs_task.product_line_code IS '产品线编码（数据权限用）';
COMMENT ON COLUMN ops_cs_task.product_line_name IS '产品线名称（冗余存储）';
COMMENT ON COLUMN ops_cs_task.source_module IS '来源模块：aftersale/order/signing/basedata/manual';

-- 2. ops_cs_opreq 操作请求主表（新建）
CREATE TABLE ops_cs_opreq (
    id BIGINT NOT NULL,
    opreq_code VARCHAR(30) NOT NULL,
    op_type VARCHAR(20) NOT NULL,
    status INTEGER NOT NULL DEFAULT 0,
    dealer_code VARCHAR(50) NOT NULL,
    dealer_name VARCHAR(100) NOT NULL,
    product_line_code VARCHAR(50),
    product_line_name VARCHAR(100),
    source_module VARCHAR(20) NOT NULL,
    source_id BIGINT,
    source_code VARCHAR(50),
    content VARCHAR(500) NOT NULL,
    creator_user_id BIGINT NOT NULL,
    assignee_id BIGINT,
    accept_time TIMESTAMP,
    submit_time TIMESTAMP,
    submit_remark VARCHAR(500),
    verify_time TIMESTAMP,
    completed_time TIMESTAMP,
    remark VARCHAR(500),
    creator VARCHAR(64) DEFAULT '',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(64) DEFAULT '',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT NOT NULL DEFAULT 0,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_ops_cs_opreq PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uk_ops_cs_opreq_code ON ops_cs_opreq (opreq_code) WHERE deleted = 0;
CREATE INDEX idx_ops_cs_opreq_dealer_code ON ops_cs_opreq (dealer_code);
CREATE INDEX idx_ops_cs_opreq_pl_code ON ops_cs_opreq (product_line_code);
CREATE INDEX idx_ops_cs_opreq_status ON ops_cs_opreq (status);
CREATE INDEX idx_ops_cs_opreq_type ON ops_cs_opreq (op_type);
CREATE INDEX idx_ops_cs_opreq_source ON ops_cs_opreq (source_module, source_id);
CREATE INDEX idx_ops_cs_opreq_creator ON ops_cs_opreq (creator_user_id);
CREATE INDEX idx_ops_cs_opreq_assignee ON ops_cs_opreq (assignee_id);
CREATE SEQUENCE ops_cs_opreq_seq START WITH 1 INCREMENT BY 1;
COMMENT ON TABLE ops_cs_opreq IS '操作请求主表';

-- 3. ops_cs_attachment 通用附件表（新建）
CREATE TABLE ops_cs_attachment (
    id BIGINT NOT NULL,
    module VARCHAR(20) NOT NULL,
    business_id BIGINT NOT NULL,
    business_code VARCHAR(50),
    file_name VARCHAR(200) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_size BIGINT,
    file_type VARCHAR(100),
    remark VARCHAR(500),
    creator VARCHAR(64) DEFAULT '',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(64) DEFAULT '',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT NOT NULL DEFAULT 0,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_ops_cs_attachment PRIMARY KEY (id)
);
CREATE INDEX idx_ops_cs_attachment_module_biz ON ops_cs_attachment (module, business_id);
CREATE INDEX idx_ops_cs_attachment_biz_code ON ops_cs_attachment (business_code);
CREATE SEQUENCE ops_cs_attachment_seq START WITH 1 INCREMENT BY 1;
COMMENT ON TABLE ops_cs_attachment IS '通用附件表';

-- ===== DML 部分（概要） =====
-- 1. 权限标识统一 service:* → dealer:cs-*（15条 UPDATE system_menu）
-- 2. 新增按钮 6097(转单)、6098(重新处理)
-- 3. 菜单 component 更新
-- 4. 角色-菜单关联（brand_admin + service_executor → 6097/6098）
-- 5. ops_cs_opreq 20 条测试数据
-- 6. ops_cs_task 6 条增强测试数据 + 3 条存量数据 UPDATE
-- 7. 4 条操作请求站内信模板
