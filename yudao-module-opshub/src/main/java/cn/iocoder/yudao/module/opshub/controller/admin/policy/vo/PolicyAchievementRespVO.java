package cn.iocoder.yudao.module.opshub.controller.admin.policy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 达成明细 Response VO")
@Data
public class PolicyAchievementRespVO {

    @Schema(description = "名称（省份/医院/产品）")
    private String name;

    @Schema(description = "达成值")
    private BigDecimal achievedValue;

}
