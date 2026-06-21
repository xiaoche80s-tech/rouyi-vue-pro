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
  processInstanceId: string
}

// 查询工单分页
export const getCsTaskPage = async (params: any) => {
  return await request.get({ url: '/opshub/cs-task/page', params })
}

// 获取各子标签工单数量
export const getTabCounts = async () => {
  return await request.get({ url: '/opshub/cs-task/tab-counts' })
}

// 查询工单详情
export const getCsTask = async (id: number) => {
  return await request.get({ url: '/opshub/cs-task/get?id=' + id })
}

// 创建工单（经销商提单）
export const createCsTask = async (data: any) => {
  return await request.post({ url: '/opshub/cs-task/create', data })
}

// 执行员转单
export const transferTask = async (data: any) => {
  return await request.post({ url: '/opshub/cs-task/transfer', data })
}

// 执行员提交审批
export const submitForApproval = async (id: number, reason?: string) => {
  return await request.post({
    url: '/opshub/cs-task/submit-for-approval?id=' + id + (reason ? '&reason=' + encodeURIComponent(reason) : '')
  })
}

// 取消/关闭工单
export const cancelTask = async (id: number, reason?: string) => {
  return await request.post({ url: '/opshub/cs-task/cancel?id=' + id + (reason ? '&reason=' + encodeURIComponent(reason) : '') })
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

// 获取工单 BPM 候选人列表
export const getTaskCandidateUsers = async (id: number) => {
  return await request.get({ url: '/opshub/cs-task/candidate-users?id=' + id })
}

// 获取工单当前运行的 BPM 任务 ID（用于转单调用 /bpm/task/transfer）
export const getBpmTaskId = async (csTaskId: number) => {
  return await request.get({ url: '/opshub/cs-task/bpm-task-id?id=' + csTaskId })
}
