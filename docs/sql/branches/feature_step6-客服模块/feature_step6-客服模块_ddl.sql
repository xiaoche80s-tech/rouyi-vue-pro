-- =============================================
-- OpsHub Step 6: 客户服务模块 - 工单系统 DDL
-- 数据库: PostgreSQL
-- 包含: 1 张业务表（ops_cs_task）
-- =============================================

-- 1.1 客服工单表
CREATE TABLE ops_cs_task (
    id                  int8         NOT NULL,
    task_no             varchar(30)  NOT NULL,
    content             text         NOT NULL,
    urgency             int2         NOT NULL DEFAULT 2,
    status              int2         NOT NULL DEFAULT 0,
    creator_user_id     int8         NOT NULL,
    assignee_id         int8         NOT NULL,
    category            int2         NOT NULL DEFAULT 0,
    sla_deadline        timestamp    NOT NULL,
    dealer_code         varchar(50)  DEFAULT NULL,
    remark              varchar(500) DEFAULT NULL,
    accept_time         timestamp    DEFAULT NULL,
    deliver_time        timestamp    DEFAULT NULL,
    verify_time         timestamp    DEFAULT NULL,
    reject_reason       varchar(500) DEFAULT NULL,
    process_instance_id varchar(64)  DEFAULT NULL,
    creator             varchar(64)  DEFAULT '',
    create_time         timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater             varchar(64)  DEFAULT '',
    update_time         timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted             int2         NOT NULL DEFAULT 0,
    tenant_id           int8         NOT NULL DEFAULT 0
);
ALTER TABLE ops_cs_task ADD CONSTRAINT pk_ops_cs_task PRIMARY KEY (id);
CREATE UNIQUE INDEX uk_ops_cs_task_no ON ops_cs_task (task_no) WHERE deleted = 0;
CREATE INDEX idx_ops_cs_task_assignee ON ops_cs_task (assignee_id);
CREATE INDEX idx_ops_cs_task_creator_user ON ops_cs_task (creator_user_id);
CREATE INDEX idx_ops_cs_task_status ON ops_cs_task (status);
CREATE INDEX idx_ops_cs_task_sla_deadline ON ops_cs_task (sla_deadline);
CREATE INDEX idx_ops_cs_task_dealer_code ON ops_cs_task (dealer_code);
CREATE SEQUENCE ops_cs_task_seq START 1;
COMMENT ON TABLE ops_cs_task IS '客服工单表';
COMMENT ON COLUMN ops_cs_task.id IS '主键';
COMMENT ON COLUMN ops_cs_task.task_no IS '工单编号 TASK-YYYYMMDD-NNN';
COMMENT ON COLUMN ops_cs_task.content IS '工单内容';
COMMENT ON COLUMN ops_cs_task.urgency IS '紧急程度：0=紧急 1=高 2=中 3=低';
COMMENT ON COLUMN ops_cs_task.status IS '状态：0=待接单 1=处理中 2=已交付 3=已关闭 4=已退回';
COMMENT ON COLUMN ops_cs_task.creator_user_id IS '提单人（经销商代理人）用户ID';
COMMENT ON COLUMN ops_cs_task.assignee_id IS '当前处理人（执行员）用户ID';
COMMENT ON COLUMN ops_cs_task.category IS '分类：0=签约 1=政策 2=售后 3=订单 4=数据 5=其他';
COMMENT ON COLUMN ops_cs_task.sla_deadline IS 'SLA 截止时间';
COMMENT ON COLUMN ops_cs_task.dealer_code IS '关联经销商编码';
COMMENT ON COLUMN ops_cs_task.remark IS '备注';
COMMENT ON COLUMN ops_cs_task.accept_time IS '接单时间';
COMMENT ON COLUMN ops_cs_task.deliver_time IS '交付时间';
COMMENT ON COLUMN ops_cs_task.verify_time IS '验收时间';
COMMENT ON COLUMN ops_cs_task.reject_reason IS '退回原因';
COMMENT ON COLUMN ops_cs_task.process_instance_id IS 'BPM流程实例编号';
COMMENT ON COLUMN ops_cs_task.creator IS '创建者';
COMMENT ON COLUMN ops_cs_task.create_time IS '创建时间';
COMMENT ON COLUMN ops_cs_task.updater IS '更新者';
COMMENT ON COLUMN ops_cs_task.update_time IS '更新时间';
COMMENT ON COLUMN ops_cs_task.deleted IS '是否删除';
COMMENT ON COLUMN ops_cs_task.tenant_id IS '租户编号';
