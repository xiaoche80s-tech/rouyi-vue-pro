# 经销商管理客服SaaS平台 — 用户权限 PRD 澄清文档

> **版本**: V1.1 | **日期**: 2026-06-13  
> **文档性质**: 功能设计 PRD 澄清（非代码实现）  
> **输入来源**: PRD V1.0 (`docs/PRD-经销商管理客服SaaS.md`) + PRD V2.0 (`经销商管理客服SaaS_PRD_V2.0.md`)  
> **技术基座**: 芋道 ruoyi-vue-pro (Spring Boot 3 + JDK 17 + Vue3)

---

## 一、需求差异分析

### 1.1 角色定义差异

| 维度 | PRD V1.0 | PRD V2.0 | 合并结论 |
|------|----------|----------|---------|
| 角色数量 | 3 个（管理员/经销商/执行员） | 5 个（超级管理员/品牌管理员/品牌销售员/服务单执行员/经销商） | 采用 V2.0 的 5 角色模型，其中 super_admin 由系统自带，业务角色为 4 个 |
| 品牌销售员 | 不存在 | 新增，仅查看权限，全部经销商数据 | 采纳 V2.0，作为只读角色 |
| 超级管理员 | 未明确提及 | 明确定义：全部功能 + 用户审核 + 身份切换 | 采纳 V2.0，复用系统 super_admin |

### 1.2 授权模型差异

| 维度 | PRD V1.0 | PRD V2.0 | 合并结论 |
|------|----------|----------|---------|
| 数据隔离维度 | 仅"数据范围"概念（全部/本公司/负责范围内），无具体维度定义 | 明确双维度：经销商维度（dealerScope）和产品线维度（productLineScope），交叉模型 | 采纳 V2.0 的双维度模型 |
| 经销商授权 | 未定义授权机制 | 经销商角色按 dealerScope 过滤，执行员/管理员按 productLineScope 过滤 | 采纳 V2.0 |
| 数据范围模型 | 未定义 | 经销商 × 产品线 交集 | 采纳 V2.0 |

### 1.3 功能需求差异

| 功能 | PRD V1.0 | PRD V2.0 | 合并结论 |
|------|----------|----------|---------|
| 注册申请流程 | 无 | 选择身份/填写信息/选择授权范围/提交审核 | **本期不纳入**，后续阶段规划 |
| 超管审核流程 | 无 | 待审核列表/通过拒绝/已注册用户管理/身份切换/审计日志 | **本期不纳入**，后续阶段规划 |
| 角色切换 | 顶部导航栏三角色切换按钮，切换后刷新数据/操作权限/UI 联动 | 三角色切换（经销商/执行员/管理员），切换后数据范围/操作权限/UI 联动 | **本期不纳入**，后续阶段规划 |
| 操作请求 | 无 | 新增操作请求 Tab（签署/付款/开票/退货/盖章），含工作流 | 采纳 V2.0 |
| 咨询队列 | 无独立 Tab | 新增咨询队列 Tab，含状态筛选/类型筛选 | 采纳 V2.0 |
| 客户服务子模块 | 单一工单列表 | 3 个子 Tab：工单列表/咨询队列/操作请求 | 采纳 V2.0 |

### 1.4 UI 联动规则差异

| 规则 | PRD V1.0 | PRD V2.0 | 裁决 |
|------|----------|----------|------|
| 经销商筛选器可见性 | 管理员/执行员可见，经销商隐藏 | 仅管理员可见（签约/售后/订单/基础数据模块） | V2.0 更严格，执行员不可见经销商筛选器。**采纳 V2.0** |
| 表格经销商列 | 管理员显示，经销商/执行员隐藏 | 管理员视角显示，经销商视角隐藏 | 执行员可见（因需处理跨经销商工单）。**采纳 V2.0** |
| 侧边栏用户信息 | 未提及 | 切换后更新底部用户信息展示 | 采纳 V2.0 |
| 导航角标 | 未提及 | 执行员视角显示待处理咨询数 | 采纳 V2.0 |

### 1.5 关键冲突与裁决

| 冲突项 | 裁决 | 理由 |
|--------|------|------|
| 经销商筛选器对执行员是否可见 | **不可见** | V2.0 明确标注"仅管理员可见"，执行员按产品线授权，无需按经销商筛选 |
| 执行员是否显示表格经销商列 | **显示** | 执行员需处理跨经销商的工单/咨询，需看到经销商信息 |
| 品牌销售员是否有操作权限 | **无，仅查看** | V2.0 明确定义品牌销售员为只读角色 |

### 1.6 本期排除项

| 排除功能 | 排除原因 | 规划阶段 |
|---------|---------|---------|
| 用户注册申请流程 | 当前阶段优先实现核心权限体系，用户由管理员后台直接创建 | 后续阶段 |
| 超管审核流程 | 依赖注册申请流程 | 后续阶段 |
| 角色切换功能 | 需要额外的 Token/会话管理机制，复杂度高 | 后续阶段 |
| 超管身份切换 | 属于审核管理子功能 | 后续阶段 |
| 审计日志（审核专用） | 依赖审核和身份切换功能 | 后续阶段 |

---

## 二、角色定义

### 2.1 角色总览

| # | 角色名称 | 角色标识 (code) | 数据范围 | 核心职责 |
|---|---------|----------------|---------|---------|
| 0 | 超级管理员 | `super_admin` | 全部数据，无限制 | 系统配置、用户管理、全局监控（系统内置） |
| 1 | 品牌管理员 | `brand_admin` | 全部经销商数据（可按产品线限定） | 全局监控、数据导入、策略制定、合同下发、工单催办 |
| 2 | 品牌销售员 | `brand_sales` | 全部经销商数据（可按产品线限定） | 仅查看数据，无操作权限 |
| 3 | 服务单执行员 | `service_executor` | 授权产品线内的经销商数据 | 工单处理、咨询回复、操作请求执行 |
| 4 | 经销商 | `dealer` | 仅自身经销商数据 | 查看自有数据、发起操作请求、验收工单、发起咨询 |

### 2.2 各角色详细权限定义

#### 2.2.1 超级管理员 (super_admin)

- **角色标识**: `super_admin`（系统内置，复用芋道 `RoleCodeEnum.SUPER_ADMIN`）
- **菜单权限**: 全部菜单 + 系统管理页面
- **数据权限**: `DataScopeEnum.ALL`，不做任何数据过滤
- **独有功能**: 系统级用户管理、角色管理、菜单管理、字典管理等
- **说明**: 超级管理员是平台级角色，不参与业务模块的角色切换

#### 2.2.2 品牌管理员 (brand_admin)

- **角色标识**: `brand_admin`
- **菜单权限**: 签约进度、政策看板、售后模块、订单模块、基础数据、客户服务（全部 6 个业务模块）
- **数据权限**: 默认查看全部经销商数据，可通过 `productLineScope` 限定产品线范围
- **操作权限**:
  - ✅ 可操作: 数据导入、合同下发、批量操作、工单催办、任务创建、发起咨询
  - ❌ 不可操作: 系统配置、用户管理（super_admin 专属）
- **UI 特征**: 经销商筛选器**可见**、表格经销商列**显示**

#### 2.2.3 品牌销售员 (brand_sales)

- **角色标识**: `brand_sales`
- **菜单权限**: 签约进度、政策看板、售后模块、订单模块、基础数据（5 个只读模块，**不含客户服务**）
- **数据权限**: 默认查看全部经销商数据，可通过 `productLineScope` 限定产品线范围
- **操作权限**: 仅查看，无增删改操作权限
- **UI 特征**: 经销商筛选器**可见**、表格经销商列**显示**，无操作按钮（无批量操作、无申请付款等）

#### 2.2.4 服务单执行员 (service_executor)

- **角色标识**: `service_executor`
- **菜单权限**: 签约进度、政策看板、售后模块、订单模块、基础数据、客户服务（全部 6 个业务模块）
- **数据权限**: 按 `productLineScope` 限定，可见授权产品线内的所有经销商数据
- **操作权限**:
  - ✅ 可操作: 工单接单/转单/提交、咨询回复/完成处理、操作请求接单/提交
  - ❌ 不可操作: 数据导入、合同下发、用户管理
- **UI 特征**: 经销商筛选器**隐藏**、表格经销商列**显示**、导航栏显示待处理咨询角标

#### 2.2.5 经销商 (dealer)

- **角色标识**: `dealer`
- **菜单权限**: 签约进度、政策看板、售后模块、订单模块、基础数据、客户服务（全部 6 个业务模块，但功能受限）
- **数据权限**: 按 `dealerScope` 限定，仅可见自身经销商数据
- **操作权限**:
  - ✅ 可操作: 发起操作请求（签署/付款/开票/退货/盖章）、发起咨询、验收工单、关闭咨询
  - ❌ 不可操作: 工单处理、数据导入、合同下发、工单催办
- **UI 特征**: 经销商筛选器**隐藏**、表格经销商列**隐藏**、显示"去签署""申请付款"等经销商专属按钮

### 2.3 菜单权限矩阵（4 业务角色 × 6 大模块）

| 模块 | 品牌管理员 | 品牌销售员 | 服务单执行员 | 经销商 |
|------|-----------|-----------|-------------|-------|
| 签约进度 | 读写 | 只读 | 读写（咨询回复） | 读写（发起签署/盖章） |
| 政策看板 | 读写 | 只读 | 读写（咨询回复） | 只读 |
| 售后模块 | 读写 | 只读 | 读写（咨询回复） | 读写（发起退货） |
| 订单模块 | 读写 | 只读 | 读写（操作请求） | 读写（申请付款/开票/退货） |
| 基础数据 | 读写 | 只读 | 读写（盖章处理） | 读写（申请盖章） |
| 客户服务 | 读写（催办/创建任务） | **不可见** | 读写（接单/回复/提交） | 读写（发起咨询/验收） |

### 2.4 按钮级权限矩阵

| 权限标识 | 说明 | 品牌管理员 | 品牌销售员 | 服务单执行员 | 经销商 |
|---------|------|:---------:|:---------:|:----------:|:-----:|
| **签约进度** | | | | | |
| `dealer:signing:query` | 查看合同列表 | ✅ | ✅ | ✅ | ✅ |
| `dealer:signing:sign` | 发起签署 | — | — | — | ✅ |
| `dealer:signing:consult` | 发起咨询 | ✅ | — | ✅ | ✅ |
| `dealer:signing:import` | 导入合同 | ✅ | — | — | — |
| `dealer:signing:stamp` | 批量申请盖章 | — | — | — | ✅ |
| `dealer:signing:export` | 导出合同 | ✅ | — | — | — |
| **政策看板** | | | | | |
| `dealer:policy:query` | 查看政策列表 | ✅ | ✅ | ✅ | ✅ |
| `dealer:policy:consult` | 发起政策咨询 | ✅ | — | ✅ | ✅ |
| **售后模块** | | | | | |
| `dealer:aftersale:query` | 查看售后列表 | ✅ | ✅ | ✅ | ✅ |
| `dealer:aftersale:consult` | 发起售后咨询 | ✅ | — | ✅ | ✅ |
| `dealer:aftersale:return` | 发起退货 | — | — | — | ✅ |
| **订单模块** | | | | | |
| `dealer:order:query` | 查看订单列表 | ✅ | ✅ | ✅ | ✅ |
| `dealer:order:pay` | 申请付款 | — | — | — | ✅ |
| `dealer:order:invoice` | 申请开票 | — | — | — | ✅ |
| `dealer:order:return` | 申请退货 | — | — | — | ✅ |
| `dealer:order:consult` | 发起订单咨询 | ✅ | — | ✅ | ✅ |
| **基础数据** | | | | | |
| `dealer:basedata:query` | 查看基础数据 | ✅ | ✅ | ✅ | ✅ |
| `dealer:basedata:ai` | AI 解读 | ✅ | ✅ | ✅ | ✅ |
| `dealer:basedata:download` | 下载文件 | ✅ | ✅ | ✅ | ✅ |
| `dealer:basedata:stamp` | 申请盖章 | — | — | — | ✅ |
| **客户服务 - 工单** | | | | | |
| `dealer:task:query` | 查看工单列表 | ✅ | — | ✅ | ✅ |
| `dealer:task:create` | 创建任务 | ✅ | — | — | — |
| `dealer:task:accept` | 接单 | — | — | ✅ | — |
| `dealer:task:submit` | 提交工单结果 | — | — | ✅ | — |
| `dealer:task:verify` | 验收工单 | — | — | — | ✅ |
| `dealer:task:urge` | 催办 | ✅ | — | — | — |
| **客户服务 - 咨询** | | | | | |
| `dealer:consult:query` | 查看咨询列表 | ✅ | — | ✅ | ✅ |
| `dealer:consult:reply` | 回复咨询 | — | — | ✅ | — |
| `dealer:consult:close` | 关闭咨询 | — | — | — | ✅ |
| `dealer:consult:complete` | 完成处理 | — | — | ✅ | — |
| **客户服务 - 操作请求** | | | | | |
| `dealer:opreq:query` | 查看操作请求 | ✅ | — | ✅ | ✅ |
| `dealer:opreq:create` | 发起操作请求 | — | — | — | ✅ |
| `dealer:opreq:accept` | 接单操作请求 | — | — | ✅ | — |
| `dealer:opreq:submit` | 提交操作请求结果 | — | — | ✅ | — |
| `dealer:opreq:verify` | 验收操作请求 | — | — | — | ✅ |

> **注**: super_admin 拥有全部权限，无需单独列出。

---

## 三、授权模型设计

### 3.1 双维度授权模型

本系统采用 **经销商维度 (dealerScope) × 产品线维度 (productLineScope)** 的交叉授权模型：

```
                 ┌─────────────────────────────────────────┐
                 │          用户可见数据范围                 │
                 │  = dealerScope ∩ productLineScope        │
                 └─────────────────────────────────────────┘
                            │                    │
             ┌──────────────┘                    └──────────────┐
             ▼                                                  ▼
┌──────────────────────────┐                   ┌────────────────────────────┐
│    dealerScope            │                   │    productLineScope         │
│    经销商维度授权           │                   │    产品线维度授权             │
│                           │                   │                             │
│ 适用角色: 经销商           │                   │ 适用角色: 执行员/管理员/销售员  │
│ 值: 经销商 ID 列表         │                   │ 值: 产品线 ID 列表           │
│ 效果: 只能看到指定         │                   │ 效果: 只能看到指定产品线      │
│       经销商的数据         │                   │       内的数据               │
└──────────────────────────┘                   └────────────────────────────┘
```

### 3.2 各角色的授权维度配置

| 角色 | dealerScope | productLineScope | 实际可见数据 |
|------|------------|-----------------|-------------|
| super_admin | 全部（不限定） | 全部（不限定） | 全部数据 |
| brand_admin | 全部（不限定） | 可配置（默认全部） | 全部经销商 × 授权产品线 |
| brand_sales | 全部（不限定） | 可配置（默认全部） | 全部经销商 × 授权产品线 |
| service_executor | 全部（不限定） | **必须配置** | 全部经销商 × 授权产品线 |
| dealer | **必须配置** | 全部（不限定） | 授权经销商 × 全部产品线 |

### 3.3 数据范围交叉模型 — SQL 过滤逻辑

实际查询时，数据过滤条件为：

```sql
WHERE (dealer_id IN ({dealerScope}) OR {dealerScope} IS ALL)
  AND (product_line_id IN ({productLineScope}) OR {productLineScope} IS ALL)
```

各角色的具体过滤规则：

| 角色 | SQL WHERE 条件 |
|------|---------------|
| super_admin | 不附加任何 WHERE 条件 |
| brand_admin / brand_sales | 若 `productLineScope` 非空 → `WHERE product_line_id IN (...)` ；若为空 → 不附加条件 |
| service_executor | `WHERE product_line_id IN (...)` |
| dealer | `WHERE dealer_id IN (...)` |

### 3.4 数据过滤在六大模块中的应用

| 模块 | 主要业务表 | dealer_id 列 | product_line_id 列 | 过滤方式 |
|------|-----------|:-----------:|:-----------------:|---------|
| 签约进度 | `dealer_contract` | `dealer_id` | `product_line_id` | dealerScope + productLineScope |
| 政策看板 | `dealer_policy` | `dealer_id` | `product_line_id` | dealerScope + productLineScope |
| 政策看板 | `dealer_policy_indicator` | `dealer_id` | `product_line_id` | dealerScope + productLineScope |
| 售后模块 | `dealer_after_sale` | `dealer_id` | `product_line_id` | dealerScope + productLineScope |
| 订单模块 | `dealer_order` | `dealer_id` | `product_line_id` | dealerScope + productLineScope |
| 订单模块 | `dealer_order_item` | 通过 order_id 关联 | `product_line_id` | JOIN 过滤 |
| 基础数据 | `dealer_document` | `dealer_id` | `product_line_id`（可为空） | dealerScope + productLineScope |
| 客户服务 - 工单 | `dealer_service_task` | `dealer_id` | `product_line_id` | dealerScope + productLineScope |
| 客户服务 - 咨询 | `dealer_consultation` | `dealer_id` | 无 | 仅 dealerScope |
| 客户服务 - 操作请求 | `dealer_operation_request` | `dealer_id` | `product_line_id` | dealerScope + productLineScope |


---

## 四、与现有系统的映射关系

### 4.1 可直接复用的能力

| 现有能力 | 对应类/组件 | 复用方式 |
|---------|-----------|---------|
| RBAC 角色管理 | `RoleDO` / `RoleService` / `RoleController` | 直接使用，创建 4 个业务角色记录即可 |
| 多租户框架 | `yudao-spring-boot-starter-biz-tenant` / `TenantBaseDO` | 创建一个租户，所有业务用户归属该租户；业务表继承 `TenantBaseDO` |
| 用户-角色关联 | `UserRoleDO` / `PermissionService.assignUserRole()` | 直接使用 |
| 角色-菜单关联 | `RoleMenuDO` / `PermissionService.assignRoleMenu()` | 直接使用 |
| 菜单权限管理 | `MenuDO` / `MenuService` / `MenuController` | 直接使用，需创建业务模块菜单树 |
| 用户管理 CRUD | `AdminUserDO` / `AdminUserService` | 直接使用，需扩展字段 |
| 登录认证 | Spring Security + JWT Token | 直接使用 |
| `@PreAuthorize` 鉴权 | `@ss.hasPermission('xxx')` | 直接使用，在新接口上标注权限 |
| `v-hasPermi` 前端指令 | 前端权限指令 | 直接使用，控制按钮显隐 |
| 字典管理 | 字典类型/数据 | 新增经销商相关字典 |
| 操作日志 | `OperateLogService` | 直接使用，自动记录业务操作 |
| 通知系统 | `NotifyService` | 复用，用于 SLA 超时、催办通知 |
| WebSocket | `yudao-spring-boot-starter-websocket` | 复用，用于聊天/通知推送 |

### 4.2 需要扩展的能力

| 现有能力 | 扩展内容 | 扩展方式 |
|---------|---------|---------|
| `RoleCodeEnum` | 新增 4 个业务角色标识 | 在枚举中添加 `BRAND_ADMIN`, `BRAND_SALES`, `SERVICE_EXECUTOR`, `DEALER` |
| `AdminUserDO` | 新增 `dealerScope` 和 `productLineScope` 字段 | 扩展 DO 类 + DDL 变更 |
| `DataPermissionRule` | 需新增经销商/产品线维度的数据过滤规则 | 新建 `DealerDataPermissionRule` 实现 `DataPermissionRule` 接口 |
| `PermissionService` | 需新增获取经销商/产品线数据权限的方法 | 扩展接口 + 实现 |
| `LoginUser` | 需携带 dealerScope/productLineScope 信息 | 扩展 info Map 或新增字段 |
| 前端用户管理 | 需新增授权范围配置 UI | 扩展 UserForm.vue 组件 |
| 前端角色管理 | 需新增产品线授权 UI | 扩展 RoleDataPermissionForm.vue |

### 4.3 需要新建的能力

| 新建功能 | 说明 | 涉及层次 |
|---------|------|---------|
| 经销商基础数据表 | `dealer_info` 表，存储经销商基本信息 | DDL + DO + Mapper + Service + Controller + 前端页面 |
| 产品线基础数据表 | `dealer_product_line` 表 | DDL + DO + Mapper + Service + Controller + 前端页面 |
| `DealerDataPermissionRule` | 基于经销商/产品线的 SQL 重写规则 | Framework 层新建 |
| 六大业务模块 | 签约/政策/售后/订单/基础数据/客户服务 | 完整的后端模块 + 前端页面 |

### 4.4 明确排除的部分

| 现有能力 | 排除原因 |
|---------|---------|
| `DeptDataPermissionRule` 部门数据权限（用于业务模块） | 业务数据隔离维度是经销商和产品线，非部门。部门数据权限仅用于系统管理场景 |

---

## 五、功能需求清单

### 5.1 数据权限过滤规则 — DealerDataPermissionRule

#### 5.1.1 设计目标

新建 `DealerDataPermissionRule` 实现 `DataPermissionRule` 接口，基于经销商和产品线两个维度自动重写 SQL WHERE 条件。

#### 5.1.2 核心逻辑

```
1. 获取当前登录用户的 LoginUser
2. 从 LoginUser 上下文获取 currentRoleCode、dealerScope、productLineScope
3. 根据表名获取表中的 dealer_id 列和 product_line_id 列配置
4. 按角色生成 SQL WHERE 条件：
   - super_admin: 返回 null（不附加条件）
   - brand_admin/brand_sales:
     若 productLineScope 非空 → WHERE product_line_id IN (...)
     若 productLineScope 为空 → 返回 null
   - service_executor:
     WHERE product_line_id IN (...)
   - dealer:
     WHERE dealer_id IN (...)
```

#### 5.1.3 与 DeptDataPermissionRule 共存策略

| 场景 | 启用的规则 | 控制方式 |
|------|-----------|---------|
| 系统管理模块（用户/角色/部门等） | DeptDataPermissionRule | `@DataPermission(includeRules = DeptDataPermissionRule.class)` |
| 经销商业务模块（签约/政策/售后/订单等） | DealerDataPermissionRule | `@DataPermission(includeRules = DealerDataPermissionRule.class)` |
| 无需数据权限 | 无 | `@DataPermission(enable = false)` |

#### 5.1.4 各模块表的过滤配置

| 表名 | dealer_id 列 | product_line_id 列 | 说明 |
|------|:-----------:|:-----------------:|------|
| `dealer_contract` | `dealer_id` | `product_line_id` | 签约进度 |
| `dealer_policy` | `dealer_id` | `product_line_id` | 政策看板 |
| `dealer_policy_indicator` | `dealer_id` | `product_line_id` | 政策指标明细 |
| `dealer_after_sale` | `dealer_id` | `product_line_id` | 售后模块 |
| `dealer_order` | `dealer_id` | `product_line_id` | 订单模块 |
| `dealer_order_item` | 通过 order_id 关联 | `product_line_id` | 订单明细（JOIN 过滤） |
| `dealer_document` | `dealer_id` | `product_line_id`（可为空） | 基础数据 |
| `dealer_service_task` | `dealer_id` | `product_line_id` | 工单 |
| `dealer_consultation` | `dealer_id` | 无 | 咨询（仅经销商维度） |
| `dealer_operation_request` | `dealer_id` | `product_line_id` | 操作请求 |

### 5.2 菜单树设计

以下为 6 大业务模块的菜单树设计，每个模块包含目录、菜单、按钮三级：

```
dealer（经销商管理 SaaS）                    一级目录
├── signing（签约进度）                        二级菜单
│   ├── dealer:signing:query                  按钮 - 查看
│   ├── dealer:signing:sign                   按钮 - 发起签署（经销商）
│   ├── dealer:signing:consult                按钮 - 咨询
│   ├── dealer:signing:import                 按钮 - 导入（管理员）
│   ├── dealer:signing:stamp                  按钮 - 申请盖章（经销商）
│   └── dealer:signing:export                 按钮 - 导出（管理员）
├── policy（政策看板）                         二级菜单
│   ├── dealer:policy:query                   按钮 - 查看
│   └── dealer:policy:consult                 按钮 - 咨询
├── aftersale（售后模块）                      二级菜单
│   ├── dealer:aftersale:query                按钮 - 查看
│   ├── dealer:aftersale:consult              按钮 - 咨询
│   └── dealer:aftersale:return               按钮 - 发起退货（经销商）
├── order（订单模块）                          二级菜单
│   ├── dealer:order:query                    按钮 - 查看
│   ├── dealer:order:pay                      按钮 - 申请付款（经销商）
│   ├── dealer:order:invoice                  按钮 - 申请开票（经销商）
│   ├── dealer:order:return                   按钮 - 申请退货（经销商）
│   └── dealer:order:consult                  按钮 - 咨询
├── basedata（基础数据）                       二级菜单
│   ├── dealer:basedata:query                 按钮 - 查看
│   ├── dealer:basedata:ai                    按钮 - AI 解读
│   ├── dealer:basedata:download              按钮 - 下载
│   └── dealer:basedata:stamp                 按钮 - 申请盖章（经销商）
└── service（客户服务）                        二级菜单
    ├── service:task:query                    按钮 - 查看工单
    ├── service:task:create                   按钮 - 创建任务（管理员）
    ├── service:task:accept                   按钮 - 接单（执行员）
    ├── service:task:submit                   按钮 - 提交结果（执行员）
    ├── service:task:verify                   按钮 - 验收（经销商）
    ├── service:task:urge                     按钮 - 催办（管理员）
    ├── service:consult:query                 按钮 - 查看咨询
    ├── service:consult:reply                 按钮 - 回复（执行员）
    ├── service:consult:close                 按钮 - 关闭（经销商）
    ├── service:consult:complete              按钮 - 完成处理（执行员）
    ├── service:opreq:query                   按钮 - 查看操作请求
    ├── service:opreq:create                  按钮 - 发起请求（经销商）
    ├── service:opreq:accept                  按钮 - 接单（执行员）
    ├── service:opreq:submit                  按钮 - 提交结果（执行员）
    └── service:opreq:verify                  按钮 - 验收（经销商）
```

### 5.3 本期不包含的功能

以下功能已明确排除在本期范围之外，将在后续阶段规划：

| 功能 | 说明 | 依赖关系 |
|------|------|---------|
| 用户注册申请流程 | 选择身份类型 → 填写基本信息 → 选择授权范围 → 提交审核 → 等待超管审核 | 无前置依赖 |
| 超管审核流程 | 待审核列表 → 通过/拒绝 → 自动创建用户 → 已注册用户管理 | 依赖注册申请 |
| 角色切换功能 | 顶部导航栏三按钮切换 → 数据范围刷新 → 操作权限刷新 → UI 联动 | 需要 Token/会话管理扩展 |
| 超管身份切换 | 模拟任意已注册用户视角浏览系统 | 依赖审核管理 |
| 审计日志（审核专用） | 记录登录/退出/审核/切换身份/修改授权等操作 | 依赖审核和身份切换 |

> **本期用户创建方式**：由 super_admin 通过现有的系统管理 → 用户管理页面直接创建用户并分配角色和授权范围。

---

## 六、数据模型变更

### 6.1 需要修改的现有表

#### 6.1.1 system_users 表（AdminUserDO）

新增字段：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `dealer_scope` | VARCHAR(500) | 经销商授权范围，JSON 数组格式，如 `[1,2,3]`（经销商 ID 列表）。仅 dealer 角色使用 |
| `product_line_scope` | VARCHAR(500) | 产品线授权范围，JSON 数组格式，如 `[1,2]`（产品线 ID 列表）。brand_admin/brand_sales/service_executor 使用 |

**DDL 变更**:

```sql
ALTER TABLE system_users ADD COLUMN dealer_scope VARCHAR(500) DEFAULT NULL COMMENT '经销商授权范围（JSON数组，经销商ID列表）';
ALTER TABLE system_users ADD COLUMN product_line_scope VARCHAR(500) DEFAULT NULL COMMENT '产品线授权范围（JSON数组，产品线ID列表）';
```

> **设计决策**: 将 dealerScope/productLineScope 放在 user 表而非 role 表，原因是同一角色下不同用户的授权范围不同（如不同经销商看到的数据不同）。这与现有 `dataScopeDeptIds` 放在 role 表上的设计不同，因为部门权限是按角色统一分配的，而经销商/产品线权限是按用户个体分配的。

### 6.2 RoleCodeEnum 枚举扩展

新增枚举值：

| 枚举值 | code | 说明 |
|--------|------|------|
| `BRAND_ADMIN` | `brand_admin` | 品牌管理员 |
| `BRAND_SALES` | `brand_sales` | 品牌销售员 |
| `SERVICE_EXECUTOR` | `service_executor` | 服务单执行员 |
| `DEALER` | `dealer` | 经销商 |

### 6.3 需要新增的表

#### 6.3.1 dealer_info（经销商信息表）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|:---:|------|
| `id` | BIGINT | PK | 经销商 ID |
| `name` | VARCHAR(100) | Y | 经销商名称 |
| `code` | VARCHAR(50) | Y | 经销商编码 |
| `contact_name` | VARCHAR(50) | N | 联系人 |
| `contact_phone` | VARCHAR(20) | N | 联系电话 |
| `address` | VARCHAR(200) | N | 地址 |
| `status` | TINYINT | Y | 状态（0=正常, 1=停用） |
| `remark` | VARCHAR(500) | N | 备注 |
| `creator` | VARCHAR(64) | N | 创建者（BaseDO） |
| `create_time` | DATETIME | Y | 创建时间（BaseDO） |
| `updater` | VARCHAR(64) | N | 更新者（BaseDO） |
| `update_time` | DATETIME | Y | 更新时间（BaseDO） |
| `deleted` | BIT(1) | Y | 逻辑删除（BaseDO） |
| `tenant_id` | BIGINT | Y | 租户编号（TenantBaseDO） |

**DDL**:

```sql
CREATE TABLE dealer_info (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '经销商ID',
    name        VARCHAR(100) NOT NULL COMMENT '经销商名称',
    code        VARCHAR(50)  NOT NULL COMMENT '经销商编码',
    contact_name VARCHAR(50) DEFAULT NULL COMMENT '联系人',
    contact_phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    address     VARCHAR(200) DEFAULT NULL COMMENT '地址',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='经销商信息表';
```

#### 6.3.2 dealer_product_line（产品线表）

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|:---:|------|
| `id` | BIGINT | PK | 产品线 ID |
| `name` | VARCHAR(100) | Y | 产品线名称（如：骨科、心内科、外科、神经外科） |
| `code` | VARCHAR(50) | Y | 产品线编码 |
| `sort` | INT | Y | 排序 |
| `status` | TINYINT | Y | 状态 |
| `remark` | VARCHAR(500) | N | 备注 |
| `creator` / `create_time` / `updater` / `update_time` / `deleted` / `tenant_id` | 标准字段 | | |

**DDL**:

```sql
CREATE TABLE dealer_product_line (
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

### 6.4 六大业务模块核心表结构概要

> 所有业务表均继承 `TenantBaseDO`，包含 `tenant_id` 字段，由框架自动注入租户过滤。业务表中的 `creator/create_time/updater/update_time/deleted` 来自 `BaseDO`，`tenant_id` 来自 `TenantBaseDO`。

#### 6.4.1 dealer_contract（合同表）— 签约进度

| 核心字段 | 类型 | 说明 |
|---------|------|------|
| id | BIGINT | 主键 |
| dealer_id | BIGINT | 经销商 ID（关联 dealer_info.id） |
| product_line_id | BIGINT | 产品线 ID（关联 dealer_product_line.id） |
| contract_type | VARCHAR(20) | 合同类型（main/policy/supplement/termination） |
| contract_code | VARCHAR(30) | 合同编码（MC-/POL-/SA-/TA- 前缀） |
| contract_name | VARCHAR(200) | 合同名称 |
| status | VARCHAR(20) | 状态（signed/unsigned） |
| issued_date | DATE | 下发日期 |
| sign_date | DATE | 签署日期 |
| summary | TEXT | 合同摘要 |
| policy_analysis | TEXT | 政策解析（仅政策合同） |
| files | JSON | 附件列表 |

#### 6.4.2 dealer_policy（政策表）+ dealer_policy_indicator（政策指标表）— 政策看板

**dealer_policy**:

| 核心字段 | 类型 | 说明 |
|---------|------|------|
| id | BIGINT | 主键 |
| dealer_id | BIGINT | 经销商 ID |
| product_line_id | BIGINT | 产品线 ID |
| policy_code | VARCHAR(30) | 政策编码 |
| policy_name | VARCHAR(200) | 政策名称 |
| policy_type | VARCHAR(20) | 政策类型（rebate/promotion/other） |
| quarter | INT | 季度 1-4 |
| month | INT | 月份 1-12 |
| status | VARCHAR(20) | 状态（executing/pending/completed） |
| description | TEXT | 政策描述 |

**dealer_policy_indicator**:

| 核心字段 | 类型 | 说明 |
|---------|------|------|
| id | BIGINT | 主键 |
| policy_id | BIGINT | 关联政策 ID |
| indicator_name | VARCHAR(100) | 指标名称 |
| target_value | DECIMAL(12,2) | 目标值 |
| achieved_value | DECIMAL(12,2) | 达成值 |
| unit | VARCHAR(20) | 单位 |

#### 6.4.3 dealer_after_sale（售后单表）— 售后模块

| 核心字段 | 类型 | 说明 |
|---------|------|------|
| id | BIGINT | 主键 |
| dealer_id | BIGINT | 经销商 ID |
| product_line_id | BIGINT | 产品线 ID |
| so_code | VARCHAR(30) | 售后单号 |
| order_code | VARCHAR(30) | 关联订单号 |
| type | VARCHAR(50) | 售后类型（9 种组合） |
| product_name | VARCHAR(200) | 产品名称 |
| status | VARCHAR(20) | 状态（completed/in_progress/exchanging/pending） |
| progress_nodes | JSON | 进度节点列表 |

#### 6.4.4 dealer_order（订单表）+ dealer_order_item（订单明细表）— 订单模块

**dealer_order**:

| 核心字段 | 类型 | 说明 |
|---------|------|------|
| id | BIGINT | 主键 |
| dealer_id | BIGINT | 经销商 ID |
| product_line_id | BIGINT | 产品线 ID |
| order_code | VARCHAR(30) | 订单号 |
| total_amount | DECIMAL(12,2) | 订单总额 |
| paid_amount | DECIMAL(12,2) | 已付金额 |
| status | VARCHAR(20) | 进度（pending/confirmed/shipped/signed/completed） |
| pay_status | VARCHAR(20) | 付款状态（unpaid/paid） |
| inv_status | VARCHAR(20) | 开票状态（uninvoiced/invoiced/partial） |
| timeline | JSON | 时间线节点 |

**dealer_order_item**:

| 核心字段 | 类型 | 说明 |
|---------|------|------|
| id | BIGINT | 主键 |
| order_id | BIGINT | 关联订单 ID |
| product_name | VARCHAR(200) | 产品名称 |
| spec | VARCHAR(100) | 规格型号 |
| unit_price | DECIMAL(12,2) | 单价 |
| quantity | INT | 数量 |
| amount | DECIMAL(12,2) | 金额 |

#### 6.4.5 dealer_document（基础数据/文件表）— 基础数据

| 核心字段 | 类型 | 说明 |
|---------|------|------|
| id | BIGINT | 主键 |
| dealer_id | BIGINT | 经销商 ID |
| product_line_id | BIGINT | 产品线 ID（可为空） |
| category | VARCHAR(30) | 文件分类（qualification/authorization/contract/product） |
| file_name | VARCHAR(200) | 文件名称 |
| file_type | VARCHAR(50) | 文件类型 |
| file_no | VARCHAR(30) | 文件编号 |
| file_url | VARCHAR(500) | 文件地址 |
| expire_date | DATE | 有效期至 |
| status | VARCHAR(20) | 状态（valid/expiring/expired） |

#### 6.4.6 dealer_service_task（工单表）— 客户服务

| 核心字段 | 类型 | 说明 |
|---------|------|------|
| id | BIGINT | 主键 |
| dealer_id | BIGINT | 经销商 ID |
| product_line_id | BIGINT | 产品线 ID |
| task_code | VARCHAR(30) | 任务编号 |
| content | TEXT | 任务内容 |
| urgency | VARCHAR(20) | 紧急程度（urgent/normal/special） |
| sla_hours | INT | SLA 小时数 |
| status | VARCHAR(20) | 状态（assigned/in_progress/pending_verify/completed） |
| handler_id | BIGINT | 处理人 ID |
| source_module | VARCHAR(30) | 来源模块 |
| proof | JSON | 凭证附件 |

#### 6.4.7 dealer_consultation（咨询表）— 客户服务

| 核心字段 | 类型 | 说明 |
|---------|------|------|
| id | BIGINT | 主键 |
| dealer_id | BIGINT | 经销商 ID |
| consultation_type | VARCHAR(30) | 咨询类型 |
| module | VARCHAR(30) | 关联模块 |
| context | VARCHAR(200) | 咨询上下文 |
| status | VARCHAR(20) | 状态（pending/in_progress/completed/closed） |
| messages | JSON | 消息列表 |
| solution_attachments | JSON | 解决方案附件 |

#### 6.4.8 dealer_operation_request（操作请求表）— 客户服务

| 核心字段 | 类型 | 说明 |
|---------|------|------|
| id | BIGINT | 主键 |
| dealer_id | BIGINT | 经销商 ID |
| product_line_id | BIGINT | 产品线 ID |
| request_type | VARCHAR(20) | 请求类型（sign/payment/invoice/return/stamp） |
| module | VARCHAR(30) | 来源模块 |
| context | VARCHAR(200) | 关联内容 |
| status | VARCHAR(20) | 状态（pending/in_progress/pending_verify/completed） |
| handler_id | BIGINT | 处理人 ID |
| proof | JSON | 凭证附件 |

### 6.5 system_role 初始数据

| name | code | data_scope | status | type | remark |
|------|------|-----------|--------|------|--------|
| 品牌管理员 | brand_admin | 1 (ALL) | 0 (正常) | 1 (自定义) | 业务角色 |
| 品牌销售员 | brand_sales | 1 (ALL) | 0 (正常) | 1 (自定义) | 业务角色-只读 |
| 服务单执行员 | service_executor | 1 (ALL) | 0 (正常) | 1 (自定义) | 业务角色 |
| 经销商 | dealer | 1 (ALL) | 0 (正常) | 1 (自定义) | 业务角色 |

> **注**: `data_scope` 设为 ALL，实际的数据过滤由 `DealerDataPermissionRule` 基于用户的 `dealer_scope` / `product_line_scope` 字段实现，不依赖 `RoleDO.dataScope`。


---

## 七、分阶段实施建议

### 7.1 第一期：核心权限 + 基础业务

| 功能 | 优先级 | 说明 |
|------|:------:|------|
| 经销商基础数据表 (dealer_info, dealer_product_line) | P0 | 其他所有功能的前置依赖 |
| system_users 表扩展 (dealer_scope, product_line_scope) | P0 | 授权模型的数据基础 |
| RoleCodeEnum 扩展 + system_role 初始数据 | P0 | 4 个业务角色创建 |
| DealerDataPermissionRule 实现 | P0 | 核心数据隔离机制 |
| LoginUser 扩展携带 dealerScope/productLineScope | P0 | 数据权限判断依赖 |
| 6 大业务模块菜单树 + 按钮权限创建 | P0 | 前端权限控制依赖 |
| 前端用户管理扩展（授权范围配置 UI） | P1 | 管理员创建用户时配置授权范围 |
| 签约进度模块（后端 + 前端） | P1 | 核心业务模块 |
| 订单模块（后端 + 前端） | P1 | 核心业务模块 |
| 售后模块（后端 + 前端） | P1 | 核心业务模块 |
| 基础数据模块（后端 + 前端） | P1 | 核心业务模块 |

### 7.2 第二期：高级功能 + 完善体验

| 功能 | 优先级 | 说明 |
|------|:------:|------|
| 政策看板模块（含 KPI 仪表盘 + 三级钻取） | P1 | 复杂度较高，含图表和钻取交互 |
| 客户服务模块（工单 + 咨询队列 + 操作请求 3 个 Tab） | P1 | 含多角色工作流 |
| AI 智能客服聊天窗口（WebSocket + AI 回复模拟） | P1 | 跨模块核心能力 |
| SLA 超时预警 + 催办通知 + WebSocket 推送 | P2 | 工单配套功能 |
| 导航角标（咨询数量） + 行高亮 + 咨询红点 | P2 | 体验增强 |

### 7.3 第三期：集成与优化

| 功能 | 说明 |
|------|------|
| 电子签章集成 | 对接法大大/上上签/e签宝 |
| 合同 AI 解读 | NLP 条款提取（政策合同） |
| 文件上传 | 基础数据模块补充上传入口 |
| 数据导入导出 | Excel 批量导入合同/订单/政策数据 |
| 性能优化 | 大数据量下的分页查询优化、缓存策略 |

### 7.4 后续阶段（视业务需要再规划）

| 功能 | 说明 |
|------|------|
| 用户注册申请流程 | 选择身份 → 填写信息 → 选择授权范围 → 提交审核 |
| 超管审核流程 | 待审核列表 → 通过/拒绝 → 用户自动创建 |
| 角色切换功能 | 顶部导航栏三角色切换 + 数据/权限/UI 联动 |
| 超管身份切换 | 模拟任意已注册用户视角 |
| 审计日志（审核专用） | 记录审核/切换身份等操作 |

---

## 八、风险与注意事项

| 风险项 | 说明 | 缓解措施 |
|--------|------|---------|
| SQL 注入 | DealerDataPermissionRule 拼接 SQL 时需注意防注入 | dealerScope/productLineScope 均为 ID 列表（Long），通过 JSqlParser 的 LongValue 构造，不接受字符串输入 |
| 双数据权限规则共存 | DeptDataPermissionRule 与 DealerDataPermissionRule 同时存在可能冲突 | 通过 `@DataPermission(includeRules/excludeRules)` 注解按方法精确控制启用哪个规则。业务模块使用 DealerDataPermissionRule，系统管理模块使用 DeptDataPermissionRule |
| 单租户 tenant_id 兼容性 | 所有业务表继承 `TenantBaseDO`，含 `tenant_id` 字段 | 系统创建一个租户（id 固定），所有用户归属该租户。框架自动注入 tenant_id 过滤，业务代码无需关心 |
| LoginUser 缓存一致性 | 用户授权范围修改后，LoginUser 中的缓存需刷新 | 修改授权范围后清除相关缓存，下次请求重新加载 |
| 前端权限刷新 | 用户角色/权限变更后，前端需重新拉取权限信息 | 变更后强制刷新 Pinia Store 和菜单缓存 |
| 产品线为空时的处理 | service_executor 未配置 productLineScope 时不应看到任何数据 | 当 service_executor 的 productLineScope 为空时，DealerDataPermissionRule 返回空结果条件 |

---

## 九、关键文件索引

### 9.1 需求文档

| 文件 | 说明 |
|------|------|
| `docs/PRD-经销商管理客服SaaS.md` | PRD V1.0 |
| `经销商管理客服SaaS_PRD_V2.0.md` | PRD V2.0 |
| `docs/PRD-用户权限设计.md` | 本文档（用户权限 PRD 澄清） |

### 9.2 后端核心代码

| 文件路径 | 说明 |
|---------|------|
| `yudao-module-system/.../dal/dataobject/permission/RoleDO.java` | 角色实体 |
| `yudao-module-system/.../dal/dataobject/user/AdminUserDO.java` | 用户实体 |
| `yudao-module-system/.../enums/permission/RoleCodeEnum.java` | 角色标识枚举（需扩展） |
| `yudao-module-system/.../enums/permission/DataScopeEnum.java` | 数据范围枚举 |
| `yudao-module-system/.../service/permission/PermissionServiceImpl.java` | 权限服务实现 |
| `yudao-module-system/.../service/permission/RoleServiceImpl.java` | 角色服务实现 |
| `yudao-module-system/.../controller/admin/permission/RoleController.java` | 角色 API 控制器 |
| `yudao-module-system/.../controller/admin/permission/PermissionController.java` | 权限 API 控制器 |
| `yudao-module-system/.../framework/datapermission/config/DataPermissionConfiguration.java` | 现有数据权限配置（参考） |

### 9.3 框架层代码

| 文件路径 | 说明 |
|---------|------|
| `yudao-framework/.../datapermission/core/rule/DataPermissionRule.java` | 数据权限规则接口（需实现 DealerDataPermissionRule） |
| `yudao-framework/.../datapermission/core/rule/dept/DeptDataPermissionRule.java` | 部门数据权限规则（参考实现） |
| `yudao-framework/.../datapermission/core/rule/dept/DeptDataPermissionRuleCustomizer.java` | 自定义配置接口（参考） |
| `yudao-framework/.../datapermission/config/YudaoDeptDataPermissionAutoConfiguration.java` | 自动配置（参考） |
| `yudao-framework/.../security/core/LoginUser.java` | 登录用户对象（需扩展） |

### 9.4 前端代码

| 文件路径 | 说明 |
|---------|------|
| `yudao-ui/yudao-ui-admin-vue3/src/views/system/user/` | 用户管理页面（需扩展授权范围 UI） |
| `yudao-ui/yudao-ui-admin-vue3/src/views/system/role/` | 角色管理页面 |
| `yudao-ui/yudao-ui-admin-vue3/src/views/system/menu/` | 菜单管理页面 |
| `yudao-ui/yudao-ui-admin-vue3/src/api/system/permission/` | 权限 API |
| `yudao-ui/yudao-ui-admin-vue3/src/api/system/role/` | 角色 API |
| `yudao-ui/yudao-ui-admin-vue3/src/api/system/user/` | 用户 API |

### 9.5 数据库脚本

| 文件路径 | 说明 |
|---------|------|
| `sql/mysql/ruoyi-vue-pro.sql` | 主库初始化脚本（system_users / system_role 等表） |

---

> **文档版本记录**
>
> | 版本 | 日期 | 内容 |
> |------|------|------|
> | V1.0 | 2026-06-13 | 初版：角色定义 + 授权模型 + 数据模型 + 实施建议（不含注册审核和角色切换） |
> | V1.1 | 2026-06-13 | 修正租户模型：使用多租户框架，创建一个租户，业务表继承 TenantBaseDO |
