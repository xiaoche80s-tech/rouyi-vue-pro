import request from '@/config/axios'

// 产品线 VO
export interface ProductLineVO {
  id: number
  name: string
  code: string
  sort: number
  status: number
  remark: string
  createTime: Date
}

// 产品线精简 VO
export interface ProductLineSimpleVO {
  id: number
  name: string
}

// 查询产品线分页
export const getProductLinePage = async (params: PageParam) => {
  return await request.get({ url: '/opshub/product-line/page', params })
}

// 查询产品线详情
export const getProductLine = async (id: number) => {
  return await request.get({ url: '/opshub/product-line/get?id=' + id })
}

// 新增产品线
export const createProductLine = async (data: ProductLineVO) => {
  return await request.post({ url: '/opshub/product-line/create', data })
}

// 修改产品线
export const updateProductLine = async (data: ProductLineVO) => {
  return await request.put({ url: '/opshub/product-line/update', data })
}

// 删除产品线
export const deleteProductLine = async (id: number) => {
  return await request.delete({ url: '/opshub/product-line/delete?id=' + id })
}

// 获得产品线精简列表（开启状态）
export const getSimpleProductLineList = async (): Promise<ProductLineSimpleVO[]> => {
  return await request.get({ url: '/opshub/product-line/simple-list' })
}

// ========== 经销商绑定 ==========

// 绑定经销商
export const bindDealer = async (productLineId: number, dealerId: number) => {
  return await request.post({ url: '/opshub/product-line/bind-dealer', data: { productLineId, dealerId } })
}

// 解绑经销商
export const unbindDealer = async (productLineId: number, dealerId: number) => {
  return await request.delete({ url: '/opshub/product-line/unbind-dealer', params: { productLineId, dealerId } })
}

// 获得产品线已绑定的经销商
export const getProductLineDealers = async (productLineId: number) => {
  return await request.get({ url: '/opshub/product-line/dealers?productLineId=' + productLineId })
}
