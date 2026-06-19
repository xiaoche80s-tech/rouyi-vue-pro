package cn.iocoder.yudao.module.opshub.controller.admin.excel.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FieldDescriptionExcelVO {
    @ExcelProperty("字段名称")
    @ColumnWidth(20)
    private String fieldName;

    @ExcelProperty("字段类型")
    @ColumnWidth(12)
    private String fieldType;

    @ExcelProperty("可选值")
    @ColumnWidth(50)
    private String options;
}
