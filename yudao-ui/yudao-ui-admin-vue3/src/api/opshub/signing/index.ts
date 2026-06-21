import request from '@/config/axios'

// 签约合同 VO
export interface SigningContractVO {
  id: number
  dealerId: number
  dealerCode: string
  dealerName: string
  productLineCode: string
  productLineName: string
  contractType: string
  contractTypeName: string
  contractCode: string
  contractName: string
  status: string
  subStatus: string
  issuedDate: string
  signDate: string
  summary: string
  policyAnalysis: string
  indicators: string
  attachmentCount: number
  signProofUrl: string
  remark: string
  createTime: Date
  updateTime: Date
}

// 合同附件 VO
export interface ContractAttachmentVO {
  id: number
  fileName: string
  fileType: string
  fileTypeName: string
  fileNo: string
  fileSize: number
  description: string
  createTime: Date
}

// 统计 VO
export interface SigningStatisticsVO {
  totalCount: number
  mainCount: number
  policyCount: number
  supplementCount: number
  terminationCount: number
  signedCount: number
  signedRate: number
  unsignedCount: number
  pendingCount: number
  signingCount: number
  main: ContractTypeStat
  policy: ContractTypeStat
  supplement: ContractTypeStat
  termination: ContractTypeStat
}

export interface ContractTypeStat {
  total: number
  signed: number
  unsigned: number
}

// 趋势 VO
export interface SigningTrendVO {
  period: string
  mainCount: number
  policyCount: number
  supplementCount: number
  terminationCount: number
  totalCount: number
}

// 查询签约合同分页
export const getSigningContractPage = async (params: any) => {
  return await request.get({ url: '/opshub/signing/page', params })
}

// 查询签约合同详情
export const getSigningContract = async (id: number) => {
  return await request.get({ url: '/opshub/signing/get?id=' + id })
}

// 获得统计数据
export const getSigningStatistics = async (): Promise<SigningStatisticsVO> => {
  return await request.get({ url: '/opshub/signing/statistics' })
}

// 获得趋势数据
export const getSigningTrend = async (timeDimension: string): Promise<SigningTrendVO[]> => {
  return await request.get({ url: '/opshub/signing/trend', params: { timeDimension } })
}

// 创建合同
export const createSigningContract = async (data: any) => {
  return await request.post({ url: '/opshub/signing/create', data })
}

// 更新合同
export const updateSigningContract = async (data: any) => {
  return await request.put({ url: '/opshub/signing/update', data })
}


// 获取合同附件列表
export const getContractAttachments = async (contractId: number): Promise<ContractAttachmentVO[]> => {
  return await request.get({ url: '/opshub/signing/attachments', params: { contractId } })
}
