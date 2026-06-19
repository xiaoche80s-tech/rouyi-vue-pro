# 工单管理 Tab 流程问题修复方案

## Context

工单管理是客户服务页面的第一个 Tab，核心流程为：**经销商提单 → 执行员接单/抢单 → 处理 → 提交审批 → BPM 审批 → 经销商验收 → 关闭**。经全链路分析识别出 10 个流程问题，已逐一确认修复方案。

## 已确认的架构决策

| 决策项 | 结论 |
|--------|------|
| BPM 定位 | **审批驱动交付**：`deliverTask` 改为"提交审批"推动 BPM，由 BPM APPROVE 回调设 DELIVERED |
| 接单模式 | **混合模式**：创建时按角色+产品线查找默认处理人，找不到则 assigneeId=null（抢单） |
| 取消机制 | 经销商（PENDING 可取消）+ 管理员（任意非 CLOSED 可关闭），同步取消 BPM |
| SLA | **完整实现**：定时扫描 + WebSocket/站内信预警 + 前端标识 |
| 工单时间线 | **复用 BPM 历史**：通过 processInstanceId 查询 BPM Activity 历史 + Comment |
| 转单 | **走 BPM 转派**：复用 BPM `transferTask` 机制，自动记录到 BPM Comment 历史 |
| 催办 | **独立接口**：发送 WebSocket + 站内信通知处理人，不进入 BPM 历史 |
| 默认处理人 | 创建时按角色+产品线自动查找，找不到则 null |
| 验收回调 | **Spring Event 事件驱动**：验收通过后发布 `CsTaskVerifiedEvent`，各业务模块按分类监听处理 |

---

## 工单列表三角色详细设计

工单列表的数据过滤由三层叠加实现：

```
Layer 1: DealerDataPermissionRule（MyBatis 拦截器，自动追加 WHERE 条件）
    ↓
Layer 2: resolveViewScope（Service 层，用户级可见性过滤）
    ↓
Layer 3: 前端筛选条件（状态/紧急度/分类等用户手动筛选）
```

### 角色可见性矩阵

| 角色 | Layer 1: 数据权限 WHERE | Layer 2: viewScope WHERE | 最终可见范围 |
|------|---------------------|------------------------|----------|
| **经销商** (dealer) | `dealer_code IN ('HK','ZS') OR dealer_code IS NULL` | `creator_user_id = 当前用户` | 仅看自己提的工单，且必须在授权经销商范围内 |
| **执行员** (service_executor) | `product_line_code IN ('GK','FK') OR product_line_code IS NULL` | `status=0 OR assignee_id = 当前用户` | 授权产品线内的待接单工单（抢单池）+ 自己负责的工单 |
| **品牌管理员** (brand_admin) | `product_line_code IN ('GK','FK') OR product_line_code IS NULL` | 无额外过滤（all） | 授权产品线内的全部工单 |
| **品牌销售员** (brand_sales) | `product_line_code IN ('GK','FK') OR product_line_code IS NULL` | 无额外过滤（all） | 授权产品线内的全部工单 |
| **超级管理员** (super_admin) | 无过滤 | 无额外过滤（all） | 全部工单 |

### 经销商 (dealer) 视角

**Layer 1 — DealerDataPermissionRule**：
- 角色命中 `OpsRoleCodeConstants.DEALER` → 走经销商维度过滤
- 自动追加 `WHERE dealer_code IN ('HK','ZS',...)` — 只看授权经销商的工单
- 需追加 `OR dealer_code IS NULL` — 无经销商关联的通用工单也可见

**Layer 2 — resolveViewScope**：
- 返回 `"creator"` → Mapper 追加 `WHERE creator_user_id = 当前用户`
- 效果：经销商只能看到**自己提的工单**

**前端操作按钮**：
| 工单状态 | 可见按钮 | 说明 |
|---------|---------|------|
| PENDING(0) | 取消、催办 | 经销商可取消自己提的未接单工单 |
| IN_PROGRESS(1) | 催办 | 可催促执行员 |
| DELIVERED(2) | 验收通过、退回 | 经销商验收自己提的工单 |
| REJECTED(4) | 无 | 等待执行员重新处理 |
| CLOSED(3) | 无 | 工单已结束 |

**前端创建工单**：
- 可见“提工单”按钮（权限 `dealer:cs-task:create`）
- assigneeId 自动根据分类+产品线查找，查不到则 null（抢单）
- dealerCode 自动填充当前用户的经销商编码

### 执行员 (service_executor) 视角

**Layer 1 — DealerDataPermissionRule**：
- 角色命中 `OpsRoleCodeConstants.SERVICE_EXECUTOR` → 走产品线维度过滤
- 自动追加 `WHERE product_line_code IN ('GK','FK',...)` — 只看授权产品线
- 需追加 `OR product_line_code IS NULL` — 无产品线关联的通用工单也可见

**Layer 2 — resolveViewScope**：
- 返回 `"assignee"` → Mapper 追加 `WHERE (status = 0 OR assignee_id = 当前用户)`
- 效果：看到**产品线范围内的所有待接单工单**（抢单池）+ **自己负责的工单**

**前端操作按钮**：
| 工单状态 | 可见按钮 | 条件 |
|---------|---------|------|
| PENDING(0) | 接单 | 仅当 assigneeId=null（抢单）或 assigneeId=当前用户（指定） |
| IN_PROGRESS(1) | 提交审批、转单 | 仅当 assigneeId=当前用户 |
| DELIVERED(2) | 无 | 等待经销商验收 |
| REJECTED(4) | 重新处理 | 仅当 assigneeId=当前用户 |
| CLOSED(3) | 无 | 工单已结束 |

**前端创建工单**：
- 可见“提工单”按钮（如果有 `dealer:cs-task:create` 权限）
- 通常执行员不创建工单，由经销商提单

### 管理员 (brand_admin / super_admin) 视角

**Layer 1 — DealerDataPermissionRule**：
- super_admin → `buildPermissionData` 返回 `all=true` → 无数据权限过滤
- brand_admin → 走产品线维度过滤，同执行员

**Layer 2 — resolveViewScope**：
- 返回 `"all"` → 无额外用户级过滤
- 效果：管理员看到**产品线范围内的全部工单**（brand_admin）或**全部工单**（super_admin）

**前端操作按钮**：
| 工单状态 | 可见按钮 | 说明 |
|---------|---------|------|
| PENDING(0) | 取消（管理员关闭） | 管理员可关闭任意工单 |
| IN_PROGRESS(1) | 取消（管理员关闭） | 管理员可关闭任意工单 |
| 所有非 CLOSED | 催办 | 管理员可催办 |

### Layer 1 缺陷修复（增强现有 DealerDataPermissionRule）

**当前问题**：`DealerDataPermissionRule` 的 `buildProductLineExpression` 和 `buildDealerExpression` 不包含 `OR IS NULL`，导致产品线/经销商编码为空的工单被过滤掉。

**方案**：增强现有 Rule，支持 per-table 的 `includeNull` 标记，不影响其他模块。

**修改文件**：
- `DealerDataPermissionRule.java` — 新增 includeNull 标记和重载方法
- `OpshubDataPermissionConfiguration.java` — 客服表注册时标记 includeNull

**具体改动**：

1. **DealerDataPermissionRule** 新增 `includeNull` 集合和重载方法：
```java
// 新增集合：记录哪些表需要包含 NULL 值
private final Set<String> productLineIncludeNull = new HashSet<>();
private final Set<String> dealerIncludeNull = new HashSet<>();

// 新增重载方法
public void addProductLineColumn(String tableName, boolean includeNull) {
    addProductLineColumn(tableName);
    if (includeNull) productLineIncludeNull.add(tableName);
}
public void addDealerColumn(String tableName, boolean includeNull) {
    addDealerColumn(tableName);
    if (includeNull) dealerIncludeNull.add(tableName);
}
```

2. **buildProductLineExpression** 改为条件性追加 OR IS NULL：
```java
private Expression buildProductLineExpression(String tableName, Alias tableAlias, Set<String> productLineCodes) {
    ...
    InExpression inExpr = new InExpression(column, new ParenthesedExpressionList(...));
    if (productLineIncludeNull.contains(tableName)) {
        return new OrExpression(inExpr, new IsNullExpression(column, false));
    }
    return inExpr;  // 其他表保持原行为
}
```

3. **OpshubDataPermissionConfiguration** 客服表注册时标记 includeNull：
```java
// Step 6：客服工单表和操作请求表 —— 允许产品线/经销商为空
rule.addDealerColumn("ops_cs_task", true);
rule.addProductLineColumn("ops_cs_task", true);
rule.addDealerColumn("ops_cs_opreq", true);
rule.addProductLineColumn("ops_cs_opreq", true);

// Step 7：咨询会话表 —— 允许产品线/经销商为空
rule.addDealerColumn("ops_cs_session", true);
rule.addProductLineColumn("ops_cs_session", true);
```

4. 其他表（签约/订单/售后/基础数据）保持原有调用方式，不受影响

### Layer 2 可见性适配（抢单模式）

**当前实现**：`resolveViewScope` 对执行员返回 `"assignee"`，Mapper 过滤 `status=0 OR assignee_id=currentUserId`。

**需适配**：抢单模式下，执行员看到 `assigneeId=null` 的 PENDING 工单（可抢单），也应看到 `assigneeId=自己` 的非 PENDING 工单。当前实现已支持，但需补充：
- 对 PENDING 且 `assigneeId!=null` 且 `assigneeId!=currentUserId` 的工单，不应显示“接单”按钮
- 这是前端 Task 4 的角色校验范围

### 前端角色获取与按钮控制

前端通过 `useUserStore` 获取角色信息：
```typescript
import { useUserStore } from '@/store/modules/user'
const userStore = useUserStore()
const roles = userStore.getRoles()       // ['service_executor'] 等
const currentUserId = userStore.getUser().id
```

已有 `v-hasRole` 指令可用：
```html
<el-button v-hasRole="['service_executor']">接单</el-button>
```

操作按钮的完整判断逻辑：
```typescript
// 判断当前用户角色
const isDealer = roles.includes('dealer')
const isExecutor = roles.includes('service_executor')
const isAdmin = roles.includes('brand_admin') || roles.includes('super_admin')

// 每行操作按钮可见性计算
const canAccept = (row) => row.status === 0 && isExecutor 
  && (row.assigneeId === null || row.assigneeId === currentUserId)
const canSubmitApproval = (row) => row.status === 1 && row.assigneeId === currentUserId
const canTransfer = (row) => row.status === 1 && row.assigneeId === currentUserId
const canVerify = (row) => row.status === 2 && row.creatorUserId === currentUserId
const canReprocess = (row) => row.status === 4 && row.assigneeId === currentUserId
const canCancel = (row) => (row.status === 0 && row.creatorUserId === currentUserId) || isAdmin
const canUrge = (row) => row.status !== 3
```

---

## Task 1: 重构 BPM 状态机 — 统一为审批驱动交付（P0）

**问题**：手动操作和 BPM 回调双轨管理状态，存在竞态冲突
**目标**：所有状态变更统一走 BPM 流程，消除双轨冲突

**修改文件**：
- `CsTaskServiceImpl.java` — 重构状态变更方法
- `CsTaskService.java` — 更新接口定义
- `CsTaskController.java` — 调整 API 语义
- `TaskTab.vue` — 前端适配新接口
- `csTask/index.ts` — API 层适配

**具体改动**：

1. **`deliverTask` → `submitForApproval`**（提交审批）
   - 不再直接设 DELIVERED，改为推动 BPM 流程到审批节点
   - 调用 `processInstanceApi` 完成当前 BPM 用户任务，推动流程前进
   - 业务状态保持 IN_PROGRESS（等待审批）

2. **BPM 回调 `updateCsTaskStatusByBpm`** 成为唯一的状态终态设置入口
   - APPROVE → DELIVERED（审批通过 = 交付）
   - REJECT → REJECTED（审批拒绝 = 退回）
   - CANCEL → CLOSED（流程取消 = 关闭）
   - 添加 `@Transactional`

3. **`verifyTask` 改为 BPM 验收节点**
   - 验收通过：推动 BPM 验收到结束节点 → BPM 回调设 CLOSED
   - 验收不通过：推动 BPM 退回 → BPM 回调设 REJECTED

4. **状态变更使用 CAS 乐观锁**
   - Mapper 新增 `updateStatusByIdAndStatus(id, oldStatus, newStatus)` 方法
   - 所有状态变更使用 `WHERE id = ? AND status = ?` 原子更新

5. **前端适配**
   - "交付" 按钮改为 "提交审批"
   - "验收通过/退回" 保持现有 UI，但后端走 BPM 节点

---

## Task 2: 修复工单号生成并发问题（P0）

**问题**：`generateTaskNo()` read-then-write 竞态，DDL 缺少唯一索引

**修改文件**：
- `CsTaskServiceImpl.java` L303-307 — 改为加锁生成
- `CsTaskMapper.java` L63-78 — SQL 加 `FOR UPDATE`
- DDL 文件 — 补充唯一索引

**具体改动**：
1. `selectMaxSeqToday` SQL 加 `FOR UPDATE` 行锁
2. DDL 补充：`CREATE UNIQUE INDEX uk_ops_cs_task_no ON ops_cs_task(task_no) WHERE deleted = 0`
3. Service 层增加唯一约束冲突重试（最多 3 次）

---

## Task 3: 实现混合接单模式（P0）

**问题**：acceptTask 无条件覆盖 assigneeId，且无角色校验

**修改文件**：
- `CsTaskServiceImpl.java` — 重构 createCsTask + acceptTask
- `CsTaskCreateReqVO.java` — assigneeId 改为可选
- 新建处理人查找工具方法

**具体改动**：

1. **创建工单时自动查找默认处理人**
   - 根据 `productLineCode` + `category` 查找拥有对应产品线权限的 `service_executor` 角色用户
   - 找到唯一匹配 → `assigneeId` = 该用户（指定模式）
   - 找不到 / 多个匹配 → `assigneeId` = null（抢单模式）
   - 前端也可手动覆盖 assigneeId

2. **`acceptTask` 混合校验**
   ```java
   if (task.getAssigneeId() != null) {
       // 指定模式：仅指定处理人可接单
       validateIsAssignee(task, currentUserId);
   }
   // 抢单模式：任何执行员可接单
   // 校验角色：必须是 service_executor
   validateIsExecutor(currentUserId);
   ```

3. **`CsTaskCreateReqVO.assigneeId`** 从 `@NotNull` 改为可选

---

## Task 4: 前端操作按钮角色校验（P1）

**问题**：操作按钮仅按状态+权限显示，不区分角色/身份

**修改文件**：
- `TaskTab.vue` — 操作列增加角色判断，引入 `useUserStore`

**具体改动**：

1. 引入 `useUserStore` 获取角色和 userId，定义可见性计算函数（见上方“前端角色获取与按钮控制”）
2. 操作按钮替换为 `v-if="canXxx(row)"` 判断：

| 按钮 | 可见条件 |
|------|----------|
| 接单 | `canAccept(row)` — status=0 AND isExecutor AND (assigneeId=null OR assigneeId=me) |
| 提交审批 | `canSubmitApproval(row)` — status=1 AND assigneeId=me |
| 转单 | `canTransfer(row)` — status=1 AND assigneeId=me |
| 验收通过/退回 | `canVerify(row)` — status=2 AND creatorUserId=me |
| 重新处理 | `canReprocess(row)` — status=4 AND assigneeId=me |
| 取消 | `canCancel(row)` — (status=0 AND creatorUserId=me) OR isAdmin |
| 催办 | `canUrge(row)` — status!=3 |

3. 修复 `handleVerify` 函数：移除无用 `passed` 参数，验收通过走独立函数

---

## Task 5: BPM 回调补充通知（P1）

**问题**：`updateCsTaskStatusByBpm` 仅更新状态不推送通知

**修改文件**：
- `CsTaskServiceImpl.java` L359-386

**具体改动**：
1. 添加 `@Transactional(rollbackFor = Exception.class)`
2. APPROVE → 通知经销商（提单人）验收 + 站内信
3. REJECT → 通知处理人被退回 + 站内信
4. CANCEL → 通知双方工单已关闭 + 站内信

---

## Task 6: 新增取消/关闭接口（P2）

**修改文件**：
- `CsTaskService.java` — 新增 `cancelTask(id, reason)` 接口
- `CsTaskServiceImpl.java` — 实现取消逻辑
- `CsTaskController.java` — 新增 `/cancel` API
- `TaskTab.vue` — 新增"取消"按钮
- `csTask/index.ts` — 新增 `cancelTask` API

**具体改动**：
1. 经销商：仅 PENDING 状态 + 仅提单人可取消
2. 管理员：任意非 CLOSED 状态可关闭
3. 取消时同步取消 BPM 流程实例：`processInstanceApi.cancelProcessInstance(processInstanceId)`
4. 发送取消通知给相关方

---

## Task 7: 工单详情 + BPM 时间线（P2）

**修改文件**：
- `TaskTab.vue` — 操作列新增"详情"按钮
- 新建 `TaskDetailDrawer.vue` — 工单详情抽屉组件
- `CsTaskController.java` — 新增 `/detail` 接口（含 BPM 审批历史）

**具体改动**：
1. 详情抽屉包含：基本信息面板 + BPM 审批记录时间线
2. 通过 `processInstanceId` 查询 BPM 历史活动（`getActivityListByProcessInstanceId`）
3. 复用/参考 `ProcessViewer.vue` 的审批记录展示组件
4. 转单操作走 BPM 转派后自动记录到 BPM Comment 历史

---

## Task 8: 完整实现 SLA 监控告警（P2）

**修改文件**：
- 新建 `CsTaskSlaJob.java` — SLA 扫描定时任务
- `CsTaskServiceImpl.java` — 新增 SLA 状态计算
- `CsTaskMapper.java` — 新增 SLA 相关查询
- `TaskTab.vue` — 列表增加 SLA 状态标识

**具体改动**：
1. 定时任务每 5 分钟扫描：
   - 预警：`status != CLOSED AND sla_deadline BETWEEN now() AND now()+30min` → 发送 `TYPE_SLA_WARNING`
   - 告警：`status != CLOSED AND sla_deadline < now()` → 发送 `TYPE_SLA_ALERT` + 升级紧急程度
2. 前端列表用颜色标识：绿色正常 / 黄色预警 / 红色超时
3. 新增 `slaStatus` 计算字段（0=正常, 1=预警, 2=超时）

---

## Task 9: 修复 reprocessTask 通知（P2）

**修改文件**：
- `CsTaskServiceImpl.java` L231-251
- `CsTaskNotification.java` — 新增 `TYPE_TASK_REPROCESS` 常量

**具体改动**：
1. 新增 WebSocket 通知类型 `TYPE_TASK_REPROCESS = "cs-task-reprocessed"`
2. 新增站内信模板 `cs-task-reprocessed`
3. `reprocessTask` 使用正确类型 + 补充站内信通知给提单人

---

## Task 10: 验收后按分类触发业务回调（P1）

**问题**：当前验收通过后仅更新工单状态为 CLOSED，不触发任何业务模块的后续处理。

**方案**：采用 Spring Event 事件驱动，工单模块发布事件，各业务模块监听并处理。复用 BPM 模块已有的 `BpmProcessInstanceStatusEvent` + `ApplicationListener` 模式。

**新建文件**：
- `service/cs/event/CsTaskVerifiedEvent.java` — 工单验收事件
- `service/cs/listener/CsTaskVerifiedEventListener.java` — 抽象监听器基类

**修改文件**：
- `CsTaskServiceImpl.java` — 验收/关闭时发布事件

**具体改动**：

1. **新建 `CsTaskVerifiedEvent`**（参考 `BpmProcessInstanceStatusEvent` 模式）：
```java
public class CsTaskVerifiedEvent extends ApplicationEvent {
    private Long taskId;
    private String taskNo;
    private Integer category;      // 工单分类
    private String sourceModule;   // 来源模块
    private boolean passed;        // true=验收通过, false=退回
    private Long dealerCode;       // 经销商编码
    private String productLineCode;
}
```

2. **新建抽象监听器 `CsTaskVerifiedEventListener`**：
```java
public abstract class CsTaskVerifiedEventListener implements ApplicationListener<CsTaskVerifiedEvent> {
    @Override
    public final void onApplicationEvent(CsTaskVerifiedEvent event) {
        // 子类可覆盖 getCategory() 过滤关心的分类
        if (getCategory() != null && !getCategory().equals(event.getCategory())) return;
        onVerified(event);
    }
    protected abstract Integer getCategory();
    protected abstract void onVerified(CsTaskVerifiedEvent event);
}
```

3. **在 `updateCsTaskStatusByBpm` 和 `cancelTask` 中发布事件**：
```java
// BPM 回调状态变为 CLOSED 时（验收通过或管理员取消）
if (newStatus == CLOSED) {
    applicationEventPublisher.publishEvent(buildVerifiedEvent(task, true));
}
```

4. **各业务模块按需实现监听器**（示例）：
- `SigningTaskVerifiedListener`：签约工单验收通过 → 更新签约进度状态
- `AftersaleTaskVerifiedListener`：售后工单验收通过 → 更新售后单状态
- `OrderTaskVerifiedListener`：订单工单验收通过 → 更新订单相关状态

---

## Task 11: Mapper 可见性过滤适配（P3）

**修改文件**：
- `TaskTab.vue` — 筛选栏按角色适配

**具体改动**：
- 执行员角色：筛选状态下拉中隐藏"待接单"选项（因为 assignee scope 已包含 PENDING）
- 或改为：执行员筛选 status=0 时，自动切换为"我可抢单的工单"语义

---

## 关键文件清单

| 文件 | 涉及 Task |
|------|----------|
| `yudao-module-opshub/.../service/cs/impl/CsTaskServiceImpl.java` | 1,2,3,5,6,8,9 |
| `yudao-module-opshub/.../service/cs/CsTaskService.java` | 1,6 |
| `yudao-module-opshub/.../dal/mysql/cs/CsTaskMapper.java` | 1,2,8 |
| `yudao-module-opshub/.../controller/admin/cs/CsTaskController.java` | 1,6,7 |
| `yudao-module-opshub/.../service/cs/listener/CsTaskStatusListener.java` | 1 |
| `yudao-module-opshub/.../controller/admin/cs/vo/CsTaskCreateReqVO.java` | 3 |
| `yudao-module-opshub/.../dal/dataobject/cs/CsTaskDO.java` | 1 |
| `yudao-module-opshub/.../framework/datapermission/rule/DealerDataPermissionRule.java` | Layer1 includeNull 增强 |
| `yudao-module-opshub/.../framework/datapermission/config/OpshubDataPermissionConfiguration.java` | 客服表注册 includeNull |
| `yudao-module-opshub/.../service/cs/event/CsTaskVerifiedEvent.java` | 10（新建） |
| `yudao-module-opshub/.../service/cs/listener/CsTaskVerifiedEventListener.java` | 10（新建） |
| `yudao-ui/.../views/opshub/customerservice/components/TaskTab.vue` | 1,4,7,8,11 |
| `yudao-ui/.../api/opshub/csTask/index.ts` | 1,6 |
| `db/branches/feature_step6-客户服务模块/feature_step6-客户服务模块_ddl.sql` | 2,8 |

## 验证方式

1. **Task 1**：创建工单 → 接单 → 提交审批 → BPM 审批通过 → 确认状态变为 DELIVERED → 验收通过 → 确认状态变为 CLOSED
2. **Task 2**：并发创建工单，确认编号不重复
3. **Task 3**：创建工单（有默认处理人）→ 确认仅指定人可接单；创建工单（无默认处理人）→ 确认任意执行员可接单
4. **Task 4**：以经销商角色登录 → 确认看不到"接单"按钮；以非处理人执行员登录 → 确认看不到"交付"按钮
5. **Task 5**：BPM 审批通过 → 确认经销商收到 WebSocket + 站内信通知
6. **Task 6**：经销商取消 PENDING 工单 → 确认 BPM 流程同步取消
7. **Task 7**：点击详情 → 确认展示 BPM 审批时间线
8. **Task 8**：创建 SLA 即将到期的工单 → 确认 5 分钟内收到预警通知
9. **Task 9**：退回后重新处理 → 确认提单人收到正确类型的通知
11. **Task 11**：执行员筛选状态 → 确认不会意外过滤 PENDING 工单
12. **Task 10**：验收通过后，确认对应分类的监听器被触发（如签约工单验收 → 签约进度更新）
