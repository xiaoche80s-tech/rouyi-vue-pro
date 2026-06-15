<template>
  <el-dialog v-model="visible" title="确认签署" width="460px">
    <div v-if="contract" class="p-10px">
      <p class="mb-12px">确认发起以下合同的线下签署流程？</p>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="合同编码">{{ contract.contractCode }}</el-descriptions-item>
        <el-descriptions-item label="合同名称">{{ contract.contractName }}</el-descriptions-item>
        <el-descriptions-item label="合同类型">{{ contract.contractTypeName }}</el-descriptions-item>
      </el-descriptions>
      <div class="mt-12px text-12px text-gray-400">
        发起后，合同将进入线下签署流程（盖章、寄送）。
      </div>
    </div>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确认签署</el-button>
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

const open = (row: SigningContractVO) => {
  contract.value = row
  visible.value = true
}

const handleSubmit = async () => {
  if (!contract.value) return
  submitting.value = true
  try {
    await SigningApi.signContract(contract.value.id)
    message.success('签署发起成功')
    visible.value = false
    emit('success')
  } catch (e) {
    console.error('签署失败', e)
  } finally {
    submitting.value = false
  }
}

defineExpose({ open })
</script>
