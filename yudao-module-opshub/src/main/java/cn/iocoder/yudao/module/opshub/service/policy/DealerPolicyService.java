package cn.iocoder.yudao.module.opshub.service.policy;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.opshub.controller.admin.policy.vo.*;
import cn.iocoder.yudao.module.opshub.dal.dataobject.policy.DealerPolicyDO;
import cn.iocoder.yudao.module.opshub.dal.dataobject.policy.DealerPolicyIndicatorDO;

import java.util.List;

/**
 * 经销商政策 Service 接口
 */
public interface DealerPolicyService {

    // ========== CRUD ==========

    /**
     * 创建政策（自动生成编码 + 指标）
     */
    Long createDealerPolicy(DealerPolicySaveReqVO reqVO);

    /**
     * 更新政策（全量替换指标）
     */
    void updateDealerPolicy(DealerPolicySaveReqVO reqVO);

    /**
     * 删除政策（级联删除指标）
     */
    void deleteDealerPolicy(Long id);

    /**
     * 获得政策详情
     */
    DealerPolicyDO getDealerPolicy(Long id);

    /**
     * 获得政策分页
     */
    PageResult<DealerPolicyDO> getDealerPolicyPage(DealerPolicyPageReqVO reqVO);

    /**
     * 获得政策下的指标列表
     */
    List<DealerPolicyIndicatorDO> getIndicatorsByPolicyId(Long policyId);

    // ========== 看板查询 ==========

    /**
     * KPI 面板数据
     */
    List<PolicyKpiPanelRespVO> getKpiPanels(PolicyKpiPanelPageReqVO reqVO);

    /**
     * 指标柱状图数据
     */
    PolicyIndicatorChartRespVO getIndicatorChart(PolicyKpiPanelPageReqVO reqVO);

    /**
     * 目标分布数据
     */
    List<PolicyTargetGroupRespVO> getTargetDistribution(PolicyKpiPanelPageReqVO reqVO);

    /**
     * 达成明细下钻
     */
    List<PolicyAchievementRespVO> getAchievementDrilldown(Integer targetYear, Long indicatorId, String level,
                                                           String province, String hospital);

    /**
     * 获取所有去重指标名称
     */
    List<String> getIndicatorNames(Integer targetYear);

    /**
     * G4: 按指标查询政策列表
     */
    List<DealerPolicyDO> getPoliciesByIndicator(String indicatorName, Integer targetYear);

}
