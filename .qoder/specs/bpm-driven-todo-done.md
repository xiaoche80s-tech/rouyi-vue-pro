# 工单待办/已办查询改为 BPM 工作流驱动

## Context

当前 `CsTaskServiceImpl` 中执行员的待办/已办查询完全基于数据库 status 字段硬编码过滤：
- 待办：`assignee_id = me AND status IN (IN_PROGRESS, REJECTED)`
- 已办：`assignee_id = me AND status IN (CLOSED)`

这导致工单的"待办/已办"判定与 BPM 流程状态脱节。需要改为：先通过 Flowable BPM 查询当前用户的待办/已办 `processInstanceId` 集合，再用 `WHERE process_instance_id IN (...)` 反查 `ops_cs_task` 表，并叠加经销商产品线数据权限。

## 涉及文件（6 个）

| 文件 | 变更 |
|------|------|
| `yudao-module-bpm/.../service/task/BpmTaskService.java` | 新增 2 个接口方法 |
| `yudao-module-bpm/.../service/task/BpmTaskServiceImpl.java` | 实现 2 个新方法 |
| `yudao-module-opshub/.../controller/admin/cs/vo/CsTaskPageReqVO.java` | 新增 `processInstanceIds` 字段 |
| `yudao-module-opshub/.../dal/mysql/cs/CsTaskMapper.java` | `selectPage` 增加 processInstanceIds 过滤 |
| `yudao-module-opshub/.../service/cs/impl/CsTaskServiceImpl.java` | 改造 `applyTabFilter` 中 pending/done 分支 |
| `yudao-module-opshub/.../service/cs/impl/CsTaskServiceImplTest.java` | 补充单元测试 |

## Task 1：BpmTaskService 新增接口方法

在 `BpmTaskService.java` 接口中新增：

```java
/** 获取用户待办任务的流程实例 ID 列表（不分页） */
List<String> getTodoProcessInstanceIds(Long userId, String processDefinitionKey);

/** 获取用户已办任务的流程实例 ID 列表（不分页，排除发起人节点） */
List<String> getDoneProcessInstanceIds(Long userId, String processDefinitionKey);
```

## Task 2：BpmTaskServiceImpl 实现

```java
@Override
public List<String> getTodoProcessInstanceIds(Long userId, String processDefinitionKey) {
    TaskQuery query = taskService.createTaskQuery()
            .taskAssignee(String.valueOf(userId))
            .active()
            .taskTenantId(FlowableUtils.getTenantId());
    if (StrUtil.isNotEmpty(processDefinitionKey)) {
        query.processDefinitionKey(processDefinitionKey);
    }
    List<Task> tasks = query.list();
    // 提取 processInstanceId 并去重
}

@Override
public List<String> getDoneProcessInstanceIds(Long userId, String processDefinitionKey) {
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
            .finished()
            .taskAssignee(String.valueOf(userId));
    if (StrUtil.isNotEmpty(processDefinitionKey)) {
        query.processDefinitionKey(processDefinitionKey);
    }
    List<HistoricTaskInstance> tasks = query.list();
    // 过滤掉 START_USER_NODE_ID，提取 processInstanceId 并去重
}
```

## Task 3：CsTaskPageReqVO 扩展

新增隐藏字段：
```java
@Schema(description = "流程实例 ID 列表（Service 层从 BPM 查询填充）", hidden = true)
private List<String> processInstanceIds;
```

## Task 4：CsTaskMapper.selectPage 增加过滤

在 `selectPage` 方法中，`unassigned` 过滤之后新增：
```java
if (CollUtil.isNotEmpty(reqVO.getProcessInstanceIds())) {
    wrapper.in(CsTaskDO::getProcessInstanceId, reqVO.getProcessInstanceIds());
} else if (reqVO.getProcessInstanceIds() != null) {
    // BPM 无匹配任务 → 永假条件返回空结果
    wrapper.eq(CsTaskDO::getProcessInstanceId, "__NO_BPM_TASK__");
}
```

**注意**：`selectCountByTab` 保持不变（仍用 status 过滤），因为：
- 避免每次页面加载多 2 次 Flowable 查询
- `updateCsTaskStatusByBpm` 回调确保最终一致性
- 角标数字短暂延迟可接受

## Task 5：CsTaskServiceImpl.applyTabFilter 改造

仅修改 assignee scope 下的 `pending` 和 `done` 分支：

```java
case "pending" -> {
    // BPM 驱动：查询 Flowable 待办 → processInstanceId 列表
    List<String> ids = bpmTaskService.getTodoProcessInstanceIds(currentUserId, PROCESS_KEY);
    reqVO.setProcessInstanceIds(ids);
    reqVO.setAssigneeId(currentUserId); // 防御性冗余
    // 不再设置 statusList
}
case "done" -> {
    // BPM 驱动：查询 Flowable 已办 → processInstanceId 列表
    List<String> ids = bpmTaskService.getDoneProcessInstanceIds(currentUserId, PROCESS_KEY);
    reqVO.setProcessInstanceIds(ids);
    reqVO.setAssigneeId(currentUserId);
    // 不再设置 statusList
}
```

**不变的部分**：`claimable`、`delivered`、`creator scope`、`all scope` 保持原逻辑。

## Task 6：数据权限

无需额外变更。`DealerDataPermissionRule` 已注册 `ops_cs_task` 表：
- `dealer_code` + `product_line_code` 过滤（`includeNull=true`）
- `assignee_id` 旁路（处理人始终可见自己的工单）

最终 SQL 叠加效果：
```sql
WHERE process_instance_id IN ('pi-1','pi-2')   -- BPM 驱动
  AND assignee_id = #{userId}                    -- 冗余过滤
  AND (product_line_code IN (...) OR product_line_code IS NULL OR assignee_id = #{userId})  -- 数据权限
```

## 边界情况

| 场景 | 处理 |
|------|------|
| BPM 无待办 | `processInstanceIds=[]` → 永假条件 → 空结果 |
| 历史工单无 processInstanceId | 不出现在 BPM 驱动结果中，但在 "all" 标签可见 |
| 标签计数与列表短暂不一致 | 计数用 status、列表用 BPM，回调到达后一致 |

## 验证方式

1. `mvn clean compile -pl yudao-module-bpm,yudao-module-opshub` — 编译通过
2. `mvn test -pl yudao-module-opshub -Dtest=CsTaskServiceImplTest` — 单元测试通过
3. 启动应用后调用 `GET /opshub/cs-task/page?tabFilter=pending` 验证返回结果基于 BPM 流程实例过滤
