package cn.iocoder.yudao.module.opshub.service.cs;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.CsMessagePageReqVO;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.CsMessageSendReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.cs.CsMessageDO;
import cn.iocoder.yudao.module.opshub.service.cs.dto.CsMessageSaveCmd;

import java.util.List;

/**
 * 咨询消息 Service 接口
 */
public interface CsMessageService {

    /**
     * 发送消息
     * 每条消息全量持久化到数据库
     */
    CsMessageDO sendMessage(CsMessageSendReqVO reqVO);

    /**
     * 仅落库系统消息，不触发 WebSocket 推送
     * 仅供会话生命周期方法（createSession / acceptSession / completeSession / closeSession）内部调用
     */
    CsMessageDO saveSystemMessage(CsMessageSaveCmd cmd);

    /**
     * 获取某会话全部历史消息（正序）
     */
    List<CsMessageDO> getMessageList(Long sessionId);

    /**
     * 获取消息分页列表（正序）
     */
    PageResult<CsMessageDO> getMessagePage(CsMessagePageReqVO reqVO);

    /**
     * 标记已读：将指定会话中对方发送的未读消息标记为已读
     */
    void markRead(Long sessionId);

}
