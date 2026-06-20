# 将 opshub/cs-task/transfer 迁移为使用 /bpm/task/transfer

## Context

当前工单转单通过 `/opshub/cs-task/transfer` 完成，包含完整的业务逻辑（状态校验、权限校验、更新 cs_task 表、同步 BPM 转派、WebSocket/站内信通知）。目标是让前端改为调用 `/bpm/task/transfer`（已存在的 BPM 原生转派端点），通过已有的 Flowable TASK_ASSIGNED 事件驱动机制完成 cs_task 表同步和通知，**BPM 模块零改动**。

## 核心设计

**调用链变化：**
```
之前: 前端 → /opshub/cs-task/transfer → CsTaskServiceImpl（全量业务逻辑 + BPM同步）
之后: 前端 → /bpm/task/transfer → BpmTaskServiceImpl → Flowable TASK_ASSIGNED
                                                          → CsTaskBpmAssignedListener（同步cs_task + 通知）
```

## Task 1: 增强 CsTaskBpmAssignedListener 支持转单通知

**文件**: `yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/service/cs/listener/CsTaskBpmAssignedListener.java`

在 `syncAssignee()` 方法中增加转单检测和通知逻辑：

1. 注入 `CsWebSocketService` 和 `NotifyMessageSendApi`（`@Lazy`）
2. 在 DB 更新前记录 `previousAssigneeId = current.getAssigneeId()`
3. DB 更新后判断：
   - 若 `previousAssigneeId != null && !previousAssigneeId.equals(assigneeUserId)` 且状态为 IN_PROGRESS → 这是**转单**，发送 `TYPE_TASK_TRANSFERRED` 通知给新处理人
   - 若状态从 PENDING 推进到 IN_PROGRESS → 这是**接单**，发送 `TYPE_TASK_ACCEPTED` 通知给提单人
4. 添加 `buildNotification()` 和 `sendNotify()` 私有方法（从 CsTaskServiceImpl 复制/提取）

## Task 2: 新增 BPM 任务 ID 解析端点

前端持有的是 cs_task ID，而 `/bpm/task/transfer` 需要 BPM Flowable 任务 ID。新增轻量解析端点。

**Controller**: `yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/controller/admin/cs/CsTaskController.java`
```java
@GetMapping("/bpm-task-id")
@Operation(summary = "获取工单当前运行的 BPM 任务 ID")
@PreAuthorize("@ss.hasPermission('dealer:cs-task:transfer')")
public CommonResult<String> getBpmTaskId(@RequestParam("id") Long id)
```

**Service**: `CsTaskService` 接口 + `CsTaskServiceImpl` 实现
- 校验工单存在 + 状态 = IN_PROGRESS
- 调用 `findCurrentBpmTaskId(processInstanceId)` 返回 BPM 任务 ID
- 不符合条件返回 null（前端禁用转单按钮）

## Task 3: 前端 API 新增 getBpmTaskId 方法

**文件**: `yudao-ui/yudao-ui-admin-vue3/src/api/opshub/csTask/index.ts`
```typescript
export const getBpmTaskId = async (csTaskId: number) => {
  return await request.get({ url: '/opshub/cs-task/bpm-task-id', params: { id: csTaskId } })
}
```

## Task 4: 前端调用方迁移

**task-detail.vue** (`submitTransfer` 方法，约 line 411):
1. 先调 `CsTaskApi.getBpmTaskId(taskId.value)` 获取 BPM 任务 ID
2. 若返回 null，提示"当前工单状态不支持转单"
3. 调 `TaskApi.transferTask({ id: bpmTaskId, assigneeUserId: transferForm.newAssigneeId, reason: transferForm.reason })`
4. import BPM 的 TaskApi

**TaskTab.vue** (`submitTransfer` 方法，约 line 406):
- 同上模式，先解析 BPM 任务 ID 再调用 BPM 转派

## Task 5: 旧端点处理 — 移除重复通知

**文件**: `yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/service/cs/impl/CsTaskServiceImpl.java`

保留旧 `/opshub/cs-task/transfer` 端点作为降级兜底，但：
- 移除 `transferTask()` 中的 WebSocket 推送和站内信调用（line 221-224），因为 `bpmTaskService.transferTask()` 会触发 TASK_ASSIGNED → 监听器已处理通知
- 这样避免双路径同时活跃时的重复通知

## Task 6: 权限对齐

确保持有 `dealer:cs-task:transfer` 的角色同时拥有 `bpm:task:update` 权限。

创建 SQL 迁移脚本：
```sql
-- 查找拥有 dealer:cs-task:transfer 权限的角色，授予 bpm:task:update
INSERT INTO system_role_menu (role_id, menu_id)
SELECT rm.role_id, m.id
FROM system_menu m
CROSS JOIN (SELECT DISTINCT role_id FROM system_role_menu WHERE menu_id = 
  (SELECT id FROM system_menu WHERE permission = 'dealer:cs-task:transfer')) rm
WHERE m.permission = 'bpm:task:update'
ON DUPLICATE KEY UPDATE id = id;
```

## Task 7: 更新测试

- **保留** `CsTaskServiceImplTest.TransferTaskTests` 现有测试（旧端点仍存在）
- **新增** `CsTaskBpmAssignedListenerTest`：
  - 转单场景：assignee 变更 + IN_PROGRESS → TYPE_TASK_TRANSFERRED 通知
  - 接单场景：PENDING → IN_PROGRESS → TYPE_TASK_ACCEPTED 通知
  - 已关闭工单：跳过同步

## 验证方案

1. **单元测试**: 运行 `CsTaskBpmAssignedListenerTest` 和 `CsTaskServiceImplTest`
2. **端到端验证**:
   - 在工单详情页点击转单，选择新处理人
   - 验证浏览器 Network 调用了 `PUT /bpm/task/transfer`
   - 验证 cs_task 表 assignee_id 已更新
   - 验证新处理人收到 WebSocket 通知和站内信
3. **降级验证**: 直接调用 `POST /opshub/cs-task/transfer` 仍然正常工作（旧端点兜底）

## 关键文件清单

| 文件 | 操作 |
|------|------|
| `CsTaskBpmAssignedListener.java` | 增强 - 添加通知逻辑 |
| `CsTaskController.java` | 新增 - `/bpm-task-id` 端点 |
| `CsTaskService.java` | 新增 - `getBpmTaskId()` 方法 |
| `CsTaskServiceImpl.java` | 新增 `getBpmTaskId()` 实现 + 移除 `transferTask()` 重复通知 |
| `src/api/opshub/csTask/index.ts` | 新增 - `getBpmTaskId()` |
| `task-detail.vue` | 修改 - `submitTransfer()` 改用 BPM API |
| `TaskTab.vue` | 修改 - `submitTransfer()` 改用 BPM API |
| `BpmTaskController.java` | **不改** |
| `BpmTaskServiceImpl.java` | **不改** |
| SQL 迁移脚本 | 新增 - 权限对齐 |
