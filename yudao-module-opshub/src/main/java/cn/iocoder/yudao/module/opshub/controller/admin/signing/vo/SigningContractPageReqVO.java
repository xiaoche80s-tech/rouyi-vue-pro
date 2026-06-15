package cn.iocoder.yudao.module.opshub.controller.admin.signing.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Schema(description = "管理后台 - 签约合同分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SigningContractPageReqVO extends PageParam {

    @Schema(description = "时间维度", example = "month")
    private String timeDimension;

    @Schema(description = "月份列表(1-12)")
    private List<Integer> months;

    @Schema(description = "季度列表(1-4)")
    private List<Integer> quarters;

    @Schema(description = "年份列表")
    private List<Integer> years;

    @Schema(description = "产品线编码列表")
    private List<String> productLineCodes;

    @Schema(description = "合同类型列表")
    private List<String> contractTypes;

    @Schema(description = "签署状态列表")
    private List<String> statuses;

    @Schema(description = "经销商编码列表")
    private List<String> dealerCodes;

    @Schema(description = "关键词搜索")
    private String keyword;

}
