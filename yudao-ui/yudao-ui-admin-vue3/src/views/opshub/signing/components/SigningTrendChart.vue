<template>
  <ContentWrap>
    <div ref="chartRef" style="width: 100%; height: 360px"></div>
  </ContentWrap>
</template>

<script lang="ts" setup>
import * as SigningApi from '@/api/opshub/signing'
import * as echarts from 'echarts/core'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([BarChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null
const timeDimension = ref('month')

const loadTrend = async () => {
  try {
    const data = await SigningApi.getSigningTrend(timeDimension.value)
    renderChart(data)
  } catch (e) {
    console.error('加载趋势数据失败', e)
  }
}

const renderChart = (data: SigningApi.SigningTrendVO[]) => {
  if (!chartRef.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['主合同', '政策合同', '补充协议', '终止协议'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '10%' },
    xAxis: { type: 'category', data: data.map(d => d.period) },
    yAxis: { type: 'value' },
    series: [
      { name: '主合同', type: 'bar', stack: 'total', data: data.map(d => d.mainCount), itemStyle: { color: '#7c3aed' } },
      { name: '政策合同', type: 'bar', stack: 'total', data: data.map(d => d.policyCount), itemStyle: { color: '#2563eb' } },
      { name: '补充协议', type: 'bar', stack: 'total', data: data.map(d => d.supplementCount), itemStyle: { color: '#f59e0b' } },
      { name: '终止协议', type: 'bar', stack: 'total', data: data.map(d => d.terminationCount), itemStyle: { color: '#ef4444' } }
    ]
  })
}

onMounted(loadTrend)
onBeforeUnmount(() => chart?.dispose())

defineExpose({ refresh: loadTrend })
</script>
