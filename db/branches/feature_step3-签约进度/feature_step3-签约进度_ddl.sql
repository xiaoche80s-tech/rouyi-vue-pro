-- =============================================
-- OpsHub Step 3: 签约进度模块
-- 数据库: PostgreSQL
-- 包含: DDL（建表 + 索引 + 序列 + 注释）
-- =============================================

-- 签约合同表
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

-- 普通索引
CREATE INDEX idx_ops_signing_contract_dealer_code ON ops_signing_contract (dealer_code);
CREATE INDEX idx_ops_signing_contract_pl_code ON ops_signing_contract (product_line_code);
CREATE INDEX idx_ops_signing_contract_type ON ops_signing_contract (contract_type);
CREATE INDEX idx_ops_signing_contract_status ON ops_signing_contract (status);
CREATE INDEX idx_ops_signing_contract_issued_date ON ops_signing_contract (issued_date);

-- 序列
CREATE SEQUENCE ops_signing_contract_seq START WITH 1 INCREMENT BY 1;

-- 表和列注释
COMMENT ON TABLE ops_signing_contract IS '签约合同表';
COMMENT ON COLUMN ops_signing_contract.id IS '主键';
COMMENT ON COLUMN ops_signing_contract.dealer_id IS '经销商ID';
COMMENT ON COLUMN ops_signing_contract.dealer_code IS '经销商编码（数据权限用）';
COMMENT ON COLUMN ops_signing_contract.product_line_code IS '产品线编码（数据权限用）';
COMMENT ON COLUMN ops_signing_contract.contract_type IS '合同类型：main/policy/supplement/termination';
COMMENT ON COLUMN ops_signing_contract.contract_type_name IS '合同类型中文名';
COMMENT ON COLUMN ops_signing_contract.contract_code IS '合同编码（唯一，系统自动生成）';
COMMENT ON COLUMN ops_signing_contract.contract_name IS '合同名称';
COMMENT ON COLUMN ops_signing_contract.status IS '签署状态：signed/unsigned';
COMMENT ON COLUMN ops_signing_contract.sub_status IS '子状态：pending/signing（仅unsigned时有效）';
COMMENT ON COLUMN ops_signing_contract.issued_date IS '下发日期';
COMMENT ON COLUMN ops_signing_contract.sign_date IS '签署日期';
COMMENT ON COLUMN ops_signing_contract.summary IS '合同摘要';
COMMENT ON COLUMN ops_signing_contract.policy_analysis IS '政策解析（仅policy类型）';
COMMENT ON COLUMN ops_signing_contract.indicators IS '政策指标JSON数组（仅policy类型）';
COMMENT ON COLUMN ops_signing_contract.file_ids IS '关联附件文件ID列表（逗号分隔）';
COMMENT ON COLUMN ops_signing_contract.sign_proof_url IS '签署凭证URL（执行员上传盖章文件）';
COMMENT ON COLUMN ops_signing_contract.remark IS '备注';
