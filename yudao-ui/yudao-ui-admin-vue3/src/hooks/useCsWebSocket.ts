import { ref, watchEffect, onBeforeUnmount } from 'vue'
import { useWebSocket } from '@vueuse/core'
import { getRefreshToken } from '@/utils/auth'

export interface CsChatMessagePayload {
  messageId: number
  sessionId: number
  sessionNo: string
  type: string
  senderId: number
  senderName: string
  senderRole: string
  messageType: string
  content: string
  attachmentIds: string
  linkUrl: string
  linkTitle: string
  createTime: string
  /** 咨询类型（前端显示用） */
  consultType: string
  /** 经销商名称（前端显示用） */
  dealerName: string
  /** 咨询上下文（前端显示用） */
  context: string
}

/**
 * 工单事件通知 Payload
 */
export interface CsTaskEventPayload {
  taskId: number
  taskNo: string
  type: string
  message: string
  status: number
  urgency: number
  operatorUserId?: number
  operatorUserName?: string
}

/**
 * 客服咨询 WebSocket Hook
 *
 * 监听 cs-chat-message / cs-session-event / cs-new-consult 三类消息推送
 */
export function useCsWebSocket(
  onChatMessage?: (msg: CsChatMessagePayload) => void,
  onSessionEvent?: (msg: CsChatMessagePayload) => void,
  onNewConsult?: (msg: CsChatMessagePayload) => void,
  onTaskEvent?: (msg: CsTaskEventPayload) => void
) {
  const server = ref(
    (import.meta.env.VITE_BASE_URL + '/infra/ws').replace('http', 'ws') +
      '?token=' +
      getRefreshToken()
  )

  const isConnected = ref(false)

  const { status, data, send, close, open } = useWebSocket(server.value, {
    autoReconnect: true,
    heartbeat: true
  })

  /** 监听接收到的数据 */
  watchEffect(() => {
    if (!data.value) {
      return
    }
    try {
      // 心跳
      if (data.value === 'pong') {
        return
      }
      const jsonMessage = JSON.parse(data.value)
      const type = jsonMessage.type
      if (!type) {
        return
      }
      // 仅处理 cs- 前缀的客服消息
      if (!type.startsWith('cs-')) {
        return
      }
      const content: CsChatMessagePayload = JSON.parse(jsonMessage.content)
      content.type = type

      isConnected.value = status.value === 'OPEN'

      switch (type) {
        case 'cs-chat-message':
          onChatMessage?.(content)
          break
        case 'cs-session-event':
          onSessionEvent?.(content)
          break
        case 'cs-new-consult':
          onNewConsult?.(content)
          break
        default:
          // 工单事件通知（cs-task-* / cs-opreq-* / cs-sla-*）
          if (type.startsWith('cs-task-') || type.startsWith('cs-opreq-') || type.startsWith('cs-sla-')) {
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            const raw = content as any
            const taskEvent: CsTaskEventPayload = {
              taskId: raw.taskId,
              taskNo: raw.taskNo,
              type: type,
              message: raw.message,
              status: raw.status,
              urgency: raw.urgency,
              operatorUserId: raw.operatorUserId,
              operatorUserName: raw.operatorUserName
            }
            onTaskEvent?.(taskEvent)
          }
          break
      }
    } catch (error) {
      console.warn('[useCsWebSocket] 消息解析异常:', error)
    }
  })

  onBeforeUnmount(() => {
    close()
  })

  return {
    status,
    isConnected,
    send,
    close,
    open
  }
}
