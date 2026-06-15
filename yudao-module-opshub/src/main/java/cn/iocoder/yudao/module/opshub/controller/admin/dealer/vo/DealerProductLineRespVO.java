package cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 产品线 Response VO")
@Data
public class DealerProductLineRespVO {

    @Schema(description = "产品线ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "产品线名称", example = "骨科")
    private String productLineName;

    @Schema(description = "产品线编码", example = "GK")
    private String productLineCode;

    @Schema(description = "排序", example = "0")
    private Integer sort;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
