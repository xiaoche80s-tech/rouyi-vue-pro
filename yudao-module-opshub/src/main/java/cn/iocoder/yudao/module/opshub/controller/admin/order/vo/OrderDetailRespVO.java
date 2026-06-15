package cn.iocoder.yudao.module.opshub.controller.admin.order.vo;

import cn.iocoder.yudao.module.opshub.dal.dataobject.order.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "管理后台 - 订单详情 Response VO（含 6 Tab 数据）")
@Data
public class OrderDetailRespVO {

    // === 基本信息 ===

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单号")
    private String orderCode;

    @Schema(description = "经销商ID")
    private Long dealerId;

    @Schema(description = "经销商名称")
    private String dealerName;

    @Schema(description = "产品线名称")
    private String productLineName;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "订单日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate orderDate;

    @Schema(description = "进度状态")
    private String progressStatus;

    @Schema(description = "付款状态")
    private String payStatus;

    @Schema(description = "开票状态")
    private String invStatus;

    @Schema(description = "已付金额")
    private BigDecimal paidAmount;

    @Schema(description = "已开票金额")
    private BigDecimal invoicedAmount;

    @Schema(description = "备注")
    private String remark;

    // === 时间线 ===

    @Schema(description = "时间线节点")
    private List<OrderTimelineDO> timeline;

    // === 产品明细 ===

    @Schema(description = "产品明细")
    private List<OrderProductDO> products;

    // === 付款信息 ===

    @Schema(description = "付款记录")
    private List<OrderPaymentDO> payments;

    // === 开票信息 ===

    @Schema(description = "开票记录")
    private List<OrderInvoiceDO> invoices;

    // === 物流轨迹 ===

    @Schema(description = "物流轨迹")
    private List<OrderLogisticsDO> logistics;

    // === 退货信息（从 product 表计算） ===

    @Schema(description = "可退货商品列表")
    private List<ReturnableProduct> returnableProducts;

    @Data
    public static class ReturnableProduct {
        @Schema(description = "产品明细ID")
        private Long productId;

        @Schema(description = "产品名称")
        private String productName;

        @Schema(description = "规格型号")
        private String specModel;

        @Schema(description = "数量")
        private Integer quantity;

        @Schema(description = "可退货数量")
        private Integer returnableQty;

        @Schema(description = "单价")
        private BigDecimal unitPrice;

        @Schema(description = "金额")
        private BigDecimal amount;
    }

}
