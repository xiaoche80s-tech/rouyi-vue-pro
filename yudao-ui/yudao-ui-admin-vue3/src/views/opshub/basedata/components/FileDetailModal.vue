<template>
  <el-dialog v-model="dialogVisible" title="文件明细" width="600px">
    <el-descriptions :column="2" border>
      <el-descriptions-item label="文件名称" :span="2">{{ fileData.fileName }}</el-descriptions-item>
      <el-descriptions-item label="文件编号">{{ fileData.fileNo || '-' }}</el-descriptions-item>
      <el-descriptions-item label="文件类型">{{ fileData.fileTypeName }}</el-descriptions-item>
      <el-descriptions-item label="文件分类">{{ getCategoryLabel(fileData.category) }}</el-descriptions-item>
      <el-descriptions-item label="经销商">{{ fileData.dealerName }}</el-descriptions-item>
      <el-descriptions-item label="有效期至">{{ fileData.expireDate || '-' }}</el-descriptions-item>
      <el-descriptions-item label="有效期状态">
        <el-tag
          v-if="fileData.expireStatus"
          :type="getExpireStatusType(fileData.expireStatus)"
        >
          {{ getExpireStatusLabel(fileData.expireStatus) }}
        </el-tag>
        <span v-else>-</span>
      </el-descriptions-item>
      <el-descriptions-item label="文件大小">{{ formatFileSize(fileData.fileSize) }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="fileData.status" />
      </el-descriptions-item>
      <el-descriptions-item label="文件描述" :span="2">{{ fileData.description || '-' }}</el-descriptions-item>
      <el-descriptions-item label="备注" :span="2">{{ fileData.remark || '-' }}</el-descriptions-item>
      <el-descriptions-item label="创建时间" :span="2">
        {{ formatNullableDate(fileData.createTime) }}
      </el-descriptions-item>
    </el-descriptions>
    <template #footer>
      <el-button
        v-hasPermi="['dealer:basedata:download']"
        type="success"
        @click="handleDownload"
      >
        下载
      </el-button>
      <el-button
        v-hasPermi="['dealer:basedata:ai']"
        type="info"
        @click="handleAiInterpret"
      >
        AI 解读
      </el-button>
      <el-button @click="dialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { DICT_TYPE } from '@/utils/dict'
import { formatNullableDate } from '@/utils/formatTime'
import type { BasedataFileVO } from '@/api/opshub/basedata'

const emit = defineEmits(['download', 'aiInterpret'])

const dialogVisible = ref(false)
const fileData = ref<BasedataFileVO>({} as BasedataFileVO)

/** 打开弹窗 */
const open = (file: BasedataFileVO) => {
  fileData.value = file
  dialogVisible.value = true
}

/** 下载 */
const handleDownload = () => {
  emit('download', fileData.value)
}

/** AI 解读 */
const handleAiInterpret = () => {
  emit('aiInterpret', fileData.value)
}

/** 分类名称 */
const getCategoryLabel = (category: string) => {
  const map: Record<string, string> = {
    qualification: '资质文件',
    authorization: '授权文件',
    contract: '合同文件',
    product: '产品文件'
  }
  return map[category] || category
}

/** 有效期状态标签 */
const getExpireStatusType = (status: string) => {
  switch (status) {
    case 'valid': return 'success'
    case 'expiring_soon': return 'warning'
    case 'expired': return 'danger'
    default: return 'info'
  }
}

const getExpireStatusLabel = (status: string) => {
  switch (status) {
    case 'valid': return '有效'
    case 'expiring_soon': return '即将到期'
    case 'expired': return '已过期'
    default: return status
  }
}

/** 格式化文件大小 */
const formatFileSize = (size: number) => {
  if (!size) return '-'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
  return (size / 1024 / 1024).toFixed(2) + ' MB'
}

defineExpose({ open })
</script>
