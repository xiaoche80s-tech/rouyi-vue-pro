-- =============================================
-- OpsHub Step 5: 售后模块
-- 数据库: PostgreSQL
-- 包含: DDL（2 张表 + 索引 + 序列 + 注释）
-- =============================================

-- =============================================
-- 1. 售后主表 ops_aftersale_info
-- =============================================
CREATE TABLE ops_aftersale_info (
    id                          BIGINT          NOT NULL,
    aftersale_code              VARCHAR(30)     NOT NULL,
    dealer_id                   BIGINT          NOT NULL,
    dealer_code                 VARCHAR(50)     NOT NULL,
    dealer_name                 VARCHAR(100)    NOT NULL,
    product_line_code           VARCHAR(50)     NOT NULL,
    product_line_name           VARCHAR(100)    NOT NULL,
    order_code                  VARCHAR(30)     NOT NULL,
    handling_method             VARCHAR(20)     NOT NULL,
    reason                      VARCHAR(20)     NOT NULL,
    progress_status             VARCHAR(20)     NOT NULL    DEFAULT 'pending',
    current_step                INTEGER         NOT NULL    DEFAULT 1,
    product_name                VARCHAR(200)    NOT NULL,
    product_spec                VARCHAR(100),
    quantity                    INTEGER         NOT NULL    DEFAULT 1,
    refund_amount               NUMERIC(15,2)               DEFAULT 0,
    refund_status               VARCHAR(20)                 DEFAULT 'none',
    red_invoice_status          VARCHAR(20)                 DEFAULT 'none',
    logistics_company           VARCHAR(100),
    logistics_no                VARCHAR(50),
    exchange_logistics_company  VARCHAR(100),
    exchange_logistics_no       VARCHAR(50),
    apply_time                  TIMESTAMP       NOT NULL,
    approved_time               TIMESTAMP,
    completed_time              TIMESTAMP,
    remark                      VARCHAR(500),
    creator                     VARCHAR(64)                 DEFAULT '',
    create_time                 TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    updater                     VARCHAR(64)                 DEFAULT '',
    update_time                 TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    deleted                     SMALLINT        NOT NULL    DEFAULT 0,
    tenant_id                   BIGINT          NOT NULL    DEFAULT 0,
    CONSTRAINT pk_ops_aftersale_info PRIMARY KEY (id)
);

CREATE INDEX idx_ops_aftersale_info_dealer_code ON ops_aftersale_info (dealer_code);
CREATE INDEX idx_ops_aftersale_info_pl_code ON ops_aftersale_info (product_line_code);
CREATE INDEX idx_ops_aftersale_info_order_code ON ops_aftersale_info (order_code);
CREATE INDEX idx_ops_aftersale_info_progress ON ops_aftersale_info (progress_status);
CREATE INDEX idx_ops_aftersale_info_handling ON ops_aftersale_info (handling_method);
CREATE INDEX idx_ops_aftersale_info_apply_time ON ops_aftersale_info (apply_time);

CREATE SEQUENCE ops_aftersale_info_seq START WITH 1 INCREMENT BY 1;

COMMENT ON TABLE ops_aftersale_info IS '售后主表';
COMMENT ON COLUMN ops_aftersale_info.id IS '主键';
COMMENT ON COLUMN ops_aftersale_info.aftersale_code IS '售后单号（唯一，如 SO20260615-001）';
COMMENT ON COLUMN ops_aftersale_info.dealer_id IS '经销商ID';
COMMENT ON COLUMN ops_aftersale_info.dealer_code IS '经销商编码（数据权限用）';
COMMENT ON COLUMN ops_aftersale_info.dealer_name IS '经销商名称（冗余存储）';
COMMENT ON COLUMN ops_aftersale_info.product_line_code IS '产品线编码（数据权限用）';
COMMENT ON COLUMN ops_aftersale_info.product_line_name IS '产品线名称（冗余存储）';
COMMENT ON COLUMN ops_aftersale_info.order_code IS '关联订单号';
COMMENT ON COLUMN ops_aftersale_info.handling_method IS '处理方式：return/exchange/return_refund';
COMMENT ON COLUMN ops_aftersale_info.reason IS '售后原因：complaint/recall/damage';
COMMENT ON COLUMN ops_aftersale_info.progress_status IS '进度状态：pending/in_progress/exchanging/completed';
COMMENT ON COLUMN ops_aftersale_info.current_step IS '当前进度节点序号（1-5）';
COMMENT ON COLUMN ops_aftersale_info.product_name IS '产品名称';
COMMENT ON COLUMN ops_aftersale_info.product_spec IS '产品规格型号';
COMMENT ON COLUMN ops_aftersale_info.quantity IS '售后数量';
COMMENT ON COLUMN ops_aftersale_info.refund_amount IS '退款金额';
COMMENT ON COLUMN ops_aftersale_info.refund_status IS '退款状态：none/pending/refunded';
COMMENT ON COLUMN ops_aftersale_info.red_invoice_status IS '红字发票状态：none/pending/issued';
COMMENT ON COLUMN ops_aftersale_info.logistics_company IS '退回物流公司';
COMMENT ON COLUMN ops_aftersale_info.logistics_no IS '退回物流单号';
COMMENT ON COLUMN ops_aftersale_info.exchange_logistics_company IS '换货物流公司';
COMMENT ON COLUMN ops_aftersale_info.exchange_logistics_no IS '换货物流单号';
COMMENT ON COLUMN ops_aftersale_info.apply_time IS '申请时间';
COMMENT ON COLUMN ops_aftersale_info.approved_time IS '审核通过时间';
COMMENT ON COLUMN ops_aftersale_info.completed_time IS '完成时间';
COMMENT ON COLUMN ops_aftersale_info.remark IS '备注';

-- =============================================
-- 2. 售后进度节点子表 ops_aftersale_progress
-- =============================================
CREATE TABLE ops_aftersale_progress (
    id              BIGINT          NOT NULL,
    aftersale_id    BIGINT          NOT NULL,
    aftersale_code  VARCHAR(30)     NOT NULL,
    node_code       VARCHAR(30)     NOT NULL,
    node_name       VARCHAR(50)     NOT NULL,
    node_time       TIMESTAMP,
    is_completed    BOOLEAN         NOT NULL    DEFAULT FALSE,
    sort_order      INTEGER         NOT NULL,
    remark          VARCHAR(500),
    creator         VARCHAR(64)                 DEFAULT '',
    create_time     TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    updater         VARCHAR(64)                 DEFAULT '',
    update_time     TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    deleted         SMALLINT        NOT NULL    DEFAULT 0,
    tenant_id       BIGINT          NOT NULL    DEFAULT 0,
    CONSTRAINT pk_ops_aftersale_progress PRIMARY KEY (id)
);

CREATE INDEX idx_ops_aftersale_progress_code ON ops_aftersale_progress (aftersale_code);
CREATE INDEX idx_ops_aftersale_progress_aid ON ops_aftersale_progress (aftersale_id);

CREATE SEQUENCE ops_aftersale_progress_seq START WITH 1 INCREMENT BY 1;

COMMENT ON TABLE ops_aftersale_progress IS '售后进度节点子表';
COMMENT ON COLUMN ops_aftersale_progress.id IS '主键';
COMMENT ON COLUMN ops_aftersale_progress.aftersale_id IS '关联售后单ID';
COMMENT ON COLUMN ops_aftersale_progress.aftersale_code IS '关联售后单号（冗余）';
COMMENT ON COLUMN ops_aftersale_progress.node_code IS '节点编码：submitted/reviewed/returned/refund_done/red_invoice/exchange_sent/received';
COMMENT ON COLUMN ops_aftersale_progress.node_name IS '节点名称（中文）';
COMMENT ON COLUMN ops_aftersale_progress.node_time IS '节点完成时间（为空表示未完成）';
COMMENT ON COLUMN ops_aftersale_progress.is_completed IS '是否完成';
COMMENT ON COLUMN ops_aftersale_progress.sort_order IS '排序序号（1-5）';
COMMENT ON COLUMN ops_aftersale_progress.remark IS '节点备注';
