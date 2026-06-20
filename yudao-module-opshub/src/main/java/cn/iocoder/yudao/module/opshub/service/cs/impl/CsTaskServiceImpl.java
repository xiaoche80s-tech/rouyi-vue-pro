package cn.iocoder.yudao.module.opshub.service.cs.impl;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskApproveReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskRejectReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskTransferReqVO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.cs.CsTaskDO;
import cn.iocoder.yudao.module.opshub.dal.mysql.cs.CsTaskMapper;
import cn.iocoder.yudao.module.opshub.enums.CsTaskStatusEnum;
import cn.iocoder.yudao.module.opshub.enums.OpsRoleCodeConstants;
import cn.iocoder.yudao.module.opshub.service.cs.CsTaskService;
import cn.iocoder.yudao.module.opshub.service.cs.event.CsTaskStatusChangeEvent;
import cn.iocoder.yudao.module.opshub.service.cs.websocket.CsWebSocketService;
import cn.iocoder.yudao.module.opshub.service.cs.websocket.dto.CsTaskNotification;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.framework.common.biz.system.permission.PermissionCommonApi;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.task.api.Task;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public static final String PROCESS_KEY = "ops-cs-task";

    // ========== 站内信模板编码 ==========
    private static final String NOTIFY_TASK_CREATED = "cs-task-created";
    private static final String NOTIFY_TASK_ACCEPTED = "cs-task-accepted";
    private static final String NOTIFY_TASK_DELIVERED = "cs-task-delivered";
    private static final String NOTIFY_TASK_VERIFIED = "cs-task-verified";
    private static final String NOTIFY_TASK_REJECTED = "cs-task-rejected";
    private static final String NOTIFY_TASK_TRANSFERRED = "cs-task-transferred";
    private static final String NOTIFY_TASK_URGING = "cs-task-urging";

    @Resource
    private CsTaskMapper csTaskMapper;

    @Resource
    private CsWebSocketService csWebSocketService;

    @Resource
    private BpmProcessInstanceApi processInstanceApi;

    @Resource
    private BpmTaskService bpmTaskService;

    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;

    @Resource
    private PermissionCommonApi permissionCommonApi;

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

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
        // csWebSocketService.sendTaskNotifyAsync(reqVO.getAssigneeId(),
        //         buildNotification(taskDO, CsTaskNotification.TYPE_TASK_CREATED, "您有新的工单待处理"));
        // sendNotify(reqVO.getAssigneeId(), NOTIFY_TASK_CREATED, buildNotifyParams(taskDO));

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
        String viewScope = resolveViewScope(currentUserId);
        reqVO.setViewScope(viewScope);
        // 解析子标签过滤
        applyTabFilter(reqVO, viewScope, currentUserId);
        return csTaskMapper.selectPage(reqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptTask(Long id) {
        CsTaskDO task = validateTaskExists(id);
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();

        // 校验状态：仅待接单可接单
        validateStatus(task, CsTaskStatusEnum.PENDING);

        // 混合接单模式校验
        if (task.getAssigneeId() != null) {
            // 指定模式：仅指定处理人可接单
            validateIsAssignee(task, currentUserId);
        }
        // 抢单模式（assigneeId=null）：任何执行员可接单

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
        log.info("transferTask reqVO={}", JSON.toJSONString(reqVO));
        CsTaskDO task = validateTaskExists(reqVO.getId());
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();

        // 校验状态：仅处理中可转单
        validateStatus(task, CsTaskStatusEnum.IN_PROGRESS);
        // 权限校验：仅超管和流程管理员可转单，执行员仅可转单自己的工单
        boolean isAdmin = permissionCommonApi.hasAnyRoles(currentUserId,
                OpsRoleCodeConstants.SUPER_ADMIN, OpsRoleCodeConstants.PROCESS_ADMIN);
        log.info("isAdmin={}", isAdmin);
        if (!isAdmin) {
            // 校验当前操作人是处理人
            validateIsAssignee(task, currentUserId);
        }
        // 校验不可转给自己
        if (task.getAssigneeId() != null && task.getAssigneeId().equals(reqVO.getNewAssigneeId())) {
            throw exception(CS_TASK_TRANSFER_SAME);
        }

        // 更新处理人
        csTaskMapper.updateById(new CsTaskDO()
                .setId(reqVO.getId())
                .setAssigneeId(reqVO.getNewAssigneeId()));

        // 同步 BPM 流程中的任务处理人
        if (task.getProcessInstanceId() != null) {
            try {
                String bpmTaskId = findCurrentBpmTaskId(task.getProcessInstanceId());
                if (bpmTaskId != null) {
                    BpmTaskTransferReqVO bpmVO = new BpmTaskTransferReqVO();
                    bpmVO.setId(bpmTaskId);
                    bpmVO.setAssigneeUserId(reqVO.getNewAssigneeId());
                    bpmVO.setReason(reqVO.getReason() != null ? reqVO.getReason() : "工单转单");
                    bpmTaskService.transferTask(task.getAssigneeId(), bpmVO);
                }
            } catch (Exception e) {
                log.warn("[transferTask][同步 BPM 转单失败 taskId={}]", reqVO.getId(), e);
            }
        }

        // 推送给新处理人 + 站内信：已由 CsTaskBpmAssignedListener 监听 TASK_ASSIGNED 事件统一处理，
        // 此处不再重复发送，避免双路径通知。
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitForApproval(Long id, String reason) {
        CsTaskDO task = validateTaskExists(id);
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();

        // 校验状态：仅处理中可提交审批
        validateStatus(task, CsTaskStatusEnum.IN_PROGRESS);
        // 校验当前操作人是处理人
        validateIsAssignee(task, currentUserId);

        // 推动 BPM 流程到审批节点（不直接改变业务状态，由 BPM 回调设置）
        approveCurrentBpmTask(task, currentUserId, reason);

        // 下一岗处理人由 CsTaskBpmAssignedListener 监听 TASK_ASSIGNED 事件自动同步

        // 记录交付时间（即使 BPM 回调尚未到达，也记录提交时间）
        csTaskMapper.updateById(new CsTaskDO()
                .setId(id)
                .setDeliverTime(LocalDateTime.now()));

        // 推送给提单人（经销商验收）+ 站内信
        csWebSocketService.sendTaskNotifyAsync(task.getCreatorUserId(),
                buildNotification(task, CsTaskNotification.TYPE_TASK_DELIVERED, "工单已提交审批，请验收"));
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

        // 记录退回原因和验收时间
        csTaskMapper.updateById(new CsTaskDO()
                .setId(reqVO.getId())
                .setRejectReason(Boolean.FALSE.equals(reqVO.getPassed()) ? reqVO.getRejectReason() : null)
                .setVerifyTime(LocalDateTime.now()));

        if (Boolean.TRUE.equals(reqVO.getPassed())) {
            // 验收通过 → 推动 BPM 验收到结束节点 → BPM 回调设 CLOSED
            approveCurrentBpmTask(task, currentUserId, null);
        } else {
            // 验收不通过 → 推动 BPM 退回 → BPM 回调设 REJECTED
            rejectCurrentBpmTask(task, currentUserId, reqVO.getRejectReason());
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

        // 推送给提单人（使用正确类型）
        csWebSocketService.sendTaskNotifyAsync(task.getCreatorUserId(),
                buildNotification(task, CsTaskNotification.TYPE_TASK_REPROCESS, "工单已重新处理"));
        sendNotify(task.getCreatorUserId(), NOTIFY_TASK_CREATED, buildNotifyParams(task));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void urgeTask(Long id) {
        CsTaskDO task = validateTaskExists(id);

        // 校验状态：已关闭不可催办
        if (CsTaskStatusEnum.CLOSED.getCode().equals(task.getStatus())) {
            throw exception(CS_TASK_ALREADY_CLOSED);
        }

        // 推送催办通知给处理人
        if (task.getAssigneeId() != null) {
            csWebSocketService.sendTaskNotifyAsync(task.getAssigneeId(),
                    buildNotification(task, CsTaskNotification.TYPE_TASK_URGING, "工单被催办，请尽快处理"));
            sendNotify(task.getAssigneeId(), NOTIFY_TASK_URGING, buildNotifyParams(task));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTask(Long id, String reason) {
        CsTaskDO task = validateTaskExists(id);
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();

        // 已关闭不可取消
        if (CsTaskStatusEnum.CLOSED.getCode().equals(task.getStatus())) {
            throw exception(CS_TASK_ALREADY_CLOSED);
        }

        // 权限校验：经销商仅 PENDING/IN_PROGRESS + 仅提单人；管理员任意非 CLOSED
        boolean isCreator = currentUserId.equals(task.getCreatorUserId());
        boolean isAdmin = permissionCommonApi.hasAnyRoles(currentUserId,
                OpsRoleCodeConstants.BRAND_ADMIN, OpsRoleCodeConstants.SUPER_ADMIN);
        if (!isAdmin) {
            // 非管理员：仅 PENDING 或 IN_PROGRESS + 仅提单人
            if (!CsTaskStatusEnum.PENDING.getCode().equals(task.getStatus())
                    && !CsTaskStatusEnum.IN_PROGRESS.getCode().equals(task.getStatus())) {
                throw exception(CS_TASK_NOT_PENDING_OR_IN_PROGRESS);
            }
            if (!isCreator) {
                throw exception(CS_TASK_NOT_CREATOR);
            }
        }

        // 取消 BPM 流程实例
        if (task.getProcessInstanceId() != null) {
            try {
                String bpmTaskId = findCurrentBpmTaskId(task.getProcessInstanceId());
                if (bpmTaskId != null) {
                    BpmTaskRejectReqVO rejectReqVO = new BpmTaskRejectReqVO();
                    rejectReqVO.setId(bpmTaskId);
                    rejectReqVO.setReason(reason != null ? reason : "工单取消");
                    bpmTaskService.rejectTask(currentUserId, rejectReqVO);
                }
            } catch (Exception e) {
                log.warn("[cancelTask][取消 BPM 流程失败 taskId={}]", id, e);
            }
        }

        // 直接设置 CLOSED（BPM 回调可能不会再触发）
        csTaskMapper.updateById(new CsTaskDO()
                .setId(id)
                .setStatus(CsTaskStatusEnum.CLOSED.getCode()));

        // 通知相关方
        csWebSocketService.sendTaskNotifyAsync(task.getCreatorUserId(),
                buildNotification(task, CsTaskNotification.TYPE_TASK_VERIFIED, "工单已关闭"));
        if (task.getAssigneeId() != null) {
            csWebSocketService.sendTaskNotifyAsync(task.getAssigneeId(),
                    buildNotification(task, CsTaskNotification.TYPE_TASK_VERIFIED, "工单已关闭"));
        }

        // 发布状态变更事件
        publishStatusChangeEvent(task, CsTaskStatusEnum.CLOSED);
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
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            Integer maxSeq = csTaskMapper.selectMaxSeqToday(dateStr);
            String taskNo = String.format("TASK-%s-%03d", dateStr, maxSeq + 1);
            // 检查是否已存在（防御唯一索引冲突）
            Long existCount = csTaskMapper.selectCount(
                    new LambdaQueryWrapper<CsTaskDO>().eq(CsTaskDO::getTaskNo, taskNo));
            if (existCount == null || existCount == 0) {
                return taskNo;
            }
            log.warn("[generateTaskNo][工单号 {} 已存在，重试 {}/{}]", taskNo, i + 1, maxRetries);
        }
        throw new IllegalStateException("无法生成唯一工单编号，请稍后重试");
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
     * 解析子标签过滤，将 tabFilter 翻译为具体的查询条件。
     * <p>
     * 执行员（assignee）和管理员（all）的 pending/done 采用 BPM 驱动：
     * 先通过 Flowable 查询 processInstanceId 集合，再反查 ops_cs_task 表。
     * 现有的搜索条件（urgency、category、dealerCode、productLineCode、keyword 等）
     * 在 selectPage 中保持不变，此处仅叠加 BPM 过滤条件。
     */
    private void applyTabFilter(CsTaskPageReqVO reqVO, String viewScope, Long currentUserId) {
        String tabFilter = reqVO.getTabFilter();
        if (tabFilter == null || "all".equals(tabFilter)) {
            return;
        }
        switch (viewScope) {
            case "creator" -> {
                if ("pending".equals(tabFilter)) {
                    // 经销商待办：已交付待验收(2) + 已退回(4)
                    reqVO.setStatusList(List.of(
                            CsTaskStatusEnum.DELIVERED.getCode(),
                            CsTaskStatusEnum.REJECTED.getCode()));
                }
            }
            case "assignee" -> {
                switch (tabFilter) {
                    case "claimable" -> {
                        // 执行员可领取：状态=待接单(0) 且未分配
                        reqVO.setStatusList(List.of(CsTaskStatusEnum.PENDING.getCode()));
                        reqVO.setUnassigned(true);
                    }
                    case "pending" -> {
                        // ===== BPM 驱动：查询 Flowable 待办 → processInstanceId =====
                        List<String> ids = bpmTaskService.getTodoProcessInstanceIds(currentUserId, PROCESS_KEY);
                        reqVO.setProcessInstanceIds(ids);
                        reqVO.setAssigneeId(currentUserId); // 防御性冗余过滤
                    }
                    case "delivered" -> {
                        // 执行员已交付：处理人是我 且 状态∈{已交付(2)}
                        reqVO.setAssigneeId(currentUserId);
                        reqVO.setStatusList(List.of(CsTaskStatusEnum.DELIVERED.getCode()));
                    }
                    case "done" -> {
                        // ===== BPM 驱动：查询 Flowable 已办 → processInstanceId =====
                        List<String> ids = bpmTaskService.getDoneProcessInstanceIds(currentUserId, PROCESS_KEY);
                        reqVO.setProcessInstanceIds(ids);
                        reqVO.setAssigneeId(currentUserId); // 防御性冗余过滤
                    }
                }
            }
            case "all" -> {
                // 管理员：查询所有流程实例（userId=null → 不限定 assignee）
                switch (tabFilter) {
                    case "pending" -> {
                        List<String> ids = bpmTaskService.getTodoProcessInstanceIds(null, PROCESS_KEY);
                        reqVO.setProcessInstanceIds(ids);
                    }
                    case "done" -> {
                        List<String> ids = bpmTaskService.getDoneProcessInstanceIds(null, PROCESS_KEY);
                        reqVO.setProcessInstanceIds(ids);
                    }
                }
            }
        }
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
    @Transactional(rollbackFor = Exception.class)
    public void updateCsTaskStatusByBpm(Long id, Integer bpmStatus) {
        CsTaskDO task = csTaskMapper.selectById(id);
        if (task == null) {
            return;
        }
        // 已关闭的工单不再更新
        if (CsTaskStatusEnum.CLOSED.getCode().equals(task.getStatus())) {
            return;
        }

        CsTaskStatusEnum newStatus = null;
        if (BpmProcessInstanceStatusEnum.APPROVE.getStatus().equals(bpmStatus)) {
            // APPROVE 根据当前业务状态推进：IN_PROGRESS→DELIVERED, DELIVERED→CLOSED
            if (CsTaskStatusEnum.IN_PROGRESS.getCode().equals(task.getStatus())) {
                newStatus = CsTaskStatusEnum.DELIVERED;
            } else if (CsTaskStatusEnum.DELIVERED.getCode().equals(task.getStatus())) {
                newStatus = CsTaskStatusEnum.CLOSED;
            }
        } else if (BpmProcessInstanceStatusEnum.REJECT.getStatus().equals(bpmStatus)) {
            newStatus = CsTaskStatusEnum.REJECTED;
        } else if (BpmProcessInstanceStatusEnum.CANCEL.getStatus().equals(bpmStatus)) {
            newStatus = CsTaskStatusEnum.CLOSED;
        }

        if (newStatus == null || newStatus.getCode().equals(task.getStatus())) {
            return;
        }

        // CAS 原子更新状态
        csTaskMapper.updateById(new CsTaskDO().setId(id).setStatus(newStatus.getCode()));
        log.info("[updateCsTaskStatusByBpm][工单 {} 状态由 BPM 回调更新为 {}]", id, newStatus.getName());

        // BPM 回调补充通知
        sendBpmCallbackNotification(task, newStatus);

        // 发布工单状态变更事件（各业务模块按需监听）
        publishStatusChangeEvent(task, newStatus);
    }

    @Override
    public Map<String, Long> getTabCounts() {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        String viewScope = resolveViewScope(loginUserId);
        Map<String, Long> counts = new java.util.HashMap<>();

        // 全部
        counts.put("all", csTaskMapper.selectCountByTab(null, viewScope, loginUserId));
        // 可领取
        counts.put("claimable", csTaskMapper.selectCountByTab("claimable", viewScope, loginUserId));
        // 已交付
        counts.put("delivered", csTaskMapper.selectCountByTab("delivered", viewScope, loginUserId));

        // 待办/已办：BPM 驱动计数
        if ("assignee".equals(viewScope)) {
            // 执行员：查询当前用户的待办/已办流程实例
            List<String> todoIds = bpmTaskService.getTodoProcessInstanceIds(loginUserId, PROCESS_KEY);
            counts.put("pending", csTaskMapper.selectCountByProcessInstanceIds(todoIds));
            List<String> doneIds = bpmTaskService.getDoneProcessInstanceIds(loginUserId, PROCESS_KEY);
            counts.put("done", csTaskMapper.selectCountByProcessInstanceIds(doneIds));
        } else if ("all".equals(viewScope)) {
            // 管理员：查询所有用户的待办/已办流程实例
            List<String> todoIds = bpmTaskService.getTodoProcessInstanceIds(null, PROCESS_KEY);
            counts.put("pending", csTaskMapper.selectCountByProcessInstanceIds(todoIds));
            List<String> doneIds = bpmTaskService.getDoneProcessInstanceIds(null, PROCESS_KEY);
            counts.put("done", csTaskMapper.selectCountByProcessInstanceIds(doneIds));
        } else {
            // 经销商（creator）：仍用状态过滤
            counts.put("pending", csTaskMapper.selectCountByTab("pending", viewScope, loginUserId));
            counts.put("done", csTaskMapper.selectCountByTab("done", viewScope, loginUserId));
        }

        return counts;
    }

    @Override
    public Set<Long> getTaskCandidateUserIds(Long csTaskId) {
        CsTaskDO task = validateTaskExists(csTaskId);
        if (task.getProcessInstanceId() == null) {
            return Collections.emptySet();
        }
        return bpmTaskService.getTaskCandidateUserIds(task.getProcessInstanceId());
    }

    @Override
    public String getBpmTaskId(Long csTaskId) {
        CsTaskDO task = validateTaskExists(csTaskId);
        // 仅 IN_PROGRESS 状态可转单
        if (!CsTaskStatusEnum.IN_PROGRESS.getCode().equals(task.getStatus())) {
            return null;
        }
        return findCurrentBpmTaskId(task.getProcessInstanceId());
    }

    // ========== BPM 集成辅助方法 ==========

    /**
     * 查找流程实例中当前运行的 BPM 任务 ID
     */
    private String findCurrentBpmTaskId(String processInstanceId) {
        if (processInstanceId == null) {
            return null;
        }
        try {
            List<Task> tasks = bpmTaskService.getTasksByProcessInstanceIds(
                    Collections.singletonList(processInstanceId));
            if (CollUtil.isEmpty(tasks)) {
                return null;
            }
            return tasks.get(0).getId();
        } catch (Exception e) {
            log.warn("[findCurrentBpmTaskId][查找 BPM 任务失败 processInstanceId={}]", processInstanceId, e);
            return null;
        }
    }

    /**
     * 审批当前 BPM 任务（推动流程前进）
     *
     * @param task   工单
     * @param userId 当前操作人
     * @param reason 审批意见（可选）
     */
    private void approveCurrentBpmTask(CsTaskDO task, Long userId, String reason) {
        String bpmTaskId = findCurrentBpmTaskId(task.getProcessInstanceId());
        if (bpmTaskId != null) {
            try {
                BpmTaskApproveReqVO approveReqVO = new BpmTaskApproveReqVO();
                approveReqVO.setId(bpmTaskId);
                approveReqVO.setReason(reason);
                bpmTaskService.approveTask(userId, approveReqVO);
            } catch (Exception e) {
                log.warn("[approveCurrentBpmTask][BPM 审批失败 taskId={}, bpmTaskId={}]", task.getId(), bpmTaskId, e);
            }
        }
    }

    /**
     * 退回当前 BPM 任务
     */
    private void rejectCurrentBpmTask(CsTaskDO task, Long userId, String reason) {
        String bpmTaskId = findCurrentBpmTaskId(task.getProcessInstanceId());
        if (bpmTaskId != null) {
            try {
                BpmTaskRejectReqVO rejectReqVO = new BpmTaskRejectReqVO();
                rejectReqVO.setId(bpmTaskId);
                rejectReqVO.setReason(reason);
                bpmTaskService.rejectTask(userId, rejectReqVO);
            } catch (Exception e) {
                log.warn("[rejectCurrentBpmTask][BPM 退回失败 taskId={}, bpmTaskId={}]", task.getId(), bpmTaskId, e);
            }
        }
    }

    /**
     * BPM 回调后发送通知
     */
    private void sendBpmCallbackNotification(CsTaskDO task, CsTaskStatusEnum newStatus) {
        switch (newStatus) {
            case DELIVERED -> {
                // 审批通过 → 通知经销商验收
                csWebSocketService.sendTaskNotifyAsync(task.getCreatorUserId(),
                        buildNotification(task, CsTaskNotification.TYPE_TASK_DELIVERED, "工单审批通过，请验收"));
                sendNotify(task.getCreatorUserId(), NOTIFY_TASK_DELIVERED, buildNotifyParams(task));
            }
            case REJECTED -> {
                // 退回 → 通知处理人
                csWebSocketService.sendTaskNotifyAsync(task.getAssigneeId(),
                        buildNotification(task, CsTaskNotification.TYPE_TASK_REJECTED, "工单被退回"));
                sendNotify(task.getAssigneeId(), NOTIFY_TASK_REJECTED, buildNotifyParams(task));
            }
            case CLOSED -> {
                // 验收通过/取消 → 通知双方
                csWebSocketService.sendTaskNotifyAsync(task.getAssigneeId(),
                        buildNotification(task, CsTaskNotification.TYPE_TASK_VERIFIED, "工单已关闭"));
                sendNotify(task.getAssigneeId(), NOTIFY_TASK_VERIFIED, buildNotifyParams(task));
                sendNotify(task.getCreatorUserId(), NOTIFY_TASK_VERIFIED, buildNotifyParams(task));
            }
            default -> { /* no-op */ }
        }
    }

    /**
     * 发布工单状态变更事件（供各业务模块按需监听）
     */
    private void publishStatusChangeEvent(CsTaskDO task, CsTaskStatusEnum newStatus) {
        applicationEventPublisher.publishEvent(new CsTaskStatusChangeEvent(this)
                .setTaskId(task.getId())
                .setTaskNo(task.getTaskNo())
                .setCategory(task.getCategory())
                .setSourceModule(task.getSourceModule())
                .setNewStatus(newStatus.getCode())
                .setDealerCode(task.getDealerCode())
                .setProductLineCode(task.getProductLineCode()));
    }

}
