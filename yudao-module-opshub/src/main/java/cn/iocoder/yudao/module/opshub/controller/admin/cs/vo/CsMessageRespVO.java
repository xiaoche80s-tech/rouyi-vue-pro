package cn.iocoder.yudao.module.opshub.controller.admin.cs.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 咨询消息 Response VO")
@Data
public class CsMessageRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "关联会话 ID")
    private Long sessionId;

    @Schema(description = "关联会话编号")
    private String sessionNo;

    @Schema(description = "发送人用户 ID")
    private Long senderId;

    @Schema(description = "发送人姓名")
    private String senderName;

    @Schema(description = "发送人角色")
    private String senderRole;

    @Schema(description = "消息类型")
    private String messageType;

    @Schema(description = "文本内容")
    private String content;

    @Schema(description = "附件 ID 列表（逗号分隔）")
    private String attachmentIds;

    @Schema(description = "链接地址")
    private String linkUrl;

    @Schema(description = "链接标题")
    private String linkTitle;

    @Schema(description = "是否已读")
    private Integer isRead;

    @Schema(description = "创建时间（发送时间）")
    private LocalDateTime createTime;

}
