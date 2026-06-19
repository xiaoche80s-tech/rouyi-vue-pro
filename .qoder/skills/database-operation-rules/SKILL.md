---
name: database-operation-rules
description: 规范 MCP 数据库写操作（DDL）的安全规则与 SQL 归档。当通过 MCP 工具（postgres-write 的 execute_sql 等）执行数据库写操作（CREATE TABLE、ALTER TABLE、DROP TABLE、CREATE INDEX、TRUNCATE 等 DDL 语句,以及 INSERT、UPDATE、DELETE 等 DML）时，自动触发此规则：根据表前缀判断是否允许执行，并在执行后自动归档 SQL 到 db/branches/{branchName}/ 目录。
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

### 规则三：写操作 SQL 归档（每次 DDL 必须执行）

每次成功执行 DDL 写操作后，**必须**完成以下两步归档：

#### 3.1 记录历史 SQL

将本次执行的完整 DDL SQL 写入历史文件：

```
db/branches/{branchName}/history/yyyy-MM-dd_HHmmss.sql
```

- `{branchName}`：当前 Git 分支名（通过 `git branch --show-current` 获取），**分支名中的 `/` 替换为 `_`**（如 `feature/add-dealer-module` → `feature_add-dealer-module`）
- `yyyy-MM-dd_HHmmss`：执行时的时间戳
- 文件头部注释格式：

```sql
-- 执行时间: yyyy-MM-dd HH:mm:ss
-- 分支: {branchName}
-- 操作说明: <简要描述本次操作>

<DDL SQL 语句>
```

#### 3.2 合并到分支 SQL（DDL 与 DML 分文件）

将本次 SQL **按类型分别追加**到对应的分支汇总文件，**DDL 与 DML 严禁混在同一文件**：

| SQL 类型 | 汇总文件 | 包含语句 |
|---------|---------|----------|
| DDL | `{branchName}_ddl.sql` | `CREATE TABLE`、`ALTER TABLE`、`DROP TABLE`、`CREATE INDEX`、`DROP INDEX`、`TRUNCATE`、`CREATE SEQUENCE` |
| DML | `{branchName}_dml.sql` | `INSERT`、`UPDATE`、`DELETE` |

文件路径：

```
db/branches/{branchName}/{branchName}_ddl.sql
db/branches/{branchName}/{branchName}_dml.sql
```

- 如果文件不存在，创建并写入文件头注释：

```sql
-- DDL 汇总文件
-- ================================================
-- 分支: {branchName}
-- 描述: <分支相关的所有 DDL 变更汇总>
-- ================================================
```

```sql
-- DML 汇总文件
-- ================================================
-- 分支: {branchName}
-- 描述: <分支相关的所有 DML 数据变更汇总>
-- ================================================
```

- 如果文件已存在，在文件末尾追加本次 SQL（用空行分隔）
- 如果本次操作只有 DDL 或只有 DML，仅更新对应文件，另一个不创建/不修改

#### 目录结构示例

> 分支名中的 `/` 在目录和文件名中统一替换为 `_`。例如 Git 分支 `feature/add-dealer-module` → 目录名和文件名均使用 `feature_add-dealer-module`。

```
db/branches/
  └── feature_add-dealer-module/
      ├── feature_add-dealer-module_ddl.sql    ← DDL 汇总（建表、改表、索引）
      ├── feature_add-dealer-module_dml.sql    ← DML 汇总（INSERT/UPDATE/DELETE）
      └── history/
          ├── 2026-06-13_233000.sql            ← 第一次执行（DDL）
          ├── 2026-06-13_235000.sql            ← 第二次执行（DML）
          └── 2026-06-14_101500.sql            ← 第三次执行（DDL）
```

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
    ↓
执行成功后（无论哪个分支）
    ↓
1. 写入历史文件: db/branches/{branchName}/history/yyyy-MM-dd_HHmmss.sql
2. 判断 SQL 类型：
   ├── DDL → 追加到 db/branches/{branchName}/{branchName}_ddl.sql
   └── DML → 追加到 db/branches/{branchName}/{branchName}_dml.sql
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

**SQL 归档示例：**
```
当前分支: feature/dealer-product-line

操作 1 - DDL: ALTER TABLE ops_dealer ADD COLUMN remark VARCHAR(500);
操作 2 - DML: INSERT INTO ops_dealer (id, name, code) VALUES (1, '测试经销商', 'D001');

归档操作:
1. 写入历史文件（每次执行各一个文件）:
   db/branches/feature_dealer-product-line/history/2026-06-13_233000.sql  ← DDL
   db/branches/feature_dealer-product-line/history/2026-06-13_233100.sql  ← DML

2. DDL 追加到:
   db/branches/feature_dealer-product-line/feature_dealer-product-line_ddl.sql

3. DML 追加到:
   db/branches/feature_dealer-product-line/feature_dealer-product-line_dml.sql
```
