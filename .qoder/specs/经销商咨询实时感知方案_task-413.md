# 经销商咨询实时感知方案

## Context

当前咨询系统已具备完整的会话创建、WebSocket 推送能力，但执行员侧缺乏实时感知机制：
- `ConsultTab.vue` 完全未接入 WebSocket，执行员必须手动刷新
- `broadcastNewConsult()` 广播给所有管理端用户（不区分角色）
- 统计接口未按角色过滤
- 表格+Drawer 模式无法一目了然，执行员处理多个咨询时需反复开关

**改造目标**（两个页面 + 全局通知）：
1. **原 ConsultTab**（表格+Drawer）— 保留原样，增加 WebSocket 实时刷新 + Tab 角标
2. **新"咨询工作台"页面**（独立菜单）— IM 聊天窗口风格，左会话列表 + 右聊天面板
3. **全局通知组件** — 任意页面收到新咨询 Toast 提醒

---

## 一、原 ConsultTab 增强（保留表格+Drawer）

**现有页面不动**，仅增加 WebSocket 实时能力：

| 改造点 | 现状 | 目标 |
|--------|------|------|
| WebSocket 接入 | 无 | 接入 `useCsWebSocket`，监听三类消息 |
| 新咨询到达 | 无反应 | 自动刷新列表 + 统计卡片 |
| 消息更新 | 无反应 | 更新对应行的 `lastMessage` / `messageCount` |
| 事件到达 | 无反应 | 接单/完成/关闭 → 更新行状态 + 刷新统计 |
| Tab 角标 | 无 | 咨询队列 Tab 显示待处理数红色角标 |

**ConsultTab.vue 改造**：
```ts
const { isConnected } = useCsWebSocket(
  (msg) => { /* cs-chat-message: 更新对应行的 lastMessage */ },
  (msg) => { /* cs-session-event: 刷新行状态 + 统计 */ getList() },
  (msg) => { /* cs-new-consult: 刷新列表 + 统计 + 高亮新行 */ getList() }
)
```

**Tab 角标**（`customerservice/index.vue`）：
```vue
<el-tab-pane name="consult">
  <template #label>
    <el-badge :value="pendingCount" :hidden="!pendingCount" :max="99">咨询队列</el-badge>
  </template>
</el-tab-pane>
```

---

## 二、新"咨询工作台"独立页面（IM 风格）

### 2.1 菜单位置

在"客户服务"（6008）同级新增独立菜单：

| 属性 | 值 |
|------|-----|
| menu id | 6099（或下一个可用 ID） |
| 名称 | 咨询工作台 |
| path | cs-workbench |
| icon | ep:chat-line-round |
| component | opshub/csWorkbench/index |
| parent_id | 6000（OpsHub 根菜单） |
| 权限 | `dealer:cs-consult:query`（与咨询队列共用权限） |

**角色授权**：brand_admin(157) + service_executor(159) + dealer(160)

### 2.2 IM 布局

```
┌──────────────────────────────────────────────────────┐
│ [待处理:3] [处理中:2] [已完成:5] [已关闭:1]  ← 顶部统计条│
├─────────────┬────────────────────────────────────────┤
│ 搜索会话...  │  🟡 待处理    CS-20260617-001         │
│ ┌─────────┐ │  华康医疗器械 | 签约咨询                │
│ │🔵 华康.. │ │  合同 MC-2026-001 签署流程咨询          │
│ │签约咨询  │ │────────────────────────────────────── │
│ │合同MC..  │ │                                        │
│ │2分钟前   │ │  [系统] 咨询会话已创建          10:30   │
│ ├─────────┤ │  [华康张三] 请问合同签署流程...  10:31   │
│ │🟠 瑞丰.. │ │  [执行员李四] 您好，合同签署需... 10:33 │
│ │售后咨询  │ │                                        │
│ │售后单AS..│ │                                        │
│ │5分钟前   │ │                                        │
│ ├─────────┤ │────────────────────────────────────────│
│ │ ...      │ │  [接单] [完成处理]                      │
│ │          │ │  ┌─────────────────────────┐ [发送]    │
│ │          │ │  │ 输入消息...              │           │
│ │          │ │  └─────────────────────────┘           │
│ └─────────┘ │                                        │
├─────────────┴────────────────────────────────────────┤
│ 筛选: [全部▾] [咨询类型▾]    排序: [最近消息▾]         │
└──────────────────────────────────────────────────────┘
```

### 2.3 组件结构

```
src/views/opshub/csWorkbench/
├── index.vue                    ← 主页面（IM 布局容器 + WebSocket 集成）
└── components/
    ├── ConsultSessionList.vue   ← 左侧会话列表
    └── ConsultChatPanel.vue     ← 右侧聊天面板（复用 ChatHeader/ChatMessageList/ChatInputBar）
```

### 2.4 左侧面板 — ConsultSessionList

| 元素 | 说明 |
|------|------|
| 搜索框 | 按经销商名/会话编号/上下文搜索 |
| 状态筛选 | 下拉：全部 / 待处理 / 处理中 / 已完成 / 已关闭 |
| 咨询类型筛选 | 下拉筛选 |
| 会话项 | 经销商首字母头像 + 名称 + 咨询类型Tag + 最后消息 + 时间 + 状态色点 + 未读角标 |
| 排序 | `lastMessageTime` 降序，待处理优先 |
| 新会话动画 | 从顶部滑入 + 3 秒黄色闪烁 |

### 2.5 右侧面板 — ConsultChatPanel

**复用现有组件**（不改）：
- `ChatHeader.vue` — 执行员视图头部（状态 + 经销商 + 操作按钮）
- `ChatMessageList.vue` — 消息列表（气泡样式）
- `ChatInputBar.vue` — 输入栏

### 2.6 WebSocket 集成

在 `index.vue` 顶层接入 `useCsWebSocket`：
- `onNewConsult` → 会话列表 unshift + 高亮闪烁
- `onChatMessage` → 更新对应会话 lastMessage；非当前会话递增未读
- `onSessionEvent` → 更新状态 + 刷新统计

---

## 三、全局感知 — Layout 通知组件

在 `Layout.vue` 中新增 `CsExecutorNotifier` 全局组件（执行员角色可见）：
- 监听 `cs-new-consult` → ElNotification Toast
- 点击 Toast → `router.push` 到咨询工作台页面（或客户服务页咨询队列 Tab）
- 精准推送保证只有相关执行员收到

---

## 四、后端能力设计

### 4.1 精准推送 — 按角色 × 产品线定向通知

**已有基础设施**：
- `PermissionApi.getUserRoleIdListByRoleIds(roleIds)` — 执行员用户 ID 集合
- `ExecutorProductLineScopeMapper.selectUserIdsByProductLineCode(code)` — 产品线授权执行员
- `WebSocketSenderApi.send(userType, userId, type, content)` — 精准推送

**推送策略**：
```
创建会话 → 获取执行员角色用户 ∩ 产品线授权用户 → 逐一推送
```

**变更**：
- `CsWebSocketService` 新增 `notifyMatchingExecutors(CsChatMessage, String productLineCode)`
- `CsSessionServiceImpl` 替换 `broadcastNewConsult()` 为精准推送
- `CsSessionServiceImpl` 注入 `PermissionApi`（替代 `PermissionCommonApi`）

### 4.2 增强消息体

`CsChatMessage.java` 新增 `consultType`、`dealerName`、`context` 字段。

### 4.3 统计接口角色化

`getStatistics()` 按角色过滤：执行员看 待处理 + 自己处理的；经销商看 自己发起的。

### 4.4 未读计数 API（P1）

`GET /opshub/cs-session/unread-count`

---

## 五、分级实施计划

### P0 — 核心（~10h）

| # | 层级 | 任务 | 关键文件 |
|---|------|------|---------|
| 1 | 后端 | 精准推送 + 增强消息体 | `CsWebSocketServiceImpl` / `CsChatMessage` / `CsSessionServiceImpl` |
| 2 | 后端 | `getStatistics()` 角色化 | `CsSessionServiceImpl` / `CsSessionMapper` |
| 3 | 前端 | **原 ConsultTab 增加 WebSocket 实时刷新 + Tab 角标** | `ConsultTab.vue` / `customerservice/index.vue` |
| 4 | 前端 | **新建 `ConsultSessionList.vue`** — 会话列表 | `csWorkbench/components/` |
| 5 | 前端 | **新建 `ConsultChatPanel.vue`** — 聊天面板 | `csWorkbench/components/` |
| 6 | 前端 | **新建 `csWorkbench/index.vue`** — IM 布局主页 + WebSocket | `csWorkbench/` |
| 7 | 前端 | 菜单 DML + Layout 全局通知 | DML SQL + `Layout.vue` + `CsExecutorNotifier.vue` |

### P1 — 体验增强（~5h）

| # | 任务 |
|---|------|
| 8 | 声音提醒 + 浏览器标题闪烁 |
| 9 | 未读计数 API + 浮动按钮真实角标 |
| 10 | 站内信精准推送 |
| 11 | 会话列表待处理优先排序 |

### P2 — 锦上添花

| # | 任务 |
|---|------|
| 12 | 通知中心面板 |
| 13 | 离线补偿 |
| 14 | 多会话并发（标签页式） |

---

## 六、关键文件清单

**后端需修改**：
- `yudao-module-opshub/.../service/cs/websocket/CsWebSocketService.java`
- `yudao-module-opshub/.../service/cs/websocket/impl/CsWebSocketServiceImpl.java`
- `yudao-module-opshub/.../service/cs/websocket/dto/CsChatMessage.java`
- `yudao-module-opshub/.../service/cs/impl/CsSessionServiceImpl.java`
- `yudao-module-opshub/.../dal/mysql/cs/CsSessionMapper.java`

**前端需新建**：
- `src/views/opshub/csWorkbench/index.vue` — IM 工作台主页
- `src/views/opshub/csWorkbench/components/ConsultSessionList.vue` — 会话列表
- `src/views/opshub/csWorkbench/components/ConsultChatPanel.vue` — 聊天面板
- `src/components/CsChatWindow/CsExecutorNotifier.vue` — 全局通知组件
- `db/branches/feature_step9-咨询工作台/` — 菜单 DML SQL

**前端需修改**：
- `src/views/opshub/customerservice/components/ConsultTab.vue` — 增加 WebSocket 实时刷新
- `src/views/opshub/customerservice/index.vue` — Tab 角标
- `src/layout/Layout.vue` — 挂载 CsExecutorNotifier

**复用（不改）**：
- `src/components/CsChatWindow/ChatHeader.vue` / `ChatMessageList.vue` / `ChatInputBar.vue` / `ChatMessageItem.vue`
- `src/hooks/useCsWebSocket.ts`
- `src/api/opshub/csSession/index.ts`

## 七、注意事项

- **两个页面共享后端 API 和 WebSocket**：精准推送同时服务 ConsultTab 和工作台页面
- **原 ChatWindow Drawer 保留**：经销商在各模块点击「咨询」仍用浮层 Drawer
- **WebSocket 连接管理**：三个入口（ConsultTab、工作台、全局通知）各建连接，P1 优化为全局单例
- **菜单 ID**：需确认下一个可用 ID，暂定 6099

## 八、验证方式

1. 经销商创建咨询（带产品线A）→ 仅负责产品线A的执行员收到全局 Toast
2. 执行员在**原咨询队列 Tab** → 新咨询自动出现在表格 + 统计卡片刷新 + Tab 角标更新
3. 执行员在**新咨询工作台页** → 左侧列表自动滑入新会话 + 黄色闪烁
4. 工作台点击会话 → 右侧实时显示聊天消息
5. 执行员接单/完成 → 两个页面状态均实时更新
6. 点击全局 Toast → 跳转到咨询工作台页面
