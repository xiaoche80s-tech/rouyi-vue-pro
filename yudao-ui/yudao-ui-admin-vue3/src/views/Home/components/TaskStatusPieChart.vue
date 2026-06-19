<template>
  <el-card shadow="hover" :body-style="{ padding: '16px' }">
    <template #header>
      <span class="text-15px font-600">工单状态分布</span>
    </template>
    <div ref="chartRef" style="width: 100%; height: 320px"></div>
  </el-card>
</template>

<script lang="ts" setup>
import * as echarts from 'echarts/core'
import { PieChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([PieChart, TooltipComponent, LegendComponent, CanvasRenderer])

defineOptions({ name: 'TaskStatusPieChart' })

const props = defineProps<{
  tabCounts?: Record<string, number>
}>()

const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

const labelMap: Record<string, string> = {
  pending: '待接单',
  claimable: '可申领',
  done: '已完成'
}
const colorMap: Record<string, string> = {
  pending: '#ef4444',
  claimable: '#f59e0b',
  done: '#9ca3af'
}

const renderChart = () => {
  if (!chartRef.value || !props.tabCounts) return
  if (!chart) chart = echarts.init(chartRef.value)

  const data = Object.entries(props.tabCounts)
    .filter(([key]) => key !== 'all')
    .map(([key, val]) => ({
      value: val,
      name: labelMap[key] || key,
      itemStyle: { color: colorMap[key] || '#9ca3af' }
    }))

  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0 },
    series: [
      {
        type: 'pie',
        radius: ['0%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
        label: { show: true, formatter: '{b}\n{c}' },
        data
      }
    ]
  })
}

watch(() => props.tabCounts, renderChart, { deep: true })
onMounted(() => nextTick(renderChart))
onBeforeUnmount(() => chart?.dispose())
</script>
