package cn.iocoder.yudao.module.opshub.controller.admin.policy.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 达成明细分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DealerPolicyAchievementPageReqVO extends PageParam {

    @Schema(description = "指标ID")
    private Long indicatorId;

    @Schema(description = "层级")
    private String achieveLevel;

}
