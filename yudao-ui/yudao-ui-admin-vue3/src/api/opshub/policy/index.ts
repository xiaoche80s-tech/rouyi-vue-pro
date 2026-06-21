import request from '@/config/axios'

// ========== 类型定义 ==========

// 政策列表 VO
export interface DealerPolicyVO {
  id: number
  dealerId: number
  dealerCode: string
  dealerName: string
  productLineCode: string
  productLineName: string
  policyCode: string
  policyName: string
  policyType: string
  achievementType: string
  policyStatus: string
  startDate: string
  endDate: string
  contractCode: string
  contractName: string
  policyDesc: string
  remark: string
  createTime: Date
}

// 政策详情 VO
export interface DealerPolicyDetailVO extends DealerPolicyVO {
  indicators: IndicatorVO[]
}

export interface IndicatorVO {
  id: number
  indicatorName: string
  targetYear: number
  targetMonth: number
  targetValue: number
  achievedValue: number
  unit: string
  rate: number
}

// KPI 面板 VO
export interface KpiPanelVO {
  indicatorName: string
  unit: string
  policyCount: number
  periods: PeriodVO[]
  targetGroups?: TargetGroupVO[]
}

export interface PeriodVO {
  label: string
  achieved: number
  target: number
  rate: number
}

// 指标柱状图 VO
export interface IndicatorChartVO {
  indicatorName: string
  timeLabels: string[]
  bars: BarVO[]
}

export interface BarVO {
  achieved: number
  target: number
  rate: number
}

// 目标分布 VO
export interface TargetGroupVO {
  targetValue: number
  unit?: string
  policyCount: number
  policies: PolicyBriefVO[]
}

export interface PolicyBriefVO {
  id: number
  policyCode: string
  policyName: string
  dealerName: string
  productLineName: string
  policyType: string
  achievementType: string
  policyStatus: string
  startDate: string
  endDate: string
  targetValue: number
  achievedValue: number
  unit: string
}

// 达成明细 VO
export interface AchievementVO {
  name: string
  achievedValue: number
}

// ========== 政策 CRUD 接口 ==========

// 查询政策分页
export const getDealerPolicyPage = async (params: any) => {
  return await request.get({ url: '/opshub/dealer-policy/page', params })
}

// 查询政策详情
export const getDealerPolicy = async (id: number) => {
  return await request.get({ url: '/opshub/dealer-policy/get?id=' + id })
}

// 创建政策
export const createDealerPolicy = async (data: any) => {
  return await request.post({ url: '/opshub/dealer-policy/create', data })
}

// 更新政策
export const updateDealerPolicy = async (data: any) => {
  return await request.put({ url: '/opshub/dealer-policy/update', data })
}

// 删除政策
export const deleteDealerPolicy = async (id: number) => {
  return await request.delete({ url: '/opshub/dealer-policy/delete?id=' + id })
}

// ========== 看板查询接口 ==========

// KPI 面板数据
export const getKpiPanels = async (params: any) => {
  return await request.get({ url: '/opshub/dealer-policy/kpi-panels', params })
}

// 指标柱状图数据
export const getIndicatorChart = async (params: any) => {
  return await request.get({ url: '/opshub/dealer-policy/indicator-chart', params })
}

// 目标分布数据
export const getTargetDistribution = async (params: any) => {
  return await request.get({ url: '/opshub/dealer-policy/target-distribution', params })
}

// 达成明细下钻
export const getAchievementDrilldown = async (params: {
  targetYear: number
  indicatorId: number
  level: string
  province?: string
  hospital?: string
}) => {
  return await request.get({ url: '/opshub/dealer-policy/achievement-drilldown', params })
}

// 获取指标名称列表
export const getIndicatorNames = async (targetYear: number) => {
  return await request.get({ url: '/opshub/dealer-policy/indicator-names?targetYear=' + targetYear })
}

// G4: 按指标查询政策列表
export const getPoliciesByIndicator = async (indicatorName: string, targetYear: number) => {
  return await request.get({
    url: '/opshub/dealer-policy/policies-by-indicator',
    params: { indicatorName, targetYear }
  })
}

// ========== 达成明细 CRUD 接口 ==========

// 查询达成明细分页
export const getAchievementPage = async (params: any) => {
  return await request.get({ url: '/opshub/dealer-policy-achievement/page', params })
}

// 创建达成明细
export const createAchievement = async (data: any) => {
  return await request.post({ url: '/opshub/dealer-policy-achievement/create', data })
}

// 更新达成明细
export const updateAchievement = async (data: any) => {
  return await request.put({ url: '/opshub/dealer-policy-achievement/update', data })
}

// 删除达成明细
export const deleteAchievement = async (id: number) => {
  return await request.delete({ url: '/opshub/dealer-policy-achievement/delete?id=' + id })
}
