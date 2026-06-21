package cn.iocoder.yudao.module.opshub.controller.admin.policy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - KPI 面板 Response VO")
@Data
public class PolicyKpiPanelRespVO {

    @Schema(description = "指标名称")
    private String indicatorName;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "关联政策数")
    private Integer policyCount;

    @Schema(description = "时间维度数据列表")
    private List<PeriodVO> periods;

    @Schema(description = "目标分布")
    private List<PolicyTargetGroupRespVO> targetGroups;

    @Schema(description = "时间维度数据")
    @Data
    public static class PeriodVO {

        @Schema(description = "时间标签（Q1/Q2/...或1月/2月/...）")
        private String label;

        @Schema(description = "汇总达成值")
        private BigDecimal achieved;

        @Schema(description = "汇总目标值")
        private BigDecimal target;

        @Schema(description = "达成率%")
        private BigDecimal rate;
    }
}
