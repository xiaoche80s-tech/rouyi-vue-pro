# PRD-Step16-经销商取消审批中工单

## 概述

扩展经销商（提单人）的工单取消权限，使其在工单处于 PENDING（待接单）和 IN_PROGRESS（处理中/审批中）状态时均可取消工单。取消时 BPM 流程实例同步终止，工单状态变为 CLOSED。取消原因为选填。

## 变更背景

1. 当前经销商只能在工单 `PENDING`（待接单，status=0）状态下取消工单
2. 执行员接单后工单变为 `IN_PROGRESS`（处理中，status=1），此时经销商无法取消
3. 实际业务中，经销商可能在审批流程进行中发现需求变化（如客户撤回、重复提单等），需要在审批完成前取消工单
4. 当前 IN_PROGRESS 状态下 BPM 流程正在进行中，取消操作需同步终止 BPM 流程实例

## 变更内容

### 1. 扩展经销商取消权限范围（P0）

**改动文件**：
- `yudao-module-opshub/.../service/cs/impl/CsTaskServiceImpl.java` — 修改 `cancelTask` 方法权限校验
- `yudao-ui/yudao-ui-admin-vue3/.../customerservice/components/TaskTab.vue` — 修改 `canCancel` 可见性判断

**设计逻辑**：

**后端权限校验变更**：

当前逻辑（经销商）：
```
仅 PENDING + 仅提单人 → 允许取消
```

新逻辑（经销商）：
```
(PENDING 或 IN_PROGRESS) + 仅提单人 → 允许取消
```

管理员逻辑不变（任意非 CLOSED 状态均可取消）。

**关键代码变更**：
```java
// 变更前
if (!isAdmin) {
    validateStatus(task, CsTaskStatusEnum.PENDING);
    if (!isCreator) {
        throw exception(CS_TASK_NOT_CREATOR);
    }
}

// 变更后
if (!isAdmin) {
    // 仅 PENDING 或 IN_PROGRESS 可取消
    if (!CsTaskStatusEnum.PENDING.getCode().equals(task.getStatus())
        && !CsTaskStatusEnum.IN_PROGRESS.getCode().equals(task.getStatus())) {
        throw exception(CS_TASK_NOT_PENDING_OR_IN_PROGRESS);
    }
    if (!isCreator) {
        throw exception(CS_TASK_NOT_CREATOR);
    }
}
```

**新增错误码**：
```java
ErrorCode CS_TASK_NOT_PENDING_OR_IN_PROGRESS = new ErrorCode(1_050_008_008, "仅待接单或处理中状态可取消");
```

**前端可见性变更**：
```typescript
// 变更前
const canCancel = (row: any) => {
  if (row.status === 3) return false
  if (props.side === 'admin') return true
  return row.status === 0 && row.creatorUserId === currentUserId.value
}

// 变更后
const canCancel = (row: any) => {
  if (row.status === 3) return false
  if (props.side === 'admin') return true
  return (row.status === 0 || row.status === 1) && row.creatorUserId === currentUserId.value
}
```

**BPM 流程处理**：

取消时 BPM 流程处理逻辑保持不变（已有实现）：
1. 查找当前 BPM 任务节点
2. 调用 `bpmTaskService.rejectTask()` 终止 BPM 流程
3. 直接设置工单状态为 CLOSED
4. `CsTaskStatusListener` 收到 BPM CANCEL 回调时跳过已 CLOSED 的工单

### 2. 取消原因弹窗优化（P1）

**改动文件**：
- `yudao-ui/yudao-ui-admin-vue3/.../customerservice/components/TaskTab.vue` — 优化 `handleCancel` 方法

**设计逻辑**：

当前已有取消原因弹窗（`ElMessageBox.prompt`），原因为选填，无需改动。但可增加提示文案说明取消影响：

```typescript
const handleCancel = async (row: any) => {
  const confirmMsg = row.status === 1
    ? '取消后流程将终止，工单将被关闭。请输入取消原因（可选）'
    : '取消后工单将被关闭。请输入取消原因（可选）'
  const { value: reason } = await ElMessageBox.prompt(confirmMsg, '取消工单', {
    confirmButtonText: '确认取消',
    cancelButtonText: '返回',
    inputType: 'textarea'
  }).catch(() => { throw new Error('cancel') })
  try {
    await CsTaskApi.cancelTask(row.id, reason || undefined)
    ElMessage.success('工单已关闭'); getList()
  } catch (e) { /* 已处理 */ }
}
```

## 页面元素规格

### 工单管理页 — 取消按钮

#### Form 表单（输入项）

无新增表单。取消操作通过弹窗输入原因（选填）。

#### 接口输出项（展示项）

取消按钮可见性变化：

| 条件 | 变更前 | 变更后 |
|------|--------|--------|
| 经销商视图，status=0，是提单人 | 可见 | 可见（不变） |
| 经销商视图，status=1，是提单人 | 不可见 | **可见（新增）** |
| 经销商视图，status=2/3/4 | 不可见 | 不可见（不变） |
| 管理员视图，status≠3 | 可见 | 可见（不变） |

## API 接口定义

### 接口 1：取消/关闭工单（已有，无变更）

| 项 | 值 |
|------|------|
| 方法 | `POST` |
| 路径 | `/opshub/cs-task/cancel` |
| 权限 | `dealer:cs-task:cancel` |
| 说明 | 取消/关闭工单，终止 BPM 流程 |

**请求参数**（Query）：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 工单 ID |
| reason | String | 否 | 取消原因 |

**响应**（`CommonResult<Boolean>`）：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| data | Boolean | 操作结果 |

> 此接口无变更，仅后端校验逻辑调整。

## 涉及文件清单

### 后端（yudao-module-opshub）

| 文件 | 操作 |
|------|------|
| `src/main/java/.../service/cs/impl/CsTaskServiceImpl.java` | 修改：`cancelTask` 权限校验，允许 PENDING/IN_PROGRESS |
| `src/main/java/.../enums/ErrorCodeConstants.java` | 修改：新增 `CS_TASK_NOT_PENDING_OR_IN_PROGRESS` 错误码 |

### 前端（yudao-ui/yudao-ui-admin-vue3）

| 文件 | 操作 |
|------|------|
| `src/views/opshub/customerservice/components/TaskTab.vue` | 修改：`canCancel` 可见性 + `handleCancel` 提示文案 |

## 已知限制

1. 工单状态变为 DELIVERED（2，已交付/BPM 审批通过）后，经销商无法取消。如需取消，需联系管理员操作。
2. BPM 流程终止通过 `bpmTaskService.rejectTask()` 实现，如 BPM 引擎侧异常，仅记录 warn 日志不影响工单关闭。
3. 取消后工单状态直接设为 CLOSED（3），不经过 REJECTED（4）状态。
