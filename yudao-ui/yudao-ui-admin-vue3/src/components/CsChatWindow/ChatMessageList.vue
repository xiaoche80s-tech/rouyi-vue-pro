<template>
  <div ref="containerRef" class="chat-message-list flex-1 overflow-y-auto px-4 py-3 min-h-0">
    <div v-if="loading" class="text-center text-gray-400 py-8">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span class="ml-2">加载历史消息...</span>
    </div>
    <div v-else-if="messages.length === 0" class="text-center text-gray-400 py-8">
      暂无消息记录
    </div>
    <ChatMessageItem
      v-for="msg in messages"
      :key="msg.id"
      :message="msg"
    />
  </div>
</template>

<script lang="ts" setup>
import { ref, nextTick } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import type { CsMessageVO } from '@/api/opshub/csSession'
import ChatMessageItem from './ChatMessageItem.vue'

defineProps<{
  messages: CsMessageVO[]
  loading: boolean
}>()

const containerRef = ref<HTMLElement>()

const scrollToBottom = () => {
  nextTick(() => {
    if (containerRef.value) {
      containerRef.value.scrollTop = containerRef.value.scrollHeight
    }
  })
}

defineExpose({ scrollToBottom })
</script>
