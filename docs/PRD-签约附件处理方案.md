# 签约工单附件处理方案

> **版本**: V1.0 | **日期**: 2026-06-19  
> **文档性质**: 技术方案设计 — 签约工单执行员附件上传 + 验收后同步基础数据  
> **前置文档**: `docs/PRD-Step3-签约进度模块.md`、`docs/PRD-Step10-签约附件.md`、`docs/PRD-Step6-工单操作服务模块.md`

---

## 一、背景与问题

### 1.1 业务场景

签约工单的处理流程如下：

```
管理员创建签约工单
  → 执行员接单，线下完成合同盖章
  → 执行员上传盖章后的合同文件（凭证附件）
  → 经销商验收通过
  → 盖章合同文件需要同步到基础数据（ops_basedata_file）的合同/授权分类中
```

### 1.2 核心问题

执行员上传的凭证附件（`ops_cs_attachment`，module='task'）与基础数据文件（`ops_basedata_file`，category='contract'/'authorization'）是两套独立的存储体系，验收通过后需要将附件信息同步过去。

### 1.3 关键约束

- **不新建数据库表**：充分利用已有表结构
- **不破坏现有接口**：复用 `CsAttachmentApi`、`BasedataFileService`
- **保持数据一致性**：同步在事务内或通过幂等事件保障

---

## 二、方案选型

### 2.1 候选方案对比

| 维度 | 方案 A: 增加 sourceBusinessId 字段 | **方案 B: 利用 businessCode 字段（推荐）** | 方案 C: 直接写基础数据 |
|------|-----------------------------------|------------------------------------------|----------------------|
| DDL 变更 | 需要（ops_cs_task 加字段） | **无** | 无 |
| 代码改动量 | 中 | **小** | 中（重构上传逻辑） |
| 数据一致性 | 好 | **好** | 语义混淆（未验收合同提前入库） |
| 语义清晰度 | 好 | **好** | 差（凭证 ≠ 正式基础数据） |
| 实现复杂度 | 中 | **低（约 50 行新代码）** | 中 |

### 2.2 选定方案：方案 B

**核心思路**：`ops_cs_attachment` 已有 `businessCode` 字段（业务编号冗余存储），上传附件时将合同编码（`contractCode`）存入该字段，验收通过后通过事件监听器读取并同步到 `ops_basedata_file`。

---

## 三、数据流设计

### 3.1 完整流程

```
① 执行员接单，线下完成合同盖章

② 执行员在工单详情页上传盖章合同
   → 调用 POST /opshub/cs-attachment/upload
   → 参数：{
       module: 'task',
       businessId: taskId,         // 工单 ID
       businessCode: contractCode, // 合同编码（如 MC-2026-001）← 关键关联字段
       fileUrl: '...',
       fileName: '盖章合同.pdf',
       fileSize: 102400,
       fileType: 'application/pdf'
     }
   → 记录到 ops_cs_attachment 表

③ 经销商验收通过 → verifyTask(passed=true)
   → BPM 流程推进到结束节点
   → BPM 回调 → updateCsTaskStatusByBpm → CLOSED
   → publishEvent(CsTaskStatusChangeEvent)

④ SigningTaskClosedListener 监听到 CLOSED 事件（category=SIGNING）
   → 查询 ops_cs_attachment（module='task', businessId=taskId）
   → 对每个附件：
       contractCode = att.getBusinessCode()
       contract = signingContractMapper.selectByContractCode(contractCode)
       INSERT INTO ops_basedata_file {
         dealerCode  = contract.getDealerCode(),
         dealerId    = contract.getDealerId(),
         category    = mapContractTypeToCategory(contract.getContractType()),
         fileType    = mapContractTypeToFileType(contract.getContractType()),
         fileNo      = contractCode,
         fileName    = att.getFileName(),
         fileUrl     = att.getFileUrl(),
         fileSize    = att.getFileSize()
       }
```

### 3.2 合同类型与基础数据分类映射

| 合同类型（contractType） | 基础数据分类（category） | 文件子类型（fileType） |
|------------------------|------------------------|----------------------|
| main（主合同）           | contract               | MC                   |
| policy（政策合同）       | contract               | POL                  |
| supplement（补充协议）   | contract               | SA                   |
| termination（终止协议）  | contract               | TA                   |
| authorization（授权书）  | authorization          | SQ                   |

> 参见 `BasedataFileTypeEnum` 和 `BasedataCategoryEnum`

### 3.3 关联字段关系

```
ops_cs_attachment                    ops_basedata_file
─────────────────                    ─────────────────
module     = 'task'
businessId = taskId
businessCode = contractCode  ──▶     file_no = contractCode
                                     category = 'contract'/'authorization'
fileName   ────────────────  ──▶     file_name
fileUrl    ────────────────  ──▶     file_url
fileSize   ────────────────  ──▶     file_size
                             ──▶     dealer_code（来自 SigningContractDO）
                             ──▶     dealer_id（来自 SigningContractDO）
                             ──▶     file_type（来自合同类型映射）
```

---

## 四、实现细节

### 4.1 新增文件清单

| # | 文件路径 | 操作 | 说明 |
|---|---------|------|------|
| 1 | `service/signing/listener/SigningTaskClosedListener.java` | **新建** | 监听签约工单 CLOSED 事件，执行同步逻辑 |
| 2 | `service/basedata/BasedataFileService.java` | 修改 | 新增 `createBasedataFile(BasedataFileDO)` 方法 |
| 3 | `service/basedata/impl/BasedataFileServiceImpl.java` | 修改 | 实现 `createBasedataFile` |

> 所有文件在 `yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/` 下

**前端改动**（工单详情页上传时多传 `businessCode`）：  
见 `docs/PRD-Step11-工单流程优化.md` 或后续工单详情页改造计划

### 4.2 SigningTaskClosedListener 实现

```java
package cn.iocoder.yudao.module.opshub.service.signing.listener;

@Component
@Slf4j
public class SigningTaskClosedListener extends CsTaskStatusChangeEventListener {

    @Resource private CsAttachmentService csAttachmentService;
    @Resource private BasedataFileService basedataFileService;
    @Resource private SigningContractMapper signingContractMapper;

    @Override
    protected Integer getCategory() {
        return CsTaskCategoryEnum.SIGNING.getCode(); // 仅监听签约工单
    }

    @Override
    protected void onStatusChange(CsTaskStatusChangeEvent event) {
        // 仅在工单 CLOSED（验收通过）时同步
        if (!CsTaskStatusEnum.CLOSED.getCode().equals(event.getNewStatus())) {
            return;
        }

        List<CsAttachmentDO> attachments =
            csAttachmentService.getAttachmentList("task", event.getTaskId());
        if (CollUtil.isEmpty(attachments)) {
            log.info("[SigningTaskClosedListener][工单 {} 无附件，跳过同步]", event.getTaskId());
            return;
        }

        for (CsAttachmentDO att : attachments) {
            String contractCode = att.getBusinessCode();
            if (StrUtil.isBlank(contractCode)) {
                log.warn("[SigningTaskClosedListener][附件 {} 无合同编码，跳过]", att.getId());
                continue;
            }

            SigningContractDO contract = signingContractMapper.selectByContractCode(contractCode);
            if (contract == null) {
                log.warn("[SigningTaskClosedListener][合同编码 {} 不存在，跳过]", contractCode);
                continue;
            }

            basedataFileService.createBasedataFile(
                new BasedataFileDO()
                    .setDealerId(contract.getDealerId())
                    .setDealerCode(contract.getDealerCode())
                    .setCategory(mapCategory(contract.getContractType()))
                    .setFileType(mapFileType(contract.getContractType()))
                    .setFileNo(contractCode)
                    .setFileName(att.getFileName())
                    .setFileUrl(att.getFileUrl())
                    .setFileSize(att.getFileSize())
                    .setStatus(0)
            );
            log.info("[SigningTaskClosedListener][附件 {} 已同步到基础数据，合同: {}]",
                att.getId(), contractCode);
        }
    }

    /** 合同类型 → 基础数据分类 */
    private String mapCategory(String contractType) {
        if ("authorization".equals(contractType)) return "authorization";
        return "contract"; // main / policy / supplement / termination
    }

    /** 合同类型 → 文件子类型 */
    private String mapFileType(String contractType) {
        return switch (contractType) {
            case "main"          -> "MC";
            case "policy"        -> "POL";
            case "supplement"    -> "SA";
            case "termination"   -> "TA";
            case "authorization" -> "SQ";
            default -> "MC";
        };
    }
}
```

### 4.3 BasedataFileService 新增方法

```java
// BasedataFileService.java
/**
 * 创建基础数据文件记录（用于工单验收后自动同步）
 */
Long createBasedataFile(BasedataFileDO fileDO);

// BasedataFileServiceImpl.java
@Override
public Long createBasedataFile(BasedataFileDO fileDO) {
    basedataFileMapper.insert(fileDO);
    return fileDO.getId();
}
```

### 4.4 前端上传时传入 contractCode

在工单详情页执行操作区，上传附件时需将关联的合同编码作为 `businessCode` 传入：

```typescript
// 上传时
await CsAttachmentApi.uploadAttachment({
  module: 'task',
  businessId: task.id,
  businessCode: task.sourceContractCode, // 工单关联的合同编码
  fileUrl: uploadedFileUrl,
  fileName: file.name,
  fileSize: file.size,
  fileType: file.type
})
```

> **前提**：签约工单创建时需在前端将合同编码传递到工单上下文（可通过 task 详情页传参或在工单详情 API 中关联返回合同编码）。具体实现见工单详情页改造方案。

---

## 五、幂等性与异常处理

### 5.1 重复同步防护

验收后如果事件被重复触发（如消息重投），会造成 `ops_basedata_file` 重复记录。可通过以下方式防护：

```java
// 同步前检查是否已存在（按 dealerCode + fileNo + fileName 去重）
BasedataFileDO existing = basedataFileMapper.selectByDealerCodeAndFileNameAndCategory(
    contract.getDealerCode(), att.getFileName(), mapCategory(contract.getContractType()));
if (existing != null) {
    log.info("[SigningTaskClosedListener][附件已存在，跳过重复同步: {}]", att.getFileName());
    continue;
}
```

> `BasedataFileMapper.selectByDealerCodeAndFileNameAndCategory` 方法已存在。

### 5.2 异常不影响主流程

`CsTaskStatusChangeEvent` 是 Spring ApplicationEvent，在同一事务内同步执行。为避免同步失败回滚验收操作，监听器应使用 `@Async` 或 `@TransactionalEventListener(phase = AFTER_COMMIT)` 隔离：

```java
@Override
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public final void onApplicationEvent(CsTaskStatusChangeEvent event) {
    // 验收事务提交后才执行同步，失败不影响验收
    ...
}
```

> 参考项目中 WebSocket 的 `executeAfterTransaction` 模式。

---

## 六、与现有模块的关系

| 模块 | 关联方式 | 说明 |
|------|---------|------|
| Step 6 工单服务 | `ops_cs_task` + `CsTaskStatusChangeEvent` | 触发源头，验收通过后发布事件 |
| Step 10 签约附件 | `ops_basedata_file`（category='contract'） | 同步目标，步骤 10 已建立查询关系 |
| Step 3 签约进度 | `ops_signing_contract` | 通过合同编码查找合同元信息 |
| ops_cs_attachment | businessCode 字段 | 中间关联字段，存储合同编码 |

---

## 七、不包含的内容

- 授权书（authorization）类型的签约工单（当前枚举中无，如需扩展在 `ContractTypeEnum` 中补充）
- 同步失败的人工补录机制（后续 Step 可补充管理端操作）
- 验收不通过时的附件回滚（附件保留在 `ops_cs_attachment`，不删除，不同步）
- 文件去重策略的精细化（当前按 dealerCode+fileName+category 去重，后续可按 fileUrl MD5 去重）

---

## 八、验证方式

| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1 | 执行员在签约工单中上传盖章合同（携带 businessCode=合同编码） | `ops_cs_attachment` 新增记录，`business_code` 字段有值 |
| 2 | 经销商验收通过 | 工单状态变为 CLOSED，事件发布 |
| 3 | 查询 `ops_basedata_file`（dealerCode + fileNo=合同编码 + category='contract'） | 新增同步记录，fileUrl 与上传的凭证一致 |
| 4 | 在基础数据页面查看合同附件 | 能看到执行员上传的盖章合同文件 |
| 5 | 验收不通过 | `ops_basedata_file` 无新增记录 |
| 6 | 重复验收通过（幂等测试） | `ops_basedata_file` 不重复新增记录 |
