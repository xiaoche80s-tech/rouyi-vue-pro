package cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 产品线创建/修改 Request VO")
@Data
public class DealerProductLineSaveReqVO {

    @Schema(description = "产品线ID", example = "1")
    private Long id;

    @Schema(description = "产品线名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "骨科")
    @NotEmpty(message = "产品线名称不能为空")
    private String name;

    @Schema(description = "产品线编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "GK")
    @NotEmpty(message = "产品线编码不能为空")
    private String code;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
