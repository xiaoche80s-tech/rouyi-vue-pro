package cn.iocoder.yudao.module.opshub.service.oprequest.handler;

import cn.iocoder.yudao.module.opshub.controller.admin.oprequest.vo.OpRequestCreateReqVO;
import cn.iocoder.yudao.module.opshub.controller.admin.oprequest.vo.OpRequestRespVO;
import cn.iocoder.yudao.module.opshub.controller.admin.oprequest.vo.OpRequestSubmitResultReqVO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.oprequest.OpRequestDO;

/**
 * 操作请求类型处理器策略接口
 * <p>
 * 每种 requestType（signing/payment/invoice/return）实现此接口，
 * 由 OpRequestServiceImpl 根据 requestType 路由到对应处理器。
 */
public interface OpRequestTypeHandler {

    /**
     * 支持的请求类型
     */
    String getRequestType();

    /**
     * 创建时写入子表
     */
    void onCreate(OpRequestDO request, OpRequestCreateReqVO vo);

    /**
     * 发起后处理（如更新合同 subStatus）
     */
    void onStart(OpRequestDO request);

    /**
     * 执行员提交处理结果时更新子表
     */
    void onSubmitResult(OpRequestDO request, OpRequestSubmitResultReqVO vo);

    /**
     * 流程终态 APPROVE 后处理（如合同→SIGNED）
     */
    void onClosed(OpRequestDO request);

    /**
     * 获取详情时填充子表数据到 RespVO
     */
    void fillDetail(OpRequestDO request, OpRequestRespVO respVO);

}
