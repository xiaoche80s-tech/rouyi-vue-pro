# Step18 — 工单跨产品线可见性修复与标签优化

## Context

修复 ops_cs_task 客服工单两类问题：①数据权限导致执行员看不到跨产品线分配的工单；②执行员待办/已办 status 赋值不正确。PRD 见 `docs/PRD-Step18-工单跨产品线可见性修复与标签优化.md`。

---

## Task 1: DealerDataPermissionRule 增加处理人旁路机制

**修改文件**：`yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/framework/datapermission/rule/DealerDataPermissionRule.java`

- 新增 `ASSIGNEE_COLUMN_NAME` 常量 + `assigneeBypassTables` Set 字段
- 新增 `addAssigneeBypass(String tableName)` 方法
- 修改 `buildDealerExpression`：注册旁路的表追加 `OR assignee_id = currentUserId`
- 修改 `buildProductLineExpression`：同上
- 新增 `buildAssigneeBypassOrExpression` 辅助方法

---

## Task 2: OpshubDataPermissionConfiguration 注册旁路表

**修改文件**：`yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/framework/datapermission/config/OpshubDataPermissionConfiguration.java`

- 在 Step 6 配置后追加 `rule.addAssigneeBypass("ops_cs_task")` 和 `rule.addAssigneeBypass("ops_cs_opreq")`

---

## Task 3: 执行员待办已办 status 修正

**修改文件**：
- `yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/service/cs/impl/CsTaskServiceImpl.java` — applyTabFilter + getTabCounts
- `yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/dal/mysql/cs/CsTaskMapper.java` — selectCountByTab

**applyTabFilter 修正**（assignee scope）：
- pending: `status IN (1, 4)` — 移除 PENDING(0)
- 新增 delivered: `status IN (2)`
- done: `status IN (3)` — 移除 DELIVERED(2)

**selectCountByTab 同步修正**（assignee scope）：同上 status 值

**getTabCounts 补充**：新增 `delivered` 计数

---

## Task 4: 前端 TaskTab 子标签配置

**修改文件**：`yudao-ui/yudao-ui-admin-vue3/src/views/opshub/customerservice/components/TaskTab.vue`

- handler 子标签配置新增 `{ value: 'delivered', label: '已交付' }`

---

## Task 5: 编译验证

- 后端：`mvn compile -pl yudao-module-opshub` 通过

---

## 实施顺序

| Task | 内容 | 依赖 |
|------|------|------|
| 1 | 数据权限旁路机制 | 无 |
| 2 | 注册旁路表 | Task 1 |
| 3 | status 修正（后端） | 无 |
| 4 | 前端子标签 | 无 |
| 5 | 编译验证 | Task 1-4 |

## 验证方式

| 验证项 | 操作 | 预期 |
|--------|------|------|
| 跨产品线可见性 | 执行员查看被分配的跨产品线工单 | 待办/已交付可见 |
| 非分配工单不可见 | 执行员查看非分配给自己的其他产品线工单 | 不可见 |
| 待办 status | 执行员待办标签 | 仅显示 IN_PROGRESS + REJECTED |
| 已交付标签 | 执行员已交付标签 | 仅显示 DELIVERED |
| 已办 status | 执行员已办标签 | 仅显示 CLOSED |
| 超管/经销商回归 | 其他角色查询 | 行为不变 |
