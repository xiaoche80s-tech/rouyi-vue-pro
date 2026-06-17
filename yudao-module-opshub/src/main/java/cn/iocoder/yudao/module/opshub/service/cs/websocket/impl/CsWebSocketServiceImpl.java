package cn.iocoder.yudao.module.opshub.service.cs.websocket.impl;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.module.infra.api.websocket.WebSocketSenderApi;
import cn.iocoder.yudao.module.opshub.dal.mysql.dealer.ExecutorProductLineScopeMapper;
import cn.iocoder.yudao.module.opshub.enums.OpsRoleCodeConstants;
import cn.iocoder.yudao.module.opshub.service.cs.websocket.CsWebSocketService;
import cn.iocoder.yudao.module.opshub.service.cs.websocket.dto.CsChatMessage;
import cn.iocoder.yudao.module.opshub.service.cs.websocket.dto.CsTaskNotification;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.api.permission.RoleApi;
import cn.iocoder.yudao.module.system.api.permission.dto.RoleRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Resource
    private PermissionApi permissionApi;

    @Resource
    private RoleApi roleApi;

    @Resource
    private ExecutorProductLineScopeMapper executorProductLineScopeMapper;

    @Override
    public void sendTaskNotifyAsync(Long userId, CsTaskNotification notification) {
        executeAfterTransaction(() -> getSelf().doSendToUser(userId, notification));
    }

    @Override
    public void broadcastSlaAlert(CsTaskNotification notification) {
        executeAfterTransaction(() -> getSelf().doBroadcastToAdmin(notification));
    }

    // ========== Step 7: 咨询聊天 WebSocket ==========

    @Override
    public void sendChatMessageAsync(Long userId, CsChatMessage chatMessage) {
        executeAfterTransaction(() -> getSelf().doSendChatToUser(userId, chatMessage));
    }

    @Override
    public void sendSessionEventAsync(Long userId, CsChatMessage sessionEvent) {
        executeAfterTransaction(() -> getSelf().doSendChatToUser(userId, sessionEvent));
    }

    @Override
    public void broadcastNewConsult(CsChatMessage consultNotify) {
        executeAfterTransaction(() -> getSelf().doBroadcastChatToAdmin(consultNotify));
    }

    @Override
    public void notifyMatchingExecutors(CsChatMessage consultNotify, String productLineCode) {
        executeAfterTransaction(() -> getSelf().doNotifyMatchingExecutors(consultNotify, productLineCode));
    }

    /**
     * 异步推送聊天消息给指定用户
     */
    @Async
    public void doSendChatToUser(Long userId, CsChatMessage chatMessage) {
        try {
            webSocketSenderApi.sendObject(UserTypeEnum.ADMIN.getValue(), userId,
                    chatMessage.getType(), chatMessage);
        } catch (Exception e) {
            log.error("[doSendChatToUser][userId({}) chatMessage({}) 发送失败]", userId, chatMessage, e);
        }
    }

    /**
     * 异步广播聊天消息给所有管理端用户
     */
    @Async
    public void doBroadcastChatToAdmin(CsChatMessage chatMessage) {
        try {
            webSocketSenderApi.sendObject(UserTypeEnum.ADMIN.getValue(),
                    chatMessage.getType(), chatMessage);
        } catch (Exception e) {
            log.error("[doBroadcastChatToAdmin][chatMessage({}) 广播失败]", chatMessage, e);
        }
    }

    /**
     * 异步精准推送新咨询通知给匹配的执行员（角色 × 产品线交集）
     */
    @Async
    public void doNotifyMatchingExecutors(CsChatMessage chatMessage, String productLineCode) {
        try {
            // 1. 获取执行员角色的用户 ID 集合
            RoleRespDTO executorRole = roleApi.getRoleByCode(OpsRoleCodeConstants.SERVICE_EXECUTOR);
            if (executorRole == null) {
                log.warn("[doNotifyMatchingExecutors][未找到执行员角色 code={}]", OpsRoleCodeConstants.SERVICE_EXECUTOR);
                return;
            }
            Set<Long> executorUserIds = permissionApi.getUserRoleIdListByRoleIds(Collections.singleton(executorRole.getId()));
            if (executorUserIds == null || executorUserIds.isEmpty()) {
                log.debug("[doNotifyMatchingExecutors][无执行员用户，跳过推送]");
                return;
            }

            // 2. 如果有产品线编码，取交集
            if (productLineCode != null && !productLineCode.isEmpty()) {
                Set<Long> productLineUserIds = executorProductLineScopeMapper.selectUserIdsByProductLineCode(productLineCode);
                executorUserIds = executorUserIds.stream()
                        .filter(productLineUserIds::contains)
                        .collect(Collectors.toSet());
            }

            // 3. 逐一推送
            for (Long userId : executorUserIds) {
                webSocketSenderApi.sendObject(UserTypeEnum.ADMIN.getValue(), userId,
                        chatMessage.getType(), chatMessage);
            }
            log.debug("[doNotifyMatchingExecutors][推送新咨询通知给 {} 个执行员, productLineCode={}]",
                    executorUserIds.size(), productLineCode);
        } catch (Exception e) {
            log.error("[doNotifyMatchingExecutors][chatMessage({}) productLineCode({}) 精准推送失败]",
                    chatMessage, productLineCode, e);
        }
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
