# PRD-Step14-首页快捷入口优化

## 概述

移除执行员和经销商首页的"快捷入口/快捷操作"独立区域，将导航能力融入已有的交互元素中：执行员通过点击"我的待办"卡片跳转，经销商通过点击 KPI 统计卡片跳转。减少页面层级，让操作更直觉化。

## 变更背景

1. 执行员首页的 QuickEntryCards（快捷入口）与 MyTodoPanel（我的待办）功能重叠，待办卡片已展示工单/咨询/操作请求数量，天然适合作为导航入口
2. 经销商首页的 QuickActionsPanel（快捷操作）按钮列表不够直观，而 KPI 卡片（我的合同/待付款订单/进行中售后/我的工单）已按业务域划分，点击即可进入对应模块更符合用户心智

## 变更内容

### 1. StatCard 支持点击导航（P0）

**改动文件**：
- `yudao-ui/yudao-ui-admin-vue3/src/views/Home/components/StatCard.vue` — 新增可选 `route` prop

**设计逻辑**：
- 新增 `route?: string` prop
- 当 `route` 存在时，卡片整体添加 `cursor: pointer` 和 `@click="$router.push(route)"`
- 无 `route` 时保持原有纯展示行为，零影响

### 2. MyTodoPanel 卡片可点击（P0）

**改动文件**：
- `yudao-ui/yudao-ui-admin-vue3/src/views/Home/components/MyTodoPanel.vue` — 3 张待办卡片添加点击导航

**导航映射**：

| 卡片 | 目标路由 | 说明 |
|------|---------|------|
| 待接单工单 | `/opshub/customerservice` | 执行员工单管理页（工单 Tab） |
| 待回复咨询 | `/dealer/cs-workbench` | 咨询工作台 |
| 待处理操作请求 | `/opshub/customerservice?tab=opreq` | 执行员操作请求 Tab |

**设计逻辑**：
- 每张 `.todo-card` 添加 `cursor: pointer` + `@click="$router.push(route)"`
- hover 效果增强（如 box-shadow），提示可交互

### 3. Index.vue 执行员分支 — 移除快捷入口（P0）

**改动文件**：
- `yudao-ui/yudao-ui-admin-vue3/src/views/Home/Index.vue` — executor 分支

**设计逻辑**：
- 删除 `QuickEntryCards` 组件调用块（当前"快捷入口"整段）
- 删除 `QuickEntryCards` 的 import 语句（如 dealer 也不再使用则一并清理）
- KPI 卡片不需要可点击导航（执行员 KPI 为全局统计，非业务入口）

### 4. Index.vue 经销商分支 — 移除快捷操作 + KPI 可点击（P0）

**改动文件**：
- `yudao-ui/yudao-ui-admin-vue3/src/views/Home/Index.vue` — dealer 分支

**导航映射**：

| KPI 卡片 | 目标路由 | 说明 |
|---------|---------|------|
| 我的合同 | `/opshub/signing` | 签约进度模块 |
| 待付款订单 | `/opshub/order` | 订单模块 |
| 进行中售后 | `/opshub/aftersale` | 售后模块 |
| 我的工单 | `/opshub/workorder-service` | 工单服务页 |

**设计逻辑**：
- 4 个 StatCard 添加 `:route` 属性
- 删除 `QuickActionsPanel` 组件调用块
- 删除 `QuickActionsPanel` 的 import 语句

### 5. 后端移除 quickActions 数据填充（P1）

**改动文件**：
- `yudao-module-opshub/.../service/dashboard/DashboardServiceImpl.java`

**设计逻辑**：
- `fillExecutorDashboard()` — 删除快捷操作构建代码（3 行 buildAction + setQuickActions）
- `fillDealerDashboard()` — 删除快捷操作构建代码（4 行 buildAction + setQuickActions）
- `buildAction()` 方法如无其他调用方，一并删除
- `DashboardRespVO.QuickAction` 内部类和 `quickActions` 字段保留（不删 VO 字段，保持 API 向后兼容）

### 6. 清理无用组件文件（P2）

**改动文件**：
- `yudao-ui/yudao-ui-admin-vue3/src/views/Home/components/QuickEntryCards.vue` — 删除
- `yudao-ui/yudao-ui-admin-vue3/src/views/Home/components/QuickActionsPanel.vue` — 删除

**设计逻辑**：
- 确认两个组件不再被任何页面引用后删除

## 涉及文件清单

### 后端（yudao-module-opshub）
| 文件 | 操作 |
|------|------|
| `service/dashboard/DashboardServiceImpl.java` | 修改 — 删除 executor/dealer 的 quickActions 填充 |

### 前端（yudao-ui/yudao-ui-admin-vue3）
| 文件 | 操作 |
|------|------|
| `views/Home/components/StatCard.vue` | 修改 — 新增 route prop + 点击导航 |
| `views/Home/components/MyTodoPanel.vue` | 修改 — 3 卡片添加点击导航 |
| `views/Home/Index.vue` | 修改 — executor 删 QuickEntryCards，dealer 删 QuickActionsPanel + KPI 加 route |
| `views/Home/components/QuickEntryCards.vue` | 删除 |
| `views/Home/components/QuickActionsPanel.vue` | 删除 |

## 已知限制

1. `/opshub/customerservice?tab=opreq` 依赖 customerservice/index.vue 支持 query 参数切换 tab；若当前不支持，需在实现时补充 `onMounted` 读取 `route.query.tab` 的逻辑
2. 路由路径由菜单系统配置，实现时需确认 `/opshub/signing`、`/opshub/order`、`/opshub/aftersale`、`/opshub/workorder-service` 路径与菜单配置一致
