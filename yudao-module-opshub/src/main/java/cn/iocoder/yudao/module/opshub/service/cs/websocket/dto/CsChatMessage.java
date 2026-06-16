package cn.iocoder.yudao.module.opshub.service.cs.websocket.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 咨询聊天消息 WebSocket 推送 DTO
 */
@Data
@Accessors(chain = true)
public class CsChatMessage {

    /**
     * 消息 ID
     */
    private Long messageId;
    /**
     * 会话 ID
     */
    private Long sessionId;
    /**
     * 会话编号
     */
    private String sessionNo;
    /**
     * 推送类型
     */
    private String type;
    /**
     * 发送人用户 ID
     */
    private Long senderId;
    /**
     * 发送人姓名
     */
    private String senderName;
    /**
     * 发送人角色
     */
    private String senderRole;
    /**
     * 消息类型
     */
    private String messageType;
    /**
     * 文本内容
     */
    private String content;
    /**
     * 附件 ID 列表（逗号分隔）
     */
    private String attachmentIds;
    /**
     * 链接地址
     */
    private String linkUrl;
    /**
     * 链接标题
     */
    private String linkTitle;
    /**
     * 消息创建时间
     */
    private LocalDateTime createTime;

    // ========== 推送类型常量 ==========

    /**
     * 新消息推送
     */
    public static final String TYPE_CHAT_MESSAGE = "cs-chat-message";
    /**
     * 会话事件（接单/完成/关闭）
     */
    public static final String TYPE_SESSION_EVENT = "cs-session-event";
    /**
     * 新咨询通知（广播给执行员）
     */
    public static final String TYPE_NEW_CONSULT = "cs-new-consult";

    // ========== 会话事件子类型（放在 message 字段） ==========

    public static final String EVENT_ACCEPTED = "accepted";
    public static final String EVENT_COMPLETED = "completed";
    public static final String EVENT_CLOSED = "closed";

}
