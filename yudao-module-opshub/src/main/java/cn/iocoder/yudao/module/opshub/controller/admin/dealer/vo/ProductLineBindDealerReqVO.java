package cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Schema(description = "管理后台 - 产品线绑定经销商 Request VO")
@Data
public class ProductLineBindDealerReqVO {

    @Schema(description = "产品线编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "GK")
    @NotEmpty(message = "产品线编码不能为空")
    private String productLineCode;

    @Schema(description = "经销商编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "HK")
    @NotEmpty(message = "经销商编码不能为空")
    private String dealerCode;

}
