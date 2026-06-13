package cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 经销商分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DealerInfoPageReqVO extends PageParam {

    @Schema(description = "经销商名称", example = "华康")
    private String name;

    @Schema(description = "经销商编码", example = "HK")
    private String code;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
