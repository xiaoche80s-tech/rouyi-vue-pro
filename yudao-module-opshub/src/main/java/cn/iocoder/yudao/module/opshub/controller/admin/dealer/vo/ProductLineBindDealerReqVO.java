package cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 产品线绑定经销商 Request VO")
@Data
public class ProductLineBindDealerReqVO {

    @Schema(description = "产品线ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "产品线ID不能为空")
    private Long productLineId;

    @Schema(description = "经销商ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "经销商ID不能为空")
    private Long dealerId;

}
