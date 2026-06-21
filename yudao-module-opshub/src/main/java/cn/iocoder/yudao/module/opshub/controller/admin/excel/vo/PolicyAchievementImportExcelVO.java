package cn.iocoder.yudao.module.opshub.controller.admin.excel.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.ExcelColumnSelect;
import cn.iocoder.yudao.module.opshub.framework.excel.convert.AchievementLevelConvert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PolicyAchievementImportExcelVO {
    @ExcelProperty("指标政策编码") private String policyCode;
    @ExcelProperty("指标名称") private String indicatorName;
    @ExcelProperty("年度") private Integer targetYear;
    @ExcelProperty("月份") private Integer targetMonth;
    @ExcelProperty(value = "层级", converter = AchievementLevelConvert.class)
    @ExcelColumnSelect(functionName = "achievement_level")
    private String achieveLevel;
    @ExcelProperty("省份") private String province;
    @ExcelProperty("省份编码") private String provinceCode;
    @ExcelProperty("医院") private String hospital;
    @ExcelProperty("医院编码") private String hospitalCode;
    @ExcelProperty("产品名称") private String productName;
    @ExcelProperty("达成值") private BigDecimal achievedValue;
}
