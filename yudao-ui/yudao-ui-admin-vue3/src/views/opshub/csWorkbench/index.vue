<template>
  <div class="cs-workbench flex flex-col h-full bg-white">
    <!-- 顶部统计条 -->
    <div class="statistics-bar flex items-center gap-4 px-4 py-2 border-b border-gray-200 bg-gray-50 shrink-0">
      <div
        v-for="item in statItems"
        :key="item.key"
        class="stat-chip flex items-center gap-1.5 px-3 py-1 rounded-full cursor-pointer text-sm transition-colors"
        :class="statFilter === item.key ? item.activeClass : 'bg-white text-gray-600 hover:bg-gray-100'"
        @click="handleStatClick(item.key)"
      >
        <span class="font-medium">{{ item.label }}</span>
        <span class="font-bold" :class="item.countClass">{{ getStatCount(item.key) }}</span>
      </div>
    </div>

    <!-- 主内容区：左列表 + 右聊天 -->
    <div class="flex-1 flex min-h-0">
      <!-- 左侧会话列表 -->
      <div class="w-[320px] border-r border-gray-200 shrink-0">
        <ConsultSessionList
          :sessions="sessions"
          :selected-session-id="selectedSessionId"
          :new-session-ids="newSessionIds"
          @select="handleSelectSession"
        />
      </div>

      <!-- 右侧聊天面板 -->
      <div class="flex-1 min-w-0">
        <ConsultChatPanel
          v-if="selectedSessionId"
          ref="chatPanelRef"
          :session-id="selectedSessionId"
          :is-connected="isConnected"
          @session-updated="loadSessionList"
        />
        <div v-else class="h-full flex items-center justify-center text-gray-400">
          <div class="text-center">
            <Icon icon="ep:chat-line-round" class="text-6xl mb-3 block" />
            <div class="text-lg">选择一个咨询会话开始处理</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch } from 'vue'
import type { CsSessionVO, CsStatisticsVO, CsMessageVO } from '@/api/opshub/csSession'
import {
  getSessionPage,
  getSessionStatistics
} from '@/api/opshub/csSession'
import { useCsWebSocket, type CsChatMessagePayload } from '@/hooks/useCsWebSocket'
import ConsultSessionList from './components/ConsultSessionList.vue'
import ConsultChatPanel from './components/ConsultChatPanel.vue'

defineOptions({ name: 'CsWorkbench' })

// ========== 统计 ==========
const statistics = ref<CsStatisticsVO>({
  totalCount: 0, pendingCount: 0, processingCount: 0, completedCount: 0, closedCount: 0
})

const statFilter = ref<string>('')

const statItems = [
  { key: '', label: '全部', activeClass: 'bg-blue-100 text-blue-800', countClass: 'text-blue-700' },
  { key: 'pending', label: '待处理', activeClass: 'bg-orange-100 text-orange-800', countClass: 'text-orange-600' },
  { key: 'processing', label: '处理中', activeClass: 'bg-blue-100 text-blue-800', countClass: 'text-blue-600' },
  { key: 'completed', label: '已完成', activeClass: 'bg-green-100 text-green-800', countClass: 'text-green-600' },
  { key: 'closed', label: '已关闭', activeClass: 'bg-gray-100 text-gray-800', countClass: 'text-gray-500' }
]

const getStatCount = (key: string) => {
  switch (key) {
    case '': return statistics.value.totalCount
    case 'pending': return statistics.value.pendingCount
    case 'processing': return statistics.value.processingCount
    case 'completed': return statistics.value.completedCount
    case 'closed': return statistics.value.closedCount
    default: return 0
  }
}

const handleStatClick = (key: string) => {
  statFilter.value = key
}

const loadStatistics = async () => {
  try {
    statistics.value = await getSessionStatistics()
  } catch (e) {
    // 静默失败
  }
}

// ========== 会话列表 ==========
const sessions = ref<CsSessionVO[]>([])
const newSessionIds = ref(new Set<number>())

const loadSessionList = async () => {
  try {
    const params: any = { pageNo: 1, pageSize: 100 }
    // 按统计筛选过滤
    const statusMap: Record<string, string> = {
      pending: '0', processing: '1', completed: '2', closed: '3'
    }
    if (statFilter.value && statusMap[statFilter.value]) {
      params.status = statusMap[statFilter.value]
    }
    const data = await getSessionPage(params)
    sessions.value = data.list
  } catch (e) {
    // 静默失败
  }
}

// 监听统计筛选变化
watch(() => statFilter.value, () => {
  loadSessionList()
})

// ========== 选中会话 ==========
const selectedSessionId = ref<number>()
const chatPanelRef = ref<InstanceType<typeof ConsultChatPanel>>()

const handleSelectSession = (sessionId: number) => {
  selectedSessionId.value = sessionId
  // 移除新会话高亮
  newSessionIds.value.delete(sessionId)
}

// ========== WebSocket 实时监听 ==========
const { isConnected } = useCsWebSocket(
  // cs-chat-message: 更新对应会话 lastMessage + 当前会话追加消息
  (msg: CsChatMessagePayload) => {
    const session = sessions.value.find(s => s.id === msg.sessionId)
    if (session) {
      session.lastMessage = msg.content
      session.lastMessageTime = msg.createTime
      session.messageCount = (session.messageCount || 0) + 1
    }
    // 如果是当前会话，追加消息
    if (msg.sessionId === selectedSessionId.value) {
      chatPanelRef.value?.appendMessage({
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
        isRead: false,
        createTime: msg.createTime
      })
    }
  },
  // cs-session-event: 接单/完成/关闭 → 刷新列表 + 当前会话
  (msg: CsChatMessagePayload) => {
    loadSessionList()
    loadStatistics()
    if (msg.sessionId === selectedSessionId.value) {
      if (msg.systemMessage) {
        // event 携带了系统消息，直接追加到聊天面板，无需 HTTP refresh
        chatPanelRef.value?.appendMessage({
          id: msg.systemMessage.id,
          sessionId: msg.sessionId,
          sessionNo: msg.sessionNo,
          senderId: undefined,
          senderName: '',
          senderRole: msg.systemMessage.senderRole,
          messageType: msg.systemMessage.messageType,
          content: msg.systemMessage.content,
          attachmentIds: '',
          linkUrl: '',
          linkTitle: '',
          isRead: true,
          createTime: msg.systemMessage.createTime
        })
        chatPanelRef.value?.updateSessionState(msg)
      } else {
        // 兴容降级：旧数据无 systemMessage 时回退到 refresh
        chatPanelRef.value?.refresh()
      }
    }
  },
  // cs-new-consult: 新咨询到达 → 列表顶部插入 + 高亮闪烁
  (msg: CsChatMessagePayload) => {
    loadSessionList()
    loadStatistics()
    // 3 秒黄色高亮
    newSessionIds.value.add(msg.sessionId)
    setTimeout(() => {
      newSessionIds.value.delete(msg.sessionId)
    }, 3000)
  }
)

// ========== 初始化 ==========
onMounted(() => {
  loadSessionList()
  loadStatistics()
})
</script>

<style scoped>
.cs-workbench {
  height: calc(100vh - 120px);
}
</style>
