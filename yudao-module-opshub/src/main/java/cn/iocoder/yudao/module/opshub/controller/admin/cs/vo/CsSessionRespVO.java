package cn.iocoder.yudao.module.opshub.controller.admin.cs.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 咨询会话 Response VO")
@Data
public class CsSessionRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "会话编号", example = "CS-20260616-001")
    private String sessionNo;

    @Schema(description = "咨询类型", example = "signing")
    private String consultType;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "咨询上下文描述")
    private String context;

    @Schema(description = "上下文关联业务 ID")
    private Long contextId;

    @Schema(description = "上下文关联业务编号")
    private String contextCode;

    @Schema(description = "来源模块")
    private String sourceModule;

    @Schema(description = "经销商编码")
    private String dealerCode;

    @Schema(description = "经销商名称")
    private String dealerName;

    @Schema(description = "产品线编码")
    private String productLineCode;

    @Schema(description = "产品线名称")
    private String productLineName;

    @Schema(description = "发起人用户 ID")
    private Long initiatorId;

    @Schema(description = "发起人姓名")
    private String initiatorName;

    @Schema(description = "当前处理人用户 ID")
    private Long assigneeId;

    @Schema(description = "当前处理人姓名")
    private String assigneeName;

    @Schema(description = "最后一条消息摘要")
    private String lastMessage;

    @Schema(description = "最后一条消息时间")
    private LocalDateTime lastMessageTime;

    @Schema(description = "消息总数")
    private Integer messageCount;

    @Schema(description = "接单时间")
    private LocalDateTime acceptTime;

    @Schema(description = "完成时间")
    private LocalDateTime completeTime;

    @Schema(description = "关闭时间")
    private LocalDateTime closeTime;

    @Schema(description = "解决方案摘要")
    private String solutionSummary;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
