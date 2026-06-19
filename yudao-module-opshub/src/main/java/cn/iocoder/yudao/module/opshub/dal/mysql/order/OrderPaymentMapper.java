package cn.iocoder.yudao.module.opshub.dal.mysql.order;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.opshub.dal.dataobject.order.OrderPaymentDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderPaymentMapper extends BaseMapperX<OrderPaymentDO> {

    default List<OrderPaymentDO> selectListByOrderCode(String orderCode) {
        return selectList(new LambdaQueryWrapperX<OrderPaymentDO>()
                .eq(OrderPaymentDO::getOrderCode, orderCode)
                .orderByDesc(OrderPaymentDO::getId));
    }

    default List<OrderPaymentDO> selectListByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapperX<OrderPaymentDO>()
                .eq(OrderPaymentDO::getOrderId, orderId)
                .orderByDesc(OrderPaymentDO::getId));
    }

    default List<OrderPaymentDO> selectPendingByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapperX<OrderPaymentDO>()
                .eq(OrderPaymentDO::getOrderId, orderId)
                .eq(OrderPaymentDO::getStatus, "pending"));
    }

    default OrderPaymentDO selectByOrderCodeAndVoucherNo(String orderCode, String voucherNo) {
        return selectOne(OrderPaymentDO::getOrderCode, orderCode,
                OrderPaymentDO::getVoucherNo, voucherNo);
    }

}
