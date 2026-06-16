import request from '@/config/axios'

// 客服工单 VO
export interface CsTaskVO {
  id: number
  taskNo: string
  content: string
  urgency: number
  status: number
  creatorUserId: number
  creatorUserName: string
  assigneeId: number
  assigneeName: string
  category: number
  slaDeadline: string
  dealerCode: string
  dealerName: string
  productLineCode: string
  productLineName: string
  sourceModule: string
  remark: string
  acceptTime: string
  deliverTime: string
  verifyTime: string
  rejectReason: string
  createTime: string
  updateTime: string
}

// 查询工单分页
export const getCsTaskPage = async (params: any) => {
  return await request.get({ url: '/opshub/cs-task/page', params })
}

// 查询工单详情
export const getCsTask = async (id: number) => {
  return await request.get({ url: '/opshub/cs-task/get?id=' + id })
}

// 创建工单（经销商提单）
export const createCsTask = async (data: any) => {
  return await request.post({ url: '/opshub/cs-task/create', data })
}

// 执行员接单
export const acceptTask = async (id: number) => {
  return await request.post({ url: '/opshub/cs-task/accept?id=' + id })
}

// 执行员转单
export const transferTask = async (data: any) => {
  return await request.post({ url: '/opshub/cs-task/transfer', data })
}

// 执行员交付工单
export const deliverTask = async (id: number) => {
  return await request.post({ url: '/opshub/cs-task/deliver?id=' + id })
}

// 经销商验收工单
export const verifyTask = async (data: any) => {
  return await request.post({ url: '/opshub/cs-task/verify', data })
}

// 退回后重新处理
export const reprocessTask = async (id: number) => {
  return await request.post({ url: '/opshub/cs-task/reprocess?id=' + id })
}

// 催办工单
export const urgeTask = async (id: number) => {
  return await request.post({ url: '/opshub/cs-task/urge?id=' + id })
}
