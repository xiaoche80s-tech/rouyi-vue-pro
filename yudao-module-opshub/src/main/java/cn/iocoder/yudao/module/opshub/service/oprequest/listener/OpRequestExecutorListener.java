package cn.iocoder.yudao.module.opshub.service.oprequest.listener;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.opshub.dal.dataobject.oprequest.OpRequestDO;
import cn.iocoder.yudao.module.opshub.dal.mysql.oprequest.OpRequestMapper;
import cn.iocoder.yudao.module.opshub.enums.OpRequestStatusEnum;
import cn.iocoder.yudao.module.opshub.service.oprequest.impl.OpRequestServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 操作请求 — 办理人节点任务监听器
 * <p>
 * 监听办理人（执行员）用户任务节点的 assignment 和 complete 事件：
 * <ul>
 *   <li>assignment：同步 assigneeId 到 ops_op_request 表，若状态为 WAITING 则推进为 IN_PROGRESS</li>
 *   <li>complete：记录执行员完成办理的日志</li>
 * </ul>
 * <p>
 * BPMN 配置：委托表达式 ${opRequestExecutorListener}
 */
@Component("opRequestExecutorListener")
@Slf4j
public class OpRequestExecutorListener implements TaskListener {

    @Resource
    @Lazy
    private OpRequestMapper opRequestMapper;

    @Resource
    @Lazy
    private RuntimeService runtimeService;

    @Override
    public void notify(DelegateTask delegateTask) {
        String eventName = delegateTask.getEventName();

        // 1. 查询流程实例，校验 processDefinitionKey
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(delegateTask.getProcessInstanceId())
                .singleResult();
        if (processInstance == null || !OpRequestServiceImpl.PROCESS_KEY.equals(processInstance.getProcessDefinitionKey())) {
            return;
        }

        // 2. 解析 requestId（businessKey）
        String businessKey = processInstance.getBusinessKey();
        if (StrUtil.isEmpty(businessKey)) {
            log.warn("[notify][processInstanceId={} businessKey 为空，跳过]", delegateTask.getProcessInstanceId());
            return;
        }
        Long requestId;
        try {
            requestId = Long.parseLong(businessKey);
        } catch (NumberFormatException e) {
            log.warn("[notify][businessKey={} 无法解析为 requestId，跳过]", businessKey);
            return;
        }

        // 3. 根据事件类型分发
        if (TaskListener.EVENTNAME_ASSIGNMENT.equals(eventName)) {
            handleAssignment(requestId, delegateTask.getAssignee());
        } else if (TaskListener.EVENTNAME_COMPLETE.equals(eventName)) {
            handleComplete(requestId, delegateTask);
        }
    }

    /**
     * 处理 assignment 事件：同步 assigneeId + 状态 WAITING → IN_PROGRESS
     */
    private void handleAssignment(Long requestId, String assignee) {
        if (StrUtil.isEmpty(assignee)) {
            log.warn("[handleAssignment][requestId={} assignee 为空，跳过]", requestId);
            return;
        }
        final Long assigneeUserId = Long.parseLong(assignee);

        // 在事务完成后执行数据库更新，确保 BPM 数据已落盘
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int transactionStatus) {
                if (ObjectUtil.equal(transactionStatus, TransactionSynchronization.STATUS_ROLLED_BACK)) {
                    return;
                }
                try {
                    syncAssignee(requestId, assigneeUserId);
                } catch (Exception e) {
                    log.error("[handleAssignment][更新操作请求处理人失败 requestId={}, assigneeUserId={}]",
                            requestId, assigneeUserId, e);
                }
            }
        });
    }

    /**
     * 更新操作请求处理人，若仍处于 WAITING 状态则同时推进为 IN_PROGRESS
     */
    private void syncAssignee(Long requestId, Long assigneeUserId) {
        OpRequestDO current = opRequestMapper.selectById(requestId);
        if (current == null) {
            log.warn("[syncAssignee][requestId={} 操作请求不存在，跳过]", requestId);
            return;
        }

        OpRequestDO updateDO = new OpRequestDO().setId(requestId).setAssigneeId(assigneeUserId);

        // 若仍处于待处理状态，推进为处理中
        if (OpRequestStatusEnum.WAITING.getCode().equals(current.getRequestStatus())) {
            updateDO.setRequestStatus(OpRequestStatusEnum.IN_PROGRESS.getCode());
        }
        opRequestMapper.updateById(updateDO);

        log.info("[syncAssignee][操作请求 {} 处理人已同步 assigneeId={}, requestStatus={}]",
                requestId, assigneeUserId, updateDO.getRequestStatus());
    }

    /**
     * 处理 complete 事件：执行员完成办理，记录日志
     */
    private void handleComplete(Long requestId, DelegateTask delegateTask) {
        log.info("[handleComplete][操作请求 {} 执行员任务完成 bpmTaskId={}]",
                requestId, delegateTask.getId());
    }

}
