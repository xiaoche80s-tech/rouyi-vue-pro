package cn.iocoder.yudao.module.opshub.controller.admin.oprequest.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 操作请求验收 Request VO")
@Data
public class OpRequestVerifyReqVO {

    @Schema(description = "操作请求 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "操作请求 ID 不能为空")
    private Long id;

    @Schema(description = "是否验收通过", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "验收结果不能为空")
    private Boolean passed;

    @Schema(description = "不通过时的原因")
    private String rejectReason;

}
