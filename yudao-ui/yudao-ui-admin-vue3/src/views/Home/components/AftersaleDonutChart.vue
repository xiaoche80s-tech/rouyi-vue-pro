<template>
  <el-card shadow="hover" :body-style="{ padding: '16px' }">
    <template #header>
      <span class="text-15px font-600">售后进度概览</span>
    </template>
    <div ref="chartRef" style="width: 100%; height: 320px"></div>
  </el-card>
</template>

<script lang="ts" setup>
import * as echarts from 'echarts/core'
import { PieChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent, GraphicComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([PieChart, TooltipComponent, LegendComponent, GraphicComponent, CanvasRenderer])

defineOptions({ name: 'AftersaleDonutChart' })

const props = defineProps<{
  statistics?: {
    totalCount: number
    refundCount: number
    returnCount: number
    completedCount: number
    inProgressCount: number
  }
}>()

const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

const renderChart = () => {
  if (!chartRef.value || !props.statistics) return
  if (!chart) chart = echarts.init(chartRef.value)

  const s = props.statistics
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0 },
    graphic: {
      type: 'text',
      left: 'center',
      top: '38%',
      style: {
        text: `${s.totalCount}\n总售后`,
        textAlign: 'center',
        fontSize: 16,
        fontWeight: 'bold',
        fill: '#333'
      }
    },
    series: [
      {
        type: 'pie',
        radius: ['50%', '75%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        label: { show: false },
        itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
        data: [
          { value: s.completedCount, name: '已完成', itemStyle: { color: '#10b981' } },
          { value: s.inProgressCount, name: '处理中', itemStyle: { color: '#3b82f6' } },
          { value: s.refundCount, name: '退款', itemStyle: { color: '#f59e0b' } },
          { value: s.returnCount, name: '退货', itemStyle: { color: '#ef4444' } }
        ]
      }
    ]
  })
}

watch(() => props.statistics, renderChart, { deep: true })
onMounted(() => nextTick(renderChart))
onBeforeUnmount(() => chart?.dispose())
</script>
