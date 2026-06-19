package cn.iocoder.yudao.module.opshub.service.order;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.opshub.controller.admin.order.vo.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.order.OrderInfoDO;

import java.time.LocalDateTime;

/**
 * 订单 Service 接口
 */
public interface OrderInfoService {

    /**
     * 获得订单详情（含 6 Tab 数据）
     */
    OrderDetailRespVO getOrderDetail(Long id);

    /**
     * 获得订单分页
     */
    PageResult<OrderInfoDO> getOrderPage(OrderInfoPageReqVO reqVO);

    /**
     * 获得统计数据（6 大卡片）
     */
    OrderStatisticsRespVO getStatistics();

    /**
     * 获得统计数据（指定时间范围）
     */
    OrderStatisticsRespVO getStatistics(LocalDateTime startTime);

    /**
     * 更新订单进度（管理员/执行员）
     * 校验状态流转合法性 → 更新 progress_status + 时间戳 + timeline 节点
     */
    void updateProgress(Long id, String progressStatus);

    /**
     * 申请付款
     */
    void applyPayment(Long orderId, String remark);

    /**
     * 申请开票
     */
    void applyInvoice(Long orderId, String companyName, String taxNo, String specialRequest);

    /**
     * 申请退货
     */
    void applyReturn(OrderApplyReturnReqVO reqVO);

}
