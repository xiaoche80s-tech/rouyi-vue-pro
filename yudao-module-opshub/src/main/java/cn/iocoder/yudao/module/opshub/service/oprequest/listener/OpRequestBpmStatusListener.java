package cn.iocoder.yudao.module.opshub.service.oprequest.listener;

import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import cn.iocoder.yudao.module.opshub.service.oprequest.OpRequestService;
import cn.iocoder.yudao.module.opshub.service.oprequest.impl.OpRequestServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 操作请求 BPM 状态回调监听器
 */
@Component
public class OpRequestBpmStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private OpRequestService opRequestService;

    @Override
    protected String getProcessDefinitionKey() {
        return OpRequestServiceImpl.PROCESS_KEY;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        Long requestId = Long.parseLong(event.getBusinessKey());
        opRequestService.updateOpRequestStatusByBpm(requestId, event.getStatus());
    }

}
