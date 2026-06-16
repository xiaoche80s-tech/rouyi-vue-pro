package cn.iocoder.yudao.module.opshub.controller.admin.cs.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 客服工单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CsTaskPageReqVO extends PageParam {

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "紧急程度", example = "2")
    private Integer urgency;

    @Schema(description = "分类", example = "0")
    private Integer category;

    @Schema(description = "处理人用户ID", example = "100")
    private Long assigneeId;

    @Schema(description = "提单人用户ID", example = "200")
    private Long creatorUserId;

    @Schema(description = "经销商编码", example = "D001")
    private String dealerCode;

    @Schema(description = "关键词搜索")
    private String keyword;

}
