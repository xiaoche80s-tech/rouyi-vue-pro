# Step 1 — 角色、菜单权限、经销商管理、产品线管理

> **版本**: V1.0 | **日期**: 2026-06-13  
> **文档性质**: 分阶段实施 PRD — Step 1（基础设施层）  
> **前置文档**: `docs/PRD-用户权限设计.md`（总体权限设计）  
> **技术约束**: `system_users` 表不可修改，通过扩展表实现授权关联

---

## 一、Step 1 目标

搭建经销商管理 SaaS 平台的基础设施层，为后续业务模块提供完整的数据和权限支撑：

| 目标 | 说明 |
|------|------|
| 角色体系 | 创建 4 个业务角色，完成 RBAC 权限体系 |
| 菜单权限 | 为 6 大业务模块建立菜单树和按钮级权限 |
| 经销商管理 | 经销商基础数据 CRUD + 用户-经销商授权关联 |
| 产品线管理 | 产品线基础数据 CRUD + 经销商-产品线关联 |
| 数据权限基础 | 建立扩展表驱动的授权模型，为 DealerDataPermissionRule 提供数据基础 |

**本阶段不包含**：六大业务模块开发、注册审核流程、角色切换、AI 客服。

---

## 二、角色定义

### 2.1 角色总览

| # | 角色名称 | 角色标识 (code) | 数据范围 | 核心职责 |
|---|---------|----------------|---------|---------|
| 0 | 超级管理员 | `super_admin` | 全部数据 | 系统配置、用户管理（系统内置） |
| 1 | 品牌管理员 | `brand_admin` | 全部经销商（可按产品线限定） | 全局监控、数据导入、合同下发、工单催办 |
| 2 | 品牌销售员 | `brand_sales` | 全部经销商（可按产品线限定） | 仅查看数据 |
| 3 | 服务单执行员 | `service_executor` | 授权产品线内的全部经销商 | 工单处理、咨询回复、操作请求执行 |
| 4 | 经销商 | `dealer` | 仅自身管理的经销商 | 查看自有数据、发起操作请求、验收工单 |

### 2.2 角色与授权维度的关系

```
┌─────────────┐     ┌──────────────────────────┐     ┌─────────────────┐
│ system_users│     │   授权扩展表（多对多）      │     │   基础数据表      │
│  (用户表)    │     │                           │     │                 │
│             │────▶│ ops_dealer_user_scope      │────▶│ ops_dealer_info │
│  user_id    │     │ (用户 ↔ 经销商 授权关联)    │     │ (经销商实体)     │
│             │────▶│ ops_executor_product_line_ │────▶│ ops_dealer_     │
│             │     │  scope (执行员↔产品线授权)   │     │  product_line  │
└─────────────┘     └──────────────────────────┘     └─────────────────┘
                                                            │
                                                            ▼
                                                   ┌─────────────────┐
                                                   │ ops_dealer_product_ │
                                                   │ line_relation     │
                                                   │ (经销商 ↔ 产品线) │
                                                   └─────────────────┘
```

**各角色的授权维度**：

| 角色 | 授权维度 | 数据来源（扩展表） | 说明 |
|------|---------|------------------|------|
| super_admin | 无限制 | 无需查扩展表 | 全部数据可见 |
| brand_admin | productLineScope | `ops_executor_product_line_scope` | 可按产品线限定可见范围，无记录则不限制 |
| brand_sales | productLineScope | `ops_executor_product_line_scope` | 可按产品线限定可见范围，无记录则不限制 |
| service_executor | productLineScope | `ops_executor_product_line_scope` | 必须配置，仅可见授权产品线内的数据 |
| dealer | dealerScope | `ops_dealer_user_scope` | 必须配置，仅可见授权经销商的数据 |

---

## 三、菜单权限设计

### 3.1 菜单树结构

```
dealer（经销商管理 SaaS）                     一级目录
├── signing（签约进度）                         二级菜单
│   ├── dealer:signing:query                   按钮 - 查看
│   ├── dealer:signing:sign                    按钮 - 发起签署（经销商）
│   ├── dealer:signing:consult                 按钮 - 咨询
│   ├── dealer:signing:import                  按钮 - 导入（管理员）
│   ├── dealer:signing:stamp                   按钮 - 申请盖章（经销商）
│   └── dealer:signing:export                  按钮 - 导出（管理员）
├── policy（政策看板）                          二级菜单
│   ├── dealer:policy:query                    按钮 - 查看
│   └── dealer:policy:consult                  按钮 - 咨询
├── aftersale（售后模块）                       二级菜单
│   ├── dealer:aftersale:query                 按钮 - 查看
│   ├── dealer:aftersale:consult               按钮 - 咨询
│   └── dealer:aftersale:return                按钮 - 发起退货（经销商）
├── order（订单模块）                           二级菜单
│   ├── dealer:order:query                     按钮 - 查看
│   ├── dealer:order:pay                       按钮 - 申请付款（经销商）
│   ├── dealer:order:invoice                   按钮 - 申请开票（经销商）
│   ├── dealer:order:return                    按钮 - 申请退货（经销商）
│   └── dealer:order:consult                   按钮 - 咨询
├── basedata（基础数据）                        二级菜单
│   ├── dealer:basedata:query                  按钮 - 查看
│   ├── dealer:basedata:ai                     按钮 - AI 解读
│   ├── dealer:basedata:download               按钮 - 下载
│   └── dealer:basedata:stamp                  按钮 - 申请盖章（经销商）
├── service（客户服务）                         二级菜单
│   ├── service:task:query                     按钮 - 查看工单
│   ├── service:task:create                    按钮 - 创建任务（管理员）
│   ├── service:task:accept                    按钮 - 接单（执行员）
│   ├── service:task:submit                    按钮 - 提交结果（执行员）
│   ├── service:task:verify                    按钮 - 验收（经销商）
│   ├── service:task:urge                      按钮 - 催办（管理员）
│   ├── service:consult:query                  按钮 - 查看咨询
│   ├── service:consult:reply                  按钮 - 回复（执行员）
│   ├── service:consult:close                  按钮 - 关闭（经销商）
│   ├── service:consult:complete               按钮 - 完成处理（执行员）
│   ├── service:opreq:query                    按钮 - 查看操作请求
│   ├── service:opreq:create                   按钮 - 发起请求（经销商）
│   ├── service:opreq:accept                   按钮 - 接单（执行员）
│   ├── service:opreq:submit                   按钮 - 提交结果（执行员）
│   └── service:opreq:verify                   按钮 - 验收（经销商）
├── dealer-mgmt（经销商管理）— Step 1 实现      二级菜单
│   ├── dealer:mgmt:query                      按钮 - 查看经销商列表
│   ├── dealer:mgmt:create                     按钮 - 新增经销商
│   ├── dealer:mgmt:update                     按钮 - 编辑经销商
│   ├── dealer:mgmt:delete                     按钮 - 删除经销商
│   ├── dealer:mgmt:bindproduct                按钮 - 绑定产品线
│   └── dealer:mgmt:binduser                   按钮 - 绑定用户
└── product-line-mgmt（产品线管理）— Step 1 实现  二级菜单
    ├── dealer:productline:query                按钮 - 查看产品线
    ├── dealer:productline:create               按钮 - 新增产品线
    ├── dealer:productline:update               按钮 - 编辑产品线
    └── dealer:productline:delete               按钮 - 删除产品线
```

### 3.2 菜单权限矩阵（4 业务角色 × Step 1 模块）

| 模块 | 品牌管理员 | 品牌销售员 | 服务单执行员 | 经销商 |
|------|:---------:|:---------:|:----------:|:-----:|
| 经销商管理 | 增删改查 + 绑定 | 只读 | 只读 | 只读（自己管理的） |
| 产品线管理 | 增删改查 | 只读 | 只读 | 只读 |
| 签约进度 | 读写 | 只读 | 读写 | 读写 |
| 政策看板 | 读写 | 只读 | 读写 | 只读 |
| 售后模块 | 读写 | 只读 | 读写 | 读写 |
| 订单模块 | 读写 | 只读 | 读写 | 读写 |
| 基础数据 | 读写 | 只读 | 读写 | 读写 |
| 客户服务 | 读写 | 不可见 | 读写 | 读写 |

> Step 1 仅实现「经销商管理」和「产品线管理」两个模块的完整 CRUD，其余 6 个业务模块菜单先创建但功能在后续 Step 实现。

### 3.3 按钮级权限标识（Step 1 新增）

| 权限标识 | 说明 | 品牌管理员 | 品牌销售员 | 服务单执行员 | 经销商 |
|---------|------|:---------:|:---------:|:----------:|:-----:|
| `dealer:mgmt:query` | 查看经销商列表 | ✅ | ✅ | ✅ | ✅ |
| `dealer:mgmt:create` | 新增经销商 | ✅ | — | — | — |
| `dealer:mgmt:update` | 编辑经销商 | ✅ | — | — | — |
| `dealer:mgmt:delete` | 删除经销商 | ✅ | — | — | — |
| `dealer:mgmt:bindproduct` | 绑定/解绑产品线 | ✅ | — | — | — |
| `dealer:mgmt:binduser` | 绑定/解绑用户 | ✅ | — | — | — |
| `dealer:productline:query` | 查看产品线列表 | ✅ | ✅ | ✅ | ✅ |
| `dealer:productline:create` | 新增产品线 | ✅ | — | — | — |
| `dealer:productline:update` | 编辑产品线 | ✅ | — | — | — |
| `dealer:productline:delete` | 删除产品线 | ✅ | — | — | — |

---

## 四、数据模型

### 4.1 核心关系

```
┌──────────────┐       ┌────────────────────┐       ┌───────────────────┐
│ system_users │       │ dealer_user_scope  │       │   dealer_info     │
│  (用户表)     │ 1:N   │ (用户-经销商授权)   │  N:1  │  (经销商实体)      │
│              │──────▶│                    │◀──────│                   │
│ id           │       │ user_id            │       │ id                │
│ username     │       │ dealer_id          │       │ name              │
│ nickname     │       │                    │       │ code              │
└──────────────┘       └────────────────────┘       └───────────────────┘
                                                            │
                                                   N:M      │
        ┌────────────────────┐       ┌──────────────────────┴──────┐
        │ executor_product_  │       │ dealer_product_line_relation │
        │ line_scope         │       │ (经销商-产品线关联)           │
        │ (执行员-产品线授权)  │       │                              │
        │                    │       │ dealer_id                    │
        │ user_id            │       │ product_line_id              │
        │ product_line_id    │       └──────────────────────────────┘
        └────────────────────┘                   │
               │                                 │ N:1
               │                                 ▼
               │                      ┌───────────────────┐
               └─────────────────────▶│ dealer_product_line│
                                      │  (产品线实体)       │
                                      │ id                 │
                                      │ name               │
                                      │ code               │
                                      └───────────────────┘
```

### 4.2 表清单

| # | 表名 | 类型 | 说明 |
|---|------|------|------|
| 1 | `ops_dealer_info` | 基础数据 | 经销商信息表 |
| 2 | `ops_dealer_product_line` | 基础数据 | 产品线表 |
| 3 | `ops_dealer_product_line_relation` | 关联表 | 经销商 ↔ 产品线（多对多） |
| 4 | `ops_dealer_user_scope` | 授权扩展表 | 用户 ↔ 经销商授权（dealer 角色用） |
| 5 | `ops_executor_product_line_scope` | 授权扩展表 | 用户 ↔ 产品线授权（执行员/管理员/销售员角色用） |

> **设计决策**：`system_users` 表不新增任何字段。所有经销商/产品线维度的授权关系均通过独立的扩展关联表实现。这样既不侵入系统表，又支持灵活的多对多关系。

### 4.3 表结构详细设计

#### 4.3.1 ops_dealer_info（经销商信息表）

继承 `TenantBaseDO`。

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|:---:|------|
| `id` | BIGINT | PK | 经销商 ID |
| `name` | VARCHAR(100) | Y | 经销商名称（如：华康医疗器械有限公司） |
| `code` | VARCHAR(50) | Y | 经销商编码（唯一） |
| `contact_name` | VARCHAR(50) | N | 联系人姓名 |
| `contact_phone` | VARCHAR(20) | N | 联系电话 |
| `address` | VARCHAR(200) | N | 地址 |
| `status` | TINYINT | Y | 状态（0=正常, 1=停用） |
| `remark` | VARCHAR(500) | N | 备注 |
| 标准字段 | | | creator, create_time, updater, update_time, deleted, tenant_id |

```sql
CREATE TABLE ops_dealer_info (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '经销商ID',
    name          VARCHAR(100) NOT NULL COMMENT '经销商名称',
    code          VARCHAR(50)  NOT NULL COMMENT '经销商编码',
    contact_name  VARCHAR(50)  DEFAULT NULL COMMENT '联系人',
    contact_phone VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    address       VARCHAR(200) DEFAULT NULL COMMENT '地址',
    status        TINYINT      NOT NULL DEFAULT 0 COMMENT '状态（0=正常, 1=停用）',
    remark        VARCHAR(500) DEFAULT NULL COMMENT '备注',
    creator       VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater       VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       BIT(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    tenant_id     BIGINT       NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='经销商信息表';
```

#### 4.3.2 ops_dealer_product_line（产品线表）

继承 `TenantBaseDO`。

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|:---:|------|
| `id` | BIGINT | PK | 产品线 ID |
| `name` | VARCHAR(100) | Y | 产品线名称（如：骨科、心内科） |
| `code` | VARCHAR(50) | Y | 产品线编码（唯一） |
| `sort` | INT | Y | 排序 |
| `status` | TINYINT | Y | 状态（0=正常, 1=停用） |
| `remark` | VARCHAR(500) | N | 备注 |
| 标准字段 | | | creator, create_time, updater, update_time, deleted, tenant_id |

```sql
CREATE TABLE ops_dealer_product_line (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '产品线ID',
    name        VARCHAR(100) NOT NULL COMMENT '产品线名称',
    code        VARCHAR(50)  NOT NULL COMMENT '产品线编码',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态（0=正常, 1=停用）',
    remark      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    creator     VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater     VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     BIT(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    tenant_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品线表';
```

#### 4.3.3 ops_dealer_product_line_relation（经销商-产品线关联表）

继承 `TenantBaseDO`。一个经销商可销售多个产品线，一个产品线可被多个经销商销售。

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|:---:|------|
| `id` | BIGINT | PK | 主键 |
| `dealer_id` | BIGINT | Y | 经销商 ID（关联 dealer_info.id） |
| `product_line_id` | BIGINT | Y | 产品线 ID（关联 dealer_product_line.id） |
| 标准字段 | | | creator, create_time, updater, update_time, deleted, tenant_id |

```sql
CREATE TABLE ops_dealer_product_line_relation (
    id              BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    dealer_id       BIGINT   NOT NULL COMMENT '经销商ID',
    product_line_id BIGINT   NOT NULL COMMENT '产品线ID',
    creator         VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater         VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    update_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         BIT(1)   NOT NULL DEFAULT b'0' COMMENT '是否删除',
    tenant_id       BIGINT   NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dealer_product (dealer_id, product_line_id),
    KEY idx_dealer_id (dealer_id),
    KEY idx_product_line_id (product_line_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='经销商-产品线关联表';
```

#### 4.3.4 ops_dealer_user_scope（用户-经销商授权表）

继承 `TenantBaseDO`。记录 dealer 角色用户被授权管理的经销商。一个用户可管理多个经销商。

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|:---:|------|
| `id` | BIGINT | PK | 主键 |
| `user_id` | BIGINT | Y | 用户 ID（关联 system_users.id） |
| `dealer_id` | BIGINT | Y | 经销商 ID（关联 dealer_info.id） |
| 标准字段 | | | creator, create_time, updater, update_time, deleted, tenant_id |

```sql
CREATE TABLE ops_dealer_user_scope (
    id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT   NOT NULL COMMENT '用户ID',
    dealer_id   BIGINT   NOT NULL COMMENT '经销商ID',
    creator     VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater     VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     BIT(1)   NOT NULL DEFAULT b'0' COMMENT '是否删除',
    tenant_id   BIGINT   NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_dealer (user_id, dealer_id),
    KEY idx_user_id (user_id),
    KEY idx_dealer_id (dealer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-经销商授权表';
```

#### 4.3.5 ops_executor_product_line_scope（执行员-产品线授权表）

继承 `TenantBaseDO`。记录 service_executor / brand_admin / brand_sales 角色用户被授权负责的产品线。一个用户可负责多个产品线。

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|:---:|------|
| `id` | BIGINT | PK | 主键 |
| `user_id` | BIGINT | Y | 用户 ID（关联 system_users.id） |
| `product_line_id` | BIGINT | Y | 产品线 ID（关联 dealer_product_line.id） |
| 标准字段 | | | creator, create_time, updater, update_time, deleted, tenant_id |

```sql
CREATE TABLE ops_executor_product_line_scope (
    id              BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id         BIGINT   NOT NULL COMMENT '用户ID',
    product_line_id BIGINT   NOT NULL COMMENT '产品线ID',
    creator         VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater         VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    update_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         BIT(1)   NOT NULL DEFAULT b'0' COMMENT '是否删除',
    tenant_id       BIGINT   NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_product_line (user_id, product_line_id),
    KEY idx_user_id (user_id),
    KEY idx_product_line_id (product_line_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='执行员-产品线授权表';
```

### 4.4 system_role 初始数据

```sql
INSERT INTO system_role (name, code, sort, status, type, data_scope, remark, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES
('品牌管理员',     'brand_admin',      10, 0, 1, 1, '业务角色',       'admin', NOW(), 'admin', NOW(), b'0', 1),
('品牌销售员',     'brand_sales',      20, 0, 1, 1, '业务角色-只读',  'admin', NOW(), 'admin', NOW(), b'0', 1),
('服务单执行员',   'service_executor', 30, 0, 1, 1, '业务角色',       'admin', NOW(), 'admin', NOW(), b'0', 1),
('经销商',         'dealer',           40, 0, 1, 1, '业务角色',       'admin', NOW(), 'admin', NOW(), b'0', 1);
```

> `data_scope = 1 (ALL)`：实际的数据过滤由 `DealerDataPermissionRule` 基于扩展表实现，不依赖 `RoleDO.dataScope`。

### 4.5 RoleCodeEnum 枚举扩展

```java
public enum RoleCodeEnum {
    SUPER_ADMIN("super_admin", "超级管理员"),
    TENANT_ADMIN("tenant_admin", "租户管理员"),
    CRM_ADMIN("crm_admin", "CRM 管理员"),
    // --- 经销商 SaaS 业务角色 ---
    BRAND_ADMIN("brand_admin", "品牌管理员"),
    BRAND_SALES("brand_sales", "品牌销售员"),
    SERVICE_EXECUTOR("service_executor", "服务单执行员"),
    DEALER("dealer", "经销商");
}
```

---

## 五、数据权限过滤设计（DealerDataPermissionRule）

### 5.1 核心逻辑

`DealerDataPermissionRule` 实现 `DataPermissionRule` 接口，在 SQL 执行前自动重写 WHERE 条件：

```
1. 获取 LoginUser → 判断角色 (currentRoleCode)
2. 根据角色查询对应的扩展表：
   - dealer 角色 → 查 dealer_user_scope WHERE user_id = ? → 获得 dealer_ids
   - 执行员/管理员/销售员 → 查 executor_product_line_scope WHERE user_id = ? → 获得 product_line_ids
3. 根据表配置生成 WHERE 条件：
   - super_admin → null（不附加条件）
   - brand_admin / brand_sales → 若有 product_line_ids → WHERE product_line_id IN (...)
   - service_executor → WHERE product_line_id IN (...)（无记录则返回空结果）
   - dealer → WHERE dealer_id IN (...)（无记录则返回空结果）
```

### 5.2 扩展表查询策略

为避免每次 SQL 都查扩展表带来的性能问题：

| 策略 | 说明 |
|------|------|
| 登录时预加载 | 用户登录时查询扩展表，将 dealer_ids 和 product_line_ids 缓存到 `LoginUser.info` Map 中 |
| 请求级缓存 | 同一次 HTTP 请求内，通过 `LoginUser.context` 缓存结果，避免重复查询 |
| 修改时清缓存 | 管理员修改用户授权范围时，清除该用户的 LoginUser 缓存 |

---

## 六、后端模块结构

### 6.1 包结构

在 `yudao-module-system` 模块中新增经销商管理相关包：

```
cn.iocoder.yudao.module.system
├── controller/admin/dealer/           — 经销商管理 REST API
│   ├── DealerInfoController.java      — 经销商 CRUD
│   ├── DealerProductLineController.java — 产品线 CRUD
│   ├── DealerUserScopeController.java — 用户-经销商授权管理
│   └── vo/                            — 请求/响应 VO
├── service/dealer/                    — 业务逻辑层
│   ├── DealerInfoService.java
│   ├── DealerProductLineService.java
│   ├── DealerUserScopeService.java
│   └── impl/
├── dal/dataobject/dealer/             — DO 对象
│   ├── DealerInfoDO.java              — extends TenantBaseDO
│   ├── DealerProductLineDO.java       — extends TenantBaseDO
│   ├── DealerProductLineRelationDO.java — extends TenantBaseDO
│   ├── DealerUserScopeDO.java         — extends TenantBaseDO
│   └── ExecutorProductLineScopeDO.java  — extends TenantBaseDO
├── dal/mysql/dealer/                  — Mapper 接口
│   ├── DealerInfoMapper.java          — extends BaseMapperX
│   └── ...
├── convert/dealer/                    — MapStruct 转换器
├── enums/permission/RoleCodeEnum.java — 扩展枚举（已有文件）
└── framework/datapermission/
    ├── config/DataPermissionConfiguration.java — 注册 DealerDataPermissionRule（已有文件，扩展）
    └── rule/DealerDataPermissionRule.java      — 新建数据权限规则
```

### 6.2 核心 API 接口

#### 经销商管理

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/system/dealer/create` | `dealer:mgmt:create` | 新增经销商 |
| PUT | `/system/dealer/update` | `dealer:mgmt:update` | 编辑经销商 |
| DELETE | `/system/dealer/delete?id=` | `dealer:mgmt:delete` | 删除经销商 |
| GET | `/system/dealer/get?id=` | `dealer:mgmt:query` | 获取单个经销商 |
| GET | `/system/dealer/page` | `dealer:mgmt:query` | 分页查询经销商 |
| POST | `/system/dealer/bind-product` | `dealer:mgmt:bindproduct` | 绑定产品线 |
| DELETE | `/system/dealer/unbind-product` | `dealer:mgmt:bindproduct` | 解绑产品线 |
| GET | `/system/dealer/product-lines?dealerId=` | `dealer:mgmt:query` | 查看经销商已绑定产品线 |
| POST | `/system/dealer/bind-user` | `dealer:mgmt:binduser` | 绑定用户 |
| DELETE | `/system/dealer/unbind-user` | `dealer:mgmt:binduser` | 解绑用户 |
| GET | `/system/dealer/users?dealerId=` | `dealer:mgmt:query` | 查看经销商关联用户 |

#### 产品线管理

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/system/product-line/create` | `dealer:productline:create` | 新增产品线 |
| PUT | `/system/product-line/update` | `dealer:productline:update` | 编辑产品线 |
| DELETE | `/system/product-line/delete?id=` | `dealer:productline:delete` | 删除产品线 |
| GET | `/system/product-line/get?id=` | `dealer:productline:query` | 获取单个产品线 |
| GET | `/system/product-line/page` | `dealer:productline:query` | 分页查询产品线 |
| GET | `/system/product-line/simple-list` | `dealer:productline:query` | 产品线简单列表（下拉选用） |

#### 用户授权管理

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/system/dealer-scope/list?userId=` | `system:user:query` | 查看用户授权的经销商 |
| POST | `/system/dealer-scope/assign` | `system:user:update` | 分配用户经销商授权 |
| GET | `/system/product-line-scope/list?userId=` | `system:user:query` | 查看用户授权的产品线 |
| POST | `/system/product-line-scope/assign` | `system:user:update` | 分配用户产品线授权 |

---

## 七、前端页面设计

### 7.1 经销商管理页面

**路径**: `views/system/dealer/index.vue`

**页面结构**:
- 顶部：搜索栏（名称/编码/状态） + 新增按钮
- 主体：经销商列表表格（名称/编码/联系人/电话/状态/已绑定产品线/关联用户/操作）
- 操作列：编辑、删除、管理产品线、管理用户

**弹窗组件**:
- `DealerForm.vue` — 经销商新增/编辑表单
- `DealerProductLineBindForm.vue` — 产品线绑定/解绑（穿梭框或多选下拉）
- `DealerUserBindForm.vue` — 用户绑定/解绑（穿梭框或用户选择器）

### 7.2 产品线管理页面

**路径**: `views/system/productLine/index.vue`

**页面结构**:
- 顶部：搜索栏（名称/编码/状态） + 新增按钮
- 主体：产品线列表表格（名称/编码/排序/状态/操作）
- 操作列：编辑、删除

**弹窗组件**:
- `ProductLineForm.vue` — 产品线新增/编辑表单

### 7.3 用户管理页面扩展

在现有的 `views/system/user/UserForm.vue` 中扩展：
- 当分配的角色包含 `dealer` 时，显示「授权经销商」多选框
- 当分配的角色包含 `service_executor` / `brand_admin` / `brand_sales` 时，显示「授权产品线」多选框

---

## 八、验证方式

| 验证项 | 验证方法 |
|--------|---------|
| 表创建 | 执行 DDL 后确认 5 张表创建成功，`SHOW CREATE TABLE` 检查字段和索引 |
| 角色创建 | 查询 `system_role` 表确认 4 个业务角色存在 |
| 菜单创建 | 前端登录后确认菜单树正确渲染，权限按钮按角色显隐 |
| 经销商 CRUD | 通过 API 或前端页面完成增删改查操作 |
| 产品线 CRUD | 通过 API 或前端页面完成增删改查操作 |
| 关联管理 | 经销商绑定/解绑产品线、绑定/解绑用户 |
| 数据权限 | 分别用 4 种角色登录，验证查询结果的数据范围是否正确过滤 |

---

## 九、与总体 PRD 的关系

```
┌──────────────────────────────────────────────────────────┐
│               经销商管理客服SaaS 分阶段实施                 │
├──────────┬───────────┬───────────┬───────────────────────┤
│  Step 1  │  Step 2   │  Step 3   │  Step 4               │
│  ★ 当前  │           │           │                       │
├──────────┼───────────┼───────────┼───────────────────────┤
│ 角色定义  │ 签约进度   │ 政策看板   │ 注册审核流程           │
│ 菜单权限  │ 订单模块   │ 客户服务   │ 角色切换              │
│ 经销商管理│ 售后模块   │ AI 客服   │ 审计日志              │
│ 产品线管理│ 基础数据   │           │                       │
│ 授权扩展表│           │           │                       │
│ 数据权限  │           │           │                       │
└──────────┴───────────┴───────────┴───────────────────────┘
```

**Step 1 输出物**：
1. ✅ 5 张数据表 + DDL
2. ✅ 4 个业务角色 + 菜单树 + 按钮权限
3. ✅ 经销商管理（CRUD + 绑定产品线 + 绑定用户）
4. ✅ 产品线管理（CRUD）
5. ✅ 用户授权管理（经销商授权 + 产品线授权）
6. ✅ DealerDataPermissionRule 数据权限基础
7. ✅ 前端经销商管理页面 + 产品线管理页面
8. ✅ 用户管理页面扩展（授权范围配置）
