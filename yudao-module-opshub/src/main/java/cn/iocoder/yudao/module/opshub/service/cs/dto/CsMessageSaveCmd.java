package cn.iocoder.yudao.module.opshub.service.cs.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 系统消息落库命令（内部使用，不暴露给 HTTP 层）
 * <p>
 * 由会话生命周期方法（createSession / acceptSession / completeSession / closeSession）调用，
 * 仅持久化消息记录，不触发 WebSocket 推送。
 */
@Data
@Accessors(chain = true)
public class CsMessageSaveCmd {

    /**
     * 会话 ID
     */
    private Long sessionId;

    /**
     * 操作人用户 ID（不依赖 SecurityFrameworkUtils，由调用方显式传入）
     */
    private Long senderId;

    /**
     * 操作人姓名
     */
    private String senderName;

    /**
     * 系统消息内容
     */
    private String content;

}
