package cn.iocoder.yudao.module.opshub.controller.admin.excel.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.module.opshub.framework.excel.convert.OrderInvoiceRecordStatusConvert;
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
public class OrderInvoiceImportExcelVO {
    @ExcelProperty("订单号") private String orderCode;
    @ExcelProperty("开票金额") private BigDecimal invoiceAmount;
    @ExcelProperty("发票号") private String invoiceNo;
    @ExcelProperty("开票日期") private LocalDate invoiceDate;
    @ExcelProperty("发票类型") private String invoiceType;
    @ExcelProperty("发票抬头") private String companyName;
    @ExcelProperty("纳税人识别号") private String taxNo;
    @ExcelProperty("特殊开票需求") private String specialRequest;
    @ExcelProperty(value = "状态", converter = OrderInvoiceRecordStatusConvert.class) private String status;
    @ExcelProperty("备注") private String remark;
}
