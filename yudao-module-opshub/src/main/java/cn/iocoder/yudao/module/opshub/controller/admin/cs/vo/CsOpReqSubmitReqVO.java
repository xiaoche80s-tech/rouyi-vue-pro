package cn.iocoder.yudao.module.opshub.controller.admin.cs.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 提交操作请求结果 Request VO")
@Data
public class CsOpReqSubmitReqVO {

    @Schema(description = "操作请求ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "操作请求ID不能为空")
    private Long id;

    @Schema(description = "处理留言")
    private String submitRemark;

}
