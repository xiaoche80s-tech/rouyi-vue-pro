package cn.iocoder.yudao.module.opshub.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 订单统计卡片 Response VO")
@Data
public class OrderStatisticsRespVO {

    @Schema(description = "订单总量")
    private Integer totalCount;

    @Schema(description = "已完成")
    private Integer completedCount;

    @Schema(description = "已付款")
    private Integer paidCount;

    @Schema(description = "未付款")
    private Integer unpaidCount;

    @Schema(description = "已开票")
    private Integer invoicedCount;

    @Schema(description = "未开票")
    private Integer uninvoicedCount;

    @Schema(description = "汇总金额")
    private BigDecimal totalAmount;

}
