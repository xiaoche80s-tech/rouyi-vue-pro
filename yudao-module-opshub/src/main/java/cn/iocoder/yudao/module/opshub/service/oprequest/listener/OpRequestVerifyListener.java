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
 * 操作请求 — 审批节点任务监听器
 * <p>
 * 监听经销商验收用户任务节点的 assignment 和 complete 事件：
 * <ul>
 *   <li>assignment：审批节点分配给经销商时，同步 assigneeId 到 ops_op_request 表</li>
 *   <li>complete：经销商验收完成时记录日志（终态由 OpRequestBpmStatusListener 处理）</li>
 * </ul>
 * <p>
 * BPMN 配置：委托表达式 ${opRequestVerifyListener}
 */
@Component("opRequestVerifyListener")
@Slf4j
public class OpRequestVerifyListener implements TaskListener {

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
     * 处理 assignment 事件：同步 assigneeId 到操作请求表
     * <p>
     * 审批节点分配给经销商时，更新 assigneeId 以记录当前验收人。
     * 后续可扩展：发送站内信/WebSocket 通知经销商进行验收。
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
                    log.error("[handleAssignment][更新操作请求验收人失败 requestId={}, assigneeUserId={}]",
                            requestId, assigneeUserId, e);
                }
            }
        });
    }

    /**
     * 更新操作请求验收人 assigneeId
     */
    private void syncAssignee(Long requestId, Long assigneeUserId) {
        OpRequestDO current = opRequestMapper.selectById(requestId);
        if (current == null) {
            log.warn("[syncAssignee][requestId={} 操作请求不存在，跳过]", requestId);
            return;
        }

        OpRequestDO entity = new OpRequestDO().setId(requestId).setAssigneeId(assigneeUserId).setRequestStatus(OpRequestStatusEnum.DELIVERED.getCode());
        opRequestMapper.updateById(entity);
        log.info("[syncAssignee][操作请求 {} 验收人已同步 assigneeId={}]", requestId, assigneeUserId);
    }

    /**
     * 处理 complete 事件：经销商验收完成，记录日志
     * <p>
     * 终态（CLOSED/REJECTED）由 {@link OpRequestBpmStatusListener} 通过流程实例状态回调处理
     */
    private void handleComplete(Long requestId, DelegateTask delegateTask) {
        log.info("[handleComplete][操作请求 {} 经销商验收完成 bpmTaskId={}]",
                requestId, delegateTask.getId());
    }

}
