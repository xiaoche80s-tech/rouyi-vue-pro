package cn.iocoder.yudao.module.opshub.controller.admin.aftersale.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 售后 Response VO")
@Data
public class AfterSaleInfoRespVO {

    @Schema(description = "售后单ID")
    private Long id;

    @Schema(description = "售后单号")
    private String aftersaleCode;

    @Schema(description = "经销商编码")
    private String dealerCode;

    @Schema(description = "经销商名称")
    private String dealerName;

    @Schema(description = "产品线编码")
    private String productLineCode;

    @Schema(description = "产品线名称")
    private String productLineName;

    @Schema(description = "关联订单号")
    private String orderCode;

    @Schema(description = "处理方式")
    private String handlingMethod;

    @Schema(description = "售后原因")
    private String reason;

    @Schema(description = "进度状态")
    private String progressStatus;

    @Schema(description = "当前进度节点")
    private Integer currentStep;

    @Schema(description = "产品名称")
    private String productName;

    @Schema(description = "产品规格")
    private String productSpec;

    @Schema(description = "售后数量")
    private Integer quantity;

    @Schema(description = "退款金额")
    private BigDecimal refundAmount;

    @Schema(description = "退款状态")
    private String refundStatus;

    @Schema(description = "红字发票状态")
    private String redInvoiceStatus;

    @Schema(description = "物流公司")
    private String logisticsCompany;

    @Schema(description = "物流单号")
    private String logisticsNo;

    @Schema(description = "申请时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime applyTime;

    @Schema(description = "备注")
    private String remark;

}
