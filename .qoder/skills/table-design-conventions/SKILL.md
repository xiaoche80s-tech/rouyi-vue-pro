---
name: table-design-conventions
description: 规范数据库创表语句（DDL）的设计约定。当生成 CREATE TABLE / ALTER TABLE 语句、用户要求新建或修改表结构、审查表设计合理性时，自动应用此规范。核心规则：禁止使用数据库外键约束，表间关联使用业务唯一字段而非自增 ID，禁止使用数据库保留关键字作为字段名。
---

# 创表语句设计规范

## 核心原则

### 1. 禁止使用数据库外键约束

**绝对不允许**在 DDL 中使用 `FOREIGN KEY` / `REFERENCES` 约束。表间关系通过应用层逻辑维护，不在数据库层建立外键约束。

**原因**：
- 外键约束会导致跨表操作强耦合，增加数据迁移和分库分表的复杂度
- 高并发场景下外键检查带来额外性能开销
- 微服务/模块化架构中，关联表可能分布在不同服务，外键无法跨库

**错误示例**：
```sql
-- ✗ 禁止：使用外键约束
CREATE TABLE ops_order (
    id          int8 NOT NULL,
    dealer_id   int8 NOT NULL,
    CONSTRAINT fk_order_dealer FOREIGN KEY (dealer_id) REFERENCES ops_dealer_info(id)
);
```

**正确示例**：
```sql
-- ✓ 正确：普通列 + 索引，应用层维护关系
CREATE TABLE ops_order (
    id          int8 NOT NULL,
    dealer_id   int8 NOT NULL
);
CREATE INDEX idx_ops_order_dealer ON ops_order (dealer_id);
```

### 2. 表间关联使用业务唯一字段，不使用自增 ID

建立表之间的关联关系时，**必须使用业务唯一标识字段**（如编码 `code`），而非自增主键 `id`。

**原因**：
- 业务字段具有语义，便于数据排查、数据迁移和多环境同步
- 自增 ID 在不同环境（开发/测试/生产）中不一致，数据导入导出容易错乱
- 使用业务字段关联天然避免了对外键的依赖

**错误示例**：
```sql
-- ✗ 禁止：用自增 ID 关联
CREATE TABLE ops_dealer_product_line_relation (
    id              int8 NOT NULL,
    dealer_id       int8 NOT NULL,      -- 关联 ops_dealer_info.id
    product_line_id int8 NOT NULL       -- 关联 ops_dealer_product_line.id
);
```

**正确示例**：
```sql
-- ✓ 正确：用业务唯一字段关联
CREATE TABLE ops_dealer_product_line_relation (
    id              int8         NOT NULL,
    dealer_code     varchar(50)  NOT NULL,  -- 关联 ops_dealer_info.code
    product_line_code varchar(50) NOT NULL   -- 关联 ops_dealer_product_line.code
);
CREATE UNIQUE INDEX uk_ops_dealer_pl_relation
    ON ops_dealer_product_line_relation (dealer_code, product_line_code);
```

**例外情况**：
- 与框架内置表（`system_users`、`system_dept` 等）关联时，因为这些表没有业务编码字段或编码字段不稳定，可使用 `user_id` / `dept_id` 等 ID 字段关联
- 纯中间关联表（多对多映射表）且双方表都无稳定业务编码时，经确认后可以使用 ID 关联

### 3. 禁止使用数据库保留关键字作为字段名

字段名和表名**不得使用** MySQL、PostgreSQL 等数据库的保留关键字。

**原因**：
- 保留关键字作为字段名会导致 SQL 解析错误，必须加引号（`"order"`、`` `order` ``）才能使用，增加开发和维护成本
- 不同数据库的保留关键字列表不同，同一字段名在 MySQL 正常但在 PostgreSQL 可能报错
- ORM 框架（MyBatis、Hibernate）生成 SQL 时通常不加引号，容易触发隐蔽的运行时错误

**常见高风险保留关键字**（业务建表最易踩坑，完整列表见 [reserved-keywords.md](reserved-keywords.md)）：

| 关键字 | 风险等级 | 安全替代方案 |
|--------|---------|-------------|
| `order` | 极高 | `order_no`、`sort_order` |
| `group` | 极高 | `group_name`、`group_code` |
| `key` | 极高 | `access_key`、`secret_key` |
| `index` | 高 | `idx`、`sort_index` |
| `value` | 高 | `amount`、`data_value` |
| `check` | 高 | `check_flag`、`is_checked` |
| `desc` | 高 | `description`、`remark` |
| `limit` | 高 | `max_count`、`threshold` |
| `offset` | 高 | `page_offset`、`data_offset` |
| `rank` | 高 | `sort_rank`、`level_rank` |
| `signal` | 中高 | `alert_signal`、`trigger_flag` |
| `trigger` | 中高 | `trigger_flag`、`event_trigger` |
| `schema` | 中高 | `schema_name`、`db_schema` |
| `grant` | 中高 | `grant_flag`、`is_granted` |
| `usage` | 中高 | `use_count`、`usage_type` |
| `table` | 中高 | `table_name`、`table_code` |
| `column` | 中高 | `column_name`、`col_code` |
| `row` / `rows` | 中 | `row_num`、`row_count` |
| `range` | 中 | `data_range`、`scope_range` |
| `match` | 中 | `match_flag`、`is_matched` |

**错误示例**：
```sql
-- ✗ 禁止：order、key、desc 均为保留关键字
CREATE TABLE ops_order_item (
    id      int8         NOT NULL,
    order   int8         NOT NULL,    -- order 是保留关键字
    key     varchar(50)  NOT NULL,    -- key 是保留关键字
    desc    varchar(200) DEFAULT NULL -- desc 是保留关键字
);
```

**正确示例**：
```sql
-- ✓ 正确：添加业务前缀或使用语义化命名
CREATE TABLE ops_order_item (
    id           int8         NOT NULL,
    order_no     varchar(50)  NOT NULL,     -- 订单编号
    access_key   varchar(50)  NOT NULL,     -- 访问密钥
    description  varchar(200) DEFAULT NULL   -- 描述
);
```

**命名策略**：
- 使用**业务前缀**：`order` → `order_no`、`order_code`
- 使用**语义化替代**：`desc` → `description`、`key` → `access_key`
- 使用**复合命名**：`group` → `group_name`、`group_code`
- 如果确实无法避免，必须在 DDL 和所有 SQL 中统一使用双引号包裹（如 `"order"`），并在列注释中标注原因

## 建表模板

### 标准表结构

```sql
CREATE TABLE ops_<业务模块>_<实体名> (
    id            int8         NOT NULL,
    -- 业务字段（根据实际需求定义）
    code          varchar(50)  NOT NULL,
    name          varchar(100) NOT NULL,
    -- 关联字段：使用业务唯一字段，不使用外键 ID
    related_code  varchar(50)  NOT NULL,
    -- 框架基础字段（必须包含）
    creator       varchar(64)  DEFAULT '',
    create_time   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater       varchar(64)  DEFAULT '',
    update_time   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted       int2         NOT NULL DEFAULT 0,
    tenant_id     int8         NOT NULL DEFAULT 0
);

-- 主键
ALTER TABLE ops_<业务模块>_<实体名>
    ADD CONSTRAINT pk_ops_<业务模块>_<实体名> PRIMARY KEY (id);

-- 业务唯一索引（code 字段必须有唯一索引）
CREATE UNIQUE INDEX uk_ops_<业务模块>_<实体名>_code
    ON ops_<业务模块>_<实体名> (code);

-- 关联字段索引（用于加速查询，替代外键的级联查询能力）
CREATE INDEX idx_ops_<业务模块>_<实体名>_related
    ON ops_<业务模块>_<实体名> (related_code);

-- 序列（PostgreSQL 必须）
CREATE SEQUENCE ops_<业务模块>_<实体名>_seq START 1;

-- 表注释
COMMENT ON TABLE ops_<业务模块>_<实体名> IS '<中文表名>';
-- 列注释（每列必须有注释）
COMMENT ON COLUMN ops_<业务模块>_<实体名>.id IS '主键';
COMMENT ON COLUMN ops_<业务模块>_<实体名>.code IS '业务编码';
COMMENT ON COLUMN ops_<业务模块>_<实体名>.name IS '名称';
COMMENT ON COLUMN ops_<业务模块>_<实体名>.related_code IS '关联业务编码';
```

## 检查清单

生成或审查 DDL 时，逐项确认：

- [ ] 无 `FOREIGN KEY` / `REFERENCES` 约束
- [ ] 关联字段使用业务唯一字段（如 `code`），而非 `xxx_id`
- [ ] 字段名和表名未使用数据库保留关键字（`order`、`key`、`group`、`desc` 等）
- [ ] 每个业务编码字段有 `UNIQUE INDEX`
- [ ] 关联字段有普通 `INDEX`（加速 JOIN 查询）
- [ ] 表名以 `ops_` 开头
- [ ] 包含框架基础字段：`creator`、`create_time`、`updater`、`update_time`、`deleted`、`tenant_id`
- [ ] 每列都有 `COMMENT ON COLUMN`
- [ ] PostgreSQL 下包含 `SEQUENCE` 定义

## 与 database-operation-rules 的协作

本技能规范表设计层面的约定，`database-operation-rules` 规范 DDL 执行安全与归档流程。两者配合使用：

1. **设计阶段**（本技能）：确保 DDL 语句符合无外键、业务字段关联的设计规范
2. **执行阶段**（database-operation-rules）：根据表前缀判断权限，执行后归档 SQL
