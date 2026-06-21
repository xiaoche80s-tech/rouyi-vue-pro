package cn.iocoder.yudao.module.opshub.controller.admin.oprequest.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 操作请求提交处理结果 Request VO")
@Data
public class OpRequestSubmitResultReqVO {

    @Schema(description = "操作请求 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "操作请求 ID 不能为空")
    private Long id;

    @Schema(description = "附件 ID 列表（签约类型必填）")
    private List<Long> attachmentIds;

}
