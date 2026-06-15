package cn.iocoder.yudao.module.opshub.dal.dataobject.order;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单付款记录 DO
 */
@TableName("ops_order_payment")
@KeySequence("ops_order_payment_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderPaymentDO extends TenantBaseDO {

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
     * 付款金额
     */
    private BigDecimal payAmount;
    /**
     * 付款日期
     */
    private LocalDate payDate;
    /**
     * 付款方式（银行转账/支票等）
     */
    private String payMethod;
    /**
     * 付款凭证号
     */
    private String voucherNo;
    /**
     * 状态：pending（审批中）/ approved（已通过）/ rejected（已拒绝）
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.OrderPaymentStatusEnum}
     */
    private String status;
    /**
     * 申请时间
     */
    private LocalDateTime applyTime;
    /**
     * 审批通过时间
     */
    private LocalDateTime approveTime;
    /**
     * 备注
     */
    private String remark;

}
