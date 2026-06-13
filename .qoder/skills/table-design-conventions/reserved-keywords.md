# 数据库保留关键字完整参考

> 来源：MySQL 8.4 + PostgreSQL 18 官方文档
> 仅列出**_reserved_**（保留）关键字，非保留关键字（non-reserved）通常可安全用作字段名。

## 一、MySQL + PostgreSQL 双重保留（最高风险）

以下关键字在 MySQL 和 PostgreSQL 中**均为保留字**，绝对不可用作字段名：

```
ALL, AND, ANY, AS, ASC, BOTH, CASE, CAST, CHECK, COLLATE, COLUMN,
CONSTRAINT, CREATE, CROSS, CURRENT_DATE, CURRENT_TIME, CURRENT_TIMESTAMP,
CURRENT_USER, DEFAULT, DESC, DISTINCT, ELSE, END, EXCEPT, EXISTS,
FALSE, FETCH, FOR, FOREIGN, FROM, FULL, GRANT, GROUP, HAVING,
IN, INNER, INTERSECT, INTO, IS, JOIN, LEADING, LEFT, LIKE,
NATURAL, NOT, NULL, OF, ON, ONLY, OR, ORDER, OUTER, PRIMARY,
REFERENCES, RIGHT, SELECT, SESSION_USER, SOME, SYMMETRIC, SYSTEM_USER,
TABLE, THEN, TO, TRAILING, TRUE, UNION, UNIQUE, USER, USING,
VALUES, WHEN, WHERE, WITH
```

## 二、MySQL 独有保留关键字

以下关键字仅在 MySQL 中保留（PostgreSQL 中为非保留，但仍建议避免）：

```
ACCESSIBLE, ADD, ANALYZE, ASENSITIVE, BEFORE, BETWEEN, BIGINT, BINARY,
BLOB, BY, CALL, CASCADE, CHANGE, CHAR, CHARACTER, CONDITION, CONTINUE,
CONVERT, CUBE, CUME_DIST, DATABASE, DATABASES, DAY_HOUR, DAY_MICROSECOND,
DAY_MINUTE, DAY_SECOND, DEC, DECIMAL, DECLARE, DELAYED, DELETE,
DENSE_RANK, DESCRIBE, DETERMINISTIC, DISTINCTROW, DIV, DOUBLE, DROP,
DUAL, EACH, ELSEIF, EMPTY, ENCLOSED, ESCAPED, EXPLAIN, EXIT,
FIRST_VALUE, FLOAT, FLOAT4, FLOAT8, FORCE, FULLTEXT, FUNCTION, GENERATED,
GET, GROUPING, GROUPS, HIGH_PRIORITY, HOUR_MICROSECOND, HOUR_MINUTE,
HOUR_SECOND, IF, IGNORE, INFILE, INOUT, INSENSITIVE, INSERT, INT,
INT1, INT2, INT3, INT4, INT8, INTEGER, INTERVAL, IO_AFTER_GTIDS,
IO_BEFORE_GTIDS, ITERATE, JSON_TABLE, KEY, KEYS, KILL, LAG,
LAST_VALUE, LATERAL, LEAD, LEAVE, LIMIT, LINEAR, LINES, LOAD,
LOCALTIME, LOCALTIMESTAMP, LOCK, LONG, LONGBLOB, LONGTEXT, LOOP,
LOW_PRIORITY, MANUAL, MATCH, MAXVALUE, MEDIUMBLOB, MEDIUMINT, MEDIUMTEXT,
MIDDLEINT, MINUTE_MICROSECOND, MINUTE_SECOND, MOD, MODIFIES, NO_WRITE_TO_BINLOG,
NTH_VALUE, NTILE, NUMERIC, OPTIMIZE, OPTIMIZER_COSTS, OPTION, OPTIONALLY,
OUT, OUTFILE, OVER, PARALLEL, PARTITION, PERCENT_RANK, PRECISION,
PROCEDURE, PURGE, QUALIFY, RANGE, RANK, READ, READS, READ_WRITE,
REAL, RECURSIVE, REGEXP, RELEASE, RENAME, REPEAT, REPLACE, REQUIRE,
RESIGNAL, RESTRICT, RETURN, REVOKE, RLIKE, ROW, ROWS, ROW_NUMBER,
SCHEMA, SCHEMAS, SECOND_MICROSECOND, SENSITIVE, SEPARATOR, SHOW, SIGNAL,
SMALLINT, SPATIAL, SPECIFIC, SQL, SQLEXCEPTION, SQLSTATE, SQLWARNING,
SQL_BIG_RESULT, SQL_CALC_FOUND_ROWS, SQL_SMALL_RESULT, SSL, STARTING,
STORED, STRAIGHT_JOIN, SYSTEM, TABLESAMPLE, TERMINATED, TINYBLOB, TINYINT,
TINYTEXT, TRIGGER, UNDO, UNLOCK, UNSIGNED, UPDATE, USAGE, USE,
USING, UTC_DATE, UTC_TIME, UTC_TIMESTAMP, VARBINARY, VARCHAR, VARCHARACTER,
VARYING, VIRTUAL, WHILE, WINDOW, WRITE, XOR, YEAR_MONTH, ZEROFILL
```

## 三、PostgreSQL 独有保留关键字

以下关键字仅在 PostgreSQL 中保留（MySQL 中为非保留，但仍建议避免）：

```
ANALYSE, ANALYZE, ARRAY, ASYMMETRIC, AUTHORIZATION, BINARY, CONCURRENTLY,
CONSTRAINT, COPY, DEFERRABLE, DO, EXCEPT, FETCH, FOR, FREEZE, FULL,
GRANT, GROUP, HAVING, ILIKE, INITIALLY, INTERSECT, INTO, IS, ISNULL,
LATERAL, LIMIT, NATURAL, NOTNULL, OFFSET, ONLY, ORDER, OUTER, OVERLAPS,
PLACING, RETURNING, SIMILAR, TABLE, TABLESAMPLE, TO, UNION, USER,
VERBOSE, WHERE, WINDOW, WITH
```

## 四、常见业务场景易踩坑关键字

以下关键字在业务建表时**最容易被误用**为字段名，需特别注意：

| 关键字 | 业务场景 | 风险等级 | 安全替代方案 |
|--------|---------|---------|-------------|
| `order` | 订单、排序 | **极高** | `order_no`, `sort_order` |
| `group` | 分组、用户组 | **极高** | `group_code`, `group_name` |
| `key` | 密钥、键值 | **极高** | `access_key`, `secret_key` |
| `index` | 索引、序号 | **高** | `idx`, `sort_index` |
| `value` | 数值、键值对 | **高** | `amount`, `data_value`, `val` |
| `check` | 校验、检查 | **高** | `check_flag`, `is_checked` |
| `desc` / `describe` | 描述 | **高** | `description`, `remark` |
| `limit` | 限制、额度 | **高** | `max_count`, `threshold` |
| `offset` | 偏移量 | **高** | `page_offset`, `data_offset` |
| `rank` | 排名、等级 | **高** | `sort_rank`, `level_rank` |
| `signal` | 信号、标志 | **中高** | `alert_signal`, `trigger_flag` |
| `trigger` | 触发器、触发条件 | **中高** | `trigger_flag`, `event_trigger` |
| `schema` | 模式、结构 | **中高** | `schema_name`, `db_schema` |
| `grant` | 授权 | **中高** | `grant_flag`, `is_granted` |
| `usage` | 使用量 | **中高** | `use_count`, `usage_type` |
| `table` | 表、表格 | **中高** | `table_name`, `table_code` |
| `column` | 列、栏目 | **中高** | `column_name`, `col_code` |
| `row` | 行、记录 | **中** | `row_num`, `line_no` |
| `rows` | 行数 | **中** | `row_count`, `total_rows` |
| `range` | 范围 | **中** | `data_range`, `scope_range` |
| `partition` | 分区 | **中** | `partition_name`, `part_code` |
| `procedure` | 存储过程 | **中** | `proc_name`, `procedure_code` |
| `constraint` | 约束 | **中** | `constraint_name`, `limit_rule` |
| `match` | 匹配 | **中** | `match_flag`, `is_matched` |
| `level` | 级别、等级 | **低** | `level_code`, `rank_level` |
| `role` | 角色 | **低** | `role_code`, `role_name` |
| `user` | 用户 | **低** | `user_id`, `user_code` |
| `status` | 状态 | **低** | 可用但需注意上下文 |
| `type` | 类型 | **低** | 可用但建议 `type_code`, `data_type` |
| `name` | 名称 | **低** | 可用但建议 `xxx_name` 复合命名 |
| `comment` | 注释 | **低** | 可用但建议 `remark`, `note` |

## 五、命名策略总结

当字段语义与保留关键字冲突时，按以下优先级选择替代方案：

1. **添加业务前缀**（最推荐）：`order` → `order_no`、`group` → `group_code`
2. **使用同义语义词**：`desc` → `description`、`key` → `access_key`
3. **添加通用后缀**：`value` → `data_value`、`rank` → `sort_rank`
4. **最后手段**：必须加双引号包裹并在注释中标注原因
