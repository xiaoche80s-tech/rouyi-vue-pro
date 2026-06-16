import request from '@/config/axios'

// 附件 VO
export interface CsAttachmentVO {
  id: number
  module: string
  businessId: number
  businessCode: string
  fileName: string
  fileUrl: string
  fileSize: number
  fileType: string
  remark: string
  createTime: string
}

// 上传附件
export const uploadAttachment = async (data: any) => {
  return await request.post({ url: '/opshub/cs-attachment/upload', data })
}

// 查询附件列表
export const getAttachmentList = async (module: string, businessId: number) => {
  return await request.get({ url: '/opshub/cs-attachment/list', params: { module, businessId } })
}

// 删除附件
export const deleteAttachment = async (id: number) => {
  return await request.delete({ url: '/opshub/cs-attachment/delete?id=' + id })
}
