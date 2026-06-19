<template>
  <el-card shadow="hover" :body-style="{ padding: '20px' }">
    <template #header>
      <span class="text-15px font-600">客服工作负荷</span>
    </template>
    <el-row :gutter="16">
      <!-- 工单 -->
      <el-col :xs="24" :sm="8" v-if="taskCounts">
        <div class="cs-section">
          <div class="flex items-center gap-2 mb-12px">
            <Icon icon="ep:ticket" :size="18" color="#7c3aed" />
            <span class="text-14px font-600">工单</span>
          </div>
          <div class="grid grid-cols-2 gap-2">
            <div v-for="(val, key) in taskCounts" :key="key" class="cs-item">
              <div class="text-12px text-gray-500">{{ taskLabel(key) }}</div>
              <div class="text-18px font-700 text-purple-600">{{ val }}</div>
            </div>
          </div>
        </div>
      </el-col>
      <!-- 咨询 -->
      <el-col :xs="24" :sm="8" v-if="consultStats">
        <div class="cs-section">
          <div class="flex items-center gap-2 mb-12px">
            <Icon icon="ep:chat-dot-round" :size="18" color="#3b82f6" />
            <span class="text-14px font-600">咨询</span>
          </div>
          <div class="grid grid-cols-2 gap-2">
            <div class="cs-item">
              <div class="text-12px text-gray-500">待处理</div>
              <div class="text-18px font-700 text-orange-500">{{ consultStats.pendingCount || 0 }}</div>
            </div>
            <div class="cs-item">
              <div class="text-12px text-gray-500">处理中</div>
              <div class="text-18px font-700 text-blue-500">{{ consultStats.processingCount || 0 }}</div>
            </div>
            <div class="cs-item">
              <div class="text-12px text-gray-500">已完成</div>
              <div class="text-18px font-700 text-green-500">{{ consultStats.completedCount || 0 }}</div>
            </div>
            <div class="cs-item">
              <div class="text-12px text-gray-500">总计</div>
              <div class="text-18px font-700">{{ consultStats.totalCount || 0 }}</div>
            </div>
          </div>
        </div>
      </el-col>
      <!-- 操作请求 -->
      <el-col :xs="24" :sm="8" v-if="opreqStats">
        <div class="cs-section">
          <div class="flex items-center gap-2 mb-12px">
            <Icon icon="ep:document" :size="18" color="#10b981" />
            <span class="text-14px font-600">操作请求</span>
          </div>
          <div class="grid grid-cols-2 gap-2">
            <div class="cs-item">
              <div class="text-12px text-gray-500">待处理</div>
              <div class="text-18px font-700 text-orange-500">{{ opreqStats.pendingCount || 0 }}</div>
            </div>
            <div class="cs-item">
              <div class="text-12px text-gray-500">处理中</div>
              <div class="text-18px font-700 text-blue-500">{{ opreqStats.inProgressCount || 0 }}</div>
            </div>
            <div class="cs-item">
              <div class="text-12px text-gray-500">等待验收</div>
              <div class="text-18px font-700 text-purple-500">{{ opreqStats.pendingVerifyCount || 0 }}</div>
            </div>
            <div class="cs-item">
              <div class="text-12px text-gray-500">已完成</div>
              <div class="text-18px font-700 text-green-500">{{ opreqStats.completedCount || 0 }}</div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </el-card>
</template>

<script lang="ts" setup>
defineOptions({ name: 'CsWorkloadPanel' })

defineProps<{
  taskCounts?: Record<string, number>
  consultStats?: {
    totalCount: number
    pendingCount: number
    processingCount: number
    completedCount: number
    closedCount: number
  }
  opreqStats?: {
    totalCount: number
    pendingCount: number
    inProgressCount: number
    pendingVerifyCount: number
    completedCount: number
  }
}>()

const taskLabel = (key: string) => {
  const map: Record<string, string> = {
    all: '全部',
    pending: '待接单',
    inProgress: '处理中',
    delivered: '已交付',
    closed: '已关闭',
    rejected: '已退回'
  }
  return map[key] || key
}
</script>

<style scoped>
.cs-section {
  padding: 12px;
  border-radius: 8px;
  background: #f8fafc;
  min-height: 140px;
}
.cs-item {
  padding: 6px 8px;
  text-align: center;
}
</style>
