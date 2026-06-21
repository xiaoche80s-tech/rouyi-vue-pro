-- 执行时间: 2026-06-21 00:00:00
-- 分支: feature_step20-操作请求工作流
-- 操作说明: Step20 操作请求工作流 DDL+DML 初始化

-- ========== DDL ==========
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

-- ========== DML ==========
-- 菜单 ID 段: 6310-6313
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, deleted) VALUES
(6310, '创建操作请求', 'dealer:op-request:create',  3, 10, 6003, '', '', '', NULL, 0, true, true, true, 0),
(6311, '查询操作请求', 'dealer:op-request:query',   3, 11, 6003, '', '', '', NULL, 0, true, true, true, 0),
(6312, '处理操作请求', 'dealer:op-request:process', 3, 12, 6003, '', '', '', NULL, 0, true, true, true, 0),
(6313, '验收操作请求', 'dealer:op-request:verify',  3, 13, 6003, '', '', '', NULL, 0, true, true, true, 0);

INSERT INTO system_role_menu (id, role_id, menu_id, tenant_id)
SELECT (SELECT COALESCE(MAX(id),0) FROM system_role_menu) + row_number() OVER (), role_id, menu_id, 123 FROM (
  SELECT 157 AS role_id, unnest(ARRAY[6310, 6311, 6313]) AS menu_id
  UNION ALL
  SELECT 158, unnest(ARRAY[6311])
  UNION ALL
  SELECT 159, unnest(ARRAY[6311, 6312])
  UNION ALL
  SELECT 160, unnest(ARRAY[6310, 6311, 6313])
) t;

DELETE FROM system_role_menu WHERE menu_id = 6038 AND tenant_id = 123;
UPDATE system_menu SET deleted = 1 WHERE id = 6038;
