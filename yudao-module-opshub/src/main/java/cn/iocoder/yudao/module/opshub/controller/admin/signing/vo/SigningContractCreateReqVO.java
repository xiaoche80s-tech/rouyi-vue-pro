package cn.iocoder.yudao.module.opshub.controller.admin.signing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Schema(description = "管理后台 - 创建签约合同 Request VO")
@Data
public class SigningContractCreateReqVO {

    @Schema(description = "经销商ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "经销商ID不能为空")
    private Long dealerId;

    @Schema(description = "经销商编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "经销商编码不能为空")
    private String dealerCode;

    @Schema(description = "产品线编码")
    private String productLineCode;

    @Schema(description = "合同类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "合同类型不能为空")
    private String contractType;

    @Schema(description = "合同类型中文名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "合同类型中文名不能为空")
    private String contractTypeName;

    @Schema(description = "合同名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "合同名称不能为空")
    private String contractName;

    @Schema(description = "下发日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "下发日期不能为空")
    private LocalDate issuedDate;

    @Schema(description = "签署日期")
    private LocalDate signDate;

    @Schema(description = "合同摘要")
    private String summary;

    @Schema(description = "政策解析")
    private String policyAnalysis;

    @Schema(description = "政策指标JSON")
    private String indicators;

    @Schema(description = "备注")
    private String remark;

}
