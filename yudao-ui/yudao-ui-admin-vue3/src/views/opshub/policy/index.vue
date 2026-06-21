<template>
  <!-- 筛选栏 -->
  <PolicyFilterBar
    :dealer-list="dealerList"
    :product-line-list="productLineList"
    :indicator-name-options="indicatorNameOptions"
    @search="handleSearch"
    @reset="handleReset"
  />

  <!-- 指标柱状图（含内嵌目标分布） -->
  <IndicatorChart
    :panels="kpiPanels"
    :loading="kpiLoading"
    @policy-count-click="handlePolicyCountClick"
    @pill-click="handlePillClick"
  />

  <!-- 政策列表弹窗（双样式） -->
  <PolicyListModal
    ref="policyListModalRef"
    @card-click="handleCardClick"
  />

  <!-- 政策详情弹窗 -->
  <PolicyDetailModal
    ref="policyDetailModalRef"
    @achievement-click="handleAchievementClick"
  />

  <!-- 达成明细下钻弹窗 -->
  <AchievementDrilldown ref="achievementDrilldownRef" />
</template>

<script lang="ts" setup>
import * as PolicyApi from '@/api/opshub/policy'
import * as DealerApi from '@/api/opshub/dealer'
import type { KpiPanelVO, TargetGroupVO, IndicatorVO } from '@/api/opshub/policy'
import PolicyFilterBar from './components/PolicyFilterBar.vue'
import IndicatorChart from './components/IndicatorChart.vue'
import PolicyListModal from './components/PolicyListModal.vue'
import PolicyDetailModal from './components/PolicyDetailModal.vue'
import AchievementDrilldown from './components/AchievementDrilldown.vue'

defineOptions({ name: 'OpshubPolicy' })

const currentYear = new Date().getFullYear()

// ========== 数据 ==========
const kpiPanels = ref<KpiPanelVO[]>([])
const kpiLoading = ref(false)
const indicatorNameOptions = ref<string[]>([])
const dealerList = ref<any[]>([])
const productLineList = ref<any[]>([])
const currentFilterParams = ref<any>({})

// ========== Refs ==========
const policyListModalRef = ref()
const policyDetailModalRef = ref()
const achievementDrilldownRef = ref()

// ========== 方法 ==========

const buildApiParams = (filters: any) => {
  const params: any = {}
  params.targetYear = filters.targetYear ?? currentYear
  if (filters.policyTypes?.length) params.policyTypes = filters.policyTypes.join(',')
  if (filters.achievementTypes?.length) params.achievementTypes = filters.achievementTypes.join(',')
  if (filters.indicatorNames?.length) params.indicatorNames = filters.indicatorNames.join(',')
  if (filters.months?.length) params.months = filters.months.join(',')
  if (filters.productLineCodes?.length) params.productLineCodes = filters.productLineCodes.join(',')
  if (filters.dealerIds?.length) params.dealerIds = filters.dealerIds.join(',')
  if (filters.policyCode) params.policyCode = filters.policyCode
  return params
}

const loadData = async (filters: any) => {
  const params = buildApiParams(filters)
  currentFilterParams.value = params

  // 同步刷新指标名称下拉（按当前年度）
  try {
    indicatorNameOptions.value = await PolicyApi.getIndicatorNames(params.targetYear)
  } catch (e) {
    console.error('加载指标名称失败', e)
  }

  // 加载 KPI 面板（含内嵌目标分布）
  kpiLoading.value = true
  try {
    kpiPanels.value = await PolicyApi.getKpiPanels(params)
  } finally {
    kpiLoading.value = false
  }
}

const handleSearch = (filters: any) => {
  loadData(filters)
}

const handleReset = () => {
  currentFilterParams.value = {}
  loadData({ targetYear: currentYear })
}

// ========== 事件处理 ==========

// H4: 点击标题政策数量 → 打开样式A弹窗
const handlePolicyCountClick = (panel: KpiPanelVO) => {
  policyListModalRef.value.openByIndicator(
    panel.indicatorName,
    currentFilterParams.value.targetYear ?? currentYear
  )
}

// H4: 点击目标分布药丸 → 打开样式B弹窗
const handlePillClick = (group: TargetGroupVO) => {
  policyListModalRef.value.openByTargetGroup(group)
}

// H4: 点击卡片 → 打开政策详情
const handleCardClick = (policyId: number) => {
  policyDetailModalRef.value.open(policyId)
}

const handleAchievementClick = (indicator: IndicatorVO) => {
  // 点击政策详情中的达成值 → 打开达成明细下钻弹窗
  achievementDrilldownRef.value.open(indicator, currentFilterParams.value.targetYear ?? currentYear)
}

// ========== 初始化 ==========
onMounted(async () => {
  // 加载经销商列表
  try {
    dealerList.value = await DealerApi.getSimpleDealerList()
  } catch (e) {
    console.error('加载经销商列表失败', e)
  }

  // 加载产品线列表
  try {
    const plList = await DealerApi.getSimpleProductLineList()
    productLineList.value = plList.map((pl: any) => ({
      productLineCode: pl.productLineCode,
      productLineName: pl.productLineName
    }))
  } catch (e) {
    console.error('加载产品线列表失败', e)
  }

  // 初始加载数据（会同步刷新指标名称）
  loadData({ targetYear: currentYear })
})
</script>
