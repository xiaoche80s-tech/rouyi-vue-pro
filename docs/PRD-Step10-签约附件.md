# Step 10 — 签约进度附件列表与下载功能 PRD

> **版本**: V1.0 | **日期**: 2026-06-17  
> **文档性质**: 分阶段实施 PRD — Step 10（签约进度附件列表与下载）  
> **前置文档**: `docs/PRD-Step3-签约进度模块.md`（Step 3）、`docs/PRD-Step2-基础数据模块.md`（Step 2）  
> **输入来源**: 业务需求 — 签约进度明细行增加附件列表及下载功能

---

## 一、背景与目标

### 1.1 背景

当前签约进度模块的合同附件依赖 `ops_signing_contract.file_ids` 字段（逗号分隔存储 `ops_basedata_file.id`），但 `AttachmentPopover.vue` 仅为骨架实现（仅显示 "附件 #ID"），无法展示真实文件信息和下载。此外，`file_ids` 的维护成本高（创建/编辑合同需手动回填），且文件地址来源于用户导入，不通过系统上传流程。

### 1.2 目标

| 目标 | 说明 |
|------|------|
| 移除 `file_ids` 字段 | 不再依赖合同表的 `file_ids` 字段，改为实时查询 `ops_basedata_file` |
| 附件查询 | 通过 `dealer_code + category='contract' + file_no=contract_code` 精确匹配合同附件 |
| 附件列表展示 | 操作列 Popover（hover 预览）+ 详情弹窗底部附件区块 |
| 附件下载 | 复用 `BasedataFileService.getDownloadUrl()` 获取预签名 URL 并触发下载 |
| 数据对齐 | 修复 Step 2/3 测试数据的 `dealer_code` 不一致问题 |

**本阶段不包含**：文件上传、文件导入批量工具、附件在线预览（PDF Viewer）。

---

## 二、数据模型

### 2.1 查询关系

```
ops_signing_contract.contract_code ──▶ ops_basedata_file.file_no
ops_signing_contract.dealer_code   ──▶ ops_basedata_file.dealer_code
                                       + ops_basedata_file.category = 'contract'
```

SQL 等价查询：
```sql
SELECT * FROM ops_basedata_file
WHERE dealer_code = :dealerCode
  AND category = 'contract'
  AND file_no = :contractCode
  AND deleted = 0
ORDER BY id ASC
```

### 2.2 移除 `file_ids` 字段

| 操作 | 表/字段 | 说明 |
|------|---------|------|
| 废弃 | `ops_signing_contract.file_ids` | 数据库列保留（不删列），但后端 DO/VO 移除该字段，前端不再读写 |

### 2.3 无 DDL 变更

本步骤不新增/修改表结构，仅废弃 `file_ids` 字段的使用。

---

## 三、后端实现

### 3.1 新增/修改文件清单

| # | 文件路径 | 操作 | 说明 |
|---|---------|------|------|
| 1 | `dal/mysql/basedata/BasedataFileMapper.java` | 修改 | 新增 `selectByContractCode` 方法 |
| 2 | `service/basedata/BasedataFileService.java` | 修改 | 新增 `getContractAttachments` 方法 |
| 3 | `service/basedata/impl/BasedataFileServiceImpl.java` | 修改 | 实现 `getContractAttachments` |
| 4 | `controller/admin/signing/SigningContractController.java` | 修改 | 新增 `/attachments` 接口 + 移除 `fileIds` 填充 |
| 5 | `controller/admin/signing/vo/SigningContractRespVO.java` | 修改 | 移除 `fileIds` 字段，新增 `attachmentCount` |
| 6 | `dal/dataobject/signing/SigningContractDO.java` | 修改 | 移除 `fileIds` 字段 |
| 7 | `controller/admin/signing/vo/SigningContractCreateReqVO.java` | 修改 | 移除 `fileIds` 相关字段（如有） |
| 8 | `controller/admin/signing/vo/SigningContractUpdateReqVO.java` | 修改 | 移除 `fileIds` / `List<Long> fileIds` |

> 所有文件在 `yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/` 下

### 3.2 新增 API 接口

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/opshub/signing/attachments` | `dealer:signing:query` | 获取合同附件列表（参数：`contractId`） |

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|:---:|------|
| `contractId` | Long | Y | 合同 ID |

**响应**：`CommonResult<List<ContractAttachmentRespVO>>`

```java
@Data
public class ContractAttachmentRespVO {
    private Long id;              // ops_basedata_file.id
    private String fileName;      // 文件名称
    private String fileType;      // 文件子类型编码（MC/SA/POL/TA）
    private String fileTypeName;  // 文件子类型名称
    private String fileNo;        // 文件编号（= contract_code）
    private Long fileSize;        // 文件大小（字节）
    private String description;   // 文件描述
    private LocalDateTime createTime; // 上传时间
}
```

### 3.3 Mapper 新增方法

```java
// BasedataFileMapper.java
default List<BasedataFileDO> selectByContractCode(String dealerCode, String contractCode) {
    return selectList(new LambdaQueryWrapperX<BasedataFileDO>()
            .eq(BasedataFileDO::getDealerCode, dealerCode)
            .eq(BasedataFileDO::getCategory, "contract")
            .eq(BasedataFileDO::getFileNo, contractCode)
            .orderByAsc(BasedataFileDO::getId));
}
```

### 3.4 Service 新增方法

```java
// BasedataFileService.java
List<BasedataFileDO> getContractAttachments(String dealerCode, String contractCode);
```

实现直接委托给 Mapper。

### 3.5 Controller 新增接口

```java
// SigningContractController.java
@GetMapping("/attachments")
@Operation(summary = "获取合同附件列表")
@Parameter(name = "contractId", description = "合同ID", required = true)
@PreAuthorize("@ss.hasPermission('dealer:signing:query')")
public CommonResult<List<ContractAttachmentRespVO>> getContractAttachments(
        @RequestParam("contractId") Long contractId) {
    SigningContractDO contract = signingContractService.getSigningContract(contractId);
    // 校验合同存在
    List<BasedataFileDO> files = basedataFileService.getContractAttachments(
            contract.getDealerCode(), contract.getContractCode());
    return success(BeanUtils.toBean(files, ContractAttachmentRespVO.class));
}
```

### 3.6 RespVO 修改

**SigningContractRespVO**：
- 移除 `fileIds` 字段
- 新增 `attachmentCount`（Integer），在分页查询时批量统计

**分页查询优化**（避免 N+1）：

在 `SigningContractController.getSigningContractPage` 中批量填充 `attachmentCount`：

```java
// 批量查询附件数量：构建 Map<contractCode, count>
Map<String, Integer> attachmentCountMap = buildAttachmentCountMap(voPageResult.getList());
for (SigningContractRespVO vo : voPageResult.getList()) {
    vo.setAttachmentCount(attachmentCountMap.getOrDefault(vo.getContractCode(), 0));
}
```

`buildAttachmentCountMap` 实现：收集所有 `dealerCode`，一次性查出 `category='contract'` 的文件，按 `file_no` 分组计数。

### 3.7 错误码

无新增错误码，复用已有的 `SIGNING_CONTRACT_NOT_EXISTS`。

---

## 四、前端实现

### 4.1 新增/修改文件清单

| # | 文件路径 | 操作 | 说明 |
|---|---------|------|------|
| 1 | `src/api/opshub/signing/index.ts` | 修改 | 移除 `fileIds`，新增 `attachmentCount`、`getContractAttachments` API |
| 2 | `src/views/opshub/signing/components/AttachmentPopover.vue` | 重写 | 从骨架升级为真实数据展示（文件名+类型+大小+下载） |
| 3 | `src/views/opshub/signing/components/ContractTable.vue` | 修改 | 操作列附件触发条件从 `fileIds` 改为 `attachmentCount > 0` |
| 4 | `src/views/opshub/signing/components/ContractPreviewModal.vue` | 修改 | 底部新增附件列表区块 |
| 5 | `src/views/opshub/signing/components/ContractFormModal.vue` | 修改 | 移除附件上传相关逻辑（如有） |
| 6 | `src/views/opshub/signing/components/ContractHoverCard.vue` | 修改 | 追加附件数量摘要 |

> 所有文件在 `yudao-ui/yudao-ui-admin-vue3/` 下

### 4.2 AttachmentPopover 重写设计

```
┌──────────────────────────────────┐
│ 📎 合同附件                  (3) │
├──────────────────────────────────┤
│ 📄 主合同-华康-2024              │
│    MC · 3.0 MB          [下载]   │
├──────────────────────────────────┤
│ 📄 补充协议-华康-仓储            │
│    SA · 1.5 MB          [下载]   │
├──────────────────────────────────┤
│ 📄 合同附件-扫描页               │
│    MC · 2.1 MB          [下载]   │
├──────────────────────────────────┤
│ 共 3 个附件 · 合计 6.6 MB       │
└──────────────────────────────────┘
```

**交互逻辑**：
- 组件接收 `contractId` prop（替代原来的 `fileIds`）
- 首次 hover 展开时调用 `getContractAttachments(contractId)` 加载附件列表
- 缓存已加载的附件数据（避免重复请求）
- 点击「下载」按钮：调用 `getDownloadUrl(fileId)` → `window.open(url)` 触发下载

### 4.3 ContractTable 操作列修改

```html
<!-- 修改前 -->
<el-popover v-if="row.fileIds" trigger="hover" placement="left" :width="280">
  <template #reference>
    <el-button link type="info">附件</el-button>
  </template>
  <AttachmentPopover :file-ids="row.fileIds" />
</el-popover>

<!-- 修改后 -->
<el-popover v-if="row.attachmentCount" trigger="hover" placement="left" :width="320">
  <template #reference>
    <el-button link type="info">附件({{ row.attachmentCount }})</el-button>
  </template>
  <AttachmentPopover :contract-id="row.id" />
</el-popover>
```

### 4.4 ContractPreviewModal 附件区块

在现有 `el-descriptions` 下方追加附件区块：

```html
<!-- 附件区块 -->
<div class="mt-16px" v-loading="attachmentLoading">
  <div class="text-14px font-bold mb-8px">
    <Icon icon="ep:paperclip" class="mr-4px" />
    合同附件 ({{ attachments.length }})
  </div>
  <div v-if="attachments.length === 0" class="text-13px text-gray-400 py-12px text-center bg-gray-50 rounded">
    暂无合同附件
  </div>
  <div v-else class="border rounded divide-y">
    <div v-for="file in attachments" :key="file.id"
         class="flex items-center justify-between px-12px py-8px hover:bg-gray-50">
      <div class="flex items-center gap-8px">
        <Icon icon="ep:document" class="text-blue-500" />
        <span class="text-13px">{{ file.fileName }}</span>
        <el-tag size="small" type="info">{{ file.fileType }}</el-tag>
        <span class="text-12px text-gray-400">{{ formatFileSize(file.fileSize) }}</span>
      </div>
      <el-button link type="primary" @click="handleDownload(file.id)">
        <Icon icon="ep:download" class="mr-2px" />下载
      </el-button>
    </div>
  </div>
</div>
```

**打开弹窗时加载附件**：
```ts
const open = async (row: SigningContractVO) => {
  contract.value = row
  visible.value = true
  await loadAttachments(row.id)
}
```

### 4.5 ContractHoverCard 追加附件摘要

在现有 hover 卡片末尾追加：

```html
<div v-if="attachmentCount > 0" class="text-12px text-gray-500 mt-8px flex items-center gap-4px">
  <Icon icon="ep:paperclip" />
  <span>{{ attachmentCount }} 个附件</span>
</div>
```

`attachmentCount` 从合同行数据中获取（已在分页响应中填充）。

### 4.6 API 层修改

```ts
// src/api/opshub/signing/index.ts

// SigningContractVO 修改
export interface SigningContractVO {
  // ... 其他字段不变
  // 移除: fileIds: string
  attachmentCount: number  // 新增
}

// 附件 VO
export interface ContractAttachmentVO {
  id: number
  fileName: string
  fileType: string
  fileTypeName: string
  fileNo: string
  fileSize: number
  description: string
  createTime: Date
}

// 新增 API
export const getContractAttachments = async (contractId: number): Promise<ContractAttachmentVO[]> => {
  return await request.get({ url: '/opshub/signing/attachments', params: { contractId } })
}
```

### 4.7 下载逻辑

复用已有的 `BasedataFileService.getDownloadUrl()`，前端通过已有 API `getDownloadUrl(id)` 获取预签名 URL 后 `window.open(url)` 打开下载。

```ts
import { getDownloadUrl } from '@/api/opshub/basedata'

const handleDownload = async (fileId: number) => {
  const url = await getDownloadUrl(fileId)
  if (url) {
    window.open(url, '_blank')
  } else {
    message.warning('文件地址为空，无法下载')
  }
}
```

---

## 五、测试数据修复

### 5.1 问题

Step 2 的 `ops_basedata_file` 测试数据使用 `dealer_code = 'HK'` / `'ZS'`，而 Step 3 的 `ops_signing_contract` 使用 `dealer_code = 'D001'` / `'D002'` / `'D003'`。两者 `dealer_code` 不一致，导致 `dealer_code + category + file_no` 查询无法匹配。

### 5.2 修复方案

在 Step 10 DML 中追加 `ops_basedata_file` 的新测试数据，使用与 Step 3 一致的 `dealer_code`，并确保 `file_no` 与 `contract_code` 匹配：

```sql
-- 合同附件 — 经销商 D001 (dealer_code=D001)
INSERT INTO ops_basedata_file (id, dealer_id, dealer_code, category, file_name, file_type, file_no, file_url, file_size, expire_date, status, description, tenant_id) VALUES
-- MC-2024-001 合同附件
(100, 101, 'D001', 'contract', '2024年度主合同-经销商A.pdf', 'MC', 'MC-2024-001', '/contract/MC-2024-001.pdf', 3072000, '2027-06-30', 0, '2024年度主合同扫描件', 123),
(101, 101, 'D001', 'contract', '主合同附件-价格清单.pdf', 'MC', 'MC-2024-001', '/contract/MC-2024-001-att1.pdf', 512000, NULL, 0, '价格清单附件', 123),
-- POL-2024-001 合同附件
(102, 101, 'D001', 'contract', '2024年Q1政策合同.pdf', 'POL', 'POL-2024-001', '/contract/POL-2024-001.pdf', 2048000, '2027-06-30', 0, 'Q1政策合同', 123),
-- SA-2024-001 合同附件
(103, 101, 'D001', 'contract', '主合同补充协议.pdf', 'SA', 'SA-2024-001', '/contract/SA-2024-001.pdf', 1536000, '2027-06-30', 0, '价格调整补充协议', 123),
-- MC-2025-001 合同附件
(104, 101, 'D001', 'contract', '2025年度主合同-经销商A.pdf', 'MC', 'MC-2025-001', '/contract/MC-2025-001.pdf', 3200000, '2028-06-30', 0, '2025续签主合同', 123),
-- MC-2024-002 合同附件（经销商 D002）
(105, 102, 'D002', 'contract', '2024年度主合同-经销商B.pdf', 'MC', 'MC-2024-002', '/contract/MC-2024-002.pdf', 2800000, '2027-12-31', 0, '经销商B主合同', 123),
-- POL-2024-002 合同附件（经销商 D002）
(106, 102, 'D002', 'contract', '2024年Q2政策合同.pdf', 'POL', 'POL-2024-002', '/contract/POL-2024-002.pdf', 1800000, '2027-06-30', 0, 'Q2政策合同', 123);
```

### 5.3 序列同步

```sql
SELECT setval('ops_basedata_file_seq', (SELECT COALESCE(MAX(id), 0) FROM ops_basedata_file));
```

---

## 六、SQL 脚本归档

`db/branches/feature_step10-签约附件/`
- `feature_step10-签约附件_dml.sql`（仅 DML，无 DDL）

---

## 七、与现有模块的交互

| 关联模块 | 关系 | 说明 |
|---------|------|------|
| Step 2 基础数据 | `ops_basedata_file` 表 | 附件数据源，复用 `BasedataFileService.getDownloadUrl()` |
| Step 3 签约进度 | `ops_signing_contract` 表 | 移除 `file_ids` 字段使用，新增 `attachmentCount` 计算 |
| infra 文件服务 | `FileApi.presignGetUrl()` | 预签名下载 URL 生成（已有依赖） |

---

## 八、验证方式

| 验证项 | 验证方法 |
|--------|---------|
| 附件查询 | 调用 `/opshub/signing/attachments?contractId=1`，验证返回 `MC-2024-001` 对应的 2 个附件 |
| 无附件合同 | 调用无附件合同的 attachments 接口，验证返回空数组 |
| 附件下载 | 点击「下载」按钮，验证获取预签名 URL 并触发浏览器下载 |
| Popover 展示 | hover 操作列「附件(N)」按钮，验证展示文件列表（名称+类型+大小+下载） |
| 详情弹窗 | 点击合同名称打开详情弹窗，验证底部附件区块正确展示 |
| Hover 卡片 | 鼠标 hover 合同名称，验证卡片底部显示 "N 个附件" |
| attachmentCount | 分页查询验证每行 `attachmentCount` 值正确 |
| fileIds 移除 | 确认前端不再读写 `fileIds`，后端 RespVO 不包含该字段 |
| 编译验证 | `mvn clean compile -pl yudao-module-opshub` 通过 |
| 前端验证 | `pnpm dev` 签约进度页面正常渲染（Popover+详情弹窗附件区块） |

---

## 九、后续阶段预留

| 功能 | 说明 | 预留阶段 |
|------|------|---------|
| 文件批量导入工具 | 管理员批量导入合同附件文件（Excel+ZIP） | 后续 Step |
| PDF 在线预览 | 集成 PDF.js 实现在线预览而非下载 | 后续 Step |
| 附件版本管理 | 同一合同多版本附件的管理 | 后续 Step |
