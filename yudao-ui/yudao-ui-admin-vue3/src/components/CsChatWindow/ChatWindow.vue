<template>
  <el-drawer
    v-model="visible"
    :title="session?.sessionNo || '在线咨询'"
    direction="rtl"
    size="480px"
    :before-close="handleClose"
    @open="handleOpen"
  >
    <!-- 头部信息 -->
    <ChatHeader
      :session="session"
      :is-connected="true"
      @accept="handleAccept"
      @complete="handleComplete"
      @close-session="handleCloseSession"
    />

    <!-- 消息列表 -->
    <ChatMessageList
      ref="messageListRef"
      :messages="messages"
      :loading="loadingMessages"
    />

    <!-- 输入栏（仅处理中可发送） -->
    <ChatInputBar
      v-if="canSend"
      :sending="sending"
      @send="handleSend"
    />

    <!-- 已关闭/已完成提示 -->
    <el-alert
      v-if="session?.status === 2"
      title="该咨询已完成处理"
      type="success"
      :closable="false"
      show-icon
      class="mx-3 mb-3"
    />
    <el-alert
      v-if="session?.status === 3"
      title="该咨询已关闭"
      type="info"
      :closable="false"
      show-icon
      class="mx-3 mb-3"
    />
  </el-drawer>
</template>

<script lang="ts" setup>
import { ref, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { CsSessionVO, CsMessageVO } from '@/api/opshub/csSession'
import {
  getSession,
  acceptSession,
  completeSession,
  closeSession,
  sendMessage,
  markRead
} from '@/api/opshub/csSession'
import ChatHeader from './ChatHeader.vue'
import ChatMessageList from './ChatMessageList.vue'
import ChatInputBar from './ChatInputBar.vue'

const props = defineProps<{
  modelValue: boolean
  sessionId?: number
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'session-updated': []
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const session = ref<CsSessionVO | null>(null)
const messages = ref<CsMessageVO[]>([])
const loadingMessages = ref(false)
const sending = ref(false)
const messageListRef = ref()

const canSend = computed(() => {
  return session.value?.status === 1 // PROCESSING
})

/** 加载会话详情和历史消息 */
const loadSession = async () => {
  if (!props.sessionId) return
  loadingMessages.value = true
  try {
    const data = await getSession(props.sessionId)
    session.value = data
    messages.value = data.messages || []
    // 标记已读
    await markRead(props.sessionId).catch(() => {})
    await nextTick()
    messageListRef.value?.scrollToBottom()
  } finally {
    loadingMessages.value = false
  }
}

const handleOpen = () => {
  loadSession()
}

const handleClose = (done: () => void) => {
  done()
}

/** 接单 */
const handleAccept = async () => {
  if (!props.sessionId) return
  try {
    await acceptSession(props.sessionId)
    ElMessage.success('接单成功')
    await loadSession()
    emit('session-updated')
  } catch (e: any) {
    ElMessage.error(e.message || '接单失败')
  }
}

/** 完成处理 */
const handleComplete = async () => {
  if (!props.sessionId) return
  try {
    const { value } = await ElMessageBox.prompt('请输入解决方案摘要', '完成处理', {
      confirmButtonText: '确认完成',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入解决方案摘要'
    })
    await completeSession({ id: props.sessionId, solutionSummary: value })
    ElMessage.success('已完成处理')
    await loadSession()
    emit('session-updated')
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

/** 关闭对话 */
const handleCloseSession = async () => {
  if (!props.sessionId) return
  try {
    await ElMessageBox.confirm('确定关闭该咨询对话？', '确认关闭', {
      type: 'warning'
    })
    await closeSession(props.sessionId)
    ElMessage.success('已关闭对话')
    await loadSession()
    emit('session-updated')
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

/** 发送消息 */
const handleSend = async (payload: { messageType: string; content: string; linkUrl?: string; linkTitle?: string }) => {
  if (!props.sessionId) return
  sending.value = true
  try {
    const msg = await sendMessage({
      sessionId: props.sessionId,
      ...payload
    })
    messages.value.push(msg)
    await nextTick()
    messageListRef.value?.scrollToBottom()
    // 更新本地 session 的 lastMessage
    if (session.value) {
      session.value.lastMessage = payload.content || payload.linkTitle || '[附件]'
      session.value.messageCount = (session.value.messageCount || 0) + 1
    }
  } catch (e: any) {
    ElMessage.error(e.message || '发送失败')
  } finally {
    sending.value = false
  }
}

/** 外部推送：追加 WebSocket 新消息 */
const appendMessage = (msg: CsMessageVO) => {
  messages.value.push(msg)
  nextTick(() => messageListRef.value?.scrollToBottom())
}

/** 外部推送：会话事件 */
const handleSessionEvent = (_eventType: string) => {
  loadSession()
  emit('session-updated')
}

defineExpose({ appendMessage, handleSessionEvent, loadSession })
</script>
