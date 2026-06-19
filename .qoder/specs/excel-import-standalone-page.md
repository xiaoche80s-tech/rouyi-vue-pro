# OpsHub Excel 批量导入 - 独立页面方案

## Context

用户要求为 OpsHub 模块的 13 张业务表实现 Excel 批量导入功能，**作为独立页面**，不嵌入现有功能页面中。所有之前的代码变更均被拒绝，从零开始实施。

## 整体设计

### 后端：独立 Controller

新建 `OpsExcelImportController`（路径 `/opshub/excel-import/`），集中处理 13 张表的模板下载和数据导入，**不修改任何已有 Controller**。

为完成导入逻辑，需创建一个新的 `OpsExcelImportService` 作为编排层，内部调用已有的各模块 Service/Mapper 完成数据持久化。

### 前端：独立页面

新建 `views/opshub/excelImport/index.vue`，采用**卡片列表**布局，13 张表按依赖层级分组展示，每张表一张卡片，包含"下载模板"和"上传导入"两个操作按钮。

## 13 张表 & 依赖层级

```
L0 基础主数据: dealer_info, dealer_product_line
L1 关联数据:   dealer_product_line_relation
L2 业务主数据: signing_contract, order_info, aftersale_info
L3 业务子表:   order_product, order_payment, order_invoice, order_logistics, order_timeline, aftersale_progress
L4 补充:       basedata_file
```

## Task 1：后端基础设施（Converter + VO）

### 新建文件

**`framework/excel/convert/AbstractMapConvert.java`** — 转换器基类，封装中文↔编码双向 Map 映射，实现 FastExcel `Converter<String>` 接口。

**`framework/excel/convert/` 下 14 个 Converter：**

| Converter | 映射 |
|-----------|------|
| ContractTypeConvert | 主合同↔main, 政策合同↔policy, 补充协议↔supplement, 终止协议↔termination |
| OrderProgressStatusConvert | 待确认↔pending, 已确认↔confirmed, 已发货↔shipped, 已签收↔signed, 已完成↔completed |
| OrderPayStatusConvert | 未付款↔unpaid, 已付款↔paid |
| OrderInvoiceStatusConvert | 未开票↔uninvoiced, 部分开票↔partial, 已开票↔invoiced |
| OrderPaymentStatusConvert | 审批中↔pending, 已通过↔approved, 已拒绝↔rejected |
| OrderInvoiceRecordStatusConvert | 待开票↔pending, 已开票↔invoiced |
| AfterSaleHandlingMethodConvert | 退货↔return, 退换货↔exchange, 退货退款↔return_refund |
| AfterSaleReasonConvert | 投诉↔complaint, 召回↔recall, 破损↔damage |
| AfterSaleProgressStatusConvert | 待处理↔pending, 进行中↔in_progress, 换货中↔exchanging, 已完成↔completed |
| AfterSaleNodeCodeConvert | 申请提交↔submitted, 审核通过↔reviewed, 商品退回↔returned, 退款完成↔refund_done, 红字发票↔red_invoice, 新商品发出↔exchange_sent, 确认收货↔received |
| OrderTimelineNodeCodeConvert | 下单↔created, 已确认↔confirmed, 已发货↔shipped, 已签收↔signed, 已完成↔completed |
| BasedataCategoryConvert | 资质文件↔qualification, 授权文件↔authorization, 合同文件↔contract, 产品文件↔product |
| CommonStatusConvert | 正常↔0, 停用↔1 |
| BooleanConvert | 是↔true, 否↔false |

**`controller/admin/excel/vo/ExcelImportRespVO.java`** — 通用导入响应：successCount, insertCount, updateCount, failureCount, failureRows(Map<Integer,String>)

**13 个 ImportExcelVO（`controller/admin/excel/vo/` 目录下）：**
- `DealerInfoImportExcelVO.java`
- `DealerProductLineImportExcelVO.java`
- `DealerProductLineRelationImportExcelVO.java`
- `SigningContractImportExcelVO.java` — contractType 使用 @ExcelProperty(converter=ContractTypeConvert.class)
- `OrderInfoImportExcelVO.java` — progressStatus/payStatus/invStatus 使用对应 Converter
- `OrderProductImportExcelVO.java`
- `OrderPaymentImportExcelVO.java`
- `OrderInvoiceImportExcelVO.java`
- `OrderLogisticsImportExcelVO.java`
- `OrderTimelineImportExcelVO.java`
- `AfterSaleInfoImportExcelVO.java` — handlingMethod/reason 使用对应 Converter
- `AfterSaleProgressImportExcelVO.java` — nodeCode 使用 AfterSaleNodeCodeConvert
- `BasedataFileImportExcelVO.java` — category 使用 BasedataCategoryConvert

ImportExcelVO 中枚举字段统一使用 String 类型 + 自定义 Converter，Service 层手动做 Integer/Boolean 转换。

## Task 2：后端 Controller + Service

### 新建文件

**`controller/admin/excel/OpsExcelImportController.java`** — 独立 Controller

API 端点（每张表 2 个端点，共 26 个）：
- `GET /opshub/excel-import/template/{type}` — 按类型下载导入模板（**含 1-2 行示例数据**，帮助用户理解字段格式）（type = dealer-info / product-line / relation / contract / order / order-product / order-payment / order-invoice / order-logistics / order-timeline / aftersale / aftersale-progress / basedata-file）
- `POST /opshub/excel-import/import/{type}` — 按类型导入 Excel

权限：`@PreAuthorize("@ss.hasPermission('opshub:excel-import:import')")`

**`service/excel/OpsExcelImportService.java`** — 接口
**`service/excel/OpsExcelImportServiceImpl.java`** — 实现

核心逻辑：
1. 根据 type 分发到对应的 import 方法
2. 每个 import 方法内部：
   - 批量查询关联数据到内存 Map（避免 N+1）
   - 逐行 try-catch 处理：校验必填→FK ID 回填→判断新增/更新→持久化
   - 收集成功/失败统计返回 ExcelImportRespVO
3. 增量策略：
   - dealer_info: dealerCode 匹配→更新，不匹配→新增
   - product_line: productLineCode 匹配→更新，不匹配→新增
   - relation: dealerCode+productLineCode 组合键，已存在→跳过，不存在→新增
   - contract: contractCode 匹配→更新，不匹配→新增（contractCode 由 Excel 提供）
   - order: orderCode 匹配→更新，不匹配→新增
   - order_product: 按 orderCode 分组→**物理删除**后重新插入（`@Delete` 注解绕过逻辑删除）
   - order_payment: orderCode+voucherNo 匹配→更新
   - order_invoice: orderCode+invoiceNo 匹配→更新
   - order_logistics: orderCode+trackingNo+sortOrder 匹配→更新
   - order_timeline: orderCode+nodeCode 匹配→更新
   - aftersale: aftersaleCode 匹配→更新，不匹配→新增
   - aftersale_progress: aftersaleCode+nodeCode 匹配→更新
   - basedata_file: dealerCode+fileName+category 匹配→更新

### 需修改的已有文件（仅添加查询方法，不改已有接口）

| 文件 | 修改内容 |
|------|---------|
| `DealerInfoMapper.java` | 添加 selectByDealerCode |
| `DealerProductLineMapper.java` | 添加 selectByProductLineCode |
| `DealerProductLineRelationMapper.java` | 添加 selectByDealerCodeAndProductLineCode |
| `SigningContractMapper.java` | 添加 selectByContractCode |
| `OrderInfoMapper.java` | 添加 selectByOrderCode |
| `OrderProductMapper.java` | 添加 deletePhysicalByOrderCode（@Delete 物理删除） |
| `OrderPaymentMapper.java` | 添加 selectByOrderCodeAndVoucherNo |
| `OrderInvoiceMapper.java` | 添加 selectByOrderCodeAndInvoiceNo |
| `OrderLogisticsMapper.java` | 添加 selectByOrderCodeAndTrackingNoAndSortOrder |
| `OrderTimelineMapper.java` | 添加 selectByOrderCodeAndNodeCode |
| `AfterSaleInfoMapper.java` | 添加 selectByAftersaleCode |
| `AfterSaleProgressMapper.java` | 添加 selectByAftersaleCodeAndNodeCode |
| `BasedataFileMapper.java` | 添加 selectByDealerCodeAndFileNameAndCategory |
| `DealerInfoService.java` / `Impl` | 添加 getDealerList() 返回全部经销商（导入校验用） |

## Task 3：前端 API + 页面

### 新建文件

**`src/api/opshub/excelImport/index.ts`** — API 接口定义

```typescript
// 下载导入模板
export const getImportTemplate = (type: string) => {
  return request.download({ url: `/opshub/excel-import/template/${type}` })
}

// 导入 Excel
export const importExcel = (type: string, file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.upload({ url: `/opshub/excel-import/import/${type}`, data: formData })
}
```

**`src/views/opshub/excelImport/index.vue`** — 独立导入页面

页面布局：
- 顶部标题栏："OpsHub 数据批量导入"
- 导入说明（依赖层级提示：请按 L0→L1→L2→L3→L4 顺序导入）
- 按层级分组的 13 张卡片，每张卡片包含：
  - 表中文名 + 表英文名
  - 字段数量 + 必填字段提示
  - "下载模板"按钮 → 调用 GET template API
  - "上传导入"按钮 → el-upload 选择文件后调用 POST import API
  - 导入结果展示区（成功数/新增数/更新数/失败数 + 失败明细）

### 路由注册

项目使用动态菜单（后台配置），前端只需创建页面文件。用户需在后台菜单管理中添加菜单项，path 指向 `opshub/excelImport`。

## Task 4：编译验证

```bash
mvn clean compile -pl yudao-module-opshub -am -DskipTests
```

## 文件清单汇总

### 新建文件（~20 个）

**后端：**
- `framework/excel/convert/AbstractMapConvert.java`
- `framework/excel/convert/` × 14 个 Converter
- `controller/admin/excel/vo/ExcelImportRespVO.java`
- `controller/admin/excel/vo/` × 13 个 ImportExcelVO
- `controller/admin/excel/OpsExcelImportController.java`
- `service/excel/OpsExcelImportService.java`
- `service/excel/OpsExcelImportServiceImpl.java`

**前端：**
- `src/api/opshub/excelImport/index.ts`
- `src/views/opshub/excelImport/index.vue`

### 修改文件（~14 个，仅添加查询方法）

- 13 个 Mapper（添加 selectByXxx / deletePhysical 方法）
- `DealerInfoService.java` + `DealerInfoServiceImpl.java`（添加 getDealerList）
