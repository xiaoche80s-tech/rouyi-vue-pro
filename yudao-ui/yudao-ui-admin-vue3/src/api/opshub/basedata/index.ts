import request from '@/config/axios'

// 基础数据文件 VO
export interface BasedataFileVO {
  id: number
  dealerId: number
  dealerCode: string
  dealerName: string
  category: string
  fileName: string
  fileType: string
  fileTypeName: string
  fileNo: string
  fileUrl: string
  fileSize: number
  expireDate: string
  expireStatus: string
  status: number
  description: string
  remark: string
  createTime: Date
}

// 分类数量统计
export interface CategoryCountVO {
  [category: string]: number
}

// 查询基础数据文件分页
export const getBasedataFilePage = async (params: PageParam) => {
  return await request.get({ url: '/opshub/basedata/page', params })
}

// 查询基础数据文件详情
export const getBasedataFile = async (id: number) => {
  return await request.get({ url: '/opshub/basedata/get?id=' + id })
}

// 获得文件预签名下载 URL
export const getDownloadUrl = async (id: number) => {
  return await request.get({ url: '/opshub/basedata/download-url?id=' + id })
}

// 获得各分类文件数量
export const getCategoryCount = async (): Promise<CategoryCountVO> => {
  return await request.get({ url: '/opshub/basedata/category-count' })
}
