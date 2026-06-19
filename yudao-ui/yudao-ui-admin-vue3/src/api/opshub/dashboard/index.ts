import request from '@/config/axios'

// ========== 类型定义 ==========

export interface QuickAction {
  label: string
  icon: string
  route: string
}

export interface SigningBlock {
  statistics?: {
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
  }
  trend?: {
    period: string
    mainCount: number
    policyCount: number
    supplementCount: number
    terminationCount: number
    totalCount: number
  }[]
}

export interface OrderBlock {
  statistics?: {
    totalCount: number
    completedCount: number
    paidCount: number
    unpaidCount: number
    invoicedCount: number
    uninvoicedCount: number
    totalAmount: number
  }
}

export interface AfterSaleBlock {
  statistics?: {
    totalCount: number
    refundCount: number
    returnCount: number
    completedCount: number
    completedRate: number
    inProgressCount: number
  }
}

export interface CsTaskBlock {
  tabCounts?: Record<string, number>
}

export interface CsConsultBlock {
  statistics?: {
    totalCount: number
    pendingCount: number
    processingCount: number
    completedCount: number
    closedCount: number
  }
}

export interface CsOpReqBlock {
  statistics?: {
    totalCount: number
    pendingCount: number
    inProgressCount: number
    pendingVerifyCount: number
    completedCount: number
  }
}

export interface DashboardVO {
  role: string
  signing?: SigningBlock
  order?: OrderBlock
  aftersale?: AfterSaleBlock
  csTask?: CsTaskBlock
  csConsult?: CsConsultBlock
  csOpReq?: CsOpReqBlock
  quickActions?: QuickAction[]
}

// 获取角色化仪表盘数据
export const getDashboard = async (): Promise<DashboardVO> => {
  return await request.get({ url: '/opshub/dashboard' })
}
