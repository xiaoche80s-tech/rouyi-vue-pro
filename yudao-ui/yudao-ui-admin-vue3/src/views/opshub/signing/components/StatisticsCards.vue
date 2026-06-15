<template>
  <ContentWrap>
    <div class="stat-grid">
      <el-card v-for="card in cards" :key="card.key" class="stat-card" shadow="hover" body-class="p-12px">
        <div class="text-12px text-gray-400 mb-4px">{{ card.label }}</div>
        <div class="text-24px font-bold" :class="card.color">{{ card.value }}</div>
        <div class="text-12px text-gray-400 mt-4px leading-tight">{{ card.sub }}</div>
      </el-card>
    </div>
  </ContentWrap>
</template>

<script lang="ts" setup>
import * as SigningApi from '@/api/opshub/signing'

const stats = ref<SigningApi.SigningStatisticsVO | null>(null)

const cards = computed(() => {
  const s = stats.value
  if (!s) return []
  return [
    {
      key: 'total',
      label: '合同总数',
      value: s.totalCount,
      color: 'text-gray-800',
      sub: `主合同 ${s.mainCount} · 政策合同 ${s.policyCount} · 补充协议 ${s.supplementCount} · 终止协议 ${s.terminationCount}`
    },
    {
      key: 'signed',
      label: '已签署',
      value: s.signedCount,
      color: 'text-green-600',
      sub: `${s.signedRate}%`
    },
    {
      key: 'unsigned',
      label: '未签署',
      value: s.unsignedCount,
      color: 'text-orange-500',
      sub: `待签署 ${s.pendingCount} · 签署中 ${s.signingCount}`
    },
    {
      key: 'main',
      label: '主合同',
      value: s.main?.total || 0,
      color: 'text-purple-600',
      sub: `已签署 ${s.main?.signed || 0} / 未签署 ${s.main?.unsigned || 0}`
    },
    {
      key: 'policy',
      label: '政策合同',
      value: s.policy?.total || 0,
      color: 'text-blue-600',
      sub: `已签署 ${s.policy?.signed || 0} / 未签署 ${s.policy?.unsigned || 0}`
    },
    {
      key: 'supplement',
      label: '补充协议',
      value: s.supplement?.total || 0,
      color: 'text-amber-600',
      sub: `已签署 ${s.supplement?.signed || 0} / 未签署 ${s.supplement?.unsigned || 0}`
    },
    {
      key: 'termination',
      label: '终止协议',
      value: s.termination?.total || 0,
      color: 'text-red-600',
      sub: `已签署 ${s.termination?.signed || 0} / 未签署 ${s.termination?.unsigned || 0}`
    }
  ]
})

const loadStats = async () => {
  try {
    stats.value = await SigningApi.getSigningStatistics()
  } catch (e) {
    console.error('加载统计数据失败', e)
  }
}

onMounted(loadStats)

defineExpose({ refresh: loadStats })
</script>

<style scoped>
.stat-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 12px;
}
.stat-card :deep(.el-card__body) {
  padding: 12px;
}
</style>
