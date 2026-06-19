
# 工单详情页改造实施计划

## Context

当前工单详情以 Dialog 弹窗形式展示（`task-detail.vue`），仅显示基础工单信息和 BPM 流程摘要卡片。需要改造为独立页面，增加：
1. 完整的 BPM 审批记录时间线（含每个节点的审批意见）
2. 执行人员操作区（上传凭证附件 + 审批操作按钮）
3. 留言即审批意见，存储在 BPM ACT_ID_COMMENT 表中
4. 附件复用现有 `ops_cs_attachment` 表（module='task'）

**无需新建任何数据库表或后端接口。**

---

## Task 1: 新增独立路由

**文件**: `yudao-ui/yudao-ui-admin-vue3/src/router/modules/remaining.ts`（或对应的 opshub 路由文件）

- 新增路由 `/opshub/task-detail`，指向 `views/opshub/customerservice/task-detail.vue`
- 路由参数：`query: { id: 工单ID }`
- 配置菜单隐藏（不显示在侧边栏）

---

## Task 2: 改造 task-detail.vue 为独立页面

**文件**: `yudao-ui/yudao-ui-admin-vue3/src/views/opshub/customerservice/task-detail.vue`

当前 221 行的 Dialog 组件改造为全页面组件，布局分三大区域：

### 2.1 页面头部
- 返回按钮（router.back()）
- 页面标题「工单详情」
- 状态标签 + 紧急程度标签

### 2.2 区域1 — 工单信息卡片
- 保留现有 `el-descriptions` 展示全部字段
- 增加退回原因警示条（仅 status=4 时显示）
- 保持现有数据加载逻辑（`CsTaskApi.getCsTask`）

### 2.3 区域2 — 审批记录时间线
- 调用 `ProcessInstanceApi.getApprovalDetail({ processInstanceId })` 获取 activityNodes
- 用 `el-timeline` 展示每个节点：
  - 节点名称 + 状态标签（已完成/进行中/待处理/已退回，不同颜色）
  - 处理人头像 + 姓名
  - 处理时间
  - 审批意见（reason 字段，来自 BPM ACT_ID_COMMENT）
- 复用 `loadBpmSummary` 中的逻辑，扩展为完整时间线

### 2.4 区域3 — 执行操作区
- **显示条件**: `task.assigneeId === currentUserId` 或通过 `CsTaskApi.getTaskCandidateUsers` 判断
- **凭证附件 Tab**:
  - 使用 `UploadFile` 组件上传文件到 infra 文件服务
  - 上传成功后调用 `CsAttachmentApi.uploadAttachment({ module: 'task', businessId: taskId, fileUrl, fileName, ... })`
  - 展示已上传附件列表（调用 `CsAttachmentApi.getAttachmentList('task', taskId)`）
  - 每个附件显示文件名、大小、上传时间、预览/删除按钮
- **操作按钮栏**（根据工单状态动态显示）:
  - status=0（待接单）: 接单按钮 → `CsTaskApi.acceptTask`
  - status=1（处理中）: 提交审批按钮（弹窗填写审批意见）→ `CsTaskApi.submitForApproval`
  - status=1（处理中）: 转单按钮 → 现有转单逻辑
  - 任意状态: 催办按钮 → `CsTaskApi.urgeTask`

### 2.5 审批意见弹窗
- 点击「提交审批」时弹出 el-dialog
- 包含：审批意见 textarea（必填）
- 确认后调用 `CsTaskApi.submitForApproval(id)`（后端内部通过 BPM approveTask 自动写入 ACT_ID_COMMENT）

---

## Task 3: 修改 TaskTab.vue 跳转方式

**文件**: `yudao-ui/yudao-ui-admin-vue3/src/views/opshub/customerservice/components/TaskTab.vue`

- `handleDetail` 方法：从设置 `detailDialogVisible` 改为 `router.push({ path: '/opshub/task-detail', query: { id: row.id } })`
- 删除 `detailDialogVisible`、`detailTaskId` 相关代码
- 删除 `<el-dialog>` 弹窗模板
- 删除 `import TaskDetail from '../task-detail.vue'`（不再作为子组件引入）

---

## Task 4: 同步修改 workorder-service 页面的 TaskTab

**文件**: `yudao-ui/yudao-ui-admin-vue3/src/views/opshub/workorder-service/index.vue`

- 该页面也使用了 TaskTab 组件（side='initiator'），TaskTab 修改后自动生效，无需额外修改

---

## Task 5: 前端 API 层补充

**文件**: `yudao-ui/yudao-ui-admin-vue3/src/api/opshub/csTask/index.ts`

- 确认 `getTaskCandidateUsers` API 已存在（用于判断当前用户是否为执行人员）

---

## 关键文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `router/modules/remaining.ts` | 修改 | 新增 /opshub/task-detail 路由 |
| `views/opshub/customerservice/task-detail.vue` | 重构 | Dialog → 独立页面，新增审批时间线+操作区 |
| `views/opshub/customerservice/components/TaskTab.vue` | 修改 | Dialog 跳转改为路由跳转 |
| `api/opshub/csTask/index.ts` | 确认 | 确认现有 API 满足需求 |
| `api/opshub/csAttachment/index.ts` | 复用 | 已有 upload/list/delete 接口 |
| `api/bpm/processInstance/index.ts` | 复用 | 已有 getApprovalDetail 接口 |

---

## 验证方式

1. **路由跳转**: 在工单列表点击工单编号，确认跳转到独立详情页
2. **工单信息**: 确认所有字段正确展示，退回状态下显示红色警示
3. **审批记录**: 确认 BPM 时间线正确渲染，每个节点显示审批人和意见
4. **附件上传**: 上传文件后确认附件列表显示正确，删除后列表更新
5. **审批操作**: 点击提交审批，填写意见弹窗，确认后流程推进
6. **权限控制**: 非执行人员访问页面时，操作区不显示
7. **返回列表**: 点击返回按钮，确认回到工单列表页
