import request from '@/config/axios'

// 操作请求 VO
export interface CsOpReqVO {
  id: number
  opreqCode: string
  opType: string
  status: number
  dealerCode: string
  dealerName: string
  productLineCode: string
  productLineName: string
  sourceModule: string
  sourceId: number
  sourceCode: string
  content: string
  creatorUserId: number
  assigneeId: number
  acceptTime: string
  submitTime: string
  submitRemark: string
  verifyTime: string
  completedTime: string
  remark: string
  createTime: string
  updateTime: string
}

// 查询操作请求分页
export const getOpReqPage = async (params: any) => {
  return await request.get({ url: '/opshub/cs-opreq/page', params })
}

// 查询操作请求详情
export const getOpReq = async (id: number) => {
  return await request.get({ url: '/opshub/cs-opreq/get?id=' + id })
}

// 创建操作请求
export const createOpReq = async (data: any) => {
  return await request.post({ url: '/opshub/cs-opreq/create', data })
}

// 执行员接单
export const acceptOpReq = async (id: number) => {
  return await request.post({ url: '/opshub/cs-opreq/accept?id=' + id })
}

// 执行员提交结果
export const submitOpReq = async (data: any) => {
  return await request.post({ url: '/opshub/cs-opreq/submit', data })
}

// 经销商验收
export const verifyOpReq = async (id: number) => {
  return await request.post({ url: '/opshub/cs-opreq/verify?id=' + id })
}
