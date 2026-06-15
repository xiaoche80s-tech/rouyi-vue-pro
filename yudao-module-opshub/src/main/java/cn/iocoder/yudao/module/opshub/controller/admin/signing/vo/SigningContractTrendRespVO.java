package cn.iocoder.yudao.module.opshub.controller.admin.signing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 签约趋势 Response VO")
@Data
public class SigningContractTrendRespVO {

    @Schema(description = "时间周期标签")
    private String period;

    @Schema(description = "主合同数")
    private Integer mainCount;

    @Schema(description = "政策合同数")
    private Integer policyCount;

    @Schema(description = "补充协议数")
    private Integer supplementCount;

    @Schema(description = "终止协议数")
    private Integer terminationCount;

    @Schema(description = "总数")
    private Integer totalCount;

}
