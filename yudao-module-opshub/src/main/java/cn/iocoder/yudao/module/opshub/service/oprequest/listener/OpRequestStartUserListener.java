package cn.iocoder.yudao.module.opshub.service.oprequest.listener;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.opshub.service.oprequest.impl.OpRequestServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 操作请求 — 发起人节点任务监听器
 * <p>
 * 监听发起人（经销商）用户任务节点的 complete 事件，
 * 记录发起完成日志，用于流程追踪。
 * <p>
 * BPMN 配置：委托表达式 ${opRequestStartUserListener}
 */
@Component("opRequestStartUserListener")
@Slf4j
public class OpRequestStartUserListener implements TaskListener {

    @Resource
    @Lazy
    private RuntimeService runtimeService;

    @Override
    public void notify(DelegateTask delegateTask) {
        // 仅处理 complete 事件
        if (!TaskListener.EVENTNAME_COMPLETE.equals(delegateTask.getEventName())) {
            return;
        }

        // 1. 查询流程实例，校验 processDefinitionKey
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(delegateTask.getProcessInstanceId())
                .singleResult();
        if (processInstance == null || !OpRequestServiceImpl.PROCESS_KEY.equals(processInstance.getProcessDefinitionKey())) {
            return;
        }

        // 2. 解析 requestId
        String businessKey = processInstance.getBusinessKey();
        if (StrUtil.isEmpty(businessKey)) {
            return;
        }

        log.info("[notify][发起人节点完成 requestId={}, startUserId={}]",
                businessKey, processInstance.getStartUserId());
    }

}
