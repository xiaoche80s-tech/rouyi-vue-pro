package cn.iocoder.yudao.module.opshub.controller.admin.excel.vo;

import cn.idev.excel.annotation.ExcelProperty;
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
public class OrderLogisticsImportExcelVO {
    @ExcelProperty("订单号") private String orderCode;
    @ExcelProperty("物流公司") private String logisticsCompany;
    @ExcelProperty("物流单号") private String trackingNo;
    @ExcelProperty("节点描述") private String nodeDesc;
    @ExcelProperty("节点时间") private LocalDateTime nodeTime;
    @ExcelProperty(value = "是否已完成", converter = BooleanConvert.class) private String isCompleted;
    @ExcelProperty("排序序号") private Integer sortOrder;
}
