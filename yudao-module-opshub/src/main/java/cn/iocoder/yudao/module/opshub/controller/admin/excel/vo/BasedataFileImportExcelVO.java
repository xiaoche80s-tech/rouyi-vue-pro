package cn.iocoder.yudao.module.opshub.controller.admin.excel.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.ExcelColumnSelect;
import cn.iocoder.yudao.module.opshub.framework.excel.convert.BasedataCategoryConvert;
import cn.iocoder.yudao.module.opshub.framework.excel.convert.CommonStatusConvert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BasedataFileImportExcelVO {
    @ExcelProperty("经销商编码") private String dealerCode;
    @ExcelProperty(value = "文件分类", converter = BasedataCategoryConvert.class)
    @ExcelColumnSelect(functionName = "basedata_category")
    private String category;
    @ExcelProperty("文件名称") private String fileName;
    @ExcelProperty("文件子类型") private String fileType;
    @ExcelProperty("文件地址") private String fileUrl;
    @ExcelProperty("有效期至") private LocalDate expireDate;
    @ExcelProperty(value = "状态", converter = CommonStatusConvert.class)
    @ExcelColumnSelect(functionName = "common_status")
    private String status;
    @ExcelProperty("文件描述") private String description;
    @ExcelProperty("备注") private String remark;
}
