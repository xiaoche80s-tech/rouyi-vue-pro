package cn.iocoder.yudao.module.opshub.controller.admin.policy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - 指标柱状图 Response VO")
@Data
public class PolicyIndicatorChartRespVO {

    @Schema(description = "指标名称")
    private String indicatorName;

    @Schema(description = "时间标签列表")
    private List<String> timeLabels;

    @Schema(description = "柱子数据列表")
    private List<BarVO> bars;

    @Schema(description = "柱子数据")
    @Data
    public static class BarVO {

        @Schema(description = "达成值")
        private BigDecimal achieved;

        @Schema(description = "目标值")
        private BigDecimal target;

        @Schema(description = "达成率%")
        private BigDecimal rate;
    }
}
