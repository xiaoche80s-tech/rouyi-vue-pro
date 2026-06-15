# Step 5 — 售后模块实施 PRD

> **版本**: V1.0 | **日期**: 2026-06-15  
> **文档性质**: 分阶段实施 PRD — Step 5（售后模块）  
> **前置文档**: `docs/PRD-Step4-订单模块.md`（Step 4）、`docs/PRD-Step3-签约进度模块.md`（Step 3）、`docs/PRD-Step2-基础数据模块.md`（Step 2）、`docs/PRD-Step1-角色菜单经销商产品线.md`（Step 1）  
> **输入来源**: PRD V1.0 模块三 + PRD V2.0 售后模块（第六章） + UI 交互原型

---

## 一、Step 5 目标

在 Step 1/2/3/4 基础设施层之上，实现**售后模块**的退货/退换货/退货退款全流程管理：

| 目标 | 说明 |
|------|------|
| 售后数据模型 | `ops_aftersale_info` 主表 + `ops_aftersale_progress` 进度节点子表 |
| 9 种售后类型 | `handling_method`（退货/退换货/退货退款）× `reason`（投诉/召回/破损）的二维组合 |
| 5 大统计卡片 | 售后订单总数、退款、退货、已完成（含占比）、未完成（存在交叉计数逻辑） |
| 高级筛选 | 售后类型（9 种）、进度、产品线、经销商（角色联动）、搜索（售后单号/关联订单） |
| 售后单列表 | 分页查询、模糊搜索、多条件筛选、可排序列、进度节点 hover 预览 |
| 三种进度流 | 退货（5 节点）、退换货（5 节点）、退货退款（5 节点），按 handling_method 自动选择模板 |
| 进度管理 | 管理员/执行员推进进度节点，自动更新主表 current_step 和 progress_status |
| 售后入口 | 售后模块不含创建功能，所有售后数据由外部提供（DML 预置）；退货入口在 Step 4 订单模块的订单详情弹窗中，产品明细行下方增加"申请退货"按钮（仅已完成状态可见） |
| 编码规则 | `SO{YYYYMMDD}-{seq}`（如 `SO20260615-001`），当前阶段编码在 DML 中直接指定 |
| 数据权限 | 注册 `ops_aftersale_info` 到 `DealerDataPermissionRule`（dealer_code + product_line_code） |

**本阶段不包含**：售后单创建（数据由外部提供，DML 预置）、客服 AI 集成、操作请求工作流（售后审批 BPM）、物流 API 对接、红字发票电子化处理、退换货的新商品出库管理（均划归后续阶段）。

---

## 二、数据模型

### 2.1 主表 — ops_aftersale_info

继承 `TenantBaseDO`，PostgreSQL 语法。

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|:---:|------|
| `id` | int8 | PK | 主键（序列 `ops_aftersale_info_seq`） |
| `aftersale_code` | varchar(30) | Y | 售后单号（唯一，系统自动生成），如 `SO20260615-001` |
| `dealer_id` | int8 | Y | 经销商 ID（关联 `ops_dealer_info.id`） |
| `dealer_code` | varchar(50) | Y | 经销商编码（数据权限用） |
| `dealer_name` | varchar(100) | Y | 经销商名称（冗余存储，列表展示用） |
| `product_line_code` | varchar(50) | Y | 产品线编码（数据权限用） |
| `product_line_name` | varchar(100) | Y | 产品线名称（冗余存储） |
| `order_code` | varchar(30) | Y | 关联订单号（关联 `ops_order_info.order_code`） |
| `handling_method` | varchar(20) | Y | 处理方式：`return`（退货）/ `exchange`（退换货）/ `return_refund`（退货退款） |
| `reason` | varchar(20) | Y | 售后原因：`complaint`（投诉）/ `recall`（召回）/ `damage`（破损） |
| `progress_status` | varchar(20) | Y | 进度状态：`pending`（待处理）/ `in_progress`（进行中）/ `exchanging`（换货中）/ `completed`（已完成） |
| `current_step` | int | Y | 当前进度节点序号（1-5，默认 1） |
| `product_name` | varchar(200) | Y | 产品名称（冗余存储） |
| `product_spec` | varchar(100) | N | 产品规格型号 |
| `quantity` | int | Y | 售后数量 |
| `refund_amount` | numeric(15,2) | N | 退款金额（退货退款类型使用，默认 0） |
| `refund_status` | varchar(20) | N | 退款状态：`none` / `pending` / `refunded`（仅 return_refund 类型） |
| `red_invoice_status` | varchar(20) | N | 红字发票状态：`none` / `pending` / `issued`（退货/退货退款类型使用） |
| `logistics_company` | varchar(100) | N | 退回物流公司 |
| `logistics_no` | varchar(50) | N | 退回物流单号 |
| `exchange_logistics_company` | varchar(100) | N | 换货物流公司（仅 exchange 类型） |
| `exchange_logistics_no` | varchar(50) | N | 换货物流单号（仅 exchange 类型） |
| `apply_time` | timestamp | Y | 申请时间 |
| `approved_time` | timestamp | N | 审核通过时间 |
| `completed_time` | timestamp | N | 完成时间 |
| `remark` | varchar(500) | N | 备注 |
| 标准字段 | | | creator, create_time, updater, update_time, deleted, tenant_id |

**索引**：

| 索引名 | 类型 | 列 |
|--------|------|----|
| `uk_ops_aftersale_info_code` | UNIQUE | aftersale_code |
| `idx_ops_aftersale_info_dealer_code` | INDEX | dealer_code |
| `idx_ops_aftersale_info_pl_code` | INDEX | product_line_code |
| `idx_ops_aftersale_info_order_code` | INDEX | order_code |
| `idx_ops_aftersale_info_progress` | INDEX | progress_status |
| `idx_ops_aftersale_info_handling` | INDEX | handling_method |
| `idx_ops_aftersale_info_apply_time` | INDEX | apply_time |

### 2.2 进度节点子表 — ops_aftersale_progress

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|:---:|------|
| `id` | int8 | PK | 主键（序列 `ops_aftersale_progress_seq`） |
| `aftersale_id` | int8 | Y | 关联售后单 ID（`ops_aftersale_info.id`） |
| `aftersale_code` | varchar(30) | Y | 关联售后单号（业务字段冗余） |
| `node_code` | varchar(30) | Y | 节点编码：`submitted` / `reviewed` / `returned` / `refund_done` / `red_invoice` / `exchange_sent` / `received` |
| `node_name` | varchar(50) | Y | 节点名称（中文） |
| `node_time` | timestamp | N | 节点完成时间（为空表示未完成） |
| `is_completed` | boolean | Y | 是否完成（默认 false） |
| `sort_order` | int | Y | 排序序号（1-5） |
| `remark` | varchar(500) | N | 节点备注（如物流信息描述） |
| 标准字段 | | | creator, create_time, updater, update_time, deleted, tenant_id |

**索引**：`idx_ops_aftersale_progress_code` ON aftersale_code

### 2.3 售后编码规则

售后编码格式为 `SO{YYYYMMDD}-{seq}`（如 `SO20260615-001`）。当前阶段**不包含售后单创建功能**，所有售后数据由外部提供，编码在 DML INSERT 时直接指定。售后单创建（含编码自动生成逻辑）留待后续 Step 实现。

### 2.4 进度节点模板

创建售后单时，根据 `handling_method` 自动初始化 5 个进度节点到 `ops_aftersale_progress` 表：

**退货（return）— 5 节点**：

| sort_order | node_code | node_name | 说明 |
|:---:|---|---|---|
| 1 | `submitted` | 申请提交 | 创建时自动完成 |
| 2 | `reviewed` | 审核通过 | 管理员/执行员审核 |
| 3 | `returned` | 商品退回 | 经销商退回商品，记录物流 |
| 4 | `refund_done` | 退款完成 | 退货类型此节点完成后即结束 |
| 5 | `red_invoice` | 红字发票 | 红字发票开具完成 |

**退换货（exchange）— 5 节点**：

| sort_order | node_code | node_name | 说明 |
|:---:|---|---|---|
| 1 | `submitted` | 申请提交 | 创建时自动完成 |
| 2 | `reviewed` | 审核通过 | 管理员/执行员审核 |
| 3 | `returned` | 问题商品退回 | 经销商退回问题商品 |
| 4 | `exchange_sent` | 新商品发出 | 管理员/执行员发出换货商品 |
| 5 | `received` | 确认收货 | 经销商确认收到新商品 |

**退货退款（return_refund）— 5 节点**：

| sort_order | node_code | node_name | 说明 |
|:---:|---|---|---|
| 1 | `submitted` | 申请提交 | 创建时自动完成 |
| 2 | `reviewed` | 审核通过 | 管理员/执行员审核 |
| 3 | `returned` | 商品退回 | 经销商退回商品 |
| 4 | `refund_done` | 退款完成 | 退款到账确认 |
| 5 | `red_invoice` | 红字发票 | 红字发票开具完成 |

### 2.5 progress_status 状态流转

```
                   ┌───────────┐
                   │  pending   │  ← 创建售后单，等待审核
                   │  待处理     │
                   └─────┬─────┘
                         │ 审核通过（step 2 完成）
            ┌────────────┼────────────┐
            ▼            ▼            ▼
     ┌────────────┐ ┌──────────┐ ┌──────────────┐
     │ in_progress│ │ exchanging│ │  in_progress │
     │ 进行中(退货) │ │ 换货中    │ │ 进行中(退款)  │
     └─────┬──────┘ └────┬─────┘ └──────┬───────┘
            │             │              │
            │ 全部节点完成  │ 全部节点完成  │ 全部节点完成
            ▼             ▼              ▼
     ┌─────────────────────────────────────────┐
     │              completed 已完成             │
     └─────────────────────────────────────────┘
```

| handling_method | 审核前 | 审核后至完成前 | 完成后 |
|---|---|---|---|
| `return`（退货） | pending | in_progress | completed |
| `exchange`（退换货） | pending | exchanging | completed |
| `return_refund`（退货退款） | pending | in_progress | completed |

### 2.6 统计卡片交叉计数规则

| 卡片 | 计数逻辑 | SQL 条件 |
|------|---------|---------|
| 售后订单总数 | 全部售后单 | `COUNT(*)` |
| 退款 | 含退款的售后单 | `WHERE handling_method = 'return_refund'` |
| 退货 | 含退货行为的售后单 | `WHERE handling_method IN ('return', 'exchange', 'return_refund')`（即全部） |
| 已完成 | 已完成的售后单 + 占比% | `WHERE progress_status = 'completed'`；占比 = completed / total × 100 |
| 未完成 | 未完成的售后单 | `WHERE progress_status IN ('pending', 'in_progress', 'exchanging')` |

> **关键规则**：退款和退货计数存在交叉 — `return_refund` 类型同时计入"退款"和"退货"两类。"退货"包含全部三种 handling_method。

### 2.7 枚举定义

```java
// AfterSaleHandlingMethodEnum
RETURN("return", "退货"),
EXCHANGE("exchange", "退换货"),
RETURN_REFUND("return_refund", "退货退款");

// AfterSaleReasonEnum
COMPLAINT("complaint", "投诉"),
RECALL("recall", "召回"),
DAMAGE("damage", "破损");

// AfterSaleProgressStatusEnum
PENDING("pending", "待处理"),
IN_PROGRESS("in_progress", "进行中"),
EXCHANGING("exchanging", "换货中"),
COMPLETED("completed", "已完成");

// AfterSaleNodeCodeEnum
SUBMITTED("submitted", "申请提交"),
REVIEWED("reviewed", "审核通过"),
RETURNED("returned", "商品退回"),
REFUND_DONE("refund_done", "退款完成"),
RED_INVOICE("red_invoice", "红字发票"),
EXCHANGE_SENT("exchange_sent", "新商品发出"),
RECEIVED("received", "确认收货");

// AfterSaleRefundStatusEnum
NONE("none", "无"),
PENDING("pending", "退款中"),
REFUNDED("refunded", "已退款");

// AfterSaleRedInvoiceStatusEnum
NONE("none", "无"),
PENDING("pending", "待开"),
ISSUED("issued", "已开");
```

### 2.8 设计决策

| 决策项 | 选择 | 理由 |
|--------|------|------|
| handling_method + reason 双字段 | 不使用单一 type 字段存 9 种值 | 二维拆分支持独立筛选和统计，前端可组合显示 |
| 进度节点独立子表 | 不使用 JSON 字段 | 节点需独立更新、查询、按时间排序；JSON 字段更新粒度太粗 |
| progress_status 四状态 | pending/in_progress/exchanging/completed | PRD V2.0 筛选器明确定义 4 种进度状态 |
| exchanging 仅 exchange 类型使用 | in_progress 用于 return 和 return_refund | 区分退换货和普通退货/退货退款的进行中状态 |
| dealer_name/product_line_name | 冗余存储在主表 | 列表展示高频使用，避免每次 JOIN 查询（与 Step 4 一致） |
| 物流字段直接存储在主表 | 不单独建物流子表 | 售后物流信息简单（退回 + 换货发出），无需独立子表 |
| 红字发票状态 | 独立字段 red_invoice_status | 符合中国增值税发票管理规范，退货和退货退款均需红字发票 |

---

## 三、后端实现

### 3.1 新增文件清单

| # | 文件路径 | 说明 |
|---|---------|------|
| 1 | `enums/AfterSaleHandlingMethodEnum.java` | 处理方式枚举 |
| 2 | `enums/AfterSaleReasonEnum.java` | 售后原因枚举 |
| 3 | `enums/AfterSaleProgressStatusEnum.java` | 进度状态枚举 |
| 4 | `enums/AfterSaleNodeCodeEnum.java` | 进度节点编码枚举 |
| 5 | `enums/AfterSaleRefundStatusEnum.java` | 退款状态枚举 |
| 6 | `enums/AfterSaleRedInvoiceStatusEnum.java` | 红字发票状态枚举 |
| 7 | `dal/dataobject/aftersale/AfterSaleInfoDO.java` | 售后主表 DO |
| 8 | `dal/dataobject/aftersale/AfterSaleProgressDO.java` | 进度节点 DO |
| 9 | `dal/mysql/aftersale/AfterSaleInfoMapper.java` | 售后主表 Mapper |
| 10 | `dal/mysql/aftersale/AfterSaleProgressMapper.java` | 进度节点 Mapper |
| 11 | `controller/admin/aftersale/vo/AfterSaleInfoPageReqVO.java` | 分页请求 VO |
| 12 | `controller/admin/aftersale/vo/AfterSaleInfoRespVO.java` | 售后响应 VO |
| 13 | `controller/admin/aftersale/vo/AfterSaleInfoSimpleRespVO.java` | 售后简要响应 VO（列表用） |
| 14 | `controller/admin/aftersale/vo/AfterSaleStatisticsRespVO.java` | 统计卡片响应 VO |
| 15 | `controller/admin/aftersale/vo/AfterSaleDetailRespVO.java` | 售后详情响应 VO（含进度节点） |
| 16 | `controller/admin/aftersale/vo/AfterSaleUpdateProgressReqVO.java` | 更新进度请求 VO |
| 17 | `service/aftersale/AfterSaleInfoService.java` | 售后 Service 接口 |
| 18 | `service/aftersale/impl/AfterSaleInfoServiceImpl.java` | 售后 Service 实现 |
| 19 | `controller/admin/aftersale/AfterSaleInfoController.java` | REST Controller |

> 所有文件在 `yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/` 下

### 3.2 修改文件清单

| # | 文件路径 | 修改内容 |
|---|---------|---------|
| 1 | `enums/ErrorCodeConstants.java` | 追加售后模块错误码（1-050-007-xxx 段） |
| 2 | `framework/datapermission/config/OpshubDataPermissionConfiguration.java` | 注册 `ops_aftersale_info` 的 dealer_code + product_line_code |

### 3.3 错误码定义

```java
// ========== 售后模块 1-050-007-xxx ==========
ErrorCode AFTERSALE_NOT_EXISTS              = new ErrorCode(1_050_007_000, "售后单不存在");
ErrorCode AFTERSALE_CODE_DUPLICATE          = new ErrorCode(1_050_007_001, "售后单号已存在");
ErrorCode AFTERSALE_DEALER_NOT_EXISTS       = new ErrorCode(1_050_007_002, "关联经销商不存在");
ErrorCode AFTERSALE_ALREADY_COMPLETED       = new ErrorCode(1_050_007_003, "售后单已完成，不可继续操作");
ErrorCode AFTERSALE_PROGRESS_NOT_EXISTS     = new ErrorCode(1_050_007_004, "进度节点不存在");
ErrorCode AFTERSALE_PROGRESS_ALREADY_DONE   = new ErrorCode(1_050_007_005, "该进度节点已完成");
ErrorCode AFTERSALE_PROGRESS_ORDER_ERROR    = new ErrorCode(1_050_007_006, "进度节点顺序错误，需按序完成");
ErrorCode AFTERSALE_REFUND_NOT_APPLICABLE   = new ErrorCode(1_050_007_007, "该售后类型不涉及退款");
ErrorCode AFTERSALE_INVOICE_NOT_APPLICABLE  = new ErrorCode(1_050_007_008, "该售后类型不涉及红字发票");
```

### 3.4 REST API 接口

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/opshub/aftersale/page` | `dealer:aftersale:query` | 分页查询售后单列表 |
| GET | `/opshub/aftersale/get?id=` | `dealer:aftersale:query` | 获取售后单详情（含进度节点） |
| GET | `/opshub/aftersale/statistics` | `dealer:aftersale:query` | 5 大统计卡片数据 |
| PUT | `/opshub/aftersale/update-progress` | `dealer:aftersale:update-progress` | 推进进度节点（管理员/执行员） |
| POST | `/opshub/aftersale/batch-consult` | `dealer:aftersale:consult` | 批量咨询服务 |

> **注意**：售后模块不包含创建售后单接口，所有售后数据由外部提供（DML 预置）。退货入口在 Step 4 订单模块的订单详情弹窗中。

### 3.5 数据权限集成

在 `OpshubDataPermissionConfiguration` 中追加：

```java
// Step 5：注册售后表
rule.addDealerColumn("ops_aftersale_info");
rule.addProductLineColumn("ops_aftersale_info");
```

### 3.6 分页请求 VO

```java
@Data @EqualsAndHashCode(callSuper = true)
public class AfterSaleInfoPageReqVO extends PageParam {
    private String handlingMethod;       // return/exchange/return_refund
    private String reason;              // complaint/recall/damage
    private String progressStatus;      // pending/in_progress/exchanging/completed
    private List<String> productLineCodes;
    private List<String> dealerCodes;   // 管理员/执行员可见
    private String keyword;             // 模糊搜索（售后单号/关联订单号）
    private String sortField;           // 排序字段：aftersale_code/order_code/apply_time/handling_method/product_name/progress_status/dealer_name
    private String sortOrder;           // asc/desc
}
```

### 3.7 统计卡片响应 VO

```java
@Data
public class AfterSaleStatisticsRespVO {
    private Integer totalCount;         // 售后订单总数
    private Integer refundCount;        // 退款（handling_method = return_refund）
    private Integer returnCount;        // 退货（全部 handling_method）
    private Integer completedCount;     // 已完成
    private BigDecimal completedRate;   // 已完成占比（%）
    private Integer inProgressCount;    // 未完成（pending + in_progress + exchanging）
}
```

Mapper 层使用 `GROUP BY handling_method` 和 `GROUP BY progress_status` 分别聚合，两次查询完成。

### 3.8 售后详情响应 VO

```java
@Data
public class AfterSaleDetailRespVO {
    // === 基本信息 ===
    private Long id;
    private String aftersaleCode;
    private String dealerName;
    private String productLineName;
    private String orderCode;
    private String handlingMethod;
    private String handlingMethodName;   // 中文：退货/退换货/退货退款
    private String reason;
    private String reasonName;           // 中文：投诉/召回/破损
    private String progressStatus;
    private Integer currentStep;
    private String productName;
    private String productSpec;
    private Integer quantity;
    private BigDecimal refundAmount;
    private String refundStatus;
    private String redInvoiceStatus;
    private String logisticsCompany;
    private String logisticsNo;
    private String exchangeLogisticsCompany;
    private String exchangeLogisticsNo;
    private LocalDateTime applyTime;
    private LocalDateTime approvedTime;
    private LocalDateTime completedTime;
    private String remark;

    // === 进度节点 ===
    private List<AfterSaleProgressDO> progressNodes;
}
```

### 3.9 更新进度请求 VO

```java
@Data
public class AfterSaleUpdateProgressReqVO {
    @NotNull
    private Long aftersaleId;            // 售后单 ID
    @NotNull
    private Integer stepOrder;           // 要完成的节点序号（2-5）
    private String remark;              // 节点备注（如物流信息）
    private String logisticsCompany;     // 物流公司（商品退回/换货发出节点）
    private String logisticsNo;          // 物流单号
    private BigDecimal refundAmount;     // 退款金额（退款完成节点回填）
    private String redInvoiceNo;        // 红字发票号（红字发票节点回填）
}
```

### 3.10 Service 核心逻辑

**更新进度流程**：

1. 校验售后单存在且 progress_status != `completed`
2. 校验 stepOrder == current_step + 1（必须按序推进）
3. 校验对应节点存在且未完成
4. 更新节点 is_completed = true，填充 node_time 和 remark
5. 更新主表 current_step = stepOrder
6. 如果 stepOrder == 2（审核通过）→ progress_status 按 handling_method 设置：exchange → `exchanging`，其他 → `in_progress`；同时填充 approved_time
7. 如果 stepOrder == 5（最后节点完成）→ progress_status = `completed`，填充 completed_time
8. 特殊节点处理：商品退回节点更新 logistics_company/logistics_no；换货发出节点更新 exchange_logistics_company/exchange_logistics_no

### 3.11 依赖

- `DealerInfoService`：校验经销商存在性
- `DealerProductLineService`：校验产品线存在性

---

## 四、前端实现

### 4.1 新增文件清单

| # | 文件路径 | 说明 |
|---|---------|------|
| 1 | `src/api/opshub/aftersale/index.ts` | API 接口定义 |
| 2 | `src/views/opshub/aftersale/index.vue` | 主页面 |
| 3 | `src/views/opshub/aftersale/components/AfterSaleStatisticsCards.vue` | 5 大统计卡片 |
| 4 | `src/views/opshub/aftersale/components/AfterSaleFilterBar.vue` | 筛选栏 |
| 5 | `src/views/opshub/aftersale/components/AfterSaleTable.vue` | 售后单列表表格 |
| 6 | `src/views/opshub/aftersale/components/AfterSaleDetailModal.vue` | 售后详情弹窗（含进度时间线） |
| 7 | `src/views/opshub/aftersale/components/AfterSaleProgressHover.vue` | 进度节点 hover 预览组件 |
| 8 | `src/views/opshub/aftersale/components/UpdateProgressModal.vue` | 推进进度弹窗 |

### 4.2 页面布局

```
┌──────────────────────────────────────────────────────────────┐
│ [售后订单总数] [退款] [退货] [已完成 xx%] [未完成]                  │
├──────────────────────────────────────────────────────────────┤
│ [售后类型▼] [进度▼] [产品线▼] [经销商▼(管理)] [搜索售后单号/订单] [重置] │
├──────────────────────────────────────────────────────────────┤
│ [批量咨询服务]                                                   │
├──────────────────────────────────────────────────────────────┤
│ □ | 经销商(管理) | 售后单号 | 关联订单 | 时间 | 类型 | 产品 |     │
│   | 当前进度 | 进度详情(hover) | 操作 [💬客服] [更新进度]          │
├──────────────────────────────────────────────────────────────┤
│ 共 N 条售后单              第 1/N 页  每页 10 条                  │
└──────────────────────────────────────────────────────────────┘
```

### 4.3 关键交互

| 功能 | 实现 | 权限控制 |
|------|------|---------|
| 统计卡片 | `/opshub/aftersale/statistics`，5 张 `el-card` 一行排列，可点击筛选 | `dealer:aftersale:query` |
| 统计卡片点击 — 退款 | 筛选 handling_method=return_refund | — |
| 统计卡片点击 — 退货 | 筛选全部（即全部 handling_method） | — |
| 统计卡片点击 — 已完成 | 筛选 progress_status=completed | — |
| 统计卡片点击 — 未完成 | 筛选 progress_status IN (pending, in_progress, exchanging) | — |
| 售后类型筛选 | `el-select`，9 种组合 = handling_method × reason 的下拉选项 | — |
| 进度筛选 | `el-select`，4 种状态 | — |
| 经销商筛选 | `el-select` multiple，仅管理员/执行员可见 | 前端角色判断 |
| 搜索 | `el-input` keyword 参数，跨售后单号/关联订单匹配 | — |
| 列排序 | 可排序列：售后单号/关联订单/时间/类型/产品/当前进度/经销商，点击表头切换 asc/desc | — |
| 重置按钮 | 清空所有筛选条件 | — |
| 进度详情 hover | `el-popover` + AfterSaleProgressHover：展示 5 个步骤节点名称和完成时间 | — |
| 当前进度列 | 显示进度条/步骤指示器，如 "3/5 商品退回"，不同 handling_method 显示不同颜色 | — |
| 行操作 — 客服 | 💬 按钮，打开 AI 售后咨询（预留骨架） | `dealer:aftersale:consult` |
| 行操作 — 更新进度 | 管理员/执行员可推进下一个进度节点 | `dealer:aftersale:update-progress` |
| 批量咨询 | 勾选后操作，未勾选提示"请先选择" | `dealer:aftersale:consult` |
| 分页 | 默认 10 条 | — |

### 4.4 售后详情弹窗设计

```
┌─────────────────────────────────────────────────┐
│ 售后单号: SO20260615-001    关联订单: ORD-2026-0613-001 │
│ 经销商: 华康医疗器械    产品线: 骨科                       │
│ 类型: 退换货-投诉    产品: 膝关节假体 × 5                │
├─────────────────────────────────────────────────┤
│ 进度时间线（水平 el-steps）：                          │
│ ✅申请提交 → ✅审核通过 → ⏳问题商品退回 → ○新商品发出 → ○确认收货 │
│ (06-15)    (06-16)    (进行中)                       │
├─────────────────────────────────────────────────┤
│ 物流信息：退回物流 顺丰 SF533374                        │
│ 退款信息：退款金额 ¥15,000  状态：退款中                 │
│ 红字发票：待开                                       │
├─────────────────────────────────────────────────┤
│ [推进进度(管理/执行)] [咨询客服]                        │
└─────────────────────────────────────────────────┘
```

### 4.5 更新进度弹窗

| 字段 | 组件 | 必填 | 说明 |
|------|------|:---:|------|
| 当前步骤 | 只读显示 | — | 显示下一步节点名称 |
| 备注 | `el-input` textarea | N | — |
| 物流公司 | `el-input` | 条件 | 商品退回/新商品发出节点必填 |
| 物流单号 | `el-input` | 条件 | 商品退回/新商品发出节点必填 |
| 退款金额 | `el-input-number` | 条件 | 退款完成节点回填 |
| 红字发票号 | `el-input` | 条件 | 红字发票节点回填 |

### 4.6 类型列显示格式

表格中"类型"列显示 `handlingMethodName-reasonName` 组合文本，如：
- 退货-投诉、退货-召回、退货-破损
- 退换货-投诉、退换货-召回、退换货-破损
- 退货退款-投诉、退货退款-召回、退货退款-破损

### 4.7 菜单更新

更新菜单 id=6005（售后模块）的 `component` 为 `opshub/aftersale/index`。

新增按钮权限 DML（id=6053，parent_id=6005）：
- `dealer:aftersale:update-progress`（管理员/执行员更新进度）

### 4.8 订单模块退货入口（Step 4 增强）

在 Step 4 订单详情弹窗的 **退货信息 Tab** 中，产品明细行列表下方增加"申请退货"按钮：

| 要素 | 说明 |
|------|------|
| 位置 | 订单详情弹窗 → 退货信息 Tab → 可退货商品明细表下方 |
| 可见条件 | 仅当订单 `progress_status = completed`（已完成）时显示 |
| 角色 | 经销商视角可见 |
| 权限 | 复用 Step 4 已有的 `dealer:order:return` |
| 点击行为 | 弹出申请退货表单（选择退货商品+数量），提交后由外部系统创建售后单 |

---

## 五、角色权限矩阵

### 5.1 按钮级权限

| 权限标识 | 说明 | 品牌管理员 | 品牌销售员 | 服务单执行员 | 经销商 |
|---------|------|:---------:|:---------:|:----------:|:-----:|
| `dealer:aftersale:query` | 查看售后列表 + 统计 | ✅ | ✅ | ✅ | ✅ |
| `dealer:aftersale:update-progress` | 推进进度节点 | ✅ | — | ✅ | — |
| `dealer:aftersale:consult` | 发起咨询 | ✅ | — | ✅ | ✅ |

### 5.2 UI 联动规则

| UI 元素 | 品牌管理员 | 品牌销售员 | 服务单执行员 | 经销商 |
|---------|-----------|-----------|-------------|-------|
| 经销商筛选器 | 可见 | 可见 | **隐藏** | **隐藏** |
| 表格经销商列 | 显示 | 显示 | 显示 | **隐藏** |
| "更新进度"操作 | 显示 | 隐藏 | 显示 | 隐藏 |
| "客服"操作 | 显示 | 隐藏 | 显示 | 显示 |
| "批量咨询"按钮 | 显示 | 隐藏 | 显示 | 显示 |
| checkbox 列 | 显示 | 隐藏 | 显示 | 显示 |
| 操作列 | 更新进度+客服 | 只读 | 更新进度+客服 | 客服 |

---

## 六、SQL 脚本

### 6.1 DDL

PostgreSQL 语法：2 张 CREATE TABLE + ALTER TABLE (PK) + UNIQUE INDEX + 多个 INDEX + SEQUENCE + COMMENT。

表清单：
1. `ops_aftersale_info` — 售后主表
2. `ops_aftersale_progress` — 进度节点子表

### 6.2 DML

- 更新菜单 id=6005 的 `component` 为 `opshub/aftersale/index`
- 新增按钮权限 DML：
  - id=6053: `dealer:aftersale:update-progress`（更新进度，parent_id=6005）
- 将新增按钮权限分配给对应角色的 `system_role_menu` 记录
- 测试数据约 15-25 条，覆盖：
  - 3 种 handling_method × 3 种 reason = 9 种类型组合
  - 4 种进度状态混合（pending/in_progress/exchanging/completed）
  - 多个经销商 × 多个产品线
  - 每条售后单 5 个进度节点（第 1 个已完成，其余按进度状态部分完成）
  - 部分售后单含物流信息、退款信息、红字发票信息
  - 关联 `ops_order_info` 中已签收/已完成的订单

### 6.3 归档路径

`db/branches/feature_step5-售后模块/`
- `feature_step5-售后模块_ddl.sql`
- `feature_step5-售后模块_dml.sql`

---

## 七、与现有模块的交互

| 关联模块 | 关系 | 说明 |
|---------|------|------|
| Step 1 经销商/产品线 | `dealer_code` / `product_line_code` | 数据权限过滤依赖扩展表 |
| Step 4 订单模块 | `order_code` → `ops_order_info.order_code` | 售后单关联订单号（业务关联）；退货入口在订单详情弹窗（已完成状态可见，复用 `dealer:order:return`） |
| Step 2 基础数据 | 无直接依赖 | 售后产品与基础数据文件无直接关联 |
| Step 3 签约进度 | 无直接依赖 | 合同→订单→售后为业务链路，但数据层无直接外键关联 |
| 后续 Step（客户服务） | 售后 → 咨询/工单 | 客服按钮依赖客户服务模块数据（当前仅骨架） |
| 后续 Step（操作请求工作流） | 售后审批 | 售后审核通过后→执行→验收的工作流（当前进度由管理员/执行员直接推进） |

---

## 八、验证方式

| 验证项 | 验证方法 |
|--------|---------|
| 表创建 | DDL 执行后 `SELECT * FROM ops_aftersale_info` 和 `ops_aftersale_progress` 确认 2 张表均存在 |
| 菜单更新 | 前端登录确认"售后模块"菜单指向 `opshub/aftersale/index` |
| 按钮权限 | 确认 `dealer:aftersale:update-progress` 按钮按角色正确显隐 |
| 分页查询 | 4 种角色分别调用 `/opshub/aftersale/page`，验证数据和分页 |
| 数据权限 | dealer 仅看到授权经销商售后单；执行员仅看到授权产品线售后单 |
| 统计卡片 | `/opshub/aftersale/statistics` 验证 5 张卡片数据正确，特别是交叉计数（退款+退货重叠） |
| 筛选功能 | 验证所有筛选组合（售后类型 9 种、进度 4 种、产品线、经销商、关键词） |
| 列排序 | 验证售后单号/关联订单/时间/类型/产品/进度/经销商列的升降序排序 |
| 售后单详情 | `/opshub/aftersale/get` 验证详情数据 + 5 个进度节点完整返回 |
| 进度 hover | 表格行 hover 进度详情列，展示 5 个步骤名称和时间戳 |
| 订单退货入口 | 订单详情弹窗 → 退货信息 Tab → 已完成订单显示"申请退货"按钮（经销商视角） |
| 进度推进 | 管理员/执行员逐步推进 5 个节点，验证 current_step/progress_status 自动更新 |
| 状态流转 | 验证 pending → in_progress/exchanging → completed 的完整状态流转 |
| 物流信息 | 商品退回和换货发出节点更新物流公司/单号 |
| 退款/红字发票 | 退货退款类型的退款完成节点和红字发票节点更新 |
| 编译验证 | `mvn clean compile -pl yudao-module-opshub` 通过 |
| 前端验证 | `pnpm dev` 页面正常渲染（卡片+表格+详情弹窗+进度时间线+更新进度弹窗） |

---

## 九、后续阶段预留

| 功能 | 说明 | 预留阶段 |
|------|------|--------|
| 售后单创建 | 售后单 CRUD + 编码自动生成 + 从订单详情发起退货创建售后单 | 后续 Step |
| 客户服务集成 | 客服按钮对接 AI 售后咨询模块 | 后续 Step |
| 操作请求工作流 | 售后申请 → 操作请求 → 执行员审批 → 处理 → 验收的完整 BPM 工作流 | 后续 Step |
| 物流 API 对接 | 对接顺丰/中通等快递 API，自动查询物流轨迹 | 后续 Step |
| 红字发票电子化 | 对接税务系统，电子开具红字增值税发票 | 后续 Step |
| 退换货出库管理 | 换货新商品的出库管理和库存扣减 | 后续 Step |
| 售后分析报表 | 售后率、退货原因分布、经销商售后排名等分析图表 | 后续 Step |
| 售后导入/导出 | Excel 批量导入售后数据 | 后续 Step |
| 批量更新进度 | 批量审核通过/批量推进进度 | 后续 Step |

---

## 附录：与总体 PRD 的关系

```
┌──────────────────────────────────────────────────────────┐
│               经销商管理客服SaaS 分阶段实施                 │
├──────────┬───────────┬───────────┬───────────┬───────────┤
│  Step 1  │  Step 2   │  Step 3   │  Step 4   │  Step 5   │
│  (done)  │  (done)   │  (done)   │  (done)   │  ★ 当前   │
├──────────┼───────────┼───────────┼───────────┼───────────┤
│ 角色定义  │ 基础数据   │ 签约进度   │ 订单模块   │ 售后模块   │
│ 菜单权限  │ 文件管理   │ 合同录入   │ 统计卡片   │ 5大卡片   │
│ 经销商管理│ 数据权限   │ 统计卡片   │ 6Tab详情   │ 9种类型   │
│ 产品线管理│ AI 解读骨架│ 趋势图表   │ 付款/开票  │ 3种进度流  │
│ 授权扩展表│           │ 签署操作   │ 退货管理   │ 进度追踪   │
│ 数据权限  │           │ 数据权限   │ 数据权限   │ 数据权限   │
├──────────┴───────────┴───────────┴───────────┴───────────┤
│  后续 Step：政策看板 / 客户服务 / AI 客服 / 操作请求工作流   │
└──────────────────────────────────────────────────────────┘
```
