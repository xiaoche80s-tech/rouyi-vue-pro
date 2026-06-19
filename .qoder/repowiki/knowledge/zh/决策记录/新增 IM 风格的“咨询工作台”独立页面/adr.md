# 新增 IM 风格的“咨询工作台”独立页面

_来源：49bb62d → ebb1cd2 提交周期内记录的编码计划——内容为规划时意图，实现可能滞后或有出入。_

**状态：** accepted

## 背景
原有的 `ConsultTab.vue` 采用“表格 + Drawer 浮层”模式，执行员在处理多个并发咨询时需反复打开/关闭抽屉，无法一目了然地切换会话，且缺乏全局的实时状态感知。

## 决策驱动
- 提升多会话并发处理效率
- 提供类似微信/DingTalk 的沉浸式沟通体验
- 保留原有表格视图以满足不同用户习惯

## 备选方案
- **重构原 ConsultTab 为 IM 布局** _（已否决）_ — 优点：统一入口，无需维护两个页面；缺点：破坏原有用户习惯，迁移成本高，且表格视图在某些统计场景下仍有价值
- **保留原表格页，新增独立 IM 工作台页面** — 优点：平滑过渡，用户可选择偏好视图；IM 布局（左列表右聊天）更适合高频沟通；复用现有聊天组件；缺点：需维护两套前端视图代码，菜单结构稍显复杂

## 决策
保留原 `ConsultTab.vue`（仅增加 WebSocket 实时刷新能力），新增独立菜单项“咨询工作台”（路径 `cs-workbench`）。新页面采用 IM 布局：左侧为 `ConsultSessionList`（支持搜索、状态筛选、未读角标），右侧为 `ConsultChatPanel`（复用 `ChatHeader`、`ChatMessageList`、`ChatInputBar`）。

## 影响
前端需新建 `src/views/opshub/csWorkbench/` 目录及相关组件；原 `ConsultTab` 仅做轻量级 WebSocket 接入；两个页面共享同一套后端 API 和 WebSocket 推送通道；经销商端的浮层聊天模式保持不变。