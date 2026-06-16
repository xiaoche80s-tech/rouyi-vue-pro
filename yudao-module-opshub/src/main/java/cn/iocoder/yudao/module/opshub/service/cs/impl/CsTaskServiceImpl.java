package cn.iocoder.yudao.module.opshub.service.cs.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.cs.CsTaskDO;
import cn.iocoder.yudao.module.opshub.dal.mysql.cs.CsTaskMapper;
import cn.iocoder.yudao.module.opshub.enums.CsTaskStatusEnum;
import cn.iocoder.yudao.module.opshub.enums.OpsRoleCodeConstants;
import cn.iocoder.yudao.module.opshub.service.cs.CsTaskService;
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
 * 客服工单 Service 实现类
 */
@Service
@Validated
@Slf4j
public class CsTaskServiceImpl implements CsTaskService {

    /**
     * BPM 流程定义 Key
     */
    public static final String PROCESS_KEY = "cs_task";

    // ========== 站内信模板编码 ==========
    private static final String NOTIFY_TASK_CREATED = "cs-task-created";
    private static final String NOTIFY_TASK_ACCEPTED = "cs-task-accepted";
    private static final String NOTIFY_TASK_DELIVERED = "cs-task-delivered";
    private static final String NOTIFY_TASK_VERIFIED = "cs-task-verified";
    private static final String NOTIFY_TASK_REJECTED = "cs-task-rejected";
    private static final String NOTIFY_TASK_TRANSFERRED = "cs-task-transferred";

    @Resource
    private CsTaskMapper csTaskMapper;

    @Resource
    private CsWebSocketService csWebSocketService;

    @Resource
    private BpmProcessInstanceApi processInstanceApi;

    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;

    @Resource
    private PermissionCommonApi permissionCommonApi;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCsTask(CsTaskCreateReqVO reqVO) {
        // 1. 生成工单编号
        String taskNo = generateTaskNo();

        // 2. 构建 DO
        CsTaskDO taskDO = BeanUtils.toBean(reqVO, CsTaskDO.class);
        taskDO.setTaskNo(taskNo);
        taskDO.setStatus(CsTaskStatusEnum.PENDING.getCode());
        taskDO.setCreatorUserId(SecurityFrameworkUtils.getLoginUserId());

        // 3. 插入
        csTaskMapper.insert(taskDO);

        // 4. 发起 BPM 流程
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        Map<String, Object> variables = new HashMap<>();
        variables.put("assigneeId", taskDO.getAssigneeId());
        variables.put("urgency", taskDO.getUrgency());
        variables.put("category", taskDO.getCategory());
        variables.put("dealerCode", taskDO.getDealerCode());
        String processInstanceId = processInstanceApi.createProcessInstance(loginUserId,
                new BpmProcessInstanceCreateReqDTO()
                        .setProcessDefinitionKey(PROCESS_KEY)
                        .setBusinessKey(String.valueOf(taskDO.getId()))
                        .setVariables(variables));
        // 回写 processInstanceId
        csTaskMapper.updateById(new CsTaskDO().setId(taskDO.getId()).setProcessInstanceId(processInstanceId));

        // 5. WebSocket 推送 + 站内信通知处理人
        csWebSocketService.sendTaskNotifyAsync(reqVO.getAssigneeId(),
                buildNotification(taskDO, CsTaskNotification.TYPE_TASK_CREATED, "您有新的工单待处理"));
        sendNotify(reqVO.getAssigneeId(), NOTIFY_TASK_CREATED, buildNotifyParams(taskDO));

        return taskDO.getId();
    }

    @Override
    public CsTaskDO getCsTask(Long id) {
        return csTaskMapper.selectById(id);
    }

    @Override
    public PageResult<CsTaskDO> getCsTaskPage(CsTaskPageReqVO reqVO) {
        // 按角色注入可见性过滤
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();
        reqVO.setCurrentUserId(currentUserId);
        reqVO.setViewScope(resolveViewScope(currentUserId));
        return csTaskMapper.selectPage(reqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptTask(Long id) {
        CsTaskDO task = validateTaskExists(id);
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();

        // 校验状态：仅待接单可接单
        validateStatus(task, CsTaskStatusEnum.PENDING);

        // 更新状态
        csTaskMapper.updateById(new CsTaskDO()
                .setId(id)
                .setStatus(CsTaskStatusEnum.IN_PROGRESS.getCode())
                .setAssigneeId(currentUserId)
                .setAcceptTime(LocalDateTime.now()));

        // 推送给提单人 + 站内信
        csWebSocketService.sendTaskNotifyAsync(task.getCreatorUserId(),
                buildNotification(task, CsTaskNotification.TYPE_TASK_ACCEPTED, "工单已被接单"));
        sendNotify(task.getCreatorUserId(), NOTIFY_TASK_ACCEPTED, buildNotifyParams(task));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferTask(CsTaskTransferReqVO reqVO) {
        CsTaskDO task = validateTaskExists(reqVO.getId());
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();

        // 校验状态：仅处理中可转单
        validateStatus(task, CsTaskStatusEnum.IN_PROGRESS);
        // 校验当前操作人是处理人
        validateIsAssignee(task, currentUserId);
        // 校验不可转给自己
        if (currentUserId.equals(reqVO.getNewAssigneeId())) {
            throw exception(CS_TASK_TRANSFER_SAME);
        }

        // 更新处理人
        csTaskMapper.updateById(new CsTaskDO()
                .setId(reqVO.getId())
                .setAssigneeId(reqVO.getNewAssigneeId()));

        // 推送给新处理人 + 站内信
        csWebSocketService.sendTaskNotifyAsync(reqVO.getNewAssigneeId(),
                buildNotification(task, CsTaskNotification.TYPE_TASK_TRANSFERRED, "有新的工单转交给您"));
        sendNotify(reqVO.getNewAssigneeId(), NOTIFY_TASK_TRANSFERRED, buildNotifyParams(task));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deliverTask(Long id) {
        CsTaskDO task = validateTaskExists(id);
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();

        // 校验状态：仅处理中可交付
        validateStatus(task, CsTaskStatusEnum.IN_PROGRESS);
        // 校验当前操作人是处理人
        validateIsAssignee(task, currentUserId);

        // 更新状态
        csTaskMapper.updateById(new CsTaskDO()
                .setId(id)
                .setStatus(CsTaskStatusEnum.DELIVERED.getCode())
                .setDeliverTime(LocalDateTime.now()));

        // 推送给提单人（经销商验收）+ 站内信
        csWebSocketService.sendTaskNotifyAsync(task.getCreatorUserId(),
                buildNotification(task, CsTaskNotification.TYPE_TASK_DELIVERED, "工单已交付，请验收"));
        sendNotify(task.getCreatorUserId(), NOTIFY_TASK_DELIVERED, buildNotifyParams(task));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyTask(CsTaskVerifyReqVO reqVO) {
        CsTaskDO task = validateTaskExists(reqVO.getId());
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();

        // 校验状态：仅已交付可验收
        validateStatus(task, CsTaskStatusEnum.DELIVERED);
        // 校验当前操作人是提单人
        validateIsCreator(task, currentUserId);

        if (Boolean.TRUE.equals(reqVO.getPassed())) {
            // 验收通过 → 已关闭
            csTaskMapper.updateById(new CsTaskDO()
                    .setId(reqVO.getId())
                    .setStatus(CsTaskStatusEnum.CLOSED.getCode())
                    .setVerifyTime(LocalDateTime.now()));

            csWebSocketService.sendTaskNotifyAsync(task.getAssigneeId(),
                    buildNotification(task, CsTaskNotification.TYPE_TASK_VERIFIED, "工单验收通过"));
            sendNotify(task.getAssigneeId(), NOTIFY_TASK_VERIFIED, buildNotifyParams(task));
        } else {
            // 验收不通过 → 已退回
            csTaskMapper.updateById(new CsTaskDO()
                    .setId(reqVO.getId())
                    .setStatus(CsTaskStatusEnum.REJECTED.getCode())
                    .setRejectReason(reqVO.getRejectReason())
                    .setVerifyTime(LocalDateTime.now()));

            csWebSocketService.sendTaskNotifyAsync(task.getAssigneeId(),
                    buildNotification(task, CsTaskNotification.TYPE_TASK_REJECTED, "工单验收不通过，已退回"));
            sendNotify(task.getAssigneeId(), NOTIFY_TASK_REJECTED, buildNotifyParams(task));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reprocessTask(Long id) {
        CsTaskDO task = validateTaskExists(id);
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();

        // 校验状态：仅已退回可重新处理
        validateStatus(task, CsTaskStatusEnum.REJECTED);
        // 校验当前操作人是处理人
        validateIsAssignee(task, currentUserId);

        // 更新状态：退回 → 处理中，清空退回原因
        csTaskMapper.updateById(new CsTaskDO()
                .setId(id)
                .setStatus(CsTaskStatusEnum.IN_PROGRESS.getCode())
                .setRejectReason(null));

        // 推送给提单人
        csWebSocketService.sendTaskNotifyAsync(task.getCreatorUserId(),
                buildNotification(task, CsTaskNotification.TYPE_TASK_ACCEPTED, "工单已重新处理"));
    }

    @Override
    public void urgeTask(Long id) {
        CsTaskDO task = validateTaskExists(id);

        // 校验状态：已关闭不可催办
        if (CsTaskStatusEnum.CLOSED.getCode().equals(task.getStatus())) {
            throw exception(CS_TASK_ALREADY_CLOSED);
        }

        // 推送催办通知给处理人
        csWebSocketService.sendTaskNotifyAsync(task.getAssigneeId(),
                buildNotification(task, CsTaskNotification.TYPE_TASK_URGING, "工单被催办，请尽快处理"));
    }

    // ========== 辅助方法 ==========

    private CsTaskDO validateTaskExists(Long id) {
        CsTaskDO task = csTaskMapper.selectById(id);
        if (task == null) {
            throw exception(CS_TASK_NOT_EXISTS);
        }
        return task;
    }

    private void validateStatus(CsTaskDO task, CsTaskStatusEnum expected) {
        if (CsTaskStatusEnum.CLOSED.getCode().equals(task.getStatus())) {
            throw exception(CS_TASK_ALREADY_CLOSED);
        }
        if (!expected.getCode().equals(task.getStatus())) {
            switch (expected) {
                case PENDING -> throw exception(CS_TASK_NOT_PENDING);
                case IN_PROGRESS -> throw exception(CS_TASK_NOT_IN_PROGRESS);
                case DELIVERED -> throw exception(CS_TASK_NOT_DELIVERED);
                default -> throw exception(CS_TASK_NOT_EXISTS);
            }
        }
    }

    private void validateIsAssignee(CsTaskDO task, Long currentUserId) {
        if (!task.getAssigneeId().equals(currentUserId)) {
            throw exception(CS_TASK_NOT_ASSIGNEE);
        }
    }

    private void validateIsCreator(CsTaskDO task, Long currentUserId) {
        if (!task.getCreatorUserId().equals(currentUserId)) {
            throw exception(CS_TASK_NOT_CREATOR);
        }
    }

    private String generateTaskNo() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        Integer maxSeq = csTaskMapper.selectMaxSeqToday(dateStr);
        return String.format("TASK-%s-%03d", dateStr, maxSeq + 1);
    }

    private CsTaskNotification buildNotification(CsTaskDO task, String type, String message) {
        return new CsTaskNotification()
                .setTaskId(task.getId())
                .setTaskNo(task.getTaskNo())
                .setType(type)
                .setMessage(message)
                .setStatus(task.getStatus())
                .setUrgency(task.getUrgency());
    }

    /**
     * 发送站内信通知
     */
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

    /**
     * 构建站内信模板参数
     */
    private Map<String, Object> buildNotifyParams(CsTaskDO task) {
        Map<String, Object> params = new HashMap<>();
        params.put("taskNo", task.getTaskNo());
        return params;
    }

    /**
     * 根据当前用户角色解析可见范围
     */
    private String resolveViewScope(Long userId) {
        // 检查是否是经销商角色
        if (permissionCommonApi.hasAnyRoles(userId, OpsRoleCodeConstants.DEALER)) {
            return "creator";
        }
        // 检查是否是执行员角色
        if (permissionCommonApi.hasAnyRoles(userId, OpsRoleCodeConstants.SERVICE_EXECUTOR)) {
            return "assignee";
        }
        // 管理员/销售员 → 看全部
        return "all";
    }

    @Override
    public void updateCsTaskStatusByBpm(Long id, Integer bpmStatus) {
        CsTaskDO task = validateTaskExists(id);
        // 已关闭的工单不再更新
        if (CsTaskStatusEnum.CLOSED.getCode().equals(task.getStatus())) {
            return;
        }

        CsTaskStatusEnum newStatus;
        if (BpmProcessInstanceStatusEnum.APPROVE.getStatus().equals(bpmStatus)) {
            newStatus = CsTaskStatusEnum.DELIVERED;
        } else if (BpmProcessInstanceStatusEnum.REJECT.getStatus().equals(bpmStatus)) {
            newStatus = CsTaskStatusEnum.REJECTED;
        } else if (BpmProcessInstanceStatusEnum.CANCEL.getStatus().equals(bpmStatus)) {
            newStatus = CsTaskStatusEnum.CLOSED;
        } else {
            // RUNNING 等状态不干预业务
            return;
        }

        // 状态相同则跳过
        if (newStatus.getCode().equals(task.getStatus())) {
            return;
        }

        csTaskMapper.updateById(new CsTaskDO().setId(id).setStatus(newStatus.getCode()));
        log.info("[updateCsTaskStatusByBpm][工单 {} 状态由 BPM 回调更新为 {}]", id, newStatus.getName());
    }

}
