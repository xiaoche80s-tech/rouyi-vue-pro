-- ========== BPM 模块 PostgreSQL 建表 SQL ==========
-- 基于 DO 类生成，ID 使用序列 + @KeySequence 机制

-- ========== 序列 ==========
CREATE SEQUENCE IF NOT EXISTS bpm_category_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS bpm_user_group_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS bpm_form_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS bpm_process_definition_info_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS bpm_process_expression_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS bpm_process_listener_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS bpm_process_instance_copy_seq START WITH 1 INCREMENT BY 1;

-- ========== bpm_category 流程分类 ==========
CREATE TABLE IF NOT EXISTS bpm_category (
    id          bigint       NOT NULL DEFAULT nextval('bpm_category_seq'),
    name        varchar(63)  NOT NULL,
    code        varchar(63)  NOT NULL,
    description varchar(255) NOT NULL,
    status      int          NOT NULL,
    sort        int          NOT NULL,
    creator     varchar(64)  DEFAULT '',
    create_time timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater     varchar(64)  DEFAULT '',
    update_time timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     smallint     NOT NULL DEFAULT 0,
    tenant_id   bigint       NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE bpm_category IS 'BPM 流程分类';
COMMENT ON COLUMN bpm_category.id IS '分类编号';
COMMENT ON COLUMN bpm_category.name IS '分类名';
COMMENT ON COLUMN bpm_category.code IS '分类标志';
COMMENT ON COLUMN bpm_category.description IS '分类描述';
COMMENT ON COLUMN bpm_category.status IS '分类状态';
COMMENT ON COLUMN bpm_category.sort IS '分类排序';
COMMENT ON COLUMN bpm_category.creator IS '创建者';
COMMENT ON COLUMN bpm_category.create_time IS '创建时间';
COMMENT ON COLUMN bpm_category.updater IS '更新者';
COMMENT ON COLUMN bpm_category.update_time IS '更新时间';
COMMENT ON COLUMN bpm_category.deleted IS '是否删除';
COMMENT ON COLUMN bpm_category.tenant_id IS '租户编号';

-- ========== bpm_user_group 用户组 ==========
CREATE TABLE IF NOT EXISTS bpm_user_group (
    id          bigint       NOT NULL DEFAULT nextval('bpm_user_group_seq'),
    name        varchar(63)  NOT NULL,
    description varchar(255) NOT NULL,
    status      int          NOT NULL,
    user_ids    varchar(255) NOT NULL,
    creator     varchar(64)  DEFAULT '',
    create_time timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater     varchar(64)  DEFAULT '',
    update_time timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     smallint     NOT NULL DEFAULT 0,
    tenant_id   bigint       NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE bpm_user_group IS 'BPM 用户组';
COMMENT ON COLUMN bpm_user_group.id IS '编号，自增';
COMMENT ON COLUMN bpm_user_group.name IS '组名';
COMMENT ON COLUMN bpm_user_group.description IS '描述';
COMMENT ON COLUMN bpm_user_group.status IS '状态';
COMMENT ON COLUMN bpm_user_group.user_ids IS '成员用户编号数组（JSON）';
COMMENT ON COLUMN bpm_user_group.creator IS '创建者';
COMMENT ON COLUMN bpm_user_group.create_time IS '创建时间';
COMMENT ON COLUMN bpm_user_group.updater IS '更新者';
COMMENT ON COLUMN bpm_user_group.update_time IS '更新时间';
COMMENT ON COLUMN bpm_user_group.deleted IS '是否删除';
COMMENT ON COLUMN bpm_user_group.tenant_id IS '租户编号';

-- ========== bpm_form 工作流表单 ==========
CREATE TABLE IF NOT EXISTS bpm_form (
    id          bigint       NOT NULL DEFAULT nextval('bpm_form_seq'),
    name        varchar(63)  NOT NULL,
    status      int          NOT NULL,
    conf        text         NOT NULL,
    fields      text         NOT NULL,
    remark      varchar(255),
    creator     varchar(64)  DEFAULT '',
    create_time timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater     varchar(64)  DEFAULT '',
    update_time timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     smallint     NOT NULL DEFAULT 0,
    tenant_id   bigint       NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE bpm_form IS 'BPM 工作流表单';
COMMENT ON COLUMN bpm_form.id IS '编号';
COMMENT ON COLUMN bpm_form.name IS '表单名';
COMMENT ON COLUMN bpm_form.status IS '状态';
COMMENT ON COLUMN bpm_form.conf IS '表单的配置（JSON）';
COMMENT ON COLUMN bpm_form.fields IS '表单项的数组（JSON）';
COMMENT ON COLUMN bpm_form.remark IS '备注';
COMMENT ON COLUMN bpm_form.creator IS '创建者';
COMMENT ON COLUMN bpm_form.create_time IS '创建时间';
COMMENT ON COLUMN bpm_form.updater IS '更新者';
COMMENT ON COLUMN bpm_form.update_time IS '更新时间';
COMMENT ON COLUMN bpm_form.deleted IS '是否删除';
COMMENT ON COLUMN bpm_form.tenant_id IS '租户编号';

-- ========== bpm_process_definition_info 流程定义扩展信息 ==========
CREATE TABLE IF NOT EXISTS bpm_process_definition_info (
    id                              bigint        NOT NULL DEFAULT nextval('bpm_process_definition_info_seq'),
    process_definition_id           varchar(128)  NOT NULL DEFAULT '',
    model_id                        varchar(128)  NOT NULL DEFAULT '',
    model_type                      int,
    category                        varchar(128)  DEFAULT '',
    icon                            varchar(256)  DEFAULT '',
    description                     varchar(500)  DEFAULT '',
    form_type                       int,
    form_id                         bigint,
    form_conf                       text,
    form_fields                     text,
    form_custom_create_path         varchar(256)  DEFAULT '',
    form_custom_view_path           varchar(256)  DEFAULT '',
    simple_model                    text,
    visible                         boolean       DEFAULT TRUE,
    sort                            bigint        DEFAULT 0,
    start_user_ids                  varchar(2048) DEFAULT '',
    start_dept_ids                  varchar(2048) DEFAULT '',
    manager_user_ids                varchar(2048) DEFAULT '',
    allow_cancel_running_process    boolean       DEFAULT TRUE,
    allow_withdraw_task             boolean       DEFAULT TRUE,
    process_id_rule                 text,
    auto_approval_type              int,
    title_setting                   text,
    summary_setting                 text,
    process_before_trigger_setting  text,
    process_after_trigger_setting   text,
    task_before_trigger_setting     text,
    task_after_trigger_setting      text,
    print_template_setting          text,
    creator                         varchar(64)   DEFAULT '',
    create_time                     timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater                         varchar(64)   DEFAULT '',
    update_time                     timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted                         smallint      NOT NULL DEFAULT 0,
    tenant_id                       bigint        NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);
CREATE INDEX idx_bpm_process_definition_info_01 ON bpm_process_definition_info (process_definition_id);
CREATE INDEX idx_bpm_process_definition_info_02 ON bpm_process_definition_info (model_id);
COMMENT ON TABLE bpm_process_definition_info IS 'BPM 流程定义扩展信息';
COMMENT ON COLUMN bpm_process_definition_info.id IS '编号';
COMMENT ON COLUMN bpm_process_definition_info.process_definition_id IS '流程定义编号';
COMMENT ON COLUMN bpm_process_definition_info.model_id IS '流程模型编号';
COMMENT ON COLUMN bpm_process_definition_info.model_type IS '流程模型类型';
COMMENT ON COLUMN bpm_process_definition_info.category IS '流程分类编码';
COMMENT ON COLUMN bpm_process_definition_info.icon IS '图标';
COMMENT ON COLUMN bpm_process_definition_info.description IS '描述';
COMMENT ON COLUMN bpm_process_definition_info.form_type IS '表单类型';
COMMENT ON COLUMN bpm_process_definition_info.form_id IS '动态表单编号';
COMMENT ON COLUMN bpm_process_definition_info.form_conf IS '表单配置（JSON）';
COMMENT ON COLUMN bpm_process_definition_info.form_fields IS '表单项数组（JSON）';
COMMENT ON COLUMN bpm_process_definition_info.form_custom_create_path IS '自定义表单提交路径';
COMMENT ON COLUMN bpm_process_definition_info.form_custom_view_path IS '自定义表单查看路径';
COMMENT ON COLUMN bpm_process_definition_info.simple_model IS 'SIMPLE 设计器模型数据（JSON）';
COMMENT ON COLUMN bpm_process_definition_info.visible IS '是否可见';
COMMENT ON COLUMN bpm_process_definition_info.sort IS '排序值';
COMMENT ON COLUMN bpm_process_definition_info.start_user_ids IS '可发起用户编号数组';
COMMENT ON COLUMN bpm_process_definition_info.start_dept_ids IS '可发起部门编号数组';
COMMENT ON COLUMN bpm_process_definition_info.manager_user_ids IS '可管理用户编号数组';
COMMENT ON COLUMN bpm_process_definition_info.allow_cancel_running_process IS '是否允许撤销审批中的申请';
COMMENT ON COLUMN bpm_process_definition_info.allow_withdraw_task IS '是否允许审批人撤回任务';
COMMENT ON COLUMN bpm_process_definition_info.process_id_rule IS '流程 ID 规则（JSON）';
COMMENT ON COLUMN bpm_process_definition_info.auto_approval_type IS '自动去重类型';
COMMENT ON COLUMN bpm_process_definition_info.title_setting IS '标题设置（JSON）';
COMMENT ON COLUMN bpm_process_definition_info.summary_setting IS '摘要设置（JSON）';
COMMENT ON COLUMN bpm_process_definition_info.process_before_trigger_setting IS '流程前置通知设置（JSON）';
COMMENT ON COLUMN bpm_process_definition_info.process_after_trigger_setting IS '流程后置通知设置（JSON）';
COMMENT ON COLUMN bpm_process_definition_info.task_before_trigger_setting IS '任务前置通知设置（JSON）';
COMMENT ON COLUMN bpm_process_definition_info.task_after_trigger_setting IS '任务后置通知设置（JSON）';
COMMENT ON COLUMN bpm_process_definition_info.print_template_setting IS '自定义打印模板设置（JSON）';
COMMENT ON COLUMN bpm_process_definition_info.creator IS '创建者';
COMMENT ON COLUMN bpm_process_definition_info.create_time IS '创建时间';
COMMENT ON COLUMN bpm_process_definition_info.updater IS '更新者';
COMMENT ON COLUMN bpm_process_definition_info.update_time IS '更新时间';
COMMENT ON COLUMN bpm_process_definition_info.deleted IS '是否删除';
COMMENT ON COLUMN bpm_process_definition_info.tenant_id IS '租户编号';

-- ========== bpm_process_expression 流程表达式 ==========
CREATE TABLE IF NOT EXISTS bpm_process_expression (
    id          bigint        NOT NULL DEFAULT nextval('bpm_process_expression_seq'),
    name        varchar(128)  NOT NULL,
    status      int           NOT NULL,
    expression  varchar(1024) DEFAULT '',
    creator     varchar(64)   DEFAULT '',
    create_time timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater     varchar(64)   DEFAULT '',
    update_time timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     smallint      NOT NULL DEFAULT 0,
    tenant_id   bigint        NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE bpm_process_expression IS 'BPM 流程表达式';
COMMENT ON COLUMN bpm_process_expression.id IS '编号';
COMMENT ON COLUMN bpm_process_expression.name IS '表达式名字';
COMMENT ON COLUMN bpm_process_expression.status IS '表达式状态';
COMMENT ON COLUMN bpm_process_expression.expression IS '表达式';
COMMENT ON COLUMN bpm_process_expression.creator IS '创建者';
COMMENT ON COLUMN bpm_process_expression.create_time IS '创建时间';
COMMENT ON COLUMN bpm_process_expression.updater IS '更新者';
COMMENT ON COLUMN bpm_process_expression.update_time IS '更新时间';
COMMENT ON COLUMN bpm_process_expression.deleted IS '是否删除';
COMMENT ON COLUMN bpm_process_expression.tenant_id IS '租户编号';

-- ========== bpm_process_listener 流程监听器 ==========
CREATE TABLE IF NOT EXISTS bpm_process_listener (
    id          bigint        NOT NULL DEFAULT nextval('bpm_process_listener_seq'),
    name        varchar(128)  NOT NULL,
    status      int           NOT NULL,
    type        varchar(32)   NOT NULL DEFAULT '',
    event       varchar(32)   NOT NULL DEFAULT '',
    value_type  varchar(32)   NOT NULL DEFAULT '',
    value       varchar(1024) DEFAULT '',
    creator     varchar(64)   DEFAULT '',
    create_time timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater     varchar(64)   DEFAULT '',
    update_time timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     smallint      NOT NULL DEFAULT 0,
    tenant_id   bigint        NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE bpm_process_listener IS 'BPM 流程监听器';
COMMENT ON COLUMN bpm_process_listener.id IS '主键 ID';
COMMENT ON COLUMN bpm_process_listener.name IS '监听器名字';
COMMENT ON COLUMN bpm_process_listener.status IS '状态';
COMMENT ON COLUMN bpm_process_listener.type IS '监听类型（execution/task）';
COMMENT ON COLUMN bpm_process_listener.event IS '监听事件';
COMMENT ON COLUMN bpm_process_listener.value_type IS '值类型（class/delegateExpression/expression）';
COMMENT ON COLUMN bpm_process_listener.value IS '值';
COMMENT ON COLUMN bpm_process_listener.creator IS '创建者';
COMMENT ON COLUMN bpm_process_listener.create_time IS '创建时间';
COMMENT ON COLUMN bpm_process_listener.updater IS '更新者';
COMMENT ON COLUMN bpm_process_listener.update_time IS '更新时间';
COMMENT ON COLUMN bpm_process_listener.deleted IS '是否删除';
COMMENT ON COLUMN bpm_process_listener.tenant_id IS '租户编号';

-- ========== bpm_process_instance_copy 流程抄送 ==========
CREATE TABLE IF NOT EXISTS bpm_process_instance_copy (
    id                      bigint        NOT NULL DEFAULT nextval('bpm_process_instance_copy_seq'),
    start_user_id           bigint        NOT NULL,
    process_instance_name   varchar(128)  NOT NULL DEFAULT '',
    process_instance_id     varchar(128)  NOT NULL DEFAULT '',
    process_definition_id   varchar(128)  NOT NULL DEFAULT '',
    category                varchar(128)  DEFAULT '',
    activity_id             varchar(128)  DEFAULT '',
    activity_name           varchar(128)  DEFAULT '',
    task_id                 varchar(128)  DEFAULT '',
    user_id                 bigint        NOT NULL,
    reason                  varchar(1024) DEFAULT '',
    creator                 varchar(64)   DEFAULT '',
    create_time             timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater                 varchar(64)   DEFAULT '',
    update_time             timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted                 smallint      NOT NULL DEFAULT 0,
    tenant_id               bigint        NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);
CREATE INDEX idx_bpm_process_instance_copy_01 ON bpm_process_instance_copy (user_id);
CREATE INDEX idx_bpm_process_instance_copy_02 ON bpm_process_instance_copy (process_instance_id);
COMMENT ON TABLE bpm_process_instance_copy IS 'BPM 流程抄送';
COMMENT ON COLUMN bpm_process_instance_copy.id IS '编号';
COMMENT ON COLUMN bpm_process_instance_copy.start_user_id IS '发起人 ID';
COMMENT ON COLUMN bpm_process_instance_copy.process_instance_name IS '流程名';
COMMENT ON COLUMN bpm_process_instance_copy.process_instance_id IS '流程实例编号';
COMMENT ON COLUMN bpm_process_instance_copy.process_definition_id IS '流程定义编号';
COMMENT ON COLUMN bpm_process_instance_copy.category IS '流程分类';
COMMENT ON COLUMN bpm_process_instance_copy.activity_id IS '流程活动编号';
COMMENT ON COLUMN bpm_process_instance_copy.activity_name IS '流程活动名字';
COMMENT ON COLUMN bpm_process_instance_copy.task_id IS '任务编号';
COMMENT ON COLUMN bpm_process_instance_copy.user_id IS '被抄送用户编号';
COMMENT ON COLUMN bpm_process_instance_copy.reason IS '抄送意见';
COMMENT ON COLUMN bpm_process_instance_copy.creator IS '创建者';
COMMENT ON COLUMN bpm_process_instance_copy.create_time IS '创建时间';
COMMENT ON COLUMN bpm_process_instance_copy.updater IS '更新者';
COMMENT ON COLUMN bpm_process_instance_copy.update_time IS '更新时间';
COMMENT ON COLUMN bpm_process_instance_copy.deleted IS '是否删除';
COMMENT ON COLUMN bpm_process_instance_copy.tenant_id IS '租户编号';
