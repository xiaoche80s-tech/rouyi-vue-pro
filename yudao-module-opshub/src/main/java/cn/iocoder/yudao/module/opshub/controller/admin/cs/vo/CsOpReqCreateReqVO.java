package cn.iocoder.yudao.module.opshub.controller.admin.cs.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "管理后台 - 创建操作请求 Request VO")
@Data
public class CsOpReqCreateReqVO {

    @Schema(description = "操作类型：sign/payment/invoice/return/stamp", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "操作类型不能为空")
    private String opType;

    @Schema(description = "经销商编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "经销商编码不能为空")
    private String dealerCode;

    @Schema(description = "产品线编码")
    private String productLineCode;

    @Schema(description = "来源模块", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "来源模块不能为空")
    private String sourceModule;

    @Schema(description = "来源业务ID")
    private Long sourceId;

    @Schema(description = "来源业务编号")
    private String sourceCode;

    @Schema(description = "请求内容描述", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "请求内容不能为空")
    private String content;

    @Schema(description = "备注")
    private String remark;

}
