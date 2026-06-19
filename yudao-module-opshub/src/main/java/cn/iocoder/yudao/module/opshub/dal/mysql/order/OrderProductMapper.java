package cn.iocoder.yudao.module.opshub.dal.mysql.order;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.opshub.dal.dataobject.order.OrderProductDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderProductMapper extends BaseMapperX<OrderProductDO> {

    default List<OrderProductDO> selectListByOrderCode(String orderCode) {
        return selectList(new LambdaQueryWrapperX<OrderProductDO>()
                .eq(OrderProductDO::getOrderCode, orderCode)
                .orderByAsc(OrderProductDO::getId));
    }

    default List<OrderProductDO> selectListByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapperX<OrderProductDO>()
                .eq(OrderProductDO::getOrderId, orderId)
                .orderByAsc(OrderProductDO::getId));
    }

    /**
     * 按订单号物理删除产品明细（绕过 BaseDO 逻辑删除）
     */
    @Delete("DELETE FROM ops_order_product WHERE order_code = #{orderCode} AND deleted = 0")
    int deletePhysicalByOrderCode(@Param("orderCode") String orderCode);

}
