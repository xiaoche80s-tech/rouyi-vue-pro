<template>
  <el-dialog v-model="visible" title="政策详情" width="750px" destroy-on-close>
    <div v-loading="loading">
      <template v-if="detail">
        <!-- 基本信息 -->
        <el-descriptions :column="2" border class="mb-16px">
          <el-descriptions-item label="政策编码">{{ detail.policyCode }}</el-descriptions-item>
          <el-descriptions-item label="政策名称">{{ detail.policyName }}</el-descriptions-item>
          <el-descriptions-item label="政策类型">{{ policyTypeLabel(detail.policyType) }}</el-descriptions-item>
          <el-descriptions-item label="达成类型">{{ achievementTypeLabel(detail.achievementType) }}</el-descriptions-item>
          <el-descriptions-item label="政策状态">{{ statusLabel(detail.policyStatus) }}</el-descriptions-item>
          <el-descriptions-item label="经销商">{{ detail.dealerName || detail.dealerCode }}</el-descriptions-item>
          <el-descriptions-item label="产品线">{{ detail.productLineName || detail.productLineCode }}</el-descriptions-item>
          <el-descriptions-item label="来源合同">
            <template v-if="detail.contractCode">
              {{ detail.contractCode }} {{ detail.contractName ? `(${detail.contractName})` : '' }}
            </template>
            <template v-else>-</template>
          </el-descriptions-item>
          <el-descriptions-item label="政策描述" :span="2">{{ detail.policyDesc || '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 指标列表 -->
        <div class="font-bold text-14px mb-8px">指标列表</div>
        <el-table :data="detail.indicators" size="small" stripe>
          <el-table-column prop="indicatorName" label="指标名称" min-width="100" />
          <el-table-column prop="targetMonth" label="月份" width="60" align="center" />
          <el-table-column label="目标值" width="100" align="right">
            <template #default="{ row }">{{ row.targetValue?.toLocaleString() }}</template>
          </el-table-column>
          <el-table-column label="达成值" width="100" align="right">
            <template #default="{ row }">
              <el-link
                type="primary"
                :underline="false"
                @click="handleAchievementClick(row)"
              >
                {{ row.achievedValue?.toLocaleString() }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column prop="unit" label="单位" width="60" align="center" />
          <el-table-column label="达成率" width="80" align="right">
            <template #default="{ row }">
              <span :style="{ color: getRateColor(row.rate) }">{{ row.rate }}%</span>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </div>
  </el-dialog>
</template>

<script lang="ts" setup>
import type { DealerPolicyDetailVO, IndicatorVO } from '@/api/opshub/policy'
import * as PolicyApi from '@/api/opshub/policy'

const visible = ref(false)
const loading = ref(false)
const detail = ref<DealerPolicyDetailVO>()

const emit = defineEmits<{
  (e: 'achievement-click', indicator: IndicatorVO): void
}>()

const policyTypeLabel = (val: string) => {
  const map: Record<string, string> = { rebate: '返利', promotion: '促销', other: '其他' }
  return map[val] || val
}

const achievementTypeLabel = (val: string) => {
  const map: Record<string, string> = { quarter: '季度政策', month: '月度政策' }
  return map[val] || val
}

const statusLabel = (val: string) => {
  const map: Record<string, string> = { executing: '执行中', pending: '待执行', completed: '已完成' }
  return map[val] || val
}

const getRateColor = (rate: number) => {
  if (rate >= 100) return '#67C23A'
  if (rate >= 60) return '#E6A23C'
  return '#F56C6C'
}

const handleAchievementClick = (indicator: IndicatorVO) => {
  emit('achievement-click', indicator)
}

const open = async (policyId: number) => {
  visible.value = true
  loading.value = true
  try {
    detail.value = await PolicyApi.getDealerPolicy(policyId)
  } finally {
    loading.value = false
  }
}

defineExpose({ open })
</script>
