-- =============================================
-- OpsHub Step 4: 订单模块
-- 数据库: PostgreSQL
-- 包含: DDL（6 张表 + 索引 + 序列 + 注释）
-- =============================================

-- =============================================
-- 1. 订单主表 ops_order_info
-- =============================================
CREATE TABLE ops_order_info (
    id                  BIGINT          NOT NULL,
    order_code          VARCHAR(30)     NOT NULL,
    dealer_id           BIGINT          NOT NULL,
    dealer_code         VARCHAR(50)     NOT NULL,
    dealer_name         VARCHAR(100)    NOT NULL,
    product_line_code   VARCHAR(50)     NOT NULL,
    product_line_name   VARCHAR(100)    NOT NULL,
    total_amount        NUMERIC(15,2)   NOT NULL    DEFAULT 0,
    order_date          DATE            NOT NULL,
    progress_status     VARCHAR(20)     NOT NULL    DEFAULT 'pending',
    pay_status          VARCHAR(20)     NOT NULL    DEFAULT 'unpaid',
    paid_amount         NUMERIC(15,2)               DEFAULT 0,
    inv_status          VARCHAR(20)     NOT NULL    DEFAULT 'uninvoiced',
    invoiced_amount     NUMERIC(15,2)               DEFAULT 0,
    confirmed_time      TIMESTAMP,
    shipped_time        TIMESTAMP,
    signed_time         TIMESTAMP,
    completed_time      TIMESTAMP,
    remark              VARCHAR(500),
    creator             VARCHAR(64)                 DEFAULT '',
    create_time         TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    updater             VARCHAR(64)                 DEFAULT '',
    update_time         TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    deleted             SMALLINT        NOT NULL    DEFAULT 0,
    tenant_id           BIGINT          NOT NULL    DEFAULT 0,
    CONSTRAINT pk_ops_order_info PRIMARY KEY (id)
);

CREATE INDEX idx_ops_order_info_dealer_code ON ops_order_info (dealer_code);
CREATE INDEX idx_ops_order_info_pl_code ON ops_order_info (product_line_code);
CREATE INDEX idx_ops_order_info_progress ON ops_order_info (progress_status);
CREATE INDEX idx_ops_order_info_pay ON ops_order_info (pay_status);
CREATE INDEX idx_ops_order_info_inv ON ops_order_info (inv_status);
CREATE INDEX idx_ops_order_info_date ON ops_order_info (order_date);

CREATE SEQUENCE ops_order_info_seq START WITH 1 INCREMENT BY 1;

COMMENT ON TABLE ops_order_info IS '订单主表';
COMMENT ON COLUMN ops_order_info.id IS '主键';
COMMENT ON COLUMN ops_order_info.order_code IS '订单号（唯一，系统自动生成）';
COMMENT ON COLUMN ops_order_info.dealer_id IS '经销商ID';
COMMENT ON COLUMN ops_order_info.dealer_code IS '经销商编码（数据权限用）';
COMMENT ON COLUMN ops_order_info.dealer_name IS '经销商名称（冗余存储）';
COMMENT ON COLUMN ops_order_info.product_line_code IS '产品线编码（数据权限用）';
COMMENT ON COLUMN ops_order_info.product_line_name IS '产品线名称（冗余存储）';
COMMENT ON COLUMN ops_order_info.total_amount IS '订单总金额';
COMMENT ON COLUMN ops_order_info.order_date IS '订单日期';
COMMENT ON COLUMN ops_order_info.progress_status IS '进度状态：pending/confirmed/shipped/signed/completed';
COMMENT ON COLUMN ops_order_info.pay_status IS '付款状态：unpaid/paid';
COMMENT ON COLUMN ops_order_info.paid_amount IS '已付金额';
COMMENT ON COLUMN ops_order_info.inv_status IS '开票状态：uninvoiced/partial/invoiced';
COMMENT ON COLUMN ops_order_info.invoiced_amount IS '已开票金额';
COMMENT ON COLUMN ops_order_info.confirmed_time IS '确认时间';
COMMENT ON COLUMN ops_order_info.shipped_time IS '发货时间';
COMMENT ON COLUMN ops_order_info.signed_time IS '签收时间';
COMMENT ON COLUMN ops_order_info.completed_time IS '完成时间';
COMMENT ON COLUMN ops_order_info.remark IS '备注';

-- =============================================
-- 2. 订单产品明细表 ops_order_product
-- =============================================
CREATE TABLE ops_order_product (
    id                  BIGINT          NOT NULL,
    order_id            BIGINT          NOT NULL,
    order_code          VARCHAR(30)     NOT NULL,
    product_code        VARCHAR(50),
    product_name        VARCHAR(200)    NOT NULL,
    spec_model          VARCHAR(100),
    unit_price          NUMERIC(12,2)   NOT NULL,
    quantity            INT             NOT NULL,
    unit                VARCHAR(20)     NOT NULL,
    amount              NUMERIC(15,2)   NOT NULL,
    returnable_qty      INT                         DEFAULT 0,
    creator             VARCHAR(64)                 DEFAULT '',
    create_time         TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    updater             VARCHAR(64)                 DEFAULT '',
    update_time         TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    deleted             SMALLINT        NOT NULL    DEFAULT 0,
    tenant_id           BIGINT          NOT NULL    DEFAULT 0,
    CONSTRAINT pk_ops_order_product PRIMARY KEY (id)
);

CREATE INDEX idx_ops_order_product_order_code ON ops_order_product (order_code);

CREATE SEQUENCE ops_order_product_seq START WITH 1 INCREMENT BY 1;

COMMENT ON TABLE ops_order_product IS '订单产品明细表';
COMMENT ON COLUMN ops_order_product.id IS '主键';
COMMENT ON COLUMN ops_order_product.order_id IS '关联订单ID';
COMMENT ON COLUMN ops_order_product.order_code IS '关联订单号';
COMMENT ON COLUMN ops_order_product.product_code IS '产品编码';
COMMENT ON COLUMN ops_order_product.product_name IS '产品名称';
COMMENT ON COLUMN ops_order_product.spec_model IS '规格型号';
COMMENT ON COLUMN ops_order_product.unit_price IS '单价';
COMMENT ON COLUMN ops_order_product.quantity IS '数量';
COMMENT ON COLUMN ops_order_product.unit IS '单位';
COMMENT ON COLUMN ops_order_product.amount IS '金额';
COMMENT ON COLUMN ops_order_product.returnable_qty IS '可退货数量';

-- =============================================
-- 3. 订单时间线表 ops_order_timeline
-- =============================================
CREATE TABLE ops_order_timeline (
    id                  BIGINT          NOT NULL,
    order_id            BIGINT          NOT NULL,
    order_code          VARCHAR(30)     NOT NULL,
    node_code           VARCHAR(30)     NOT NULL,
    node_name           VARCHAR(50)     NOT NULL,
    node_time           TIMESTAMP,
    is_completed        BOOLEAN         NOT NULL    DEFAULT FALSE,
    sort_order          INT             NOT NULL,
    creator             VARCHAR(64)                 DEFAULT '',
    create_time         TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    updater             VARCHAR(64)                 DEFAULT '',
    update_time         TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    deleted             SMALLINT        NOT NULL    DEFAULT 0,
    tenant_id           BIGINT          NOT NULL    DEFAULT 0,
    CONSTRAINT pk_ops_order_timeline PRIMARY KEY (id)
);

CREATE INDEX idx_ops_order_timeline_order_code ON ops_order_timeline (order_code);

CREATE SEQUENCE ops_order_timeline_seq START WITH 1 INCREMENT BY 1;

COMMENT ON TABLE ops_order_timeline IS '订单时间线表';
COMMENT ON COLUMN ops_order_timeline.id IS '主键';
COMMENT ON COLUMN ops_order_timeline.order_id IS '关联订单ID';
COMMENT ON COLUMN ops_order_timeline.order_code IS '关联订单号';
COMMENT ON COLUMN ops_order_timeline.node_code IS '节点编码：created/confirmed/shipped/signed/completed';
COMMENT ON COLUMN ops_order_timeline.node_name IS '节点名称';
COMMENT ON COLUMN ops_order_timeline.node_time IS '节点完成时间';
COMMENT ON COLUMN ops_order_timeline.is_completed IS '是否完成';
COMMENT ON COLUMN ops_order_timeline.sort_order IS '排序序号';

-- =============================================
-- 4. 订单付款记录表 ops_order_payment
-- =============================================
CREATE TABLE ops_order_payment (
    id                  BIGINT          NOT NULL,
    order_id            BIGINT          NOT NULL,
    order_code          VARCHAR(30)     NOT NULL,
    pay_amount          NUMERIC(15,2)   NOT NULL,
    pay_date            DATE,
    pay_method          VARCHAR(50),
    voucher_no          VARCHAR(50),
    status              VARCHAR(20)     NOT NULL    DEFAULT 'pending',
    apply_time          TIMESTAMP       NOT NULL,
    approve_time        TIMESTAMP,
    remark              VARCHAR(500),
    creator             VARCHAR(64)                 DEFAULT '',
    create_time         TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    updater             VARCHAR(64)                 DEFAULT '',
    update_time         TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    deleted             SMALLINT        NOT NULL    DEFAULT 0,
    tenant_id           BIGINT          NOT NULL    DEFAULT 0,
    CONSTRAINT pk_ops_order_payment PRIMARY KEY (id)
);

CREATE INDEX idx_ops_order_payment_order_code ON ops_order_payment (order_code);

CREATE SEQUENCE ops_order_payment_seq START WITH 1 INCREMENT BY 1;

COMMENT ON TABLE ops_order_payment IS '订单付款记录表';
COMMENT ON COLUMN ops_order_payment.id IS '主键';
COMMENT ON COLUMN ops_order_payment.order_id IS '关联订单ID';
COMMENT ON COLUMN ops_order_payment.order_code IS '关联订单号';
COMMENT ON COLUMN ops_order_payment.pay_amount IS '付款金额';
COMMENT ON COLUMN ops_order_payment.pay_date IS '付款日期';
COMMENT ON COLUMN ops_order_payment.pay_method IS '付款方式';
COMMENT ON COLUMN ops_order_payment.voucher_no IS '付款凭证号';
COMMENT ON COLUMN ops_order_payment.status IS '状态：pending/approved/rejected';
COMMENT ON COLUMN ops_order_payment.apply_time IS '申请时间';
COMMENT ON COLUMN ops_order_payment.approve_time IS '审批通过时间';
COMMENT ON COLUMN ops_order_payment.remark IS '备注';

-- =============================================
-- 5. 订单开票记录表 ops_order_invoice
-- =============================================
CREATE TABLE ops_order_invoice (
    id                  BIGINT          NOT NULL,
    order_id            BIGINT          NOT NULL,
    order_code          VARCHAR(30)     NOT NULL,
    invoice_amount      NUMERIC(15,2)   NOT NULL,
    invoice_no          VARCHAR(50),
    invoice_date        DATE,
    invoice_type        VARCHAR(50),
    company_name        VARCHAR(200)    NOT NULL,
    tax_no              VARCHAR(50)     NOT NULL,
    special_request     VARCHAR(500),
    status              VARCHAR(20)     NOT NULL    DEFAULT 'pending',
    apply_time          TIMESTAMP       NOT NULL,
    remark              VARCHAR(500),
    creator             VARCHAR(64)                 DEFAULT '',
    create_time         TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    updater             VARCHAR(64)                 DEFAULT '',
    update_time         TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    deleted             SMALLINT        NOT NULL    DEFAULT 0,
    tenant_id           BIGINT          NOT NULL    DEFAULT 0,
    CONSTRAINT pk_ops_order_invoice PRIMARY KEY (id)
);

CREATE INDEX idx_ops_order_invoice_order_code ON ops_order_invoice (order_code);

CREATE SEQUENCE ops_order_invoice_seq START WITH 1 INCREMENT BY 1;

COMMENT ON TABLE ops_order_invoice IS '订单开票记录表';
COMMENT ON COLUMN ops_order_invoice.id IS '主键';
COMMENT ON COLUMN ops_order_invoice.order_id IS '关联订单ID';
COMMENT ON COLUMN ops_order_invoice.order_code IS '关联订单号';
COMMENT ON COLUMN ops_order_invoice.invoice_amount IS '开票金额';
COMMENT ON COLUMN ops_order_invoice.invoice_no IS '发票号';
COMMENT ON COLUMN ops_order_invoice.invoice_date IS '开票日期';
COMMENT ON COLUMN ops_order_invoice.invoice_type IS '发票类型';
COMMENT ON COLUMN ops_order_invoice.company_name IS '发票抬头';
COMMENT ON COLUMN ops_order_invoice.tax_no IS '纳税人识别号';
COMMENT ON COLUMN ops_order_invoice.special_request IS '特殊开票需求';
COMMENT ON COLUMN ops_order_invoice.status IS '状态：pending/invoiced';
COMMENT ON COLUMN ops_order_invoice.apply_time IS '申请时间';
COMMENT ON COLUMN ops_order_invoice.remark IS '备注';

-- =============================================
-- 6. 订单物流轨迹表 ops_order_logistics
-- =============================================
CREATE TABLE ops_order_logistics (
    id                  BIGINT          NOT NULL,
    order_id            BIGINT          NOT NULL,
    order_code          VARCHAR(30)     NOT NULL,
    logistics_company   VARCHAR(100),
    tracking_no         VARCHAR(50),
    node_desc           VARCHAR(500)    NOT NULL,
    node_time           TIMESTAMP       NOT NULL,
    is_completed        BOOLEAN         NOT NULL    DEFAULT FALSE,
    sort_order          INT             NOT NULL,
    creator             VARCHAR(64)                 DEFAULT '',
    create_time         TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    updater             VARCHAR(64)                 DEFAULT '',
    update_time         TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    deleted             SMALLINT        NOT NULL    DEFAULT 0,
    tenant_id           BIGINT          NOT NULL    DEFAULT 0,
    CONSTRAINT pk_ops_order_logistics PRIMARY KEY (id)
);

CREATE INDEX idx_ops_order_logistics_order_code ON ops_order_logistics (order_code);

CREATE SEQUENCE ops_order_logistics_seq START WITH 1 INCREMENT BY 1;

COMMENT ON TABLE ops_order_logistics IS '订单物流轨迹表';
COMMENT ON COLUMN ops_order_logistics.id IS '主键';
COMMENT ON COLUMN ops_order_logistics.order_id IS '关联订单ID';
COMMENT ON COLUMN ops_order_logistics.order_code IS '关联订单号';
COMMENT ON COLUMN ops_order_logistics.logistics_company IS '物流公司';
COMMENT ON COLUMN ops_order_logistics.tracking_no IS '物流单号';
COMMENT ON COLUMN ops_order_logistics.node_desc IS '节点描述';
COMMENT ON COLUMN ops_order_logistics.node_time IS '节点时间';
COMMENT ON COLUMN ops_order_logistics.is_completed IS '是否已完成节点';
COMMENT ON COLUMN ops_order_logistics.sort_order IS '排序序号';
