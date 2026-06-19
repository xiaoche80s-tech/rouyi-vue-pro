# PRD-Step18-工单跨产品线可见性修复与标签优化

## 概述

修复客服工单（ops_cs_task）两类问题：①跨产品线分配场景下的数据可见性 Bug——在数据权限层增加"处理人旁路"机制；②执行员待办/已办 status 赋值不正确——移除不合理状态值，新增"已交付"标签页，使执行员工作流更清晰。

## 变更背景

1. **工单不可见 Bug**：经销商为产品线 P 创建工单，该产品线无配置执行员，BPM 流程将工单分配给执行员 A（A 的产品线范围不含 P）。执行员 A 查看待办/已办时，数据权限追加 `product_line_code IN ('A的产品线') OR product_line_code IS NULL`，工单的 `product_line_code='P'` 不匹配，导致工单不可见。
2. **影响范围广泛**：该 Bug 不仅影响工单列表查询（`selectPage`），还影响标签计数统计（`selectCountByTab`）、工单详情查看（`selectById`）、工单操作（接单/转单/提交审批等 `validateTaskExists`），以及首页仪表盘统计。
3. **操作请求同病**：操作请求表 `ops_cs_opreq` 也注册了产品线数据权限，存在相同的跨产品线不可见风险。
4. **执行员待办已办 status 赋值不正确**：当前执行员"待办"包含 PENDING(0)（BPM 自动分配后不会出现）、"已办"包含 DELIVERED(2)（工单仍在流转中不算已办），导致标签语义混乱。

## 变更内容

### 1. 处理人旁路机制（P0）

**改动文件**：
- `yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/framework/datapermission/rule/DealerDataPermissionRule.java` — 增加处理人旁路逻辑

**设计逻辑**：

在 `DealerDataPermissionRule` 中新增"处理人旁路"（Assignee Bypass）注册机制：

- 新增 `assigneeBypassTables` 集合，记录需要旁路的表名
- 新增 `addAssigneeBypass(String tableName)` 注册方法
- 修改 `buildDealerExpression` 和 `buildProductLineExpression`：当表注册了旁路时，在原始数据权限条件后追加 `OR assignee_id = currentUserId`
- 新增 `buildAssigneeBypassOrExpression` 辅助方法，构建 `(原始条件) OR assignee_id = {userId}` 并用括号包裹避免优先级问题

**修复前 SQL**：
```sql
WHERE assignee_id = A AND status IN (0,1,4)
AND (product_line_code IN ('GK') OR product_line_code IS NULL)
-- product_line_code='FK' 的工单被过滤掉 → 不可见
```

**修复后 SQL**：
```sql
WHERE assignee_id = A AND status IN (0,1,4)
AND ((product_line_code IN ('GK') OR product_line_code IS NULL) OR assignee_id = A)
-- assignee_id=A 的工单始终可见
```

**关键设计点**：
- 旁路仅在表注册了 `addAssigneeBypass` 时生效，不影响签约/订单/售后等其他表
- `super_admin` 角色返回 null（不过滤），旁路不生效，管理员查询不受影响
- 旁路条件使用 `Parenthesis` 包裹，避免与外层 AND 产生 SQL 优先级问题
- 即使产品线/经销商权限为空（`__NO_ACCESS__` 永假条件），旁路仍生效

### 2. 注册旁路表配置（P0）

**改动文件**：
- `yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/framework/datapermission/config/OpshubDataPermissionConfiguration.java` — 注册旁路表

**设计逻辑**：

在现有 Step 6 数据权限配置之后，追加旁路注册：

```java
// 处理人旁路：被分配的处理人始终能看到分配给自己的工单
rule.addAssigneeBypass("ops_cs_task");
rule.addAssigneeBypass("ops_cs_opreq");
```

### 3. 执行员待办已办 status 修正（P0）

**改动文件**：
- `yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/service/cs/impl/CsTaskServiceImpl.java` — 修改 `applyTabFilter` 方法
- `yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/dal/mysql/cs/CsTaskMapper.java` — 修改 `selectCountByTab` 方法
- `yudao-ui/yudao-ui-admin-vue3/src/views/opshub/customerservice/components/TaskTab.vue` — 修改 handler 子标签配置

**设计逻辑**：

**问题现状**（执行员 viewScope="assignee"）：

| 标签 | 当前 status | 问题 |
|------|------------|------|
| 可领取 claimable | status=0 且 assignee IS NULL | ✓ 正确 |
| 待办 pending | status IN (0,1,4) | ✗ PENDING(0) 不应出现（BPM 自动分配后自动转 IN_PROGRESS） |
| 已办 done | status IN (2,3) | ✗ DELIVERED(2) 仍在流转（等待经销商验收），不算已办 |

**修正后**：

| 标签 | 修正后 status | 说明 |
|------|-------------|------|
| 可领取 claimable | status=0 且 assignee IS NULL | 未分配工单 |
| 待办 pending | status IN (1, 4) | 执行员正在处理 + 退回待重处理 |
| 已交付 delivered | status=2 | **新增标签** — 已提交审批，等待经销商验收 |
| 已办 done | status=3 | 工单已关闭 |

**前端 handler 子标签配置修改**：
```typescript
handler: [
  { value: 'claimable', label: '可领取' },
  { value: 'pending', label: '待办' },
  { value: 'delivered', label: '已交付' },  // 新增
  { value: 'done', label: '已办' }
]
```

**后端 `applyTabFilter` 修改**（assignee scope）：
```java
case "assignee" -> {
    switch (tabFilter) {
        case "claimable" -> {
            reqVO.setStatusList(List.of(CsTaskStatusEnum.PENDING.getCode()));
            reqVO.setUnassigned(true);
        }
        case "pending" -> {
            // 修正：移除 PENDING(0)，只保留 IN_PROGRESS + REJECTED
            reqVO.setAssigneeId(currentUserId);
            reqVO.setStatusList(List.of(
                    CsTaskStatusEnum.IN_PROGRESS.getCode(),
                    CsTaskStatusEnum.REJECTED.getCode()));
        }
        case "delivered" -> {
            // 新增：已交付标签
            reqVO.setAssigneeId(currentUserId);
            reqVO.setStatusList(List.of(
                    CsTaskStatusEnum.DELIVERED.getCode()));
        }
        case "done" -> {
            // 修正：移除 DELIVERED(2)，只保留 CLOSED
            reqVO.setAssigneeId(currentUserId);
            reqVO.setStatusList(List.of(
                    CsTaskStatusEnum.CLOSED.getCode()));
        }
    }
}
```

**后端 `selectCountByTab` 同步修改**（assignee scope）：
```java
case "assignee" -> {
    switch (tabFilter) {
        case "claimable" -> { /* 不变 */ }
        case "pending" -> {
            wrapper.eq(CsTaskDO::getAssigneeId, currentUserId);
            wrapper.in(CsTaskDO::getStatus, java.util.List.of(1, 4)); // 移除 0
        }
        case "delivered" -> {
            wrapper.eq(CsTaskDO::getAssigneeId, currentUserId);
            wrapper.in(CsTaskDO::getStatus, java.util.List.of(2));
        }
        case "done" -> {
            wrapper.eq(CsTaskDO::getAssigneeId, currentUserId);
            wrapper.in(CsTaskDO::getStatus, java.util.List.of(3)); // 移除 2
        }
    }
}
```

**`getTabCounts` 补充 delivered 计数**：
```java
counts.put("delivered", csTaskMapper.selectCountByTab("delivered", viewScope, loginUserId));
```

### 4. 单元测试（P1）

**改动文件**：
- `yudao-module-opshub/src/test/java/cn/iocoder/yudao/module/opshub/framework/datapermission/rule/DealerDataPermissionRuleTest.java` — 新增或补充测试

**设计逻辑**：

新增以下测试场景：
- 执行员（产品线=['GK']）查询已分配给自己的跨产品线工单（product_line_code='FK'），应可见
- 执行员（产品线=['GK']）查询非分配给自己的跨产品线工单，应不可见
- 执行员无产品线配置时，查询已分配给自己的工单，应可见
- 经销商角色查询不受旁路影响（经销商无 assignee_id 旁路）
- 超管不受旁路影响（返回 null，不过滤）

## API 接口定义

本 Step 不涉及新增 API 接口。修复通过数据权限层自动生效，覆盖以下已有接口的查询结果：

| 接口 | 路径 | 修复效果 |
|------|------|--------|
| 工单分页 | `GET /opshub/cs-task/page` | 跨产品线分配的工单对处理人可见；执行员标签 status 修正 |
| 工单详情 | `GET /opshub/cs-task/get` | 处理人可查看跨产品线工单详情 |
| 标签计数 | `GET /opshub/cs-task/tab-counts` | 新增 delivered 计数；统计包含跨产品线工单 |
| 工单操作 | `POST /opshub/cs-task/accept` 等 | `validateTaskExists` 不再报"工单不存在" |

## DDL 变更

无。

## DML 变更

无。

## 涉及文件清单

### 后端（yudao-module-opshub）

| 文件 | 操作 |
|------|------|
| `framework/datapermission/rule/DealerDataPermissionRule.java` | 修改 — 增加处理人旁路逻辑 |
| `framework/datapermission/config/OpshubDataPermissionConfiguration.java` | 修改 — 注册旁路表 |
| `service/cs/impl/CsTaskServiceImpl.java` | 修改 — applyTabFilter status 修正 + getTabCounts 新增 delivered |
| `dal/mysql/cs/CsTaskMapper.java` | 修改 — selectCountByTab status 修正 |
| `framework/datapermission/rule/DealerDataPermissionRuleTest.java` | 新增/修改 — 补充旁路测试 |

### 前端（yudao-ui/yudao-ui-admin-vue3）

| 文件 | 操作 |
|------|------|
| `views/opshub/customerservice/components/TaskTab.vue` | 修改 — handler 子标签新增 delivered |

### DDL/DML

无数据库变更。

## 已知限制

1. **根本原因未解决**：本修复解决了"看不到工单"的表象问题。根本原因"BPM 将工单分配给不属于该产品线的执行员"属于 BPM 流程候选人配置问题，建议在 BPM 流程定义中限制候选人范围为该产品线对应的执行员。
2. **consult_session 未覆盖**：咨询会话表 `ops_cs_session` 也注册了产品线数据权限且有 `assignee_id` 字段，如存在类似的 BPM 分配场景，建议后续补充旁路注册。

## 剩余待做（P2/P3）

| 优先级 | 任务 | 说明 |
|-------|------|------|
| P2 | BPM 流程候选人优化 | 在 ops-cs-task 流程定义中限制候选人范围为产品线对应执行员 |
| P2 | ops_cs_session 旁路 | 评估咨询会话是否需要同样的处理人旁路机制 |
| P3 | 数据权限审计日志 | 记录数据权限旁路生效的查询，便于安全审计 |
