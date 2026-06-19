# Fix: OpsHub 管理员转单时 BPM 同步失败

## Context

OpsHub 工单转单功能在管理员（`brand_admin`角色）操作时报错：
```
操作失败，原因：该任务的审批人不是你
```

**根因**：OpsHub 层和 BPM 层对"管理员"的定义不一致：

| 层级 | 管理员角色 | 代码位置 |
|------|-----------|---------|
| OpsHub (`CsTaskServiceImpl:185`) | `brand_admin`, `super_admin`, `process_admin` | opshub 模块 |
| BPM (`BpmTaskServiceImpl:1085`) | `process_admin`, `super_admin` | bpm 模块 |

当 `brand_admin` 用户执行转单时：
1. OpsHub 层通过（认为该用户是管理员）✅
2. 调用 `bpmTaskService.transferTask(currentUserId, bpmVO)` 时，BPM 层检查该用户是否有 `process_admin`/`super_admin` → 否 → 降级为 assignee 校验 → 用户不是 BPM 任务的 assignee → 抛异常 ❌
3. 异常被 catch 吞掉（仅 warn 日志），但 BPM 任务处理人**未实际更新**，导致业务数据与 BPM 状态不一致

## 修复方案

**不修改 BPM 共享模块**，在 OpsHub 层传入 BPM 任务当前 assignee 的 userId 来绕过 BPM 层的 assignee 校验。

### Task 1: 修改 `CsTaskServiceImpl.transferTask()`

**文件**: `yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/service/cs/impl/CsTaskServiceImpl.java`

在调用 `bpmTaskService.transferTask()` 时，判断当前用户是否是 BPM 任务的 assignee：
- 如果是 → 传 `currentUserId`（正常路径）
- 如果不是（管理员代转场景） → 传 BPM 任务当前 assignee 的 userId，让 BPM 层认为是 assignee 自己在转

```java
// 同步 BPM 流程中的任务处理人
if (task.getProcessInstanceId() != null) {
    try {
        String bpmTaskId = findCurrentBpmTaskId(task.getProcessInstanceId());
        if (bpmTaskId != null) {
            Task bpmTask = bpmTaskService.getTask(bpmTaskId);
            BpmTaskTransferReqVO bpmVO = new BpmTaskTransferReqVO();
            bpmVO.setId(bpmTaskId);
            bpmVO.setAssigneeUserId(reqVO.getNewAssigneeId());
            bpmVO.setReason(reqVO.getReason() != null ? reqVO.getReason() : "工单转单");

            // 决定传给 BPM 的操作人：
            // - 如果当前用户就是 BPM 任务 assignee，直接传 currentUserId
            // - 否则为管理员代转场景，传 BPM assignee 绕过校验
            Long bpmOperatorId = currentUserId;
            if (bpmTask != null && StrUtil.isNotBlank(bpmTask.getAssignee())
                    && !bpmTask.getAssignee().equals(currentUserId.toString())) {
                bpmOperatorId = NumberUtils.parseLong(bpmTask.getAssignee());
            }
            bpmTaskService.transferTask(bpmOperatorId, bpmVO);
        }
    } catch (Exception e) {
        log.warn("[transferTask][同步 BPM 转单失败 taskId={}]", reqVO.getId(), e);
    }
}
```

需要新增 import：
- `cn.hutool.core.util.NumberUtils`

### Task 2: 验证修复

- 用 `brand_admin` 角色用户登录，执行工单转单操作，验证 CsTask 和 BPM 均更新
- 用 `service_executor`（非管理员、非 assignee）登录，确认转单被拒绝
