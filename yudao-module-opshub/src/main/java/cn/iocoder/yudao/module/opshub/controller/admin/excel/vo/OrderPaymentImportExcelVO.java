package cn.iocoder.yudao.module.opshub.controller.admin.excel.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.ExcelColumnSelect;
import cn.iocoder.yudao.module.opshub.framework.excel.convert.OrderPaymentStatusConvert;
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
public class OrderPaymentImportExcelVO {
    @ExcelProperty("订单号") private String orderCode;
    @ExcelProperty("付款金额") private BigDecimal payAmount;
    @ExcelProperty("付款日期") private LocalDate payDate;
    @ExcelProperty("付款方式") private String payMethod;
    @ExcelProperty("付款凭证号") private String voucherNo;
    @ExcelProperty(value = "状态", converter = OrderPaymentStatusConvert.class)
    @ExcelColumnSelect(functionName = "order_payment_status")
    private String status;
    @ExcelProperty("备注") private String remark;
}
