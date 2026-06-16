package cn.iocoder.yudao.module.opshub.service.cs.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.CsMessagePageReqVO;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.CsMessageSendReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.cs.CsMessageDO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.cs.CsSessionDO;
import cn.iocoder.yudao.module.opshub.dal.mysql.cs.CsMessageMapper;
import cn.iocoder.yudao.module.opshub.dal.mysql.cs.CsSessionMapper;
import cn.iocoder.yudao.module.opshub.service.cs.CsMessageService;
import cn.iocoder.yudao.module.opshub.service.cs.websocket.CsWebSocketService;
import cn.iocoder.yudao.module.opshub.service.cs.websocket.dto.CsChatMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.opshub.enums.ErrorCodeConstants.*;

/**
 * 咨询消息 Service 实现类
 */
@Service
@Validated
@Slf4j
public class CsMessageServiceImpl implements CsMessageService {

    @Resource
    private CsMessageMapper csMessageMapper;

    @Resource
    @Lazy
    private CsSessionMapper csSessionMapper;

    @Resource
    private CsWebSocketService csWebSocketService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CsMessageDO sendMessage(CsMessageSendReqVO reqVO) {
        // 1. 校验会话存在
        CsSessionDO session = csSessionMapper.selectById(reqVO.getSessionId());
        if (session == null) {
            throw exception(CS_SESSION_NOT_EXISTS);
        }

        // 2. 构建消息 DO
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();
        String currentUserName = SecurityFrameworkUtils.getLoginUserNickname();
        String senderRole = resolveSenderRole(currentUserId, session);

        CsMessageDO messageDO = new CsMessageDO();
        messageDO.setSessionId(reqVO.getSessionId());
        messageDO.setSessionNo(session.getSessionNo());
        messageDO.setSenderId(currentUserId);
        messageDO.setSenderName(currentUserName);
        messageDO.setSenderRole(senderRole);
        messageDO.setMessageType(reqVO.getMessageType());
        messageDO.setContent(reqVO.getContent());
        messageDO.setAttachmentIds(CollUtil.isNotEmpty(reqVO.getAttachmentIds())
                ? CollUtil.join(reqVO.getAttachmentIds(), ",") : null);
        messageDO.setLinkUrl(reqVO.getLinkUrl());
        messageDO.setLinkTitle(reqVO.getLinkTitle());
        messageDO.setIsRead(0);

        // 3. 持久化
        csMessageMapper.insert(messageDO);

        // 4. 更新会话的 last_message 和 last_message_time 和 message_count
        String lastMsg = messageDO.getContent();
        if (lastMsg == null) {
            lastMsg = messageDO.getLinkTitle() != null ? messageDO.getLinkTitle() : "[附件]";
        }
        csSessionMapper.updateById(new CsSessionDO()
                .setId(session.getId())
                .setLastMessage(StrUtil.maxLength(lastMsg, 200))
                .setLastMessageTime(messageDO.getCreateTime())
                .setMessageCount(session.getMessageCount() + 1));

        // 5. WebSocket 推送消息给对方
        CsChatMessage chatMsg = new CsChatMessage()
                .setMessageId(messageDO.getId())
                .setSessionId(session.getId())
                .setSessionNo(session.getSessionNo())
                .setType(CsChatMessage.TYPE_CHAT_MESSAGE)
                .setSenderId(currentUserId)
                .setSenderName(currentUserName)
                .setSenderRole(senderRole)
                .setMessageType(reqVO.getMessageType())
                .setContent(reqVO.getContent())
                .setAttachmentIds(messageDO.getAttachmentIds())
                .setLinkUrl(reqVO.getLinkUrl())
                .setLinkTitle(reqVO.getLinkTitle())
                .setCreateTime(messageDO.getCreateTime());

        // 推送给发起人（如果发送人不是发起人）
        if (session.getInitiatorId() != null && !currentUserId.equals(session.getInitiatorId())) {
            csWebSocketService.sendChatMessageAsync(session.getInitiatorId(), chatMsg);
        }
        // 推送给处理人（如果发送人不是处理人）
        if (session.getAssigneeId() != null && !currentUserId.equals(session.getAssigneeId())) {
            csWebSocketService.sendChatMessageAsync(session.getAssigneeId(), chatMsg);
        }

        return messageDO;
    }

    @Override
    public List<CsMessageDO> getMessageList(Long sessionId) {
        return csMessageMapper.selectListBySessionId(sessionId);
    }

    @Override
    public PageResult<CsMessageDO> getMessagePage(CsMessagePageReqVO reqVO) {
        return csMessageMapper.selectPageBySession(reqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long sessionId) {
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();
        csMessageMapper.updateMarkRead(sessionId, currentUserId);
    }

    // ========== 辅助方法 ==========

    /**
     * 根据当前用户与会话的关系推断发送人角色
     */
    private String resolveSenderRole(Long userId, CsSessionDO session) {
        if (userId == null) {
            return "system";
        }
        if (userId.equals(session.getInitiatorId())) {
            return "dealer";
        }
        if (userId.equals(session.getAssigneeId())) {
            return "executor";
        }
        return "admin";
    }

}
