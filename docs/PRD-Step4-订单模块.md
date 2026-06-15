# Step 4 — 订单模块实施 PRD

> **版本**: V1.0 | **日期**: 2026-06-15  
> **文档性质**: 分阶段实施 PRD — Step 4（订单模块）  
> **前置文档**: `docs/PRD-Step3-签约进度模块.md`（Step 3）、`docs/PRD-Step2-基础数据模块.md`（Step 2）、`docs/PRD-Step1-角色菜单经销商产品线.md`（Step 1）  
> **输入来源**: PRD V1.0 模块四 + PRD V2.0 订单模块（第七章） + UI 交互原型

---

## 一、Step 4 目标

在 Step 1/2/3 基础设施层之上，实现**订单模块**的采购订单全生命周期管理：

| 目标 | 说明 |
|------|------|
| 订单数据模型 | `ops_order_info` 主表 + `ops_order_product` 产品明细子表 + `ops_order_timeline` 时间线子表 + `ops_order_payment` 付款记录子表 + `ops_order_invoice` 开票记录子表 + `ops_order_logistics` 物流轨迹子表 |
| 6 大统计卡片 | 订单总量、已完成、已付款、未付款、已开票、未开票 |
| 高级筛选 | 快捷时间（今日/本周/本月/全部）、季度、月度、产品线、经销商（角色联动）、进度、付款状态、开票状态、搜索 |
| 订单列表 | 分页查询、汇总金额、模糊搜索、多条件筛选、表头快捷筛选 |
| 订单详情弹窗 | 6 个 Tab（基本信息/付款信息/开票信息/退货信息/物流轨迹/沟通记录） |
| 订单生命周期 | 5 节点进度：待确认 → 已确认 → 已发货 → 已签收 → 已完成 |
| 付款流程 | 独立于订单进度：未付款 → 已付款，经销商发起申请 |
| 开票流程 | 独立于订单进度：未开票 → 部分开票 → 已开票，经销商发起申请 |
| 退货管理 | 已签收/已完成订单可发起退货，按商品明细退货 |
| 数据权限 | 注册 `ops_order_info` 到 `DealerDataPermissionRule`（dealer_code + product_line_code） |

**本阶段不包含**：管理员创建/编辑订单（留待后续 Step，订单数据通过 DML 预置）、政策看板、售后模块、客户服务工单、AI 智能客服、操作请求工作流（均划归后续阶段）。

---

## 二、数据模型

### 2.1 主表 — ops_order_info

继承 `TenantBaseDO`，PostgreSQL 语法。

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|:---:|------|
| `id` | int8 | PK | 主键（序列 `ops_order_info_seq`） |
| `order_code` | varchar(30) | Y | 订单号（唯一，系统自动生成），如 `ORD-2026-0613-001` |
| `dealer_id` | int8 | Y | 经销商 ID（关联 `ops_dealer_info.id`） |
| `dealer_code` | varchar(50) | Y | 经销商编码（数据权限用） |
| `dealer_name` | varchar(100) | Y | 经销商名称（冗余存储，列表展示用） |
| `product_line_code` | varchar(50) | Y | 产品线编码（数据权限用） |
| `product_line_name` | varchar(100) | Y | 产品线名称（冗余存储） |
| `total_amount` | numeric(15,2) | Y | 订单总金额 |
| `order_date` | date | Y | 订单日期 |
| `progress_status` | varchar(20) | Y | 进度状态：`pending` / `confirmed` / `shipped` / `signed` / `completed` |
| `pay_status` | varchar(20) | Y | 付款状态：`unpaid` / `paid` |
| `paid_amount` | numeric(15,2) | N | 已付金额（默认 0） |
| `inv_status` | varchar(20) | Y | 开票状态：`uninvoiced` / `partial` / `invoiced` |
| `invoiced_amount` | numeric(15,2) | N | 已开票金额（默认 0） |
| `confirmed_time` | timestamp | N | 确认时间 |
| `shipped_time` | timestamp | N | 发货时间 |
| `signed_time` | timestamp | N | 签收时间 |
| `completed_time` | timestamp | N | 完成时间 |
| `remark` | varchar(500) | N | 备注 |
| 标准字段 | | | creator, create_time, updater, update_time, deleted, tenant_id |

**索引**：

| 索引名 | 类型 | 列 |
|--------|------|----|
| `uk_ops_order_info_code` | UNIQUE | order_code |
| `idx_ops_order_info_dealer_code` | INDEX | dealer_code |
| `idx_ops_order_info_pl_code` | INDEX | product_line_code |
| `idx_ops_order_info_progress` | INDEX | progress_status |
| `idx_ops_order_info_pay` | INDEX | pay_status |
| `idx_ops_order_info_inv` | INDEX | inv_status |
| `idx_ops_order_info_date` | INDEX | order_date |

### 2.2 产品明细子表 — ops_order_product

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|:---:|------|
| `id` | int8 | PK | 主键（序列 `ops_order_product_seq`） |
| `order_id` | int8 | Y | 关联订单 ID（`ops_order_info.id`） |
| `order_code` | varchar(30) | Y | 关联订单号（业务字段冗余） |
| `product_name` | varchar(200) | Y | 产品名称 |
| `spec_model` | varchar(100) | N | 规格型号 |
| `unit_price` | numeric(12,2) | Y | 单价 |
| `quantity` | int | Y | 数量 |
| `unit` | varchar(20) | Y | 单位（件/台/套等） |
| `amount` | numeric(15,2) | Y | 金额（= 单价 × 数量） |
| `returnable_qty` | int | N | 可退货数量（默认 = quantity，退货后扣减） |
| 标准字段 | | | creator, create_time, updater, update_time, deleted, tenant_id |

**索引**：`idx_ops_order_product_order_code` ON order_code

### 2.3 时间线子表 — ops_order_timeline

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|:---:|------|
| `id` | int8 | PK | 主键（序列 `ops_order_timeline_seq`） |
| `order_id` | int8 | Y | 关联订单 ID |
| `order_code` | varchar(30) | Y | 关联订单号 |
| `node_code` | varchar(30) | Y | 节点编码：`created` / `confirmed` / `shipped` / `signed` / `completed` |
| `node_name` | varchar(50) | Y | 节点名称：下单 / 已确认 / 已发货 / 已签收 / 已完成 |
| `node_time` | timestamp | N | 节点完成时间（为空表示未完成） |
| `is_completed` | boolean | Y | 是否完成（默认 false） |
| `sort_order` | int | Y | 排序序号（1-5） |
| 标准字段 | | | creator, create_time, updater, update_time, deleted, tenant_id |

### 2.4 付款记录子表 — ops_order_payment

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|:---:|------|
| `id` | int8 | PK | 主键（序列 `ops_order_payment_seq`） |
| `order_id` | int8 | Y | 关联订单 ID |
| `order_code` | varchar(30) | Y | 关联订单号 |
| `pay_amount` | numeric(15,2) | Y | 付款金额 |
| `pay_date` | date | N | 付款日期 |
| `pay_method` | varchar(50) | N | 付款方式（银行转账/支票等） |
| `voucher_no` | varchar(50) | N | 付款凭证号 |
| `status` | varchar(20) | Y | 状态：`pending`（审批中）/ `approved`（已通过）/ `rejected`（已拒绝） |
| `apply_time` | timestamp | Y | 申请时间 |
| `approve_time` | timestamp | N | 审批通过时间 |
| `remark` | varchar(500) | N | 备注 |
| 标准字段 | | | creator, create_time, updater, update_time, deleted, tenant_id |

### 2.5 开票记录子表 — ops_order_invoice

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|:---:|------|
| `id` | int8 | PK | 主键（序列 `ops_order_invoice_seq`） |
| `order_id` | int8 | Y | 关联订单 ID |
| `order_code` | varchar(30) | Y | 关联订单号 |
| `invoice_amount` | numeric(15,2) | Y | 开票金额 |
| `invoice_no` | varchar(50) | N | 发票号（管理员/执行员回填） |
| `invoice_date` | date | N | 开票日期 |
| `invoice_type` | varchar(50) | N | 发票类型（增值税专用/增值税普通） |
| `company_name` | varchar(200) | Y | 发票抬头 |
| `tax_no` | varchar(50) | Y | 纳税人识别号 |
| `special_request` | varchar(500) | N | 特殊开票需求 |
| `status` | varchar(20) | Y | 状态：`pending`（待开票）/ `invoiced`（已开票） |
| `apply_time` | timestamp | Y | 申请时间 |
| `remark` | varchar(500) | N | 备注 |
| 标准字段 | | | creator, create_time, updater, update_time, deleted, tenant_id |

### 2.6 物流轨迹子表 — ops_order_logistics

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|:---:|------|
| `id` | int8 | PK | 主键（序列 `ops_order_logistics_seq`） |
| `order_id` | int8 | Y | 关联订单 ID |
| `order_code` | varchar(30) | Y | 关联订单号 |
| `logistics_company` | varchar(100) | N | 物流公司 |
| `tracking_no` | varchar(50) | N | 物流单号 |
| `node_desc` | varchar(500) | Y | 节点描述 |
| `node_time` | timestamp | Y | 节点时间 |
| `is_completed` | boolean | Y | 是否已完成节点 |
| `sort_order` | int | Y | 排序序号 |
| 标准字段 | | | creator, create_time, updater, update_time, deleted, tenant_id |

### 2.7 订单编码规则

订单编码格式为 `ORD-{year}-{month_day}-{seq}`（如 `ORD-2026-0613-001`），当前阶段订单数据全部通过 DML 测试数据预置，编码在 INSERT 时直接指定。管理员创建订单（含编码自动生成）留待后续 Step 实现。

### 2.8 枚举定义

```java
// OrderProgressStatusEnum
PENDING("pending", "待确认"),
CONFIRMED("confirmed", "已确认"),
SHIPPED("shipped", "已发货"),
SIGNED("signed", "已签收"),
COMPLETED("completed", "已完成");

// OrderPayStatusEnum
UNPAID("unpaid", "未付款"),
PAID("paid", "已付款");

// OrderInvoiceStatusEnum
UNINVOICED("uninvoiced", "未开票"),
PARTIAL("partial", "部分开票"),
INVOICED("invoiced", "已开票");

// OrderPaymentStatusEnum（付款记录）
PENDING("pending", "审批中"),
APPROVED("approved", "已通过"),
REJECTED("rejected", "已拒绝");

// OrderInvoiceRecordStatusEnum（开票记录）
PENDING("pending", "待开票"),
INVOICED("invoiced", "已开票");
```

### 2.9 设计决策

| 决策项 | 选择 | 理由 |
|--------|------|------|
| 表名 `ops_order_info` | 不使用 `ops_order` | `order` 是 PostgreSQL/MySQL 保留关键字，禁止用作表名 |
| 进度/付款/开票三条独立线 | progress_status + pay_status + inv_status 三个独立字段 | PRD 明确定义付款和开票是两个独立流程，可交叉进行 |
| 子表拆分为 5 张 | product/timeline/payment/invoice/logistics | 各子表数据量和更新频率不同，拆表便于独立查询和维护 |
| dealer_name/product_line_name | 冗余存储在主表 | 列表展示高频使用，避免每次 JOIN 查询 |
| 退货功能 | 基于 ops_order_product.returnable_qty 字段 | 退货按商品明细粒度，可退货数量实时扣减 |
| 沟通记录 | Step 4 不建表 | 沟通记录依赖客户服务模块（后续 Step），当前仅在 UI 展示骨架 |

---

## 三、后端实现

### 3.1 新增文件清单

| # | 文件路径 | 说明 |
|---|---------|------|
| 1 | `enums/OrderProgressStatusEnum.java` | 订单进度状态枚举 |
| 2 | `enums/OrderPayStatusEnum.java` | 订单付款状态枚举 |
| 3 | `enums/OrderInvoiceStatusEnum.java` | 订单开票状态枚举 |
| 4 | `dal/dataobject/order/OrderInfoDO.java` | 订单主表 DO |
| 5 | `dal/dataobject/order/OrderProductDO.java` | 订单产品明细 DO |
| 6 | `dal/dataobject/order/OrderTimelineDO.java` | 订单时间线 DO |
| 7 | `dal/dataobject/order/OrderPaymentDO.java` | 订单付款记录 DO |
| 8 | `dal/dataobject/order/OrderInvoiceDO.java` | 订单开票记录 DO |
| 9 | `dal/dataobject/order/OrderLogisticsDO.java` | 订单物流轨迹 DO |
| 10 | `dal/mysql/order/OrderInfoMapper.java` | 订单主表 Mapper |
| 11 | `dal/mysql/order/OrderProductMapper.java` | 产品明细 Mapper |
| 12 | `dal/mysql/order/OrderTimelineMapper.java` | 时间线 Mapper |
| 13 | `dal/mysql/order/OrderPaymentMapper.java` | 付款记录 Mapper |
| 14 | `dal/mysql/order/OrderInvoiceMapper.java` | 开票记录 Mapper |
| 15 | `dal/mysql/order/OrderLogisticsMapper.java` | 物流轨迹 Mapper |
| 16 | `controller/admin/order/vo/OrderInfoPageReqVO.java` | 分页请求 VO |
| 17 | `controller/admin/order/vo/OrderInfoRespVO.java` | 订单响应 VO |
| 18 | `controller/admin/order/vo/OrderInfoSimpleRespVO.java` | 订单简要响应 VO（列表用） |
| 19 | `controller/admin/order/vo/OrderStatisticsRespVO.java` | 统计卡片响应 VO |
| 20 | `controller/admin/order/vo/OrderDetailRespVO.java` | 订单详情响应 VO（含 6 Tab 数据） |
| 21 | `controller/admin/order/vo/OrderApplyPaymentReqVO.java` | 申请付款请求 VO |
| 22 | `controller/admin/order/vo/OrderApplyInvoiceReqVO.java` | 申请开票请求 VO |
| 23 | `controller/admin/order/vo/OrderApplyReturnReqVO.java` | 申请退货请求 VO |
| 24 | `service/order/OrderInfoService.java` | 订单 Service 接口 |
| 25 | `service/order/impl/OrderInfoServiceImpl.java` | 订单 Service 实现 |
| 26 | `controller/admin/order/OrderInfoController.java` | REST Controller |

> 所有文件在 `yudao-module-opshub/src/main/java/cn/iocoder/yudao/module/opshub/` 下

### 3.2 修改文件清单

| # | 文件路径 | 修改内容 |
|---|---------|---------|
| 1 | `enums/ErrorCodeConstants.java` | 追加订单模块错误码（1-050-006-xxx 段） |
| 2 | `framework/datapermission/config/OpshubDataPermissionConfiguration.java` | 注册 `ops_order_info` 的 dealer_code + product_line_code |

### 3.3 错误码定义

```java
// ========== 订单模块 1-050-006-xxx ==========
ErrorCode ORDER_NOT_EXISTS                = new ErrorCode(1_050_006_000, "订单不存在");
ErrorCode ORDER_CODE_DUPLICATE            = new ErrorCode(1_050_006_001, "订单号已存在");
ErrorCode ORDER_DEALER_NOT_EXISTS         = new ErrorCode(1_050_006_002, "关联经销商不存在");
ErrorCode ORDER_ALREADY_PAID              = new ErrorCode(1_050_006_003, "订单已付款，不可重复申请");
ErrorCode ORDER_ALREADY_INVOICED          = new ErrorCode(1_050_006_004, "订单已全部开票");
ErrorCode ORDER_NOT_SIGNED                = new ErrorCode(1_050_006_005, "订单未签收，不可申请退货");
ErrorCode ORDER_PRODUCT_NOT_EXISTS        = new ErrorCode(1_050_006_006, "订单产品明细不存在");
ErrorCode ORDER_RETURN_QTY_EXCEED         = new ErrorCode(1_050_006_007, "退货数量超过可退货数量");
ErrorCode ORDER_PAYMENT_PENDING           = new ErrorCode(1_050_006_008, "存在审批中的付款申请");
ErrorCode ORDER_INVOICE_PENDING           = new ErrorCode(1_050_006_009, "存在待开票的开票申请");
```

### 3.4 REST API 接口

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/opshub/order/page` | `dealer:order:query` | 分页查询订单列表 |
| GET | `/opshub/order/get?id=` | `dealer:order:query` | 获取订单详情（含 6 Tab 数据） |
| GET | `/opshub/order/statistics` | `dealer:order:query` | 6 大统计卡片数据 |
| PUT | `/opshub/order/update-progress` | `dealer:order:update` | 更新订单进度（管理员/执行员） |
| POST | `/opshub/order/apply-payment` | `dealer:order:pay` | 经销商申请付款 |
| POST | `/opshub/order/apply-invoice` | `dealer:order:invoice` | 经销商申请开票（含表单：抬头/税号/备注） |
| POST | `/opshub/order/apply-return` | `dealer:order:return` | 经销商申请退货（按产品明细退货） |
| POST | `/opshub/order/batch-apply-payment` | `dealer:order:pay` | 批量申请付款 |
| POST | `/opshub/order/batch-apply-invoice` | `dealer:order:invoice` | 批量申请开票 |
| POST | `/opshub/order/batch-apply-return` | `dealer:order:return` | 批量申请退货 |
| POST | `/opshub/order/batch-consult` | `dealer:order:consult` | 批量咨询服务 |

### 3.5 数据权限集成

在 `OpshubDataPermissionConfiguration` 中追加：

```java
// Step 4：注册订单表
rule.addDealerColumn("ops_order_info");
rule.addProductLineColumn("ops_order_info");
```

### 3.6 分页请求 VO

```java
@Data @EqualsAndHashCode(callSuper = true)
public class OrderInfoPageReqVO extends PageParam {
    private String quickTime;           // today/week/month/all
    private List<Integer> quarters;     // 1-4
    private List<Integer> months;       // 1-12
    private List<String> productLineCodes;
    private List<String> dealerCodes;   // 管理员/执行员可见
    private String progressStatus;      // pending/confirmed/shipped/signed/completed
    private String payStatus;           // unpaid/paid
    private String invStatus;           // uninvoiced/partial/invoiced
    private String keyword;             // 模糊搜索（订单号/产品线/经销商）
}
```

### 3.8 统计卡片响应 VO

```java
@Data
public class OrderStatisticsRespVO {
    private Integer totalCount;         // 订单总量
    private Integer completedCount;     // 已完成
    private Integer paidCount;          // 已付款
    private Integer unpaidCount;        // 未付款
    private Integer invoicedCount;      // 已开票
    private Integer uninvoicedCount;    // 未开票
}
```

Mapper 层使用 `GROUP BY` 分别按 progress_status / pay_status / inv_status 聚合，一次查询完成。

### 3.9 订单详情响应 VO

```java
@Data
public class OrderDetailRespVO {
    // === 基本信息 ===
    private Long id;
    private String orderCode;
    private String dealerName;
    private String productLineName;
    private BigDecimal totalAmount;
    private LocalDate orderDate;
    private String progressStatus;
    private String payStatus;
    private String invStatus;
    private BigDecimal paidAmount;
    private BigDecimal invoicedAmount;

    // === 时间线 ===
    private List<OrderTimelineDO> timeline;

    // === 产品明细 ===
    private List<OrderProductDO> products;

    // === 付款信息 ===
    private List<OrderPaymentDO> payments;

    // === 开票信息 ===
    private List<OrderInvoiceDO> invoices;

    // === 物流轨迹 ===
    private List<OrderLogisticsDO> logistics;

    // === 退货信息（从 product 表计算） ===
    private List<ReturnableProduct> returnableProducts;

    @Data
    public static class ReturnableProduct {
        private Long productId;
        private String productName;
        private String specModel;
        private Integer quantity;
        private Integer returnableQty;
        private BigDecimal unitPrice;
        private BigDecimal amount;
    }
}
```

### 3.10 申请付款/开票/退货请求 VO

```java
@Data
public class OrderApplyPaymentReqVO {
    @NotNull
    private Long orderId;
    private String remark;              // 备注
}

@Data
public class OrderApplyInvoiceReqVO {
    @NotNull
    private Long orderId;
    @NotBlank
    private String companyName;         // 发票抬头
    @NotBlank
    private String taxNo;               // 纳税人识别号
    private String specialRequest;      // 特殊开票需求
}

@Data
public class OrderApplyReturnReqVO {
    @NotNull
    private Long orderId;
    @NotEmpty
    private List<ReturnItem> items;

    @Data
    public static class ReturnItem {
        @NotNull
        private Long productId;         // 产品明细 ID
        @NotNull
        private Integer returnQty;      // 退货数量
    }
}
```

### 3.11 依赖

- `DealerInfoService`：填充经销商名称、校验经销商存在性
- `DealerProductLineService`：填充产品线名称、校验产品线存在性
- `FileApi`（yudao-module-infra）：物流凭证文件上传（后续扩展）

---

## 四、前端实现

### 4.1 新增文件清单

| # | 文件路径 | 说明 |
|---|---------|------|
| 1 | `src/api/opshub/order/index.ts` | API 接口定义 |
| 2 | `src/views/opshub/order/index.vue` | 主页面 |
| 3 | `src/views/opshub/order/components/OrderStatisticsCards.vue` | 6 大统计卡片 |
| 4 | `src/views/opshub/order/components/OrderFilterBar.vue` | 筛选栏（含快捷时间） |
| 5 | `src/views/opshub/order/components/OrderTable.vue` | 订单列表表格 |
| 6 | `src/views/opshub/order/components/OrderDetailModal.vue` | 订单详情弹窗（6 Tab） |
| 7 | `src/views/opshub/order/components/tabs/BasicInfoTab.vue` | 基本信息 Tab |
| 8 | `src/views/opshub/order/components/tabs/PaymentInfoTab.vue` | 付款信息 Tab |
| 9 | `src/views/opshub/order/components/tabs/InvoiceInfoTab.vue` | 开票信息 Tab |
| 10 | `src/views/opshub/order/components/tabs/ReturnInfoTab.vue` | 退货信息 Tab |
| 11 | `src/views/opshub/order/components/tabs/LogisticsTab.vue` | 物流轨迹 Tab |
| 12 | `src/views/opshub/order/components/tabs/CommunicationTab.vue` | 沟通记录 Tab（骨架） |
| 13 | `src/views/opshub/order/components/ApplyPaymentModal.vue` | 申请付款弹窗 |
| 14 | `src/views/opshub/order/components/ApplyInvoiceModal.vue` | 申请开票弹窗 |
| 15 | `src/views/opshub/order/components/ApplyReturnModal.vue` | 申请退货弹窗 |

### 4.2 页面布局

```
┌──────────────────────────────────────────────────────────────┐
│ [订单总量] [已完成] [已付款] [未付款] [已开票] [未开票]              │
├──────────────────────────────────────────────────────────────┤
│ [□今日] [□本周] [□本月] [☑全部]  [季度▼] [月度▼] [产品线▼]      │
│ [经销商▼(管理)] [进度▼] [付款▼] [开票▼] [搜索订单号...] [重置]     │
├──────────────────────────────────────────────────────────────┤
│ [批量付款(经销商)] [批量开票] [批量退货] [咨询]                     │
├──────────────────────────────────────────────────────────────┤
│ □ | 订单号 | 经销商(管理) | 产品线 | 金额 | 订单时间 |            │
│   | 付款 | 开票 | 进度 | 操作 [...]                               │
├──────────────────────────────────────────────────────────────┤
│ 筛选结果：共 N 条订单 | 汇总金额：¥X    第 1/N 页  每页 50 条     │
└──────────────────────────────────────────────────────────────┘
```

### 4.3 关键交互

| 功能 | 实现 | 权限控制 |
|------|------|---------|
| 统计卡片 | `/opshub/order/statistics`，6 张 `el-card` 一行排列，可点击筛选 | `dealer:order:query` |
| 快捷时间 | `el-checkbox-group` 今日/本周/本月/全部（互斥单选行为），联动列表 | — |
| 经销商筛选 | `el-select` multiple，仅管理员/执行员可见 | 前端角色判断 |
| 模糊搜索 | `el-input` keyword 参数，跨订单号/产品线/经销商匹配 | — |
| 行点击 | 点击订单行打开 OrderDetailModal（6 Tab） | — |
| "申请付款" | 经销商视角，未付款时显示，弹出确认提示 | `dealer:order:pay` |
| "申请开票" | 经销商视角，未完全开票时显示，弹出开票表单（抬头/税号/备注） | `dealer:order:invoice` |
| "申请退货" | 经销商视角，已签收/已完成时显示，弹出退货商品选择 | `dealer:order:return` |
| 批量操作 | 勾选后操作，未勾选提示"请先选择" | 对应按钮权限 |
| 操作列 `...` | `el-dropdown` 下拉菜单，按角色和状态条件显示操作项 | — |
| 汇总信息 | 底部分页区域显示"共 N 条订单，汇总金额 ¥X" | — |
| 分页 | 默认 50 条 | — |

### 4.4 订单详情弹窗 6 Tab 设计

#### Tab 1：基本信息

| 区域 | 内容 |
|------|------|
| 基本信息区 | 订单号、进度状态、经销商、产品线 |
| 金额信息区 | 订单金额、付款状态、已付款、未付款 |
| 时间线 | 5 节点水平时间线（`el-steps`）：下单→已确认→已发货→已签收→已完成，带时间和完成状态 |
| 产品明细表 | `el-table`：产品名称/规格型号/单价/数量/单位/金额，底部合计 |

#### Tab 2：付款信息

| 区域 | 内容 |
|------|------|
| 付款概要 | 订单金额 / 已付 / 未付 / 状态 |
| 付款记录列表 | 付款日期 / 方式 / 凭证号 / 金额 / 状态 |
| 申请付款按钮 | 经销商视角，未付款时显示 |

#### Tab 3：开票信息

| 区域 | 内容 |
|------|------|
| 开票概要 | 状态 / 可开票金额 / 已开票金额 |
| 已开票记录 | 发票号 / 开票日期 / 发票类型 / 金额 |
| 申请开票按钮 | 经销商视角，未完全开票时显示 |

#### Tab 4：退货信息

| 区域 | 内容 |
|------|------|
| 退货概要 | 当前退货状态 / 可退货商品数 |
| 可退货商品明细 | 产品名称/规格/数量/金额/操作（经销商可申请退货） |

#### Tab 5：物流轨迹

| 区域 | 内容 |
|------|------|
| 物流节点列表 | 时间 + 描述，已完成节点高亮（`el-timeline`） |

#### Tab 6：沟通记录

| 区域 | 内容 |
|------|------|
| 沟通记录列表 | 内容/来源/渠道/时间（Step 4 仅展示骨架，依赖后续客户服务模块） |
| 发起新沟通 | 按钮跳转到订单咨询（预留） |

### 4.5 统计卡片详细设计

| # | 卡片 | 指标 | 点击行为 |
|---|------|------|---------|
| 1 | 订单总量 | totalCount | 激活全部筛选 |
| 2 | 已完成 | completedCount | 筛选 progress_status=completed |
| 3 | 已付款 | paidCount | 筛选 pay_status=paid |
| 4 | 未付款 | unpaidCount | 筛选 pay_status=unpaid |
| 5 | 已开票 | invoicedCount | 筛选 inv_status=invoiced |
| 6 | 未开票 | uninvoicedCount | 筛选 inv_status=uninvoiced |

### 4.6 操作列条件显示

操作列使用 `el-dropdown` 下拉菜单，按角色和状态条件渲染：

| 操作按钮 | 显示条件 | 点击行为 |
|---------|---------|---------|
| 申请付款 | 经销商 + pay_status=unpaid | 弹出 ApplyPaymentModal |
| 申请开票 | 经销商 + inv_status≠invoiced | 弹出 ApplyInvoiceModal |
| 申请退货 | 经销商 + (已签收 OR 已完成) | 弹出 ApplyReturnModal |
| 咨询 | 始终显示 | 打开 AI 订单咨询（预留） |

### 4.7 申请开票弹窗

| 字段 | 组件 | 必填 | 说明 |
|------|------|:---:|------|
| 发票抬头 | `el-input` | Y | — |
| 纳税人识别号 | `el-input` | Y | — |
| 特殊开票需求 | `el-input` textarea | N | — |

### 4.8 菜单更新

更新菜单 id=6006（订单模块）的 `component` 为 `opshub/order/index`。

---

## 五、角色权限矩阵

### 5.1 按钮级权限

| 权限标识 | 说明 | 品牌管理员 | 品牌销售员 | 服务单执行员 | 经销商 |
|---------|------|:---------:|:---------:|:----------:|:-----:|
| `dealer:order:query` | 查看订单 + 统计 | ✅ | ✅ | ✅ | ✅ |
| `dealer:order:update` | 更新进度 | ✅ | — | ✅ | — |
| `dealer:order:pay` | 申请付款 | — | — | — | ✅ |
| `dealer:order:invoice` | 申请开票 | — | — | — | ✅ |
| `dealer:order:return` | 申请退货 | — | — | — | ✅ |
| `dealer:order:consult` | 发起咨询 | ✅ | — | ✅ | ✅ |

### 5.2 UI 联动规则

| UI 元素 | 品牌管理员 | 品牌销售员 | 服务单执行员 | 经销商 |
|---------|-----------|-----------|-------------|-------|
| 经销商筛选器 | 可见 | 可见 | 可见 | **隐藏** |
| 表格经销商列 | 显示 | 显示 | 显示 | **隐藏** |
| "申请付款"操作 | 隐藏 | 隐藏 | 隐藏 | 显示（仅 unpaid） |
| "申请开票"操作 | 隐藏 | 隐藏 | 隐藏 | 显示（仅 uninvoiced/partial） |
| "申请退货"操作 | 隐藏 | 隐藏 | 隐藏 | 显示（仅 signed/completed） |
| "批量付款"按钮 | 隐藏 | 隐藏 | 隐藏 | 显示 |
| "批量开票"按钮 | 隐藏 | 隐藏 | 隐藏 | 显示 |
| "批量退货"按钮 | 隐藏 | 隐藏 | 隐藏 | 显示 |
| 操作列 | 咨询 | 只读+咨询 | 咨询 | 付款/开票/退货+咨询 |

---

## 六、SQL 脚本

### 6.1 DDL

PostgreSQL 语法：6 张 CREATE TABLE + ALTER TABLE (PK) + UNIQUE INDEX + 多个 INDEX + SEQUENCE + COMMENT。

表清单：
1. `ops_order_info` — 订单主表
2. `ops_order_product` — 订单产品明细
3. `ops_order_timeline` — 订单时间线
4. `ops_order_payment` — 付款记录
5. `ops_order_invoice` — 开票记录
6. `ops_order_logistics` — 物流轨迹

### 6.2 DML

- 更新菜单 id=6006 的 `component` 为 `opshub/order/index`
- 新增按钮权限 DML（`dealer:order:update`，用于 update-progress）
- 测试数据约 20-30 条，覆盖：
  - 4 条产品线 × 多个经销商
  - 5 种进度状态混合（pending/confirmed/shipped/signed/completed）
  - 付款状态混合（unpaid/paid）
  - 开票状态混合（uninvoiced/partial/invoiced）
  - 订单产品明细 2-5 条/订单
  - 时间线 5 节点/订单
  - 部分订单含付款记录和开票记录
  - 部分订单含物流轨迹

### 6.3 归档路径

`db/branches/feature_step4-订单模块/`
- `feature_step4-订单模块_ddl.sql`
- `feature_step4-订单模块_dml.sql`

---

## 七、与现有模块的交互

| 关联模块 | 关系 | 说明 |
|---------|------|------|
| Step 1 经销商/产品线 | `dealer_code` / `product_line_code` | 数据权限过滤依赖扩展表；创建订单时校验经销商和产品线存在性 |
| Step 3 签约进度 | 合同签署完成后创建订单 | Step 3 完成签署追踪，订单在 Step 4 管理（业务关联但无直接数据依赖） |
| Step 2 基础数据 | 无直接依赖 | 订单产品与基础数据文件无直接关联 |
| 后续 Step（售后模块） | 订单 → 售后 | 售后单关联订单号（`ops_order_info.order_code`），在后续 Step 实现 |
| 后续 Step（客户服务） | 订单 → 咨询/工单 | 沟通记录 Tab 依赖客户服务模块数据 |

---

## 八、验证方式

| 验证项 | 验证方法 |
|--------|---------|
| 表创建 | DDL 执行后 `SELECT * FROM ops_order_info` 确认 6 张表均存在 |
| 菜单更新 | 前端登录确认"订单模块"菜单指向 `opshub/order/index` |
| 分页查询 | 4 种角色分别调用 `/opshub/order/page`，验证数据和分页 |
| 数据权限 | dealer 仅看到授权经销商订单；执行员仅看到授权产品线订单 |
| 统计卡片 | `/opshub/order/statistics` 验证 6 张卡片数据正确 |
| 筛选功能 | 验证所有筛选组合（快捷时间、季度、月度、产品线、进度、付款、开票、关键词） |
| 订单详情 | `/opshub/order/get` 验证 6 Tab 数据完整返回 |
| 申请付款 | 经销商申请付款，验证 ops_order_payment 记录创建 |
| 申请开票 | 经销商填写抬头/税号申请开票，验证 ops_order_invoice 记录创建 |
| 申请退货 | 经销商按产品明细退货，验证 returnable_qty 扣减正确 |
| 批量操作 | 批量付款/开票/退货正确执行 |
| 进度更新 | 管理员更新进度状态，验证时间线节点更新 |
| 编译验证 | `mvn clean compile -pl yudao-module-opshub` 通过 |
| 前端验证 | `pnpm dev` 页面正常渲染（卡片+表格+详情弹窗+申请弹窗） |

---

## 九、后续阶段预留

| 功能 | 说明 | 预留阶段 |
|------|------|---------|
| 售后模块集成 | 售后单关联订单号，退货/退换货流程 | 后续 Step |
| 管理员创建/编辑订单 | 订单 CRUD + 编码自动生成 + OrderFormModal | 后续 Step |
| 客户服务集成 | 沟通记录 Tab 对接客户服务模块 | 后续 Step |
| AI 智能客服 | 订单咨询 AI 自动回复 | 后续 Step |
| 操作请求工作流 | 付款/开票/退货 → 操作请求 → 执行员处理 → 验收 | 后续 Step |
| 订单导入/导出 | Excel 批量处理 | 后续 Step |
| 付款审批自动化 | 对接审批流程（BPM） | 后续 Step |

---

## 附录：与总体 PRD 的关系

```
┌──────────────────────────────────────────────────────────┐
│               经销商管理客服SaaS 分阶段实施                 │
├──────────┬───────────┬───────────┬───────────┬───────────┤
│  Step 1  │  Step 2   │  Step 3   │  Step 4   │           │
│  (done)  │  (done)   │  (done)   │  ★ 当前   │           │
├──────────┼───────────┼───────────┼───────────┼───────────┤
│ 角色定义  │ 基础数据   │ 签约进度   │ 订单模块   │           │
│ 菜单权限  │ 文件管理   │ 合同录入   │ 统计卡片   │           │
│ 经销商管理│ 数据权限   │ 统计卡片   │ 6Tab详情   │           │
│ 产品线管理│ AI 解读骨架│ 趋势图表   │ 付款/开票  │           │
│ 授权扩展表│           │ 签署操作   │ 退货管理   │           │
│ 数据权限  │           │ 数据权限   │ 数据权限   │           │
├──────────┴───────────┴───────────┴───────────┴───────────┤
│  后续 Step：政策看板 / 售后模块 / 客户服务 / AI 客服        │
└──────────────────────────────────────────────────────────┘
```
