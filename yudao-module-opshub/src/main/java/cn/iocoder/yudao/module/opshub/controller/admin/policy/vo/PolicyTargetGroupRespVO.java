package cn.iocoder.yudao.module.opshub.controller.admin.policy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "管理后台 - 目标分布 Response VO")
@Data
public class PolicyTargetGroupRespVO {

    @Schema(description = "目标值")
    private BigDecimal targetValue;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "该目标下政策数")
    private Integer policyCount;

    @Schema(description = "政策简要列表")
    private List<PolicyBriefVO> policies;

    @Schema(description = "政策简要信息")
    @Data
    public static class PolicyBriefVO {

        @Schema(description = "政策ID")
        private Long id;

        @Schema(description = "政策编码")
        private String policyCode;

        @Schema(description = "政策名称")
        private String policyName;

        @Schema(description = "经销商名称")
        private String dealerName;

        @Schema(description = "产品线名称")
        private String productLineName;

        @Schema(description = "政策类型")
        private String policyType;

        @Schema(description = "达成类型")
        private String achievementType;

        @Schema(description = "政策状态")
        private String policyStatus;

        @Schema(description = "政策开始日期")
        private LocalDate startDate;

        @Schema(description = "政策结束日期")
        private LocalDate endDate;

        @Schema(description = "目标值")
        private BigDecimal targetValue;

        @Schema(description = "达成值")
        private BigDecimal achievedValue;

        @Schema(description = "单位")
        private String unit;
    }
}
