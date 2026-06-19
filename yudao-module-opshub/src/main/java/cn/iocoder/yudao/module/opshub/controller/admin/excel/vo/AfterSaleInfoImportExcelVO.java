package cn.iocoder.yudao.module.opshub.controller.admin.excel.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.module.opshub.framework.excel.convert.AfterSaleHandlingMethodConvert;
import cn.iocoder.yudao.module.opshub.framework.excel.convert.AfterSaleProgressStatusConvert;
import cn.iocoder.yudao.module.opshub.framework.excel.convert.AfterSaleReasonConvert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AfterSaleInfoImportExcelVO {
    @ExcelProperty("售后单号") private String aftersaleCode;
    @ExcelProperty("经销商编码") private String dealerCode;
    @ExcelProperty("产品线编码") private String productLineCode;
    @ExcelProperty("关联订单号") private String orderCode;
    @ExcelProperty(value = "处理方式", converter = AfterSaleHandlingMethodConvert.class) private String handlingMethod;
    @ExcelProperty(value = "售后原因", converter = AfterSaleReasonConvert.class) private String reason;
    @ExcelProperty(value = "进度状态", converter = AfterSaleProgressStatusConvert.class) private String progressStatus;
    @ExcelProperty("产品名称") private String productName;
    @ExcelProperty("产品规格型号") private String productSpec;
    @ExcelProperty("售后数量") private Integer quantity;
    @ExcelProperty("退款金额") private BigDecimal refundAmount;
    @ExcelProperty("退回物流公司") private String logisticsCompany;
    @ExcelProperty("退回物流单号") private String logisticsNo;
    @ExcelProperty("换货物流公司") private String exchangeLogisticsCompany;
    @ExcelProperty("换货物流单号") private String exchangeLogisticsNo;
    @ExcelProperty("备注") private String remark;
}
