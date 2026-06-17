<template>
  <Teleport to="body">
    <Transition name="chat-slide">
      <div v-if="visible" class="cs-chat-modal fixed z-[2000] flex flex-col bg-white rounded-t-xl shadow-2xl border border-gray-200 overflow-hidden"
        style="right: 24px; bottom: 24px; width: 420px; height: 600px;">
        <!-- 标题栏 -->
        <div class="flex items-center justify-between px-4 py-3 bg-blue-600 text-white shrink-0">
          <span class="text-sm font-semibold truncate">{{ modalTitle }}</span>
          <el-button text size="small" class="!text-white hover:!bg-blue-700" @click="handleClose">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>

        <!-- 头部信息 -->
        <ChatHeader
          :session="session"
          :is-connected="wsConnected"
          :mode="effectiveMode"
          @accept="handleAccept"
          @complete="handleComplete"
          @close-session="handleCloseSession"
        />

        <!-- 经销商视图：待处理时显示转接等待 -->
        <div v-if="effectiveMode === 'dealer' && session?.status === 0"
          class="flex-1 flex flex-col items-center justify-center px-6">
          <el-icon class="is-loading text-amber-400 text-4xl mb-4"><Loading /></el-icon>
          <div class="text-base font-medium text-gray-700 mb-1">正在为您转接人工客服</div>
          <div class="text-sm text-gray-400 text-center">系统已收到您的咨询，正在分配客服人员<br/>请耐心等待...</div>
        </div>

        <!-- 正常消息列表 -->
        <ChatMessageList
          v-else
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

        <!-- 经销商：已完成/已关闭底部提示 -->
        <div v-if="effectiveMode === 'dealer' && (session?.status === 2 || session?.status === 3)"
          class="px-4 py-3 border-t border-gray-100 bg-gray-50 text-center text-sm text-gray-500 shrink-0">
          {{ session?.status === 2 ? '该咨询已完成处理，感谢您的咨询' : '该咨询已关闭' }}
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script lang="ts" setup>
import { ref, computed, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, Close } from '@element-plus/icons-vue'
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
import { useCsWebSocket, type CsChatMessagePayload } from '@/hooks/useCsWebSocket'
import { getCurrentUserId } from '@/utils/auth'

const props = withDefaults(defineProps<{
  modelValue: boolean
  sessionId?: number
  /** 'dealer' 经销商视图 | 'agent' 客服执行员视图 | 'auto' 自动检测 */
  mode?: 'dealer' | 'agent' | 'auto'
}>(), {
  mode: 'auto'
})

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

// ========== WebSocket 实时监听（方案二：HTTP 上行 + WebSocket 下行）==========
const wsConnected = ref(false)

const onWsChatMessage = (msg: CsChatMessagePayload) => {
  if (!props.sessionId || msg.sessionId !== props.sessionId) return
  if (msg.senderId === getCurrentUserId()) return
  if (messages.value.some(m => m.id === msg.messageId)) return
  const vo: CsMessageVO = {
    id: msg.messageId,
    sessionId: msg.sessionId,
    sessionNo: msg.sessionNo,
    senderId: msg.senderId,
    senderName: msg.senderName,
    senderRole: msg.senderRole,
    messageType: msg.messageType,
    content: msg.content,
    attachmentIds: msg.attachmentIds,
    linkUrl: msg.linkUrl,
    linkTitle: msg.linkTitle,
    createTime: msg.createTime
  }
  messages.value.push(vo)
  nextTick(() => messageListRef.value?.scrollToBottom())
  if (session.value) {
    session.value.lastMessage = msg.content || msg.linkTitle || '[附件]'
    session.value.messageCount = (session.value.messageCount || 0) + 1
  }
}

const onWsSessionEvent = (msg: CsChatMessagePayload) => {
  if (!props.sessionId || msg.sessionId !== props.sessionId) return
  loadSession()
  emit('session-updated')
}

const { isConnected: wsIsConnected } = useCsWebSocket(onWsChatMessage, onWsSessionEvent)

watch(wsIsConnected, (v) => { wsConnected.value = v }, { immediate: true })

watch(() => props.modelValue, (open) => {
  if (open) {
    wsConnected.value = wsIsConnected.value
    loadSession()
  }
})

// ========== 角色模式检测 ==========
const consultTypeMap: Record<string, string> = {
  signing: '签约咨询', policy: '政策咨询', aftersale: '售后咨询',
  order: '订单咨询', basedata: '基础数据咨询', other: '其他咨询'
}

const effectiveMode = computed<'dealer' | 'agent'>(() => {
  if (props.mode !== 'auto') return props.mode
  if (!session.value) return 'dealer'
  return session.value.initiatorId === getCurrentUserId() ? 'dealer' : 'agent'
})

const modalTitle = computed(() => {
  if (effectiveMode.value === 'dealer') {
    const t = session.value?.consultType
    return t ? (consultTypeMap[t] || t) : '在线咨询'
  }
  return session.value?.sessionNo || '在线咨询'
})

const canSend = computed(() => {
  return session.value?.status === 1
})

const loadSession = async () => {
  if (!props.sessionId) return
  loadingMessages.value = true
  try {
    const data = await getSession(props.sessionId)
    session.value = data
    messages.value = data.messages || []
    await markRead(props.sessionId).catch(() => {})
    await nextTick()
    messageListRef.value?.scrollToBottom()
  } finally {
    loadingMessages.value = false
  }
}

const handleClose = () => {
  visible.value = false
}

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

const appendMessage = (msg: CsMessageVO) => {
  if (messages.value.some(m => m.id === msg.id)) return
  messages.value.push(msg)
  nextTick(() => messageListRef.value?.scrollToBottom())
}

const handleSessionEvent = (_eventType: string) => {
  loadSession()
  emit('session-updated')
}

defineExpose({ appendMessage, handleSessionEvent, loadSession })
</script>

<style scoped>
.chat-slide-enter-active,
.chat-slide-leave-active {
  transition: all 0.3s ease;
}
.chat-slide-enter-from,
.chat-slide-leave-to {
  opacity: 0;
  transform: translateY(30px);
}
</style>
