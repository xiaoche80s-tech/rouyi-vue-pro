import request from '@/config/axios'

// ========== 用户经销商授权 ==========

// 查看用户授权的经销商
export const getDealerIdsByUserId = async (userId: number) => {
  return await request.get({ url: '/opshub/dealer-scope/list', params: { userId } })
}

// 分配用户经销商授权
export const assignDealerScope = async (userId: number, dealerIds: number[]) => {
  return await request.post({ url: '/opshub/dealer-scope/assign', data: { userId, dealerIds } })
}

// ========== 执行员产品线授权 ==========

// 查看用户授权的产品线
export const getProductLineIdsByUserId = async (userId: number) => {
  return await request.get({ url: '/opshub/product-line-scope/list', params: { userId } })
}

// 分配用户产品线授权
export const assignProductLineScope = async (userId: number, productLineIds: number[]) => {
  return await request.post({
    url: '/opshub/product-line-scope/assign',
    data: { userId, productLineIds }
  })
}
