package cn.iocoder.yudao.module.opshub.controller.admin.cs.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 操作请求分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CsOpReqPageReqVO extends PageParam {

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "操作类型", example = "sign")
    private String opType;

    @Schema(description = "经销商编码", example = "D001")
    private String dealerCode;

    @Schema(description = "产品线编码", example = "PL01")
    private String productLineCode;

    @Schema(description = "来源模块", example = "signing")
    private String sourceModule;

    @Schema(description = "关键词搜索")
    private String keyword;

    // ========== 以下字段由 Service 层按角色自动填充 ==========

    @Schema(description = "可见范围：all/creator/assignee（Service 层自动填充）", hidden = true)
    private String viewScope;

    @Schema(description = "当前用户 ID（Service 层自动填充）", hidden = true)
    private Long currentUserId;

}
