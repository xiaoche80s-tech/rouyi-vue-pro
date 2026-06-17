package cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 产品线授权项 VO")
@Data
public class ProductLineItemVO {

    @Schema(description = "产品线编码", example = "GK")
    private String productLineCode;

    @Schema(description = "产品线名称", example = "骨科")
    private String productLineName;

}
