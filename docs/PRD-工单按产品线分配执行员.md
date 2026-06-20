# 工单按产品线分配执行员方案

## 需求描述

工单任务按产品线分配到不同的执行员。如果产品线没有配置执行员，则走默认执行人（流程管理员，取第一个）。

## 方案设计

### 核心思路

新增一个 BPM 候选人策略 `PRODUCT_LINE_EXECUTOR(70)`，当 Flowable 引擎流转到执行员审批节点时：

1. 从流程变量中读取 `productLineCode`
2. 查询 `ops_executor_product_line_scope` 表，找到该产品线关联的执行员
3. 如果有匹配的执行员 → 随机选一个作为负责人
4. 如果没有匹配 → 回退到流程管理员（`managerUserIds` 的第一个）

### 数据流

```
经销商创建工单（带 productLineCode）
  → CsTaskServiceImpl.createCsTask()
    → 将 productLineCode 写入流程变量
    → 启动 BPM 流程
      → Flowable 流转到执行员审批节点
        → BpmTaskCandidateProductLineExecutorStrategy
          → 查 ops_executor_product_line_scope 表
            → 有匹配 → 随机选一个执行员
            → 无匹配 → 取流程管理员第一个
```

---

## 代码改动清单

### 1. 后端 - 新增枚举值

**文件**: `yudao-module-bpm/.../enums/BpmTaskCandidateStrategyEnum.java`

新增 `PRODUCT_LINE_EXECUTOR(70, "产品线执行员")`：

```java
EXPRESSION(60, "流程表达式"),
PRODUCT_LINE_EXECUTOR(70, "产品线执行员"), // 根据产品线从 ops_executor_product_line_scope 表匹配执行员
ASSIGN_EMPTY(1, "审批人为空"),
```

### 2. 后端 - 新建策略类

**文件**: `yudao-module-opshub/.../framework/bpm/BpmTaskCandidateProductLineExecutorStrategy.java`

```java
package cn.iocoder.yudao.module.opshub.framework.bpm;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateStrategy;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import cn.iocoder.yudao.module.bpm.service.definition.BpmProcessDefinitionService;
import cn.iocoder.yudao.module.opshub.service.dealer.ExecutorProductLineScopeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 产品线执行员候选人策略
 *
 * 根据流程变量中的 productLineCode，从 ops_executor_product_line_scope 表匹配对应的执行员。
 * 如果该产品线没有配置执行员，则回退到流程管理员（取第一个）。
 */
@Component
@Slf4j
public class BpmTaskCandidateProductLineExecutorStrategy implements BpmTaskCandidateStrategy {

    public static final String VARIABLE_PRODUCT_LINE_CODE = "productLineCode";

    @Resource
    private ExecutorProductLineScopeService executorProductLineScopeService;

    @Resource
    private BpmProcessDefinitionService processDefinitionService;

    @Override
    public BpmTaskCandidateStrategyEnum getStrategy() {
        return BpmTaskCandidateStrategyEnum.PRODUCT_LINE_EXECUTOR;
    }

    @Override
    public void validateParam(String param) {}

    @Override
    public boolean isParamRequired() {
        return false;
    }

    @Override
    public Set<Long> calculateUsersByTask(DelegateExecution execution, String param) {
        String productLineCode = (String) execution.getVariable(VARIABLE_PRODUCT_LINE_CODE);
        if (StrUtil.isEmpty(productLineCode)) {
            log.warn("[calculateUsersByTask] 流程变量 productLineCode 为空，使用流程管理员兜底");
            return getManagerFallback(execution.getProcessDefinitionId());
        }

        Set<Long> userIds = executorProductLineScopeService.getUserIdsByProductLineCode(productLineCode);
        if (CollUtil.isNotEmpty(userIds)) {
            return userIds;
        }

        log.info("[calculateUsersByTask] productLineCode={} 无匹配执行员，使用流程管理员兜底", productLineCode);
        return getManagerFallback(execution.getProcessDefinitionId());
    }

    @Override
    public Set<Long> calculateUsersByActivity(BpmnModel bpmnModel, String activityId, String param,
                                               Long startUserId, String processDefinitionId,
                                               Map<String, Object> processVariables) {
        if (processVariables == null) {
            return getManagerFallback(processDefinitionId);
        }
        String productLineCode = (String) processVariables.get(VARIABLE_PRODUCT_LINE_CODE);
        if (StrUtil.isEmpty(productLineCode)) {
            return getManagerFallback(processDefinitionId);
        }

        Set<Long> userIds = executorProductLineScopeService.getUserIdsByProductLineCode(productLineCode);
        if (CollUtil.isNotEmpty(userIds)) {
            return userIds;
        }

        return getManagerFallback(processDefinitionId);
    }

    private Set<Long> getManagerFallback(String processDefinitionId) {
        var processDefinitionInfo = processDefinitionService.getProcessDefinitionInfo(processDefinitionId);
        if (processDefinitionInfo != null && CollUtil.isNotEmpty(processDefinitionInfo.getManagerUserIds())) {
            Long managerUserId = processDefinitionInfo.getManagerUserIds().get(0);
            return new LinkedHashSet<>(CollUtil.newArrayList(managerUserId));
        }
        log.warn("[getManagerFallback] 流程定义 {} 无管理员配置，返回空集合", processDefinitionId);
        return new LinkedHashSet<>();
    }
}
```

### 3. 后端 - 修改工单创建逻辑

**文件**: `yudao-module-opshub/.../service/cs/impl/CsTaskServiceImpl.java`

在 `createCsTask()` 方法中，发起 BPM 流程时新增 `productLineCode` 流程变量：

```java
variables.put("assigneeId", taskDO.getAssigneeId());
variables.put("urgency", taskDO.getUrgency());
variables.put("category", taskDO.getCategory());
variables.put("dealerCode", taskDO.getDealerCode());
variables.put("productLineCode", taskDO.getProductLineCode()); // 新增
```

### 4. 前端 - 添加枚举和下拉选项

**文件**: `yudao-ui/yudao-ui-admin-vue3/src/components/SimpleProcessDesignerV2/src/consts.ts`

枚举新增：

```typescript
EXPRESSION = 60,
PRODUCT_LINE_EXECUTOR = 70  // 产品线执行员
```

下拉列表新增：

```typescript
{ label: '产品线执行员', value: CandidateStrategy.PRODUCT_LINE_EXECUTOR }
```

### 5. 前端 - 添加展示文本

**文件**: `yudao-ui/yudao-ui-admin-vue3/src/components/SimpleProcessDesignerV2/src/node.ts`

在 `getShowText` 函数中添加：

```typescript
if (configForm.value?.candidateStrategy === CandidateStrategy.PRODUCT_LINE_EXECUTOR) {
  showText = `按产品线分配执行员`
}
```

---

## 部署与配置步骤

### Step 1: 重新编译部署

```bash
# 后端
mvn clean install -DskipTests
mvn spring-boot:run -pl yudao-server

# 前端
cd yudao-ui/yudao-ui-admin-vue3
pnpm dev
```

### Step 2: 在流程设计器中修改 ops-cs-task 模型

1. 打开管理后台 → **工作流** → **流程模型**
2. 找到 `ops-cs-task` 模型，点击 **设计**
3. 找到 **执行员处理** 的审批节点（非"发起人"节点）
4. 在右侧配置面板中，**候选人设置** 下拉框选择 **「产品线执行员」**
5. 该策略 **不需要填写任何参数**（产品线编码自动从工单的流程变量中获取）
6. 点击 **保存** → **部署**

### Step 3: 确保执行员已分配产品线

在 **权限范围管理** 页面（`/opshub/scope`）确认执行员已分配对应的产品线，否则所有工单都会走流程管理员兜底。

---

## 关键设计说明

| 项目 | 说明 |
|------|------|
| 策略编号 | 70 |
| 策略名称 | 产品线执行员 |
| 参数 | 无需参数（从流程变量 `productLineCode` 自动获取） |
| 数据来源 | `ops_executor_product_line_scope` 表 |
| 兜底策略 | 流程管理员（`bpm_process_definition_info.manager_user_ids` 第一个） |
| 多人处理 | 随机选一个执行员作为负责人 |
| 模块归属 | 策略类在 `yudao-module-opshub`，枚举在 `yudao-module-bpm` |

## 涉及的数据表

| 表名 | 用途 |
|------|------|
| `ops_executor_product_line_scope` | 执行员-产品线授权关系（userId ↔ productLineCode） |
| `bpm_process_definition_info` | 流程定义扩展信息（取 managerUserIds 兜底） |
| `ops_cs_task` | 工单表（提供 productLineCode） |
