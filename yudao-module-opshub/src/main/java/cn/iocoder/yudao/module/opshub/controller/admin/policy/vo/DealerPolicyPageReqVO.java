package cn.iocoder.yudao.module.opshub.controller.admin.policy.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Schema(description = "管理后台 - 政策分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DealerPolicyPageReqVO extends PageParam {

    @Schema(description = "政策类型列表")
    private List<String> policyTypes;

    @Schema(description = "达成类型列表")
    private List<String> achievementTypes;

    @Schema(description = "状态列表")
    private List<String> statuses;

    @Schema(description = "经销商ID")
    private Long dealerId;

    @Schema(description = "关键词搜索")
    private String keyword;

}
