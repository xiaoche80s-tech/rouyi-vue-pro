import request from '@/config/axios'

// 经销商信息 VO
export interface DealerVO {
  id: number
  dealerName: string
  dealerCode: string
  contactName: string
  contactPhone: string
  address: string
  status: number
  remark: string
  createTime: Date
}

// 产品线 VO（精简）
export interface ProductLineSimpleVO {
  id: number
  productLineName: string
  productLineCode: string
}

// 经销商 VO（精简）
export interface DealerSimpleVO {
  id: number
  dealerName: string
  dealerCode: string
}

// 产品线 VO（完整）
export interface ProductLineVO {
  id: number
  productLineName: string
  productLineCode: string
  sort: number
  status: number
  remark: string
  createTime: Date
}

// ========== 经销商 CRUD ==========

// 查询经销商分页
export const getDealerPage = async (params: PageParam) => {
  return await request.get({ url: '/opshub/dealer/page', params })
}

// 查询经销商详情
export const getDealer = async (id: number) => {
  return await request.get({ url: '/opshub/dealer/get?id=' + id })
}

// 新增经销商
export const createDealer = async (data: DealerVO) => {
  return await request.post({ url: '/opshub/dealer/create', data })
}

// 修改经销商
export const updateDealer = async (data: DealerVO) => {
  return await request.put({ url: '/opshub/dealer/update', data })
}

// 删除经销商
export const deleteDealer = async (id: number) => {
  return await request.delete({ url: '/opshub/dealer/delete?id=' + id })
}

// 获得经销商精简列表（开启状态）
export const getSimpleDealerList = async (): Promise<DealerSimpleVO[]> => {
  return await request.get({ url: '/opshub/dealer/simple-list' })
}

// 获得产品线精简列表（开启状态）
export const getSimpleProductLineList = async (): Promise<ProductLineSimpleVO[]> => {
  return await request.get({ url: '/opshub/product-line/simple-list' })
}

