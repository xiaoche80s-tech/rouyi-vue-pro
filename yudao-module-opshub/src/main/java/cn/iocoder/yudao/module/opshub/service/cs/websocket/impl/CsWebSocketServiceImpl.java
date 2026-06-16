package cn.iocoder.yudao.module.opshub.service.cs.websocket.impl;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.module.infra.api.websocket.WebSocketSenderApi;
import cn.iocoder.yudao.module.opshub.service.cs.websocket.CsWebSocketService;
import cn.iocoder.yudao.module.opshub.service.cs.websocket.dto.CsTaskNotification;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

/**
 * 客服 WebSocket 推送 Service 实现类
 * <p>
 * 当调用方处于事务中时，推送会延迟到事务提交后再异步执行，
 * 避免客户端收到 WebSocket 消息时数据库变更尚未可见。
 */
@Service
@Validated
@Slf4j
public class CsWebSocketServiceImpl implements CsWebSocketService {

    @Resource
    private WebSocketSenderApi webSocketSenderApi;

    @Override
    public void sendTaskNotifyAsync(Long userId, CsTaskNotification notification) {
        executeAfterTransaction(() -> getSelf().doSendToUser(userId, notification));
    }

    @Override
    public void broadcastSlaAlert(CsTaskNotification notification) {
        executeAfterTransaction(() -> getSelf().doBroadcastToAdmin(notification));
    }

    /**
     * 异步推送给指定用户
     */
    @Async
    public void doSendToUser(Long userId, CsTaskNotification notification) {
        try {
            webSocketSenderApi.sendObject(UserTypeEnum.ADMIN.getValue(), userId,
                    notification.getType(), notification);
        } catch (Exception e) {
            log.error("[doSendToUser][userId({}) notification({}) 发送失败]", userId, notification, e);
        }
    }

    /**
     * 异步广播给所有管理端用户
     */
    @Async
    public void doBroadcastToAdmin(CsTaskNotification notification) {
        try {
            webSocketSenderApi.sendObject(UserTypeEnum.ADMIN.getValue(),
                    notification.getType(), notification);
        } catch (Exception e) {
            log.error("[doBroadcastToAdmin][notification({}) 广播失败]", notification, e);
        }
    }

    /**
     * 事务感知的任务调度：
     * - 有事务：注册 afterCommit 回调，事务提交后再执行
     * - 无事务：直接执行
     */
    private void executeAfterTransaction(Runnable task) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            task.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {
                task.run();
            }

        });
    }

    /**
     * 获得自身的代理对象，解决 @Async AOP 代理问题
     */
    private CsWebSocketServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
