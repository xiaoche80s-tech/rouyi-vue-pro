package cn.iocoder.yudao.module.opshub.controller.admin.dashboard.vo;

import cn.iocoder.yudao.module.opshub.controller.admin.aftersale.vo.AfterSaleStatisticsRespVO;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.CsConsultStatisticsRespVO;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.CsOpReqStatisticsRespVO;
import cn.iocoder.yudao.module.opshub.controller.admin.order.vo.OrderStatisticsRespVO;
import cn.iocoder.yudao.module.opshub.controller.admin.signing.vo.SigningContractStatisticsRespVO;
import cn.iocoder.yudao.module.opshub.controller.admin.signing.vo.SigningContractTrendRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 角色化仪表盘 Response VO")
@Data
public class DashboardRespVO {

    @Schema(description = "当前角色 code")
    private String role;

    @Schema(description = "签约数据块")
    private SigningBlock signing;

    @Schema(description = "订单数据块")
    private OrderBlock order;

    @Schema(description = "售后数据块")
    private AfterSaleBlock aftersale;

    @Schema(description = "工单数据块")
    private CsTaskBlock csTask;

    @Schema(description = "咨询数据块")
    private CsConsultBlock csConsult;

    @Schema(description = "操作请求数据块")
    private CsOpReqBlock csOpReq;

    @Schema(description = "快捷操作列表")
    private List<QuickAction> quickActions;

    // ========== 内部 Block 类 ==========

    @Data
    public static class SigningBlock {
        @Schema(description = "签约统计数据")
        private SigningContractStatisticsRespVO statistics;
        @Schema(description = "签约趋势数据")
        private List<SigningContractTrendRespVO> trend;
    }

    @Data
    public static class OrderBlock {
        @Schema(description = "订单统计数据")
        private OrderStatisticsRespVO statistics;
    }

    @Data
    public static class AfterSaleBlock {
        @Schema(description = "售后统计数据")
        private AfterSaleStatisticsRespVO statistics;
    }

    @Data
    public static class CsTaskBlock {
        @Schema(description = "工单各 Tab 数量")
        private Map<String, Long> tabCounts;
    }

    @Data
    public static class CsConsultBlock {
        @Schema(description = "咨询统计数据")
        private CsConsultStatisticsRespVO statistics;
    }

    @Data
    public static class CsOpReqBlock {
        @Schema(description = "操作请求统计数据")
        private CsOpReqStatisticsRespVO statistics;
    }

    @Data
    public static class QuickAction {
        @Schema(description = "按钮标签")
        private String label;
        @Schema(description = "图标标识")
        private String icon;
        @Schema(description = "路由路径")
        private String route;
    }

}
