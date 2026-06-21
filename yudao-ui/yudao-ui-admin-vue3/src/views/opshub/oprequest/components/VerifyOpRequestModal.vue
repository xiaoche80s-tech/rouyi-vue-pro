<template>
  <el-dialog v-model="visible" title="验收操作请求" width="480px">
    <div v-loading="loadingOpRequest" class="p-10px">
      <el-descriptions :column="1" border class="mb-16px">
        <el-descriptions-item label="请求编号">{{ opRequestInfo.requestNo }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ opRequestInfo.requestTypeName }}</el-descriptions-item>
        <el-descriptions-item label="合同编码">{{ opRequestInfo.contractCode }}</el-descriptions-item>
      </el-descriptions>

      <el-form label-width="80px">
        <el-form-item label="验收结果" required>
          <el-radio-group v-model="passed">
            <el-radio :value="true">验收通过</el-radio>
            <el-radio :value="false">验收不通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="!passed" label="拒绝原因">
          <el-input
            v-model="rejectReason"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="请填写拒绝原因"
          />
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确认</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { ElMessage } from 'element-plus'
import * as OpRequestApi from '@/api/opshub/oprequest'

const emit = defineEmits<{ (e: 'success'): void }>()

const visible = ref(false)
const loadingOpRequest = ref(false)
const submitting = ref(false)
const opRequestId = ref<number | null>(null)
const opRequestInfo = ref<{ requestNo: string; requestTypeName: string; contractCode: string }>({
  requestNo: '',
  requestTypeName: '',
  contractCode: ''
})
const passed = ref(true)
const rejectReason = ref('')

const open = async (row: any) => {
  visible.value = true
  loadingOpRequest.value = true
  passed.value = true
  rejectReason.value = ''
  opRequestId.value = null
  opRequestInfo.value = { requestNo: '', requestTypeName: '', contractCode: '' }

  try {
    const opRequest = await OpRequestApi.getActiveByContractId(row.id)
    if (opRequest) {
      opRequestId.value = opRequest.id
      opRequestInfo.value = {
        requestNo: opRequest.requestNo || '',
        requestTypeName: opRequest.requestTypeName || '',
        contractCode: opRequest.contractCode || row.contractCode || ''
      }
    }
  } catch (e) {
    console.error('查找操作请求失败', e)
  } finally {
    loadingOpRequest.value = false
  }
}

const handleSubmit = async () => {
  if (!opRequestId.value) {
    ElMessage.error('未找到关联的操作请求')
    return
  }
  if (!passed.value && !rejectReason.value) {
    ElMessage.warning('请填写拒绝原因')
    return
  }
  submitting.value = true
  try {
    await OpRequestApi.verifyOpRequest({
      id: opRequestId.value,
      passed: passed.value,
      rejectReason: passed.value ? undefined : rejectReason.value
    })
    ElMessage.success(passed.value ? '验收通过' : '已退回')
    visible.value = false
    emit('success')
  } catch (e) {
    console.error('验收失败', e)
  } finally {
    submitting.value = false
  }
}

defineExpose({ open })
</script>
