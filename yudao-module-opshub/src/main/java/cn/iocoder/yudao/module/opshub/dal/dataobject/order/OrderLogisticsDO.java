package cn.iocoder.yudao.module.opshub.dal.dataobject.order;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 订单物流轨迹 DO
 */
@TableName("ops_order_logistics")
@KeySequence("ops_order_logistics_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderLogisticsDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 关联订单 ID
     */
    private Long orderId;
    /**
     * 关联订单号
     */
    private String orderCode;
    /**
     * 物流公司
     */
    private String logisticsCompany;
    /**
     * 物流单号
     */
    private String trackingNo;
    /**
     * 节点描述
     */
    private String nodeDesc;
    /**
     * 节点时间
     */
    private LocalDateTime nodeTime;
    /**
     * 是否已完成节点
     */
    private Boolean isCompleted;
    /**
     * 排序序号
     */
    private Integer sortOrder;

}
