---
kind: design
name: 实施全局 Layout 级执行员通知组件
source: session
category: adr
---

# 实施全局 Layout 级执行员通知组件

_来源：49bb62d → ebb1cd2 提交周期内记录的编码计划——内容为规划时意图，实现可能滞后或有出入。_

**状态：** accepted

## 背景
执行员在非咨询页面（如仪表盘、其他业务模块）工作时，无法感知新咨询的到来，必须主动切换到咨询页才能发现，导致响应延迟。

## 决策驱动
- 实现跨页面的实时业务感知
- 利用精准推送降低全局通知的噪音
- 提供快捷跳转入口

## 备选方案
- **仅在咨询页内显示通知** _（已否决）_ — 优点：实现简单，无全局影响；缺点：执行员离开咨询页后即失去感知能力，违背“实时感知”目标
- **Layout 级全局通知组件** — 优点：无论用户在哪个页面，均可通过 Toast/Notification 收到提醒；点击可直接跳转至咨询工作台；配合后端精准推送，确保只有执行员收到；缺点：需在 `Layout.vue` 中挂载组件，增加全局 WebSocket 监听逻辑（P0 阶段允许独立连接）

## 决策
在 `Layout.vue` 中新增 `CsExecutorNotifier` 组件，仅对执行员角色可见。该组件监听 `cs-new-consult` 消息，触发 `ElNotification` 桌面弹窗，点击后路由跳转至咨询工作台或咨询队列 Tab。

## 影响
执行员在任何页面均可收到新咨询提醒；P0 阶段 `CsExecutorNotifier` 与咨询页可能建立独立的 WebSocket 连接，P1 阶段需优化为全局单例 Store 以复用连接；需增强 `CsChatMessage` 消息体以包含 `dealerName`、`consultType` 等字段供通知展示。