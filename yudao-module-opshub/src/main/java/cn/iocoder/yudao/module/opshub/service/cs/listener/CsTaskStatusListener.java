package cn.iocoder.yudao.module.opshub.service.cs.listener;

import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import cn.iocoder.yudao.module.opshub.service.cs.CsTaskService;
import cn.iocoder.yudao.module.opshub.service.cs.impl.CsTaskServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 客服工单 BPM 流程状态监听器
 * <p>
 * 监听 processDefinitionKey = "cs_task" 的流程实例状态变更事件，
 * 回调更新工单业务状态。
 */
@Component
@Slf4j
public class CsTaskStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private CsTaskService csTaskService;

    @Override
    protected String getProcessDefinitionKey() {
        return CsTaskServiceImpl.PROCESS_KEY;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        Long taskId = Long.parseLong(event.getBusinessKey());
        log.info("[onEvent][客服工单 {} 收到 BPM 流程状态回调: status={}]", taskId, event.getStatus());
        csTaskService.updateCsTaskStatusByBpm(taskId, event.getStatus());
    }

}
