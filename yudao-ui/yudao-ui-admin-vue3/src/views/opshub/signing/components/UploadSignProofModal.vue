<template>
  <el-dialog v-model="visible" title="上传盖章文件" width="500px">
    <div v-if="contract" class="p-10px">
      <el-descriptions :column="1" border class="mb-16px">
        <el-descriptions-item label="合同编码">{{ contract.contractCode }}</el-descriptions-item>
        <el-descriptions-item label="合同名称">{{ contract.contractName }}</el-descriptions-item>
      </el-descriptions>

      <el-form label-width="100px">
        <el-form-item label="盖章文件URL" required>
          <el-input v-model="signProofUrl" placeholder="请输入盖章后的合同扫描件URL" />
        </el-form-item>
        <div class="text-12px text-gray-400 mt-4px">
          上传盖章后的合同扫描件，系统将更新合同状态为"已签署"。
        </div>
      </el-form>
    </div>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确认上传</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import * as SigningApi from '@/api/opshub/signing'
import type { SigningContractVO } from '@/api/opshub/signing'

const message = useMessage()
const emit = defineEmits<{ (e: 'success'): void }>()

const visible = ref(false)
const submitting = ref(false)
const contract = ref<SigningContractVO | null>(null)
const signProofUrl = ref('')

const open = (row: SigningContractVO) => {
  contract.value = row
  signProofUrl.value = row.signProofUrl || ''
  visible.value = true
}

const handleSubmit = async () => {
  if (!contract.value || !signProofUrl.value) {
    message.warning('请输入盖章文件URL')
    return
  }
  submitting.value = true
  try {
    await SigningApi.uploadSignProof(contract.value.id, signProofUrl.value)
    message.success('上传成功，合同已更新为已签署')
    visible.value = false
    emit('success')
  } catch (e) {
    console.error('上传失败', e)
  } finally {
    submitting.value = false
  }
}

defineExpose({ open })
</script>
