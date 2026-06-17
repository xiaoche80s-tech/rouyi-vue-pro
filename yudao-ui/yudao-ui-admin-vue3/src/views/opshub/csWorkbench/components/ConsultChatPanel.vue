<template>
  <div class="consult-chat-panel flex flex-col h-full">
    <!-- 头部 -->
    <ChatHeader
      :session="session"
      :is-connected="isConnected"
      mode="agent"
      @accept="handleAccept"
      @complete="handleComplete"
    />

    <!-- 消息列表 -->
    <ChatMessageList
      ref="messageListRef"
      :messages="messages"
      :loading="messagesLoading"
    />

    <!-- 输入栏 -->
    <ChatInputBar
      :sending="sending"
      @send="handleSend"
    />

    <!-- 完成处理对话框 -->
    <el-dialog v-model="completeDialogVisible" title="完成处理" width="400px" :close-on-click-modal="false">
      <el-form>
        <el-form-item label="解决方案摘要">
          <el-input
            v-model="solutionSummary"
            type="textarea"
            :autosize="{ minRows: 3, maxRows: 6 }"
            placeholder="请描述解决方案..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="completing" @click="submitComplete">确认完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import type { CsSessionVO, CsMessageVO } from '@/api/opshub/csSession'
import {
  getSession,
  acceptSession,
  completeSession,
  sendMessage
} from '@/api/opshub/csSession'
import ChatHeader from '@/components/CsChatWindow/ChatHeader.vue'
import ChatMessageList from '@/components/CsChatWindow/ChatMessageList.vue'
import ChatInputBar from '@/components/CsChatWindow/ChatInputBar.vue'

const props = defineProps<{
  sessionId: number
  isConnected: boolean
}>()

const emit = defineEmits<{
  'session-updated': []
}>()

// ========== 会话详情 ==========
const session = ref<CsSessionVO | null>(null)
const messages = ref<CsMessageVO[]>([])
const messagesLoading = ref(false)
const messageListRef = ref<InstanceType<typeof ChatMessageList>>()

const loadSession = async () => {
  if (!props.sessionId) return
  messagesLoading.value = true
  try {
    const data = await getSession(props.sessionId)
    session.value = data
    messages.value = data.messages || []
    await nextTick()
    messageListRef.value?.scrollToBottom()
  } finally {
    messagesLoading.value = false
  }
}

watch(() => props.sessionId, () => {
  loadSession()
}, { immediate: true })

// ========== 操作 ==========
const sending = ref(false)
const handleSend = async (payload: { messageType: string; content: string; linkUrl?: string; linkTitle?: string }) => {
  if (!props.sessionId) return
  sending.value = true
  try {
    await sendMessage({ sessionId: props.sessionId, ...payload })
    // 重新加载消息
    await loadSession()
    emit('session-updated')
  } finally {
    sending.value = false
  }
}

const handleAccept = async () => {
  if (!props.sessionId) return
  try {
    await acceptSession(props.sessionId)
    ElMessage.success('接单成功')
    await loadSession()
    emit('session-updated')
  } catch (e) {
    // 错误由全局异常处理
  }
}

const completeDialogVisible = ref(false)
const solutionSummary = ref('')
const completing = ref(false)

const handleComplete = () => {
  solutionSummary.value = ''
  completeDialogVisible.value = true
}

const submitComplete = async () => {
  if (!props.sessionId) return
  completing.value = true
  try {
    await completeSession({
      id: props.sessionId,
      solutionSummary: solutionSummary.value
    })
    ElMessage.success('已完成处理')
    completeDialogVisible.value = false
    await loadSession()
    emit('session-updated')
  } finally {
    completing.value = false
  }
}

// ========== 外部方法 ==========
/** 添加新消息到列表（WebSocket 实时推送时使用） */
const appendMessage = (msg: CsMessageVO) => {
  messages.value.push(msg)
  nextTick(() => {
    messageListRef.value?.scrollToBottom()
  })
}

/** 刷新会话数据 */
const refresh = () => {
  loadSession()
}

defineExpose({ appendMessage, refresh })
</script>
