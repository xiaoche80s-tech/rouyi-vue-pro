---
name: database-operation-rules
description: 规范 MCP 数据库写操作（DDL）的安全规则。当通过 MCP 工具（postgres-write 的 execute_sql 等）执行数据库写操作（CREATE TABLE、ALTER TABLE、DROP TABLE、CREATE INDEX、TRUNCATE 等 DDL 语句）时，自动触发此规则，根据表前缀判断是否允许执行。
---

# 数据库 DDL 操作规范

## 适用场景

当调用 MCP 工具（如 `postgres-write` 的 `execute_sql`）执行以下 DDL 语句时，必须遵守本规范：

- `CREATE TABLE` / `DROP TABLE`
- `ALTER TABLE`（增删改列、约束、索引）
- `CREATE INDEX` / `DROP INDEX`
- `TRUNCATE TABLE`
- 其他结构性变更语句

## 操作规则

### 规则一：禁止操作（绝对不允许执行）

以下表前缀属于框架/引擎核心表，**严禁执行任何 DDL 操作**，即使用户明确要求也应拒绝并说明原因：

| 表前缀 | 说明 | 数量（约） |
|--------|------|-----------|
| `act_*` | Flowable 工作流引擎表 | 39 |
| `flw_*` | Flowable 事件表 | 4 |
| `bpm_*` | BPM 业务表 | 7 |
| `qrtz_*` | Quartz 定时任务表 | 11 |
| `yudao_demo*` | 示例 / Demo 表 | 5 |

**触发时行为**：直接拒绝执行，向用户说明该表属于受保护表，不可通过 DDL 修改。

### 规则二：需人类确认后方可操作

以下表前缀属于平台核心数据表，**必须获得用户明确同意后才能执行**：

| 表前缀 | 说明 | 数量（约） |
|--------|------|-----------|
| `infra_*` | 基础设施表 | 10 |
| `system_*` | 系统管理表 | 28 |

**触发时行为**：

1. 向用户展示将要执行的**完整 SQL 语句**
2. 明确询问："即将对 `xxx` 表执行 DDL 操作，是否同意执行？"
3. **只有在用户明确回复同意/确认/可以执行后**，才可执行该 SQL
4. 若用户拒绝，停止执行并记录原因

## 判断流程

```
收到 DDL 语句
    ↓
解析目标表名
    ↓
匹配表前缀
    ↓
├── act_* / flw_* / bpm_* / qrtz_* / yudao_demo*  → 拒绝执行，说明原因
├── infra_* / system_*                             → 展示 SQL，等待用户确认
└── 其他业务表                                     → 直接执行
```

## 示例

**禁止操作示例：**
```
用户：给 act_ge_property 表加一个字段
响应：该表属于 Flowable 引擎核心表（act_* 前缀），受数据库操作规范保护，不允许执行 DDL 变更。
```

**需确认操作示例：**
```
用户：给 system_users 表增加 remark 字段
响应：即将对 system_users 表执行以下 DDL：
ALTER TABLE system_users ADD COLUMN remark VARCHAR(500);
该表属于系统管理核心表（system_* 前缀），是否同意执行？
```
