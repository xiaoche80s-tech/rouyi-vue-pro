package cn.iocoder.yudao.module.opshub.controller.admin.excel.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderProductImportExcelVO {
    @ExcelProperty("订单号") private String orderCode;
    @ExcelProperty("产品编码") private String productCode;
    @ExcelProperty("产品名称") private String productName;
    @ExcelProperty("规格型号") private String specModel;
    @ExcelProperty("单价") private BigDecimal unitPrice;
    @ExcelProperty("数量") private Integer quantity;
    @ExcelProperty("单位") private String unit;
}
