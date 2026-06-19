package cn.iocoder.yudao.module.opshub.service.cs.listener;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmnModelConstants;
import cn.iocoder.yudao.module.opshub.dal.dataobject.cs.CsTaskDO;
import cn.iocoder.yudao.module.opshub.dal.mysql.cs.CsTaskMapper;
import cn.iocoder.yudao.module.opshub.enums.CsTaskStatusEnum;
import cn.iocoder.yudao.module.opshub.service.cs.impl.CsTaskServiceImpl;
import com.google.common.collect.ImmutableSet;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

/**
 * 客服工单 BPM 任务处理人变更监听器
 * <p>
 * 监听 Flowable 引擎的 TASK_ASSIGNED 事件，当 ops-cs-task 流程的非发起人节点
 * 分配处理人时，自动更新 ops_cs_task 表的 assignee_id 字段。
 * <p>
 * 此方案彻底替代了原 syncBpmAssignee 主动拉取方式中存在的时序竞争问题：
 * TASK_ASSIGNED 事件在 BPM 引擎分配 assignee 后立即触发，时机精确，无需轮询或 sleep 重试。
 * <p>
 * 注册机制：{@link cn.iocoder.yudao.module.bpm.framework.flowable.config.BpmFlowableConfiguration}
 * 会通过 ObjectProvider&lt;FlowableEventListener&gt; 自动收集 Spring 容器中所有
 * FlowableEventListener Bean 并注册到 Flowable 引擎，无需改动 BPM 模块。
 */
@Component
@Slf4j
public class CsTaskBpmAssignedListener extends AbstractFlowableEngineEventListener {

    @Resource
    @Lazy
    private CsTaskMapper csTaskMapper;

    @Resource
    @Lazy
    private RuntimeService runtimeService;

    public CsTaskBpmAssignedListener() {
        super(ImmutableSet.of(FlowableEngineEventType.TASK_ASSIGNED));
    }

    @Override
    protected void taskAssigned(FlowableEngineEntityEvent event) {
        Task task = (Task) event.getEntity();

        // 1. 过滤：跳过发起人节点（StartUserNode），该节点 assignee 为工单创建人（经销商），不是执行人
        if (BpmnModelConstants.START_USER_NODE_ID.equals(task.getTaskDefinitionKey())) {
            return;
        }

        // 2. 过滤：跳过无处理人的节点
        if (StrUtil.isEmpty(task.getAssignee())) {
            return;
        }

        // 3. 查询流程实例，获取 processDefinitionKey 和 businessKey
        //    注意：Task 对象上只有 processDefinitionId，没有 processDefinitionKey，需要通过查询获取
        //    此调用在 BPM 引擎事务中执行，processInstance 已在当前事务中可见
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .singleResult();
        if (processInstance == null) {
            log.warn("[taskAssigned][processInstanceId={} 找不到流程实例，跳过]", task.getProcessInstanceId());
            return;
        }

        // 4. 过滤：仅处理 ops-cs-task 流程
        if (!CsTaskServiceImpl.PROCESS_KEY.equals(processInstance.getProcessDefinitionKey())) {
            return;
        }

        // 5. 解析工单 ID
        String businessKey = processInstance.getBusinessKey();
        if (StrUtil.isEmpty(businessKey)) {
            log.warn("[taskAssigned][processInstanceId={} businessKey 为空，跳过]", task.getProcessInstanceId());
            return;
        }
        Long csTaskId;
        try {
            csTaskId = Long.parseLong(businessKey);
        } catch (NumberFormatException e) {
            log.warn("[taskAssigned][businessKey={} 无法解析为 csTaskId，跳过]", businessKey);
            return;
        }

        // 捕获本地变量，供 Lambda 使用
        final Long assigneeUserId = Long.parseLong(task.getAssignee());
        final Long finalCsTaskId = csTaskId;

        // 6. 在事务完成后执行数据库更新，确保 BPM 数据已落盘
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int transactionStatus) {
                // 事务回滚则跳过
                if (ObjectUtil.equal(transactionStatus, TransactionSynchronization.STATUS_ROLLED_BACK)) {
                    return;
                }
                try {
                    syncAssignee(finalCsTaskId, assigneeUserId);
                } catch (Exception e) {
                    log.error("[taskAssigned][更新工单处理人失败 csTaskId={}, assigneeUserId={}]",
                            finalCsTaskId, assigneeUserId, e);
                }
            }
        });
    }

    /**
     * 更新工单处理人，若工单仍处于 PENDING 状态则同时转为 IN_PROGRESS
     */
    private void syncAssignee(Long csTaskId, Long assigneeUserId) {
        CsTaskDO current = csTaskMapper.selectById(csTaskId);
        if (current == null) {
            log.warn("[syncAssignee][csTaskId={} 工单不存在，跳过]", csTaskId);
            return;
        }
        CsTaskDO updateDO = new CsTaskDO().setId(csTaskId).setAssigneeId(assigneeUserId);
        // 若工单仍是待接单状态，一并推进为处理中并记录接单时间
        if (ObjectUtil.equal(current.getStatus(), CsTaskStatusEnum.PENDING.getCode())) {
            updateDO.setStatus(CsTaskStatusEnum.IN_PROGRESS.getCode())
                    .setAcceptTime(LocalDateTime.now());
        }
        csTaskMapper.updateById(updateDO);
        log.info("[syncAssignee][工单 {} 处理人已同步 assigneeId={}, status={}]",
                csTaskId, assigneeUserId, updateDO.getStatus());
    }

}
