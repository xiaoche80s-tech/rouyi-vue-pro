package cn.iocoder.yudao.module.opshub.controller.admin.aftersale.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 售后简要 Response VO（列表用）")
@Data
public class AfterSaleInfoSimpleRespVO {

    @Schema(description = "售后单ID")
    private Long id;

    @Schema(description = "售后单号")
    private String aftersaleCode;

    @Schema(description = "经销商名称")
    private String dealerName;

    @Schema(description = "产品线名称")
    private String productLineName;

    @Schema(description = "关联订单号")
    private String orderCode;

    @Schema(description = "处理方式")
    private String handlingMethod;

    @Schema(description = "售后原因")
    private String reason;

    @Schema(description = "进度状态")
    private String progressStatus;

    @Schema(description = "当前进度节点")
    private Integer currentStep;

    @Schema(description = "产品名称")
    private String productName;

    @Schema(description = "申请时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime applyTime;

    @Schema(description = "进度节点预览（hover用）")
    private List<ProgressNodePreview> progressNodes;

    @Data
    public static class ProgressNodePreview {
        @Schema(description = "节点名称")
        private String nodeName;

        @Schema(description = "节点编码")
        private String nodeCode;

        @Schema(description = "是否完成")
        private Boolean isCompleted;

        @Schema(description = "完成时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime nodeTime;
    }

}
