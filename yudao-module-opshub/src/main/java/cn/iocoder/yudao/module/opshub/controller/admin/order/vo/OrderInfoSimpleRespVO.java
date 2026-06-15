package cn.iocoder.yudao.module.opshub.controller.admin.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "管理后台 - 订单简要 Response VO（列表用）")
@Data
public class OrderInfoSimpleRespVO {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单号")
    private String orderCode;

    @Schema(description = "经销商编码")
    private String dealerCode;

    @Schema(description = "经销商名称")
    private String dealerName;

    @Schema(description = "产品线编码")
    private String productLineCode;

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

}
