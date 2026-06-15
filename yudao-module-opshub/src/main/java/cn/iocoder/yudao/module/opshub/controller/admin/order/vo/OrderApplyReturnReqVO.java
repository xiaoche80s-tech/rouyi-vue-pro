package cn.iocoder.yudao.module.opshub.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 申请退货 Request VO")
@Data
public class OrderApplyReturnReqVO {

    @Schema(description = "订单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @Schema(description = "退货商品列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "退货商品列表不能为空")
    @Valid
    private List<ReturnItem> items;

    @Data
    public static class ReturnItem {

        @Schema(description = "产品明细ID", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "产品明细ID不能为空")
        private Long productId;

        @Schema(description = "退货数量", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "退货数量不能为空")
        private Integer returnQty;
    }

}
