<template>
  <el-card shadow="hover" :body-style="{ padding: '16px' }">
    <template #header>
      <span class="text-15px font-600">订单状态概览</span>
    </template>
    <div ref="chartRef" style="width: 100%; height: 320px"></div>
  </el-card>
</template>

<script lang="ts" setup>
import * as echarts from 'echarts/core'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([BarChart, GridComponent, TooltipComponent, CanvasRenderer])

defineOptions({ name: 'OrderBarChart' })

const props = defineProps<{
  statistics?: {
    completedCount: number
    paidCount: number
    unpaidCount: number
    invoicedCount: number
    uninvoicedCount: number
  }
}>()

const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

const renderChart = () => {
  if (!chartRef.value || !props.statistics) return
  if (!chart) chart = echarts.init(chartRef.value)

  const s = props.statistics
  const categories = ['已完成', '已付款', '待付款', '已开票', '未开票']
  const values = [s.completedCount, s.paidCount, s.unpaidCount, s.invoicedCount, s.uninvoicedCount]
  const colors = ['#10b981', '#3b82f6', '#f59e0b', '#8b5cf6', '#ef4444']

  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: categories, axisLabel: { interval: 0 } },
    yAxis: { type: 'value' },
    series: [
      {
        type: 'bar',
        barWidth: '45%',
        data: values.map((v, i) => ({ value: v, itemStyle: { color: colors[i], borderRadius: [4, 4, 0, 0] } }))
      }
    ]
  })
}

watch(() => props.statistics, renderChart, { deep: true })
onMounted(() => nextTick(renderChart))
onBeforeUnmount(() => chart?.dispose())
</script>
