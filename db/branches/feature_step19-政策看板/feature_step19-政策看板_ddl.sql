-- =============================================
-- OpsHub Step 19: 政策看板模块
-- 数据库: MySQL
-- 包含: 3 张新建表
-- =============================================

-- 政策主表
CREATE TABLE ops_dealer_policy (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    dealer_id     BIGINT        NOT NULL COMMENT '经销商ID',
    dealer_code   VARCHAR(50)   DEFAULT NULL COMMENT '经销商编码（数据权限用）',
    product_line_code VARCHAR(50) DEFAULT NULL COMMENT '产品线编码（数据权限用）',
    product_line_name VARCHAR(100) DEFAULT NULL COMMENT '产品线名称（冗余，便于展示）',
    policy_code   VARCHAR(30)   NOT NULL COMMENT '政策编码',
    policy_name   VARCHAR(200)  NOT NULL COMMENT '政策名称',
    policy_type   VARCHAR(20)   NOT NULL COMMENT '政策类型(rebate/promotion/other)',
    achievement_type VARCHAR(20)  NOT NULL COMMENT '达成类型(quarter=季度/month=月度)',
    policy_status VARCHAR(20)   NOT NULL DEFAULT 'executing' COMMENT '状态(executing/pending/completed)',
    start_date    DATE          DEFAULT NULL COMMENT '政策开始日期',
    end_date      DATE          DEFAULT NULL COMMENT '政策结束日期',
    policy_desc   TEXT          DEFAULT NULL COMMENT '政策描述',
    contract_code VARCHAR(30)   DEFAULT NULL COMMENT '来源合同编码',
    contract_name VARCHAR(200)  DEFAULT NULL COMMENT '来源合同名称',
    source_contract_id BIGINT   DEFAULT NULL COMMENT '来源政策合同ID',
    remark        VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    creator       VARCHAR(64)   DEFAULT '' COMMENT '创建者',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater       VARCHAR(64)   DEFAULT '' COMMENT '更新者',
    update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       BIT(1)        NOT NULL DEFAULT b'0' COMMENT '是否删除',
    tenant_id     BIGINT        NOT NULL DEFAULT 0 COMMENT '租户编号',
    UNIQUE KEY uk_policy_code (policy_code, tenant_id),
    KEY idx_dealer_id (dealer_id),
    KEY idx_policy_type (policy_type),
    KEY idx_achievement_type (achievement_type),
    KEY idx_policy_status (policy_status),
    KEY idx_contract_code (contract_code)
) ENGINE=InnoDB COMMENT='经销商政策表';

-- 政策指标表
CREATE TABLE ops_dealer_policy_indicator (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    policy_id       BIGINT        NOT NULL COMMENT '关联政策ID',
    policy_code     VARCHAR(30)   NOT NULL COMMENT '政策编码（冗余，便于查询）',
    indicator_name  VARCHAR(100)  NOT NULL COMMENT '指标名称',
    target_year     INT           NOT NULL COMMENT '年度',
    target_month    INT           NOT NULL COMMENT '月份(季度政策:3/6/9/12,月度政策:1-12)',
    target_value    DECIMAL(12,2) NOT NULL COMMENT '目标值',
    achieved_value  DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '达成值',
    unit            VARCHAR(20)   NOT NULL COMMENT '单位',
    creator         VARCHAR(64)   DEFAULT '' COMMENT '创建者',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater         VARCHAR(64)   DEFAULT '' COMMENT '更新者',
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         BIT(1)        NOT NULL DEFAULT b'0' COMMENT '是否删除',
    tenant_id       BIGINT        NOT NULL DEFAULT 0 COMMENT '租户编号',
    KEY idx_policy_id (policy_id),
    KEY idx_policy_code (policy_code),
    KEY idx_indicator_name (indicator_name),
    KEY idx_target_month (target_month),
    KEY idx_target_year (target_year)
) ENGINE=InnoDB COMMENT='政策指标表';

-- 政策达成明细表
CREATE TABLE ops_dealer_policy_achievement (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    indicator_id    BIGINT        NOT NULL COMMENT '关联指标ID',
    indicator_name  VARCHAR(100)  NOT NULL COMMENT '指标名称（冗余，便于查询）',
    province        VARCHAR(50)   DEFAULT NULL COMMENT '省份',
    province_code   VARCHAR(20)   DEFAULT NULL COMMENT '省份编码',
    hospital        VARCHAR(200)  DEFAULT NULL COMMENT '医院名称',
    hospital_code   VARCHAR(50)   DEFAULT NULL COMMENT '医院编码',
    product_name    VARCHAR(200)  DEFAULT NULL COMMENT '产品名称',
    target_year     INT           NOT NULL COMMENT '年度',
    achieved_value  DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '达成值',
    achieve_level   VARCHAR(10)   NOT NULL COMMENT '层级(province/hospital/product)',
    creator         VARCHAR(64)   DEFAULT '' COMMENT '创建者',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater         VARCHAR(64)   DEFAULT '' COMMENT '更新者',
    update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         BIT(1)        NOT NULL DEFAULT b'0' COMMENT '是否删除',
    tenant_id       BIGINT        NOT NULL DEFAULT 0 COMMENT '租户编号',
    KEY idx_indicator_id (indicator_id),
    KEY idx_achieve_level (achieve_level),
    KEY idx_target_year (target_year)
) ENGINE=InnoDB COMMENT='政策达成明细表';
