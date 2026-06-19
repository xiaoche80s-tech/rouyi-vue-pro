<template>
  <div class="chat-input-bar border-t border-gray-200 px-4 py-3">
    <!-- 链接输入 -->
    <div v-if="showLinkInput" class="mb-2 flex gap-2">
      <el-input v-model="linkUrl" placeholder="请输入链接地址" size="small" clearable />
      <el-input v-model="linkTitle" placeholder="链接标题（可选）" size="small" clearable />
      <el-button size="small" type="primary" @click="sendLink">发送链接</el-button>
      <el-button size="small" @click="showLinkInput = false">取消</el-button>
    </div>

    <!-- 工具栏：下发任务（条件显示） -->
    <div v-if="showTaskDispatch" class="mb-2 flex items-center">
      <el-button size="small" type="warning" plain @click="$emit('taskDispatch')">
        <el-icon class="mr-1"><Tickets /></el-icon>下发任务
      </el-button>
    </div>

    <!-- 文本输入 -->
    <div class="flex gap-2">
      <el-input
        v-model="text"
        :autosize="{ minRows: 1, maxRows: 3 }"
        placeholder="输入消息..."
        type="textarea"
        :disabled="sending"
        @keydown.enter.exact.prevent="sendText"
      />
      <div class="flex flex-col gap-1">
        <el-button type="primary" :loading="sending" :disabled="!text.trim()" @click="sendText">
          发送
        </el-button>
        <el-button size="small" plain @click="showLinkInput = true">
          链接
        </el-button>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { Tickets } from '@element-plus/icons-vue'

defineProps<{
  sending: boolean
  showTaskDispatch?: boolean
}>()

const emit = defineEmits<{
  send: [payload: { messageType: string; content: string; linkUrl?: string; linkTitle?: string }]
  taskDispatch: []
}>()

const text = ref('')
const showLinkInput = ref(false)
const linkUrl = ref('')
const linkTitle = ref('')

const sendText = () => {
  const content = text.value.trim()
  if (!content) return
  emit('send', { messageType: 'text', content })
  text.value = ''
}

const sendLink = () => {
  if (!linkUrl.value.trim()) return
  emit('send', {
    messageType: 'link',
    content: '',
    linkUrl: linkUrl.value.trim(),
    linkTitle: linkTitle.value.trim() || undefined
  })
  linkUrl.value = ''
  linkTitle.value = ''
  showLinkInput.value = false
}
</script>
