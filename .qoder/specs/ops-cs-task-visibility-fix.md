# ops_cs_task 状态分析与待办已办可见性 Bug 修复

## Context

ops_cs_task 客服工单的状态流转依赖 BPM 引擎驱动，当产品线未配置执行员时，BPM 可能将工单分配给不属于该产品线的执行员。由于 `DealerDataPermissionRule` 数据权限规则对 `ops_cs_task` 表追加了产品线过滤条件，被分配的执行员在待办/已办标签页中看不到该工单。本方案在数据权限层增加"处理人旁路"机制，确保分配给自己的工单始终可见。

---

## 一、ops_cs_task 状态变化过程

```
PENDING(0) ──acceptTask/BPM分配──→ IN_PROGRESS(1) ──submitForApproval──→ DELIVERED(2) ──verifyTask(通过)──→ CLOSED(3)
    ↑                                    ↓                                    ↓
    │                              reprocessTask                     verifyTask(不通过)
    │                                    ↓                                    ↓
    └────────────────────────────── REJECTED(4) ←────────────────────────────┘
    
cancelTask: 任意非 CLOSED 状态 → CLOSED(3)
```

**状态枚举** (`CsTaskStatusEnum`):
| Code | 状态 | 说明 |
|------|------|------|
| 0 | PENDING | 待接单 — 工单刚创建，等待执行员接单 |
| 1 | IN_PROGRESS | 处理中 — 执行员已接单，正在处理 |
| 2 | DELIVERED | 已交付 — 执行员提交审批，等待经销商验收 |
| 3 | CLOSED | 已关闭 — 验收通过/管理员取消 |
| 4 | REJECTED | 已退回 — 经销商验收不通过 |

**关键流转节点**:

1. **创建** (`createCsTask`): status=PENDING(0)，发起 BPM 流程
2. **BPM 自动分配** (`CsTaskBpmAssignedListener`): 监听 TASK_ASSIGNED → 同步 assignee_id，若 PENDING→IN_PROGRESS
3. **手动接单** (`acceptTask`): 抢单模式，assigneeId=null 时任何执行员可接单
4. **转单** (`transferTask`): 状态不变，更新 assignee_id
5. **提交审批** (`submitForApproval`): 推动 BPM → BPM 回调 → DELIVERED(2)
6. **验收** (`verifyTask`): 通过→CLOSED(3)；不通过→REJECTED(4)
7. **重新处理** (`reprocessTask`): REJECTED(4) → IN_PROGRESS(1)
8. **取消** (`cancelTask`): 经销商仅 PENDING/IN_PROGRESS+提单人；管理员任意非 CLOSED
9. **BPM 回调** (`updateCsTaskStatusByBpm`): APPROVE→推进；REJECT→REJECTED；CANCEL→CLOSED

---

## 二、经销商与执行员待办已办数据方案

### 可见范围（resolveViewScope）

| 角色 | viewScope | 基础过滤 |
|------|-----------|---------|
| 经销商 | `creator` | `creator_user_id = me` |
| 执行员 | `assignee` | `status = 0 OR assignee_id = me` |
| 管理员 | `all` | 无额外过滤 |

### 子标签过滤

**执行员**:
| 标签 | 条件 |
|------|------|
| 全部 | `status=0 OR assignee_id=me` |
| 可领取 | `status=0 AND assignee_id IS NULL` |
| 待办 | `assignee_id=me AND status IN (0,1,4)` |
| 已办 | `assignee_id=me AND status IN (2,3)` |

**经销商**:
| 标签 | 条件 |
|------|------|
| 全部 | `creator_user_id=me` |
| 待办 | `creator_user_id=me AND status IN (2,4)` |

### 数据权限叠加

`DealerDataPermissionRule` 自动追加：
- 经销商角色：`dealer_code IN (...) OR dealer_code IS NULL`
- 执行员/品牌管理：`product_line_code IN (...) OR product_line_code IS NULL`

---

## 三、Bug 分析与修复方案

### Bug 根因

**触发**：产品线 P 无执行员 → BPM 分配给执行员 A（A 产品线范围不含 P）

```sql
-- 执行员 A 查看待办（A 产品线=['GK']，工单 product_line_code='P'）
WHERE assignee_id = A AND status IN (0,1,4)  -- ✓ 业务过滤匹配
AND (product_line_code IN ('GK') OR product_line_code IS NULL)  -- ✗ 数据权限过滤掉
→ 工单不可见
```

**影响范围**：selectPage、selectCountByTab、selectById（详情/操作校验）、首页仪表盘

### 修复方案：处理人旁路

为 `ops_cs_task` 注册旁路，数据权限追加 `OR assignee_id = currentUserId`：

```sql
-- 修复后
WHERE assignee_id = A AND status IN (0,1,4)
AND ((product_line_code IN ('GK') OR product_line_code IS NULL) OR assignee_id = A)
→ 分配给自己的工单始终可见 ✓
```

### Task 1: 修改 `DealerDataPermissionRule.java`

路径: `yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/framework/datapermission/rule/DealerDataPermissionRule.java`

1. 新增 `assigneeBypassTables` Set 字段 + `ASSIGNEE_COLUMN_NAME` 常量
2. 新增 `addAssigneeBypass(String tableName)` 方法
3. 修改 `buildDealerExpression`：注册旁路的表追加 `OR assignee_id = me`
4. 修改 `buildProductLineExpression`：同上
5. 新增 `buildAssigneeBypassOrExpression` 辅助方法

### Task 2: 修改 `OpshubDataPermissionConfiguration.java`

路径: `yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/framework/datapermission/config/OpshubDataPermissionConfiguration.java`

```java
rule.addAssigneeBypass("ops_cs_task");
rule.addAssigneeBypass("ops_cs_opreq");
```

### Task 3: 单元测试

为 `DealerDataPermissionRule` 新增/补充测试，覆盖旁路逻辑。

### Task 4: 验证

1. 正向：跨产品线分配工单，执行员待办可见
2. 反向：非分配给自己的其他产品线工单仍不可见
3. 回归：超管/经销商/品牌管理员查询不变
4. 统计：tab-counts 和首页数字正确
