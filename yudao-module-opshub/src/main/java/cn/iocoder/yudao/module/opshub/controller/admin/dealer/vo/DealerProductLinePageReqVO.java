package cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 产品线分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DealerProductLinePageReqVO extends PageParam {

    @Schema(description = "产品线名称", example = "骨科")
    private String productLineName;

    @Schema(description = "产品线编码", example = "GK")
    private String productLineCode;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
