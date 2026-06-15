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
  </el-dialog>
</template>

<script lang="ts" setup>
import type { SigningContractVO } from '@/api/opshub/signing'

const visible = ref(false)
const contract = ref<SigningContractVO | null>(null)

const open = (row: SigningContractVO) => {
  contract.value = row
  visible.value = true
}

defineExpose({ open })
</script>
