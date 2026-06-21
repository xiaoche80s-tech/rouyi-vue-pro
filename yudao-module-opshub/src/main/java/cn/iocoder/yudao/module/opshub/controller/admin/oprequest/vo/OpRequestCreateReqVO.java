package cn.iocoder.yudao.module.opshub.controller.admin.oprequest.vo;

import cn.iocoder.yudao.module.opshub.enums.OpRequestTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 操作请求创建 Request VO")
@Data
public class OpRequestCreateReqVO {

    @Schema(description = "请求类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请求类型不能为空")
    private String requestType;

    @Schema(description = "经销商 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "经销商不能为空")
    private Long dealerId;

    @Schema(description = "经销商编码")
    private String dealerCode;

    @Schema(description = "合同 ID（签约类型必填）")
    private Long contractId;

    @Schema(description = "备注")
    private String remark;

}
