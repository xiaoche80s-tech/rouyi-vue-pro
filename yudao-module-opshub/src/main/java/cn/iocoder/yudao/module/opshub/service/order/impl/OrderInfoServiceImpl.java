package cn.iocoder.yudao.module.opshub.service.order.impl;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.opshub.controller.admin.order.vo.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.order.*;
import cn.iocoder.yudao.module.opshub.dal.mysql.order.*;
import cn.iocoder.yudao.module.opshub.enums.*;
import cn.iocoder.yudao.module.opshub.service.order.OrderInfoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.opshub.enums.ErrorCodeConstants.*;

/**
 * 订单 Service 实现类
 */
@Service
@Validated
public class OrderInfoServiceImpl implements OrderInfoService {

    /** 进度状态合法流转映射 */
    private static final Map<String, String> PROGRESS_TRANSITIONS = new LinkedHashMap<>();
    static {
        PROGRESS_TRANSITIONS.put("pending", "confirmed");
        PROGRESS_TRANSITIONS.put("confirmed", "shipped");
        PROGRESS_TRANSITIONS.put("shipped", "signed");
        PROGRESS_TRANSITIONS.put("signed", "completed");
    }

    @Resource
    private OrderInfoMapper orderInfoMapper;
    @Resource
    private OrderProductMapper orderProductMapper;
    @Resource
    private OrderTimelineMapper orderTimelineMapper;
    @Resource
    private OrderPaymentMapper orderPaymentMapper;
    @Resource
    private OrderInvoiceMapper orderInvoiceMapper;
    @Resource
    private OrderLogisticsMapper orderLogisticsMapper;

    @Override
    public OrderDetailRespVO getOrderDetail(Long id) {
        // 1. 查询主表
        OrderInfoDO order = orderInfoMapper.selectById(id);
        if (order == null) {
            throw exception(ORDER_NOT_EXISTS);
        }

        // 2. 组装详情
        OrderDetailRespVO resp = new OrderDetailRespVO();
        resp.setId(order.getId());
        resp.setOrderCode(order.getOrderCode());
        resp.setDealerId(order.getDealerId());
        resp.setDealerName(order.getDealerName());
        resp.setProductLineName(order.getProductLineName());
        resp.setTotalAmount(order.getTotalAmount());
        resp.setOrderDate(order.getOrderDate());
        resp.setProgressStatus(order.getProgressStatus());
        resp.setPayStatus(order.getPayStatus());
        resp.setInvStatus(order.getInvStatus());
        resp.setPaidAmount(order.getPaidAmount());
        resp.setInvoicedAmount(order.getInvoicedAmount());
        resp.setRemark(order.getRemark());

        // 3. 子表数据
        resp.setTimeline(orderTimelineMapper.selectListByOrderId(id));
        resp.setProducts(orderProductMapper.selectListByOrderId(id));
        resp.setPayments(orderPaymentMapper.selectListByOrderId(id));
        resp.setInvoices(orderInvoiceMapper.selectListByOrderId(id));
        resp.setLogistics(orderLogisticsMapper.selectListByOrderId(id));

        // 4. 可退货商品
        List<OrderDetailRespVO.ReturnableProduct> returnableProducts = resp.getProducts().stream()
                .filter(p -> p.getReturnableQty() != null && p.getReturnableQty() > 0)
                .map(p -> {
                    OrderDetailRespVO.ReturnableProduct rp = new OrderDetailRespVO.ReturnableProduct();
                    rp.setProductId(p.getId());
                    rp.setProductName(p.getProductName());
                    rp.setSpecModel(p.getSpecModel());
                    rp.setQuantity(p.getQuantity());
                    rp.setReturnableQty(p.getReturnableQty());
                    rp.setUnitPrice(p.getUnitPrice());
                    rp.setAmount(p.getAmount());
                    return rp;
                }).collect(Collectors.toList());
        resp.setReturnableProducts(returnableProducts);

        return resp;
    }

    @Override
    public PageResult<OrderInfoDO> getOrderPage(OrderInfoPageReqVO reqVO) {
        return orderInfoMapper.selectPage(reqVO);
    }

    @Override
    public OrderStatisticsRespVO getStatistics() {
        return getStatistics(null);
    }

    @Override
    public OrderStatisticsRespVO getStatistics(LocalDateTime startTime) {
        LambdaQueryWrapperX<OrderInfoDO> wrapper = new LambdaQueryWrapperX<>();
        if (startTime != null) {
            wrapper.ge(OrderInfoDO::getCreateTime, startTime);
        }
        List<OrderInfoDO> allOrders = orderInfoMapper.selectList(wrapper);

        OrderStatisticsRespVO resp = new OrderStatisticsRespVO();
        resp.setTotalCount(allOrders.size());
        resp.setCompletedCount((int) allOrders.stream()
                .filter(o -> "completed".equals(o.getProgressStatus())).count());
        resp.setPaidCount((int) allOrders.stream()
                .filter(o -> "paid".equals(o.getPayStatus())).count());
        resp.setUnpaidCount((int) allOrders.stream()
                .filter(o -> "unpaid".equals(o.getPayStatus())).count());
        resp.setInvoicedCount((int) allOrders.stream()
                .filter(o -> "invoiced".equals(o.getInvStatus())).count());
        resp.setUninvoicedCount((int) allOrders.stream()
                .filter(o -> "uninvoiced".equals(o.getInvStatus())).count());
        resp.setTotalAmount(allOrders.stream()
                .map(OrderInfoDO::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProgress(Long id, String progressStatus) {
        // 1. 校验存在
        OrderInfoDO order = orderInfoMapper.selectById(id);
        if (order == null) {
            throw exception(ORDER_NOT_EXISTS);
        }

        // 2. 校验状态流转合法性
        String currentStatus = order.getProgressStatus();
        String expectedNext = PROGRESS_TRANSITIONS.get(currentStatus);
        if (expectedNext == null || !expectedNext.equals(progressStatus)) {
            throw exception(ORDER_NOT_EXISTS); // TODO: 后续补充更精确的错误码
        }

        // 3. 更新主表
        OrderInfoDO updateDO = new OrderInfoDO();
        updateDO.setId(id);
        updateDO.setProgressStatus(progressStatus);
        LocalDateTime now = LocalDateTime.now();
        switch (progressStatus) {
            case "confirmed":
                updateDO.setConfirmedTime(now);
                break;
            case "shipped":
                updateDO.setShippedTime(now);
                break;
            case "signed":
                updateDO.setSignedTime(now);
                break;
            case "completed":
                updateDO.setCompletedTime(now);
                break;
        }
        orderInfoMapper.updateById(updateDO);

        // 4. 更新时间线节点
        List<OrderTimelineDO> timelines = orderTimelineMapper.selectListByOrderId(id);
        for (OrderTimelineDO timeline : timelines) {
            if (timeline.getNodeCode().equals(progressStatus)) {
                OrderTimelineDO updateTimeline = new OrderTimelineDO();
                updateTimeline.setId(timeline.getId());
                updateTimeline.setIsCompleted(true);
                updateTimeline.setNodeTime(now);
                orderTimelineMapper.updateById(updateTimeline);
                break;
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyPayment(Long orderId, String remark) {
        // 1. 校验订单存在
        OrderInfoDO order = orderInfoMapper.selectById(orderId);
        if (order == null) {
            throw exception(ORDER_NOT_EXISTS);
        }
        // 2. 校验付款状态
        if ("paid".equals(order.getPayStatus())) {
            throw exception(ORDER_ALREADY_PAID);
        }
        // 3. 校验无 pending 付款记录
        List<OrderPaymentDO> pendingPayments = orderPaymentMapper.selectPendingByOrderId(orderId);
        if (CollUtil.isNotEmpty(pendingPayments)) {
            throw exception(ORDER_PAYMENT_PENDING);
        }
        // 4. 插入付款记录
        OrderPaymentDO payment = new OrderPaymentDO();
        payment.setOrderId(orderId);
        payment.setOrderCode(order.getOrderCode());
        payment.setPayAmount(order.getTotalAmount().subtract(
                order.getPaidAmount() != null ? order.getPaidAmount() : BigDecimal.ZERO));
        payment.setStatus(OrderPaymentStatusEnum.PENDING.getCode());
        payment.setApplyTime(LocalDateTime.now());
        payment.setRemark(remark);
        orderPaymentMapper.insert(payment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyInvoice(Long orderId, String companyName, String taxNo, String specialRequest) {
        // 1. 校验订单存在
        OrderInfoDO order = orderInfoMapper.selectById(orderId);
        if (order == null) {
            throw exception(ORDER_NOT_EXISTS);
        }
        // 2. 校验开票状态
        if ("invoiced".equals(order.getInvStatus())) {
            throw exception(ORDER_ALREADY_INVOICED);
        }
        // 3. 校验无 pending 开票记录
        List<OrderInvoiceDO> pendingInvoices = orderInvoiceMapper.selectPendingByOrderId(orderId);
        if (CollUtil.isNotEmpty(pendingInvoices)) {
            throw exception(ORDER_INVOICE_PENDING);
        }
        // 4. 插入开票记录
        OrderInvoiceDO invoice = new OrderInvoiceDO();
        invoice.setOrderId(orderId);
        invoice.setOrderCode(order.getOrderCode());
        invoice.setInvoiceAmount(order.getTotalAmount().subtract(
                order.getInvoicedAmount() != null ? order.getInvoicedAmount() : BigDecimal.ZERO));
        invoice.setCompanyName(companyName);
        invoice.setTaxNo(taxNo);
        invoice.setSpecialRequest(specialRequest);
        invoice.setStatus(OrderInvoiceRecordStatusEnum.PENDING.getCode());
        invoice.setApplyTime(LocalDateTime.now());
        orderInvoiceMapper.insert(invoice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyReturn(OrderApplyReturnReqVO reqVO) {
        // 1. 校验订单存在
        OrderInfoDO order = orderInfoMapper.selectById(reqVO.getOrderId());
        if (order == null) {
            throw exception(ORDER_NOT_EXISTS);
        }
        // 2. 校验进度状态（仅 signed / completed 可退货）
        String progress = order.getProgressStatus();
        if (!"signed".equals(progress) && !"completed".equals(progress)) {
            throw exception(ORDER_NOT_SIGNED);
        }
        // 3. 逐条校验并扣减 returnable_qty
        for (OrderApplyReturnReqVO.ReturnItem item : reqVO.getItems()) {
            OrderProductDO product = orderProductMapper.selectById(item.getProductId());
            if (product == null) {
                throw exception(ORDER_PRODUCT_NOT_EXISTS);
            }
            if (product.getReturnableQty() == null || product.getReturnableQty() < item.getReturnQty()) {
                throw exception(ORDER_RETURN_QTY_EXCEED);
            }
            // 扣减可退货数量
            OrderProductDO updateProduct = new OrderProductDO();
            updateProduct.setId(product.getId());
            updateProduct.setReturnableQty(product.getReturnableQty() - item.getReturnQty());
            orderProductMapper.updateById(updateProduct);
        }
    }

}
