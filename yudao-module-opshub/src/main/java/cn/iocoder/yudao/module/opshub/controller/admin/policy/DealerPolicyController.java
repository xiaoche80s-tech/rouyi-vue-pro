package cn.iocoder.yudao.module.opshub.controller.admin.policy;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.opshub.controller.admin.policy.vo.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.DealerInfoDO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.policy.DealerPolicyDO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.policy.DealerPolicyIndicatorDO;
import cn.iocoder.yudao.module.opshub.service.dealer.DealerInfoService;
import cn.iocoder.yudao.module.opshub.service.policy.DealerPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 政策看板")
@RestController
@RequestMapping("/opshub/dealer-policy")
@Validated
public class DealerPolicyController {

    @Resource
    private DealerPolicyService dealerPolicyService;

    @Resource
    private DealerInfoService dealerInfoService;

    // ========== CRUD 接口 ==========

    @PostMapping("/create")
    @Operation(summary = "创建政策")
    @PreAuthorize("@ss.hasPermission('dealer:policy:create')")
    public CommonResult<Long> createDealerPolicy(@Valid @RequestBody DealerPolicySaveReqVO reqVO) {
        return success(dealerPolicyService.createDealerPolicy(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新政策")
    @PreAuthorize("@ss.hasPermission('dealer:policy:update')")
    public CommonResult<Boolean> updateDealerPolicy(@Valid @RequestBody DealerPolicySaveReqVO reqVO) {
        dealerPolicyService.updateDealerPolicy(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除政策")
    @Parameter(name = "id", description = "政策ID", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:policy:delete')")
    public CommonResult<Boolean> deleteDealerPolicy(@RequestParam("id") Long id) {
        dealerPolicyService.deleteDealerPolicy(id);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得政策分页")
    @PreAuthorize("@ss.hasPermission('dealer:policy:query')")
    public CommonResult<PageResult<DealerPolicyRespVO>> getDealerPolicyPage(@Valid DealerPolicyPageReqVO pageReqVO) {
        PageResult<DealerPolicyDO> pageResult = dealerPolicyService.getDealerPolicyPage(pageReqVO);
        PageResult<DealerPolicyRespVO> voPageResult = BeanUtils.toBean(pageResult, DealerPolicyRespVO.class);
        fillDealerNames(voPageResult.getList());
        return success(voPageResult);
    }

    @GetMapping("/get")
    @Operation(summary = "获得政策详情")
    @Parameter(name = "id", description = "政策ID", required = true)
    @PreAuthorize("@ss.hasPermission('dealer:policy:query')")
    public CommonResult<DealerPolicyDetailRespVO> getDealerPolicy(@RequestParam("id") Long id) {
        DealerPolicyDO policy = dealerPolicyService.getDealerPolicy(id);
        DealerPolicyDetailRespVO vo = BeanUtils.toBean(policy, DealerPolicyDetailRespVO.class);
        if (vo != null && policy != null) {
            // 填充经销商名称
            DealerInfoDO dealer = dealerInfoService.getDealer(policy.getDealerId());
            if (dealer != null) {
                vo.setDealerName(dealer.getDealerName());
            }
            // 填充指标列表
            List<DealerPolicyIndicatorDO> indicators = dealerPolicyService.getIndicatorsByPolicyId(id);
            List<DealerPolicyDetailRespVO.IndicatorVO> indicatorVOs = BeanUtils.toBean(indicators, DealerPolicyDetailRespVO.IndicatorVO.class);
            if (indicatorVOs != null) {
                for (DealerPolicyDetailRespVO.IndicatorVO indVO : indicatorVOs) {
                    // 计算达成率
                    DealerPolicyIndicatorDO ind = indicators.stream()
                            .filter(i -> i.getId().equals(indVO.getId())).findFirst().orElse(null);
                    if (ind != null && ind.getTargetValue() != null && ind.getTargetValue().compareTo(BigDecimal.ZERO) != 0) {
                        indVO.setRate(ind.getAchievedValue().multiply(BigDecimal.valueOf(100))
                                .divide(ind.getTargetValue(), 1, RoundingMode.HALF_UP));
                    } else {
                        indVO.setRate(BigDecimal.ZERO);
                    }
                }
            }
            vo.setIndicators(indicatorVOs);
        }
        return success(vo);
    }

    // ========== 看板查询接口 ==========

    @GetMapping("/kpi-panels")
    @Operation(summary = "KPI 面板数据")
    @PreAuthorize("@ss.hasPermission('dealer:policy:query')")
    public CommonResult<List<PolicyKpiPanelRespVO>> getKpiPanels(@Valid PolicyKpiPanelPageReqVO reqVO) {
        return success(dealerPolicyService.getKpiPanels(reqVO));
    }

    @GetMapping("/indicator-chart")
    @Operation(summary = "指标柱状图数据")
    @PreAuthorize("@ss.hasPermission('dealer:policy:query')")
    public CommonResult<PolicyIndicatorChartRespVO> getIndicatorChart(@Valid PolicyKpiPanelPageReqVO reqVO) {
        return success(dealerPolicyService.getIndicatorChart(reqVO));
    }

    @GetMapping("/target-distribution")
    @Operation(summary = "目标分布数据")
    @PreAuthorize("@ss.hasPermission('dealer:policy:query')")
    public CommonResult<List<PolicyTargetGroupRespVO>> getTargetDistribution(@Valid PolicyKpiPanelPageReqVO reqVO) {
        return success(dealerPolicyService.getTargetDistribution(reqVO));
    }

    @GetMapping("/achievement-drilldown")
    @Operation(summary = "达成明细下钻")
    @PreAuthorize("@ss.hasPermission('dealer:policy:query')")
    public CommonResult<List<PolicyAchievementRespVO>> getAchievementDrilldown(
            @RequestParam("targetYear") Integer targetYear,
            @RequestParam("indicatorId") Long indicatorId,
            @RequestParam("level") String level,
            @RequestParam(value = "province", required = false) String province,
            @RequestParam(value = "hospital", required = false) String hospital) {
        return success(dealerPolicyService.getAchievementDrilldown(targetYear, indicatorId, level, province, hospital));
    }

    @GetMapping("/indicator-names")
    @Operation(summary = "获取指标名称列表")
    @PreAuthorize("@ss.hasPermission('dealer:policy:query')")
    public CommonResult<List<String>> getIndicatorNames(@RequestParam("targetYear") Integer targetYear) {
        return success(dealerPolicyService.getIndicatorNames(targetYear));
    }

    @GetMapping("/policies-by-indicator")
    @Operation(summary = "按指标查询政策列表")
    @PreAuthorize("@ss.hasPermission('dealer:policy:query')")
    public CommonResult<List<DealerPolicyRespVO>> getPoliciesByIndicator(
            @RequestParam("indicatorName") String indicatorName,
            @RequestParam("targetYear") Integer targetYear) {
        List<DealerPolicyDO> policies = dealerPolicyService.getPoliciesByIndicator(indicatorName, targetYear);
        List<DealerPolicyRespVO> voList = BeanUtils.toBean(policies, DealerPolicyRespVO.class);
        fillDealerNames(voList);
        return success(voList);
    }

    // ========== 辅助方法 ==========

    private void fillDealerNames(List<DealerPolicyRespVO> list) {
        if (list == null || list.isEmpty()) return;
        Set<Long> dealerIds = list.stream()
                .map(DealerPolicyRespVO::getDealerId)
                .collect(Collectors.toSet());
        Map<Long, String> dealerNameMap = dealerIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> {
                            DealerInfoDO dealer = dealerInfoService.getDealer(id);
                            return dealer != null ? dealer.getDealerName() : "";
                        }));
        for (DealerPolicyRespVO vo : list) {
            vo.setDealerName(dealerNameMap.getOrDefault(vo.getDealerId(), ""));
        }
    }

}
