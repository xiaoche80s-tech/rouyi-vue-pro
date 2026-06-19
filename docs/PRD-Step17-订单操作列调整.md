# PRD-Step17-订单操作列调整

## 概述

将订单模块列表的操作列从 el-dropdown 下拉菜单方式调整为直接展示的 link 按钮，精简操作项（去掉查看详情、更新进度、申请退货），仅保留申请付款、申请开票、客服三个操作，横排排列用 "/" 分隔，缩减操作列宽度。

## 变更背景

1. 当前操作列采用 `el-dropdown` 下拉菜单，所有操作隐藏在下拉中，用户需要额外点击才能看到可用操作，不够直观
2. 部分操作（查看详情、更新进度、申请退货）使用频率低或已有其他入口，占据下拉菜单空间
3. 操作列宽度 180px 偏大，可进一步缩窄以给其他数据列留出空间

## 变更内容

### 1. 操作列呈现方式调整（P0）

**改动文件**：
- `yudao-ui/yudao-ui-admin-vue3/src/views/opshub/order/components/OrderTable.vue` — 将 el-dropdown 替换为 el-button link 横排展示

**设计逻辑**：
- 移除 `el-dropdown` + `el-dropdown-menu` + `el-dropdown-item` 结构
- 改为直接使用 `<el-button link type="primary">` 横排排列
- 按钮之间用 `<span>` 渲染 "/" 分隔符
- 操作列宽度从 `width="180"` 缩减为 `width="140"`
- 移除 `handleCommand` 分发函数，改为直接绑定 `@click` 事件

**界面原型**：见 Canvas 原型 `order-actions-prototype.canvas.tsx`

### 2. 精简操作项（P0）

**改动文件**：
- `yudao-ui/yudao-ui-admin-vue3/src/views/opshub/order/components/OrderTable.vue` — 删除不再需要的操作项和对应 emit
- `yudao-ui/yudao-ui-admin-vue3/src/views/opshub/order/index.vue` — 删除不再需要的操作处理函数

**设计逻辑**：

| 操作项 | 变更 | 说明 |
|--------|------|------|
| 查看详情 | **移除** | 订单号已可点击查看详情，入口重复 |
| 申请付款 | **保留** | 条件：`payStatus === 'unpaid'` 且权限 `dealer:order:pay` |
| 申请开票 | **保留** | 条件：`payStatus === 'paid'` 且 `invStatus !== 'invoiced'` 且权限 `dealer:order:invoice` |
| 申请退货 | **移除** | 低频操作，从操作列移除 |
| 咨询 | **保留并重命名** | 按钮文字改为「客服」，权限 `dealer:order:consult` |
| 更新进度 | **移除** | 从操作列移除 |

**调整后可见操作矩阵**：

| 付款状态 | 开票状态 | 可见操作 |
|----------|----------|----------|
| 未付款 | 任意 | 申请付款 / 客服 |
| 已付款 | 未开票 / 部分开票 | 申请开票 / 客服 |
| 已付款 | 已开票 | 客服 |

### 3. 父组件清理（P0）

**改动文件**：
- `yudao-ui/yudao-ui-admin-vue3/src/views/opshub/order/index.vue` — 移除 `updateProgress` 事件监听和处理函数

**设计逻辑**：
- 移除 `@update-progress` 事件绑定
- 移除 `handleUpdateProgress` 函数
- 移除 `OrderTable` 组件的 `updateProgress` emit 声明
- `applyReturn` 相关代码也一并清理（退货操作已移除）

## 页面元素规格

### 订单列表 — 操作列

#### Form 表单（输入项）

无输入表单，操作列仅包含按钮。

#### 接口输出项（展示项）

| 字段名 | 数据类型 | 取值范围 | 说明 |
|--------|----------|----------|------|
| payStatus | String | unpaid / paid | 控制「申请付款」按钮是否显示 |
| invStatus | String | uninvoiced / partial / invoiced | 控制「申请开票」按钮是否显示 |

**操作按钮规格**：

| 按钮文字 | 元素类型 | 显示条件 | 权限码 | 点击行为 |
|----------|----------|----------|--------|----------|
| 申请付款 | el-button link primary | `payStatus === 'unpaid'` | `dealer:order:pay` | 弹出付款申请弹窗 |
| 申请开票 | el-button link primary | `payStatus === 'paid' && invStatus !== 'invoiced'` | `dealer:order:invoice` | 弹出开票申请弹窗 |
| 客服 | el-button link primary | 始终显示 | `dealer:order:consult` | 打开客服聊天窗口 |

**分隔符**：按钮之间用 `<span style="color: #dcdfe6; margin: 0 4px">/</span>` 分隔。

## API 接口定义

本次变更不涉及后端 API 接口的新增或修改，仅调整前端操作列的呈现方式。

现有接口保持不变：
- `POST /opshub/order/apply-payment` — 申请付款
- `POST /opshub/order/apply-invoice` — 申请开票
- `POST /opshub/order/consult` — 客服咨询（实际走 IM 会话创建）

## DDL 变更

无。

## DML 变更

无。

## 涉及文件清单

### 后端（yudao-module-opshub）
无后端变更。

### 前端（yudao-ui/yudao-ui-admin-vue3）
| 文件 | 操作 |
|------|------|
| `src/views/opshub/order/components/OrderTable.vue` | 修改 — 操作列从 el-dropdown 改为 link 按钮，精简操作项，缩窄列宽 |
| `src/views/opshub/order/index.vue` | 修改 — 移除 updateProgress、applyReturn 相关事件和处理函数 |

### DDL/DML
无。

## 已知限制

- 退货功能从操作列移除后，如需退货入口，需通过其他途径（如订单详情页）访问
- 更新进度功能移除后，管理员如需更新进度，需通过其他途径操作

## 剩余待做（P2/P3）

| 优先级 | 任务 | 说明 |
|-------|------|------|
| P2 | 订单详情页增加退货入口 | 退货操作从列表移除后，在详情弹窗中保留退货功能 |
| P2 | 订单详情页增加进度更新入口 | 更新进度从列表移除后，在详情弹窗中保留进度更新功能 |
