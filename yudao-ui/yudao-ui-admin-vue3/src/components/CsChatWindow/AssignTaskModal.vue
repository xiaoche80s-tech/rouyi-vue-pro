<template>
  <el-dialog
    title="下发新任务"
    :model-value="modelValue"
    width="520px"
    append-to-body
    :close-on-click-modal="false"
    @update:model-value="(v: boolean) => emit('update:modelValue', v)"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="90px"
      @submit.prevent
    >
      <!-- 只读：经销商 -->
      <el-form-item label="经销商">
        <span class="text-gray-700">{{ session?.dealerName || '-' }}</span>
      </el-form-item>

      <!-- 只读：产品线 -->
      <el-form-item label="产品线">
        <span class="text-gray-700">{{ session?.productLineName || '-' }}</span>
      </el-form-item>

      <!-- 任务内容 -->
      <el-form-item label="任务内容" prop="content">
        <el-input
          v-model="formData.content"
          type="textarea"
          :rows="4"
          placeholder="例如：追踪售后订单SO2026XXX进度，1天内完成"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <!-- 紧急程度 -->
      <el-form-item label="紧急程度" prop="urgency">
        <el-select v-model="formData.urgency" placeholder="请选择" style="width: 100%" @change="handleUrgencyChange">
          <el-option
            v-for="item in urgencyOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <!-- SLA截止时间 -->
      <el-form-item label="SLA截止时间" prop="slaDeadline">
        <el-date-picker
          v-model="formData.slaDeadline"
          type="datetime"
          placeholder="根据紧急程度自动计算"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 100%"
          :disabled-date="(date: Date) => date.getTime() < Date.now() - 86400000"
        />
      </el-form-item>

      <!-- 特殊备注 -->
      <el-form-item label="特殊备注">
        <el-input
          v-model="formData.remark"
          type="textarea"
          :rows="2"
          placeholder="特殊要求说明..."
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确认下发</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import dayjs from 'dayjs'
import type { CsSessionVO } from '@/api/opshub/csSession'
import { createCsTask } from '@/api/opshub/csTask'

const props = defineProps<{
  modelValue: boolean
  session: CsSessionVO | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

// ========== 紧急程度选项 ==========
const urgencyOptions = [
  { value: 0, label: '紧急 (SLA: 4小时)', hours: 4 },
  { value: 1, label: '高 (SLA: 12小时)', hours: 12 },
  { value: 2, label: '普通 (SLA: 48小时)', hours: 48 },
  { value: 3, label: '低 (SLA: 72小时)', hours: 72 }
]

// ========== 表单 ==========
const formRef = ref<FormInstance>()
const submitting = ref(false)

const formData = reactive({
  content: '',
  urgency: 2, // 默认"普通"
  slaDeadline: '',
  remark: ''
})

const formRules: FormRules = {
  content: [{ required: true, message: '请输入任务内容', trigger: 'blur' }],
  urgency: [{ required: true, message: '请选择紧急程度', trigger: 'change' }],
  slaDeadline: [{ required: true, message: '请选择SLA截止时间', trigger: 'change' }]
}

// ========== SLA 自动计算 ==========
const handleUrgencyChange = (val: number) => {
  const option = urgencyOptions.find(o => o.value === val)
  if (option) {
    formData.slaDeadline = dayjs().add(option.hours, 'hour').format('YYYY-MM-DD HH:mm:ss')
  }
}

// ========== 打开时初始化 ==========
watch(() => props.modelValue, (open) => {
  if (open) {
    // 重置表单
    formData.content = ''
    formData.urgency = 2
    formData.remark = ''
    // 默认 SLA：普通(48h)
    const defaultOption = urgencyOptions.find(o => o.value === 2)
    if (defaultOption) {
      formData.slaDeadline = dayjs().add(defaultOption.hours, 'hour').format('YYYY-MM-DD HH:mm:ss')
    }
    formRef.value?.clearValidate()
  }
})

// ========== 提交 ==========
const handleSubmit = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createCsTask({
      content: formData.content,
      urgency: formData.urgency,
      category: 0, // 签约
      slaDeadline: formData.slaDeadline,
      remark: formData.remark || undefined,
      // 从 session 自动填充
      sourceModule: props.session?.sourceModule,
      dealerCode: props.session?.dealerCode,
      dealerName: props.session?.dealerName,
      productLineCode: props.session?.productLineCode,
      productLineName: props.session?.productLineName
    })
    ElMessage.success('工单已下发')
    emit('update:modelValue', false)
    emit('success')
  } catch (e: any) {
    ElMessage.error(e.message || '下发失败')
  } finally {
    submitting.value = false
  }
}
</script>
