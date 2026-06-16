package cn.iocoder.yudao.module.opshub.dal.dataobject.aftersale;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 售后主表 DO
 */
@TableName("ops_aftersale_info")
@KeySequence("ops_aftersale_info_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class AfterSaleInfoDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 售后单号（唯一，如 SO20260615-001）
     */
    private String aftersaleCode;
    /**
     * 经销商 ID
     */
    private Long dealerId;
    /**
     * 经销商编码（数据权限用）
     */
    private String dealerCode;
    /**
     * 经销商名称（冗余存储）
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
     * 关联订单号
     */
    private String orderCode;
    /**
     * 处理方式
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.AfterSaleHandlingMethodEnum}
     */
    private String handlingMethod;
    /**
     * 售后原因
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.AfterSaleReasonEnum}
     */
    private String reason;
    /**
     * 进度状态
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.AfterSaleProgressStatusEnum}
     */
    private String progressStatus;
    /**
     * 当前进度节点序号（1-5）
     */
    private Integer currentStep;
    /**
     * 产品名称
     */
    private String productName;
    /**
     * 产品规格型号
     */
    private String productSpec;
    /**
     * 售后数量
     */
    private Integer quantity;
    /**
     * 退款金额
     */
    private BigDecimal refundAmount;
    /**
     * 退款状态
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.AfterSaleRefundStatusEnum}
     */
    private String refundStatus;
    /**
     * 红字发票状态
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.AfterSaleRedInvoiceStatusEnum}
     */
    private String redInvoiceStatus;
    /**
     * 退回物流公司
     */
    private String logisticsCompany;
    /**
     * 退回物流单号
     */
    private String logisticsNo;
    /**
     * 换货物流公司
     */
    private String exchangeLogisticsCompany;
    /**
     * 换货物流单号
     */
    private String exchangeLogisticsNo;
    /**
     * 申请时间
     */
    private LocalDateTime applyTime;
    /**
     * 审核通过时间
     */
    private LocalDateTime approvedTime;
    /**
     * 完成时间
     */
    private LocalDateTime completedTime;
    /**
     * 备注
     */
    private String remark;

}
