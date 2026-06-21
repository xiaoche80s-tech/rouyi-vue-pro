package cn.iocoder.yudao.module.opshub.controller.admin.excel.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.ExcelColumnSelect;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PolicyIndicatorImportExcelVO {
    @ExcelProperty("政策编码") private String policyCode;
    @ExcelProperty("指标名称") private String indicatorName;
    @ExcelProperty("年度") private Integer targetYear;
    @ExcelProperty("月份") private Integer targetMonth;
    @ExcelProperty("目标值") private BigDecimal targetValue;
    @ExcelProperty("达成值") private BigDecimal achievedValue;
    @ExcelProperty(value = "单位")
    @ExcelColumnSelect(functionName = "indicator_unit")
    private String unit;
}
