package cn.iocoder.yudao.module.opshub.controller.admin.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 订单 Response VO")
@Data
public class OrderInfoRespVO {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单号")
    private String orderCode;

    @Schema(description = "经销商ID")
    private Long dealerId;

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

    @Schema(description = "已付金额")
    private BigDecimal paidAmount;

    @Schema(description = "开票状态")
    private String invStatus;

    @Schema(description = "已开票金额")
    private BigDecimal invoicedAmount;

    @Schema(description = "确认时间")
    private LocalDateTime confirmedTime;

    @Schema(description = "发货时间")
    private LocalDateTime shippedTime;

    @Schema(description = "签收时间")
    private LocalDateTime signedTime;

    @Schema(description = "完成时间")
    private LocalDateTime completedTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
