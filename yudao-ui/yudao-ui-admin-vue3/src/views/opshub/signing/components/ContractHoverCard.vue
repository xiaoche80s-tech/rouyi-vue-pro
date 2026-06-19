<template>
  <div v-if="contract" class="p-10px">
    <div class="text-14px font-bold mb-8px">{{ contract.contractName }}</div>
    <div class="text-12px text-gray-500 mb-4px">编码：{{ contract.contractCode }}</div>
    <div class="text-12px text-gray-500 mb-4px">类型：{{ contract.contractTypeName }}</div>
    <div class="text-12px text-gray-500 mb-4px">下发日期：{{ contract.issuedDate }}</div>
    <div v-if="contract.summary" class="text-12px text-gray-600 mt-8px">
      <div class="font-bold mb-4px">摘要：</div>
      <div>{{ contract.summary }}</div>
    </div>
    <div v-if="contract.contractType === 'policy' && contract.policyAnalysis" class="text-12px text-gray-600 mt-8px">
      <div class="font-bold mb-4px">政策解析：</div>
      <div>{{ contract.policyAnalysis }}</div>
    </div>
    <div v-if="contract.contractType === 'policy' && contract.indicators" class="text-12px text-gray-600 mt-8px">
      <div class="font-bold mb-4px">政策指标：</div>
      <pre class="text-11px bg-gray-50 p-4px rounded">{{ formatIndicators(contract.indicators) }}</pre>
    </div>
    <div v-if="contract.attachmentCount" class="text-12px text-gray-500 mt-8px flex items-center gap-4px">
      <Icon icon="ep:paperclip" />
      <span>{{ contract.attachmentCount }} 个附件</span>
    </div>
  </div>
</template>

<script lang="ts" setup>
import type { SigningContractVO } from '@/api/opshub/signing'

defineProps<{ contract: SigningContractVO }>()

const formatIndicators = (indicators: string) => {
  try {
    return JSON.stringify(JSON.parse(indicators), null, 2)
  } catch {
    return indicators
  }
}
</script>
