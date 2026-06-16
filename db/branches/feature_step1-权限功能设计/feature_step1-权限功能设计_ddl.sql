-- =============================================
-- OpsHub Step 1: 经销商管理 SaaS 基础设施层
-- 数据库: PostgreSQL
-- 包含: 纯 DDL（5 张业务表）
-- =============================================

-- =============================================
-- 一、DDL — 5 张业务表
-- =============================================

-- 1.1 经销商信息表
CREATE TABLE ops_dealer_info (
    id            int8         NOT NULL,
    dealer_name   varchar(100) NOT NULL,
    dealer_code   varchar(50)  NOT NULL,
    contact_name  varchar(50)  DEFAULT NULL,
    contact_phone varchar(20)  DEFAULT NULL,
    address       varchar(200) DEFAULT NULL,
    status        int2         NOT NULL DEFAULT 0,
    remark        varchar(500) DEFAULT NULL,
    creator       varchar(64)  DEFAULT '',
    create_time   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater       varchar(64)  DEFAULT '',
    update_time   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted       int2         NOT NULL DEFAULT 0,
    tenant_id     int8         NOT NULL DEFAULT 0
);
ALTER TABLE ops_dealer_info ADD CONSTRAINT pk_ops_dealer_info PRIMARY KEY (id);
CREATE SEQUENCE ops_dealer_info_seq START 1;
COMMENT ON TABLE ops_dealer_info IS '经销商信息表';
COMMENT ON COLUMN ops_dealer_info.id IS '经销商ID';
COMMENT ON COLUMN ops_dealer_info.dealer_name IS '经销商名称';
COMMENT ON COLUMN ops_dealer_info.dealer_code IS '经销商编码';
COMMENT ON COLUMN ops_dealer_info.contact_name IS '联系人';
COMMENT ON COLUMN ops_dealer_info.contact_phone IS '联系电话';
COMMENT ON COLUMN ops_dealer_info.address IS '地址';
COMMENT ON COLUMN ops_dealer_info.status IS '状态（0=正常, 1=停用）';
COMMENT ON COLUMN ops_dealer_info.remark IS '备注';
COMMENT ON COLUMN ops_dealer_info.creator IS '创建者';
COMMENT ON COLUMN ops_dealer_info.create_time IS '创建时间';
COMMENT ON COLUMN ops_dealer_info.updater IS '更新者';
COMMENT ON COLUMN ops_dealer_info.update_time IS '更新时间';
COMMENT ON COLUMN ops_dealer_info.deleted IS '是否删除';
COMMENT ON COLUMN ops_dealer_info.tenant_id IS '租户编号';

-- 1.2 产品线表
CREATE TABLE ops_dealer_product_line (
    id                int8         NOT NULL,
    product_line_name varchar(100) NOT NULL,
    product_line_code varchar(50)  NOT NULL,
    sort              int4         NOT NULL DEFAULT 0,
    status            int2         NOT NULL DEFAULT 0,
    remark            varchar(500) DEFAULT NULL,
    creator           varchar(64)  DEFAULT '',
    create_time       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater           varchar(64)  DEFAULT '',
    update_time       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted           int2         NOT NULL DEFAULT 0,
    tenant_id         int8         NOT NULL DEFAULT 0
);
ALTER TABLE ops_dealer_product_line ADD CONSTRAINT pk_ops_dealer_product_line PRIMARY KEY (id);
CREATE SEQUENCE ops_dealer_product_line_seq START 1;
COMMENT ON TABLE ops_dealer_product_line IS '产品线表';
COMMENT ON COLUMN ops_dealer_product_line.id IS '产品线ID';
COMMENT ON COLUMN ops_dealer_product_line.product_line_name IS '产品线名称';
COMMENT ON COLUMN ops_dealer_product_line.product_line_code IS '产品线编码';
COMMENT ON COLUMN ops_dealer_product_line.sort IS '排序';
COMMENT ON COLUMN ops_dealer_product_line.status IS '状态（0=正常, 1=停用）';
COMMENT ON COLUMN ops_dealer_product_line.remark IS '备注';
COMMENT ON COLUMN ops_dealer_product_line.creator IS '创建者';
COMMENT ON COLUMN ops_dealer_product_line.create_time IS '创建时间';
COMMENT ON COLUMN ops_dealer_product_line.updater IS '更新者';
COMMENT ON COLUMN ops_dealer_product_line.update_time IS '更新时间';
COMMENT ON COLUMN ops_dealer_product_line.deleted IS '是否删除';
COMMENT ON COLUMN ops_dealer_product_line.tenant_id IS '租户编号';

-- 1.3 经销商-产品线关联表（物理删除，无 deleted 字段）
CREATE TABLE ops_dealer_product_line_relation (
    id                int8         NOT NULL,
    dealer_code       varchar(50)  NOT NULL,
    product_line_code varchar(50)  NOT NULL,
    creator           varchar(64)  DEFAULT '',
    create_time       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater           varchar(64)  DEFAULT '',
    update_time       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tenant_id         int8         NOT NULL DEFAULT 0,
    deleted     int2         NOT NULL DEFAULT 0,
);
ALTER TABLE ops_dealer_product_line_relation ADD CONSTRAINT pk_ops_dealer_product_line_relation PRIMARY KEY (id);
CREATE INDEX idx_ops_dealer_pl_relation_dealer ON ops_dealer_product_line_relation (dealer_code);
CREATE INDEX idx_ops_dealer_pl_relation_pl ON ops_dealer_product_line_relation (product_line_code);
CREATE SEQUENCE ops_dealer_product_line_relation_seq START 1;
COMMENT ON TABLE ops_dealer_product_line_relation IS '经销商-产品线关联表';
COMMENT ON COLUMN ops_dealer_product_line_relation.id IS '主键';
COMMENT ON COLUMN ops_dealer_product_line_relation.dealer_code IS '经销商编码';
COMMENT ON COLUMN ops_dealer_product_line_relation.product_line_code IS '产品线编码';
COMMENT ON COLUMN ops_dealer_product_line_relation.creator IS '创建者';
COMMENT ON COLUMN ops_dealer_product_line_relation.create_time IS '创建时间';
COMMENT ON COLUMN ops_dealer_product_line_relation.updater IS '更新者';
COMMENT ON COLUMN ops_dealer_product_line_relation.update_time IS '更新时间';
COMMENT ON COLUMN ops_dealer_product_line_relation.tenant_id IS '租户编号';

-- 1.4 用户-经销商授权表
CREATE TABLE ops_dealer_user_scope (
    id          int8         NOT NULL,
    user_id     int8         NOT NULL,
    dealer_code varchar(50)  NOT NULL,
    creator     varchar(64)  DEFAULT '',
    create_time timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater     varchar(64)  DEFAULT '',
    update_time timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     int2         NOT NULL DEFAULT 0,
    tenant_id   int8         NOT NULL DEFAULT 0
);
ALTER TABLE ops_dealer_user_scope ADD CONSTRAINT pk_ops_dealer_user_scope PRIMARY KEY (id);
CREATE INDEX idx_ops_dealer_user_scope_user ON ops_dealer_user_scope (user_id);
CREATE INDEX idx_ops_dealer_user_scope_dealer ON ops_dealer_user_scope (dealer_code);
CREATE SEQUENCE ops_dealer_user_scope_seq START 1;
COMMENT ON TABLE ops_dealer_user_scope IS '用户-经销商授权表';
COMMENT ON COLUMN ops_dealer_user_scope.id IS '主键';
COMMENT ON COLUMN ops_dealer_user_scope.user_id IS '用户ID';
COMMENT ON COLUMN ops_dealer_user_scope.dealer_code IS '经销商编码';
COMMENT ON COLUMN ops_dealer_user_scope.creator IS '创建者';
COMMENT ON COLUMN ops_dealer_user_scope.create_time IS '创建时间';
COMMENT ON COLUMN ops_dealer_user_scope.updater IS '更新者';
COMMENT ON COLUMN ops_dealer_user_scope.update_time IS '更新时间';
COMMENT ON COLUMN ops_dealer_user_scope.deleted IS '是否删除';
COMMENT ON COLUMN ops_dealer_user_scope.tenant_id IS '租户编号';

-- 1.5 执行员-产品线授权表
CREATE TABLE ops_executor_product_line_scope (
    id                int8         NOT NULL,
    user_id           int8         NOT NULL,
    product_line_code varchar(50)  NOT NULL,
    creator           varchar(64)  DEFAULT '',
    create_time       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater           varchar(64)  DEFAULT '',
    update_time       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted           int2         NOT NULL DEFAULT 0,
    tenant_id         int8         NOT NULL DEFAULT 0
);
ALTER TABLE ops_executor_product_line_scope ADD CONSTRAINT pk_ops_executor_product_line_scope PRIMARY KEY (id);
CREATE INDEX idx_ops_executor_pl_scope_user ON ops_executor_product_line_scope (user_id);
CREATE INDEX idx_ops_executor_pl_scope_pl ON ops_executor_product_line_scope (product_line_code);
CREATE SEQUENCE ops_executor_product_line_scope_seq START 1;
COMMENT ON TABLE ops_executor_product_line_scope IS '执行员-产品线授权表';
COMMENT ON COLUMN ops_executor_product_line_scope.id IS '主键';
COMMENT ON COLUMN ops_executor_product_line_scope.user_id IS '用户ID';
COMMENT ON COLUMN ops_executor_product_line_scope.product_line_code IS '产品线编码';
COMMENT ON COLUMN ops_executor_product_line_scope.creator IS '创建者';
COMMENT ON COLUMN ops_executor_product_line_scope.create_time IS '创建时间';
COMMENT ON COLUMN ops_executor_product_line_scope.updater IS '更新者';
COMMENT ON COLUMN ops_executor_product_line_scope.update_time IS '更新时间';
COMMENT ON COLUMN ops_executor_product_line_scope.deleted IS '是否删除';
COMMENT ON COLUMN ops_executor_product_line_scope.tenant_id IS '租户编号';
