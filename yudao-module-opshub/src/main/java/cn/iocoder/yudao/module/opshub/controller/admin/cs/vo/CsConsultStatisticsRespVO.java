package cn.iocoder.yudao.module.opshub.controller.admin.cs.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 咨询统计 Response VO")
@Data
public class CsConsultStatisticsRespVO {

    @Schema(description = "全部咨询数")
    private Integer totalCount;

    @Schema(description = "待处理数")
    private Integer pendingCount;

    @Schema(description = "处理中数")
    private Integer processingCount;

    @Schema(description = "已完成数")
    private Integer completedCount;

    @Schema(description = "已关闭数")
    private Integer closedCount;

}
