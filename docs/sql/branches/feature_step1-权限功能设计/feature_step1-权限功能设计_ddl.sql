-- DDL 汇总文件
-- ================================================
-- 分支: feature/step1-权限功能设计
-- 描述: 所有 DDL 变更汇总
-- ================================================

-- ====== Step3 签约进度模块 (2026-06-15) ======

CREATE TABLE ops_signing_contract (
    id                  BIGINT          NOT NULL,
    dealer_id           BIGINT          NOT NULL,
    dealer_code         VARCHAR(50)     NOT NULL,
    product_line_code   VARCHAR(50),
    contract_type       VARCHAR(20)     NOT NULL,
    contract_type_name  VARCHAR(50)     NOT NULL,
    contract_code       VARCHAR(30)     NOT NULL,
    contract_name       VARCHAR(200)    NOT NULL,
    status              VARCHAR(20)     NOT NULL    DEFAULT 'unsigned',
    sub_status          VARCHAR(20)                 DEFAULT 'pending',
    issued_date         DATE            NOT NULL,
    sign_date           DATE,
    summary             TEXT,
    policy_analysis     TEXT,
    indicators          TEXT,
    file_ids            VARCHAR(500),
    sign_proof_url      VARCHAR(500),
    remark              VARCHAR(500),
    creator             VARCHAR(64)                 DEFAULT '',
    create_time         TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    updater             VARCHAR(64)                 DEFAULT '',
    update_time         TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    deleted             SMALLINT        NOT NULL    DEFAULT 0,
    tenant_id           BIGINT          NOT NULL    DEFAULT 0,
    CONSTRAINT pk_ops_signing_contract PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_ops_signing_contract_code ON ops_signing_contract (contract_code) WHERE deleted = 0;
CREATE INDEX idx_ops_signing_contract_dealer_code ON ops_signing_contract (dealer_code);
CREATE INDEX idx_ops_signing_contract_pl_code ON ops_signing_contract (product_line_code);
CREATE INDEX idx_ops_signing_contract_type ON ops_signing_contract (contract_type);
CREATE INDEX idx_ops_signing_contract_status ON ops_signing_contract (status);
CREATE INDEX idx_ops_signing_contract_issued_date ON ops_signing_contract (issued_date);
CREATE SEQUENCE ops_signing_contract_seq START WITH 1 INCREMENT BY 1;

-- ====== Step4 订单模块 (2026-06-15) ======
-- 6 张表: ops_order_info, ops_order_product, ops_order_timeline,
--         ops_order_payment, ops_order_invoice, ops_order_logistics
-- 详见 db/branches/feature_step4-订单模块/feature_step4-订单模块_ddl.sql

ALTER TABLE ops_order_product ADD COLUMN product_code varchar(50) NULL;
