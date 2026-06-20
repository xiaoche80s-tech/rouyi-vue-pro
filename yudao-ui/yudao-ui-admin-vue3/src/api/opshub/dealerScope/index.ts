import request from '@/config/axios'

// ========== 授权用户响应类型 ==========

export interface ProductLineItemVO {
  productLineCode: string
  productLineName: string
}

export interface DealerItemVO {
  dealerCode: string
  dealerName: string
}

export interface ExecutorScopeUserRespVO {
  userId: number
  nickname: string
  productLines: ProductLineItemVO[]
}

export interface DealerScopeUserRespVO {
  userId: number
  nickname: string
  dealers: DealerItemVO[]
}

// ========== 用户经销商授权 ==========

// 查看用户授权的经销商
export const getDealerCodesByUserId = async (userId: number) => {
  return await request.get({ url: '/opshub/dealer-scope/list', params: { userId } })
}

// 分配用户经销商授权
export const assignDealerScope = async (userId: number, dealerCodes: string[]) => {
  return await request.post({ url: '/opshub/dealer-scope/assign', data: { userId, dealerCodes } })
}

// 获取所有有经销商授权的用户列表
export const getDealerScopeUsers = async (): Promise<DealerScopeUserRespVO[]> => {
  return await request.get({ url: '/opshub/dealer-scope/scope-users' })
}

// 获取当前登录用户授权的经销商列表
export const getMyDealers = async (): Promise<DealerItemVO[]> => {
  return await request.get({ url: '/opshub/dealer-scope/my-dealers' })
}

// ========== 执行员产品线授权 ==========

// 查看用户授权的产品线
export const getProductLineCodesByUserId = async (userId: number) => {
  return await request.get({ url: '/opshub/product-line-scope/list', params: { userId } })
}

// 分配用户产品线授权
export const assignProductLineScope = async (userId: number, productLineCodes: string[]) => {
  return await request.post({
    url: '/opshub/product-line-scope/assign',
    data: { userId, productLineCodes }
  })
}

// 获取所有有产品线授权的用户列表
export const getExecutorScopeUsers = async (): Promise<ExecutorScopeUserRespVO[]> => {
  return await request.get({ url: '/opshub/product-line-scope/scope-users' })
}
