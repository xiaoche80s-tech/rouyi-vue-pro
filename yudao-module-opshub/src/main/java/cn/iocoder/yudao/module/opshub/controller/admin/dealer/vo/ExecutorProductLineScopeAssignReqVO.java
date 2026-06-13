package cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Schema(description = "管理后台 - 执行员产品线授权分配 Request VO")
@Data
public class ExecutorProductLineScopeAssignReqVO {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "产品线ID集合", example = "[1, 2, 3]")
    private Set<Long> productLineIds;

}
