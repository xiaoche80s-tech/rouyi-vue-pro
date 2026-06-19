<template>
  <el-card shadow="hover" :body-style="{ padding: '16px' }">
    <template #header>
      <span class="text-15px font-600">签约类型分布</span>
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

defineOptions({ name: 'SigningPieChart' })

const props = defineProps<{
  statistics?: {
    mainCount: number
    policyCount: number
    supplementCount: number
    terminationCount: number
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
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { show: true, formatter: '{b}\n{c}' },
        data: [
          { value: s.mainCount, name: '主合同', itemStyle: { color: '#7c3aed' } },
          { value: s.policyCount, name: '政策合同', itemStyle: { color: '#2563eb' } },
          { value: s.supplementCount, name: '补充协议', itemStyle: { color: '#f59e0b' } },
          { value: s.terminationCount, name: '终止协议', itemStyle: { color: '#ef4444' } }
        ]
      }
    ]
  })
}

watch(() => props.statistics, renderChart, { deep: true })
onMounted(() => nextTick(renderChart))
onBeforeUnmount(() => chart?.dispose())
</script>
