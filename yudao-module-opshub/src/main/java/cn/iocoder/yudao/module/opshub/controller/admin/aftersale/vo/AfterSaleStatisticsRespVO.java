package cn.iocoder.yudao.module.opshub.controller.admin.aftersale.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 售后统计卡片 Response VO")
@Data
public class AfterSaleStatisticsRespVO {

    @Schema(description = "售后订单总数")
    private Integer totalCount;

    @Schema(description = "退款（handling_method = return_refund）")
    private Integer refundCount;

    @Schema(description = "退货（全部 handling_method）")
    private Integer returnCount;

    @Schema(description = "已完成")
    private Integer completedCount;

    @Schema(description = "已完成占比（%）")
    private BigDecimal completedRate;

    @Schema(description = "未完成（pending + in_progress + exchanging）")
    private Integer inProgressCount;

}
