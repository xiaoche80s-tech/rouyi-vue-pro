-- =============================================
-- OpsHub Step 6: 客户服务模块
-- 数据库: PostgreSQL
-- 包含: DDL（3 张表 + 索引 + 序列 + 注释）
--   1. ops_cs_task（完整 CREATE TABLE + ALTER 新增 4 字段）
--   2. ops_cs_opreq（新建）
--   3. ops_cs_attachment（新建）
-- =============================================

-- =============================================
-- 1. 客服工单表 ops_cs_task（补齐之前缺失的 DDL）
-- =============================================
CREATE TABLE ops_cs_task (
    id                  BIGINT          NOT NULL,
    task_no             VARCHAR(30)     NOT NULL,
    content             VARCHAR(2000)   NOT NULL,
    urgency             INTEGER         NOT NULL    DEFAULT 2,
    status              INTEGER         NOT NULL    DEFAULT 0,
    creator_user_id     BIGINT          NOT NULL,
    assignee_id         BIGINT,
    category            INTEGER         NOT NULL    DEFAULT 5,
    sla_deadline        TIMESTAMP,
    dealer_code         VARCHAR(50),
    dealer_name         VARCHAR(100),
    product_line_code   VARCHAR(50),
    product_line_name   VARCHAR(100),
    source_module       VARCHAR(20),
    remark              VARCHAR(500),
    accept_time         TIMESTAMP,
    deliver_time        TIMESTAMP,
    verify_time         TIMESTAMP,
    reject_reason       VARCHAR(500),
    process_instance_id VARCHAR(64),
    creator             VARCHAR(64)                 DEFAULT '',
    create_time         TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    updater             VARCHAR(64)                 DEFAULT '',
    update_time         TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    deleted             SMALLINT        NOT NULL    DEFAULT 0,
    tenant_id           BIGINT          NOT NULL    DEFAULT 0,
    CONSTRAINT pk_ops_cs_task PRIMARY KEY (id)
);

CREATE INDEX idx_ops_cs_task_status ON ops_cs_task (status);
CREATE INDEX idx_ops_cs_task_dealer_code ON ops_cs_task (dealer_code);
CREATE INDEX idx_ops_cs_task_pl_code ON ops_cs_task (product_line_code);
CREATE INDEX idx_ops_cs_task_assignee ON ops_cs_task (assignee_id);
CREATE INDEX idx_ops_cs_task_creator ON ops_cs_task (creator_user_id);

CREATE SEQUENCE ops_cs_task_seq START WITH 1 INCREMENT BY 1;

COMMENT ON TABLE ops_cs_task IS '客服工单表';
COMMENT ON COLUMN ops_cs_task.id IS '主键';
COMMENT ON COLUMN ops_cs_task.task_no IS '工单编号（TASK-YYYYMMDD-NNN）';
COMMENT ON COLUMN ops_cs_task.content IS '工单内容';
COMMENT ON COLUMN ops_cs_task.urgency IS '紧急程度：0=紧急 1=高 2=中 3=低';
COMMENT ON COLUMN ops_cs_task.status IS '状态：0=待接单 1=处理中 2=已交付 3=已关闭 4=已退回';
COMMENT ON COLUMN ops_cs_task.creator_user_id IS '提单人用户ID';
COMMENT ON COLUMN ops_cs_task.assignee_id IS '当前处理人用户ID';
COMMENT ON COLUMN ops_cs_task.category IS '分类：0=签约 1=政策 2=售后 3=订单 4=数据 5=其他';
COMMENT ON COLUMN ops_cs_task.sla_deadline IS 'SLA截止时间';
COMMENT ON COLUMN ops_cs_task.dealer_code IS '经销商编码（数据权限用）';
COMMENT ON COLUMN ops_cs_task.dealer_name IS '经销商名称（冗余存储）';
COMMENT ON COLUMN ops_cs_task.product_line_code IS '产品线编码（数据权限用）';
COMMENT ON COLUMN ops_cs_task.product_line_name IS '产品线名称（冗余存储）';
COMMENT ON COLUMN ops_cs_task.source_module IS '来源模块：aftersale/order/signing/basedata/manual';
COMMENT ON COLUMN ops_cs_task.remark IS '备注';
COMMENT ON COLUMN ops_cs_task.accept_time IS '接单时间';
COMMENT ON COLUMN ops_cs_task.deliver_time IS '交付时间';
COMMENT ON COLUMN ops_cs_task.verify_time IS '验收时间';
COMMENT ON COLUMN ops_cs_task.reject_reason IS '退回原因';
COMMENT ON COLUMN ops_cs_task.process_instance_id IS 'BPM流程实例编号';

-- =============================================
-- 2. 操作请求主表 ops_cs_opreq
-- =============================================
CREATE TABLE ops_cs_opreq (
    id                  BIGINT          NOT NULL,
    opreq_code          VARCHAR(30)     NOT NULL,
    op_type             VARCHAR(20)     NOT NULL,
    status              INTEGER         NOT NULL    DEFAULT 0,
    dealer_code         VARCHAR(50)     NOT NULL,
    dealer_name         VARCHAR(100)    NOT NULL,
    product_line_code   VARCHAR(50),
    product_line_name   VARCHAR(100),
    source_module       VARCHAR(20)     NOT NULL,
    source_id           BIGINT,
    source_code         VARCHAR(50),
    content             VARCHAR(500)    NOT NULL,
    creator_user_id     BIGINT          NOT NULL,
    assignee_id         BIGINT,
    accept_time         TIMESTAMP,
    submit_time         TIMESTAMP,
    submit_remark       VARCHAR(500),
    verify_time         TIMESTAMP,
    completed_time      TIMESTAMP,
    remark              VARCHAR(500),
    creator             VARCHAR(64)                 DEFAULT '',
    create_time         TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    updater             VARCHAR(64)                 DEFAULT '',
    update_time         TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    deleted             SMALLINT        NOT NULL    DEFAULT 0,
    tenant_id           BIGINT          NOT NULL    DEFAULT 0,
    CONSTRAINT pk_ops_cs_opreq PRIMARY KEY (id)
);

CREATE INDEX idx_ops_cs_opreq_dealer_code ON ops_cs_opreq (dealer_code);
CREATE INDEX idx_ops_cs_opreq_pl_code ON ops_cs_opreq (product_line_code);
CREATE INDEX idx_ops_cs_opreq_status ON ops_cs_opreq (status);
CREATE INDEX idx_ops_cs_opreq_type ON ops_cs_opreq (op_type);
CREATE INDEX idx_ops_cs_opreq_source ON ops_cs_opreq (source_module, source_id);
CREATE INDEX idx_ops_cs_opreq_creator ON ops_cs_opreq (creator_user_id);
CREATE INDEX idx_ops_cs_opreq_assignee ON ops_cs_opreq (assignee_id);

CREATE SEQUENCE ops_cs_opreq_seq START WITH 1 INCREMENT BY 1;

COMMENT ON TABLE ops_cs_opreq IS '操作请求主表';
COMMENT ON COLUMN ops_cs_opreq.id IS '主键';
COMMENT ON COLUMN ops_cs_opreq.opreq_code IS '操作请求编号（OPR-YYYYMMDD-NNN）';
COMMENT ON COLUMN ops_cs_opreq.op_type IS '操作类型：sign/invoice/stamp/aftersale';
COMMENT ON COLUMN ops_cs_opreq.status IS '状态：0=待处理 1=处理中 2=等待验收 3=已完成';
COMMENT ON COLUMN ops_cs_opreq.dealer_code IS '经销商编码（数据权限用）';
COMMENT ON COLUMN ops_cs_opreq.dealer_name IS '经销商名称';
COMMENT ON COLUMN ops_cs_opreq.product_line_code IS '产品线编码（数据权限用）';
COMMENT ON COLUMN ops_cs_opreq.product_line_name IS '产品线名称';
COMMENT ON COLUMN ops_cs_opreq.source_module IS '来源模块：signing/order/aftersale/basedata';
COMMENT ON COLUMN ops_cs_opreq.source_id IS '来源业务ID';
COMMENT ON COLUMN ops_cs_opreq.source_code IS '来源业务编号（展示用）';
COMMENT ON COLUMN ops_cs_opreq.content IS '请求内容描述';
COMMENT ON COLUMN ops_cs_opreq.creator_user_id IS '发起人用户ID';
COMMENT ON COLUMN ops_cs_opreq.assignee_id IS '当前处理人用户ID';
COMMENT ON COLUMN ops_cs_opreq.accept_time IS '接单时间';
COMMENT ON COLUMN ops_cs_opreq.submit_time IS '提交时间';
COMMENT ON COLUMN ops_cs_opreq.submit_remark IS '处理留言';
COMMENT ON COLUMN ops_cs_opreq.verify_time IS '验收时间';
COMMENT ON COLUMN ops_cs_opreq.completed_time IS '完成时间';
COMMENT ON COLUMN ops_cs_opreq.remark IS '备注';

-- =============================================
-- 3. 通用附件表 ops_cs_attachment
-- =============================================
CREATE TABLE ops_cs_attachment (
    id              BIGINT          NOT NULL,
    module          VARCHAR(20)     NOT NULL,
    business_id     BIGINT          NOT NULL,
    business_code   VARCHAR(50),
    file_name       VARCHAR(200)    NOT NULL,
    file_url        VARCHAR(500)    NOT NULL,
    file_size       BIGINT,
    file_type       VARCHAR(100),
    remark          VARCHAR(500),
    creator         VARCHAR(64)                 DEFAULT '',
    create_time     TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    updater         VARCHAR(64)                 DEFAULT '',
    update_time     TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    deleted         SMALLINT        NOT NULL    DEFAULT 0,
    tenant_id       BIGINT          NOT NULL    DEFAULT 0,
    CONSTRAINT pk_ops_cs_attachment PRIMARY KEY (id)
);

CREATE INDEX idx_ops_cs_attachment_module_biz ON ops_cs_attachment (module, business_id);
CREATE INDEX idx_ops_cs_attachment_biz_code ON ops_cs_attachment (business_code);

CREATE SEQUENCE ops_cs_attachment_seq START WITH 1 INCREMENT BY 1;

COMMENT ON TABLE ops_cs_attachment IS '通用附件表';
COMMENT ON COLUMN ops_cs_attachment.id IS '主键';
COMMENT ON COLUMN ops_cs_attachment.module IS '关联模块：task/opreq/basedata/signing/order/aftersale';
COMMENT ON COLUMN ops_cs_attachment.business_id IS '业务ID';
COMMENT ON COLUMN ops_cs_attachment.business_code IS '业务编号（冗余存储）';
COMMENT ON COLUMN ops_cs_attachment.file_name IS '原始文件名';
COMMENT ON COLUMN ops_cs_attachment.file_url IS '文件URL';
COMMENT ON COLUMN ops_cs_attachment.file_size IS '文件大小（字节）';
COMMENT ON COLUMN ops_cs_attachment.file_type IS 'MIME类型';
COMMENT ON COLUMN ops_cs_attachment.remark IS '备注';
