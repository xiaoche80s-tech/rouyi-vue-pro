package cn.iocoder.yudao.module.opshub.controller.admin.policy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 政策详情 Response VO")
@Data
public class DealerPolicyDetailRespVO {

    @Schema(description = "政策ID")
    private Long id;

    @Schema(description = "经销商ID")
    private Long dealerId;

    @Schema(description = "经销商编码")
    private String dealerCode;

    @Schema(description = "经销商名称")
    private String dealerName;

    @Schema(description = "产品线编码")
    private String productLineCode;

    @Schema(description = "产品线名称")
    private String productLineName;

    @Schema(description = "政策编码")
    private String policyCode;

    @Schema(description = "政策名称")
    private String policyName;

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

    @Schema(description = "来源合同编码")
    private String contractCode;

    @Schema(description = "来源合同名称")
    private String contractName;

    @Schema(description = "政策描述")
    private String policyDesc;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "指标列表")
    private List<IndicatorVO> indicators;

    @Schema(description = "指标 VO")
    @Data
    public static class IndicatorVO {

        @Schema(description = "指标ID")
        private Long id;

        @Schema(description = "指标名称")
        private String indicatorName;

        @Schema(description = "年度")
        private Integer targetYear;

        @Schema(description = "月份")
        private Integer targetMonth;

        @Schema(description = "目标值")
        private BigDecimal targetValue;

        @Schema(description = "达成值")
        private BigDecimal achievedValue;

        @Schema(description = "单位")
        private String unit;

        @Schema(description = "达成率%")
        private BigDecimal rate;
    }
}
