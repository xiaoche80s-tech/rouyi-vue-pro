package cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 产品线精简 Response VO")
@Data
public class DealerProductLineSimpleRespVO {

    @Schema(description = "产品线ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "产品线名称", example = "骨科")
    private String productLineName;

    @Schema(description = "产品线编码", example = "GK")
    private String productLineCode;

}
