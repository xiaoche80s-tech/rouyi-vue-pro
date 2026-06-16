package cn.iocoder.yudao.module.opshub.controller.admin.cs.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 完成咨询处理 Request VO")
@Data
public class CsSessionCompleteReqVO {

    @Schema(description = "会话 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "会话 ID 不能为空")
    private Long id;

    @Schema(description = "解决方案摘要", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "解决方案摘要不能为空")
    private String solutionSummary;

}
