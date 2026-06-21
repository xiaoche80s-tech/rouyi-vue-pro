<template>
  <div class="target-dist-inline">
    <!-- 标题栏 + 排序 -->
    <div class="flex items-center gap-10px mb-10px">
      <span class="font-bold text-14px">目标分布</span>
      <div class="flex items-center gap-4px">
        <el-button
          size="small"
          :type="sortOrder === 'desc' ? 'primary' : 'default'"
          :plain="sortOrder !== 'desc'"
          @click="setSort('desc')"
        >
          ↓ 降序
        </el-button>
        <el-button
          size="small"
          :type="sortOrder === 'asc' ? 'primary' : 'default'"
          :plain="sortOrder !== 'asc'"
          @click="setSort('asc')"
        >
          ↑ 升序
        </el-button>
      </div>
      <span v-if="sortedGroups.length" class="text-gray-400 text-13px">
        共 {{ sortedGroups.length }} 个目标值
      </span>
    </div>

    <!-- 药丸列表 -->
    <div class="pill-scroll-wrap">
      <div
        v-for="group in sortedGroups"
        :key="group.targetValue"
        class="target-pill"
        @click="emit('pill-click', group)"
      >
        <div class="pill-icon">
          <el-icon :size="14"><Aim /></el-icon>
        </div>
        <div class="pill-body">
          <span class="pill-value">
            {{ formatNumber(group.targetValue) }}{{ group.unit || '' }}
          </span>
          <span class="pill-count">· {{ group.policyCount }}个政策</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { Aim } from '@element-plus/icons-vue'
import type { TargetGroupVO } from '@/api/opshub/policy'

const props = defineProps<{
  groups: TargetGroupVO[]
  loading: boolean
}>()

const emit = defineEmits<{
  (e: 'pill-click', group: TargetGroupVO): void
}>()

const sortOrder = ref<'desc' | 'asc'>('desc')

const sortedGroups = computed(() => {
  const list = [...props.groups]
  if (sortOrder.value === 'asc') {
    list.sort((a, b) => Number(a.targetValue) - Number(b.targetValue))
  } else {
    list.sort((a, b) => Number(b.targetValue) - Number(a.targetValue))
  }
  return list
})

const setSort = (order: 'desc' | 'asc') => {
  sortOrder.value = order
}

const formatNumber = (val: number) => {
  if (val == null) return '-'
  return val.toLocaleString()
}
</script>

<style scoped>
.target-dist-inline {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.pill-scroll-wrap {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding-bottom: 8px;
  scrollbar-width: thin;
}

.target-pill {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 20px;
  padding: 8px 16px;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
  white-space: nowrap;
}

.target-pill:hover {
  background: #ecf5ff;
  border-color: #409eff;
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.15);
}

.pill-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f56c6c, #e6a23c);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.pill-body {
  display: flex;
  flex-direction: column;
}

.pill-value {
  font-size: 14px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.pill-count {
  font-size: 12px;
  color: #909399;
  line-height: 1.2;
}
</style>
