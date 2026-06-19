package cn.iocoder.yudao.module.opshub.controller.admin.excel.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.module.opshub.framework.excel.convert.CommonStatusConvert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DealerProductLineImportExcelVO {
    @ExcelProperty("产品线名称") private String productLineName;
    @ExcelProperty("产品线编码") private String productLineCode;
    @ExcelProperty("排序") private Integer sort;
    @ExcelProperty(value = "状态", converter = CommonStatusConvert.class) private String status;
    @ExcelProperty("备注") private String remark;
}
