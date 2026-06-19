package cn.iocoder.yudao.module.opshub.controller.admin.excel.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.module.opshub.framework.excel.convert.OrderInvoiceStatusConvert;
import cn.iocoder.yudao.module.opshub.framework.excel.convert.OrderPayStatusConvert;
import cn.iocoder.yudao.module.opshub.framework.excel.convert.OrderProgressStatusConvert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderInfoImportExcelVO {
    @ExcelProperty("订单号") private String orderCode;
    @ExcelProperty("经销商编码") private String dealerCode;
    @ExcelProperty("产品线编码") private String productLineCode;
    @ExcelProperty("订单总金额") private BigDecimal totalAmount;
    @ExcelProperty("订单日期") private LocalDate orderDate;
    @ExcelProperty(value = "进度状态", converter = OrderProgressStatusConvert.class) private String progressStatus;
    @ExcelProperty(value = "付款状态", converter = OrderPayStatusConvert.class) private String payStatus;
    @ExcelProperty(value = "开票状态", converter = OrderInvoiceStatusConvert.class) private String invStatus;
    @ExcelProperty("备注") private String remark;
}
