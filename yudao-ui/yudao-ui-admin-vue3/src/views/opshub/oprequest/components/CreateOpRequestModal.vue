<template>
  <el-dialog v-model="visible" title="确认签署" width="480px">
    <div v-if="contract" class="p-10px">
      <p class="mb-12px">确认发起以下合同的签署流程？</p>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="合同编码">{{ contract.contractCode }}</el-descriptions-item>
        <el-descriptions-item label="合同名称">{{ contract.contractName }}</el-descriptions-item>
        <el-descriptions-item label="合同类型">{{ contract.contractTypeName }}</el-descriptions-item>
      </el-descriptions>
      <el-form label-width="80px" class="mt-16px">
        <el-form-item label="签署备注">
          <el-input
            v-model="remark"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="可选，填写签署备注"
          />
        </el-form-item>
      </el-form>
      <div class="text-12px text-gray-400">
        发起后，合同将进入线下签署流程（盖章、寄送），由执行员处理。
      </div>
    </div>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确认发起</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import * as OpRequestApi from '@/api/opshub/oprequest'

const message = useMessage()
const emit = defineEmits<{ (e: 'success'): void }>()

const visible = ref(false)
const submitting = ref(false)
const contract = ref<any>(null)
const requestType = ref('signing')
const remark = ref('')

const open = (type: string, row: any) => {
  requestType.value = type
  contract.value = row
  remark.value = ''
  visible.value = true
}

const handleSubmit = async () => {
  if (!contract.value) return
  submitting.value = true
  try {
    await OpRequestApi.createOpRequest({
      requestType: requestType.value,
      dealerId: contract.value.dealerId,
      dealerCode: contract.value.dealerCode,
      contractId: contract.value.id,
      remark: remark.value || undefined
    })
    message.success('操作请求发起成功')
    visible.value = false
    emit('success')
  } catch (e) {
    console.error('发起失败', e)
  } finally {
    submitting.value = false
  }
}

defineExpose({ open })
</script>
