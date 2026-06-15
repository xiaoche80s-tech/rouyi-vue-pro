package cn.iocoder.yudao.module.opshub.controller.admin.signing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 签约合同 Response VO")
@Data
public class SigningContractRespVO {

    @Schema(description = "合同ID")
    private Long id;

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

    @Schema(description = "合同类型")
    private String contractType;

    @Schema(description = "合同类型中文名")
    private String contractTypeName;

    @Schema(description = "合同编码")
    private String contractCode;

    @Schema(description = "合同名称")
    private String contractName;

    @Schema(description = "签署状态")
    private String status;

    @Schema(description = "子状态")
    private String subStatus;

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

    @Schema(description = "附件文件ID列表(逗号分隔)")
    private String fileIds;

    @Schema(description = "签署凭证URL")
    private String signProofUrl;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
