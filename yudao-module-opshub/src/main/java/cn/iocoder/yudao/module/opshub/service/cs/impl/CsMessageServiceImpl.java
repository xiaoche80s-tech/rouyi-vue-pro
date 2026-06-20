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
import cn.iocoder.yudao.module.opshub.service.cs.dto.CsMessageSaveCmd;
import cn.iocoder.yudao.module.opshub.service.cs.websocket.CsWebSocketService;
import cn.iocoder.yudao.module.opshub.service.cs.websocket.dto.CsChatMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

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

    // ========== 对外接口 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CsMessageDO sendMessage(CsMessageSendReqVO reqVO) {
        // 1. 校验会话存在
        CsSessionDO session = validateSession(reqVO.getSessionId());

        // 2. 落库
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();
        String currentUserName = SecurityFrameworkUtils.getLoginUserNickname();
        String senderRole = resolveSenderRole(currentUserId, session);

        CsMessageDO messageDO = doSave(session, currentUserId, currentUserName, senderRole,
                reqVO.getMessageType(), reqVO.getContent(),
                CollUtil.isNotEmpty(reqVO.getAttachmentIds())
                        ? CollUtil.join(reqVO.getAttachmentIds(), ",") : null,
                reqVO.getLinkUrl(), reqVO.getLinkTitle());

        // 3. 推送 cs-chat-message 给对方（用户主动发的消息才走此路径）
        pushChatMessage(session, currentUserId, currentUserName, senderRole, messageDO);

        return messageDO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CsMessageDO saveSystemMessage(CsMessageSaveCmd cmd) {
        // 1. 校验会话存在
        CsSessionDO session = validateSession(cmd.getSessionId());

        // 2. 仅落库，不推送 WebSocket（由调用方通过 cs-session-event 携带系统消息内容）
        return doSave(session, cmd.getSenderId(), cmd.getSenderName(), "system",
                "system", cmd.getContent(), null, null, null);
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

    // ========== 私有辅助方法 ==========

    /**
     * 校验会话存在
     */
    private CsSessionDO validateSession(Long sessionId) {
        CsSessionDO session = csSessionMapper.selectById(sessionId);
        if (session == null) {
            throw exception(CS_SESSION_NOT_EXISTS);
        }
        return session;
    }

    /**
     * 落库：持久化消息 + 更新会话 lastMessage / messageCount
     */
    private CsMessageDO doSave(CsSessionDO session,
                                Long senderId, String senderName, String senderRole,
                                String messageType, String content,
                                String attachmentIds, String linkUrl, String linkTitle) {
        CsMessageDO messageDO = new CsMessageDO();
        messageDO.setSessionId(session.getId());
        messageDO.setSessionNo(session.getSessionNo());
        messageDO.setSenderId(senderId);
        messageDO.setSenderName(senderName);
        messageDO.setSenderRole(senderRole);
        messageDO.setMessageType(messageType);
        messageDO.setContent(content);
        messageDO.setAttachmentIds(attachmentIds);
        messageDO.setLinkUrl(linkUrl);
        messageDO.setLinkTitle(linkTitle);
        messageDO.setIsRead(0);

        csMessageMapper.insert(messageDO);

        // 更新会话摘要
        String lastMsg = content;
        if (lastMsg == null) {
            lastMsg = linkTitle != null ? linkTitle : "[附件]";
        }
        csSessionMapper.updateById(new CsSessionDO()
                .setId(session.getId())
                .setLastMessage(StrUtil.maxLength(lastMsg, 200))
                .setLastMessageTime(messageDO.getCreateTime())
                .setMessageCount(session.getMessageCount() + 1));

        return messageDO;
    }

    /**
     * 推送 cs-chat-message 给会话对方（发起人 / 处理人）
     */
    private void pushChatMessage(CsSessionDO session,
                                  Long currentUserId, String currentUserName,
                                  String senderRole, CsMessageDO messageDO) {
        CsChatMessage chatMsg = new CsChatMessage()
                .setMessageId(messageDO.getId())
                .setSessionId(session.getId())
                .setSessionNo(session.getSessionNo())
                .setType(CsChatMessage.TYPE_CHAT_MESSAGE)
                .setSenderId(currentUserId)
                .setSenderName(currentUserName)
                .setSenderRole(senderRole)
                .setMessageType(messageDO.getMessageType())
                .setContent(messageDO.getContent())
                .setAttachmentIds(messageDO.getAttachmentIds())
                .setLinkUrl(messageDO.getLinkUrl())
                .setLinkTitle(messageDO.getLinkTitle())
                .setCreateTime(messageDO.getCreateTime());

        // 推送给发起人（如果发送人不是发起人）
        if (session.getInitiatorId() != null && !currentUserId.equals(session.getInitiatorId())) {
            csWebSocketService.sendChatMessageAsync(session.getInitiatorId(), chatMsg);
        }
        // 推送给处理人（如果发送人不是处理人）
        if (session.getAssigneeId() != null && !currentUserId.equals(session.getAssigneeId())) {
            csWebSocketService.sendChatMessageAsync(session.getAssigneeId(), chatMsg);
        }
    }

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
