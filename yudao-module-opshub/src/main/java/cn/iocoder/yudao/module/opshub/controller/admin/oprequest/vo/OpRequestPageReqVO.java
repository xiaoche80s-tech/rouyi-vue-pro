package cn.iocoder.yudao.module.opshub.controller.admin.oprequest.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 操作请求分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OpRequestPageReqVO extends PageParam {

    @Schema(description = "请求类型")
    private String requestType;

    @Schema(description = "状态")
    private String requestStatus;

    @Schema(description = "经销商编码")
    private String dealerCode;

    @Schema(description = "关键词（请求编号/编码模糊搜索）")
    private String keyword;

}
