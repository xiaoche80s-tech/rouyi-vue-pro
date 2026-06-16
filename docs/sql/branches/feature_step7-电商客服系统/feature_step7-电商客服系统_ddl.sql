-- =============================================
-- OpsHub Step 7: 电商客服系统（在线咨询聊天）
-- 数据库: PostgreSQL
-- 包含: DDL（2 张表 + 索引 + 序列 + 注释）
--   1. ops_cs_session（会话主表）
--   2. ops_cs_message（消息记录子表）
-- =============================================

-- =============================================
-- 1. 会话主表 ops_cs_session
-- =============================================
CREATE TABLE ops_cs_session (
    id                  BIGINT          NOT NULL,
    session_no          VARCHAR(30)     NOT NULL,
    consult_type        VARCHAR(20)     NOT NULL,
    status              INTEGER         NOT NULL    DEFAULT 0,
    context             VARCHAR(500),
    context_id          BIGINT,
    context_code        VARCHAR(50),
    source_module       VARCHAR(20)     NOT NULL,
    dealer_code         VARCHAR(50)     NOT NULL,
    dealer_name         VARCHAR(100)    NOT NULL,
    product_line_code   VARCHAR(50),
    product_line_name   VARCHAR(100),
    initiator_id        BIGINT          NOT NULL,
    initiator_name      VARCHAR(50)     NOT NULL,
    assignee_id         BIGINT,
    assignee_name       VARCHAR(50),
    last_message        VARCHAR(500),
    last_message_time   TIMESTAMP,
    message_count       INTEGER         NOT NULL    DEFAULT 0,
    accept_time         TIMESTAMP,
    complete_time       TIMESTAMP,
    close_time          TIMESTAMP,
    solution_summary    VARCHAR(1000),
    remark              VARCHAR(500),
    creator             VARCHAR(64)                 DEFAULT '',
    create_time         TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    updater             VARCHAR(64)                 DEFAULT '',
    update_time         TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    deleted             SMALLINT        NOT NULL    DEFAULT 0,
    tenant_id           BIGINT          NOT NULL    DEFAULT 0,
    CONSTRAINT pk_ops_cs_session PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_ops_cs_session_no ON ops_cs_session (session_no) WHERE deleted = 0;
CREATE INDEX idx_ops_cs_session_type ON ops_cs_session (consult_type);
CREATE INDEX idx_ops_cs_session_status ON ops_cs_session (status);
CREATE INDEX idx_ops_cs_session_dealer_code ON ops_cs_session (dealer_code);
CREATE INDEX idx_ops_cs_session_pl_code ON ops_cs_session (product_line_code);
CREATE INDEX idx_ops_cs_session_initiator ON ops_cs_session (initiator_id);
CREATE INDEX idx_ops_cs_session_assignee ON ops_cs_session (assignee_id);
CREATE INDEX idx_ops_cs_session_context ON ops_cs_session (source_module, context_code);
CREATE INDEX idx_ops_cs_session_last_msg_time ON ops_cs_session (last_message_time);

CREATE SEQUENCE ops_cs_session_seq START WITH 1 INCREMENT BY 1;

COMMENT ON TABLE ops_cs_session IS '咨询会话主表';
COMMENT ON COLUMN ops_cs_session.id IS '主键';
COMMENT ON COLUMN ops_cs_session.session_no IS '会话编号（CS-YYYYMMDD-NNN）';
COMMENT ON COLUMN ops_cs_session.consult_type IS '咨询类型：signing/policy/aftersale/order/basedata/other';
COMMENT ON COLUMN ops_cs_session.status IS '状态：0=待处理 1=处理中 2=已完成 3=已关闭';
COMMENT ON COLUMN ops_cs_session.context IS '咨询上下文描述';
COMMENT ON COLUMN ops_cs_session.context_id IS '上下文关联业务ID';
COMMENT ON COLUMN ops_cs_session.context_code IS '上下文关联业务编号';
COMMENT ON COLUMN ops_cs_session.source_module IS '来源模块：signing/order/aftersale/basedata/policy/manual';
COMMENT ON COLUMN ops_cs_session.dealer_code IS '经销商编码（数据权限用）';
COMMENT ON COLUMN ops_cs_session.dealer_name IS '经销商名称（冗余存储）';
COMMENT ON COLUMN ops_cs_session.product_line_code IS '产品线编码（数据权限用）';
COMMENT ON COLUMN ops_cs_session.product_line_name IS '产品线名称（冗余存储）';
COMMENT ON COLUMN ops_cs_session.initiator_id IS '发起人用户ID';
COMMENT ON COLUMN ops_cs_session.initiator_name IS '发起人姓名';
COMMENT ON COLUMN ops_cs_session.assignee_id IS '当前处理人（执行员）用户ID';
COMMENT ON COLUMN ops_cs_session.assignee_name IS '当前处理人姓名';
COMMENT ON COLUMN ops_cs_session.last_message IS '最后一条消息摘要';
COMMENT ON COLUMN ops_cs_session.last_message_time IS '最后一条消息时间';
COMMENT ON COLUMN ops_cs_session.message_count IS '消息总数';
COMMENT ON COLUMN ops_cs_session.accept_time IS '接单时间';
COMMENT ON COLUMN ops_cs_session.complete_time IS '完成时间';
COMMENT ON COLUMN ops_cs_session.close_time IS '关闭时间';
COMMENT ON COLUMN ops_cs_session.solution_summary IS '解决方案摘要';
COMMENT ON COLUMN ops_cs_session.remark IS '备注';

-- =============================================
-- 2. 消息记录子表 ops_cs_message
-- =============================================
CREATE TABLE ops_cs_message (
    id              BIGINT          NOT NULL,
    session_id      BIGINT          NOT NULL,
    session_no      VARCHAR(30)     NOT NULL,
    sender_id       BIGINT          NOT NULL,
    sender_name     VARCHAR(50)     NOT NULL,
    sender_role     VARCHAR(20)     NOT NULL,
    message_type    VARCHAR(20)     NOT NULL,
    content         TEXT,
    attachment_ids  VARCHAR(500),
    link_url        VARCHAR(500),
    link_title      VARCHAR(200),
    is_read         SMALLINT        NOT NULL    DEFAULT 0,
    remark          VARCHAR(500),
    creator         VARCHAR(64)                 DEFAULT '',
    create_time     TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    updater         VARCHAR(64)                 DEFAULT '',
    update_time     TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    deleted         SMALLINT        NOT NULL    DEFAULT 0,
    tenant_id       BIGINT          NOT NULL    DEFAULT 0,
    CONSTRAINT pk_ops_cs_message PRIMARY KEY (id)
);

CREATE INDEX idx_ops_cs_message_session ON ops_cs_message (session_id);
CREATE INDEX idx_ops_cs_message_session_no ON ops_cs_message (session_no);
CREATE INDEX idx_ops_cs_message_sender ON ops_cs_message (sender_id);
CREATE INDEX idx_ops_cs_message_create_time ON ops_cs_message (create_time);

CREATE SEQUENCE ops_cs_message_seq START WITH 1 INCREMENT BY 1;

COMMENT ON TABLE ops_cs_message IS '咨询消息记录表';
COMMENT ON COLUMN ops_cs_message.id IS '主键';
COMMENT ON COLUMN ops_cs_message.session_id IS '关联会话ID';
COMMENT ON COLUMN ops_cs_message.session_no IS '关联会话编号';
COMMENT ON COLUMN ops_cs_message.sender_id IS '发送人用户ID';
COMMENT ON COLUMN ops_cs_message.sender_name IS '发送人姓名';
COMMENT ON COLUMN ops_cs_message.sender_role IS '发送人角色：dealer/executor/admin/system';
COMMENT ON COLUMN ops_cs_message.message_type IS '消息类型：text/attachment/system/link';
COMMENT ON COLUMN ops_cs_message.content IS '文本内容';
COMMENT ON COLUMN ops_cs_message.attachment_ids IS '附件ID列表（逗号分隔）';
COMMENT ON COLUMN ops_cs_message.link_url IS '链接地址';
COMMENT ON COLUMN ops_cs_message.link_title IS '链接标题';
COMMENT ON COLUMN ops_cs_message.is_read IS '是否已读：0=未读 1=已读';
COMMENT ON COLUMN ops_cs_message.remark IS '备注';
