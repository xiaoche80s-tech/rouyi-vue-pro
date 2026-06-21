package cn.iocoder.yudao.module.opshub.service.policy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.opshub.controller.admin.policy.vo.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.dealer.DealerInfoDO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.policy.DealerPolicyAchievementDO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.policy.DealerPolicyDO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.policy.DealerPolicyIndicatorDO;
import cn.iocoder.yudao.module.opshub.dal.mysql.policy.DealerPolicyAchievementMapper;
import cn.iocoder.yudao.module.opshub.dal.mysql.policy.DealerPolicyIndicatorMapper;
import cn.iocoder.yudao.module.opshub.dal.mysql.policy.DealerPolicyMapper;
import cn.iocoder.yudao.module.opshub.service.dealer.DealerInfoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.opshub.enums.ErrorCodeConstants.*;

/**
 * 经销商政策 Service 实现类
 */
@Service
@Validated
public class DealerPolicyServiceImpl implements DealerPolicyService {

    @Resource
    private DealerPolicyMapper dealerPolicyMapper;

    @Resource
    private DealerPolicyIndicatorMapper indicatorMapper;

    @Resource
    private DealerPolicyAchievementMapper achievementMapper;

    @Resource
    private DealerInfoService dealerInfoService;

    // ========== CRUD ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDealerPolicy(DealerPolicySaveReqVO reqVO) {
        // 1. 校验经销商
        DealerInfoDO dealer = dealerInfoService.getDealer(reqVO.getDealerId());
        if (dealer == null) {
            throw exception(DEALER_NOT_EXISTS);
        }

        // 2. 自动生成政策编码
        String policyCode = generatePolicyCode();

        // 3. 构建 DO
        DealerPolicyDO policyDO = BeanUtils.toBean(reqVO, DealerPolicyDO.class);
        policyDO.setPolicyCode(policyCode);
        policyDO.setDealerCode(dealer.getDealerCode());

        // 4. 插入政策
        dealerPolicyMapper.insert(policyDO);

        // 5. 插入指标
        insertIndicators(policyDO, reqVO.getIndicators());

        return policyDO.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDealerPolicy(DealerPolicySaveReqVO reqVO) {
        // 1. 校验存在
        DealerPolicyDO existDO = dealerPolicyMapper.selectById(reqVO.getId());
        if (existDO == null) {
            throw exception(POLICY_NOT_EXISTS);
        }

        // 2. 更新政策
        DealerPolicyDO updateDO = BeanUtils.toBean(reqVO, DealerPolicyDO.class);
        dealerPolicyMapper.updateById(updateDO);

        // 3. 全量替换指标
        indicatorMapper.deleteByPolicyId(existDO.getId());
        insertIndicators(existDO, reqVO.getIndicators());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDealerPolicy(Long id) {
        // 1. 校验存在
        DealerPolicyDO existDO = dealerPolicyMapper.selectById(id);
        if (existDO == null) {
            throw exception(POLICY_NOT_EXISTS);
        }

        // 2. 级联删除指标
        indicatorMapper.deleteByPolicyId(id);

        // 3. 删除政策
        dealerPolicyMapper.deleteById(id);
    }

    @Override
    public DealerPolicyDO getDealerPolicy(Long id) {
        return dealerPolicyMapper.selectById(id);
    }

    @Override
    public PageResult<DealerPolicyDO> getDealerPolicyPage(DealerPolicyPageReqVO reqVO) {
        return dealerPolicyMapper.selectPage(reqVO);
    }

    @Override
    public List<DealerPolicyIndicatorDO> getIndicatorsByPolicyId(Long policyId) {
        return indicatorMapper.selectListByPolicyId(policyId);
    }

    // ========== 看板查询 ==========

    @Override
    public List<PolicyKpiPanelRespVO> getKpiPanels(PolicyKpiPanelPageReqVO reqVO) {
        // 1. 构建查询条件
        LambdaQueryWrapperX<DealerPolicyDO> policyWrapper = buildPolicyWrapper(reqVO);
        List<DealerPolicyDO> policies = dealerPolicyMapper.selectList(policyWrapper);
        if (CollUtil.isEmpty(policies)) {
            return Collections.emptyList();
        }

        // 2. 查询所有关联指标
        Set<Long> policyIds = policies.stream().map(DealerPolicyDO::getId).collect(Collectors.toSet());
        Map<Long, DealerPolicyDO> policyMap = policies.stream()
                .collect(Collectors.toMap(DealerPolicyDO::getId, p -> p));

        List<DealerPolicyIndicatorDO> indicators = indicatorMapper.selectList(
                new LambdaQueryWrapperX<DealerPolicyIndicatorDO>()
                        .in(DealerPolicyIndicatorDO::getPolicyId, policyIds)
                        .eq(DealerPolicyIndicatorDO::getTargetYear, reqVO.getTargetYear())
                        .inIfPresent(DealerPolicyIndicatorDO::getIndicatorName, parseStrList(reqVO.getIndicatorNames()))
                        .inIfPresent(DealerPolicyIndicatorDO::getTargetMonth, parseIntList(reqVO.getMonths())));

        // 3. 按 indicator_name 分组
        Map<String, List<DealerPolicyIndicatorDO>> grouped = indicators.stream()
                .collect(Collectors.groupingBy(DealerPolicyIndicatorDO::getIndicatorName));

        // G2: 查询经销商名称 Map
        Set<Long> dealerIds = policies.stream().map(DealerPolicyDO::getDealerId).collect(Collectors.toSet());
        Map<Long, String> dealerNameMap = new HashMap<>();
        for (Long dealerId : dealerIds) {
            DealerInfoDO dealer = dealerInfoService.getDealer(dealerId);
            dealerNameMap.put(dealerId, dealer != null ? dealer.getDealerName() : "");
        }

        // 4. 构建面板
        List<PolicyKpiPanelRespVO> result = new ArrayList<>();
        for (Map.Entry<String, List<DealerPolicyIndicatorDO>> entry : grouped.entrySet()) {
            String indicatorName = entry.getKey();
            List<DealerPolicyIndicatorDO> indList = entry.getValue();

            // 统计关联政策数
            Set<Long> relatedPolicyIds = indList.stream()
                    .map(DealerPolicyIndicatorDO::getPolicyId).collect(Collectors.toSet());

            // 取单位（第一条）
            String unit = indList.get(0).getUnit();

            // 按时间维度聚合
            List<PolicyKpiPanelRespVO.PeriodVO> periods = aggregatePeriods(indList, policyMap);

            // G2: Build targetGroups for this indicator
            List<PolicyTargetGroupRespVO> targetGroups = buildTargetGroups(indList, policyMap, dealerNameMap);

            PolicyKpiPanelRespVO panel = new PolicyKpiPanelRespVO();
            panel.setIndicatorName(indicatorName);
            panel.setUnit(unit);
            panel.setPolicyCount(relatedPolicyIds.size());
            panel.setPeriods(periods);
            panel.setTargetGroups(targetGroups);
            result.add(panel);
        }
        return result;
    }

    /**
     * G2: Build target groups for a single indicator
     */
    private List<PolicyTargetGroupRespVO> buildTargetGroups(List<DealerPolicyIndicatorDO> indList,
                                                             Map<Long, DealerPolicyDO> policyMap,
                                                             Map<Long, String> dealerNameMap) {
        Map<BigDecimal, List<DealerPolicyIndicatorDO>> grouped = indList.stream()
                .collect(Collectors.groupingBy(DealerPolicyIndicatorDO::getTargetValue));

        List<PolicyTargetGroupRespVO> result = new ArrayList<>();
        for (Map.Entry<BigDecimal, List<DealerPolicyIndicatorDO>> entry : grouped.entrySet()) {
            PolicyTargetGroupRespVO group = new PolicyTargetGroupRespVO();
            group.setTargetValue(entry.getKey());
            group.setUnit(entry.getValue().get(0).getUnit());

            Set<Long> relatedIds = entry.getValue().stream()
                    .map(DealerPolicyIndicatorDO::getPolicyId).collect(Collectors.toSet());
            group.setPolicyCount(relatedIds.size());

            List<PolicyTargetGroupRespVO.PolicyBriefVO> briefs = new ArrayList<>();
            for (Long pid : relatedIds) {
                DealerPolicyDO p = policyMap.get(pid);
                if (p != null) {
                    PolicyTargetGroupRespVO.PolicyBriefVO brief = new PolicyTargetGroupRespVO.PolicyBriefVO();
                    brief.setId(p.getId());
                    brief.setPolicyCode(p.getPolicyCode());
                    brief.setPolicyName(p.getPolicyName());
                    brief.setDealerName(dealerNameMap.getOrDefault(p.getDealerId(), ""));
                    brief.setProductLineName(p.getProductLineName());
                    brief.setPolicyType(p.getPolicyType());
                    brief.setAchievementType(p.getAchievementType());
                    brief.setPolicyStatus(p.getPolicyStatus());
                    brief.setStartDate(p.getStartDate());
                    brief.setEndDate(p.getEndDate());
                    // Add target/achieved values from first indicator of this policy in this group
                    DealerPolicyIndicatorDO firstInd = entry.getValue().stream()
                            .filter(ind -> ind.getPolicyId().equals(pid))
                            .findFirst().orElse(null);
                    if (firstInd != null) {
                        brief.setTargetValue(firstInd.getTargetValue());
                        brief.setAchievedValue(firstInd.getAchievedValue());
                        brief.setUnit(firstInd.getUnit());
                    }
                    briefs.add(brief);
                }
            }
            group.setPolicies(briefs);
            result.add(group);
        }

        // 按目标值降序
        result.sort((a, b) -> b.getTargetValue().compareTo(a.getTargetValue()));
        return result;
    }

    @Override
    public PolicyIndicatorChartRespVO getIndicatorChart(PolicyKpiPanelPageReqVO reqVO) {
        // 与 kpi-panels 类似，但只返回单个指标
        LambdaQueryWrapperX<DealerPolicyDO> policyWrapper = buildPolicyWrapper(reqVO);
        List<DealerPolicyDO> policies = dealerPolicyMapper.selectList(policyWrapper);
        if (CollUtil.isEmpty(policies)) {
            return new PolicyIndicatorChartRespVO();
        }

        Set<Long> policyIds = policies.stream().map(DealerPolicyDO::getId).collect(Collectors.toSet());
        Map<Long, DealerPolicyDO> policyMap = policies.stream()
                .collect(Collectors.toMap(DealerPolicyDO::getId, p -> p));

        List<DealerPolicyIndicatorDO> indicators = indicatorMapper.selectList(
                new LambdaQueryWrapperX<DealerPolicyIndicatorDO>()
                        .in(DealerPolicyIndicatorDO::getPolicyId, policyIds)
                        .eq(DealerPolicyIndicatorDO::getTargetYear, reqVO.getTargetYear())
                        .eq(DealerPolicyIndicatorDO::getIndicatorName, reqVO.getIndicatorName()));

        List<PolicyKpiPanelRespVO.PeriodVO> periods = aggregatePeriods(indicators, policyMap);

        PolicyIndicatorChartRespVO chart = new PolicyIndicatorChartRespVO();
        chart.setIndicatorName(reqVO.getIndicatorName());
        chart.setTimeLabels(periods.stream().map(PolicyKpiPanelRespVO.PeriodVO::getLabel).collect(Collectors.toList()));
        chart.setBars(periods.stream().map(p -> {
            PolicyIndicatorChartRespVO.BarVO bar = new PolicyIndicatorChartRespVO.BarVO();
            bar.setAchieved(p.getAchieved());
            bar.setTarget(p.getTarget());
            bar.setRate(p.getRate());
            return bar;
        }).collect(Collectors.toList()));
        return chart;
    }

    @Override
    public List<PolicyTargetGroupRespVO> getTargetDistribution(PolicyKpiPanelPageReqVO reqVO) {
        LambdaQueryWrapperX<DealerPolicyDO> policyWrapper = buildPolicyWrapper(reqVO);
        List<DealerPolicyDO> policies = dealerPolicyMapper.selectList(policyWrapper);
        if (CollUtil.isEmpty(policies)) {
            return Collections.emptyList();
        }

        Set<Long> policyIds = policies.stream().map(DealerPolicyDO::getId).collect(Collectors.toSet());
        Map<Long, DealerPolicyDO> policyMap = policies.stream()
                .collect(Collectors.toMap(DealerPolicyDO::getId, p -> p));

        List<DealerPolicyIndicatorDO> indicators = indicatorMapper.selectList(
                new LambdaQueryWrapperX<DealerPolicyIndicatorDO>()
                        .in(DealerPolicyIndicatorDO::getPolicyId, policyIds)
                        .eq(DealerPolicyIndicatorDO::getTargetYear, reqVO.getTargetYear())
                        .eqIfPresent(DealerPolicyIndicatorDO::getIndicatorName, reqVO.getIndicatorName()));

        // 按 targetValue 分组
        Map<BigDecimal, List<DealerPolicyIndicatorDO>> grouped = indicators.stream()
                .collect(Collectors.groupingBy(DealerPolicyIndicatorDO::getTargetValue));

        // 填充经销商名称
        Set<Long> dealerIds = policies.stream().map(DealerPolicyDO::getDealerId).collect(Collectors.toSet());
        Map<Long, String> dealerNameMap = new HashMap<>();
        for (Long dealerId : dealerIds) {
            DealerInfoDO dealer = dealerInfoService.getDealer(dealerId);
            dealerNameMap.put(dealerId, dealer != null ? dealer.getDealerName() : "");
        }

        List<PolicyTargetGroupRespVO> result = new ArrayList<>();
        for (Map.Entry<BigDecimal, List<DealerPolicyIndicatorDO>> entry : grouped.entrySet()) {
            PolicyTargetGroupRespVO group = new PolicyTargetGroupRespVO();
            group.setTargetValue(entry.getKey());

            // G1: Fill unit from first indicator in group
            group.setUnit(entry.getValue().get(0).getUnit());

            Set<Long> relatedIds = entry.getValue().stream()
                    .map(DealerPolicyIndicatorDO::getPolicyId).collect(Collectors.toSet());
            group.setPolicyCount(relatedIds.size());

            List<PolicyTargetGroupRespVO.PolicyBriefVO> briefs = new ArrayList<>();
            for (Long pid : relatedIds) {
                DealerPolicyDO p = policyMap.get(pid);
                if (p != null) {
                    PolicyTargetGroupRespVO.PolicyBriefVO brief = new PolicyTargetGroupRespVO.PolicyBriefVO();
                    brief.setId(p.getId());
                    brief.setPolicyCode(p.getPolicyCode());
                    brief.setPolicyName(p.getPolicyName());
                    brief.setDealerName(dealerNameMap.getOrDefault(p.getDealerId(), ""));
                    brief.setProductLineName(p.getProductLineName());
                    // G3: Add more fields to PolicyBriefVO
                    brief.setPolicyType(p.getPolicyType());
                    brief.setAchievementType(p.getAchievementType());
                    brief.setPolicyStatus(p.getPolicyStatus());
                    brief.setStartDate(p.getStartDate());
                    brief.setEndDate(p.getEndDate());
                    // G3: Add target/achieved values from first indicator of this policy in this group
                    DealerPolicyIndicatorDO firstInd = entry.getValue().stream()
                            .filter(ind -> ind.getPolicyId().equals(pid))
                            .findFirst().orElse(null);
                    if (firstInd != null) {
                        brief.setTargetValue(firstInd.getTargetValue());
                        brief.setAchievedValue(firstInd.getAchievedValue());
                        brief.setUnit(firstInd.getUnit());
                    }
                    briefs.add(brief);
                }
            }
            group.setPolicies(briefs);
            result.add(group);
        }

        // 按目标值降序
        result.sort((a, b) -> b.getTargetValue().compareTo(a.getTargetValue()));
        return result;
    }

    @Override
    public List<PolicyAchievementRespVO> getAchievementDrilldown(Integer targetYear, Long indicatorId, String level,
                                                                   String province, String hospital) {
        List<DealerPolicyAchievementDO> list = achievementMapper.selectDrilldown(targetYear, indicatorId, level, province, hospital);

        List<PolicyAchievementRespVO> result = new ArrayList<>();
        for (DealerPolicyAchievementDO item : list) {
            PolicyAchievementRespVO vo = new PolicyAchievementRespVO();
            switch (level) {
                case "province":
                    vo.setName(item.getProvince());
                    break;
                case "hospital":
                    vo.setName(item.getHospital());
                    break;
                case "product":
                    vo.setName(item.getProductName());
                    break;
            }
            vo.setAchievedValue(item.getAchievedValue());
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<String> getIndicatorNames(Integer targetYear) {
        return indicatorMapper.selectDistinctIndicatorNames(targetYear);
    }

    @Override
    public List<DealerPolicyDO> getPoliciesByIndicator(String indicatorName, Integer targetYear) {
        // G4: Query policies by indicator name and year
        List<DealerPolicyIndicatorDO> indicators = indicatorMapper.selectList(
                new LambdaQueryWrapperX<DealerPolicyIndicatorDO>()
                        .eq(DealerPolicyIndicatorDO::getIndicatorName, indicatorName)
                        .eq(DealerPolicyIndicatorDO::getTargetYear, targetYear));
        if (CollUtil.isEmpty(indicators)) {
            return Collections.emptyList();
        }
        Set<Long> policyIds = indicators.stream()
                .map(DealerPolicyIndicatorDO::getPolicyId)
                .collect(Collectors.toSet());
        return dealerPolicyMapper.selectList(
                new LambdaQueryWrapperX<DealerPolicyDO>()
                        .in(DealerPolicyDO::getId, policyIds));
    }

    // ========== 辅助方法 ==========

    private void insertIndicators(DealerPolicyDO policyDO, List<DealerPolicySaveReqVO.IndicatorSaveVO> indicators) {
        if (CollUtil.isEmpty(indicators)) {
            return;
        }
        for (DealerPolicySaveReqVO.IndicatorSaveVO ind : indicators) {
            DealerPolicyIndicatorDO indicatorDO = BeanUtils.toBean(ind, DealerPolicyIndicatorDO.class);
            indicatorDO.setPolicyId(policyDO.getId());
            indicatorDO.setPolicyCode(policyDO.getPolicyCode());
            if (indicatorDO.getAchievedValue() == null) {
                indicatorDO.setAchievedValue(BigDecimal.ZERO);
            }
            indicatorMapper.insert(indicatorDO);
        }
    }

    private String generatePolicyCode() {
        int year = LocalDate.now().getYear();
        Integer maxSeq = dealerPolicyMapper.selectMaxSeq(year);
        int nextSeq = (maxSeq != null ? maxSeq : 0) + 1;
        return String.format("POL-%d-%03d", year, nextSeq);
    }

    /**
     * 按时间维度聚合指标数据
     * <p>
     * 展示规则：
     * - 仅季度政策：Q1、Q2、Q3、Q4
     * - 仅月度政策：1月～12月
     * - 混合：统一按月份展示，季度数据按 Q1→3月、Q2→6月、Q3→9月、Q4→12月聚合
     */
    private List<PolicyKpiPanelRespVO.PeriodVO> aggregatePeriods(List<DealerPolicyIndicatorDO> indicators,
                                                                   Map<Long, DealerPolicyDO> policyMap) {
        // 判断类型组成
        boolean hasQuarter = false;
        boolean hasMonth = false;
        for (DealerPolicyIndicatorDO ind : indicators) {
            DealerPolicyDO policy = policyMap.get(ind.getPolicyId());
            if (policy != null) {
                if ("quarter".equals(policy.getAchievementType())) {
                    hasQuarter = true;
                } else {
                    hasMonth = true;
                }
            }
        }

        // 按 label 聚合：label -> [achieved, target]
        Map<String, BigDecimal[]> aggregated = new LinkedHashMap<>();

        if (hasQuarter && !hasMonth) {
            // 仅季度：固定 Q1-Q4
            for (int q = 1; q <= 4; q++) {
                aggregated.put("Q" + q, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            }
            for (DealerPolicyIndicatorDO ind : indicators) {
                int quarter = (ind.getTargetMonth() - 1) / 3 + 1;
                String label = "Q" + quarter;
                BigDecimal[] agg = aggregated.get(label);
                agg[0] = agg[0].add(ind.getAchievedValue() != null ? ind.getAchievedValue() : BigDecimal.ZERO);
                agg[1] = agg[1].add(ind.getTargetValue() != null ? ind.getTargetValue() : BigDecimal.ZERO);
            }
        } else if (hasMonth && !hasQuarter) {
            // 仅月度：固定 1-12 月
            for (int i = 1; i <= 12; i++) {
                aggregated.put(i + "月", new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            }
            for (DealerPolicyIndicatorDO ind : indicators) {
                String label = ind.getTargetMonth() + "月";
                BigDecimal[] agg = aggregated.get(label);
                agg[0] = agg[0].add(ind.getAchievedValue() != null ? ind.getAchievedValue() : BigDecimal.ZERO);
                agg[1] = agg[1].add(ind.getTargetValue() != null ? ind.getTargetValue() : BigDecimal.ZERO);
            }
        } else {
            // 混合：统一按月份，Q1→3月, Q2→6月, Q3→9月, Q4→12月
            for (int i = 1; i <= 12; i++) {
                aggregated.put(i + "月", new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            }
            for (DealerPolicyIndicatorDO ind : indicators) {
                DealerPolicyDO policy = policyMap.get(ind.getPolicyId());
                int month;
                if (policy != null && "quarter".equals(policy.getAchievementType())) {
                    month = ((ind.getTargetMonth() - 1) / 3 + 1) * 3;
                } else {
                    month = ind.getTargetMonth();
                }
                String label = month + "月";
                BigDecimal[] agg = aggregated.get(label);
                agg[0] = agg[0].add(ind.getAchievedValue() != null ? ind.getAchievedValue() : BigDecimal.ZERO);
                agg[1] = agg[1].add(ind.getTargetValue() != null ? ind.getTargetValue() : BigDecimal.ZERO);
            }
        }

        // 构建结果
        List<PolicyKpiPanelRespVO.PeriodVO> result = new ArrayList<>();
        for (Map.Entry<String, BigDecimal[]> entry : aggregated.entrySet()) {
            BigDecimal[] agg = entry.getValue();

            PolicyKpiPanelRespVO.PeriodVO period = new PolicyKpiPanelRespVO.PeriodVO();
            period.setLabel(entry.getKey());
            period.setAchieved(agg[0]);
            period.setTarget(agg[1]);
            // 计算达成率
            if (agg[1].compareTo(BigDecimal.ZERO) != 0) {
                period.setRate(agg[0].multiply(BigDecimal.valueOf(100))
                        .divide(agg[1], 1, RoundingMode.HALF_UP));
            } else {
                period.setRate(BigDecimal.ZERO);
            }
            result.add(period);
        }
        return result;
    }

    private LambdaQueryWrapperX<DealerPolicyDO> buildPolicyWrapper(PolicyKpiPanelPageReqVO reqVO) {
        LambdaQueryWrapperX<DealerPolicyDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.inIfPresent(DealerPolicyDO::getPolicyType, parseStrList(reqVO.getPolicyTypes()));
        wrapper.inIfPresent(DealerPolicyDO::getAchievementType, parseStrList(reqVO.getAchievementTypes()));
        wrapper.inIfPresent(DealerPolicyDO::getProductLineCode, parseStrList(reqVO.getProductLineCodes()));
        wrapper.inIfPresent(DealerPolicyDO::getDealerId, parseLongList(reqVO.getDealerIds()));
        if (StrUtil.isNotBlank(reqVO.getPolicyCode())) {
            wrapper.like(DealerPolicyDO::getPolicyCode, reqVO.getPolicyCode());
        }
        return wrapper;
    }

    private List<String> parseStrList(String commaSeparated) {
        if (StrUtil.isBlank(commaSeparated)) return null;
        return Arrays.asList(commaSeparated.split(","));
    }

    private List<Integer> parseIntList(String commaSeparated) {
        if (StrUtil.isBlank(commaSeparated)) return null;
        return Arrays.stream(commaSeparated.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    private List<Long> parseLongList(String commaSeparated) {
        if (StrUtil.isBlank(commaSeparated)) return null;
        return Arrays.stream(commaSeparated.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

}
