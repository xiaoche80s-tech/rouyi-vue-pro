package cn.iocoder.yudao.module.opshub.service.cs.websocket;

import cn.iocoder.yudao.module.opshub.service.cs.websocket.dto.CsChatMessage;
import cn.iocoder.yudao.module.opshub.service.cs.websocket.dto.CsTaskNotification;

import java.util.List;

/**
 * 客服 WebSocket 推送 Service 接口
 */
public interface CsWebSocketService {

    /**
     * 推送工单通知给指定用户（事务感知 + 异步）
     */
    void sendTaskNotifyAsync(Long userId, CsTaskNotification notification);

    /**
     * 广播 SLA 超时告警给所有管理端用户（事务感知 + 异步）
     */
    void broadcastSlaAlert(CsTaskNotification notification);

    // ========== Step 7: 咨询聊天 WebSocket ==========

    /**
     * 推送聊天消息给指定用户（事务感知 + 异步）
     */
    void sendChatMessageAsync(Long userId, CsChatMessage chatMessage);

    /**
     * 推送会话事件给指定用户（接单/完成/关闭）（事务感知 + 异步）
     */
    void sendSessionEventAsync(Long userId, CsChatMessage sessionEvent);

    /**
     * 广播新咨询通知给所有管理端用户（事务感知 + 异步）
     */
    void broadcastNewConsult(CsChatMessage consultNotify);

}
