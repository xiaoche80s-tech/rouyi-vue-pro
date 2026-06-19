# PRD-Step13-工单管理增强与数据权限修复

## 概述

Step 13 对客服工单管理模块进行多项增强，涵盖 TaskTab 组件架构重构、处理人信息同步、子标签角标计数、工单详情弹窗、BPM 审批处理人联动，以及 JSqlParser 5.2 数据权限兼容性修复。

## 变更背景

工单管理在实际使用中存在以下问题：
1. TaskTab 内部硬编码角色检测（useUserStore），扩展性差，新增角色需改动组件
2. 工单列表"处理人"列始终为空，assigneeName 未被解析
3. BPM 流程路由后工单表 assigneeId 未同步，状态停留在"待接单"
4. 子标签（可领取/待办/已办）缺少任务数量角标
5. 工单编号不可点击，无法快速查看详情
6. 执行员提交审批后，下一岗处理人未同步到工单表
7. DealerDataPermissionRule 的 `OrExpression` 缺少括号，且 `Parenthesis.withExpression()` 在 JSqlParser 5.2 中有 bug

## 变更内容

### 1. TaskTab side prop 重构（P0）

**改动文件**：
- `TaskTab.vue` — 新增 `defineProps<{side: 'initiator' | 'handler' | 'admin'}>()`
- `customerservice/index.vue` — `<TaskTab side="handler" />`
- `workorder-service/index.vue` — `<TaskTab side="initiator" />`

**设计逻辑**：
- 删除 `isDealer`/`isExecutor`/`isAdmin`/`roles` 等内部角色检测代码
- 保留 `useUserStore` 仅用于获取 `currentUserId`
- 子标签改为静态 `subTabsMap` 按 side 取值
- `canAccept` → `props.side === 'handler'`
- `canCancel` → `props.side === 'admin'`
- 提工单按钮加 `v-if="side !== 'handler'"`
- 未来新增发起方角色（如医院）只需传 `<TaskTab side="initiator" />`，组件零改动

### 2. 处理人姓名解析（P0）

**改动文件**：
- `CsTaskController.java` — 注入 `AdminUserApi`，新增 `fillUserNames()` 方法

**实现方式**：
- 在 `getCsTaskPage()` 和 `getCsTask()` 返回前调用 `fillUserNames()`
- 批量收集 `assigneeId` + `creatorUserId`，通过 `adminUserApi.getUserMap()` 一次查询
- 填充 `CsTaskRespVO.assigneeName` 和 `CsTaskRespVO.creatorUserName`

### 3. BPM 处理人同步（P0）

**改动文件**：
- `CsTaskServiceImpl.java` — 提取 `syncBpmAssignee()` 方法

**核心逻辑**：
```java
private void syncBpmAssignee(Long taskId, String processInstanceId, boolean autoInProgress) {
    List<Task> bpmTasks = bpmTaskService.getTasksByProcessInstanceIds(
            Collections.singletonList(processInstanceId));
    if (CollUtil.isNotEmpty(bpmTasks)) {
        String bpmAssignee = bpmTasks.get(0).getAssignee();
        if (bpmAssignee != null) {
            Long bpmAssigneeId = Long.parseLong(bpmAssignee);
            CsTaskDO updateDO = new CsTaskDO().setId(taskId).setAssigneeId(bpmAssigneeId);
            if (autoInProgress) {
                updateDO.setStatus(CsTaskStatusEnum.IN_PROGRESS.getCode())
                        .setAcceptTime(LocalDateTime.now());
            }
            csTaskMapper.updateById(updateDO);
        }
    }
}
```

**调用时机**：
| 场景 | autoInProgress | 说明 |
|------|---------------|------|
| `createCsTask()` | true | 创建后自动设为处理中 + 接单时间 |
| `submitForApproval()` | false | 提交审批后仅更新下一岗处理人 |

### 4. 子标签角标计数（P1）

**改动文件**：
- `CsTaskController.java` — 新增 `GET /tab-counts` 端点
- `CsTaskService.java` — 新增 `getTabCounts()` 接口
- `CsTaskServiceImpl.java` — 实现 `getTabCounts()`
- `CsTaskMapper.java` — 新增 `selectCountByTab()` 方法
- `csTask/index.ts` — 新增 `getTabCounts()` API
- `TaskTab.vue` — 使用 `<el-badge>` 展示角标

**后端**：
- `selectCountByTab(tabFilter, viewScope, currentUserId)` 复用与 `selectPage` 相同的可见性/标签过滤逻辑
- `getTabCounts()` 返回 `Map<String, Long>`：`{all, pending, claimable, done}`

**前端**：
- `<el-tab-pane>` 使用 `#label` 插槽，内嵌 `<el-badge :value="count" :max="999" />`
- 每次 `getList()` 后自动刷新计数
- 数量 > 0 才显示角标

### 5. 工单编号超链接 → 详情弹窗（P1）

**改动文件**：
- `TaskTab.vue` — 工单编号列改为 `<el-button link type="primary">`
- `TaskTab.vue` — 新增工单详情 `<el-dialog>`，内嵌 `<TaskDetail>` 组件

**实现方式**：
- 点击工单编号 → 设置 `detailTaskId` → 打开 Dialog
- Dialog 设置 `destroy-on-close` + `v-if="detailDialogVisible"`，确保每次打开重新加载数据
- 复用已有的 `task-detail.vue` 组件，通过 `:id` prop 传入工单 ID

### 6. 数据权限 Parenthesis 修复（P0）

**改动文件**：
- `DealerDataPermissionRule.java`

**问题一：OrExpression 缺少括号**
- 原代码：`return new OrExpression(inExpr, new IsNullExpression(column))`
- 数据权限拦截器用 `AndExpression` 拼接后，`AND` 优先级高于 `OR`，导致条件被拆分
- 生成 SQL：`... AND product_line_code IN (...) OR product_line_code IS NULL`（错误）

**问题二：JSqlParser 5.2 Parenthesis.withExpression() bug**
- `Parenthesis.setExpression()` 内部调用 `ArrayList.set(0, expr)` 但列表为空
- 直接抛出 `IndexOutOfBoundsException: Index 0 out of bounds for length 0`

**修复方案**：
```java
// 使用 Parenthesis.add() 代替有 bug 的 withExpression()
Parenthesis p = new Parenthesis();
p.add(new OrExpression(inExpr, new IsNullExpression(column)));
return p;
```
- 生成 SQL：`... AND (product_line_code IN (...) OR product_line_code IS NULL)`（正确）

**问题三：EqualsTo(null, null) 不安全**
- JSqlParser 5.2 中 `new EqualsTo(null, null)` 生成 `null = null`，可能导致序列化异常
- 改为 `new EqualsTo(column, new StringValue("__NO_ACCESS__"))` 生成 `column = '__NO_ACCESS__'`（永假条件）

## 涉及文件清单

### 后端（yudao-module-opshub）
| 文件 | 操作 |
|------|------|
| `CsTaskController.java` | 新增 `fillUserNames()`、`/tab-counts` 端点 |
| `CsTaskService.java` | 新增 `getTabCounts()` 接口 |
| `CsTaskServiceImpl.java` | 提取 `syncBpmAssignee()`、实现 `getTabCounts()`、`submitForApproval()` 增加同步 |
| `CsTaskMapper.java` | 新增 `selectCountByTab()` 方法 |
| `DealerDataPermissionRule.java` | Parenthesis 修复 + EqualsTo 安全替代 |

### 前端（yudao-ui/yudao-ui-admin-vue3）
| 文件 | 操作 |
|------|------|
| `views/opshub/customerservice/components/TaskTab.vue` | side prop 重构 + 角标 + 详情弹窗 |
| `views/opshub/customerservice/index.vue` | 传入 `side="handler"` |
| `views/opshub/workorder-service/index.vue` | 传入 `side="initiator"` |
| `api/opshub/csTask/index.ts` | 新增 `getTabCounts()` API |

## 已知限制

1. `Parenthesis` 类在 JSqlParser 5.2 中标记为 `@Deprecated(since = "5.1")`，但 `add()` 方法仍可正常工作；若后续 JSqlParser 移除此类，需改用 `ParenthesedExpressionList` 替代
2. 角标计数与列表查询为两次独立请求，极端并发下数字可能与实际列表条数有短暂不一致
