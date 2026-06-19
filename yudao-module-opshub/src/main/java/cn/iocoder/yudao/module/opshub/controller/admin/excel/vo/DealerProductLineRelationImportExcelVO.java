package cn.iocoder.yudao.module.opshub.controller.admin.excel.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DealerProductLineRelationImportExcelVO {
    @ExcelProperty("经销商编码") private String dealerCode;
    @ExcelProperty("产品线编码") private String productLineCode;
}
