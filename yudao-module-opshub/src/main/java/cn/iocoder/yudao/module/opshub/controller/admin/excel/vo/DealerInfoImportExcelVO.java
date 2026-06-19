package cn.iocoder.yudao.module.opshub.controller.admin.excel.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.ExcelColumnSelect;
import cn.iocoder.yudao.module.opshub.framework.excel.convert.CommonStatusConvert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DealerInfoImportExcelVO {
    @ExcelProperty("经销商名称") private String dealerName;
    @ExcelProperty("经销商编码") private String dealerCode;
    @ExcelProperty("联系人") private String contactName;
    @ExcelProperty("联系电话") private String contactPhone;
    @ExcelProperty("地址") private String address;
    @ExcelProperty(value = "状态", converter = CommonStatusConvert.class)
    @ExcelColumnSelect(functionName = "common_status")
    private String status;
    @ExcelProperty("备注") private String remark;
}
