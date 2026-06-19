# PRD-Step11-工单流程优化

## 概述

Step 11 针对客户服务模块中的工单管理 tab 进行流程问题修复，涵盖 BPM 状态机重构、并发安全、角色权限、事件回调等核心改进。

## 变更背景

工单管理 tab 存在以下核心问题：
1. `deliverTask` 直接设置状态，未推动 BPM 流程前进，导致状态机与 BPM 脱节
2. 工单号生成无并发保护，高并发下可能出现重复工单号
3. 接单模式不支持指定/抢单混合模式
4. 前端操作按钮未按角色做细粒度控制
5. BPM 回调后缺少通知和事件驱动机制

## 变更内容

### 1. BPM 状态机重构（P0）

**改动文件**：
- `CsTaskServiceImpl.java` — `deliverTask` → `submitForApproval`，推动 BPM 前进
- `CsTaskServiceImpl.java` — `updateCsTaskStatusByBpm` 作为统一终态入口
- `CsTaskService.java` — 接口同步更新
- `CsTaskController.java` — `/deliver` → `/submit-for-approval`

**设计逻辑**：
- APPROVE 按当前业务状态推进：IN_PROGRESS → DELIVERED，DELIVERED → CLOSED
- REJECT 设为 REJECTED
- CANCEL 设为 CLOSED
- 已 CLOSED 不再处理（幂等保护）

### 2. 工单号并发安全（P0）

**改动文件**：
- `CsTaskMapper.java` — `selectMaxSeqToday` 添加 `FOR UPDATE` 行锁
- `CsTaskServiceImpl.java` — `generateTaskNo` 加 3 次重试 + 唯一性校验
- DDL — `ops_cs_task` 添加部分唯一索引 `uk_ops_cs_task_no`

### 3. 混合接单模式（P0）

**改动文件**：
- `CsTaskCreateReqVO.java` — `assigneeId` 改为可选（`@Nullable`）
- `CsTaskServiceImpl.java` — `acceptTask` 分支：指定模式 vs 抢单模式

**校验逻辑**：
```java
if (task.getAssigneeId() != null) {
    // 指定模式：仅指定人可接单
    validateIsAssignee(task, currentUserId);
}
// 抢单模式（assigneeId 为空）：任何执行员可接单
```

### 4. 前端角色控制（P1）

**改动文件**：
- `TaskTab.vue` — 引入 `useUserStore`，按角色/用户ID 控制按钮可见性
- `index.ts`（API）— `deliverTask` → `submitForApproval`，新增 `cancelTask`

**可见性规则**：
| 操作 | 可见条件 |
|------|---------|
| 接单 | 状态=PENDING && 执行员角色 && (指定模式=本人 \|\| 抢单模式) |
| 提交审批 | 状态=IN_PROGRESS && 处理人是当前用户 |
| 验收 | 状态=DELIVERED && 创建人是当前用户 |
| 取消 | 管理员=任意状态可取消；创建人=仅PENDING可取消 |

### 5. BPM 回调通知（P1）

**改动文件**：
- `CsTaskServiceImpl.java` — 新增 `sendBpmCallbackNotification`、`publishStatusChangeEvent`
- `CsTaskNotification.java` — 新增 `TYPE_TASK_REPROCESS = 8`

**通知模板**：
| 回调 | 码 | 模板 |
|------|------|------|
| APPROVE→DELIVERED | 1100004005 | 【客户服务】您的工单{taskNo}已验收通过 |
| APPROVE→CLOSED | 1100004006 | 【客户服务】您的工单{taskNo}已完成关闭 |
| REJECT | 1100004007 | 【客户服务】工单{taskNo}验收不通过：{reason} |
| CANCEL | 1100004008 | 【客户服务】工单{taskNo}已关闭：{reason} |

### 6. 取消/关闭接口（P2）

**改动文件**：
- `CsTaskController.java` — 新增 `POST /cancel`
- `CsTaskServiceImpl.java` — 新增 `cancelTask` 方法

**权限**：
- PENDING 状态：工单创建人可取消
- 其他状态：仅管理员可关闭
- 取消后同步调用 `BpmProcessInstanceService.cancelProcessInstance` 关闭 BPM 流程

### 7. 验收事件回调（P1）

**新建文件**：
- `event/CsTaskStatusChangeEvent.java` — Spring Event
- `event/CsTaskStatusChangeEventListener.java` — 抽象监听器基类

**使用方式**：
各业务模块继承 `CsTaskStatusChangeEventListener`，按 `category` 过滤处理：
```java
@Component
public class BasicDataVerifyListener extends CsTaskStatusChangeEventListener {
    @Override
    protected Integer getCategory() { return 1; } // 仅处理基础数据工单
    
    @Override
    protected void onStatusChange(CsTaskStatusChangeEvent event) {
        if (CLOSED.equals(event.getNewStatus())) {
            // 基础数据状态置为已验证
        }
    }
}
```

### 8. 数据权限 NULL 修复（Layer 1）

**改动文件**：
- `DealerDataPermissionRule.java` — 新增 `includeNull` 集合，条件性追加 `OR IS NULL`
- `OpshubDataPermissionConfiguration.java` — 客服表注册时指定 `includeNull=true`

**原理**：客服工单的 `dealer_code`/`product_line_code` 可能为空（未分配经销商/产品线），`includeNull=true` 时管理员可看到所有工单（含空值），不破坏签约/订单/售后模块的严格权限。

## DDL 变更

新增文件：`db/branches/feature_step11-工单流程优化/feature_step11-工单流程优化_ddl.sql`

```sql
CREATE UNIQUE INDEX uk_ops_cs_task_no ON ops_cs_task (task_no) WHERE deleted = 0;
```

## 涉及文件清单

### 后端（yudao-module-opshub）
| 文件 | 操作 |
|------|------|
| `CsTaskServiceImpl.java` | 重构（BPM 集成、取消、事件） |
| `CsTaskService.java` | 接口更新 |
| `CsTaskController.java` | 端点重命名 + 新增 |
| `CsTaskCreateReqVO.java` | assigneeId 可选 |
| `CsTaskMapper.java` | FOR UPDATE + CAS 方法 |
| `CsTaskNotification.java` | 新增 TYPE_TASK_REPROCESS |
| `DealerDataPermissionRule.java` | includeNull 增强 |
| `OpshubDataPermissionConfiguration.java` | 客服表 includeNull=true |
| `OpsRoleCodeConstants.java` | 新增 SUPER_ADMIN |
| `event/CsTaskStatusChangeEvent.java` | **新建** |
| `event/CsTaskStatusChangeEventListener.java` | **新建** |

### 前端（yudao-ui/yudao-ui-admin-vue3）
| 文件 | 操作 |
|------|------|
| `views/opshub/customerservice/components/TaskTab.vue` | 角色控制 + 取消按钮 |
| `api/opshub/csTask/index.ts` | 接口重命名 + 新增 |

### DDL
| 文件 | 操作 |
|------|------|
| `db/branches/feature_step11-工单流程优化/feature_step11-工单流程优化_ddl.sql` | **新建** |

## 剩余待做（P2/P3）

| 优先级 | 任务 | 说明 |
|-------|------|------|
| P2 | Task 7: 工单详情 + BPM 时间线 | 新建 TaskDetailDrawer.vue，复用 BPM 审批记录 |
| P2 | Task 8: SLA 监控告警 | 定时任务扫描 SLA 即将超期工单 |
| P3 | Task 11: Mapper 可见性适配 | 前端筛选栏按角色动态调整 |
