package cn.iocoder.yudao.module.opshub.controller.admin.cs.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 转单 Request VO")
@Data
public class CsTaskTransferReqVO {

    @Schema(description = "工单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "工单ID不能为空")
    private Long id;

    @Schema(description = "新处理人用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "新处理人不能为空")
    private Long newAssigneeId;

    @Schema(description = "转单原因")
    private String reason;

}
