package cn.iocoder.yudao.module.opshub.service.cs.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.cs.CsOpReqDO;
import cn.iocoder.yudao.module.opshub.dal.mysql.cs.CsOpReqMapper;
import cn.iocoder.yudao.module.opshub.enums.CsOpReqStatusEnum;
import cn.iocoder.yudao.module.opshub.enums.CsOpReqTypeEnum;
import cn.iocoder.yudao.module.opshub.enums.OpsRoleCodeConstants;
import cn.iocoder.yudao.module.opshub.service.cs.CsOpReqService;
import cn.iocoder.yudao.module.opshub.service.cs.websocket.CsWebSocketService;
import cn.iocoder.yudao.module.opshub.service.cs.websocket.dto.CsTaskNotification;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.framework.common.biz.system.permission.PermissionCommonApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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
 * 操作请求 Service 实现类
 */
@Service
@Validated
@Slf4j
public class CsOpReqServiceImpl implements CsOpReqService {

    // ========== 站内信模板编码 ==========
    private static final String NOTIFY_OPREQ_CREATED = "cs-opreq-created";
    private static final String NOTIFY_OPREQ_ACCEPTED = "cs-opreq-accepted";
    private static final String NOTIFY_OPREQ_SUBMITTED = "cs-opreq-submitted";
    private static final String NOTIFY_OPREQ_VERIFIED = "cs-opreq-verified";

    @Resource
    private CsOpReqMapper csOpReqMapper;

    @Resource
    private CsWebSocketService csWebSocketService;

    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;

    @Resource
    private PermissionCommonApi permissionApi;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOpReq(CsOpReqCreateReqVO reqVO) {
        // 1. 校验操作类型
        if (CsOpReqTypeEnum.getByCode(reqVO.getOpType()) == null) {
            throw exception(CS_OPREQ_NOT_EXISTS);
        }

        // 2. 生成编号
        String opreqCode = generateOpreqCode();

        // 3. 构建 DO
        CsOpReqDO opReqDO = BeanUtils.toBean(reqVO, CsOpReqDO.class);
        opReqDO.setOpreqCode(opreqCode);
        opReqDO.setStatus(CsOpReqStatusEnum.PENDING.getCode());
        opReqDO.setCreatorUserId(SecurityFrameworkUtils.getLoginUserId());

        // 4. 冗余填充经销商名称和产品线名称（如果有产品线编码）
        // 注意：这里简单处理，名称由前端传入或后续通过 DealerInfoMapper 查询
        // 如果前端没传 dealerName，可从 VO 扩展字段获取；这里先保持 VO 传入的值

        // 5. 插入
        csOpReqMapper.insert(opReqDO);

        // 6. WebSocket + 站内信通知（暂无指定处理人，广播通知管理员）
        // 操作请求创建后不指定处理人，由执行员自行接单

        return opReqDO.getId();
    }

    @Override
    public CsOpReqDO getOpReq(Long id) {
        return csOpReqMapper.selectById(id);
    }

    @Override
    public PageResult<CsOpReqDO> getOpReqPage(CsOpReqPageReqVO reqVO) {
        // 按角色注入可见性过滤
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();
        reqVO.setCurrentUserId(currentUserId);
        reqVO.setViewScope(resolveViewScope(currentUserId));
        return csOpReqMapper.selectPage(reqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptOpReq(Long id) {
        CsOpReqDO opReq = validateOpReqExists(id);
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();

        // 校验状态：仅待处理可接单
        validateStatus(opReq, CsOpReqStatusEnum.PENDING);

        // 更新状态
        csOpReqMapper.updateById(new CsOpReqDO()
                .setId(id)
                .setStatus(CsOpReqStatusEnum.IN_PROGRESS.getCode())
                .setAssigneeId(currentUserId)
                .setAcceptTime(LocalDateTime.now()));

        // 推送给发起人 + 站内信
        csWebSocketService.sendTaskNotifyAsync(opReq.getCreatorUserId(),
                buildNotification(opReq, CsTaskNotification.TYPE_OPREQ_ACCEPTED, "操作请求已被接单"));
        sendNotify(opReq.getCreatorUserId(), NOTIFY_OPREQ_ACCEPTED, buildNotifyParams(opReq));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitOpReq(CsOpReqSubmitReqVO reqVO) {
        CsOpReqDO opReq = validateOpReqExists(reqVO.getId());
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();

        // 校验状态：仅处理中可提交
        validateStatus(opReq, CsOpReqStatusEnum.IN_PROGRESS);
        // 校验当前操作人是处理人
        if (!currentUserId.equals(opReq.getAssigneeId())) {
            throw exception(CS_OPREQ_NOT_ASSIGNEE);
        }

        // 更新状态
        csOpReqMapper.updateById(new CsOpReqDO()
                .setId(reqVO.getId())
                .setStatus(CsOpReqStatusEnum.DELIVERED.getCode())
                .setSubmitTime(LocalDateTime.now())
                .setSubmitRemark(reqVO.getSubmitRemark()));

        // 推送给发起人验收 + 站内信
        csWebSocketService.sendTaskNotifyAsync(opReq.getCreatorUserId(),
                buildNotification(opReq, CsTaskNotification.TYPE_OPREQ_SUBMITTED, "操作请求已提交，请验收"));
        sendNotify(opReq.getCreatorUserId(), NOTIFY_OPREQ_SUBMITTED, buildNotifyParams(opReq));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyOpReq(Long id) {
        CsOpReqDO opReq = validateOpReqExists(id);
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();

        // 校验状态：仅等待验收可验收
        validateStatus(opReq, CsOpReqStatusEnum.DELIVERED);
        // 校验当前操作人是发起人
        if (!currentUserId.equals(opReq.getCreatorUserId())) {
            throw exception(CS_TASK_NOT_CREATOR);
        }

        // 更新状态
        LocalDateTime now = LocalDateTime.now();
        csOpReqMapper.updateById(new CsOpReqDO()
                .setId(id)
                .setStatus(CsOpReqStatusEnum.CLOSED.getCode())
                .setVerifyTime(now)
                .setCompletedTime(now));

        // 推送给处理人 + 站内信
        csWebSocketService.sendTaskNotifyAsync(opReq.getAssigneeId(),
                buildNotification(opReq, CsTaskNotification.TYPE_OPREQ_VERIFIED, "操作请求已验收完成"));
        sendNotify(opReq.getAssigneeId(), NOTIFY_OPREQ_VERIFIED, buildNotifyParams(opReq));
    }

    @Override
    public CsOpReqStatisticsRespVO getStatistics() {
        return getStatistics(null);
    }

    @Override
    public CsOpReqStatisticsRespVO getStatistics(LocalDateTime startTime) {
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();
        String viewScope = resolveViewScope(currentUserId);
        Map<Integer, Long> countMap = csOpReqMapper.selectCountGroupByStatusWithScope(viewScope, currentUserId, startTime);

        int pendingCount = countMap.getOrDefault(CsOpReqStatusEnum.PENDING.getCode(), 0L).intValue();
        int inProgressCount = countMap.getOrDefault(CsOpReqStatusEnum.IN_PROGRESS.getCode(), 0L).intValue();
        int pendingVerifyCount = countMap.getOrDefault(CsOpReqStatusEnum.DELIVERED.getCode(), 0L).intValue();
        int completedCount = countMap.getOrDefault(CsOpReqStatusEnum.CLOSED.getCode(), 0L).intValue();

        CsOpReqStatisticsRespVO respVO = new CsOpReqStatisticsRespVO();
        respVO.setTotalCount(pendingCount + inProgressCount + pendingVerifyCount + completedCount);
        respVO.setPendingCount(pendingCount);
        respVO.setInProgressCount(inProgressCount);
        respVO.setPendingVerifyCount(pendingVerifyCount);
        respVO.setCompletedCount(completedCount);
        return respVO;
    }

    // ========== 辅助方法 ==========

    private CsOpReqDO validateOpReqExists(Long id) {
        CsOpReqDO opReq = csOpReqMapper.selectById(id);
        if (opReq == null) {
            throw exception(CS_OPREQ_NOT_EXISTS);
        }
        return opReq;
    }

    private void validateStatus(CsOpReqDO opReq, CsOpReqStatusEnum expected) {
        if (CsOpReqStatusEnum.CLOSED.getCode().equals(opReq.getStatus())) {
            throw exception(CS_OPREQ_ALREADY_CLOSED);
        }
        if (!expected.getCode().equals(opReq.getStatus())) {
            switch (expected) {
                case PENDING -> throw exception(CS_OPREQ_NOT_PENDING);
                case IN_PROGRESS -> throw exception(CS_OPREQ_NOT_IN_PROGRESS);
                case DELIVERED -> throw exception(CS_OPREQ_NOT_DELIVERED);
                default -> throw exception(CS_OPREQ_NOT_EXISTS);
            }
        }
    }

    /**
     * 根据当前用户角色解析可见范围
     */
    private String resolveViewScope(Long userId) {
        // 获取用户角色
        // 通过 PermissionCommonApi 判断角色

        // 检查是否是经销商角色
        if (hasRole(userId, OpsRoleCodeConstants.DEALER)) {
            return "creator";
        }
        // 检查是否是执行员角色
        if (hasRole(userId, OpsRoleCodeConstants.SERVICE_EXECUTOR)) {
            return "assignee";
        }
        // 管理员/销售员 → 看全部
        return "all";
    }

    private boolean hasRole(Long userId, String roleCode) {
        return permissionApi.hasAnyRoles(userId, roleCode);
    }

    private String generateOpreqCode() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        Integer maxSeq = csOpReqMapper.selectMaxSeqToday(dateStr);
        return String.format("OPR-%s-%03d", dateStr, maxSeq + 1);
    }

    private CsTaskNotification buildNotification(CsOpReqDO opReq, String type, String message) {
        return new CsTaskNotification()
                .setTaskId(opReq.getId())
                .setTaskNo(opReq.getOpreqCode())
                .setType(type)
                .setMessage(message)
                .setStatus(opReq.getStatus());
    }

    private void sendNotify(Long userId, String templateCode, Map<String, Object> params) {
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

    private Map<String, Object> buildNotifyParams(CsOpReqDO opReq) {
        Map<String, Object> params = new HashMap<>();
        params.put("opreqCode", opReq.getOpreqCode());
        params.put("opType", opReq.getOpType());
        return params;
    }

}
