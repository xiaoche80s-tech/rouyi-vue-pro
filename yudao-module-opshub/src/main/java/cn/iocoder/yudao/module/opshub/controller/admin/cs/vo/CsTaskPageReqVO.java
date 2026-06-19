package cn.iocoder.yudao.module.opshub.controller.admin.cs.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

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

    @Schema(description = "产品线编码", example = "PL01")
    private String productLineCode;

    @Schema(description = "来源模块", example = "signing")
    private String sourceModule;

    // ========== 以下字段由 Service 层按角色自动填充 ==========

    @Schema(description = "可见范围：all/creator/assignee（Service 层自动填充）", hidden = true)
    private String viewScope;

    @Schema(description = "当前用户 ID（Service 层自动填充）", hidden = true)
    private Long currentUserId;

    // ========== 子标签过滤字段 ==========

    @Schema(description = "状态列表（多状态 IN 查询）", example = "[0,1,4]")
    private List<Integer> statusList;

    @Schema(description = "仅查询未分配工单（可领取）", hidden = true)
    private Boolean unassigned;

    @Schema(description = "子标签过滤: all/pending/done/claimable", example = "pending")
    private String tabFilter;

}
