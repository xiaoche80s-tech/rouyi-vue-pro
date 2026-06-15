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
 * 订单主表 DO
 */
@TableName("ops_order_info")
@KeySequence("ops_order_info_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderInfoDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 订单号（唯一）
     */
    private String orderCode;
    /**
     * 经销商 ID
     */
    private Long dealerId;
    /**
     * 经销商编码（数据权限用）
     */
    private String dealerCode;
    /**
     * 经销商名称（冗余存储，列表展示用）
     */
    private String dealerName;
    /**
     * 产品线编码（数据权限用）
     */
    private String productLineCode;
    /**
     * 产品线名称（冗余存储）
     */
    private String productLineName;
    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;
    /**
     * 订单日期
     */
    private LocalDate orderDate;
    /**
     * 进度状态
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.OrderProgressStatusEnum}
     */
    private String progressStatus;
    /**
     * 付款状态
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.OrderPayStatusEnum}
     */
    private String payStatus;
    /**
     * 已付金额
     */
    private BigDecimal paidAmount;
    /**
     * 开票状态
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.OrderInvoiceStatusEnum}
     */
    private String invStatus;
    /**
     * 已开票金额
     */
    private BigDecimal invoicedAmount;
    /**
     * 确认时间
     */
    private LocalDateTime confirmedTime;
    /**
     * 发货时间
     */
    private LocalDateTime shippedTime;
    /**
     * 签收时间
     */
    private LocalDateTime signedTime;
    /**
     * 完成时间
     */
    private LocalDateTime completedTime;
    /**
     * 备注
     */
    private String remark;

}
