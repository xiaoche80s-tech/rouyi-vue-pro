# opshub-step1.sql 合规整改方案

## Context

审阅 `sql/postgresql/opshub-step1.sql`，发现 5 类不合规问题，需整改后重新归档并同步更新 `db/branches/` 下的 DDL/DML 分离文件。

---

## 问题清单与整改方案

### 问题 1：DDL 与 DML 混写在同一文件

**现状**：`opshub-step1.sql` 同时包含 DDL（5 张 CREATE TABLE）和 DML（角色/菜单/角色-菜单 INSERT）。  
**规范**：DDL 与 DML 必须严格分离到 `_ddl.sql` 和 `_dml.sql` 两个文件。  
**同时**：`db/branches/feature_step1-权限功能设计/` 下的 `_ddl.sql` 和 `_dml.sql` 内容完全相同（都是混合内容），需要修正。

**整改**：
- `sql/postgresql/opshub-step1.sql` → 保留为纯 DDL 文件
- `db/branches/.../feature_step1-权限功能设计_ddl.sql` → 纯 DDL
- `db/branches/.../feature_step1-权限功能设计_dml.sql` → 纯 DML

---

### 问题 2：`ops_dealer_product_line_relation` 不应有 `deleted` 字段

**现状**：表 DDL 含 `deleted int2 NOT NULL DEFAULT 0`。  
**决策**：该表使用物理删除，不适用逻辑删除。

**整改**：删除 `deleted` 字段定义及 COMMENT。

---

### 问题 3：关联表应改用业务编码字段关联

**整改明细**：

**ops_dealer_product_line_relation**：
- `dealer_id int8` → `dealer_code varchar(50)`
- `product_line_id int8` → `product_line_code varchar(50)`
- 索引/唯一索引同步改为 code 字段
- 新增唯一索引 `uk_ops_dealer_pl_relation(dealer_code, product_line_code)`

**ops_dealer_user_scope**：
- `dealer_id int8` → `dealer_code varchar(50)`
- `user_id` 保持不变（关联 system_users 框架表，已豁免）
- 唯一索引改为 `(user_id, dealer_code)`

**ops_executor_product_line_scope**：
- `product_line_id int8` → `product_line_code varchar(50)`
- `user_id` 保持不变（同上豁免）
- 唯一索引改为 `(user_id, product_line_code)`

---

### 问题 4：`ops_dealer_product_line.code` 缺少唯一索引

**整改**：新增 `CREATE UNIQUE INDEX uk_ops_dealer_product_line_code ON ops_dealer_product_line (code);`

---

### 问题 5：文件头注释计数错误

| 注释 | 实际 |
|-----|------|
| 8 菜单 + 36 按钮 = 45 条 | 9 菜单 + 43 按钮 = 53 条 |
| brand_admin 58 条 | 53 条 |
| brand_sales 18 条 | 19 条 |
| service_executor 48 条 | 49 条 |
| dealer 37 条 | 38 条 |

---

## 执行步骤

| Task | 说明 | 涉及文件 |
|------|------|---------|
| 1 | 生成修正后的纯 DDL（5 张表 + 索引 + 注释） | `sql/postgresql/opshub-step1.sql` |
| 2 | 写入纯 DDL 到归档 DDL 文件 | `db/branches/.../feature_step1-权限功能设计_ddl.sql` |
| 3 | 写入纯 DML 到归档 DML 文件 | `db/branches/.../feature_step1-权限功能设计_dml.sql` |

## 验证

- DDL 文件无 INSERT/UPDATE/DELETE
- DML 文件无 CREATE/ALTER/DROP
- 关联表已改用 code 字段
- `ops_dealer_product_line_relation` 无 deleted 字段
- `ops_dealer_product_line` 有 code 唯一索引
- 注释计数正确
