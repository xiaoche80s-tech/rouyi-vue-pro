<template>
  <el-dialog v-model="visible" :title="isCreate ? '创建合同' : '编辑合同'" width="680px" destroy-on-close>
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <!-- 经销商 -->
      <el-form-item label="经销商" prop="dealerId">
        <el-select
          v-model="formData.dealerId"
          class="w-full"
          filterable
          placeholder="请选择经销商"
          @change="handleDealerChange"
        >
          <el-option v-for="d in dealerList" :key="d.id" :label="d.dealerName" :value="d.id" />
        </el-select>
      </el-form-item>

      <!-- 产品线 -->
      <el-form-item label="产品线">
        <el-select v-model="formData.productLineCode" class="w-full" clearable placeholder="请选择产品线">
          <el-option v-for="pl in productLineList" :key="pl.productLineCode" :label="pl.productLineName" :value="pl.productLineCode" />
        </el-select>
      </el-form-item>

      <!-- 合同类型 -->
      <el-form-item label="合同类型" prop="contractType">
        <el-select v-model="formData.contractType" class="w-full" placeholder="请选择合同类型" @change="handleTypeChange">
          <el-option label="主合同" value="main" />
          <el-option label="政策合同" value="policy" />
          <el-option label="补充协议" value="supplement" />
          <el-option label="终止协议" value="termination" />
        </el-select>
      </el-form-item>

      <!-- 合同编码（编辑模式只读） -->
      <el-form-item v-if="!isCreate" label="合同编码">
        <el-input :model-value="formData.contractCode" disabled />
      </el-form-item>

      <!-- 合同名称 -->
      <el-form-item label="合同名称" prop="contractName">
        <el-input v-model="formData.contractName" placeholder="请输入合同名称" />
      </el-form-item>

      <!-- 下发日期 -->
      <el-form-item label="下发日期" prop="issuedDate">
        <el-date-picker v-model="formData.issuedDate" type="date" value-format="YYYY-MM-DD" class="w-full" placeholder="选择下发日期" />
      </el-form-item>

      <!-- 签署日期 -->
      <el-form-item label="签署日期">
        <el-date-picker v-model="formData.signDate" type="date" value-format="YYYY-MM-DD" class="w-full" placeholder="选择签署日期" />
      </el-form-item>

      <!-- 合同摘要 -->
      <el-form-item label="合同摘要">
        <el-input v-model="formData.summary" type="textarea" :rows="3" placeholder="请输入合同摘要" />
      </el-form-item>

      <!-- 政策解析（仅 policy） -->
      <el-form-item v-if="formData.contractType === 'policy'" label="政策解析">
        <el-input v-model="formData.policyAnalysis" type="textarea" :rows="3" placeholder="请输入政策解析" />
      </el-form-item>

      <!-- 政策指标（仅 policy） -->
      <el-form-item v-if="formData.contractType === 'policy'" label="政策指标">
        <el-input v-model="formData.indicators" type="textarea" :rows="4" placeholder='JSON格式，如 [{"indicatorName":"xxx","targetValue":100}]' />
      </el-form-item>

      <!-- 备注 -->
      <el-form-item label="备注">
        <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="备注" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import * as SigningApi from '@/api/opshub/signing'
import * as DealerApi from '@/api/opshub/dealer'

const message = useMessage()
const emit = defineEmits<{ (e: 'success'): void }>()

const visible = ref(false)
const submitting = ref(false)
const isCreate = ref(true)
const formRef = ref()

const dealerList = ref<DealerApi.DealerSimpleVO[]>([])
const productLineList = ref<any[]>([])

const contractTypeNames: Record<string, string> = {
  main: '主合同',
  policy: '政策合同',
  supplement: '补充协议',
  termination: '终止协议'
}

const formData = reactive({
  id: undefined as number | undefined,
  dealerId: undefined as number | undefined,
  dealerCode: '',
  productLineCode: '',
  contractType: '',
  contractTypeName: '',
  contractCode: '',
  contractName: '',
  issuedDate: '',
  signDate: '',
  summary: '',
  policyAnalysis: '',
  indicators: '',
  remark: ''
})

const rules = {
  dealerId: [{ required: true, message: '请选择经销商', trigger: 'change' }],
  contractType: [{ required: true, message: '请选择合同类型', trigger: 'change' }],
  contractName: [{ required: true, message: '请输入合同名称', trigger: 'blur' }],
  issuedDate: [{ required: true, message: '请选择下发日期', trigger: 'change' }]
}

const open = async (mode: 'create' | 'update', row?: any) => {
  isCreate.value = mode === 'create'
  // 加载经销商列表
  try {
    dealerList.value = await DealerApi.getSimpleDealerList()
  } catch (e) {
    console.error('加载经销商列表失败', e)
  }

  if (mode === 'update' && row) {
    Object.assign(formData, {
      id: row.id,
      dealerId: row.dealerId,
      dealerCode: row.dealerCode,
      productLineCode: row.productLineCode || '',
      contractType: row.contractType,
      contractTypeName: row.contractTypeName,
      contractCode: row.contractCode,
      contractName: row.contractName,
      issuedDate: row.issuedDate || '',
      signDate: row.signDate || '',
      summary: row.summary || '',
      policyAnalysis: row.policyAnalysis || '',
      indicators: row.indicators || '',
      remark: row.remark || ''
    })
  } else {
    Object.assign(formData, {
      id: undefined, dealerId: undefined, dealerCode: '', productLineCode: '',
      contractType: '', contractTypeName: '', contractCode: '', contractName: '',
      issuedDate: '', signDate: '', summary: '', policyAnalysis: '', indicators: '', remark: ''
    })
  }
  visible.value = true
}

const handleDealerChange = (dealerId: number) => {
  const dealer = dealerList.value.find(d => d.id === dealerId)
  formData.dealerCode = dealer?.dealerCode || ''
}

const handleTypeChange = (type: string) => {
  formData.contractTypeName = contractTypeNames[type] || ''
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (isCreate.value) {
      await SigningApi.createSigningContract(formData)
      message.success('创建成功')
    } else {
      await SigningApi.updateSigningContract(formData)
      message.success('更新成功')
    }
    visible.value = false
    emit('success')
  } catch (e) {
    console.error('提交失败', e)
  } finally {
    submitting.value = false
  }
}

defineExpose({ open })
</script>
