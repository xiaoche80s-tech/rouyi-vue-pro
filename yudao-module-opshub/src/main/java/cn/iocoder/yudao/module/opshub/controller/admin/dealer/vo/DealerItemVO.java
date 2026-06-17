package cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 经销商授权项 VO")
@Data
public class DealerItemVO {

    @Schema(description = "经销商编码", example = "D001")
    private String dealerCode;

    @Schema(description = "经销商名称", example = "华东经销商")
    private String dealerName;

}
