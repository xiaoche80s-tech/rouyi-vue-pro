import request from '@/config/axios'

// ========== 类型定义 ==========

export interface OrderSimpleVO {
  id: number
  orderCode: string
  dealerCode: string
  dealerName: string
  productLineCode: string
  productLineName: string
  totalAmount: number
  orderDate: string
  progressStatus: string
  payStatus: string
  invStatus: string
  paidAmount: number
  invoicedAmount: number
}

export interface OrderStatisticsVO {
  totalCount: number
  completedCount: number
  paidCount: number
  unpaidCount: number
  invoicedCount: number
  uninvoicedCount: number
  totalAmount: number
}

export interface OrderDetailVO {
  id: number
  orderCode: string
  dealerId: number
  dealerName: string
  productLineName: string
  totalAmount: number
  orderDate: string
  progressStatus: string
  payStatus: string
  invStatus: string
  paidAmount: number
  invoicedAmount: number
  remark: string
  timeline: TimelineNode[]
  products: ProductItem[]
  payments: PaymentRecord[]
  invoices: InvoiceRecord[]
  logistics: LogisticsNode[]
  returnableProducts: ReturnableProduct[]
}

export interface TimelineNode {
  id: number
  nodeCode: string
  nodeName: string
  nodeTime: string | null
  isCompleted: boolean
  sortOrder: number
}

export interface ProductItem {
  id: number
  productCode: string
  productName: string
  specModel: string
  unitPrice: number
  quantity: number
  unit: string
  amount: number
  returnableQty: number
}

export interface PaymentRecord {
  id: number
  payAmount: number
  payDate: string | null
  payMethod: string
  voucherNo: string
  status: string
  applyTime: string
  approveTime: string | null
  remark: string
}

export interface InvoiceRecord {
  id: number
  invoiceAmount: number
  invoiceNo: string | null
  invoiceDate: string | null
  invoiceType: string
  companyName: string
  taxNo: string
  specialRequest: string
  status: string
  applyTime: string
  remark: string
}

export interface LogisticsNode {
  id: number
  logisticsCompany: string
  trackingNo: string
  nodeDesc: string
  nodeTime: string
  isCompleted: boolean
  sortOrder: number
}

export interface ReturnableProduct {
  productId: number
  productCode: string
  productName: string
  specModel: string
  quantity: number
  returnableQty: number
  unitPrice: number
  amount: number
}

// ========== API 函数 ==========

// 分页查询订单列表
export const getOrderPage = async (params: any) => {
  return await request.get({ url: '/opshub/order/page', params })
}

// 获取订单详情
export const getOrderDetail = async (id: number): Promise<OrderDetailVO> => {
  return await request.get({ url: '/opshub/order/get?id=' + id })
}

// 获取统计卡片数据
export const getOrderStatistics = async (): Promise<OrderStatisticsVO> => {
  return await request.get({ url: '/opshub/order/statistics' })
}

// 更新订单进度
export const updateOrderProgress = async (id: number, progressStatus: string) => {
  return await request.put({
    url: '/opshub/order/update-progress',
    params: { id, progressStatus }
  })
}

// 申请付款
export const applyPayment = async (data: { orderId: number; remark?: string }) => {
  return await request.post({ url: '/opshub/order/apply-payment', data })
}

// 申请开票
export const applyInvoice = async (data: {
  orderId: number
  companyName: string
  taxNo: string
  specialRequest?: string
}) => {
  return await request.post({ url: '/opshub/order/apply-invoice', data })
}

// 申请退货
export const applyReturn = async (data: {
  orderId: number
  items: { productId: number; returnQty: number }[]
}) => {
  return await request.post({ url: '/opshub/order/apply-return', data })
}

// 批量申请付款
export const batchApplyPayment = async (orderIds: number[]) => {
  return await request.post({ url: '/opshub/order/batch-apply-payment', data: orderIds })
}

// 批量申请开票
export const batchApplyInvoice = async (orderIds: number[]) => {
  return await request.post({ url: '/opshub/order/batch-apply-invoice', data: orderIds })
}

// 批量申请退货
export const batchApplyReturn = async (data: any[]) => {
  return await request.post({ url: '/opshub/order/batch-apply-return', data })
}

// 批量咨询
export const batchConsult = async (orderIds: number[]) => {
  return await request.post({ url: '/opshub/order/batch-consult', data: orderIds })
}
