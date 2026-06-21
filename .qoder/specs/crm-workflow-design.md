# CRM 模块工作流设计文档

## Context

本文档是对 `yudao-module-crm` 模块中工作流实现机制的完整设计分析。CRM 模块采用了**双轨工作流架构**：对于需要人工审批的场景（合同、回款）集成 BPM 引擎（Flowable），对于日常销售推进场景（商机阶段）使用自建的轻量状态流转机制。本文档旨在帮助开发者理解现有架构，为后续扩展和维护提供参考。

---

## 一、整体架构

### 1.1 双轨工作流概览

```
┌───────────────────────────────────────────────────────────────┐
│                       Controller 层                            │
│  CrmContractController / CrmReceivableController              │
│  CrmBusinessController / CrmBusinessStatusController          │
└──────────┬───────────────┬───────────────────┬────────────────┘
           │               │                   │
┌──────────▼───────────────▼───────────────────▼────────────────┐
│                       Service 层                                │
│  CrmContractServiceImpl    CrmReceivableServiceImpl            │
│  CrmBusinessServiceImpl    CrmBusinessStatusServiceImpl        │
└──────────┬───────────────┬───────────────────┬────────────────┘
           │               │                   │
    ┌──────▼─────┐  ┌──────▼──────┐    ┌──────▼──────────┐
    │ BPM API 调用│  │  Spring Event│    │ 直接 DB 状态更新  │
    │ (发起流程)  │  │  监听器      │    │ (无外部依赖)     │
    └──────┬─────┘  └──────┬──────┘    └──────┬──────────┘
           │               │                   │
    ┌──────▼───────────────▼──────┐    ┌──────▼──────────┐
    │    Flowable BPM 引擎         │    │  crm_business    │
    │  (yudao-module-bpm)         │    │  _status 配置表   │
    └─────────────────────────────┘    └─────────────────┘
```

### 1.2 两种机制定位

| 维度 | BPM 引擎集成 | 自建状态流转 |
|------|-------------|-------------|
| 适用场景 | 需要人工审批的流程（合同、回款） | 销售阶段的自然推进（商机） |
| 技术栈 | Flowable + Spring Event | 纯数据库状态字段 + Service 层逻辑 |
| 复杂度 | 高（跨模块依赖、事件驱动） | 低（模块内自治） |
| 灵活性 | 多级审批、会签、加签、驳回 | 线性阶段推进 + 终止状态 |

---

## 二、机制一：BPM 引擎集成（合同审批 & 回款审批）

### 2.1 核心组件

| 组件 | 文件路径 | 职责 |
|------|---------|------|
| 合同服务 | `yudao-module-crm/.../service/contract/CrmContractServiceImpl.java` | 合同 CRUD + 提交审批 + 审批结果回写 |
| 回款服务 | `yudao-module-crm/.../service/receivable/CrmReceivableServiceImpl.java` | 回款 CRUD + 提交审批 + 审批结果回写 |
| 合同监听器 | `yudao-module-crm/.../service/contract/listener/CrmContractStatusListener.java` | 监听 BPM 事件，回写合同审批状态 |
| 回款监听器 | `yudao-module-crm/.../service/receivable/listener/CrmReceivableStatusListener.java` | 监听 BPM 事件，回写回款审批状态 |
| 状态转换工具 | `yudao-module-crm/.../util/CrmAuditStatusUtils.java` | BPM 结果码 → CRM 审批状态映射 |
| 审批状态枚举 | `yudao-module-crm/.../enums/common/CrmAuditStatusEnum.java` | DRAFT/PROCESS/APPROVE/REJECT/CANCEL |

### 2.2 流程定义标识

- **合同审批**：`crm-contract-audit`（`CrmContractServiceImpl.BPM_PROCESS_DEFINITION_KEY`）
- **回款审批**：`crm-receivable-audit`（`CrmReceivableServiceImpl.BPM_PROCESS_DEFINITION_KEY`）

### 2.3 审批状态枚举 `CrmAuditStatusEnum`

| 枚举值 | 状态码 | 含义 | 触发时机 |
|--------|--------|------|---------|
| DRAFT | 0 | 草稿 | 创建时默认状态 |
| PROCESS | 10 | 审批中 | 用户提交审批后 |
| APPROVE | 20 | 审批通过 | BPM 流程结束且通过 |
| REJECT | 30 | 审批驳回 | BPM 流程结束且驳回 |
| CANCEL | 40 | 已取消 | BPM 流程取消 |

### 2.4 完整生命周期

#### 阶段一：创建（草稿）

创建合同/回款时，`auditStatus` 默认为 `DRAFT(0)`，不触发 BPM 流程。

```java
// CrmContractServiceImpl.createContract()
CrmContractDO contract = BeanUtils.toBean(createReqVO, CrmContractDO.class).setNo(no);
contractMapper.insert(contract);  // auditStatus 默认 null，前端展示为草稿
```

#### 阶段二：提交审批

用户手动提交，触发 BPM 流程实例创建：

```java
// CrmContractServiceImpl.submitContract()
// 1. 校验状态必须为 DRAFT
if (ObjUtil.notEqual(contract.getAuditStatus(), CrmAuditStatusEnum.DRAFT.getStatus())) {
    throw exception(CONTRACT_SUBMIT_FAIL_NOT_DRAFT);
}
// 2. 创建 BPM 流程实例
String processInstanceId = bpmProcessInstanceApi.createProcessInstance(userId,
    new BpmProcessInstanceCreateReqDTO()
        .setProcessDefinitionKey(BPM_PROCESS_DEFINITION_KEY)  // "crm-contract-audit"
        .setBusinessKey(String.valueOf(id)));
// 3. 更新状态为审批中，保存流程实例 ID
contractMapper.updateById(new CrmContractDO().setId(id)
    .setProcessInstanceId(processInstanceId)
    .setAuditStatus(CrmAuditStatusEnum.PROCESS.getStatus()));
```

#### 阶段三：BPM 引擎处理

Flowable 引擎接管流程，按照预定义的 BPMN 流程执行审批节点。CRM 模块不感知具体审批节点配置。

#### 阶段四：审批结果回写（事件驱动）

BPM 流程结束时，BPM 模块发布 `BpmProcessInstanceStatusEvent` Spring 事件：

```
BpmProcessInstanceStatusEvent 发布
        │
        ▼
BpmProcessInstanceStatusEventListener.onApplicationEvent()  [final 方法]
        │  检查 event.processDefinitionKey == getProcessDefinitionKey()
        │  不匹配则忽略
        ▼
CrmContractStatusListener.onEvent(event)
        │  解析 event.businessKey → contractId
        ▼
contractService.updateContractAuditStatus(id, bpmResult)
        │  1. 校验当前状态为 PROCESS
        │  2. CrmAuditStatusUtils.convertBpmResultToAuditStatus(bpmResult)
        │  3. 更新数据库 auditStatus
        ▼
完成
```

### 2.5 状态转换映射（CrmAuditStatusUtils）

```java
BpmTaskStatusEnum.APPROVE  →  CrmAuditStatusEnum.APPROVE(20)
BpmTaskStatusEnum.REJECT   →  CrmAuditStatusEnum.REJECT(30)
BpmTaskStatusEnum.CANCEL   →  BpmTaskStatusEnum.CANCEL(40)
```

### 2.6 事件路由机制

`BpmProcessInstanceStatusEventListener`（BPM 模块提供的抽象基类）采用**模板方法模式**：

```java
public abstract class BpmProcessInstanceStatusEventListener
        implements ApplicationListener<BpmProcessInstanceStatusEvent> {

    @Override
    public final void onApplicationEvent(BpmProcessInstanceStatusEvent event) {
        if (!StrUtil.equals(event.getProcessDefinitionKey(), getProcessDefinitionKey())) {
            return;  // 过滤无关事件
        }
        onEvent(event);  // 委托子类
    }

    protected abstract String getProcessDefinitionKey();
    protected abstract void onEvent(BpmProcessInstanceStatusEvent event);
}
```

新增审批业务只需：
1. 定义新的 `BPM_PROCESS_DEFINITION_KEY`
2. 新增一个 Listener 子类继承 `BpmProcessInstanceStatusEventListener`
3. 实现 `getProcessDefinitionKey()` 和 `onEvent()` 即可

### 2.7 业务约束规则

| 操作 | 约束条件 | 异常码 |
|------|---------|--------|
| 提交审批 | 仅 DRAFT 状态可提交 | `CONTRACT_SUBMIT_FAIL_NOT_DRAFT` |
| 编辑 | 仅 DRAFT 和 PROCESS 状态可编辑 | `CONTRACT_UPDATE_FAIL_NOT_DRAFT` |
| 删除 | APPROVE 状态不可删除 | `CONTRACT_DELETE_FAIL` |
| 审批结果回写 | 仅 PROCESS 状态可更新 | `CONTRACT_UPDATE_AUDIT_STATUS_FAIL_NOT_PROCESS` |
| 创建回款 | 关联合同必须已 APPROVE | `RECEIVABLE_CREATE_FAIL_CONTRACT_NOT_APPROVE` |
| 创建回款 | 金额不得超过合同剩余未回款金额 | `RECEIVABLE_CREATE_FAIL_PRICE_EXCEEDS_LIMIT` |

### 2.8 BPM 侧关键接口

| 接口/类 | 路径 | 说明 |
|---------|------|------|
| `BpmProcessInstanceApi` | `yudao-module-bpm/.../api/task/BpmProcessInstanceApi.java` | 创建流程实例 |
| `BpmProcessInstanceCreateReqDTO` | `yudao-module-bpm/.../api/task/dto/BpmProcessInstanceCreateReqDTO.java` | processDefinitionKey + businessKey + variables |
| `BpmProcessInstanceStatusEvent` | `yudao-module-bpm/.../api/event/BpmProcessInstanceStatusEvent.java` | Spring Event，含 id/processDefinitionKey/status/businessKey |
| `BpmProcessInstanceStatusEventListener` | `yudao-module-bpm/.../api/event/BpmProcessInstanceStatusEventListener.java` | 抽象监听器基类 |

---

## 三、机制二：自建商机状态流转

### 3.1 核心组件

| 组件 | 文件路径 | 职责 |
|------|---------|------|
| 商机服务 | `yudao-module-crm/.../service/business/CrmBusinessServiceImpl.java` | 商机 CRUD + 状态变更 |
| 状态配置服务 | `yudao-module-crm/.../service/business/CrmBusinessStatusServiceImpl.java` | 状态类型 CRUD 管理 |
| 状态类型实体 | `CrmBusinessStatusTypeDO` | 状态类型定义（如"标准销售流程"） |
| 状态项实体 | `CrmBusinessStatusDO` | 阶段项定义（如"初步接触"、"需求确认"） |
| 结束状态枚举 | `CrmBusinessEndStatusEnum` | WIN(1)/LOSE(2)/INVALID(3) |

### 3.2 数据模型

```
CrmBusinessStatusTypeDO (状态类型)
├── id
├── name (如："标准销售流程")
├── deptIds (适用部门)
│
└── CrmBusinessStatusDO (状态项列表, 按 sort 排序)
    ├── id
    ├── typeId (关联状态类型)
    ├── name (如："初步接触"、"需求确认"、"方案报价")
    └── sort (阶段排序号)
```

### 3.3 状态流转逻辑

```java
// CrmBusinessServiceImpl.updateBusinessStatus()
// 1. 校验商机存在
// 2. 校验 endStatus == null（已结束不可变更）
// 3. 校验目标状态属于该状态类型
// 4. 校验目标状态与当前不同
// 5. 直接更新：statusId + endStatus
businessMapper.updateById(new CrmBusinessDO().setId(reqVO.getId())
    .setStatusId(reqVO.getStatusId())
    .setEndStatus(reqVO.getEndStatus()));
```

### 3.4 结束状态枚举

| 枚举值 | 状态码 | 含义 |
|--------|--------|------|
| WIN | 1 | 赢单 |
| LOSE | 2 | 输单 |
| INVALID | 3 | 无效 |

结束状态为终态，一旦设置后不可再变更。

### 3.5 配置管理约束

- 状态类型名称全局唯一
- **已被商机引用的状态类型不可更新**（`BUSINESS_STATUS_UPDATE_FAIL_USED`）
- 已被使用的状态类型不可删除（`BUSINESS_STATUS_DELETE_FAIL_USED`）
- 创建商机时默认选择该状态类型的第一个阶段（sort=0）

---

## 四、设计模式总结

### 4.1 模板方法模式

**应用**：`BpmProcessInstanceStatusEventListener` 基类将事件过滤逻辑封装为 `final` 方法，子类只需实现 `getProcessDefinitionKey()` 和 `onEvent()`。

### 4.2 观察者模式（事件驱动）

**应用**：BPM 模块通过 Spring `ApplicationEvent` 发布流程状态变更，CRM 模块监听并处理。两个模块通过事件总线解耦，BPM 不感知 CRM 的存在。

### 4.3 适配器模式

**应用**：`CrmAuditStatusUtils` 将 BPM 的 `BpmTaskStatusEnum` 适配为 CRM 的 `CrmAuditStatusEnum`，实现两个领域的语义隔离。

### 4.4 AOP 切面

**应用**：
- `@CrmPermission`：方法级数据权限校验（READ/WRITE/OWNER）
- `@LogRecord`：操作日志自动记录，支持 SpEL 上下文变量

### 4.5 乐观状态锁

**应用**：`updateContractAuditStatus()` / `updateReceivableAuditStatus()` 先校验当前状态为 PROCESS 才允许更新，防止并发或重复事件导致非法状态转换。

---

## 五、关键文件索引

### CRM 模块

| 文件 | 行数 | 说明 |
|------|------|------|
| `service/contract/CrmContractServiceImpl.java` | 417 | 合同服务（BPM 集成） |
| `service/receivable/CrmReceivableServiceImpl.java` | 311 | 回款服务（BPM 集成） |
| `service/contract/listener/CrmContractStatusListener.java` | 32 | 合同审批监听器 |
| `service/receivable/listener/CrmReceivableStatusListener.java` | 32 | 回款审批监听器 |
| `service/business/CrmBusinessServiceImpl.java` | 386 | 商机服务（自建状态流转） |
| `service/business/CrmBusinessStatusServiceImpl.java` | 196 | 商机状态配置管理 |
| `util/CrmAuditStatusUtils.java` | 28 | BPM→CRM 状态转换工具 |
| `enums/common/CrmAuditStatusEnum.java` | 35 | 审批状态枚举 |
| `enums/business/CrmBusinessEndStatusEnum.java` | 46 | 商机结束状态枚举 |

### BPM 模块（API 层）

| 文件 | 行数 | 说明 |
|------|------|------|
| `api/task/BpmProcessInstanceApi.java` | 25 | 流程实例 API 接口 |
| `api/task/dto/BpmProcessInstanceCreateReqDTO.java` | 45 | 创建流程实例请求 DTO |
| `api/event/BpmProcessInstanceStatusEvent.java` | 47 | 流程状态变更事件 |
| `api/event/BpmProcessInstanceStatusEventListener.java` | 35 | 事件监听器抽象基类 |

---

## 六、风险与注意事项

| 风险点 | 说明 | 建议 |
|--------|------|------|
| BPM 事件丢失 | Spring Event 是内存事件，监听器未就绪时可能丢失 | 考虑引入 MQ 或补偿机制 |
| 状态不一致 | BPM 已有结果但 CRM 更新失败（DB 异常），两侧状态不一致 | 增加定时对账任务 |
| 审批中可编辑 | PROCESS 状态下仍可编辑，审批人可能基于旧数据审批 | 评估提交后锁定关键字段 |
| 商机状态类型锁定 | 一旦被引用即不可修改，影响业务调整 | 考虑增加"归档"机制 |
