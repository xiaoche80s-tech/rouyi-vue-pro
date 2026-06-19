package cn.iocoder.yudao.module.opshub.controller.admin.excel.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.module.opshub.framework.excel.convert.ContractTypeConvert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SigningContractImportExcelVO {
    @ExcelProperty("合同编码") private String contractCode;
    @ExcelProperty("经销商编码") private String dealerCode;
    @ExcelProperty("产品线编码") private String productLineCode;
    @ExcelProperty(value = "合同类型", converter = ContractTypeConvert.class) private String contractType;
    @ExcelProperty("合同名称") private String contractName;
    @ExcelProperty("下发日期") private LocalDate issuedDate;
    @ExcelProperty("签署日期") private LocalDate signDate;
    @ExcelProperty("合同摘要") private String summary;
    @ExcelProperty("备注") private String remark;
}
