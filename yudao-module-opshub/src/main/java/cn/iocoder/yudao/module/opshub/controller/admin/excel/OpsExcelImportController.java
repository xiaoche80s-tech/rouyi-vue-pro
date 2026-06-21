package cn.iocoder.yudao.module.opshub.controller.admin.excel;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.excel.core.annotations.ExcelColumnSelect;
import cn.iocoder.yudao.framework.excel.core.function.ExcelColumnSelectFunction;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.opshub.controller.admin.excel.vo.*;
import cn.iocoder.yudao.module.opshub.service.excel.OpsExcelImportService;
import cn.idev.excel.ExcelWriter;
import cn.idev.excel.FastExcelFactory;
import cn.idev.excel.write.metadata.WriteSheet;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.excel.core.handler.SelectSheetWriteHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import cn.idev.excel.annotation.ExcelProperty;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - OpsHub Excel 批量导入")
@RestController
@RequestMapping("/opshub/excel-import")
@Validated
public class OpsExcelImportController {

    @Resource
    private OpsExcelImportService opsExcelImportService;

    /** functionName -> ExcelColumnSelectFunction，用于构建字段说明 Sheet */
    @Resource
    private List<ExcelColumnSelectFunction> selectFunctions;

    private Map<String, List<String>> getFunctionMap() {
        Map<String, List<String>> map = new HashMap<>();
        if (selectFunctions != null) {
            selectFunctions.forEach(f -> map.put(f.getName(), f.getOptions()));
        }
        return map;
    }

    // ==================== 模板下载 ====================

    @GetMapping("/template/{type}")
    @Operation(summary = "下载导入模板（含示例数据 + 字段说明）")
    @Parameter(name = "type", description = "导入类型", required = true,
            example = "dealer-info / product-line / relation / contract / order / order-product / " +
                    "order-payment / order-invoice / order-logistics / order-timeline / " +
                    "aftersale / aftersale-progress / basedata-file")
    @PreAuthorize("@ss.hasPermission('opshub:excel-import:import')")
    public void downloadTemplate(@PathVariable("type") String type,
                                 HttpServletResponse response) throws IOException {
        switch (type) {
            case "dealer-info" ->
                    writeMultiSheetExcel(response, "经销商导入模板.xlsx",
                            DealerInfoImportExcelVO.class, sampleDealerInfo());
            case "product-line" ->
                    writeMultiSheetExcel(response, "产品线导入模板.xlsx",
                            DealerProductLineImportExcelVO.class, sampleProductLine());
            case "relation" ->
                    writeMultiSheetExcel(response, "经销商产品线关联导入模板.xlsx",
                            DealerProductLineRelationImportExcelVO.class, sampleRelation());
            case "contract" ->
                    writeMultiSheetExcel(response, "签约合同导入模板.xlsx",
                            SigningContractImportExcelVO.class, sampleContract());
            case "order" ->
                    writeMultiSheetExcel(response, "订单导入模板.xlsx",
                            OrderInfoImportExcelVO.class, sampleOrder());
            case "order-product" ->
                    writeMultiSheetExcel(response, "订单产品导入模板.xlsx",
                            OrderProductImportExcelVO.class, sampleOrderProduct());
            case "order-payment" ->
                    writeMultiSheetExcel(response, "订单付款导入模板.xlsx",
                            OrderPaymentImportExcelVO.class, sampleOrderPayment());
            case "order-invoice" ->
                    writeMultiSheetExcel(response, "订单发票导入模板.xlsx",
                            OrderInvoiceImportExcelVO.class, sampleOrderInvoice());
            case "order-logistics" ->
                    writeMultiSheetExcel(response, "订单物流导入模板.xlsx",
                            OrderLogisticsImportExcelVO.class, sampleOrderLogistics());
            case "order-timeline" ->
                    writeMultiSheetExcel(response, "订单时间线导入模板.xlsx",
                            OrderTimelineImportExcelVO.class, sampleOrderTimeline());
            case "aftersale" ->
                    writeMultiSheetExcel(response, "售后单导入模板.xlsx",
                            AfterSaleInfoImportExcelVO.class, sampleAfterSale());
            case "aftersale-progress" ->
                    writeMultiSheetExcel(response, "售后进度导入模板.xlsx",
                            AfterSaleProgressImportExcelVO.class, sampleAfterSaleProgress());
            case "basedata-file" ->
                    writeMultiSheetExcel(response, "基础数据文件导入模板.xlsx",
                            BasedataFileImportExcelVO.class, sampleBasedataFile());
            case "policy" ->
                    writeMultiSheetExcel(response, "政策导入模板.xlsx",
                            PolicyImportExcelVO.class, samplePolicy());
            case "policy-indicator" ->
                    writeMultiSheetExcel(response, "政策指标导入模板.xlsx",
                            PolicyIndicatorImportExcelVO.class, samplePolicyIndicator());
            case "policy-achievement" ->
                    writeMultiSheetExcel(response, "政策达成明细导入模板.xlsx",
                            PolicyAchievementImportExcelVO.class, samplePolicyAchievement());
            default ->
                    writeMultiSheetExcel(response, "未知模板.xlsx",
                            DealerInfoImportExcelVO.class, Collections.emptyList());
        }
    }

    // ==================== 批量打包下载 ====================

    @GetMapping("/template/all")
    @Operation(summary = "批量打包下载全部导入模板（ZIP）")
    @PreAuthorize("@ss.hasPermission('opshub:excel-import:import')")
    public void downloadAllTemplates(HttpServletResponse response) throws IOException {
        response.setContentType("application/zip");
        response.addHeader("Content-Disposition",
                "attachment;filename=" + java.net.URLEncoder.encode("OpsHub导入模板.zip", "UTF-8"));

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            writeZipEntry(zos, "L0_经销商导入模板.xlsx", DealerInfoImportExcelVO.class, sampleDealerInfo());
            writeZipEntry(zos, "L0_产品线导入模板.xlsx", DealerProductLineImportExcelVO.class, sampleProductLine());
            writeZipEntry(zos, "L1_经销商产品线关联导入模板.xlsx", DealerProductLineRelationImportExcelVO.class, sampleRelation());
            writeZipEntry(zos, "L2_签约合同导入模板.xlsx", SigningContractImportExcelVO.class, sampleContract());
            writeZipEntry(zos, "L2_订单导入模板.xlsx", OrderInfoImportExcelVO.class, sampleOrder());
            writeZipEntry(zos, "L3_订单产品导入模板.xlsx", OrderProductImportExcelVO.class, sampleOrderProduct());
            writeZipEntry(zos, "L3_订单付款导入模板.xlsx", OrderPaymentImportExcelVO.class, sampleOrderPayment());
            writeZipEntry(zos, "L3_订单发票导入模板.xlsx", OrderInvoiceImportExcelVO.class, sampleOrderInvoice());
            writeZipEntry(zos, "L3_订单物流导入模板.xlsx", OrderLogisticsImportExcelVO.class, sampleOrderLogistics());
            writeZipEntry(zos, "L3_订单时间线导入模板.xlsx", OrderTimelineImportExcelVO.class, sampleOrderTimeline());
            writeZipEntry(zos, "L2_售后单导入模板.xlsx", AfterSaleInfoImportExcelVO.class, sampleAfterSale());
            writeZipEntry(zos, "L3_售后进度导入模板.xlsx", AfterSaleProgressImportExcelVO.class, sampleAfterSaleProgress());
            writeZipEntry(zos, "L4_基础数据文件导入模板.xlsx", BasedataFileImportExcelVO.class, sampleBasedataFile());
            writeZipEntry(zos, "L5_政策导入模板.xlsx", PolicyImportExcelVO.class, samplePolicy());
            writeZipEntry(zos, "L5_政策指标导入模板.xlsx", PolicyIndicatorImportExcelVO.class, samplePolicyIndicator());
            writeZipEntry(zos, "L5_政策达成明细导入模板.xlsx", PolicyAchievementImportExcelVO.class, samplePolicyAchievement());
        }
    }

    // ==================== 多 Sheet 写入工具方法 ====================

    /**
     * 多 Sheet 写入：Sheet1=数据（含下拉） + Sheet2=字段说明
     * 注意：响应头必须在写入数据之前设置，否则响应提交后无法再修改头信息
     */
    @SuppressWarnings("unchecked")
    private <T> void writeMultiSheetExcel(HttpServletResponse response, String fileName,
                                          Class<T> head, List<T> data) throws IOException {
        // 先设置 header 和 contentType（必须在写入之前）
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.addHeader("Content-Disposition", "attachment;filename=" + HttpUtils.encodeUtf8(fileName));
        writeMultiSheet(response.getOutputStream(), head, data);
    }

    private <T> void writeZipEntry(ZipOutputStream zos, String fileName,
                                   Class<T> head, List<T> data) throws IOException {
        zos.putNextEntry(new ZipEntry(fileName));
        writeMultiSheet(zos, head, data);
        zos.closeEntry();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> void writeMultiSheet(OutputStream out, Class<T> head, List<T> data) throws IOException {
        ExcelWriter writer = FastExcelFactory.write(out, head)
                .autoCloseStream(false)
                .registerWriteHandler(new SelectSheetWriteHandler(head))
                .build();
        boolean success = false;
        try {
            // Sheet 1: 数据
            WriteSheet dataSheet = cn.idev.excel.EasyExcel.writerSheet(0, "数据").build();
            writer.write(data, dataSheet);
            // Sheet 2: 字段说明
            WriteSheet descSheet = cn.idev.excel.EasyExcel.writerSheet(1, "字段说明")
                    .head(FieldDescriptionExcelVO.class).build();
            writer.write(buildFieldDescriptions(head), descSheet);
            success = true;
        } finally {
            if (success) {
                writer.finish();
            } else {
                // 异常路径：仅关闭 writer，跳过 dispose 以避免 "Stream closed" 警告
                try {
                    writer.close();
                } catch (Exception ignored) {
                    // 清理阶段的异常可以忽略
                }
            }
        }
    }

    /**
     * 通过反射解析 VO 类构建字段说明列表
     */
    private List<FieldDescriptionExcelVO> buildFieldDescriptions(Class<?> head) {
        Map<String, List<String>> functionMap = getFunctionMap();
        List<FieldDescriptionExcelVO> descriptions = new ArrayList<>();
        for (Field field : head.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) &&
                    java.lang.reflect.Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            ExcelProperty ep = field.getAnnotation(ExcelProperty.class);
            if (ep == null) continue;

            String fieldName = ep.value().length > 0 ? ep.value()[0] : field.getName();
            String fieldType = resolveFieldType(field.getType());
            String options = "";

            // 检查是否有 @ExcelColumnSelect
            ExcelColumnSelect ecs = field.getAnnotation(ExcelColumnSelect.class);
            if (ecs != null && !ecs.functionName().isEmpty()) {
                List<String> opts = functionMap.get(ecs.functionName());
                if (opts != null) {
                    fieldType = "枚举";
                    options = String.join("、", opts);
                }
            }
            descriptions.add(FieldDescriptionExcelVO.builder()
                    .fieldName(fieldName).fieldType(fieldType).options(options).build());
        }
        return descriptions;
    }

    private String resolveFieldType(Class<?> clazz) {
        if (clazz == String.class) return "文本";
        if (clazz == Integer.class || clazz == Long.class || clazz == int.class || clazz == long.class) return "整数";
        if (clazz == BigDecimal.class || clazz == Double.class || clazz == double.class) return "数字";
        if (clazz == LocalDate.class) return "日期";
        if (clazz == LocalDateTime.class) return "日期时间";
        return "文本";
    }

    // ==================== Excel 导入 ====================

    @PostMapping("/import/{type}")
    @Operation(summary = "导入 Excel")
    @Parameter(name = "type", description = "导入类型", required = true)
    @PreAuthorize("@ss.hasPermission('opshub:excel-import:import')")
    public CommonResult<ExcelImportRespVO> importExcel(@PathVariable("type") String type,
                                                       @RequestParam("file") MultipartFile file) throws Exception {
        return switch (type) {
            case "dealer-info" -> {
                List<DealerInfoImportExcelVO> list = ExcelUtils.read(file, DealerInfoImportExcelVO.class);
                yield success(opsExcelImportService.importDealerInfoList(list));
            }
            case "product-line" -> {
                List<DealerProductLineImportExcelVO> list = ExcelUtils.read(file, DealerProductLineImportExcelVO.class);
                yield success(opsExcelImportService.importProductLineList(list));
            }
            case "relation" -> {
                List<DealerProductLineRelationImportExcelVO> list = ExcelUtils.read(file, DealerProductLineRelationImportExcelVO.class);
                yield success(opsExcelImportService.importRelationList(list));
            }
            case "contract" -> {
                List<SigningContractImportExcelVO> list = ExcelUtils.read(file, SigningContractImportExcelVO.class);
                yield success(opsExcelImportService.importContractList(list));
            }
            case "order" -> {
                List<OrderInfoImportExcelVO> list = ExcelUtils.read(file, OrderInfoImportExcelVO.class);
                yield success(opsExcelImportService.importOrderList(list));
            }
            case "order-product" -> {
                List<OrderProductImportExcelVO> list = ExcelUtils.read(file, OrderProductImportExcelVO.class);
                yield success(opsExcelImportService.importOrderProductList(list));
            }
            case "order-payment" -> {
                List<OrderPaymentImportExcelVO> list = ExcelUtils.read(file, OrderPaymentImportExcelVO.class);
                yield success(opsExcelImportService.importOrderPaymentList(list));
            }
            case "order-invoice" -> {
                List<OrderInvoiceImportExcelVO> list = ExcelUtils.read(file, OrderInvoiceImportExcelVO.class);
                yield success(opsExcelImportService.importOrderInvoiceList(list));
            }
            case "order-logistics" -> {
                List<OrderLogisticsImportExcelVO> list = ExcelUtils.read(file, OrderLogisticsImportExcelVO.class);
                yield success(opsExcelImportService.importOrderLogisticsList(list));
            }
            case "order-timeline" -> {
                List<OrderTimelineImportExcelVO> list = ExcelUtils.read(file, OrderTimelineImportExcelVO.class);
                yield success(opsExcelImportService.importOrderTimelineList(list));
            }
            case "aftersale" -> {
                List<AfterSaleInfoImportExcelVO> list = ExcelUtils.read(file, AfterSaleInfoImportExcelVO.class);
                yield success(opsExcelImportService.importAfterSaleList(list));
            }
            case "aftersale-progress" -> {
                List<AfterSaleProgressImportExcelVO> list = ExcelUtils.read(file, AfterSaleProgressImportExcelVO.class);
                yield success(opsExcelImportService.importAfterSaleProgressList(list));
            }
            case "basedata-file" -> {
                List<BasedataFileImportExcelVO> list = ExcelUtils.read(file, BasedataFileImportExcelVO.class);
                yield success(opsExcelImportService.importBasedataFileList(list));
            }
            case "policy" -> {
                List<PolicyImportExcelVO> list = ExcelUtils.read(file, PolicyImportExcelVO.class);
                yield success(opsExcelImportService.importPolicyList(list));
            }
            case "policy-indicator" -> {
                List<PolicyIndicatorImportExcelVO> list = ExcelUtils.read(file, PolicyIndicatorImportExcelVO.class);
                yield success(opsExcelImportService.importPolicyIndicatorList(list));
            }
            case "policy-achievement" -> {
                List<PolicyAchievementImportExcelVO> list = ExcelUtils.read(file, PolicyAchievementImportExcelVO.class);
                yield success(opsExcelImportService.importPolicyAchievementList(list));
            }
            default -> success(ExcelImportRespVO.builder()
                    .failureCount(0).successCount(0).insertCount(0).updateCount(0).build());
        };
    }

    // ==================== 示例数据 ====================

    private List<DealerInfoImportExcelVO> sampleDealerInfo() {
        return List.of(
                DealerInfoImportExcelVO.builder()
                        .dealerName("示例经销商A").dealerCode("D-001")
                        .contactName("张三").contactPhone("13800138000")
                        .address("上海市浦东新区XX路100号").status("正常").remark("示例数据")
                        .build(),
                DealerInfoImportExcelVO.builder()
                        .dealerName("示例经销商B").dealerCode("D-002")
                        .contactName("李四").contactPhone("13900139000")
                        .address("北京市朝阳区YY路200号").status("正常").remark("")
                        .build()
        );
    }

    private List<DealerProductLineImportExcelVO> sampleProductLine() {
        return List.of(
                DealerProductLineImportExcelVO.builder()
                        .productLineName("示例产品线A").productLineCode("PL-001")
                        .sort(1).status("正常").remark("示例数据")
                        .build(),
                DealerProductLineImportExcelVO.builder()
                        .productLineName("示例产品线B").productLineCode("PL-002")
                        .sort(2).status("正常").remark("")
                        .build()
        );
    }

    private List<DealerProductLineRelationImportExcelVO> sampleRelation() {
        return List.of(
                DealerProductLineRelationImportExcelVO.builder()
                        .dealerCode("D-001").productLineCode("PL-001").build(),
                DealerProductLineRelationImportExcelVO.builder()
                        .dealerCode("D-001").productLineCode("PL-002").build()
        );
    }

    private List<SigningContractImportExcelVO> sampleContract() {
        return List.of(
                SigningContractImportExcelVO.builder()
                        .contractCode("CT-2024-001").dealerCode("D-001").productLineCode("PL-001")
                        .contractType("主合同").contractName("2024年度主合同")
                        .issuedDate(LocalDate.of(2024, 1, 1)).signDate(LocalDate.of(2024, 1, 15))
                        .summary("年度合作框架合同").remark("示例数据")
                        .build()
        );
    }

    private List<OrderInfoImportExcelVO> sampleOrder() {
        return List.of(
                OrderInfoImportExcelVO.builder()
                        .orderCode("ORD-2024-001").dealerCode("D-001").productLineCode("PL-001")
                        .totalAmount(new BigDecimal("50000.00")).orderDate(LocalDate.of(2024, 3, 15))
                        .progressStatus("待确认").payStatus("未付款").invStatus("未开票")
                        .remark("示例数据")
                        .build()
        );
    }

    private List<OrderProductImportExcelVO> sampleOrderProduct() {
        return List.of(
                OrderProductImportExcelVO.builder()
                        .orderCode("ORD-2024-001").productCode("SKU-001").productName("示例产品A")
                        .specModel("规格A").unitPrice(new BigDecimal("100.00")).quantity(10).unit("件")
                        .build(),
                OrderProductImportExcelVO.builder()
                        .orderCode("ORD-2024-001").productCode("SKU-002").productName("示例产品B")
                        .specModel("规格B").unitPrice(new BigDecimal("200.00")).quantity(5).unit("件")
                        .build()
        );
    }

    private List<OrderPaymentImportExcelVO> sampleOrderPayment() {
        return List.of(
                OrderPaymentImportExcelVO.builder()
                        .orderCode("ORD-2024-001").payAmount(new BigDecimal("50000.00"))
                        .payDate(LocalDate.of(2024, 3, 20)).payMethod("银行转账")
                        .voucherNo("PAY-2024-001").status("已通过").remark("示例数据")
                        .build()
        );
    }

    private List<OrderInvoiceImportExcelVO> sampleOrderInvoice() {
        return List.of(
                OrderInvoiceImportExcelVO.builder()
                        .orderCode("ORD-2024-001").invoiceAmount(new BigDecimal("50000.00"))
                        .invoiceNo("INV-2024-001").invoiceDate(LocalDate.of(2024, 3, 25))
                        .invoiceType("增值税专用发票").companyName("示例公司")
                        .taxNo("91310000XXXXXXXXXX").specialRequest("")
                        .status("已开票").remark("示例数据")
                        .build()
        );
    }

    private List<OrderLogisticsImportExcelVO> sampleOrderLogistics() {
        return List.of(
                OrderLogisticsImportExcelVO.builder()
                        .orderCode("ORD-2024-001").logisticsCompany("顺丰速运")
                        .trackingNo("SF1234567890").nodeDesc("已发货")
                        .nodeTime(LocalDateTime.of(2024, 3, 20, 10, 0, 0))
                        .isCompleted("否").sortOrder(1)
                        .build()
        );
    }

    private List<OrderTimelineImportExcelVO> sampleOrderTimeline() {
        return List.of(
                OrderTimelineImportExcelVO.builder()
                        .orderCode("ORD-2024-001").nodeCode("下单").nodeName("订单创建")
                        .nodeTime(LocalDateTime.of(2024, 3, 15, 9, 0, 0))
                        .isCompleted("是").sortOrder(1)
                        .build(),
                OrderTimelineImportExcelVO.builder()
                        .orderCode("ORD-2024-001").nodeCode("已确认").nodeName("订单确认")
                        .nodeTime(LocalDateTime.of(2024, 3, 16, 14, 0, 0))
                        .isCompleted("是").sortOrder(2)
                        .build()
        );
    }

    private List<AfterSaleInfoImportExcelVO> sampleAfterSale() {
        return List.of(
                AfterSaleInfoImportExcelVO.builder()
                        .aftersaleCode("AS-2024-001").dealerCode("D-001").productLineCode("PL-001")
                        .orderCode("ORD-2024-001").handlingMethod("退换货").reason("破损")
                        .progressStatus("待处理").productName("示例产品A").productSpec("规格A")
                        .quantity(2).refundAmount(null)
                        .logisticsCompany("顺丰速运").logisticsNo("SF9876543210")
                        .exchangeLogisticsCompany("").exchangeLogisticsNo("")
                        .remark("示例数据")
                        .build()
        );
    }

    private List<AfterSaleProgressImportExcelVO> sampleAfterSaleProgress() {
        return List.of(
                AfterSaleProgressImportExcelVO.builder()
                        .aftersaleCode("AS-2024-001").nodeCode("申请提交").nodeName("售后申请已提交")
                        .nodeTime(LocalDateTime.of(2024, 4, 1, 10, 0, 0))
                        .isCompleted("是").sortOrder(1).remark("示例数据")
                        .build()
        );
    }

    private List<BasedataFileImportExcelVO> sampleBasedataFile() {
        return List.of(
                BasedataFileImportExcelVO.builder()
                        .dealerCode("D-001").category("资质文件").fileName("营业执照")
                        .fileType("PDF").fileUrl("https://example.com/files/license.pdf")
                        .expireDate(LocalDate.of(2025, 12, 31)).status("正常")
                        .description("营业执照副本").remark("示例数据")
                        .build()
        );
    }

    private List<PolicyImportExcelVO> samplePolicy() {
        return List.of(
                PolicyImportExcelVO.builder()
                        .dealerCode("D-001").productLineCode("PL-001").productLineName("示例产品线A")
                        .policyCode("POL-2024-001").policyName("2024年Q1返利政策")
                        .policyType("返利").achievementType("季度政策").policyStatus("执行中")
                        .contractCode("CT-2024-001").contractName("2024年度主合同")
                        .policyDesc("示例数据")
                        .build(),
                PolicyImportExcelVO.builder()
                        .dealerCode("D-002").productLineCode("PL-002").productLineName("示例产品线B")
                        .policyCode("POL-2024-002").policyName("2024年月度促销政策")
                        .policyType("促销").achievementType("月度政策").policyStatus("待执行")
                        .contractCode("").contractName("")
                        .policyDesc("示例数据")
                        .build()
        );
    }

    private List<PolicyIndicatorImportExcelVO> samplePolicyIndicator() {
        return List.of(
                PolicyIndicatorImportExcelVO.builder()
                        .policyCode("POL-2024-001").indicatorName("销售额").targetYear(2024).targetMonth(3)
                        .targetValue(new BigDecimal("100000.00")).achievedValue(new BigDecimal("85000.00"))
                        .unit("元")
                        .build(),
                PolicyIndicatorImportExcelVO.builder()
                        .policyCode("POL-2024-001").indicatorName("销售量").targetYear(2024).targetMonth(3)
                        .targetValue(new BigDecimal("500.00")).achievedValue(new BigDecimal("420.00"))
                        .unit("件")
                        .build()
        );
    }

    private List<PolicyAchievementImportExcelVO> samplePolicyAchievement() {
        return List.of(
                PolicyAchievementImportExcelVO.builder()
                        .policyCode("POL-2024-001").indicatorName("销售额").targetYear(2024).targetMonth(3)
                        .achieveLevel("省级").province("上海市").provinceCode("310000")
                        .hospital("").hospitalCode("").productName("")
                        .achievedValue(new BigDecimal("50000.00"))
                        .build(),
                PolicyAchievementImportExcelVO.builder()
                        .policyCode("POL-2024-001").indicatorName("销售额").targetYear(2024).targetMonth(3)
                        .achieveLevel("医院级").province("上海市").provinceCode("310000")
                        .hospital("示例医院A").hospitalCode("H-001").productName("")
                        .achievedValue(new BigDecimal("30000.00"))
                        .build()
        );
    }
}
