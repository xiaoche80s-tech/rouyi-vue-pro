import request from '@/config/axios'

// ========== 类型定义 ==========

export interface AfterSaleSimpleVO {
  id: number
  aftersaleCode: string
  dealerName: string
  productLineName: string
  orderCode: string
  handlingMethod: string
  reason: string
  progressStatus: string
  currentStep: number
  productName: string
  applyTime: string
  progressNodes: ProgressNodePreview[]
}

export interface ProgressNodePreview {
  nodeName: string
  nodeCode: string
  isCompleted: boolean
  nodeTime: string | null
}

export interface AfterSaleStatisticsVO {
  totalCount: number
  refundCount: number
  returnCount: number
  completedCount: number
  completedRate: number
  inProgressCount: number
}

export interface AfterSaleDetailVO {
  id: number
  aftersaleCode: string
  dealerName: string
  productLineName: string
  orderCode: string
  handlingMethod: string
  handlingMethodName: string
  reason: string
  reasonName: string
  progressStatus: string
  currentStep: number
  productName: string
  productSpec: string
  quantity: number
  refundAmount: number
  refundStatus: string
  redInvoiceStatus: string
  logisticsCompany: string
  logisticsNo: string
  exchangeLogisticsCompany: string
  exchangeLogisticsNo: string
  applyTime: string
  approvedTime: string | null
  completedTime: string | null
  remark: string
  progressNodes: AfterSaleProgressNode[]
}

export interface AfterSaleProgressNode {
  id: number
  aftersaleId: number
  aftersaleCode: string
  nodeCode: string
  nodeName: string
  nodeTime: string | null
  isCompleted: boolean
  sortOrder: number
  remark: string | null
}

export interface AfterSalePageParams {
  pageNo: number
  pageSize: number
  handlingMethod?: string
  reason?: string
  progressStatus?: string
  productLineCodes?: string[]
  dealerCodes?: string[]
  keyword?: string
  sortField?: string
  sortOrder?: string
}

export interface AfterSaleUpdateProgressParams {
  aftersaleId: number
  stepOrder: number
  remark?: string
  logisticsCompany?: string
  logisticsNo?: string
  refundAmount?: number
  redInvoiceNo?: string
}

// ========== API ==========

// 分页查询
export const getAfterSalePage = (params: AfterSalePageParams) => {
  return request.get({ url: '/opshub/aftersale/page', params })
}

// 获取详情
export const getAfterSaleDetail = (id: number) => {
  return request.get({ url: '/opshub/aftersale/get', params: { id } })
}

// 统计数据
export const getAfterSaleStatistics = () => {
  return request.get({ url: '/opshub/aftersale/statistics' })
}

// 更新进度
export const updateAfterSaleProgress = (data: AfterSaleUpdateProgressParams) => {
  return request.put({ url: '/opshub/aftersale/update-progress', data })
}

// 批量咨询
export const batchAfterSaleConsult = (aftersaleIds: number[]) => {
  return request.post({ url: '/opshub/aftersale/batch-consult', data: aftersaleIds })
}
