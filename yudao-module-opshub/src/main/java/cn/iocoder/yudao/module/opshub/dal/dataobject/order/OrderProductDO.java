package cn.iocoder.yudao.module.opshub.dal.dataobject.order;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单产品明细 DO
 */
@TableName("ops_order_product")
@KeySequence("ops_order_product_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderProductDO extends TenantBaseDO {

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
     * 关联订单号（业务字段冗余）
     */
    private String orderCode;
    /**
     * 产品编码
     */
    private String productCode;
    /**
     * 产品名称
     */
    private String productName;
    /**
     * 规格型号
     */
    private String specModel;
    /**
     * 单价
     */
    private BigDecimal unitPrice;
    /**
     * 数量
     */
    private Integer quantity;
    /**
     * 单位（件/台/套等）
     */
    private String unit;
    /**
     * 金额（= 单价 × 数量）
     */
    private BigDecimal amount;
    /**
     * 可退货数量（默认 = quantity，退货后扣减）
     */
    private Integer returnableQty;

}
