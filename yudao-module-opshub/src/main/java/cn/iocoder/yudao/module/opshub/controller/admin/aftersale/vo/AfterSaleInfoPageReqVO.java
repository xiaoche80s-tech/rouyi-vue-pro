package cn.iocoder.yudao.module.opshub.controller.admin.aftersale.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Schema(description = "管理后台 - 售后分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AfterSaleInfoPageReqVO extends PageParam {

    @Schema(description = "处理方式", example = "return")
    private String handlingMethod;

    @Schema(description = "售后原因", example = "complaint")
    private String reason;

    @Schema(description = "进度状态", example = "pending")
    private String progressStatus;

    @Schema(description = "产品线编码列表")
    private List<String> productLineCodes;

    @Schema(description = "经销商编码列表")
    private List<String> dealerCodes;

    @Schema(description = "关键词搜索（售后单号/关联订单号）")
    private String keyword;

    @Schema(description = "排序字段", example = "apply_time")
    private String sortField;

    @Schema(description = "排序方向", example = "desc")
    private String sortOrder;

}
