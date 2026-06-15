package cn.iocoder.yudao.module.opshub.controller.admin.signing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "管理后台 - 更新签约合同 Request VO")
@Data
public class SigningContractUpdateReqVO {

    @Schema(description = "合同ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "合同ID不能为空")
    private Long id;

    @Schema(description = "经销商ID")
    private Long dealerId;

    @Schema(description = "经销商编码")
    private String dealerCode;

    @Schema(description = "产品线编码")
    private String productLineCode;

    @Schema(description = "合同类型")
    private String contractType;

    @Schema(description = "合同类型中文名")
    private String contractTypeName;

    @Schema(description = "合同名称")
    private String contractName;

    @Schema(description = "下发日期")
    private LocalDate issuedDate;

    @Schema(description = "签署日期")
    private LocalDate signDate;

    @Schema(description = "合同摘要")
    private String summary;

    @Schema(description = "政策解析")
    private String policyAnalysis;

    @Schema(description = "政策指标JSON")
    private String indicators;

    @Schema(description = "附件文件ID列表")
    private List<Long> fileIds;

    @Schema(description = "备注")
    private String remark;

}
