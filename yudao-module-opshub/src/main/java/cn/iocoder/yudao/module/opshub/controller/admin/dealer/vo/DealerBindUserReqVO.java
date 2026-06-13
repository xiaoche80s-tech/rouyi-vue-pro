package cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 经销商绑定用户 Request VO")
@Data
public class DealerBindUserReqVO {

    @Schema(description = "经销商ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "经销商ID不能为空")
    private Long dealerId;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

}
