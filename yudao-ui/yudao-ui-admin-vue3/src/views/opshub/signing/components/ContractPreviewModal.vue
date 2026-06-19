<template>
  <el-dialog v-model="visible" title="合同详情" width="640px">
    <el-descriptions v-if="contract" :column="2" border>
      <el-descriptions-item label="合同编码">{{ contract.contractCode }}</el-descriptions-item>
      <el-descriptions-item label="合同类型">{{ contract.contractTypeName }}</el-descriptions-item>
      <el-descriptions-item label="合同名称" :span="2">{{ contract.contractName }}</el-descriptions-item>
      <el-descriptions-item label="经销商">{{ contract.dealerName }}</el-descriptions-item>
      <el-descriptions-item label="产品线">{{ contract.productLineCode || '-' }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="contract.status === 'signed' ? 'success' : 'warning'">
          {{ contract.status === 'signed' ? '已签署' : '未签署' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="子状态">{{ contract.subStatus === 'signing' ? '签署中' : contract.subStatus === 'pending' ? '待签署' : '-' }}</el-descriptions-item>
      <el-descriptions-item label="下发日期">{{ contract.issuedDate }}</el-descriptions-item>
      <el-descriptions-item label="签署日期">{{ contract.signDate || '-' }}</el-descriptions-item>
      <el-descriptions-item label="摘要" :span="2">{{ contract.summary || '-' }}</el-descriptions-item>
      <el-descriptions-item v-if="contract.policyAnalysis" label="政策解析" :span="2">{{ contract.policyAnalysis }}</el-descriptions-item>
      <el-descriptions-item v-if="contract.remark" label="备注" :span="2">{{ contract.remark }}</el-descriptions-item>
      <el-descriptions-item v-if="contract.signProofUrl" label="签署凭证" :span="2">
        <a :href="contract.signProofUrl" target="_blank" class="text-blue-600">查看盖章文件</a>
      </el-descriptions-item>
    </el-descriptions>

    <!-- 附件区块 -->
    <div class="mt-16px" v-loading="attachmentLoading">
      <div class="text-14px font-bold mb-8px flex items-center gap-4px">
        <Icon icon="ep:paperclip" />
        <span>合同附件 ({{ attachments.length }})</span>
      </div>

      <div v-if="!attachmentLoading && attachments.length === 0" class="text-13px text-gray-400 py-12px text-center bg-gray-50 rounded">
        暂无合同附件
      </div>

      <div v-else-if="attachments.length > 0" class="border border-gray-200 rounded divide-y divide-gray-100">
        <div
          v-for="file in attachments"
          :key="file.id"
          class="flex items-center justify-between px-12px py-8px hover:bg-gray-50 transition-colors"
        >
          <div class="flex items-center gap-8px min-w-0 flex-1">
            <Icon icon="ep:document" class="text-blue-500 text-14px flex-shrink-0" />
            <span class="text-13px truncate" :title="file.fileName">{{ file.fileName }}</span>
            <el-tag size="small" type="info">{{ file.fileType }}</el-tag>
            <span class="text-12px text-gray-400 flex-shrink-0">{{ formatFileSize(file.fileSize) }}</span>
          </div>
          <el-button link type="primary" class="flex-shrink-0 ml-8px" @click="handleDownload(file.id)">
            <Icon icon="ep:download" class="mr-2px" />下载
          </el-button>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script lang="ts" setup>
import type { SigningContractVO, ContractAttachmentVO } from '@/api/opshub/signing'
import * as SigningApi from '@/api/opshub/signing'
import * as BasedataApi from '@/api/opshub/basedata'

const visible = ref(false)
const contract = ref<SigningContractVO | null>(null)
const attachments = ref<ContractAttachmentVO[]>([])
const attachmentLoading = ref(false)

const formatFileSize = (bytes: number) => {
  if (!bytes || bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let idx = 0
  let size = bytes
  while (size >= 1024 && idx < units.length - 1) {
    size /= 1024
    idx++
  }
  return size.toFixed(idx > 0 ? 1 : 0) + ' ' + units[idx]
}

const handleDownload = async (fileId: number) => {
  try {
    const url = await BasedataApi.getDownloadUrl(fileId)
    if (url) {
      window.open(url, '_blank')
    } else {
      ElMessage.warning('文件地址为空，无法下载')
    }
  } catch {
    ElMessage.error('获取下载链接失败')
  }
}

const loadAttachments = async (contractId: number) => {
  attachmentLoading.value = true
  try {
    attachments.value = await SigningApi.getContractAttachments(contractId)
  } catch {
    attachments.value = []
  } finally {
    attachmentLoading.value = false
  }
}

const open = async (row: SigningContractVO) => {
  contract.value = row
  visible.value = true
  await loadAttachments(row.id)
}

defineExpose({ open })
</script>
