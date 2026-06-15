package cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Schema(description = "管理后台 - 用户经销商授权分配 Request VO")
@Data
public class DealerUserScopeAssignReqVO {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "经销商编码集合", example = "[\"HK\", \"ZS\"]")
    private Set<String> dealerCodes;

}
