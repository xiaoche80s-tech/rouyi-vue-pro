package cn.iocoder.yudao.module.opshub.dal.dataobject.order;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 订单时间线 DO
 */
@TableName("ops_order_timeline")
@KeySequence("ops_order_timeline_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderTimelineDO extends TenantBaseDO {

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
     * 节点编码：created / confirmed / shipped / signed / completed
     */
    private String nodeCode;
    /**
     * 节点名称：下单 / 已确认 / 已发货 / 已签收 / 已完成
     */
    private String nodeName;
    /**
     * 节点完成时间（为空表示未完成）
     */
    private LocalDateTime nodeTime;
    /**
     * 是否完成
     */
    private Boolean isCompleted;
    /**
     * 排序序号（1-5）
     */
    private Integer sortOrder;

}
