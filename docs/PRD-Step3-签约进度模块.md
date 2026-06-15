# Step 3 — 签约进度模块实施 PRD

> **版本**: V1.1 | **日期**: 2026-06-15  
> **文档性质**: 分阶段实施 PRD — Step 3（签约进度模块）  
> **前置文档**: `docs/PRD-Step1-角色菜单经销商产品线.md`（Step 1）、`docs/PRD-Step2-基础数据模块.md`（Step 2）  
> **输入来源**: PRD V1.0 模块一 + PRD V2.0 签约进度模块（第四章） + UI 交互原型

---

## 一、Step 3 目标

在 Step 1/2 基础设施层之上，实现**签约进度模块**的合同录入与签署进度追踪：

| 目标 | 说明 |
|------|------|
| 合同数据模型 | `ops_signing_contract` 表，覆盖 4 种合同类型（主合同/政策合同/补充协议/终止协议） |
| 合同录入（管理员） | 品牌商管理员创建/编辑合同记录（名称、类型、经销商、下发日期等），合同编码由系统自动生成（MC-/POL-/SA-/TA- 前缀），支持上传合同附件文件（复用 Step 2 `ops_basedata_file`） |
| 7 大统计卡片 | 合同总数、已签署、未签署、4 种类型各自的签署统计 |
| 高级筛选 | 时间维度（月/季/年）、产品线、合同类型、状态、经销商（角色联动） |
| 合同列表 | 分页查询、模糊搜索、多条件筛选、表头筛选、hover 预览 |
| 签署操作 | 经销商点击“去签署”标记发起（sub_status → signing）→ 线下盖章寄送（系统外）→ 执行员收到后上传盖章文件更新为已签署（status → signed） |
| 编码自动生成 | MC-/POL-/SA-/TA- 前缀 + 年份 + 3 位序号的合同编码自动生成逻辑 |
| 趋势图表 | ECharts 堆叠柱状图，按月度/季度/年度展示 4 种合同类型的累计签署趋势 |
| 合同附件 | 关联 `ops_basedata_file`（category='contract'）复用已有文件管理体系 |
| 数据权限 | 注册 `ops_signing_contract` 到 `DealerDataPermissionRule`（dealer_code + product_line_code） |

**本阶段不包含**：电子签章对接（二期功能）、合同 AI 解读（后续 Step）。

---

## 二、数据模型

### 2.1 表结构 — ops_signing_contract

继承 `TenantBaseDO`，PostgreSQL 语法（与 Step 1/2 一致）。

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|:---:|------|
| `id` | int8 | PK | 主键（序列 `ops_signing_contract_seq`） |
| `dealer_id` | int8 | Y | 经销商 ID（关联 `ops_dealer_info.id`） |
| `dealer_code` | varchar(50) | Y | 经销商编码（数据权限用，关联 `ops_dealer_info.dealer_code`） |
| `product_line_code` | varchar(50) | N | 产品线编码（数据权限用，关联 `ops_dealer_product_line.product_line_code`，可空） |
| `contract_type` | varchar(20) | Y | 合同类型：`main` / `policy` / `supplement` / `termination` |
| `contract_type_name` | varchar(50) | Y | 合同类型中文名：主合同 / 政策合同 / 补充协议 / 终止协议 |
| `contract_code` | varchar(30) | Y | 合同编码（唯一，系统自动生成），如 `MC-2026-001` |
| `contract_name` | varchar(200) | Y | 合同名称 |
| `status` | varchar(20) | Y | 签署状态：`signed` / `unsigned` |
| `sub_status` | varchar(20) | N | 子状态（仅 unsigned 时有效）：`pending`（待签署）/ `signing`（签署中） |
| `issued_date` | date | Y | 下发日期 |
| `sign_date` | date | N | 签署日期（签署后填写） |
| `summary` | text | N | 合同摘要（hover 预览卡片显示） |
| `policy_analysis` | text | N | 政策解析（仅 policy 类型合同） |
| `indicators` | text | N | 政策指标 JSON 数组（仅 policy 类型合同），格式见 2.2 |
| `file_ids` | varchar(500) | N | 关联附件文件 ID 列表（逗号分隔，关联 `ops_basedata_file.id`） |
| `sign_proof_url` | varchar(500) | N | 签署凭证 URL（执行员上传盖章后的合同扫描件回填） |
| `remark` | varchar(500) | N | 备注 |
| 标准字段 | | | creator, create_time, updater, update_time, deleted, tenant_id |

**索引**：

| 索引名 | 类型 | 列 |
|--------|------|----|
| `uk_ops_signing_contract_code` | UNIQUE | contract_code |
| `idx_ops_signing_contract_dealer_code` | INDEX | dealer_code |
| `idx_ops_signing_contract_pl_code` | INDEX | product_line_code |
| `idx_ops_signing_contract_type` | INDEX | contract_type |
| `idx_ops_signing_contract_status` | INDEX | status |
| `idx_ops_signing_contract_issued_date` | INDEX | issued_date |

**设计决策**：

| 决策项 | 选择 | 理由 |
|--------|------|------|
| 合同附件 | `file_ids` 逗号分隔 + 关联 `ops_basedata_file` | 复用 Step 2 已有文件管理体系，`ops_basedata_file` 中 category='contract' 的记录即合同附件 |
| 合同编码 | 系统自动生成 | 创建合同时自动生成编码（前缀+年份+3位序号），管理员无需手动填写，保证唯一性和规范性 |
| sub_status | 新增独立字段 | PRD V2.0 未签署卡片需显示"待签署/签署中"子分类，仅靠 status 无法满足 |
| policy_analysis/indicators | 直接存储在合同表 | 与合同一一对应，无需拆表；indicators 用 JSON 文本存储 |
| dealer_code + product_line_code | 冗余存储 | DealerDataPermissionRule 基于 Code 列过滤（与 Step 1/2 表保持一致） |

### 2.2 indicators JSON 格式

仅 policy 类型合同使用：

```json
[
  {
    "indicatorName": "骨科关节销量",
    "targetValue": 500,
    "unit": "件",
    "quarter": 1,
    "achievedValue": 320,
    "achieveRate": 64.0
  }
]
```

### 2.3 合同编码规则

合同编码在管理员创建合同时由**系统自动生成**：

| 合同类型 | 前缀 | 格式 | 示例 |
|---------|------|------|------|
| 主合同 (main) | MC | `MC-{year}-{seq}` | MC-2026-001 |
| 政策合同 (policy) | POL | `POL-{year}-{seq}` | POL-2026-001 |
| 补充协议 (supplement) | SA | `SA-{year}-{seq}` | SA-2026-001 |
| 终止协议 (termination) | TA | `TA-{year}-{seq}` | TA-2025-001 |

序号生成：按 `(contract_type, year)` 组合查询 `MAX(seq)` +1，3 位补零。并发安全通过 `SELECT ... FOR UPDATE` 或分布式锁保证。生成后写入 `contract_code` 字段，前端创建表单中编码字段为只读显示。

### 2.4 枚举定义

```java
// ContractTypeEnum
MAIN("main", "主合同", "MC"),
POLICY("policy", "政策合同", "POL"),
SUPPLEMENT("supplement", "补充协议", "SA"),
TERMINATION("termination", "终止协议", "TA");

// ContractStatusEnum
SIGNED("signed", "已签署"),
UNSIGNED("unsigned", "未签署");

// ContractSubStatusEnum（仅 unsigned 时有效）
PENDING("pending", "待签署"),
SIGNING("signing", "签署中");
```

### 2.5 签署流程说明

```
经销商点击“去签署” ─── sub_status: pending → signing
    │
    │  （系统外）经销商线下盖经销商章
    │  （系统外）线下寄到品牌商公司
    │  （外部系统）执行员收到合同，发起内部盖章流程
    │
    ▼
执行员上传盖章文件 ─── sign_proof_url 回填，sign_date 回填
                        status: unsigned → signed
```

**角色职责**：

| 角色 | 操作 | 系统内/外 |
|------|------|----------|
| 经销商 | 点击“去签署”发起签署 | 系统内（sub_status → signing） |
| 经销商 | 线下盖经销商章 | 系统外 |
| 经销商 | 线下寄送到品牌商公司 | 系统外 |
| 执行员 | 收到合同，发起内部盖章 | 外部系统 |
| 执行员 | 上传盖章后的合同扫描件 | 系统内（回填 sign_proof_url，status → signed） |

### 2.6 与 ops_basedata_file 的关联

```
ops_signing_contract.file_ids ──── 逗号分隔 ────▶ ops_basedata_file.id
                                                 （WHERE category='contract'）
```

前端展示合同时，通过 file_ids 查询对应文件记录，获取文件名、大小、URL 等信息。

管理员创建/编辑合同时，可通过文件上传组件上传合同附件，文件写入 `ops_basedata_file`（category='contract'），回填 `file_ids`。

---

## 三、后端实现

### 3.1 新增文件清单

| # | 文件路径 | 说明 |
|---|---------|------|
| 1 | `enums/ContractTypeEnum.java` | 合同类型枚举 |
| 2 | `enums/ContractStatusEnum.java` | 合同签署状态枚举 |
| 3 | `enums/ContractSubStatusEnum.java` | 合同子状态枚举 |
| 4 | `dal/dataobject/signing/SigningContractDO.java` | DO 对象 |
| 5 | `dal/mysql/signing/SigningContractMapper.java` | Mapper 接口 |
| 6 | `controller/admin/signing/vo/SigningContractPageReqVO.java` | 分页请求 VO |
| 7 | `controller/admin/signing/vo/SigningContractCreateReqVO.java` | 创建合同请求 VO |
| 8 | `controller/admin/signing/vo/SigningContractUpdateReqVO.java` | 更新合同请求 VO |
| 9 | `controller/admin/signing/vo/SigningContractRespVO.java` | 响应 VO |
| 10 | `controller/admin/signing/vo/SigningContractStatisticsRespVO.java` | 统计卡片响应 VO |
| 11 | `controller/admin/signing/vo/SigningContractTrendRespVO.java` | 趋势图表响应 VO |
| 12 | `service/signing/SigningContractService.java` | Service 接口 |
| 13 | `service/signing/impl/SigningContractServiceImpl.java` | Service 实现 |
| 14 | `controller/admin/signing/SigningContractController.java` | REST Controller |

> 所有文件在 `yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/` 下

### 3.2 修改文件清单

| # | 文件路径 | 修改内容 |
|---|---------|---------|
| 1 | `enums/ErrorCodeConstants.java` | 追加 5 个错误码（1-050-005-xxx 段） |
| 2 | `framework/datapermission/config/OpshubDataPermissionConfiguration.java` | 注册 `ops_signing_contract` 的 dealer_code + product_line_code |

### 3.3 错误码定义

```java
// ========== 签约进度 1-050-005-xxx ==========
ErrorCode SIGNING_CONTRACT_NOT_EXISTS        = new ErrorCode(1_050_005_000, "合同不存在");
ErrorCode SIGNING_CONTRACT_CODE_DUPLICATE    = new ErrorCode(1_050_005_001, "合同编码已存在");
ErrorCode SIGNING_CONTRACT_ALREADY_SIGNED    = new ErrorCode(1_050_005_002, "合同已签署，不可重复签署");
ErrorCode SIGNING_CONTRACT_NOT_UNSIGNED      = new ErrorCode(1_050_005_003, "仅未签署合同可发起签署");
ErrorCode SIGNING_CONTRACT_DEALER_NOT_EXISTS = new ErrorCode(1_050_005_004, "关联经销商不存在");
```

### 3.4 REST API 接口

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/opshub/signing/page` | `dealer:signing:query` | 分页查询合同列表 |
| GET | `/opshub/signing/get?id=` | `dealer:signing:query` | 获取合同详情 |
| GET | `/opshub/signing/statistics` | `dealer:signing:query` | 7 大统计卡片数据 |
| GET | `/opshub/signing/trend` | `dealer:signing:query` | 签约趋势图表数据 |
| POST | `/opshub/signing/create` | `dealer:signing:create` | 创建合同（管理员） |
| PUT | `/opshub/signing/update` | `dealer:signing:update` | 编辑合同（管理员，含附件关联更新） |
| POST | `/opshub/signing/upload-attachment` | `dealer:signing:update` | 上传合同附件（写入 `ops_basedata_file` category='contract'，回填 `file_ids`） |
| POST | `/opshub/signing/sign` | `dealer:signing:sign` | 经销商发起签署（sub_status pending → signing，标记合同进入线下签署流程） |
| POST | `/opshub/signing/upload-sign-proof` | `dealer:signing:upload-proof` | 执行员上传盖章文件（回填 `sign_proof_url`，status → signed，sign_date 回填） |
| POST | `/opshub/signing/batch-stamp` | `dealer:signing:stamp` | 批量申请盖章（经销商专属） |
| POST | `/opshub/signing/batch-consult` | `dealer:signing:consult` | 批量咨询服务 |

### 3.5 数据权限集成

在 `OpshubDataPermissionConfiguration` 中追加：

```java
// Step 3：注册签约进度表
rule.addDealerColumn("ops_signing_contract");
rule.addProductLineColumn("ops_signing_contract");
```

### 3.6 创建/更新合同请求 VO

```java
@Data
public class SigningContractCreateReqVO {
    @NotNull
    private Long dealerId;              // 经销商 ID
    @NotBlank
    private String dealerCode;          // 经销商编码
    private String productLineCode;     // 产品线编码（可空）
    @NotBlank
    private String contractType;        // main/policy/supplement/termination
    @NotBlank
    private String contractTypeName;    // 合同类型中文名
    @NotBlank
    private String contractName;        // 合同名称
    // contractCode 由系统自动生成，前端不传
    @NotNull
    private LocalDate issuedDate;       // 下发日期
    private LocalDate signDate;         // 签署日期
    private String summary;             // 合同摘要
    private String policyAnalysis;      // 政策解析（policy 类型）
    private String indicators;          // 政策指标 JSON（policy 类型）
    private String remark;              // 备注
}

@Data
public class SigningContractUpdateReqVO {
    @NotNull
    private Long id;                    // 合同 ID
    // 其余字段同 CreateReqVO，均可选更新
    private Long dealerId;
    private String dealerCode;
    private String productLineCode;
    private String contractType;
    private String contractTypeName;
    private String contractCode;
    private String contractName;
    private LocalDate issuedDate;
    private LocalDate signDate;
    private String status;              // signed/unsigned
    private String subStatus;           // pending/signing
    private String summary;
    private String policyAnalysis;
    private String indicators;
    private List<Long> fileIds;         // 附件文件 ID 列表（更新时回填 file_ids）
    private String remark;
}
```

### 3.7 分页请求 VO

```java
@Data @EqualsAndHashCode(callSuper = true)
public class SigningContractPageReqVO extends PageParam {
    private String timeDimension;       // month / quarter / year
    private List<Integer> months;       // 1-12
    private List<Integer> quarters;     // 1-4
    private List<Integer> years;        // 2024/2025/2026
    private List<String> productLineCodes;
    private List<String> contractTypes; // main/policy/supplement/termination
    private List<String> statuses;      // signed/unsigned
    private List<String> dealerCodes;   // 管理员可见
    private String keyword;             // 模糊搜索（编码/名称/经销商/摘要）
}
```

### 3.8 统计卡片响应 VO

```java
@Data
public class SigningContractStatisticsRespVO {
    private Integer totalCount;
    private Integer mainCount, policyCount, supplementCount, terminationCount;
    private Integer signedCount;
    private BigDecimal signedRate;
    private Integer unsignedCount, pendingCount, signingCount;
    private ContractTypeStat main, policy, supplement, termination;

    @Data
    public static class ContractTypeStat {
        private Integer total, signed, unsigned;
    }
}
```

Mapper 层使用一次 `GROUP BY contract_type, status` 聚合查询完成，避免 N+1。

### 3.9 趋势图表响应 VO

```java
@Data
public class SigningContractTrendRespVO {
    private String period;              // "2026-01" / "2026-Q1" / "2026"
    private Integer mainCount, policyCount, supplementCount, terminationCount;
    private Integer totalCount;
}
```

Mapper 使用 `TO_CHAR(issued_date, 'YYYY-MM')` 按指定粒度聚合。

### 3.10 依赖

- `DealerInfoService`：填充经销商名称、校验经销商存在性
- `DealerProductLineService`：填充产品线名称
- `BasedataFileService`：根据 file_ids 查询附件信息；上传附件时创建文件记录
- `FileApi`（yudao-module-infra）：预签名下载 URL、文件上传

---

## 四、前端实现

### 4.1 新增文件清单

| # | 文件路径 | 说明 |
|---|---------|------|
| 1 | `src/api/opshub/signing/index.ts` | API 接口定义 |
| 2 | `src/views/opshub/signing/index.vue` | 主页面 |
| 3 | `src/views/opshub/signing/components/StatisticsCards.vue` | 7 大统计卡片 |
| 4 | `src/views/opshub/signing/components/SigningFilterBar.vue` | 筛选栏（时间维度切换） |
| 5 | `src/views/opshub/signing/components/ContractTable.vue` | 合同列表表格 |
| 6 | `src/views/opshub/signing/components/ContractPreviewModal.vue` | 合同预览弹窗 |
| 7 | `src/views/opshub/signing/components/ContractHoverCard.vue` | 合同名称 hover 卡片 |
| 8 | `src/views/opshub/signing/components/SigningTrendChart.vue` | 签约趋势堆叠柱状图 |
| 9 | `src/views/opshub/signing/components/SignRequestModal.vue` | 去签署弹窗（经销商确认发起线下签署流程） |
| 10 | `src/views/opshub/signing/components/UploadSignProofModal.vue` | 上传盖章文件弹窗（执行员上传盖章后的合同扫描件） |
| 11 | `src/views/opshub/signing/components/AttachmentPopover.vue` | 附件 hover 预览/下载 |
| 12 | `src/views/opshub/signing/components/ContractFormModal.vue` | 合同创建/编辑弹窗（管理员，含文件上传） |

### 4.2 页面布局

```
┌──────────────────────────────────────────────────────────────┐
│ [合同总数] [已签署 80%] [未签署] [主合同] [政策合同] [补充协议] [终止协议] │
├──────────────────────────────────────────────────────────────┤
│ 筛选栏：[时间维度▼] [月份/季度] [年度▼] [产品线▼] [合同类型▼]      │
│        [状态▼] [经销商▼(管理员)] [搜索] [重置]                     │
├──────────────────────────────────────────────────────────────┤
│ [新建合同(管理员)] [批量盖章(经销商)] [批量咨询]                        │
├──────────────────────────────────────────────────────────────┤
│ □ | 经销商(管理) | 类型 | 编码 | 名称 | 状态 | 下发时间 |         │
│   | 签署日期 | 操作 [编辑] [去签署] [附件] [咨询]                    │
├──────────────────────────────────────────────────────────────┤
│ 已选 N 个合同 | 批量咨询服务    共 N 条  每页 50 条                  │
├──────────────────────────────────────────────────────────────┤
│ 签约趋势图（ECharts 堆叠柱状图）                                  │
│ ■主合同 ■政策合同 ■补充协议 ■终止协议                              │
└──────────────────────────────────────────────────────────────┘
```

### 4.3 关键交互

| 功能 | 实现 | 权限控制 |
|------|------|---------|
| 统计卡片 | `/opshub/signing/statistics`，7 张 `el-card` 一行排列 | `dealer:signing:query` |
| 时间维度切换 | `el-select` 切换 month/quarter/year，联动月份/季度选择器 | — |
| 经销商筛选 | `el-select` multiple，仅管理员/执行员可见 | 前端角色判断 |
| 模糊搜索 | `el-input` keyword 参数，跨 4 字段 OR 匹配 | — |
| 合同名称 hover | `el-popover` + ContractHoverCard（含政策解析） | — |
| 附件 hover | `el-popover` + AttachmentPopover（预览/下载） | — |
| "新建合同" | 管理员点击打开 ContractFormModal，填写合同信息+上传附件 | `dealer:signing:create` |
| "编辑合同" | 管理员点击操作列编辑，修改合同信息+附件 | `dealer:signing:update` |
| "去签署" | 仅经销商 + unsigned：标记合同进入线下签署流程，sub_status → signing | `dealer:signing:sign` |
| "上传盖章文件" | 仅执行员 + signing(sub_status)：上传盖章后的合同扫描件，回填 sign_proof_url，sign_date，status → signed | `dealer:signing:upload-proof` |
| 批量盖章 | 勾选后操作，未勾选提示"请先选择" | `dealer:signing:stamp` |
| 趋势图表 | ECharts 堆叠柱状图，点击柱子弹窗合同明细 | — |
| 分页 | 默认 50 条，支持 50/100/200/500/99999 | — |

### 4.4 ContractFormModal 详细设计

管理员创建/编辑合同的弹窗组件：

| 字段 | 组件 | 必填 | 说明 |
|------|------|:---:|------|
| 经销商 | `el-select`（搜索选择） | Y | 关联 `ops_dealer_info`，自动填充 dealer_code |
| 产品线 | `el-select` | N | 关联经销商的产品线列表 |
| 合同类型 | `el-select` | Y | 4 种类型，自动填充 contract_type_name |
| 合同编码 | 只读显示 | — | 系统自动生成，创建后展示，不可编辑 |
| 合同名称 | `el-input` | Y | — |
| 下发日期 | `el-date-picker` | Y | — |
| 签署日期 | `el-date-picker` | N | — |
| 合同摘要 | `el-input` textarea | N | — |
| 政策解析 | `el-input` textarea | N | 仅 policy 类型显示 |
| 政策指标 | 动态表格 | N | 仅 policy 类型显示，JSON 编辑 |
| 合同附件 | 文件上传组件 | N | 调用 `/opshub/signing/upload-attachment`，复用 Step 2 文件上传逻辑 |
| 备注 | `el-input` textarea | N | — |

**编辑模式**：加载已有合同数据，附件显示已上传文件列表（支持新增/删除）。

### 4.5 统计卡片详细设计

| # | 卡片 | 主指标 | 子指标 |
|---|------|--------|--------|
| 1 | 合同总数 | totalCount | "主合同 {mainCount} · 政策合同 {policyCount} · 补充协议 {supplementCount} · 终止协议 {terminationCount}" |
| 2 | 已签署 | signedCount | signedRate + "%" |
| 3 | 未签署 | unsignedCount | "待签署 {pendingCount} · 签署中 {signingCount}" |
| 4 | 主合同 | main.total | "已签署 {main.signed} / 未签署 {main.unsigned}" |
| 5 | 政策合同 | policy.total | "已签署 {policy.signed} / 未签署 {policy.unsigned}" |
| 6 | 补充协议 | supplement.total | "已签署 {supplement.signed} / 未签署 {supplement.unsigned}" |
| 7 | 终止协议 | termination.total | "已签署 {termination.signed} / 未签署 {termination.unsigned}" |

### 4.6 趋势图表设计

- **图表库**：ECharts 按需引入（`echarts/core` + BarChart + 必要组件）
- **类型**：堆叠柱状图
- **X 轴**：时间标签（按月/季/年切换）
- **4 个系列**：main（紫色）、policy（蓝色）、supplement（橙色）、termination（红色）
- **交互**：点击柱子 → 弹窗该时段合同明细列表
- **联动**：筛选条件变化自动重绘

### 4.7 菜单更新

更新菜单 id=6003（签约进度）的 `component` 为 `opshub/signing/index`。

---

## 五、角色权限矩阵

### 5.1 按钮级权限

| 权限标识 | 说明 | 品牌管理员 | 品牌销售员 | 服务单执行员 | 经销商 |
|---------|------|:---------:|:---------:|:----------:|:-----:|
| `dealer:signing:query` | 查看合同 + 统计 + 趋势 | ✅ | ✅ | ✅ | ✅ |
| `dealer:signing:create` | 创建合同 | ✅ | — | — | — |
| `dealer:signing:update` | 编辑合同 + 上传附件 | ✅ | — | — | — |
| `dealer:signing:sign` | 经销商发起签署（标记进入线下流程） | — | — | — | ✅ |
| `dealer:signing:upload-proof` | 执行员上传盖章文件 + 更新状态 | — | — | ✅ | — |
| `dealer:signing:consult` | 发起咨询 | ✅ | — | ✅ | ✅ |
| `dealer:signing:stamp` | 批量申请盖章 | — | — | — | ✅ |

### 5.2 UI 联动规则

| UI 元素 | 品牌管理员 | 品牌销售员 | 服务单执行员 | 经销商 |
|---------|-----------|-----------|-------------|-------|
| 经销商筛选器 | 可见 | 可见 | **隐藏** | **隐藏** |
| 表格经销商列 | 显示 | 显示 | 显示 | **隐藏** |
| "新建合同"按钮 | 显示 | 隐藏 | 隐藏 | 隐藏 |
| "编辑"操作 | 显示 | 隐藏 | 隐藏 | 隐藏 |
| "去签署"按钮 | 隐藏 | 隐藏 | 隐藏 | 显示（仅 unsigned） |
| "批量盖章"按钮 | 隐藏 | 隐藏 | 隐藏 | 显示 |
| "上传盖章文件"按钮 | 隐藏 | 隐藏 | 显示（仅 signing） | 隐藏 |
| 操作列 | 编辑+附件+咨询 | 附件(只读) | 上传盖章文件+附件+咨询 | 去签署+附件+咨询 |

---

## 六、SQL 脚本

### 6.1 DDL

PostgreSQL 语法：CREATE TABLE + ALTER TABLE (PK) + UNIQUE INDEX + 5 INDEX + SEQUENCE + COMMENT。

### 6.2 DML

- 更新菜单 id=6003 的 `component` 为 `opshub/signing/index`
- 新增按钮权限 DML（`dealer:signing:create`、`dealer:signing:update`、`dealer:signing:upload-proof`）
- 测试数据约 30-40 条，覆盖：
  - 4 种合同类型 × 2-3 个经销商
  - 混合 signed/unsigned 状态（含 pending/signing 子状态）
  - 政策合同含 policy_analysis 和 indicators
  - 跨年份（2024/2025/2026）验证趋势图表
  - 关联 ops_basedata_file 中 category='contract' 的记录

### 6.3 归档路径

`db/branches/feature_step3-签约进度/`
- `feature_step3-签约进度_ddl.sql`
- `feature_step3-签约进度_dml.sql`

---

## 七、与现有模块的交互

| 关联模块 | 关系 | 说明 |
|---------|------|------|
| Step 2 基础数据 | `file_ids` → `ops_basedata_file.id` | 合同附件复用已有文件管理体系；上传附件写入文件表 |
| Step 1 经销商/产品线 | `dealer_code` / `product_line_code` | 数据权限过滤依赖扩展表；创建合同时校验经销商存在性 |
| Step 4 订单模块（后续） | 合同签署后进入订单流程 | Step 3 完成签署追踪，订单管理在 Step 4 实现 |

---

## 八、验证方式

| 验证项 | 验证方法 |
|--------|---------|
| 表创建 | DDL 执行后 `SELECT * FROM ops_signing_contract` 确认表存在 |
| 菜单更新 | 前端登录确认"签约进度"菜单指向 `opshub/signing/index` |
| 编码生成 | 验证 MC-/POL-/SA-/TA- 编码自动递增不重复 |
| 合同创建 | 管理员创建合同，验证编码自动生成、经销商存在性 |
| 合同编辑 | 管理员编辑合同信息和附件关联 |
| 附件上传 | 上传合同附件，验证 `ops_basedata_file` 记录创建和 `file_ids` 回填 |
| 分页查询 | 4 种角色分别调用 `/opshub/signing/page`，验证数据和分页 |
| 数据权限 | dealer 仅看到授权经销商合同；执行员仅看到授权产品线合同 |
| 统计卡片 | `/opshub/signing/statistics` 验证 7 张卡片数据正确 |
| 趋势图表 | `/opshub/signing/trend` 验证按月/季/年聚合正确 |
| 筛选功能 | 验证所有筛选组合（时间维度、产品线、类型、状态、经销商、关键词） |
| 发起签署 | 经销商“去签署”触发 sub_status pending → signing，标记进入线下签署流程 |
| 上传盖章文件 | 执行员上传盖章扫描件，验证 sign_proof_url 回填、sign_date 回填、status → signed |
| 附件关联 | 合同附件可预览和下载 |
| 编译验证 | `mvn clean compile -pl yudao-module-opshub` 通过 |
| 前端验证 | `pnpm dev` 页面正常渲染（卡片+表格+图表+新建/编辑弹窗） |

---

## 九、后续阶段预留

| 功能 | 说明 | 预留阶段 |
|------|------|---------|
| 电子签章集成 | 对接法大大/上上签/e签宝 | 二期 |
| 合同 AI 解读 | NLP 条款提取，复用 AiInterpretModal 骨架 | 后续 Step |
| 合同导入/导出 | Excel 批量处理 | 后续 Step |
| 合同到期预警 | 定时任务扫描通知 | 后续 Step |

---

## 附录：与总体 PRD 的关系

```
┌──────────────────────────────────────────────────────────┐
│               经销商管理客服SaaS 分阶段实施                 │
├──────────┬───────────┬───────────┬───────────────────────┤
│  Step 1  │  Step 2   │  Step 3   │  Step 4               │
│  (done)  │  (done)   │  ★ 当前   │                       │
├──────────┼───────────┼───────────┼───────────────────────┤
│ 角色定义  │ 基础数据   │ 签约进度   │ 订单模块               │
│ 菜单权限  │ 文件管理   │ 合同录入   │ 订单全生命周期          │
│ 经销商管理│ 数据权限   │ 统计卡片   │ 付款/开票流程          │
│ 产品线管理│ AI 解读骨架│ 趋势图表   │                       │
│ 授权扩展表│           │ 签署操作   │                       │
│ 数据权限  │           │ 数据权限   │                       │
├──────────┴───────────┴───────────┴───────────────────────┤
│  后续 Step：政策看板 / 售后模块 / 客户服务 / AI 客服        │
└──────────────────────────────────────────────────────────┘
```
