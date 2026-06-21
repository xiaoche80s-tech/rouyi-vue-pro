-- =============================================
-- OpsHub Step 20: 操作请求工作流
-- 数据库: PostgreSQL
-- 包含: DDL（2 张表 + 索引 + 序列 + 注释）
-- =============================================

-- =============================================
-- 1. 操作请求主表 ops_op_request
-- =============================================
CREATE TABLE ops_op_request (
    id                      BIGINT          NOT NULL,
    request_no              VARCHAR(32)     NOT NULL,
    request_type            VARCHAR(32)     NOT NULL,
    request_type_name       VARCHAR(64),
    dealer_id               BIGINT          NOT NULL,
    dealer_code             VARCHAR(32),
    request_status          VARCHAR(16)     NOT NULL    DEFAULT 'waiting',
    process_instance_id     VARCHAR(64),
    assignee_id             BIGINT,
    remark                  VARCHAR(500),
    creator                 VARCHAR(64)                 DEFAULT '',
    create_time             TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    updater                 VARCHAR(64)                 DEFAULT '',
    update_time             TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    deleted                 SMALLINT        NOT NULL    DEFAULT 0,
    tenant_id               BIGINT          NOT NULL    DEFAULT 0,
    CONSTRAINT pk_ops_op_request PRIMARY KEY (id)
);

CREATE INDEX idx_op_request_type ON ops_op_request (request_type);
CREATE INDEX idx_op_request_dealer ON ops_op_request (dealer_code);
CREATE INDEX idx_op_request_status ON ops_op_request (request_status);

CREATE SEQUENCE ops_op_request_seq START WITH 1 INCREMENT BY 1;

COMMENT ON TABLE ops_op_request IS '操作请求主表';
COMMENT ON COLUMN ops_op_request.id IS '主键';
COMMENT ON COLUMN ops_op_request.request_no IS '请求编号（自动生成，如 OP-20260621-001）';
COMMENT ON COLUMN ops_op_request.request_type IS '类型：signing/payment/invoice/return';
COMMENT ON COLUMN ops_op_request.request_type_name IS '类型中文名';
COMMENT ON COLUMN ops_op_request.dealer_id IS '经销商ID';
COMMENT ON COLUMN ops_op_request.dealer_code IS '经销商编码';
COMMENT ON COLUMN ops_op_request.request_status IS '状态：waiting/in_progress/delivered/closed/rejected/cancel';
COMMENT ON COLUMN ops_op_request.process_instance_id IS 'BPM流程实例ID';
COMMENT ON COLUMN ops_op_request.assignee_id IS '当前处理人（执行员）ID';
COMMENT ON COLUMN ops_op_request.remark IS '备注';

-- =============================================
-- 2. 操作请求-签约子表 ops_op_request_signing
-- =============================================
CREATE TABLE ops_op_request_signing (
    id              BIGINT          NOT NULL,
    request_id      BIGINT          NOT NULL,
    contract_id     BIGINT          NOT NULL,
    contract_code   VARCHAR(64),
    contract_name   VARCHAR(128),
    creator         VARCHAR(64)                 DEFAULT '',
    create_time     TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    updater         VARCHAR(64)                 DEFAULT '',
    update_time     TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    deleted         SMALLINT        NOT NULL    DEFAULT 0,
    tenant_id       BIGINT          NOT NULL    DEFAULT 0,
    CONSTRAINT pk_ops_op_request_signing PRIMARY KEY (id)
);

CREATE INDEX idx_op_req_signing_request ON ops_op_request_signing (request_id);
CREATE INDEX idx_op_req_signing_contract ON ops_op_request_signing (contract_id);

CREATE SEQUENCE ops_op_request_signing_seq START WITH 1 INCREMENT BY 1;

COMMENT ON TABLE ops_op_request_signing IS '操作请求-签约子表';
COMMENT ON COLUMN ops_op_request_signing.id IS '主键';
COMMENT ON COLUMN ops_op_request_signing.request_id IS '关联 ops_op_request.id';
COMMENT ON COLUMN ops_op_request_signing.contract_id IS '关联 ops_signing_contract.id';
COMMENT ON COLUMN ops_op_request_signing.contract_code IS '合同编码';
COMMENT ON COLUMN ops_op_request_signing.contract_name IS '合同名称';
