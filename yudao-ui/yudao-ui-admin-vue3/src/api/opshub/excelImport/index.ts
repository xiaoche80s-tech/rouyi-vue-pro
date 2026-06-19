import request from '@/config/axios'

// 下载导入模板（含示例数据）
export const getImportTemplate = (type: string) => {
  return request.download({ url: `/opshub/excel-import/template/${type}` })
}

// 批量打包下载全部导入模板（ZIP）
export const downloadAllTemplates = () => {
  return request.download({ url: '/opshub/excel-import/template/all' })
}

// 导入 Excel
export const importExcel = (type: string, file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.upload({ url: `/opshub/excel-import/import/${type}`, data: formData })
}
