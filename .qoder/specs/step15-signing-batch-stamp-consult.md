# Step15 — 签约进度：批量盖章 / 批量咨询 / 咨询功能

## Context

签约进度模块（Step3）已完成合同 CRUD、签署流程、统计趋势等核心功能。前端已预留"批量盖章"和"批量咨询"按钮，但：
- **批量盖章**：`handleBatchStamp` 为占位符（`message.info('批量盖章功能开发中')`），后端无对应端点
- **批量咨询**：前端已通过 `useCsConsult.openBatchConsult()` hook 完整实现（直接调用 cs-session/create），后端无对应端点但前端已闭环
- **单条咨询**：前端 `useCsConsult.openConsult()` + `ChatWindow` 已完整实现并可正常工作

本 Step 的目标：**实现批量盖章功能**（经销商通过 CsOpReq 操作请求系统发起盖章申请），确认批量咨询和单条咨询已就绪无需额外改动。

---

## Task 1: 新增批量盖章请求/响应 VO

**新建 2 个文件**：

### 1.1 `BatchStampReqVO.java`
- 路径：`yudao-module-opshub/.../controller/admin/signing/vo/BatchStampReqVO.java`
- 字段：`List<Long> contractIds`（@NotEmpty, @Size(max=50)）

### 1.2 `BatchStampRespVO.java`
- 路径：`yudao-module-opshub/.../controller/admin/signing/vo/BatchStampRespVO.java`
- 字段：`totalCount`, `successCount`, `skippedCount`, `List<SkippedItem> skippedItems`
- 内部类 `SkippedItem`：`contractId`, `contractCode`, `reason`

---

## Task 2: 新增防重复查询方法到 CsOpReqMapper

**修改文件**：`yudao-module-opshub/.../dal/mysql/cs/CsOpReqMapper.java`

新增 default 方法 `selectActiveStampSourceIds(String sourceModule, List<Long> sourceIds)`：
- 查询条件：`opType='stamp'`, `sourceModule=传入值`, `sourceId IN 传入列表`, `status IN (0,1,2)`（PENDING/IN_PROGRESS/DELIVERED）
- 返回：`Set<Long>` — 已有进行中盖章请求的 sourceId 集合

---

## Task 3: 实现 batchStamp 业务逻辑

### 3.1 修改 `SigningContractService.java`
- 新增方法签名：`BatchStampRespVO batchStamp(List<Long> contractIds)`

### 3.2 修改 `SigningContractServiceImpl.java`
- 注入 `CsOpReqService` 和 `CsOpReqMapper`
- 实现 `batchStamp` 方法，核心逻辑：
  1. `selectBatchIds` 批量查询合同
  2. 调用 `CsOpReqMapper.selectActiveStampSourceIds` 获取已有进行中盖章请求的合同 ID 集合
  3. 遍历每个 contractId：
     - 不存在 → skipped（"合同不存在"）
     - status != unsigned → skipped（"合同已签署"）
     - 已有进行中 OpReq → skipped（"已有进行中的盖章请求"）
     - 通过校验 → 创建 `CsOpReqCreateReqVO`（opType=stamp, sourceModule=signing, sourceId/sourceCode=合同ID/编码, dealerCode/productLineCode 从合同取）
  4. 返回 `BatchStampRespVO`

---

## Task 4: 新增 batch-stamp 端点到 Controller

**修改文件**：`SigningContractController.java`

在 `uploadSignProof` 方法之后新增：
```
POST /opshub/signing/batch-stamp
@PreAuthorize("@ss.hasPermission('dealer:signing:stamp')")
接收 @Valid @RequestBody BatchStampReqVO
调用 signingContractService.batchStamp(reqVO.getContractIds())
返回 CommonResult<BatchStampRespVO>
```

---

## Task 5: 前端对接批量盖章 API

### 5.1 修改 `api/opshub/signing/index.ts`
- 新增 `batchStamp(contractIds: number[])` 函数，POST `/opshub/signing/batch-stamp`

### 5.2 修改 `views/opshub/signing/index.vue`
- 替换 `handleBatchStamp` 占位符（第 178-180 行）：
  - 筛选 unsigned 状态的选中合同
  - 调用 `SigningApi.batchStamp(ids)`
  - 显示结果消息（成功数/跳过数）
  - 刷新列表

---

## Task 6: 编译验证

- 后端：`mvn compile -pl yudao-module-opshub` 通过
- 前端：无 TypeScript 报错

---

## 不需要改动的功能

| 功能 | 原因 |
|------|------|
| 批量咨询 | 前端 `useCsConsult.openBatchConsult()` 已完整实现（并行调用 createSession，上限 20 条），无需后端额外端点 |
| 单条咨询 | `useCsConsult.openConsult()` + `ChatWindow` 已完整运行 |
| SQL/DML | `dealer:signing:stamp`（menu_id=6034）和 `dealer:signing:consult`（menu_id=6032）权限已在 Step1 DML 中定义并分配角色 |

## 关键文件清单

| # | 文件 | 操作 |
|---|------|------|
| 1 | `.../signing/vo/BatchStampReqVO.java` | 新增 |
| 2 | `.../signing/vo/BatchStampRespVO.java` | 新增 |
| 3 | `.../dal/mysql/cs/CsOpReqMapper.java` | 修改 |
| 4 | `.../service/signing/SigningContractService.java` | 修改 |
| 5 | `.../service/signing/impl/SigningContractServiceImpl.java` | 修改 |
| 6 | `.../controller/admin/signing/SigningContractController.java` | 修改 |
| 7 | `src/api/opshub/signing/index.ts` | 修改 |
| 8 | `src/views/opshub/signing/index.vue` | 修改 |

## 验证方式

1. 以经销商登录 → 勾选 2-3 个 unsigned 合同 → 点击"批量盖章"
2. 验证成功消息显示"成功 X 个"
3. 在操作请求列表页（客服工单）可看到新建的 stamp 类型请求
4. 再次对同一批合同操作，验证显示"跳过 — 已有进行中的盖章请求"
5. 勾选已签署合同操作，验证提示"已签署"被跳过
6. 批量咨询和单条咨询保持正常工作
