package cn.iocoder.yudao.module.opshub.controller.admin.cs.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 客服工单 Response VO")
@Data
public class CsTaskRespVO {

    @Schema(description = "工单ID")
    private Long id;

    @Schema(description = "工单编号")
    private String taskNo;

    @Schema(description = "工单内容")
    private String content;

    @Schema(description = "紧急程度：0=紧急 1=高 2=中 3=低")
    private Integer urgency;

    @Schema(description = "状态：0=待接单 1=处理中 2=已交付 3=已关闭 4=已退回")
    private Integer status;

    @Schema(description = "提单人用户ID")
    private Long creatorUserId;

    @Schema(description = "提单人姓名")
    private String creatorUserName;

    @Schema(description = "当前处理人用户ID")
    private Long assigneeId;

    @Schema(description = "当前处理人姓名")
    private String assigneeName;

    @Schema(description = "分类：0=签约 1=政策 2=售后 3=订单 4=数据 5=其他")
    private Integer category;

    @Schema(description = "SLA 截止时间")
    private LocalDateTime slaDeadline;

    @Schema(description = "关联经销商编码")
    private String dealerCode;

    @Schema(description = "关联经销商名称")
    private String dealerName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "接单时间")
    private LocalDateTime acceptTime;

    @Schema(description = "交付时间")
    private LocalDateTime deliverTime;

    @Schema(description = "验收时间")
    private LocalDateTime verifyTime;

    @Schema(description = "退回原因")
    private String rejectReason;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
