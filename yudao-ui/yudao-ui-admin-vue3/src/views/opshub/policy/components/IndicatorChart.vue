<template>
  <div v-loading="loading" class="indicator-chart-wrap">
    <template v-if="panels && panels.length">
      <div v-for="panel in panels" :key="panel.indicatorName" class="indicator-card">
        <!-- 标题行 -->
        <div class="indicator-header">
          <span class="indicator-name">{{ panel.indicatorName }}</span>
          <el-tag size="small" type="info">{{ panel.unit }}</el-tag>
          <span class="policy-count-text">
            (
            <span class="policy-count-link" @click="handlePolicyCountClick(panel)">
              {{ panel.policyCount }}
            </span>
            条政策 · {{ getDimensionText(panel) }})
          </span>
        </div>

        <!-- ECharts 柱状图 -->
        <div :ref="(el) => setChartRef(panel.indicatorName, el as HTMLElement)" class="chart-container"></div>

        <!-- 目标分布药丸行 -->
        <TargetDistribution
          v-if="panel.targetGroups && panel.targetGroups.length"
          :groups="panel.targetGroups"
          :loading="false"
          @pill-click="(group) => emit('pill-click', group)"
        />
      </div>
    </template>
    <el-empty v-else description="暂无指标数据" />
  </div>
</template>

<script lang="ts" setup>
import * as echarts from 'echarts'
import type { KpiPanelVO, TargetGroupVO } from '@/api/opshub/policy'
import TargetDistribution from './TargetDistribution.vue'

const props = defineProps<{
  panels: KpiPanelVO[]
  loading: boolean
}>()

const emit = defineEmits<{
  (e: 'policy-count-click', panel: KpiPanelVO): void
  (e: 'pill-click', group: TargetGroupVO): void
}>()

// Chart refs: indicatorName -> HTMLElement
const chartRefs: Record<string, HTMLElement> = {}
const chartInstances: Record<string, echarts.ECharts> = {}

const setChartRef = (name: string, el: HTMLElement | null) => {
  if (el) {
    chartRefs[name] = el
  }
}

const getDimensionText = (panel: KpiPanelVO) => {
  const hasQuarter = panel.periods.some((p) => p.label.startsWith('Q'))
  return hasQuarter ? '全年4季度' : '全年12个月'
}

const getBarColor = (rate: number) => {
  if (rate >= 100) return '#67C23A'
  return '#F56C6C'
}

const renderChart = (panel: KpiPanelVO) => {
  const el = chartRefs[panel.indicatorName]
  if (!el) return

  // Dispose existing instance
  if (chartInstances[panel.indicatorName]) {
    chartInstances[panel.indicatorName].dispose()
  }

  const chart = echarts.init(el)
  chartInstances[panel.indicatorName] = chart

  const unit = panel.unit
  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const idx = params[0]?.dataIndex
        if (idx == null || !panel.periods[idx]) return ''
        const p = panel.periods[idx]
        return `${p.label}<br/>
                达成: ${p.achieved?.toLocaleString()}${unit}<br/>
                目标: ${p.target?.toLocaleString()}${unit}<br/>
                达成率: ${p.rate}%`
      }
    },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '20%', containLabel: true },
    xAxis: {
      type: 'category',
      data: panel.periods.map((p) => p.label),
      axisLabel: { fontSize: 11 }
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '达成值',
        type: 'bar',
        data: panel.periods.map((p) => ({
          value: p.achieved,
          target: p.target,
          rate: p.rate,
          unit: unit,
          itemStyle: { color: getBarColor(p.rate) }
        })),
        barMaxWidth: 44,
        label: {
          show: true,
          position: 'top',
          formatter: (params: any) => {
            const d = params.data
            return `${d.value}${d.unit}\n满点 ${d.target}${d.unit}`
          },
          fontSize: 11,
          lineHeight: 14,
          color: '#303133'
        },
        markLine: {
          silent: true,
          symbol: 'none',
          lineStyle: { type: 'dashed', color: '#909399' },
          data: []
        }
      }
    ],
    // Add bottom label for achievement rate
    graphic: panel.periods.map((_p) => ({
      type: 'text',
      left: 'center',
      bottom: 4,
      style: {
        text: `达成率 ${_p.rate}%`,
        fill: _p.rate >= 100 ? '#67C23A' : '#F56C6C',
        fontSize: 10,
        fontWeight: 'bold'
      },
      z: 100
    }))
  }

  // For multi-bar scenarios, use a simpler approach with axis label for rate
  // Remove graphic and use xAxis axisLabel instead
  option.graphic = []

  chart.setOption(option, true)

  // Add custom rendering for bottom rate labels via axisLabel
  chart.setOption({
    xAxis: {
      type: 'category',
      data: panel.periods.map((p) => p.label),
      axisLabel: {
        formatter: (value: string, index: number) => {
          const p = panel.periods[index]
          if (!p) return value
          return [
            `{name|${value}}`,
            `{rate|达成率 ${p.rate}%}`
          ].join('\n')
        },
        rich: {
          name: { fontSize: 12, fontWeight: 'bold', color: '#303133', lineHeight: 16 },
          rate: { fontSize: 10, fontWeight: 'bold', color: '#F56C6C', lineHeight: 14 }
        }
      }
    }
  }, false)
}

const handlePolicyCountClick = (panel: KpiPanelVO) => {
  emit('policy-count-click', panel)
}

// Watch for panel changes and re-render
watch(
  () => props.panels,
  (newPanels) => {
    nextTick(() => {
      newPanels.forEach((panel) => renderChart(panel))
    })
  },
  { deep: true, immediate: true }
)

// Resize handler
const handleResize = () => {
  Object.values(chartInstances).forEach((chart) => chart.resize())
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  Object.values(chartInstances).forEach((chart) => chart.dispose())
})
</script>

<style scoped>
.indicator-chart-wrap {
  min-height: 200px;
}

.indicator-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.indicator-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.indicator-name {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
}

.policy-count-text {
  font-size: 13px;
  color: #909399;
}

.policy-count-link {
  color: #409eff;
  cursor: pointer;
  text-decoration: underline;
  font-weight: 600;
}

.policy-count-link:hover {
  color: #66b1ff;
}

.chart-container {
  height: 320px;
  width: 100%;
}
</style>
