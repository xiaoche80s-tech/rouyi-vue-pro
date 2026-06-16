-- 执行时间: 2026-06-15 23:30:00
-- 分支: feature/step1-权限功能设计
-- 操作说明: Step 5 售后模块建表 — ops_aftersale_info + ops_aftersale_progress

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

CREATE UNIQUE INDEX uk_ops_aftersale_info_code ON ops_aftersale_info (aftersale_code) WHERE deleted = 0;
CREATE INDEX idx_ops_aftersale_info_dealer_code ON ops_aftersale_info (dealer_code);
CREATE INDEX idx_ops_aftersale_info_pl_code ON ops_aftersale_info (product_line_code);
CREATE INDEX idx_ops_aftersale_info_order_code ON ops_aftersale_info (order_code);
CREATE INDEX idx_ops_aftersale_info_progress ON ops_aftersale_info (progress_status);
CREATE INDEX idx_ops_aftersale_info_handling ON ops_aftersale_info (handling_method);
CREATE INDEX idx_ops_aftersale_info_apply_time ON ops_aftersale_info (apply_time);

CREATE SEQUENCE ops_aftersale_info_seq START WITH 1 INCREMENT BY 1;

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
