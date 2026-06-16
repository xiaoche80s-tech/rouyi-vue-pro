package cn.iocoder.yudao.module.opshub.controller.admin.cs.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 工单验收 Request VO")
@Data
public class CsTaskVerifyReqVO {

    @Schema(description = "工单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "工单ID不能为空")
    private Long id;

    @Schema(description = "是否通过", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "验收结果不能为空")
    private Boolean passed;

    @Schema(description = "退回原因（验收不通过时填写）")
    private String rejectReason;

}
