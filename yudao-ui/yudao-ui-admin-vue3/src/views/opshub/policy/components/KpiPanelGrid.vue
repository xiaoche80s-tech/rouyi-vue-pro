<template>
  <div v-loading="loading">
    <div v-for="panel in panels" :key="panel.indicatorName" class="mb-16px">
      <KpiPanel
        :panel="panel"
        @policy-count-click="handlePolicyCountClick"
        @bar-click="handleBarClick"
      />
    </div>
    <el-empty v-if="!loading && panels.length === 0" description="暂无 KPI 面板数据" />
  </div>
</template>

<script lang="ts" setup>
import type { KpiPanelVO } from '@/api/opshub/policy'
import KpiPanel from './KpiPanel.vue'

defineProps<{
  panels: KpiPanelVO[]
  loading: boolean
}>()

const emit = defineEmits<{
  (e: 'policy-count-click', panel: KpiPanelVO): void
  (e: 'bar-click', data: { indicatorName: string; label: string }): void
}>()

const handlePolicyCountClick = (panel: KpiPanelVO) => {
  emit('policy-count-click', panel)
}

const handleBarClick = (data: { indicatorName: string; label: string }) => {
  emit('bar-click', data)
}
</script>
