package cn.iocoder.yudao.module.opshub.controller.admin.aftersale.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 售后更新进度 Request VO")
@Data
public class AfterSaleUpdateProgressReqVO {

    @Schema(description = "售后单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "售后单ID不能为空")
    private Long aftersaleId;

    @Schema(description = "要完成的节点序号（2-5）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "节点序号不能为空")
    private Integer stepOrder;

    @Schema(description = "节点备注")
    private String remark;

    @Schema(description = "物流公司（商品退回/换货发出节点）")
    private String logisticsCompany;

    @Schema(description = "物流单号")
    private String logisticsNo;

    @Schema(description = "退款金额（退款完成节点回填）")
    private BigDecimal refundAmount;

    @Schema(description = "红字发票号（红字发票节点回填）")
    private String redInvoiceNo;

}
