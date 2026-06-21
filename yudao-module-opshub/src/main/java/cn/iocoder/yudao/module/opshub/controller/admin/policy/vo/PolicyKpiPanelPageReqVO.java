package cn.iocoder.yudao.module.opshub.controller.admin.policy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - KPI 面板查询 Request VO")
@Data
public class PolicyKpiPanelPageReqVO {

    @Schema(description = "政策类型（逗号分隔）")
    private String policyTypes;

    @Schema(description = "指标名称（逗号分隔）")
    private String indicatorNames;

    @Schema(description = "达成类型（逗号分隔）")
    private String achievementTypes;

    @Schema(description = "月份（逗号分隔）")
    private String months;

    @Schema(description = "产品线编码（逗号分隔）")
    private String productLineCodes;

    @Schema(description = "经销商ID（逗号分隔）")
    private String dealerIds;

    @Schema(description = "政策编码（模糊搜索）")
    private String policyCode;

    @Schema(description = "年度", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "年度不能为空")
    private Integer targetYear;

    @Schema(description = "指标名称（柱状图/目标分布必填）")
    private String indicatorName;

}
