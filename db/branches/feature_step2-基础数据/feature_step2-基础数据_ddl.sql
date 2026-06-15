-- =============================================
-- OpsHub Step 2: 基础数据模块
-- 数据库: PostgreSQL
-- 包含: 纯 DDL（1 张业务表）
-- =============================================

-- 1.1 基础数据文件表
CREATE TABLE ops_basedata_file (
    id            int8         NOT NULL,
    dealer_id     int8         NOT NULL,
    dealer_code   varchar(50)  NOT NULL,
    category      varchar(30)  NOT NULL,
    file_name     varchar(200) NOT NULL,
    file_type     varchar(50)  NOT NULL,
    file_no       varchar(30)  DEFAULT NULL,
    file_url      varchar(500) DEFAULT NULL,
    file_size     int8         DEFAULT NULL,
    expire_date   date         DEFAULT NULL,
    status        int2         NOT NULL DEFAULT 0,
    description   varchar(500) DEFAULT NULL,
    remark        varchar(500) DEFAULT NULL,
    creator       varchar(64)  DEFAULT '',
    create_time   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater       varchar(64)  DEFAULT '',
    update_time   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted       int2         NOT NULL DEFAULT 0,
    tenant_id     int8         NOT NULL DEFAULT 0
);
ALTER TABLE ops_basedata_file ADD CONSTRAINT pk_ops_basedata_file PRIMARY KEY (id);
CREATE INDEX idx_ops_basedata_file_dealer_id ON ops_basedata_file (dealer_id);
CREATE INDEX idx_ops_basedata_file_dealer_code ON ops_basedata_file (dealer_code);
CREATE INDEX idx_ops_basedata_file_category ON ops_basedata_file (category);
CREATE SEQUENCE ops_basedata_file_seq START 1;

COMMENT ON TABLE ops_basedata_file IS '基础数据文件表';
COMMENT ON COLUMN ops_basedata_file.id IS '主键';
COMMENT ON COLUMN ops_basedata_file.dealer_id IS '经销商ID';
COMMENT ON COLUMN ops_basedata_file.dealer_code IS '经销商编码（数据权限用）';
COMMENT ON COLUMN ops_basedata_file.category IS '文件分类：qualification/authorization/contract/product';
COMMENT ON COLUMN ops_basedata_file.file_name IS '文件名称';
COMMENT ON COLUMN ops_basedata_file.file_type IS '文件子类型编码：BL/JYXK/BA/QMS/SQ/MC/POL/SA/TA/ZCZ/HGZ/SMS/JS/JCBG';
COMMENT ON COLUMN ops_basedata_file.file_no IS '文件编号';
COMMENT ON COLUMN ops_basedata_file.file_url IS '文件地址';
COMMENT ON COLUMN ops_basedata_file.file_size IS '文件大小（字节）';
COMMENT ON COLUMN ops_basedata_file.expire_date IS '有效期至';
COMMENT ON COLUMN ops_basedata_file.status IS '状态（0=正常, 1=停用）';
COMMENT ON COLUMN ops_basedata_file.description IS '文件描述';
COMMENT ON COLUMN ops_basedata_file.remark IS '备注';
COMMENT ON COLUMN ops_basedata_file.creator IS '创建者';
COMMENT ON COLUMN ops_basedata_file.create_time IS '创建时间';
COMMENT ON COLUMN ops_basedata_file.updater IS '更新者';
COMMENT ON COLUMN ops_basedata_file.update_time IS '更新时间';
COMMENT ON COLUMN ops_basedata_file.deleted IS '是否删除';
COMMENT ON COLUMN ops_basedata_file.tenant_id IS '租户编号';
