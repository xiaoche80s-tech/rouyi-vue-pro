package cn.iocoder.yudao.module.opshub.controller.admin.cs.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 咨询消息分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CsMessagePageReqVO extends PageParam {

    @Schema(description = "会话 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "会话 ID 不能为空")
    private Long sessionId;

}
