<template>
  <el-dialog v-model="visible" title="申请付款" width="500px" destroy-on-close>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="订单号">
        <el-input :model-value="orderCode" disabled />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入付款备注（选填）" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确认申请</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import * as OrderApi from '@/api/opshub/order'

const emit = defineEmits<{ (e: 'success'): void }>()
const message = useMessage()

const visible = ref(false)
const submitting = ref(false)
const orderId = ref<number>(0)
const orderCode = ref('')
const formRef = ref()

const form = reactive({ remark: '' })
const rules = {}

const open = (id: number, code: string) => {
  orderId.value = id
  orderCode.value = code
  form.remark = ''
  visible.value = true
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    await OrderApi.applyPayment({ orderId: orderId.value, remark: form.remark })
    message.success('付款申请已提交')
    visible.value = false
    emit('success')
  } catch (e) {
    console.error('付款申请失败', e)
  } finally {
    submitting.value = false
  }
}

defineExpose({ open })
</script>
