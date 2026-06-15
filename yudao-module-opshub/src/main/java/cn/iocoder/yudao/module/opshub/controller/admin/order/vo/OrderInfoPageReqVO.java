package cn.iocoder.yudao.module.opshub.controller.admin.order.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Schema(description = "管理后台 - 订单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OrderInfoPageReqVO extends PageParam {

    @Schema(description = "快捷时间", example = "today/week/month/all")
    private String quickTime;

    @Schema(description = "季度列表(1-4)")
    private List<Integer> quarters;

    @Schema(description = "月度列表(1-12)")
    private List<Integer> months;

    @Schema(description = "产品线编码列表")
    private List<String> productLineCodes;

    @Schema(description = "经销商编码列表")
    private List<String> dealerCodes;

    @Schema(description = "进度状态", example = "pending")
    private String progressStatus;

    @Schema(description = "付款状态", example = "unpaid")
    private String payStatus;

    @Schema(description = "开票状态", example = "uninvoiced")
    private String invStatus;

    @Schema(description = "关键词搜索")
    private String keyword;

}
