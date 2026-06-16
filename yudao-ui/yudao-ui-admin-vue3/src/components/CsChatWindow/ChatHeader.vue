<template>
  <div class="chat-header px-4 py-3 border-b border-gray-200">
    <div class="flex items-center justify-between mb-2">
      <div>
        <el-tag :type="statusTagType" size="small" class="mr-2">{{ statusText }}</el-tag>
        <span class="text-sm text-gray-500">{{ session?.consultType }}</span>
      </div>
      <div class="flex items-center gap-1">
        <el-tag v-if="isConnected" type="success" size="small" effect="plain">
          <el-icon class="mr-1"><CircleCheck /></el-icon>已连接
        </el-tag>
        <el-tag v-else type="danger" size="small" effect="plain">未连接</el-tag>
      </div>
    </div>
    <div class="text-sm text-gray-600 mb-1">
      <span>{{ session?.dealerName || '未知经销商' }}</span>
      <span v-if="session?.productLineName" class="ml-2 text-gray-400">| {{ session.productLineName }}</span>
    </div>
    <div v-if="session?.context" class="text-xs text-gray-400 mb-2 truncate">{{ session.context }}</div>

    <!-- 操作按钮 -->
    <div class="flex gap-2">
      <el-button v-if="session?.status === 0" type="primary" size="small" @click="$emit('accept')">
        接单
      </el-button>
      <el-button v-if="session?.status === 1" type="success" size="small" @click="$emit('complete')">
        完成处理
      </el-button>
      <el-button
        v-if="session?.status === 0 || session?.status === 1"
        type="danger"
        size="small"
        plain
        @click="$emit('closeSession')"
      >
        关闭对话
      </el-button>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import { CircleCheck } from '@element-plus/icons-vue'
import type { CsSessionVO } from '@/api/opshub/csSession'

const props = defineProps<{
  session: CsSessionVO | null
  isConnected: boolean
}>()

defineEmits<{
  accept: []
  complete: []
  closeSession: []
}>()

const statusText = computed(() => {
  const map: Record<number, string> = { 0: '待处理', 1: '处理中', 2: '已完成', 3: '已关闭' }
  return props.session ? map[props.session.status] || '未知' : '加载中'
})

const statusTagType = computed(() => {
  const map: Record<number, string> = { 0: 'warning', 1: '', 2: 'success', 3: 'info' }
  return (props.session ? map[props.session.status] : 'info') as any
})
</script>
