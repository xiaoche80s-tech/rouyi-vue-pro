package cn.iocoder.yudao.module.opshub.controller.admin.excel.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.ExcelColumnSelect;
import cn.iocoder.yudao.module.opshub.framework.excel.convert.PolicyAchievementTypeConvert;
import cn.iocoder.yudao.module.opshub.framework.excel.convert.PolicyStatusConvert;
import cn.iocoder.yudao.module.opshub.framework.excel.convert.PolicyTypeConvert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PolicyImportExcelVO {
    @ExcelProperty("经销商编码") private String dealerCode;
    @ExcelProperty("产品线编码") private String productLineCode;
    @ExcelProperty("产品线名称") private String productLineName;
    @ExcelProperty("政策编码") private String policyCode;
    @ExcelProperty("政策名称") private String policyName;
    @ExcelProperty(value = "政策类型", converter = PolicyTypeConvert.class)
    @ExcelColumnSelect(functionName = "policy_type")
    private String policyType;
    @ExcelProperty(value = "达成类型", converter = PolicyAchievementTypeConvert.class)
    @ExcelColumnSelect(functionName = "policy_achievement_type")
    private String achievementType;
    @ExcelProperty(value = "政策状态", converter = PolicyStatusConvert.class)
    @ExcelColumnSelect(functionName = "policy_status")
    private String policyStatus;
    @ExcelProperty("来源合同编码") private String contractCode;
    @ExcelProperty("来源合同名称") private String contractName;
    @ExcelProperty("政策描述") private String policyDesc;
}
