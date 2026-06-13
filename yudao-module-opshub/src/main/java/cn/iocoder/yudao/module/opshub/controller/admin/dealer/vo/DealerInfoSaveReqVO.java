package cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 经销商创建/修改 Request VO")
@Data
public class DealerInfoSaveReqVO {

    @Schema(description = "经销商ID", example = "1")
    private Long id;

    @Schema(description = "经销商名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "华康医疗器械")
    @NotEmpty(message = "经销商名称不能为空")
    private String name;

    @Schema(description = "经销商编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "HK")
    @NotEmpty(message = "经销商编码不能为空")
    private String code;

    @Schema(description = "联系人", example = "张三")
    private String contactName;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "地址", example = "北京市朝阳区")
    private String address;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
