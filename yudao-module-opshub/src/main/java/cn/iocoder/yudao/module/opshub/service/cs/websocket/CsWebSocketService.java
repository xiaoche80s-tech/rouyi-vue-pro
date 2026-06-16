package cn.iocoder.yudao.module.opshub.service.cs.websocket;

import cn.iocoder.yudao.module.opshub.service.cs.websocket.dto.CsTaskNotification;

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

}
