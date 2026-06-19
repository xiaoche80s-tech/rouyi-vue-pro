package cn.iocoder.yudao.module.opshub.service.dashboard;

import cn.iocoder.yudao.framework.common.biz.system.permission.PermissionCommonApi;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.opshub.controller.admin.aftersale.vo.AfterSaleStatisticsRespVO;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.CsConsultStatisticsRespVO;
import cn.iocoder.yudao.module.opshub.controller.admin.cs.vo.CsOpReqStatisticsRespVO;
import cn.iocoder.yudao.module.opshub.controller.admin.dashboard.vo.DashboardRespVO;
import cn.iocoder.yudao.module.opshub.controller.admin.dashboard.vo.DashboardRespVO.*;
import cn.iocoder.yudao.module.opshub.controller.admin.order.vo.OrderStatisticsRespVO;
import cn.iocoder.yudao.module.opshub.controller.admin.signing.vo.SigningContractStatisticsRespVO;
import cn.iocoder.yudao.module.opshub.controller.admin.signing.vo.SigningContractTrendRespVO;
import cn.iocoder.yudao.module.opshub.enums.OpsRoleCodeConstants;
import cn.iocoder.yudao.module.opshub.service.aftersale.AfterSaleInfoService;
import cn.iocoder.yudao.module.opshub.service.cs.CsOpReqService;
import cn.iocoder.yudao.module.opshub.service.cs.CsSessionService;
import cn.iocoder.yudao.module.opshub.service.cs.CsTaskService;
import cn.iocoder.yudao.module.opshub.service.order.OrderInfoService;
import cn.iocoder.yudao.module.opshub.service.signing.SigningContractService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 仪表盘 Service 实现
 * <p>
 * 按角色优先级（super_admin > brand_admin > brand_sales > service_executor > dealer）
 * 聚合各模块统计数据，返回角色感知的仪表盘数据。
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private SigningContractService signingContractService;
    @Resource
    private OrderInfoService orderInfoService;
    @Resource
    private AfterSaleInfoService afterSaleInfoService;
    @Resource
    private CsTaskService csTaskService;
    @Resource
    private CsSessionService csSessionService;
    @Resource
    private CsOpReqService csOpReqService;
    @Resource
    private PermissionCommonApi permissionApi;

    @Override
    public DashboardRespVO getDashboard() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String role = resolveRole(userId);
        LocalDateTime startTime = LocalDateTime.now().minusMonths(3);

        DashboardRespVO resp = new DashboardRespVO();
        resp.setRole(role);

        switch (role) {
            case OpsRoleCodeConstants.SUPER_ADMIN, OpsRoleCodeConstants.BRAND_ADMIN -> fillAdminDashboard(resp, startTime);
            case OpsRoleCodeConstants.BRAND_SALES -> fillSalesDashboard(resp, startTime);
            case OpsRoleCodeConstants.SERVICE_EXECUTOR -> fillExecutorDashboard(resp, startTime);
            case OpsRoleCodeConstants.DEALER -> fillDealerDashboard(resp, startTime);
            default -> fillAdminDashboard(resp, startTime); // fallback: 未知角色展示全量
        }

        return resp;
    }

    /**
     * 解析当前用户最高优先级角色
     */
    private String resolveRole(Long userId) {
        if (permissionApi.hasAnyRoles(userId, OpsRoleCodeConstants.SUPER_ADMIN)) {
            return OpsRoleCodeConstants.SUPER_ADMIN;
        }
        if (permissionApi.hasAnyRoles(userId, OpsRoleCodeConstants.BRAND_ADMIN)) {
            return OpsRoleCodeConstants.BRAND_ADMIN;
        }
        if (permissionApi.hasAnyRoles(userId, OpsRoleCodeConstants.BRAND_SALES)) {
            return OpsRoleCodeConstants.BRAND_SALES;
        }
        if (permissionApi.hasAnyRoles(userId, OpsRoleCodeConstants.SERVICE_EXECUTOR)) {
            return OpsRoleCodeConstants.SERVICE_EXECUTOR;
        }
        if (permissionApi.hasAnyRoles(userId, OpsRoleCodeConstants.DEALER)) {
            return OpsRoleCodeConstants.DEALER;
        }
        return OpsRoleCodeConstants.SUPER_ADMIN; // fallback
    }

    /**
     * 管理员（super_admin / brand_admin）：全量数据，无快捷操作
     */
    private void fillAdminDashboard(DashboardRespVO resp, LocalDateTime startTime) {
        // 签约
        SigningBlock signing = new SigningBlock();
        signing.setStatistics(signingContractService.getStatistics(startTime));
        signing.setTrend(signingContractService.getTrend("month"));
        resp.setSigning(signing);

        // 订单
        OrderBlock order = new OrderBlock();
        order.setStatistics(orderInfoService.getStatistics(startTime));
        resp.setOrder(order);

        // 售后
        AfterSaleBlock aftersale = new AfterSaleBlock();
        aftersale.setStatistics(afterSaleInfoService.getStatistics(startTime));
        resp.setAftersale(aftersale);

        // 工单
        CsTaskBlock csTask = new CsTaskBlock();
        csTask.setTabCounts(csTaskService.getTabCounts());
        resp.setCsTask(csTask);

        // 咨询
        CsConsultBlock csConsult = new CsConsultBlock();
        csConsult.setStatistics(csSessionService.getStatistics(startTime));
        resp.setCsConsult(csConsult);

        // 操作请求
        CsOpReqBlock csOpReq = new CsOpReqBlock();
        csOpReq.setStatistics(csOpReqService.getStatistics(startTime));
        resp.setCsOpReq(csOpReq);
    }

    /**
     * 品牌销售（brand_sales）：签约 + 订单 + 售后，无客服模块，无快捷操作
     */
    private void fillSalesDashboard(DashboardRespVO resp, LocalDateTime startTime) {
        // 签约
        SigningBlock signing = new SigningBlock();
        signing.setStatistics(signingContractService.getStatistics(startTime));
        signing.setTrend(signingContractService.getTrend("month"));
        resp.setSigning(signing);

        // 订单
        OrderBlock order = new OrderBlock();
        order.setStatistics(orderInfoService.getStatistics(startTime));
        resp.setOrder(order);

        // 售后
        AfterSaleBlock aftersale = new AfterSaleBlock();
        aftersale.setStatistics(afterSaleInfoService.getStatistics(startTime));
        resp.setAftersale(aftersale);

        // csTask / csConsult / csOpReq 均为 null（brand_sales 不涉及客服）
    }

    /**
     * 服务执行员（service_executor）：签约(简) + 售后(简) + 工单 + 咨询 + 操作请求
     */
    private void fillExecutorDashboard(DashboardRespVO resp, LocalDateTime startTime) {
        // 签约（统计数据 + 趋势）
        SigningBlock signing = new SigningBlock();
        signing.setStatistics(signingContractService.getStatistics(startTime));
        signing.setTrend(signingContractService.getTrend("month"));
        resp.setSigning(signing);

        // 售后（简化：仅统计数据）
        AfterSaleBlock aftersale = new AfterSaleBlock();
        aftersale.setStatistics(afterSaleInfoService.getStatistics(startTime));
        resp.setAftersale(aftersale);

        // 工单
        CsTaskBlock csTask = new CsTaskBlock();
        csTask.setTabCounts(csTaskService.getTabCounts());
        resp.setCsTask(csTask);

        // 咨询
        CsConsultBlock csConsult = new CsConsultBlock();
        csConsult.setStatistics(csSessionService.getStatistics(startTime));
        resp.setCsConsult(csConsult);

        // 操作请求
        CsOpReqBlock csOpReq = new CsOpReqBlock();
        csOpReq.setStatistics(csOpReqService.getStatistics(startTime));
        resp.setCsOpReq(csOpReq);
    }

    /**
     * 经销商（dealer）：签约 + 订单 + 售后 + 工单(简) + 操作请求(简)
     */
    private void fillDealerDashboard(DashboardRespVO resp, LocalDateTime startTime) {
        // 签约
        SigningBlock signing = new SigningBlock();
        signing.setStatistics(signingContractService.getStatistics(startTime));
        signing.setTrend(signingContractService.getTrend("month"));
        resp.setSigning(signing);

        // 订单
        OrderBlock order = new OrderBlock();
        order.setStatistics(orderInfoService.getStatistics(startTime));
        resp.setOrder(order);

        // 售后
        AfterSaleBlock aftersale = new AfterSaleBlock();
        aftersale.setStatistics(afterSaleInfoService.getStatistics(startTime));
        resp.setAftersale(aftersale);

        // 工单（简化：仅 tab 计数）
        CsTaskBlock csTask = new CsTaskBlock();
        csTask.setTabCounts(csTaskService.getTabCounts());
        resp.setCsTask(csTask);

        // 操作请求（简化：仅统计数据）
        CsOpReqBlock csOpReq = new CsOpReqBlock();
        csOpReq.setStatistics(csOpReqService.getStatistics(startTime));
        resp.setCsOpReq(csOpReq);
    }
}
