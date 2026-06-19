package cn.iocoder.yudao.module.opshub.dal.mysql.order;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.opshub.dal.dataobject.order.OrderLogisticsDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderLogisticsMapper extends BaseMapperX<OrderLogisticsDO> {

    default List<OrderLogisticsDO> selectListByOrderCode(String orderCode) {
        return selectList(new LambdaQueryWrapperX<OrderLogisticsDO>()
                .eq(OrderLogisticsDO::getOrderCode, orderCode)
                .orderByAsc(OrderLogisticsDO::getSortOrder));
    }

    default List<OrderLogisticsDO> selectListByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapperX<OrderLogisticsDO>()
                .eq(OrderLogisticsDO::getOrderId, orderId)
                .orderByAsc(OrderLogisticsDO::getSortOrder));
    }

    default OrderLogisticsDO selectByOrderCodeAndTrackingNoAndSortOrder(String orderCode, String trackingNo, Integer sortOrder) {
        return selectOne(new LambdaQueryWrapperX<OrderLogisticsDO>()
                .eq(OrderLogisticsDO::getOrderCode, orderCode)
                .eq(OrderLogisticsDO::getTrackingNo, trackingNo)
                .eq(OrderLogisticsDO::getSortOrder, sortOrder));
    }

}
