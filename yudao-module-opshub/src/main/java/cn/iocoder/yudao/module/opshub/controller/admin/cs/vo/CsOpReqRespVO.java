package cn.iocoder.yudao.module.opshub.controller.admin.cs.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 操作请求 Response VO")
@Data
public class CsOpReqRespVO {

    @Schema(description = "操作请求ID")
    private Long id;

    @Schema(description = "操作请求编号")
    private String opreqCode;

    @Schema(description = "操作类型：sign/payment/invoice/return/stamp")
    private String opType;

    @Schema(description = "状态：0=待处理 1=处理中 2=等待验收 3=已完成")
    private Integer status;

    @Schema(description = "经销商编码")
    private String dealerCode;

    @Schema(description = "经销商名称")
    private String dealerName;

    @Schema(description = "产品线编码")
    private String productLineCode;

    @Schema(description = "产品线名称")
    private String productLineName;

    @Schema(description = "来源模块")
    private String sourceModule;

    @Schema(description = "来源业务ID")
    private Long sourceId;

    @Schema(description = "来源业务编号")
    private String sourceCode;

    @Schema(description = "请求内容描述")
    private String content;

    @Schema(description = "发起人用户ID")
    private Long creatorUserId;

    @Schema(description = "当前处理人用户ID")
    private Long assigneeId;

    @Schema(description = "接单时间")
    private LocalDateTime acceptTime;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "处理留言")
    private String submitRemark;

    @Schema(description = "验收时间")
    private LocalDateTime verifyTime;

    @Schema(description = "完成时间")
    private LocalDateTime completedTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

}
