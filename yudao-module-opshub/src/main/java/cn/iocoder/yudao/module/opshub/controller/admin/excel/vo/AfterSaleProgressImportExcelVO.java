package cn.iocoder.yudao.module.opshub.controller.admin.excel.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.module.opshub.framework.excel.convert.AfterSaleNodeCodeConvert;
import cn.iocoder.yudao.module.opshub.framework.excel.convert.BooleanConvert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AfterSaleProgressImportExcelVO {
    @ExcelProperty("售后单号") private String aftersaleCode;
    @ExcelProperty(value = "节点编码", converter = AfterSaleNodeCodeConvert.class) private String nodeCode;
    @ExcelProperty("节点名称") private String nodeName;
    @ExcelProperty("节点完成时间") private LocalDateTime nodeTime;
    @ExcelProperty(value = "是否完成", converter = BooleanConvert.class) private String isCompleted;
    @ExcelProperty("排序序号") private Integer sortOrder;
    @ExcelProperty("节点备注") private String remark;
}
