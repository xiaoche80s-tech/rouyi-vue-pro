# Step17 — 订单操作列调整

## Context

将订单模块列表操作列从 el-dropdown 下拉菜单改为直接展示的 link 按钮，精简操作项，仅保留申请付款、申请开票、客服。PRD 见 `docs/PRD-Step17-订单操作列调整.md`。

---

## Task 1: 修改 OrderTable.vue — 操作列改为 link 按钮

**修改文件**：`yudao-ui/yudao-ui-admin-vue3/src/views/opshub/order/components/OrderTable.vue`

详细描述：
- 操作列宽度从 `width="180"` 改为 `width="140"`
- 移除整个 `el-dropdown` 结构（el-dropdown + el-dropdown-menu + el-dropdown-item）
- 替换为 `el-button link type="primary"` 横排排列，按钮间用 "/" 分隔
- 三个操作按钮：
  - 申请付款：`v-if="row.payStatus === 'unpaid' && checkPermi(['dealer:order:pay'])"`，`@click="emit('applyPayment', row)"`
  - 申请开票：`v-if="row.invStatus !== 'invoiced' && checkPermi(['dealer:order:invoice'])"`，`@click="emit('applyInvoice', row)"`
  - 客服：`v-if="checkPermi(['dealer:order:consult'])"`，`@click="emit('consult', row)"`
- 移除 emit 中的 `viewDetail`、`applyReturn`、`updateProgress` 事件
- 移除 `handleCommand` 分发函数
- 移除 `import { checkPermi } from '@/utils/permission'` 中不再需要的部分（checkPermi 仍需保留用于 v-if 判断）

---

## Task 2: 修改 index.vue — 清理不再需要的事件和处理函数

**修改文件**：`yudao-ui/yudao-ui-admin-vue3/src/views/opshub/order/index.vue`

详细描述：
- 移除 `OrderTable` 组件上的 `@view-detail`、`@apply-return`、`@update-progress` 事件绑定
- 移除 `handleViewDetail` 函数（详情由订单号点击触发，保留 detailModalRef）
- 移除 `handleApplyReturn` 函数及 `returnModalRef` 相关代码
- 移除 `handleUpdateProgress` 函数
- 移除 `ApplyReturnModal` 组件的引入和使用
- 保留 `handleViewDetail`（因为订单号点击仍需查看详情）

---

## Task 3: 编译验证

- 前端：`cd yudao-ui/yudao-ui-admin-vue3 && npx vue-tsc --noEmit` 无 TypeScript 报错

---

## 实施顺序

| Task | 内容 | 依赖 |
|------|------|------|
| 1 | OrderTable.vue 操作列改造 | 无 |
| 2 | index.vue 清理 | Task 1 |
| 3 | 编译验证 | Task 1, 2 |

---

## 验证方式

| 验证项 | 操作 | 预期 |
|--------|------|------|
| 操作列呈现 | 查看订单列表操作列 | 显示为 link 按钮横排，"/" 分隔 |
| 未付款状态 | 查看未付款订单行 | 显示「申请付款 / 客服」 |
| 已付款未开票 | 查看已付款未开票行 | 显示「申请开票 / 客服」 |
| 已付款已开票 | 查看已付款已开票行 | 显示「客服」 |
| 列宽 | 检查操作列宽度 | 约 140px，比之前窄 |
| 编译 | vue-tsc 检查 | 无报错 |

---

## 风险与注意事项

| 风险 | 缓解措施 |
|------|---------|
| 退货入口丢失 | P2 任务：后续在订单详情页保留退货功能 |
| 查看详情 emit 移除后订单号点击失效 | 保留 handleViewDetail 和 detailModalRef，订单号点击不走操作列 |
