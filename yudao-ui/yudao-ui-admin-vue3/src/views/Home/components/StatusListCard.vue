<template>
  <el-card shadow="hover" :body-style="{ padding: '16px 20px' }">
    <template #header>
      <span class="text-15px font-600">{{ title }}</span>
    </template>
    <div class="status-list">
      <div
        v-for="(item, index) in items"
        :key="index"
        class="status-item"
        :class="{ 'status-item--highlight': item.highlight }"
      >
        <span class="status-label">{{ item.label }}</span>
        <span class="status-value" :style="{ color: item.color || '#333' }">{{ item.value }}</span>
      </div>
    </div>
  </el-card>
</template>

<script lang="ts" setup>
defineOptions({ name: 'StatusListCard' })

defineProps<{
  title: string
  items: { label: string; value: number; highlight?: boolean; color?: string }[]
}>()
</script>

<style scoped>
.status-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border-radius: 6px;
  transition: background 0.15s;
}
.status-item--highlight {
  background: #f0f9ff;
}
.status-label {
  font-size: 14px;
  color: #6b7280;
}
.status-value {
  font-size: 18px;
  font-weight: 700;
}
</style>
