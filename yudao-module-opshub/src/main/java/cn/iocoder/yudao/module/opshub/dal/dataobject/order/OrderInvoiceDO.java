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
 * 订单开票记录 DO
 */
@TableName("ops_order_invoice")
@KeySequence("ops_order_invoice_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderInvoiceDO extends TenantBaseDO {

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
     * 开票金额
     */
    private BigDecimal invoiceAmount;
    /**
     * 发票号（管理员/执行员回填）
     */
    private String invoiceNo;
    /**
     * 开票日期
     */
    private LocalDate invoiceDate;
    /**
     * 发票类型（增值税专用/增值税普通）
     */
    private String invoiceType;
    /**
     * 发票抬头
     */
    private String companyName;
    /**
     * 纳税人识别号
     */
    private String taxNo;
    /**
     * 特殊开票需求
     */
    private String specialRequest;
    /**
     * 状态：pending（待开票）/ invoiced（已开票）
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.OrderInvoiceRecordStatusEnum}
     */
    private String status;
    /**
     * 申请时间
     */
    private LocalDateTime applyTime;
    /**
     * 备注
     */
    private String remark;

}
