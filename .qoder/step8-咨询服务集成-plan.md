# Step 8 — 咨询服务入口全模块集成

## Context

Step 7 已完成电商客服系统的核心能力（会话管理、消息收发、WebSocket 推送、聊天窗口组件），但各业务模块的「💬 咨询」按钮仍为占位符，未接入客服系统。Step 8 目标：**将 Step 7 客服系统与签约/售后/订单/基础数据/客户服务模块的咨询入口全部打通，实现完整闭环**。

---

## 一、现状分析

| 模块 | 咨询按钮状态 | 需做工作 |
|------|------------|---------|
| 签约进度 | `ContractTable.vue` "咨询"按钮无 `@click`（死按钮）；`handleBatchConsult` = `'批量咨询功能开发中'` | 接线 + 批量 |
| 订单 | `handleConsult` = `'咨询功能开发中'`；`handleBatchConsult` 调用空壳后端 API | 替换两处 |
| 售后 | `handleConsult` = `'咨询功能开发中'`；`handleBatchConsult` 调用空壳后端 API | 替换两处 |
| 基础数据 | **完全没有咨询按钮**（行内 + 批量均缺失）；缺少 `dealer:basedata:consult` 权限 | 新增按钮 + 权限 |
| 客户服务 | ConsultTab OK；**缺少 6 大分类入口卡片** | 新增分类卡片组件 |
| 全局浮动按钮 | `ChatFloatingButton.vue` 已实现但**从未挂载到 Layout.vue** | 挂载 + WebSocket |

---

## 二、新建文件（2 个）

### 2.1 `src/hooks/useCsConsult.ts` — 共享咨询 composable

统一封装「创建/复用会话 → 打开 ChatWindow」逻辑，各模块复用避免重复代码。

```ts
interface ConsultParams {
  consultType: string   // signing/policy/aftersale/order/basedata/other
  sourceModule: string  // signing/order/aftersale/basedata/manual
  context?: string
  contextId?: number
  contextCode?: string
  productLineCode?: string
  productLineName?: string
  dealerCode?: string
  dealerName?: string
}

// 返回值
chatVisible: Ref<boolean>           // ChatWindow Drawer 可见性
currentSessionId: Ref<number>       // 当前打开的会话 ID
openConsult(params)                 // 单条咨询：createSession → 打开 Drawer
openBatchConsult(rows, builder)     // 批量咨询：逐条 createSession → 打开第一个
openByCategory(consultType)         // 分类卡片入口（无上下文）
```

### 2.2 `src/views/opshub/customerservice/components/CategoryCards.vue` — 6 大分类入口卡片

渲染 PRD 定义的 6 类咨询入口卡片（签约📝/政策📊/售后🔄/订单📦/数据📁/其他💬），点击调用 `openByCategory(consultType)` 创建会话并打开 ChatWindow。采用 `el-row` + `el-col` 网格布局。

---

## 三、修改文件清单

### 3.1 `src/layout/Layout.vue` — 挂载全局浮动💬按钮

- 引入 `ChatFloatingButton` + `useCsConsult` + `useCsWebSocket`
- WebSocket 监听 `cs-chat-message` 和 `cs-new-consult` 累计未读计数
- 浮动按钮点击 → `openByCategory('other')` 创建「其他咨询」会话
- JSX 中追加 `<ChatFloatingButton>` + `<ChatWindow>` 到 `<Setting>` 之后

### 3.2 `src/views/opshub/signing/components/ContractTable.vue` — 咨询按钮接线

- L75: 给按钮加 `@click="emit('consult', row)"`
- `defineEmits` 新增 `(e: 'consult', row): void`

### 3.3 `src/views/opshub/signing/index.vue` — 集成 useCsConsult

- `<ContractTable>` 增加 `@consult="handleConsult"`
- 引入 `useCsConsult`，实现 `handleConsult` 携带 `contractCode` 上下文
- 替换 `handleBatchConsult`：逐条 `createSession` 后打开第一个
- 模板末尾追加 `<ChatWindow v-model="chatVisible" :session-id="currentSessionId" />`

### 3.4 `src/views/opshub/order/index.vue` — 替换占位符

- 引入 `useCsConsult`
- 替换 `handleConsult`：携带 `orderCode` 上下文
- 替换 `handleBatchConsult`：不再调用 `OrderApi.batchConsult`，改为前端逐条创建
- 模板追加 ChatWindow

### 3.5 `src/views/opshub/aftersale/index.vue` — 替换占位符

- 引入 `useCsConsult`
- 替换 `handleConsult`（L461）：携带 `aftersaleCode` 上下文
- 替换 `handleBatchConsult`（L465-478）：不再调用 `AfterSaleApi.batchAfterSaleConsult`
- 注意：`AfterSaleSimpleVO` 无 `dealerCode`/`productLineCode` 字段，仅传 name
- 模板追加 ChatWindow

### 3.6 `src/views/opshub/basedata/index.vue` — 新增咨询按钮

- 操作列追加「💬 咨询」按钮（权限 `dealer:basedata:consult`）
- 批量操作栏追加「批量咨询」按钮
- 引入 `useCsConsult`，实现 `handleConsult` 携带 `fileNo` 上下文
- 修改 `handleSelectionChange` 保存完整行数据（当前仅存 `selectedIds: number[]`）
- 模板追加 ChatWindow

### 3.7 `src/views/opshub/customerservice/index.vue` — 插入分类卡片

- 在 `<el-tabs>` 之前插入 `<ContentWrap><CategoryCards /></ContentWrap>`

---

## 四、DML — 新增基础数据咨询权限

新建 `db/branches/feature_step8-咨询服务集成/feature_step8-咨询服务集成_dml.sql`：

```sql
-- 新增基础数据「咨询」按钮权限 (menu_id=6074)
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, ...)
VALUES (6074, '咨询', 'dealer:basedata:consult', 3, 5, 6007, ...);

-- 角色分配: 6074 → brand_admin(157) / service_executor(159) / dealer(160)
INSERT INTO system_role_menu ...
```

---

## 五、实施顺序

| Task | 内容 | 依赖 |
|------|------|------|
| 1 | 新建 `useCsConsult.ts` composable | 无 |
| 2 | 签约模块集成（ContractTable + index） | Task 1 |
| 3 | 订单模块集成（index） | Task 1 |
| 4 | 售后模块集成（index） | Task 1 |
| 5 | 基础数据集成（index + 权限 DML） | Task 1 |
| 6 | 全局浮动按钮（Layout.vue） | Task 1 |
| 7 | 客户服务分类卡片（CategoryCards + index） | Task 1 |
| 8 | DML 脚本 + 权限验证 | Task 5 |
| 9 | 编译验证 `pnpm dev` | 全部 |

---

## 六、验证方式

| 验证项 | 操作 | 预期 |
|--------|------|------|
| 签约-单条咨询 | 签约列表 → 行「咨询」按钮 | ChatWindow 弹出，contextCode=合同编号 |
| 签约-批量咨询 | 勾选 3 条 → 「批量咨询」 | 创建 3 个会话，打开第一个 |
| 去重验证 | 同一合同连续点击两次「咨询」 | 第二次直接打开已有会话 |
| 订单-单条咨询 | 订单下拉菜单 → 「咨询」 | ChatWindow 弹出，contextCode=订单号 |
| 售后-单条咨询 | 售后列表 → 「客服」按钮 | ChatWindow 弹出，contextCode=售后单号 |
| 基础数据-咨询 | 基础数据列表 → 「💬 咨询」 | ChatWindow 弹出，contextCode=文件编号 |
| 浮动按钮 | 任意页面右下角 | 点击创建「other」类型会话 |
| 分类卡片 | 进入「客户服务」模块 | 顶部 6 张卡片，点击打开对应类型会话 |
| 上下文关联 | 客服端查看签约发起的咨询 | 列表显示 sourceModule=signing |
| 前端编译 | `pnpm dev` | 无 TypeScript 编译错误 |

---

## 七、风险与注意事项

| 风险 | 缓解措施 |
|------|---------|
| `dealer:basedata:consult` 权限缺失 | Step 8 DML 补录菜单 + 角色关联 |
| 批量咨询产生大量 HTTP 请求 | `openBatchConsult` 内用 `Promise.all` 并行，限制上限 20 条 |
| Layout 层 WebSocket 连接 | 复用 `useCsWebSocket`，确认 token 鉴权正常 |
| 各模块 ChatWindow 独立实例 | 同一时间仅一个 Drawer 打开，页面切换时自动关闭（组件卸载） |
