package cn.iocoder.yudao.module.opshub.controller.admin.cs.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 发送咨询消息 Request VO")
@Data
public class CsMessageSendReqVO {

    @Schema(description = "会话 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "会话 ID 不能为空")
    private Long sessionId;

    @Schema(description = "消息类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "text")
    @NotBlank(message = "消息类型不能为空")
    private String messageType;

    @Schema(description = "文本内容")
    private String content;

    @Schema(description = "附件 ID 列表")
    private List<Long> attachmentIds;

    @Schema(description = "链接地址")
    private String linkUrl;

    @Schema(description = "链接标题")
    private String linkTitle;

}
