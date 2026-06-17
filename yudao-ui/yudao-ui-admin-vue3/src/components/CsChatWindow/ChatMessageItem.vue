<template>
  <div :class="['chat-message-item mb-3 flex', isSelf ? 'justify-end' : 'justify-start']">
    <!-- 系统消息 -->
    <div v-if="message.messageType === 'system'" class="text-center text-xs text-gray-400 py-1 w-full">
      {{ message.content }}
      <span class="ml-2">{{ formatTime(message.createTime) }}</span>
    </div>

    <!-- 普通消息 -->
    <div v-else :class="['max-w-[75%]', isSelf ? 'items-end' : 'items-start']">
      <div class="flex items-center gap-1 mb-1" :class="isSelf ? 'flex-row-reverse' : ''">
        <span class="text-xs font-medium" :class="isSelf ? 'text-blue-600' : 'text-green-600'">
          {{ message.senderName }}
        </span>
        <span class="text-xs text-gray-400">{{ formatTime(message.createTime) }}</span>
      </div>
      <div
        :class="[
          'rounded-lg px-3 py-2 text-sm',
          isSelf ? 'bg-blue-500 text-white' : 'bg-gray-100 text-gray-800'
        ]"
      >
        <!-- 文本消息 -->
        <div v-if="message.messageType === 'text'">{{ message.content }}</div>

        <!-- 附件消息 -->
        <div v-else-if="message.messageType === 'attachment'">
          <div v-if="message.content" class="mb-1">{{ message.content }}</div>
          <div class="text-xs opacity-80">
            <el-icon><Document /></el-icon> 附件（ID: {{ message.attachmentIds }}）
          </div>
        </div>

        <!-- 链接消息 -->
        <div v-else-if="message.messageType === 'link'">
          <a :href="message.linkUrl" target="_blank" class="underline" :class="isSelf ? 'text-white' : 'text-blue-600'">
            {{ message.linkTitle || message.linkUrl }}
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import { Document } from '@element-plus/icons-vue'
import type { CsMessageVO } from '@/api/opshub/csSession'
import { formatDate } from '@/utils/formatTime'
import { getCurrentUserId } from '@/utils/auth'

const props = defineProps<{
  message: CsMessageVO
}>()

const isSelf = computed(() => {
  return props.message.senderId === getCurrentUserId()
})

const formatTime = (time: string) => {
  if (!time) return ''
  return formatDate(time, 'HH:mm')
}
</script>
