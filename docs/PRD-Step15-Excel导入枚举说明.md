# PRD-Step15-Excel导入枚举说明

## 概述

为 Excel 批量导入功能的 13 张导入模板增加枚举字段说明，在**前端卡片**中通过折叠面板展示枚举取值说明，在**下载的 Excel 模板**中通过「字段说明」Sheet 页和列下拉选择（Data Validation）两种方式辅助用户正确填写枚举值。

## 变更背景

1. 当前 13 张导入模板中有 11 张包含枚举字段（共 14 种枚举 Converter），用户无法直接知道某个字段可以填哪些值
2. Excel 模板没有下拉选择，用户需手动输入中文标签，容易拼写错误
3. 前端卡片没有展示枚举字段的可填值说明

## 枚举字段全量清单

| 导入表 | 字段 | Converter | 可选中文值 |
|--------|------|-----------|------------|
| 经销商信息 | 状态 | CommonStatusConvert | 正常、停用 |
| 产品线 | 状态 | CommonStatusConvert | 正常、停用 |
| 经销商产品线关联 | （无枚举字段） | — | — |
| 签约合同 | 合同类型 | ContractTypeConvert | 主合同、政策合同、补充协议、终止协议 |
| 订单信息 | 进度状态 | OrderProgressStatusConvert | 待确认、已确认、已发货、已签收、已完成 |
| 订单信息 | 付款状态 | OrderPayStatusConvert | 未付款、已付款 |
| 订单信息 | 开票状态 | OrderInvoiceStatusConvert | 未开票、部分开票、已开票 |
| 订单产品 | （无枚举字段） | — | — |
| 订单付款 | 状态 | OrderPaymentStatusConvert | 审批中、已通过、已拒绝 |
| 订单发票 | 状态 | OrderInvoiceRecordStatusConvert | 待开票、已开票 |
| 订单物流 | 是否已完成 | BooleanConvert | 是、否 |
| 订单时间线 | 节点编码 | OrderTimelineNodeCodeConvert | 下单、已确认、已发货、已签收、已完成 |
| 订单时间线 | 是否完成 | BooleanConvert | 是、否 |
| 售后单 | 处理方式 | AfterSaleHandlingMethodConvert | 退货、退换货、退货退款 |
| 售后单 | 售后原因 | AfterSaleReasonConvert | 投诉、召回、破损 |
| 售后单 | 进度状态 | AfterSaleProgressStatusConvert | 待处理、进行中、换货中、已完成 |
| 售后进度 | 节点编码 | AfterSaleNodeCodeConvert | 申请提交、审核通过、商品退回、退款完成、红字发票、新商品发出、确认收货 |
| 售后进度 | 是否完成 | BooleanConvert | 是、否 |
| 基础数据文件 | 文件分类 | BasedataCategoryConvert | 资质文件、授权文件、合同文件、产品文件 |
| 基础数据文件 | 状态 | CommonStatusConvert | 正常、停用 |

## 变更内容

### 1. 后端：ImportExcelVO 枚举字段添加 @ExcelColumnSelect 注解（P0）

**改动文件**：
- `yudao-module-opshub/.../controller/admin/excel/vo/DealerInfoImportExcelVO.java` — 添加 @ExcelColumnSelect
- `yudao-module-opshub/.../controller/admin/excel/vo/DealerProductLineImportExcelVO.java` — 同上
- `yudao-module-opshub/.../controller/admin/excel/vo/SigningContractImportExcelVO.java` — 同上
- `yudao-module-opshub/.../controller/admin/excel/vo/OrderInfoImportExcelVO.java` — 同上
- `yudao-module-opshub/.../controller/admin/excel/vo/OrderPaymentImportExcelVO.java` — 同上
- `yudao-module-opshub/.../controller/admin/excel/vo/OrderInvoiceImportExcelVO.java` — 同上
- `yudao-module-opshub/.../controller/admin/excel/vo/OrderLogisticsImportExcelVO.java` — 同上
- `yudao-module-opshub/.../controller/admin/excel/vo/OrderTimelineImportExcelVO.java` — 同上
- `yudao-module-opshub/.../controller/admin/excel/vo/AfterSaleInfoImportExcelVO.java` — 同上
- `yudao-module-opshub/.../controller/admin/excel/vo/AfterSaleProgressImportExcelVO.java` — 同上
- `yudao-module-opshub/.../controller/admin/excel/vo/BasedataFileImportExcelVO.java` — 同上

**设计逻辑**：
- 在每个使用 converter 的 `@ExcelProperty` 字段上，追加 `@ExcelColumnSelect(functionName = "xxx")` 注解
- `functionName` 对应 `ExcelColumnSelectFunction.getName()` 返回的名称
- SelectSheetWriteHandler 会自动识别注解 → 创建隐藏字典 Sheet → 在数据 Sheet 对应列设置下拉验证

**示例**：
```java
// Before
@ExcelProperty(value = "合同类型", converter = ContractTypeConvert.class)
private String contractType;

// After
@ExcelProperty(value = "合同类型", converter = ContractTypeConvert.class)
@ExcelColumnSelect(functionName = "contract_type")
private String contractType;
```

### 2. 后端：注册 ExcelColumnSelectFunction Bean（P0）

**改动文件**：
- `yudao-module-opshub/.../framework/excel/function/OpsExcelSelectFunctions.java`（**新建**）

**设计逻辑**：
- 创建 14 个静态内部类（每种 Converter 对应一个），均实现 `ExcelColumnSelectFunction`
- 每个类的 `getName()` 返回约定名称（与 VO 的 @ExcelColumnSelect 对应）
- 每个类的 `getOptions()` 返回对应 Converter 的中文标签列表（从 LABEL_TO_CODE 的 keySet 获取）
- 使用 `@Component` 注册为 Spring Bean

**functionName 与标签映射**：

| functionName | getOptions() 返回值 |
|---|---|
| `common_status` | 正常、停用 |
| `contract_type` | 主合同、政策合同、补充协议、终止协议 |
| `order_progress_status` | 待确认、已确认、已发货、已签收、已完成 |
| `order_pay_status` | 未付款、已付款 |
| `order_invoice_status` | 未开票、部分开票、已开票 |
| `order_payment_status` | 审批中、已通过、已拒绝 |
| `order_invoice_record_status` | 待开票、已开票 |
| `order_timeline_node_code` | 下单、已确认、已发货、已签收、已完成 |
| `boolean_value` | 是、否 |
| `aftersale_handling_method` | 退货、退换货、退货退款 |
| `aftersale_reason` | 投诉、召回、破损 |
| `aftersale_progress_status` | 待处理、进行中、换货中、已完成 |
| `aftersale_node_code` | 申请提交、审核通过、商品退回、退款完成、红字发票、新商品发出、确认收货 |
| `basedata_category` | 资质文件、授权文件、合同文件、产品文件 |

### 3. 后端：Excel 模板增加「字段说明」Sheet 页（P0）

**改动文件**：
- `yudao-module-opshub/.../controller/admin/excel/OpsExcelImportController.java` — 修改模板下载逻辑

**设计逻辑**：
- 不再使用 `ExcelUtils.write()`，改为使用 `FastExcelFactory.write()` 手动写入多 Sheet
- **Sheet 1「数据」**：保持现有表头 + 示例数据 + SelectSheetWriteHandler 下拉
- **Sheet 2「字段说明」**：三列表格（字段名称、字段类型、可选值），列出所有字段信息
  - 非枚举字段：类型标注 "文本"/"数字"/"日期" 等，可选值留空
  - 枚举字段：类型标注 "枚举"，可选值列出所有中文标签，用 `、` 分隔
- 字段说明 Sheet 的列宽自适应，表头加粗 + 背景色

**伪代码**：
```java
ExcelWriter writer = FastExcelFactory.write(response.getOutputStream(), head)
    .registerWriteHandler(new SelectSheetWriteHandler(head))
    .build();
// Sheet 1: 数据
WriteSheet dataSheet = EasyExcel.writerSheet(0, "数据").build();
writer.write(sampleData, dataSheet);
// Sheet 2: 字段说明
WriteSheet descSheet = EasyExcel.writerSheet(1, "字段说明").head(DescriptionHead.class).build();
writer.write(buildFieldDescriptions(head), descSheet);
writer.finish();
```

### 4. 前端：卡片增加枚举说明折叠面板（P0）

**改动文件**：
- `yudao-ui/yudao-ui-admin-vue3/src/views/opshub/excelImport/index.vue` — ImportCard 组件 + 卡片配置

**设计逻辑**：
- 在 `ImportCardItem` 接口中新增 `enumFields?: Array<{ field: string; label: string; values: string[] }>` 可选属性
- 在卡片 `card-body` 区域底部，当 `enumFields` 非空时渲染一个 `<el-collapse>` 折叠面板
- 展开后以 Tag 列表展示每个枚举字段的可选值
- 默认折叠，不占用额外空间

**卡片配置变更示例**：
```ts
{
  type: 'contract', name: '签约合同', nameEn: 'signing_contract', fieldCount: 9,
  requiredFields: '合同编码、经销商编码', fileName: '签约合同导入模板.xlsx',
  enumFields: [
    { field: '合同类型', values: ['主合同', '政策合同', '补充协议', '终止协议'] }
  ]
}
```

**折叠面板 UI**：
```
┌─────────────────────────────────┐
│ 签约合同          signing_contract │
│ 字段  9 个                        │
│ 必填  合同编码、经销商编码          │
│                                 │
│ ▸ 枚举字段说明                    │  ← 点击展开
│   ┌────────────────────────────┐│
│   │ 合同类型                    ││
│   │ [主合同] [政策合同] [补充协议] ││
│   │ [终止协议]                  ││
│   └────────────────────────────┘│
│                                 │
│ [下载模板]        [上传导入]      │
└─────────────────────────────────┘
```

## 涉及文件清单

### 后端（yudao-module-opshub）
| 文件 | 操作 |
|------|------|
| `controller/admin/excel/vo/*ImportExcelVO.java`（11 个文件） | 修改：添加 @ExcelColumnSelect |
| `framework/excel/function/OpsExcelSelectFunctions.java` | 新增：14 个 ExcelColumnSelectFunction 实现 |
| `controller/admin/excel/OpsExcelImportController.java` | 修改：模板下载增加「字段说明」Sheet |

### 前端（yudao-ui/yudao-ui-admin-vue3）
| 文件 | 操作 |
|------|------|
| `views/opshub/excelImport/index.vue` | 修改：ImportCardItem 接口 + 卡片配置 + 折叠面板 UI |

## 已知限制

1. SelectSheetWriteHandler 的字典 Sheet 名为 "字典sheet"（框架固定），用户一般不需要看到
2. 「字段说明」Sheet 为静态说明，不包含动态联动逻辑
3. BooleanConvert（是/否）字段虽然也有 Converter，但通常用户容易理解，可选择不展示下拉（仍保留折叠面板说明）
