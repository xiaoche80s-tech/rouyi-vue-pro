---
kind: design
name: 采用 WebSocket 实现客服实时通讯与状态同步
source: session
category: adr
---

# 采用 WebSocket 实现客服实时通讯与状态同步

_来源：11502cc → 4b587e4 提交周期内记录的编码计划——内容为规划时意图，实现可能滞后或有出入。_

**状态：** accepted

## 背景
Step 7 需构建在线咨询聊天系统，核心需求包括持久化保存聊天记录、实时通讯、跨模块咨询入口及咨询队列管理。传统的 HTTP 轮询无法满足实时性要求，且需支持多角色（经销商、执行人、管理员）间的即时消息推送和会话状态变更通知。

## 决策驱动
- 实时性体验
- 双向通信能力
- 系统解耦

## 备选方案
- **WebSocket 实时推送** — 优点：支持服务器主动向客户端推送消息和事件，延迟低，适合聊天场景；可复用现有框架的 executeAfterTransaction 模式保证数据一致性后推送。；缺点：需维护长连接状态，增加服务端资源消耗；需处理断线重连等复杂场景。
- **HTTP 短轮询** _（已否决）_ — 优点：实现简单，无状态，易于扩展。；缺点：实时性差，频繁请求增加服务器负载，无法有效支持即时聊天体验。

## 决策
采用 WebSocket 作为实时通讯基础。后端新增 `CsWebSocketService` 及其实现，提供 `sendChatMessageAsync`、`sendSessionEventAsync` 和 `broadcastNewConsult` 方法，在事务提交后异步推送消息。前端通过 `useCsWebSocket` Hook 监听 `cs-chat` 和 `cs-session` 事件，实现消息实时接收和会话状态更新。

## 影响
实现了低延迟的在线聊天体验，支持新咨询广播和消息实时推送。但引入了 WebSocket 连接管理的复杂性，需确保前端正确处理连接生命周期和事件监听。