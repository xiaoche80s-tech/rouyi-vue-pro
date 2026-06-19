package cn.iocoder.yudao.module.opshub.controller.admin.excel;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.opshub.controller.admin.excel.vo.*;
import cn.iocoder.yudao.module.opshub.service.excel.OpsExcelImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - OpsHub Excel 批量导入")
@RestController
@RequestMapping("/opshub/excel-import")
@Validated
public class OpsExcelImportController {

    @Resource
    private OpsExcelImportService opsExcelImportService;

    // ==================== 模板下载 ====================

    @GetMapping("/template/{type}")
    @Operation(summary = "下载导入模板（含示例数据）")
    @Parameter(name = "type", description = "导入类型", required = true,
            example = "dealer-info / product-line / relation / contract / order / order-product / " +
                    "order-payment / order-invoice / order-logistics / order-timeline / " +
                    "aftersale / aftersale-progress / basedata-file")
    @PreAuthorize("@ss.hasPermission('opshub:excel-import:import')")
    public void downloadTemplate(@PathVariable("type") String type,
                                 HttpServletResponse response) throws IOException {
        switch (type) {
            case "dealer-info" ->
                    ExcelUtils.write(response, "经销商导入模板.xlsx", "数据",
                            DealerInfoImportExcelVO.class, sampleDealerInfo());
            case "product-line" ->
                    ExcelUtils.write(response, "产品线导入模板.xlsx", "数据",
                            DealerProductLineImportExcelVO.class, sampleProductLine());
            case "relation" ->
                    ExcelUtils.write(response, "经销商产品线关联导入模板.xlsx", "数据",
                            DealerProductLineRelationImportExcelVO.class, sampleRelation());
            case "contract" ->
                    ExcelUtils.write(response, "签约合同导入模板.xlsx", "数据",
                            SigningContractImportExcelVO.class, sampleContract());
            case "order" ->
                    ExcelUtils.write(response, "订单导入模板.xlsx", "数据",
                            OrderInfoImportExcelVO.class, sampleOrder());
            case "order-product" ->
                    ExcelUtils.write(response, "订单产品导入模板.xlsx", "数据",
                            OrderProductImportExcelVO.class, sampleOrderProduct());
            case "order-payment" ->
                    ExcelUtils.write(response, "订单付款导入模板.xlsx", "数据",
                            OrderPaymentImportExcelVO.class, sampleOrderPayment());
            case "order-invoice" ->
                    ExcelUtils.write(response, "订单发票导入模板.xlsx", "数据",
                            OrderInvoiceImportExcelVO.class, sampleOrderInvoice());
            case "order-logistics" ->
                    ExcelUtils.write(response, "订单物流导入模板.xlsx", "数据",
                            OrderLogisticsImportExcelVO.class, sampleOrderLogistics());
            case "order-timeline" ->
                    ExcelUtils.write(response, "订单时间线导入模板.xlsx", "数据",
                            OrderTimelineImportExcelVO.class, sampleOrderTimeline());
            case "aftersale" ->
                    ExcelUtils.write(response, "售后单导入模板.xlsx", "数据",
                            AfterSaleInfoImportExcelVO.class, sampleAfterSale());
            case "aftersale-progress" ->
                    ExcelUtils.write(response, "售后进度导入模板.xlsx", "数据",
                            AfterSaleProgressImportExcelVO.class, sampleAfterSaleProgress());
            case "basedata-file" ->
                    ExcelUtils.write(response, "基础数据文件导入模板.xlsx", "数据",
                            BasedataFileImportExcelVO.class, sampleBasedataFile());
            default ->
                    ExcelUtils.write(response, "未知模板.xlsx", "数据",
                            DealerInfoImportExcelVO.class, Collections.emptyList());
        }
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
}
