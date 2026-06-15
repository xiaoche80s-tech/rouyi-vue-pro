<template>
  <el-dialog v-model="visible" title="申请开票" width="560px" destroy-on-close>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="订单号">
        <el-input :model-value="orderCode" disabled />
      </el-form-item>
      <el-form-item label="发票抬头" prop="companyName">
        <el-input v-model="form.companyName" placeholder="请输入公司全称" />
      </el-form-item>
      <el-form-item label="税号" prop="taxNo">
        <el-input v-model="form.taxNo" placeholder="请输入纳税人识别号" />
      </el-form-item>
      <el-form-item label="特殊需求" prop="specialRequest">
        <el-input
          v-model="form.specialRequest"
          type="textarea"
          :rows="2"
          placeholder="如：增值税专用发票 / 普通发票等（选填）"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="success" :loading="submitting" @click="handleSubmit">确认申请</el-button>
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

const form = reactive({
  companyName: '',
  taxNo: '',
  specialRequest: ''
})

const rules = {
  companyName: [{ required: true, message: '请输入发票抬头', trigger: 'blur' }],
  taxNo: [{ required: true, message: '请输入税号', trigger: 'blur' }]
}

const open = (id: number, code: string) => {
  orderId.value = id
  orderCode.value = code
  form.companyName = ''
  form.taxNo = ''
  form.specialRequest = ''
  visible.value = true
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    await OrderApi.applyInvoice({
      orderId: orderId.value,
      companyName: form.companyName,
      taxNo: form.taxNo,
      specialRequest: form.specialRequest
    })
    message.success('开票申请已提交')
    visible.value = false
    emit('success')
  } catch (e) {
    console.error('开票申请失败', e)
  } finally {
    submitting.value = false
  }
}

defineExpose({ open })
</script>
