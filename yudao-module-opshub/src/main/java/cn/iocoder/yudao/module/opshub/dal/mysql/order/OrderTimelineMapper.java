package cn.iocoder.yudao.module.opshub.dal.mysql.order;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.opshub.dal.dataobject.order.OrderTimelineDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderTimelineMapper extends BaseMapperX<OrderTimelineDO> {

    default List<OrderTimelineDO> selectListByOrderCode(String orderCode) {
        return selectList(new LambdaQueryWrapperX<OrderTimelineDO>()
                .eq(OrderTimelineDO::getOrderCode, orderCode)
                .orderByAsc(OrderTimelineDO::getSortOrder));
    }

    default List<OrderTimelineDO> selectListByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapperX<OrderTimelineDO>()
                .eq(OrderTimelineDO::getOrderId, orderId)
                .orderByAsc(OrderTimelineDO::getSortOrder));
    }

}
