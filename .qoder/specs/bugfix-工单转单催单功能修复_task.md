# BugFix — 工单转单/催单功能修复

## 问题描述

### Bug 1：转单选择人不是 BPM 候选人

- **现象**：转单弹窗显示所有系统用户（通过 `getSimpleUserList()` 获取），而非 BPM 流程中定义的候选人
- **预期**：转单时只显示 BPM 流程当前节点配置的角色/岗位对应的候选人列表
- **复现步骤**：
  1. 以执行员登录，进入客户服务 → 工单列表
  2. 点击一条"处理中"工单的"转单"按钮
  3. 观察"新处理人"下拉框显示的是全部系统用户
- **影响范围**：opshub 模块工单管理、前端 TaskTab 组件

### Bug 2：转单后 BPM 流程执行人员未同步

- **现象**：转单后 `ops_cs_task` 表的 `assigneeId` 更新了，但 BPM 流程中的任务处理人（assignee）未变
- **预期**：转单后 BPM 流程任务的 assignee 也应同步更新为新处理人
- **复现步骤**：
  1. 以执行员登录，转单给另一个执行员
  2. 查看工单表 `ops_cs_task` → assigneeId 已变更 ✅
  3. 查看 BPM 流程实例详情 → 当前任务处理人仍是原审批人 ❌
- **影响范围**：opshub 模块 CsTaskServiceImpl、BPM 流程引擎

### Bug 3：催单功能无效果

- **现象**：点击催办按钮前端提示"催办成功"，但处理人没有收到任何催办通知
- **预期**：处理人应实时收到催办 WebSocket 通知或站内信
- **复现步骤**：
  1. 以经销商/管理员登录，进入工单列表
  2. 点击一条非"已关闭"工单的"催办"按钮
  3. 前端提示"催办成功"，但处理人（执行员）端无任何通知
- **错误信息**：无报错，但通知未送达
- **影响范围**：opshub 模块 urgeTask 方法、前端 TaskTab 组件

## 根因分析

### Bug 1 根因

- **问题定位**：`TaskTab.vue` 第 162-164 行，转单弹窗的"新处理人"下拉框使用 `userList`（来自 `getSimpleUserList()` 全量用户列表）
- **原因**：开发时复用了创建工单的处理人下拉框（全量用户），未对接 BPM 流程候选人 API
- **关联**：BPM 模块已有 `BpmTaskCandidateInvoker.calculateUsersByTask()` 方法计算候选人，但未暴露给 opshub 模块调用

### Bug 2 根因

- **问题定位**：`CsTaskServiceImpl.transferTask()` 第 191-194 行，只执行了 `csTaskMapper.updateById()` 更新工单表
- **原因**：遗漏了同步调用 BPM 模块的 `bpmTaskService.transferTask()` 来变更 Flowable 任务处理人
- **对比**：同文件中 `submitForApproval()` / `verifyTask()` / `cancelTask()` 都正确调用了 BPM 方法

### Bug 3 根因（双重问题）

- **问题 A — 后端**：`CsTaskServiceImpl.urgeTask()` 方法（第 280-291 行）缺少 `@Transactional` 注解，且未发送站内信通知
  - 无 `@Transactional` → `executeAfterTransaction()` 直接执行（功能上可工作，但不规范）
  - 仅发送 WebSocket，无 `sendNotify()` 站内信兜底 → 处理人离线时无法收到催办
- **问题 B — 前端**：`TaskTab.vue` 没有任何 WebSocket 监听
  - `useCsWebSocket` hook 仅在 `ConsultTab.vue` 中使用
  - 即使后端正确发送了 `cs-task-urging` WebSocket 消息，前端 TaskTab 也不会收到
  - `useCsWebSocket` 的 switch 语句只处理 `cs-chat-message`/`cs-session-event`/`cs-new-consult`，未处理 `cs-task-*` 类型

## 修复方案

### Task 1: 后端 — 新增获取 BPM 候选人列表 API

**修改文件**：
- `yudao-module-opshub/.../controller/admin/cs/CsTaskController.java`
- `yudao-module-opshub/.../service/cs/CsTaskService.java`
- `yudao-module-opshub/.../service/cs/impl/CsTaskServiceImpl.java`

具体内容：
1. 在 `CsTaskService` 新增方法 `List<Long> getBpmTaskCandidateUsers(Long csTaskId)`
2. 在 `CsTaskServiceImpl` 实现：
   - 根据工单 `processInstanceId` 获取当前 BPM 任务
   - 调用 `BpmTaskCandidateInvoker.calculateUsersByTask(execution)` 获取候选人 ID 列表
   - 需要通过 Flowable Task API 获取 `DelegateExecution`
3. 在 `CsTaskController` 新增接口：
   - `GET /opshub/cs-task/candidate-users?id=xxx`
   - 权限码：`dealer:cs-task:transfer`（复用转单权限）
   - 返回：`CommonResult<List<UserSimpleRespVO>>`（用户简化信息列表）

### Task 2: 后端 — 修复转单同步 BPM 处理人

**修改文件**：
- `yudao-module-opshub/.../service/cs/impl/CsTaskServiceImpl.java`

具体内容：
在 `transferTask()` 方法中，更新工单表后，增加调用 BPM 转派：
```java
// 现有逻辑：更新工单表
csTaskMapper.updateById(...);

// 新增：同步 BPM 流程任务处理人
String bpmTaskId = findCurrentBpmTaskId(task.getProcessInstanceId());
if (bpmTaskId != null) {
    BpmTaskTransferReqVO bpmTransferReqVO = new BpmTaskTransferReqVO();
    bpmTransferReqVO.setId(bpmTaskId);
    bpmTransferReqVO.setAssigneeUserId(reqVO.getNewAssigneeId());
    bpmTransferReqVO.setReason(reqVO.getReason() != null ? reqVO.getReason() : "工单转单");
    bpmTaskService.transferTask(currentUserId, bpmTransferReqVO);
}
```

### Task 3: 后端 — 修复催单功能

**修改文件**：
- `yudao-module-opshub/.../service/cs/impl/CsTaskServiceImpl.java`

具体内容：
1. 给 `urgeTask()` 添加 `@Transactional(rollbackFor = Exception.class)` 注解
2. 增加站内信通知作为兜底：
```java
sendNotify(task.getAssigneeId(), NOTIFY_TASK_URGING, buildNotifyParams(task));
```
3. 增加站内信模板常量 `NOTIFY_TASK_URGING = "cs-task-urging"`（如不存在）
4. 增加 `assigneeId` 为 null 时的防御性检查

### Task 4: 前端 — 转单弹窗改用 BPM 候选人 API

**修改文件**：
- `yudao-ui/yudao-ui-admin-vue3/src/api/opshub/csTask/index.ts`
- `yudao-ui/yudao-ui-admin-vue3/src/views/opshub/customerservice/components/TaskTab.vue`

具体内容：
1. 在 `csTask/index.ts` 新增 API：
```ts
// 获取工单 BPM 候选人列表
export const getBpmCandidateUsers = async (id: number) => {
  return await request.get({ url: '/opshub/cs-task/candidate-users?id=' + id })
}
```
2. 在 `TaskTab.vue` 中：
   - 新增 `candidateUserList` ref 变量
   - 在 `handleTransfer` 方法中调用新 API 加载候选人列表
   - 转单弹窗的 `el-select` 改为使用 `candidateUserList` 替代 `userList`

### Task 5: 前端 — TaskTab 增加工单通知 WebSocket 监听

**修改文件**：
- `yudao-ui/yudao-ui-admin-vue3/src/hooks/useCsWebSocket.ts`
- `yudao-ui/yudao-ui-admin-vue3/src/views/opshub/customerservice/components/TaskTab.vue`

具体内容：
1. 在 `useCsWebSocket.ts` 的 switch 中增加 `cs-task-urging`、`cs-task-transferred` 等任务通知类型的处理
2. 或者在 `TaskTab.vue` 中引入 `useCsWebSocket` hook，增加催办/转单等通知的处理回调
3. 收到催办通知时，使用 `ElNotification` 弹出提示
4. 收到转单通知时，刷新列表 `getList()`

### Task 6: DML — 新增催办站内信模板

**修改文件**：
- `db/branches/bugfix-task-transfer-urge/` 目录（新建）

具体内容：
```sql
INSERT INTO system_notify_template (name, code, nickname, content, params, status, remark, ...)
VALUES ('工单催办通知', 'cs-task-urging', '系统通知',
  '工单 {taskNo} 被催办，请尽快处理。内容：{content}',
  '["taskNo","content"]', 0, '催办时通知处理人', ...);
```

### Task 7: 编译验证

- 后端：`mvn compile -pl yudao-module-opshub` 通过
- 前端：无 TypeScript 报错

## 实施顺序

| Task | 内容 | 依赖 |
|------|------|------|
| 1 | 后端新增 BPM 候选人 API | 无 |
| 2 | 后端修复转单同步 BPM | 无 |
| 3 | 后端修复催单功能 | 无 |
| 4 | 前端转单弹窗改用候选人 API | Task 1 |
| 5 | 前端 TaskTab 增加 WebSocket 监听 | Task 3 |
| 6 | DML 催办站内信模板 | Task 3 |
| 7 | 编译验证 | Task 1-6 |

## 验证方式

| 验证项 | 操作 | 预期 |
|--------|------|------|
| Bug 1 - 转单选人 | 执行员点击转单，观察下拉框 | 只显示 BPM 流程定义的角色/岗位候选人 |
| Bug 2 - BPM 同步 | 转单后查看 BPM 流程实例详情 | 当前任务处理人已变更为新处理人 |
| Bug 3 - 催办 WebSocket | 经销商催办，执行员端观察 | 执行员实时收到催办通知弹窗 |
| Bug 3 - 催办站内信 | 经销商催办，执行员查看站内信 | 执行员收到催办站内信 |
| 回归 - 转单通知 | 转单后观察新处理人端 | 新处理人收到转单通知 |
| 回归 - 工单流程 | 完整走一遍 创建→接单→转单→提交审批→验收 | 全流程正常 |

## 涉及文件清单

| 文件 | 操作 |
|------|------|
| `CsTaskController.java` | 修改（新增候选人 API） |
| `CsTaskService.java` | 修改（新增接口方法） |
| `CsTaskServiceImpl.java` | 修改（修复转单同步 + 催单 + 新增候选人方法） |
| `csTask/index.ts` | 修改（新增 API 方法） |
| `TaskTab.vue` | 修改（转单弹窗 + WebSocket 监听） |
| `useCsWebSocket.ts` | 修改（增加任务通知类型处理） |
| `db/branches/bugfix-task-transfer-urge/` | 新建（DML） |

## 风险与注意事项

| 风险 | 缓解措施 |
|------|---------|
| `BpmTaskCandidateInvoker` 是 BPM 内部组件，opshub 跨模块调用可能引入循环依赖 | 通过 `BpmTaskService` 暴露候选人查询方法，避免 opshub 直接依赖 BPM 内部类 |
| BPM 转派需要当前用户是 BPM 任务的 assignee | `transferTask()` 已有 `validateIsAssignee` 校验，BPM 的 `validateTask` 也会校验 |
| WebSocket 通知在前端是"尽力交付"，不保证 100% 送达 | 催办同时发送站内信作为兜底，站内信可在右上角消息中心查看 |
| 候选人列表 API 依赖流程实例存在 | 工单创建时已回填 `processInstanceId`，正常流程下不会为 null |
