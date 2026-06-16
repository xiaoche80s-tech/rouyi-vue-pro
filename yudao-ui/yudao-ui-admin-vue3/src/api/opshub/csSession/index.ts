import request from '@/config/axios'

// 咨询会话 VO
export interface CsSessionVO {
  id: number
  sessionNo: string
  consultType: string
  status: number
  context: string
  contextId: number
  contextCode: string
  sourceModule: string
  dealerCode: string
  dealerName: string
  productLineCode: string
  productLineName: string
  initiatorId: number
  initiatorName: string
  assigneeId: number
  assigneeName: string
  lastMessage: string
  lastMessageTime: string
  messageCount: number
  acceptTime: string
  completeTime: string
  closeTime: string
  solutionSummary: string
  remark: string
  createTime: string
  messages?: CsMessageVO[]
}

export interface CsMessageVO {
  id: number
  sessionId: number
  sessionNo: string
  senderId: number
  senderName: string
  senderRole: string
  messageType: string
  content: string
  attachmentIds: string
  linkUrl: string
  linkTitle: string
  isRead: boolean
  createTime: string
}

export interface CsStatisticsVO {
  totalCount: number
  pendingCount: number
  processingCount: number
  completedCount: number
  closedCount: number
}

// 创建咨询会话
export const createSession = async (data: any) => {
  return await request.post({ url: '/opshub/cs-session/create', data })
}

// 查询咨询会话分页
export const getSessionPage = async (params: any) => {
  return await request.get({ url: '/opshub/cs-session/page', params })
}

// 查询咨询会话详情（含历史消息）
export const getSession = async (id: number) => {
  return await request.get({ url: '/opshub/cs-session/get?id=' + id })
}

// 执行员接单
export const acceptSession = async (id: number) => {
  return await request.post({ url: '/opshub/cs-session/accept?id=' + id })
}

// 执行员完成处理
export const completeSession = async (data: any) => {
  return await request.post({ url: '/opshub/cs-session/complete', data })
}

// 经销商关闭对话
export const closeSession = async (id: number) => {
  return await request.post({ url: '/opshub/cs-session/close?id=' + id })
}

// 获取咨询统计
export const getSessionStatistics = async () => {
  return await request.get({ url: '/opshub/cs-session/statistics' })
}

// 发送咨询消息
export const sendMessage = async (data: any) => {
  return await request.post({ url: '/opshub/cs-message/send', data })
}

// 查询消息分页
export const getMessagePage = async (params: any) => {
  return await request.get({ url: '/opshub/cs-message/page', params })
}

// 标记已读
export const markRead = async (sessionId: number) => {
  return await request.post({ url: '/opshub/cs-message/mark-read?sessionId=' + sessionId })
}
