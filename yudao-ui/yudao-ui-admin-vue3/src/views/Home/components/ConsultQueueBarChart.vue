<template>
  <el-card shadow="hover" :body-style="{ padding: '16px' }">
    <template #header>
      <span class="text-15px font-600">咨询队列状态</span>
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

defineOptions({ name: 'ConsultQueueBarChart' })

const props = defineProps<{
  statistics?: {
    pendingCount: number
    processingCount: number
    completedCount: number
  }
}>()

const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

const renderChart = () => {
  if (!chartRef.value || !props.statistics) return
  if (!chart) chart = echarts.init(chartRef.value)

  const s = props.statistics
  const categories = ['待处理', '处理中', '已完成']
  const values = [s.pendingCount, s.processingCount, s.completedCount]
  const colors = ['#f59e0b', '#3b82f6', '#10b981']

  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '8%', bottom: '3%', top: '8%', containLabel: true },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: categories, axisLabel: { fontSize: 13 } },
    series: [
      {
        type: 'bar',
        barWidth: '50%',
        data: values.map((v, i) => ({
          value: v,
          itemStyle: { color: colors[i], borderRadius: [0, 4, 4, 0] }
        })),
        label: { show: true, position: 'right', fontSize: 13, fontWeight: 'bold' }
      }
    ]
  })
}

watch(() => props.statistics, renderChart, { deep: true })
onMounted(() => nextTick(renderChart))
onBeforeUnmount(() => chart?.dispose())
</script>
