package cn.iocoder.yudao.module.opshub.controller.admin.signing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 签约合同统计 Response VO")
@Data
public class SigningContractStatisticsRespVO {

    @Schema(description = "合同总数")
    private Integer totalCount;

    @Schema(description = "主合同数")
    private Integer mainCount;

    @Schema(description = "政策合同数")
    private Integer policyCount;

    @Schema(description = "补充协议数")
    private Integer supplementCount;

    @Schema(description = "终止协议数")
    private Integer terminationCount;

    @Schema(description = "已签署数")
    private Integer signedCount;

    @Schema(description = "签署率")
    private BigDecimal signedRate;

    @Schema(description = "未签署数")
    private Integer unsignedCount;

    @Schema(description = "待签署数")
    private Integer pendingCount;

    @Schema(description = "签署中数")
    private Integer signingCount;

    @Schema(description = "主合同统计")
    private ContractTypeStat main;

    @Schema(description = "政策合同统计")
    private ContractTypeStat policy;

    @Schema(description = "补充协议统计")
    private ContractTypeStat supplement;

    @Schema(description = "终止协议统计")
    private ContractTypeStat termination;

    @Data
    public static class ContractTypeStat {
        @Schema(description = "总数")
        private Integer total;
        @Schema(description = "已签署")
        private Integer signed;
        @Schema(description = "未签署")
        private Integer unsigned;
    }

}
