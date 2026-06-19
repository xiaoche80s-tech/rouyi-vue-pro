<template>
  <el-card shadow="hover" :body-style="{ padding: '16px' }">
    <template #header>
      <span class="text-15px font-600">签约趋势</span>
    </template>
    <div ref="chartRef" style="width: 100%; height: 320px"></div>
  </el-card>
</template>

<script lang="ts" setup>
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([LineChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

defineOptions({ name: 'SigningTrendChart' })

const props = defineProps<{
  data?: { period: string; mainCount: number; policyCount: number; supplementCount: number; terminationCount: number; totalCount: number }[]
}>()

const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

const renderChart = () => {
  if (!chartRef.value || !props.data?.length) return
  if (!chart) chart = echarts.init(chartRef.value)

  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['主合同', '政策合同', '补充协议', '终止协议', '合计'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '14%', containLabel: true },
    xAxis: { type: 'category', data: props.data.map(d => d.period), boundaryGap: false },
    yAxis: { type: 'value' },
    series: [
      { name: '主合同', type: 'line', smooth: true, data: props.data.map(d => d.mainCount), itemStyle: { color: '#7c3aed' } },
      { name: '政策合同', type: 'line', smooth: true, data: props.data.map(d => d.policyCount), itemStyle: { color: '#2563eb' } },
      { name: '补充协议', type: 'line', smooth: true, data: props.data.map(d => d.supplementCount), itemStyle: { color: '#f59e0b' } },
      { name: '终止协议', type: 'line', smooth: true, data: props.data.map(d => d.terminationCount), itemStyle: { color: '#ef4444' } },
      { name: '合计', type: 'line', smooth: true, data: props.data.map(d => d.totalCount), lineStyle: { width: 3 }, itemStyle: { color: '#10b981' } }
    ]
  })
}

watch(() => props.data, renderChart, { deep: true })
onMounted(() => nextTick(renderChart))
onBeforeUnmount(() => chart?.dispose())
</script>
