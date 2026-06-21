package cn.iocoder.yudao.module.opshub.service.oprequest;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.opshub.controller.admin.oprequest.vo.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.oprequest.OpRequestDO;

/**
 * 操作请求 Service 接口
 */
public interface OpRequestService {

    /**
     * 创建操作请求
     */
    Long createOpRequest(OpRequestCreateReqVO vo);

    /**
     * 操作请求分页查询
     */
    PageResult<OpRequestDO> getOpRequestPage(OpRequestPageReqVO reqVO);

    /**
     * 操作请求详情
     */
    OpRequestRespVO getOpRequest(Long id);

    /**
     * 执行员提交处理结果
     */
    void submitResult(OpRequestSubmitResultReqVO vo);

    /**
     * 经销商验收
     */
    void verifyRequest(OpRequestVerifyReqVO vo);

    /**
     * BPM 回调更新状态（仅处理终态）
     */
    void updateOpRequestStatusByBpm(Long id, Integer bpmStatus);

    /**
     * 根据合同ID查找进行中的操作请求（签约类型）
     */
    OpRequestDO findActiveByContractId(Long contractId);

}
