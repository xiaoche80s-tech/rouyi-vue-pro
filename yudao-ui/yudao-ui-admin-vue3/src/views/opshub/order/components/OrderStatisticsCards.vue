<template>
  <ContentWrap>
    <div class="stat-grid">
      <el-card
        v-for="card in cards"
        :key="card.key"
        class="stat-card cursor-pointer"
        :class="{ 'border-blue-500': activeCard === card.key }"
        shadow="hover"
        body-class="p-12px"
        @click="handleCardClick(card)"
      >
        <div class="text-12px text-gray-400 mb-4px">{{ card.label }}</div>
        <div class="text-24px font-bold" :class="card.color">{{ card.value }}</div>
      </el-card>
    </div>
  </ContentWrap>
</template>

<script lang="ts" setup>
import * as OrderApi from '@/api/opshub/order'

const emit = defineEmits<{
  (e: 'filter', filters: any): void
}>()

const stats = ref<OrderApi.OrderStatisticsVO | null>(null)
const activeCard = ref<string>('all')

const cards = computed(() => {
  const s = stats.value
  if (!s) return []
  return [
    { key: 'all', label: '订单总量', value: s.totalCount, color: 'text-gray-800', filter: {} },
    { key: 'completed', label: '已完成', value: s.completedCount, color: 'text-green-600', filter: { progressStatus: 'completed' } },
    { key: 'paid', label: '已付款', value: s.paidCount, color: 'text-blue-600', filter: { payStatus: 'paid' } },
    { key: 'unpaid', label: '未付款', value: s.unpaidCount, color: 'text-orange-500', filter: { payStatus: 'unpaid' } },
    { key: 'invoiced', label: '已开票', value: s.invoicedCount, color: 'text-purple-600', filter: { invStatus: 'invoiced' } },
    { key: 'uninvoiced', label: '未开票', value: s.uninvoicedCount, color: 'text-red-500', filter: { invStatus: 'uninvoiced' } }
  ]
})

const handleCardClick = (card: any) => {
  activeCard.value = card.key
  emit('filter', card.filter)
}

const loadStats = async () => {
  try {
    stats.value = await OrderApi.getOrderStatistics()
  } catch (e) {
    console.error('加载统计数据失败', e)
  }
}

onMounted(loadStats)

defineExpose({ refresh: loadStats, resetActive: () => { activeCard.value = 'all' } })
</script>

<style scoped>
.stat-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}
.stat-card :deep(.el-card__body) {
  padding: 12px;
}
</style>
