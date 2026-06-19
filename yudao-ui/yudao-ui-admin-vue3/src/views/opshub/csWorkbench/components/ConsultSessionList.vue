<template>
  <div class="session-list flex flex-col h-full">
    <!-- 搜索框 -->
    <div class="px-3 pt-3 pb-2">
      <el-input
        v-model="keyword"
        placeholder="搜索经销商/编号/上下文"
        clearable
        size="small"
        @input="handleSearch"
      >
        <template #prefix>
          <Icon icon="ep:search" />
        </template>
      </el-input>
    </div>

    <!-- 筛选 -->
    <div class="px-3 pb-2 flex gap-2">
      <el-select v-model="statusFilter" placeholder="状态" size="small" clearable style="width: 90px">
        <el-option label="全部" value="" />
        <el-option label="待处理" :value="0" />
        <el-option label="处理中" :value="1" />
        <el-option label="已完成" :value="2" />
        <el-option label="已关闭" :value="3" />
      </el-select>
      <el-select v-model="consultTypeFilter" placeholder="类型" size="small" clearable style="width: 90px">
        <el-option v-for="t in consultTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
      </el-select>
    </div>

    <!-- 会话列表 -->
    <div class="flex-1 overflow-y-auto min-h-0">
      <div
        v-for="session in filteredSessions"
        :key="session.id"
        class="session-item flex items-center gap-3 px-3 py-3 cursor-pointer border-b border-gray-100 transition-all duration-300"
        :class="{
          'bg-blue-50 border-l-3 border-l-blue-500': selectedSessionId === session.id,
          'bg-white hover:bg-gray-50': selectedSessionId !== session.id,
          'session-new-highlight': newSessionIds.has(session.id)
        }"
        @click="$emit('select', session.id)"
      >
        <!-- 头像 -->
        <div
          class="w-10 h-10 rounded-full flex items-center justify-center text-white text-sm font-bold shrink-0"
          :style="{ backgroundColor: getAvatarColor(session.dealerName) }"
        >
          {{ (session.dealerName || '?').charAt(0) }}
        </div>

        <!-- 信息 -->
        <div class="flex-1 min-w-0">
          <div class="flex items-center justify-between mb-0.5">
            <span class="text-sm font-medium text-gray-800 truncate">{{ session.dealerName || '未知经销商' }}</span>
            <span class="text-xs text-gray-400 shrink-0 ml-2">{{ getRelativeTime(session.lastMessageTime || session.createTime) }}</span>
          </div>
          <div class="flex items-center gap-1 mb-0.5">
            <el-tag size="small" :type="getConsultTypeTagType(session.consultType)">{{ getConsultTypeLabel(session.consultType) }}</el-tag>
            <el-tag size="small" :type="getStatusTagType(session.status)" effect="plain">{{ getStatusLabel(session.status) }}</el-tag>
          </div>
          <div class="text-xs text-gray-500 truncate">{{ session.lastMessage || session.context || '暂无消息' }}</div>
        </div>

        <!-- 未读/状态 -->
        <div class="shrink-0">
          <span
            class="status-dot inline-block w-2 h-2 rounded-full"
            :class="getStatusDotClass(session.status)"
          ></span>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredSessions.length === 0" class="text-center text-gray-400 py-12">
        <Icon icon="ep:chat-line-square" class="text-4xl mb-2 block" />
        <span class="text-sm">暂无咨询会话</span>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, watch } from 'vue'
import type { CsSessionVO } from '@/api/opshub/csSession'

const props = defineProps<{
  sessions: CsSessionVO[]
  selectedSessionId?: number
  newSessionIds?: Set<number>
}>()

defineEmits<{
  select: [sessionId: number]
}>()

const keyword = ref('')
const statusFilter = ref<number | string>('')
const consultTypeFilter = ref<string>('')

const consultTypeOptions = [
  { value: 'signing', label: '签约' },
  { value: 'policy', label: '政策' },
  { value: 'aftersale', label: '售后' },
  { value: 'order', label: '订单' },
  { value: 'basedata', label: '基础数据' },
  { value: 'other', label: '其他' }
]

const getStatusLabel = (val: number) => {
  const map: Record<number, string> = { 0: '待处理', 1: '处理中', 2: '已完成', 3: '已关闭' }
  return map[val] || ''
}

const getStatusTagType = (val: number) =>
  (['warning', 'primary', 'success', 'info'] as const)[val] || undefined

const getStatusDotClass = (val: number) => {
  const map: Record<number, string> = {
    0: 'bg-orange-400',
    1: 'bg-blue-400',
    2: 'bg-green-400',
    3: 'bg-gray-300'
  }
  return map[val] || 'bg-gray-300'
}

const consultTypeMap: Record<string, string> = {
  signing: '签约', policy: '政策', aftersale: '售后',
  order: '订单', basedata: '基础数据', other: '其他'
}

const getConsultTypeLabel = (val: string) =>
  consultTypeMap[val] || val || ''

const getConsultTypeTagType = (val: string) => {
  const map: Record<string, string> = {
    signing: 'primary', policy: 'info', aftersale: 'warning',
    order: 'success', basedata: 'danger', other: 'info'
  }
  return (map[val] || undefined) as any
}

const getAvatarColor = (name: string) => {
  const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#8B5CF6', '#EC4899', '#06B6D4']
  if (!name) return colors[0]
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length]
}

const getRelativeTime = (dateStr: string | number) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return ''
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 7) return `${days}天前`
  // 用 Date 方法格式化，兼容 string 和 number 输入
  const mm = String(date.getMonth() + 1).padStart(2, '0')
  const dd = String(date.getDate()).padStart(2, '0')
  const hh = String(date.getHours()).padStart(2, '0')
  const mi = String(date.getMinutes()).padStart(2, '0')
  return `${mm}-${dd} ${hh}:${mi}`
}

const filteredSessions = computed(() => {
  let result = props.sessions
  if (keyword.value) {
    const kw = keyword.value.toLowerCase()
    result = result.filter(s =>
      (s.sessionNo || '').toLowerCase().includes(kw) ||
      (s.dealerName || '').toLowerCase().includes(kw) ||
      (s.context || '').toLowerCase().includes(kw)
    )
  }
  if (statusFilter.value !== '' && statusFilter.value !== undefined) {
    result = result.filter(s => s.status === statusFilter.value)
  }
  if (consultTypeFilter.value) {
    result = result.filter(s => s.consultType === consultTypeFilter.value)
  }
  return result
})

const handleSearch = () => {
  // reactive computed, no need for manual trigger
}
</script>

<style scoped>
.session-new-highlight {
  animation: sessionHighlight 3s ease-out;
}

@keyframes sessionHighlight {
  0% { background-color: #FEF9C3; }
  100% { background-color: transparent; }
}

.status-dot {
  display: inline-block;
}
</style>
