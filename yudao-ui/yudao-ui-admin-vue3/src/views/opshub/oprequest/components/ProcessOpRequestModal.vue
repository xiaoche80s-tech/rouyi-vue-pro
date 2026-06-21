<template>
  <el-dialog v-model="visible" title="上传盖章文件" width="560px">
    <div v-loading="loadingOpRequest" class="p-10px">
      <el-descriptions :column="1" border class="mb-16px">
        <el-descriptions-item label="合同编码">{{ contractInfo.contractCode }}</el-descriptions-item>
        <el-descriptions-item label="合同名称">{{ contractInfo.contractName }}</el-descriptions-item>
      </el-descriptions>

      <!-- 附件上传 -->
      <div class="mb-16px">
        <div class="text-13px font-semibold text-gray-600 mb-8px">上传盖章文件</div>
        <el-upload
          :action="uploadUrl"
          :http-request="handleUpload"
          :show-file-list="false"
          :before-upload="beforeUpload"
        >
          <el-button type="primary" plain>
            <Icon icon="ep:upload-filled" class="mr-4px" />上传文件
          </el-button>
        </el-upload>
        <div v-if="attachments.length > 0" class="mt-12px">
          <div class="text-13px font-semibold text-gray-600 mb-6px">
            已上传附件（{{ attachments.length }}）
          </div>
          <div class="border border-gray-200 rounded divide-y divide-gray-100">
            <div
              v-for="att in attachments"
              :key="att.id"
              class="flex items-center justify-between px-12px py-8px"
            >
              <div class="flex items-center gap-8px flex-1 min-w-0">
                <Icon icon="ep:document" class="text-blue-400 text-14px flex-shrink-0" />
                <span class="text-13px truncate">{{ att.fileName }}</span>
                <span class="text-12px text-gray-400 flex-shrink-0">{{ formatFileSize(att.fileSize) }}</span>
              </div>
              <el-button link type="danger" @click="handleDeleteAttachment(att.id)">删除</el-button>
            </div>
          </div>
        </div>
        <div class="text-12px text-gray-400 mt-4px">
          上传盖章后的合同扫描件，提交后合同将进入验收环节。
        </div>
      </div>
    </div>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" :disabled="attachments.length === 0" @click="handleSubmit">确认提交</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import * as OpRequestApi from '@/api/opshub/oprequest'
import * as CsAttachmentApi from '@/api/opshub/csAttachment'
import type { CsAttachmentVO } from '@/api/opshub/csAttachment'
import { useUpload } from '@/components/UploadFile/src/useUpload'

const emit = defineEmits<{ (e: 'success'): void }>()

const { uploadUrl, httpRequest } = useUpload()

const visible = ref(false)
const loadingOpRequest = ref(false)
const submitting = ref(false)
const opRequestId = ref<number | null>(null)
const contractInfo = ref<{ contractCode: string; contractName: string }>({ contractCode: '', contractName: '' })
const attachments = ref<CsAttachmentVO[]>([])

const open = async (row: any) => {
  visible.value = true
  loadingOpRequest.value = true
  contractInfo.value = { contractCode: row.contractCode || '', contractName: row.contractName || '' }
  opRequestId.value = null
  attachments.value = []

  try {
    const opRequest = await OpRequestApi.getActiveByContractId(row.id)
    if (opRequest) {
      opRequestId.value = opRequest.id
      await loadAttachments()
    }
  } catch (e) {
    console.error('查找操作请求失败', e)
  } finally {
    loadingOpRequest.value = false
  }
}

const beforeUpload = (file: any) => {
  const isLt20M = file.size / 1024 / 1024 < 20
  if (!isLt20M) {
    ElMessage.warning('文件大小不能超过 20MB')
    return false
  }
  return true
}

const handleUpload = async (options: any) => {
  if (!opRequestId.value) {
    ElMessage.error('未找到关联的操作请求')
    return
  }
  try {
    const res = await httpRequest(options) as any
    const fileUrl = res?.data || res
    await CsAttachmentApi.uploadAttachment({
      module: 'op-request',
      businessId: opRequestId.value,
      fileUrl: typeof fileUrl === 'string' ? fileUrl : String(fileUrl),
      fileName: options.file.name,
      fileSize: options.file.size,
      fileType: options.file.type
    })
    ElMessage.success('文件上传成功')
    await loadAttachments()
  } catch (e) {
    console.error('上传附件失败', e)
    ElMessage.error('上传失败，请重试')
  }
}

const loadAttachments = async () => {
  if (!opRequestId.value) return
  try {
    attachments.value = await CsAttachmentApi.getAttachmentList('op-request', opRequestId.value)
  } catch (e) {
    attachments.value = []
  }
}

const handleDeleteAttachment = async (id: number) => {
  await ElMessageBox.confirm('确认删除该附件？', '提示', { type: 'warning' })
  await CsAttachmentApi.deleteAttachment(id)
  ElMessage.success('已删除')
  await loadAttachments()
}

const handleSubmit = async () => {
  if (!opRequestId.value) {
    ElMessage.error('未找到关联的操作请求')
    return
  }
  if (attachments.value.length === 0) {
    ElMessage.warning('请至少上传一个盖章文件')
    return
  }
  submitting.value = true
  try {
    await OpRequestApi.submitOpRequestResult({ id: opRequestId.value })
    ElMessage.success('提交成功，合同已进入验收环节')
    visible.value = false
    emit('success')
  } catch (e) {
    console.error('提交失败', e)
  } finally {
    submitting.value = false
  }
}

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

defineExpose({ open })
</script>
