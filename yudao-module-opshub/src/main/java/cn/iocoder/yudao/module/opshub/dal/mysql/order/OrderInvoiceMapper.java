package cn.iocoder.yudao.module.opshub.dal.mysql.order;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.opshub.dal.dataobject.order.OrderInvoiceDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderInvoiceMapper extends BaseMapperX<OrderInvoiceDO> {

    default List<OrderInvoiceDO> selectListByOrderCode(String orderCode) {
        return selectList(new LambdaQueryWrapperX<OrderInvoiceDO>()
                .eq(OrderInvoiceDO::getOrderCode, orderCode)
                .orderByDesc(OrderInvoiceDO::getId));
    }

    default List<OrderInvoiceDO> selectListByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapperX<OrderInvoiceDO>()
                .eq(OrderInvoiceDO::getOrderId, orderId)
                .orderByDesc(OrderInvoiceDO::getId));
    }

    default List<OrderInvoiceDO> selectPendingByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapperX<OrderInvoiceDO>()
                .eq(OrderInvoiceDO::getOrderId, orderId)
                .eq(OrderInvoiceDO::getStatus, "pending"));
    }

}
