package cn.iocoder.yudao.module.opshub.controller.admin.policy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "管理后台 - 政策创建/更新 Request VO")
@Data
public class DealerPolicySaveReqVO {

    @Schema(description = "政策ID（更新时必填）")
    private Long id;

    @Schema(description = "政策名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026年Q1返利政策")
    private String policyName;

    @Schema(description = "政策类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "rebate")
    private String policyType;

    @Schema(description = "达成类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "quarter")
    private String achievementType;

    @Schema(description = "经销商ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long dealerId;

    @Schema(description = "产品线编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "PL-001")
    private String productLineCode;

    @Schema(description = "产品线名称", example = "骨科")
    private String productLineName;

    @Schema(description = "政策状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "executing")
    private String policyStatus;

    @Schema(description = "政策开始日期", example = "2026-01-01")
    private LocalDate startDate;

    @Schema(description = "政策结束日期", example = "2026-12-31")
    private LocalDate endDate;

    @Schema(description = "政策描述", example = "2026年第一季度骨科返利政策")
    private String policyDesc;

    @Schema(description = "来源合同编码", example = "POL-2026-001")
    private String contractCode;

    @Schema(description = "来源合同名称", example = "2026年政策合同")
    private String contractName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "指标列表")
    private List<IndicatorSaveVO> indicators;

    @Schema(description = "指标保存 VO")
    @Data
    public static class IndicatorSaveVO {

        @Schema(description = "指标名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "骨科关节销量")
        private String indicatorName;

        @Schema(description = "年度", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026")
        private Integer targetYear;

        @Schema(description = "月份", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
        private Integer targetMonth;

        @Schema(description = "目标值", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        private BigDecimal targetValue;

        @Schema(description = "达成值", example = "0")
        private BigDecimal achievedValue;

        @Schema(description = "单位", requiredMode = Schema.RequiredMode.REQUIRED, example = "件")
        private String unit;
    }
}
