---
kind: design
name: 采用基于角色与产品线的 WebSocket 精准推送机制
source: session
category: adr
---

# 采用基于角色与产品线的 WebSocket 精准推送机制

_来源：49bb62d → ebb1cd2 提交周期内记录的编码计划——内容为规划时意图，实现可能滞后或有出入。_

**状态：** accepted

## 背景
原有的 `broadcastNewConsult()` 将新咨询消息广播给所有管理端用户，导致品牌管理员、销售等非执行员角色收到无关通知，造成信息干扰；同时执行员侧缺乏实时感知能力，需手动刷新页面。

## 决策驱动
- 减少非相关用户的消息干扰
- 确保负责特定产品线的执行员能即时接收通知
- 利用现有权限与产品线映射基础设施

## 备选方案
- **全量广播 + 前端角色过滤** _（已否决）_ — 优点：后端实现简单，无需查询用户列表；缺点：所有管理端用户均收到 WebSocket 消息，前端需额外逻辑丢弃，网络开销随用户数增加，且无法解决非执行员角色的视觉干扰
- **基于角色与产品线的后端精准推送** — 优点：仅向拥有 `service_executor` 角色且授权了对应产品线的用户发送消息，精准高效，前端无需过滤；缺点：后端需在推送前查询权限 API (`PermissionApi`) 和产品线范围 Mapper (`ExecutorProductLineScopeMapper`)，增加少量 DB/Cache 读取

## 决策
弃用全量广播，在 `CsWebSocketService` 中新增 `notifyMatchingExecutors` 方法。创建会话时，通过 `PermissionApi.getUserRoleIdListByRoleIds` 获取执行员用户 ID，若会话关联了产品线，则通过 `ExecutorProductLineScopeMapper` 取交集，最终调用 `WebSocketSenderApi.send` 进行点对点推送。

## 影响
非相关角色（如品牌管理员）不再收到新咨询 WebSocket 消息；执行员仅收到其负责产品线或全量（无产品线时）的咨询通知；后端 `CsSessionServiceImpl` 需注入 `PermissionApi` 替代原有的 `PermissionCommonApi`。