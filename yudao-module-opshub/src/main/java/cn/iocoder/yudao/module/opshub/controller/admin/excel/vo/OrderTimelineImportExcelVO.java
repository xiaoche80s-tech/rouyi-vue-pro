package cn.iocoder.yudao.module.opshub.controller.admin.excel.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.module.opshub.framework.excel.convert.BooleanConvert;
import cn.iocoder.yudao.module.opshub.framework.excel.convert.OrderTimelineNodeCodeConvert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderTimelineImportExcelVO {
    @ExcelProperty("订单号") private String orderCode;
    @ExcelProperty(value = "节点编码", converter = OrderTimelineNodeCodeConvert.class) private String nodeCode;
    @ExcelProperty("节点名称") private String nodeName;
    @ExcelProperty("节点完成时间") private LocalDateTime nodeTime;
    @ExcelProperty(value = "是否完成", converter = BooleanConvert.class) private String isCompleted;
    @ExcelProperty("排序序号") private Integer sortOrder;
}
