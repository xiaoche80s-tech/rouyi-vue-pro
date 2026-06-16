package cn.iocoder.yudao.module.opshub.controller.admin.aftersale.vo;

import cn.iocoder.yudao.module.opshub.dal.dataobject.aftersale.AfterSaleProgressDO;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 售后详情 Response VO（含进度节点）")
@Data
public class AfterSaleDetailRespVO {

    // === 基本信息 ===

    @Schema(description = "售后单ID")
    private Long id;

    @Schema(description = "售后单号")
    private String aftersaleCode;

    @Schema(description = "经销商名称")
    private String dealerName;

    @Schema(description = "产品线名称")
    private String productLineName;

    @Schema(description = "关联订单号")
    private String orderCode;

    @Schema(description = "处理方式编码")
    private String handlingMethod;

    @Schema(description = "处理方式中文名")
    private String handlingMethodName;

    @Schema(description = "售后原因编码")
    private String reason;

    @Schema(description = "售后原因中文名")
    private String reasonName;

    @Schema(description = "进度状态")
    private String progressStatus;

    @Schema(description = "当前进度节点")
    private Integer currentStep;

    @Schema(description = "产品名称")
    private String productName;

    @Schema(description = "产品规格")
    private String productSpec;

    @Schema(description = "售后数量")
    private Integer quantity;

    @Schema(description = "退款金额")
    private BigDecimal refundAmount;

    @Schema(description = "退款状态")
    private String refundStatus;

    @Schema(description = "红字发票状态")
    private String redInvoiceStatus;

    @Schema(description = "退回物流公司")
    private String logisticsCompany;

    @Schema(description = "退回物流单号")
    private String logisticsNo;

    @Schema(description = "换货物流公司")
    private String exchangeLogisticsCompany;

    @Schema(description = "换货物流单号")
    private String exchangeLogisticsNo;

    @Schema(description = "申请时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime applyTime;

    @Schema(description = "审核通过时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime approvedTime;

    @Schema(description = "完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime completedTime;

    @Schema(description = "备注")
    private String remark;

    // === 进度节点 ===

    @Schema(description = "进度节点列表")
    private List<AfterSaleProgressDO> progressNodes;

}
