package cn.iocoder.yudao.module.opshub.controller.admin.cs.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 创建客服工单 Request VO")
@Data
public class CsTaskCreateReqVO {

    @Schema(description = "工单内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "工单内容不能为空")
    private String content;

    @Schema(description = "紧急程度：0=紧急 1=高 2=中 3=低", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "紧急程度不能为空")
    private Integer urgency;

    @Schema(description = "指定处理人用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "处理人不能为空")
    private Long assigneeId;

    @Schema(description = "分类：0=签约 1=政策 2=售后 3=订单 4=数据 5=其他", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "分类不能为空")
    private Integer category;

    @Schema(description = "SLA 截止时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "SLA截止时间不能为空")
    private LocalDateTime slaDeadline;

    @Schema(description = "关联经销商编码")
    private String dealerCode;

    @Schema(description = "经销商名称")
    private String dealerName;

    @Schema(description = "产品线编码")
    private String productLineCode;

    @Schema(description = "产品线名称")
    private String productLineName;

    @Schema(description = "来源模块：aftersale/order/signing/basedata/manual")
    private String sourceModule;

    @Schema(description = "备注")
    private String remark;

}
