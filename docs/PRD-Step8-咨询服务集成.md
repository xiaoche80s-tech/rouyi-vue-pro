# Step 8 — 咨询服务集成实施 PRD

> **版本**: V1.0 | **日期**: 2026-06-17  
> **文档性质**: 分阶段实施 PRD — Step 8（咨询服务集成）  
> **前置文档**: `docs/PRD-Step7-电商客服系统.md`（Step 7）  
> **依赖模块**: 签约（Step 3）、订单（Step 4）、售后（Step 5）、基础数据（Step 2）、全局布局

---

## 一、Step 8 目标

在 Step 7 电商客服系统（在线聊天）基础上，将各业务模块的**咨询入口**统一接入 ChatWindow 会话 Drawer，实现经销商在各模块点击「💬 咨询」按钮时，自动创建或复用已有咨询会话，并在 Drawer 内与执行员实时沟通。

| 目标 | 说明 |
|------|------|
| 共享 composable | `useCsConsult.ts` 封装「创建会话 → 打开 Drawer」逻辑，各模块复用 |
| 签约模块 | 行操作列「咨询」+ 批量「批量咨询」按钮接入客服系统 |
| 订单模块 | 行操作列「咨询」+ 批量「批量咨询」按钮接入客服系统 |
| 售后模块 | 行操作列「💬 客服」+ 批量「批量咨询」按钮接入客服系统 |
| 基础数据模块 | 新增行操作列「💬 咨询」+ 工具栏「批量咨询」按钮 |
| 全局浮动按钮 | Layout.vue 挂载 ChatFloatingButton，任意页面均可发起咨询 |
| 权限补录 | 新增 `dealer:basedata:consult`（menu ID 6074）按钮权限 |
| 会话去重 | 复用 Step 7 后端去重逻辑：相同 `consultType + contextCode + 活跃状态` 返回已有会话 |

**本阶段不包含**：客户服务模块 6 大分类入口卡片（后续 Step）、「去签署」功能（Step 10）。

---

## 二、共享 Composable — useCsConsult

### 2.1 文件位置

```
src/hooks/useCsConsult.ts
```

### 2.2 接口定义

```ts
export interface ConsultParams {
  consultType: string   // signing | policy | aftersale | order | basedata | other
  sourceModule: string  // signing | order | aftersale | basedata | manual
  context?: string      // 例：'合同 MC-2026-001 签署流程咨询'
  contextId?: number    // 业务记录 ID
  contextCode?: string  // 合同号 / 订单号 / 售后单号 / 文件编号
  productLineCode?: string
  productLineName?: string
  dealerCode?: string
  dealerName?: string
}
```

### 2.3 返回值

```ts
{
  chatVisible: Ref<boolean>          // ChatWindow Drawer 显示/隐藏
  currentSessionId: Ref<number|undefined>  // 当前打开的会话 ID
  openConsult: (params: ConsultParams) => Promise<void>  // 单条咨询
  openBatchConsult: <T>(rows: T[], builder: (row: T) => ConsultParams) => Promise<void>  // 批量咨询
  openByCategory: (consultType?: string) => Promise<void>  // 浮动按钮：无上下文 other 类型
}
```

### 2.4 核心逻辑

```
1. 调用 createSession(params) → 后端返回 sessionId（已有活跃会话则复用）
2. 设置 currentSessionId = sessionId
3. 设置 chatVisible = true → 打开 ChatWindow Drawer
4. 批量咨询使用 Promise.all 并行创建，完成后打开第一个会话
```

### 2.5 批量上限

单次最多 **20 条**，超出自动截取并提示用户。

---

## 三、模块集成详细设计

### 3.1 签约模块（SigningContract）

**文件**：`views/opshub/signing/index.vue`、`views/opshub/signing/components/ContractTable.vue`

| 入口 | 触发方式 | consultType | contextCode | 携带字段 |
|------|----------|-------------|-------------|----------|
| 行操作「咨询」 | `ContractTable @consult` | `signing` | `contractCode` | dealerCode, dealerName, productLineCode, productLineName |
| 工具栏「批量咨询」 | `handleBatchConsult()` | `signing` | `contractCode` | 同上 |

**权限**：`dealer:signing:consult`（menu ID 6032，Step 1 已配置）

### 3.2 订单模块（Order）

**文件**：`views/opshub/order/index.vue`

| 入口 | 触发方式 | consultType | contextCode | 携带字段 |
|------|----------|-------------|-------------|----------|
| 行操作「咨询」 | `OrderTable @consult` | `order` | `orderCode` | dealerCode, dealerName, productLineCode, productLineName |
| 工具栏「批量咨询」 | `handleBatchConsult()` | `order` | `orderCode` | 同上 |

**权限**：`dealer:order:consult`（menu ID 6064，Step 1 已配置）

### 3.3 售后模块（AfterSale）

**文件**：`views/opshub/aftersale/index.vue`

| 入口 | 触发方式 | consultType | contextCode | 携带字段 |
|------|----------|-------------|-------------|----------|
| 行操作「💬 客服」 | `handleConsult(row)` | `aftersale` | `aftersaleCode` | dealerName, productLineName（VO 无 Code 字段） |
| 工具栏「批量咨询」 | `handleBatchConsult()` | `aftersale` | `aftersaleCode` | 同上 |

**权限**：`dealer:aftersale:consult`（menu ID 6051，Step 1 已配置）

### 3.4 基础数据模块（BaseData）

**文件**：`views/opshub/basedata/index.vue`

| 入口 | 触发方式 | consultType | contextCode | 携带字段 |
|------|----------|-------------|-------------|----------|
| 行操作「💬 咨询」（新增） | `handleConsult(row)` | `basedata` | `fileNo` | dealerCode, dealerName |
| 工具栏「批量咨询」（新增） | `handleBatchConsult()` | `basedata` | `fileNo` | 同上 |

**权限**：`dealer:basedata:consult`（menu ID **6074**，Step 8 新增）

**变更点**：
- 操作列新增「💬 咨询」按钮（第 5 个按钮）
- 工具栏新增「批量咨询」按钮
- `handleSelectionChange` 同时维护 `selectedRows` 和 `selectedIds`

### 3.5 全局浮动按钮

**文件**：`layout/Layout.vue`

| 入口 | 触发方式 | consultType | sourceModule |
|------|----------|-------------|--------------|
| 右下角 💬 浮动按钮 | `handleFloatingClick()` | `other` | `manual` |

**实现**：
- 在 Layout TSX render 函数中挂载 `ChatFloatingButton` + `ChatWindow`
- 调用 `useCsConsult().openByCategory('other')` 创建无上下文的通用咨询
- 未读数（`unreadCount`）当前占位为 0，后续通过 WebSocket 推送接入

---

## 四、权限 DML

### 4.1 新增菜单

```sql
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, ...) VALUES
(6074, '基础数据咨询', 'dealer:basedata:consult', 3, 5, 6007, ...);
```

### 4.2 角色授权

| 角色 | role_id | 是否授权 6074 |
|------|---------|--------------|
| 品牌管理员 | 157 | ✅ |
| 执行员 | 159 | ✅ |
| 经销商 | 160 | ✅ |
| 品牌销售 | 158 | ❌（只读角色，不授权按钮） |

### 4.3 脚本位置

```
db/branches/feature_step8-咨询服务集成/feature_step8-咨询服务集成_dml.sql
```

---

## 五、会话去重机制

复用 Step 7 后端 `CsSessionServiceImpl.createSession()` 的去重逻辑：

```
IF 存在 (consultType = X AND contextCode = Y AND status IN (0,1))
  THEN 返回已有 sessionId
  ELSE 创建新会话
```

**效果**：用户在同一条记录上多次点击「咨询」，始终打开同一个活跃会话，不会重复创建。

---

## 六、文件变更清单

| 文件路径 | 变更类型 | 说明 |
|----------|----------|------|
| `src/hooks/useCsConsult.ts` | 新增 | 共享咨询 composable |
| `src/views/opshub/signing/components/ContractTable.vue` | 修改 | 咨询按钮 @click 接线 |
| `src/views/opshub/signing/index.vue` | 修改 | 集成 useCsConsult + ChatWindow |
| `src/views/opshub/order/index.vue` | 修改 | 替换咨询占位符 + ChatWindow |
| `src/views/opshub/aftersale/index.vue` | 修改 | 替换咨询占位符 + ChatWindow |
| `src/views/opshub/basedata/index.vue` | 修改 | 新增咨询按钮 + ChatWindow |
| `src/layout/Layout.vue` | 修改 | 挂载 ChatFloatingButton + ChatWindow |
| `db/branches/feature_step8-咨询服务集成/feature_step8-咨询服务集成_dml.sql` | 新增 | 权限 DML |

---

## 七、验收标准

### 7.1 签约模块
- [ ] 行操作列点击「咨询」→ 打开 ChatWindow Drawer，标题显示合同上下文
- [ ] 批量选中后点击「批量咨询」→ 为每条记录创建/复用会话，打开第一个

### 7.2 订单模块
- [ ] 行操作列点击「咨询」→ 打开 ChatWindow Drawer，标题显示订单号
- [ ] 批量选中后点击「批量咨询」→ 同上

### 7.3 售后模块
- [ ] 行操作列点击「💬 客服」→ 打开 ChatWindow Drawer，标题显示售后单号
- [ ] 批量选中后点击「批量咨询」→ 同上

### 7.4 基础数据模块
- [ ] 行操作列新增「💬 咨询」按钮（有权限时可见）
- [ ] 工具栏新增「批量咨询」按钮（选中后激活）
- [ ] 点击后行为与其他模块一致

### 7.5 全局浮动按钮
- [ ] 任意页面右下角显示 💬 浮动按钮
- [ ] 点击后创建 `other` 类型咨询会话并打开 Drawer

### 7.6 权限
- [ ] `dealer:basedata:consult` 权限对经销商、执行员、品牌管理员可见
- [ ] 品牌销售角色不可见该按钮

### 7.7 会话去重
- [ ] 同一记录多次点击咨询，不会创建多个会话（复用已有活跃会话）
