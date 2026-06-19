package cn.iocoder.yudao.module.opshub.controller.admin.cs.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 操作请求统计 Response VO")
@Data
public class CsOpReqStatisticsRespVO {

    @Schema(description = "操作请求总数")
    private Integer totalCount;

    @Schema(description = "待处理数")
    private Integer pendingCount;

    @Schema(description = "处理中数")
    private Integer inProgressCount;

    @Schema(description = "等待验收数")
    private Integer pendingVerifyCount;

    @Schema(description = "已完成数")
    private Integer completedCount;

}
