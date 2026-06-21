import request from '@/config/axios'

// 操作请求 VO
export interface OpRequestVO {
  id: number
  requestNo: string
  requestType: string
  requestTypeName: string
  dealerId: number
  dealerCode: string
  requestStatus: string
  processInstanceId: string
  assigneeId: number
  remark: string
  // 签约子表字段
  contractId?: number
  contractCode?: string
  contractName?: string
  createTime: Date
  updateTime: Date
}

// 创建操作请求
export const createOpRequest = async (data: any) => {
  return await request.post({ url: '/opshub/op-request/create', data })
}

// 操作请求分页
export const getOpRequestPage = async (params: any) => {
  return await request.get({ url: '/opshub/op-request/page', params })
}

// 操作请求详情
export const getOpRequest = async (id: number): Promise<OpRequestVO> => {
  return await request.get({ url: '/opshub/op-request/get?id=' + id })
}

// 提交处理结果
export const submitOpRequestResult = async (data: any) => {
  return await request.post({ url: '/opshub/op-request/submit-result', data })
}

// 经销商验收
export const verifyOpRequest = async (data: any) => {
  return await request.post({ url: '/opshub/op-request/verify', data })
}

// 根据合同ID查找进行中的操作请求
export const getActiveByContractId = async (contractId: number): Promise<OpRequestVO | null> => {
  return await request.get({ url: '/opshub/op-request/get-active-by-contract', params: { contractId } })
}
