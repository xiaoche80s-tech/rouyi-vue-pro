package cn.iocoder.yudao.module.opshub.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 申请开票 Request VO")
@Data
public class OrderApplyInvoiceReqVO {

    @Schema(description = "订单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @Schema(description = "发票抬头", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "发票抬头不能为空")
    private String companyName;

    @Schema(description = "纳税人识别号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "纳税人识别号不能为空")
    private String taxNo;

    @Schema(description = "特殊开票需求")
    private String specialRequest;

}
