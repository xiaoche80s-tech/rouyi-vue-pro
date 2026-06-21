package cn.iocoder.yudao.module.opshub.controller.admin.policy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 达成明细创建/更新 Request VO")
@Data
public class DealerPolicyAchievementSaveReqVO {

    @Schema(description = "达成明细ID（更新时必填）")
    private Long id;

    @Schema(description = "关联指标ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long indicatorId;

    @Schema(description = "指标名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "骨科关节销量")
    private String indicatorName;

    @Schema(description = "层级", requiredMode = Schema.RequiredMode.REQUIRED, example = "province")
    private String achieveLevel;

    @Schema(description = "省份", example = "上海市")
    private String province;

    @Schema(description = "省份编码", example = "310000")
    private String provinceCode;

    @Schema(description = "医院名称", example = "上海市第一人民医院")
    private String hospital;

    @Schema(description = "医院编码", example = "H-001")
    private String hospitalCode;

    @Schema(description = "产品名称", example = "膝关节假体")
    private String productName;

    @Schema(description = "达成值", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    private BigDecimal achievedValue;

}
