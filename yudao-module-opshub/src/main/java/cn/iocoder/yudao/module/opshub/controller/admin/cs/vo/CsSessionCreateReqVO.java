package cn.iocoder.yudao.module.opshub.controller.admin.cs.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "管理后台 - 创建咨询会话 Request VO")
@Data
public class CsSessionCreateReqVO {

    @Schema(description = "咨询类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "signing")
    @NotBlank(message = "咨询类型不能为空")
    private String consultType;

    @Schema(description = "来源模块", requiredMode = Schema.RequiredMode.REQUIRED, example = "signing")
    @NotBlank(message = "来源模块不能为空")
    private String sourceModule;

    @Schema(description = "咨询上下文描述", example = "合同 MC-2026-001 签署流程咨询")
    private String context;

    @Schema(description = "上下文关联业务 ID", example = "101")
    private Long contextId;

    @Schema(description = "上下文关联业务编号", example = "MC-2026-001")
    private String contextCode;

    @Schema(description = "产品线编码", example = "PL01")
    private String productLineCode;

    @Schema(description = "产品线名称", example = "骨科")
    private String productLineName;

    @Schema(description = "经销商编码", example = "D001")
    private String dealerCode;

    @Schema(description = "经销商名称", example = "华康医疗器械有限公司")
    private String dealerName;

}
