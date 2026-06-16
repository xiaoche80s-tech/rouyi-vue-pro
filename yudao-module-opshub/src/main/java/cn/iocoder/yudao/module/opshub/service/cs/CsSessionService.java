package cn.iocoder.yudao.module.opshub.service.cs;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.cs.CsSessionDO;

/**
 * 咨询会话 Service 接口
 */
public interface CsSessionService {

    /**
     * 创建咨询会话
     * 含去重校验：相同 consult_type + context_code + 活跃状态
     */
    Long createSession(CsSessionCreateReqVO reqVO);

    /**
     * 获取会话详情（含历史消息）
     */
    CsSessionDO getSession(Long id);

    /**
     * 获取会话分页列表（含角色可见性过滤）
     */
    PageResult<CsSessionDO> getSessionPage(CsSessionPageReqVO reqVO);

    /**
     * 执行员接单
     * 状态：待处理(0) → 处理中(1)
     */
    void acceptSession(Long id);

    /**
     * 执行员完成处理
     * 状态：处理中(1) → 已完成(2)
     */
    void completeSession(CsSessionCompleteReqVO reqVO);

    /**
     * 经销商关闭对话
     * 状态：* → 已关闭(3)
     */
    void closeSession(Long id);

    /**
     * 获取咨询统计数据
     */
    CsConsultStatisticsRespVO getStatistics();

}
