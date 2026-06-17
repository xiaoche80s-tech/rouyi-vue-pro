package cn.iocoder.yudao.module.opshub.service.cs.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.cs.CsSessionDO;
import cn.iocoder.yudao.module.opshub.dal.mysql.cs.CsSessionMapper;
import cn.iocoder.yudao.module.opshub.enums.CsSessionStatusEnum;
import cn.iocoder.yudao.module.opshub.enums.OpsRoleCodeConstants;
import cn.iocoder.yudao.module.opshub.service.cs.CsMessageService;
import cn.iocoder.yudao.module.opshub.service.cs.CsSessionService;
import cn.iocoder.yudao.module.opshub.service.cs.websocket.CsWebSocketService;
import cn.iocoder.yudao.module.opshub.service.cs.websocket.dto.CsChatMessage;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.opshub.enums.ErrorCodeConstants.*;

/**
 * 咨询会话 Service 实现类
 */
@Service
@Validated
@Slf4j
public class CsSessionServiceImpl implements CsSessionService {

    // ========== 站内信模板编码 ==========
    private static final String NOTIFY_SESSION_CREATED = "cs-session-created";
    private static final String NOTIFY_SESSION_ACCEPTED = "cs-session-accepted";
    private static final String NOTIFY_SESSION_COMPLETED = "cs-session-completed";
    private static final String NOTIFY_SESSION_CLOSED = "cs-session-closed";

    @Resource
    private CsSessionMapper csSessionMapper;

    @Resource
    private CsWebSocketService csWebSocketService;

    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;

    @Resource
    private PermissionApi permissionApi;

    @Resource
    @Lazy
    private CsMessageService csMessageService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSession(CsSessionCreateReqVO reqVO) {
        // 1. 去重校验：相同 consult_type + context_code + 活跃状态
        if (reqVO.getContextCode() != null) {
            CsSessionDO existing = csSessionMapper.selectDuplicateSession(
                    reqVO.getConsultType(), reqVO.getContextCode());
            if (existing != null) {
                return existing.getId();
            }
        }

        // 2. 生成编号
        String sessionNo = generateSessionNo();

        // 3. 构建 DO
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();
        String currentUserName = SecurityFrameworkUtils.getLoginUserNickname();
        CsSessionDO sessionDO = BeanUtils.toBean(reqVO, CsSessionDO.class);
        sessionDO.setSessionNo(sessionNo);
        sessionDO.setStatus(CsSessionStatusEnum.PENDING.getCode());
        sessionDO.setInitiatorId(currentUserId);
        sessionDO.setInitiatorName(currentUserName);
        sessionDO.setMessageCount(0);

        // 4. 插入
        csSessionMapper.insert(sessionDO);

        // 5. 创建系统消息
        csMessageService.sendMessage(new CsMessageSendReqVO()
                .setSessionId(sessionDO.getId())
                .setMessageType("system")
                .setContent("咨询会话已创建"));

        // 6. 精准推送新咨询通知给匹配的执行员 + 站内信
        CsChatMessage consultNotify = buildChatNotify(sessionDO, CsChatMessage.TYPE_NEW_CONSULT);
        csWebSocketService.notifyMatchingExecutors(consultNotify, sessionDO.getProductLineCode());

        return sessionDO.getId();
    }

    @Override
    public CsSessionDO getSession(Long id) {
        CsSessionDO session = csSessionMapper.selectById(id);
        if (session == null) {
            throw exception(CS_SESSION_NOT_EXISTS);
        }
        return session;
    }

    @Override
    public PageResult<CsSessionDO> getSessionPage(CsSessionPageReqVO reqVO) {
        // 按角色注入可见性过滤
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();
        reqVO.setCurrentUserId(currentUserId);
        reqVO.setViewScope(resolveViewScope(currentUserId));
        return csSessionMapper.selectPage(reqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptSession(Long id) {
        CsSessionDO session = validateSessionExists(id);
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();
        String currentUserName = SecurityFrameworkUtils.getLoginUserNickname();

        // 校验状态：仅待处理可接单
        validateStatus(session, CsSessionStatusEnum.PENDING);

        // 更新状态
        csSessionMapper.updateById(new CsSessionDO()
                .setId(id)
                .setStatus(CsSessionStatusEnum.PROCESSING.getCode())
                .setAssigneeId(currentUserId)
                .setAssigneeName(currentUserName)
                .setAcceptTime(LocalDateTime.now()));

        // 推送系统消息
        csMessageService.sendMessage(new CsMessageSendReqVO()
                .setSessionId(id)
                .setMessageType("system")
                .setContent(currentUserName + " 已接单"));

        // WebSocket + 站内信通知发起人
        CsChatMessage event = buildChatNotify(session, CsChatMessage.TYPE_SESSION_EVENT);
        event.setContent(CsChatMessage.EVENT_ACCEPTED);
        csWebSocketService.sendSessionEventAsync(session.getInitiatorId(), event);
        sendNotify(session.getInitiatorId(), NOTIFY_SESSION_ACCEPTED, buildNotifyParams(session));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeSession(CsSessionCompleteReqVO reqVO) {
        CsSessionDO session = validateSessionExists(reqVO.getId());
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();

        // 校验状态：仅处理中可完成
        validateStatus(session, CsSessionStatusEnum.PROCESSING);
        // 校验当前操作人是处理人
        if (!currentUserId.equals(session.getAssigneeId())) {
            throw exception(CS_SESSION_NOT_ASSIGNEE);
        }

        // 更新状态
        csSessionMapper.updateById(new CsSessionDO()
                .setId(reqVO.getId())
                .setStatus(CsSessionStatusEnum.COMPLETED.getCode())
                .setCompleteTime(LocalDateTime.now())
                .setSolutionSummary(reqVO.getSolutionSummary()));

        // 推送系统消息
        csMessageService.sendMessage(new CsMessageSendReqVO()
                .setSessionId(reqVO.getId())
                .setMessageType("system")
                .setContent("咨询已完成处理"));

        // WebSocket + 站内信通知发起人
        CsChatMessage event = buildChatNotify(session, CsChatMessage.TYPE_SESSION_EVENT);
        event.setContent(CsChatMessage.EVENT_COMPLETED);
        csWebSocketService.sendSessionEventAsync(session.getInitiatorId(), event);
        sendNotify(session.getInitiatorId(), NOTIFY_SESSION_COMPLETED, buildNotifyParams(session));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeSession(Long id) {
        CsSessionDO session = validateSessionExists(id);
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();

        // 校验当前操作人是发起人（经销商）
        if (!currentUserId.equals(session.getInitiatorId())) {
            throw exception(CS_SESSION_NOT_INITIATOR);
        }

        // 更新状态
        csSessionMapper.updateById(new CsSessionDO()
                .setId(id)
                .setStatus(CsSessionStatusEnum.CLOSED.getCode())
                .setCloseTime(LocalDateTime.now()));

        // 推送系统消息
        csMessageService.sendMessage(new CsMessageSendReqVO()
                .setSessionId(id)
                .setMessageType("system")
                .setContent("经销商已关闭对话"));

        // WebSocket + 站内信通知处理人
        if (session.getAssigneeId() != null) {
            CsChatMessage event = buildChatNotify(session, CsChatMessage.TYPE_SESSION_EVENT);
            event.setContent(CsChatMessage.EVENT_CLOSED);
            csWebSocketService.sendSessionEventAsync(session.getAssigneeId(), event);
            sendNotify(session.getAssigneeId(), NOTIFY_SESSION_CLOSED, buildNotifyParams(session));
        }
    }

    @Override
    public CsConsultStatisticsRespVO getStatistics() {
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();
        String viewScope = resolveViewScope(currentUserId);
        Map<Integer, Long> countMap = csSessionMapper.selectCountGroupByStatusWithScope(viewScope, currentUserId);
        int pendingCount = countMap.getOrDefault(CsSessionStatusEnum.PENDING.getCode(), 0L).intValue();
        int processingCount = countMap.getOrDefault(CsSessionStatusEnum.PROCESSING.getCode(), 0L).intValue();
        int completedCount = countMap.getOrDefault(CsSessionStatusEnum.COMPLETED.getCode(), 0L).intValue();
        int closedCount = countMap.getOrDefault(CsSessionStatusEnum.CLOSED.getCode(), 0L).intValue();

        CsConsultStatisticsRespVO respVO = new CsConsultStatisticsRespVO();
        respVO.setTotalCount(pendingCount + processingCount + completedCount + closedCount);
        respVO.setPendingCount(pendingCount);
        respVO.setProcessingCount(processingCount);
        respVO.setCompletedCount(completedCount);
        respVO.setClosedCount(closedCount);
        return respVO;
    }

    // ========== 辅助方法 ==========

    private CsSessionDO validateSessionExists(Long id) {
        CsSessionDO session = csSessionMapper.selectById(id);
        if (session == null) {
            throw exception(CS_SESSION_NOT_EXISTS);
        }
        return session;
    }

    private void validateStatus(CsSessionDO session, CsSessionStatusEnum expected) {
        if (CsSessionStatusEnum.CLOSED.getCode().equals(session.getStatus())) {
            throw exception(CS_SESSION_ALREADY_CLOSED);
        }
        if (!expected.getCode().equals(session.getStatus())) {
            switch (expected) {
                case PENDING -> throw exception(CS_SESSION_NOT_PENDING);
                case PROCESSING -> throw exception(CS_SESSION_NOT_PROCESSING);
                case COMPLETED -> throw exception(CS_SESSION_NOT_COMPLETED);
                default -> throw exception(CS_SESSION_NOT_EXISTS);
            }
        }
    }

    private String resolveViewScope(Long userId) {
        if (hasRole(userId, OpsRoleCodeConstants.DEALER)) {
            return "creator";
        }
        if (hasRole(userId, OpsRoleCodeConstants.SERVICE_EXECUTOR)) {
            return "assignee";
        }
        return "all";
    }

    private boolean hasRole(Long userId, String roleCode) {
        return permissionApi.hasAnyRoles(userId, roleCode);
    }

    private String generateSessionNo() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        Integer maxSeq = csSessionMapper.selectMaxSeqToday(dateStr);
        return String.format("CS-%s-%03d", dateStr, maxSeq + 1);
    }

    private CsChatMessage buildChatNotify(CsSessionDO session, String type) {
        return new CsChatMessage()
                .setSessionId(session.getId())
                .setSessionNo(session.getSessionNo())
                .setType(type)
                .setSenderId(session.getInitiatorId())
                .setSenderName(session.getInitiatorName())
                .setConsultType(session.getConsultType())
                .setDealerName(session.getDealerName())
                .setContext(session.getContext());
    }

    private void sendNotify(Long userId, String templateCode, Map<String, Object> params) {
        if (userId == null) {
            return;
        }
        try {
            notifyMessageSendApi.sendSingleMessageToAdmin(
                    new NotifySendSingleToUserReqDTO()
                            .setUserId(userId)
                            .setTemplateCode(templateCode)
                            .setTemplateParams(params));
        } catch (Exception e) {
            log.warn("[sendNotify][发送站内信失败 templateCode={}, userId={}]", templateCode, userId, e);
        }
    }

    private Map<String, Object> buildNotifyParams(CsSessionDO session) {
        Map<String, Object> params = new HashMap<>();
        params.put("sessionNo", session.getSessionNo());
        params.put("consultType", session.getConsultType());
        return params;
    }

}
