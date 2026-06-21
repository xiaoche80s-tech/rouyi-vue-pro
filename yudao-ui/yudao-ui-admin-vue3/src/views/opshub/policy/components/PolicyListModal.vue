<template>
  <el-dialog v-model="visible" :title="modalTitle" width="750px" destroy-on-close>
    <div v-loading="loading">
      <!-- 样式A: 点击标题政策数量 -->
      <div v-if="variant === 'A'" class="policy-card-grid">
        <div
          v-for="policy in policiesA"
          :key="policy.id"
          class="policy-card-a"
          :class="{ 'is-completed': policy.policyStatus === 'completed' }"
          @click="handleCardClick(policy)"
        >
          <!-- Header: 编码 + 类型 + 状态 -->
          <div class="card-a-header">
            <span class="policy-code">{{ policy.policyCode }}</span>
            <el-tag size="small" type="info">{{ policyTypeLabel(policy.policyType) }}</el-tag>
            <el-tag
              size="small"
              :type="statusTone(policy.policyStatus)"
              class="ml-auto"
            >
              {{ statusLabel(policy.policyStatus) }}
            </el-tag>
          </div>

          <!-- 合作方 / 产品线 -->
          <div class="card-a-info">
            <div>合作方：{{ policy.dealerName }}</div>
            <div>产品线：{{ policy.productLineName || '-' }}</div>
          </div>

          <!-- 指标达成明细 -->
          <div class="card-a-indicators">
            <div class="indicators-title">📌 政策目标与达成</div>
            <div v-for="(ind, i) in policy.indicators" :key="i" class="indicator-row">
              <span class="ind-name">{{ ind.indicatorName }}：</span>
              <span class="ind-target">目标 {{ ind.targetValue }}{{ ind.unit }}</span>
              <span class="ind-arrow">→</span>
              <span :class="['ind-achieved', ind.rate >= 100 ? 'text-success' : 'text-muted']">
                达成 {{ ind.achievedValue }}{{ ind.unit }}
              </span>
              <span :class="['ind-rate', ind.rate >= 100 ? 'text-success' : 'text-danger']">
                ({{ ind.rate }}%)
              </span>
            </div>
          </div>

          <!-- 激励说明 -->
          <div v-if="policy.remark" class="card-a-remark">
            📝 {{ policy.remark }}
          </div>
        </div>
      </div>

      <!-- 样式B: 点击目标分布药丸 -->
      <div v-else class="policy-card-grid">
        <div
          v-for="policy in policiesB"
          :key="policy.id"
          class="policy-card-b"
          @click="handleCardClickB(policy)"
        >
          <!-- Header: 编码 + 名称 + 类型 -->
          <div class="card-b-header">
            <span class="policy-code">{{ policy.policyCode }}</span>
            <span class="policy-name">{{ policy.policyName }}</span>
            <el-tag size="small" type="info">{{ policyTypeLabel(policy.policyType) }}</el-tag>
          </div>

          <!-- 目标达成行 -->
          <div class="card-b-target-row">
            <div class="target-badge target-icon-red">◎</div>
            <span class="target-text">
              目标：<strong>{{ policy.targetValue }}</strong>{{ pillUnit }}
            </span>

            <div
              :class="[
                'target-badge',
                isAchieved(policy) ? 'target-icon-green' : 'target-icon-red'
              ]"
            >
              {{ isAchieved(policy) ? '✓' : '◎' }}
            </div>
            <span class="target-text">
              达成：<strong :class="isAchieved(policy) ? 'text-success' : 'text-danger'">
                {{ policy.achievedValue }}
              </strong>{{ pillUnit }}
            </span>

            <span class="date-range">{{ policy.startDate }}~{{ policy.endDate }}</span>

            <span class="status-badge" :class="policy.policyStatus === 'completed' ? 'status-completed' : 'status-executing'">
              {{ policy.policyStatus === 'completed' ? '✕' : '→' }}
              {{ statusLabel(policy.policyStatus) }}
            </span>
          </div>

          <!-- 经销商 -->
          <div class="card-b-dealer">{{ policy.dealerName }}</div>

          <!-- 提示 -->
          <div class="card-b-hint">点击卡片可查看完整政策详情</div>
        </div>
      </div>

      <el-empty v-if="!loading && currentPolicies.length === 0" description="暂无政策数据" />
    </div>
  </el-dialog>
</template>

<script lang="ts" setup>
import type {
  DealerPolicyVO,
  DealerPolicyDetailVO,
  TargetGroupVO,
  PolicyBriefVO
} from '@/api/opshub/policy'
import * as PolicyApi from '@/api/opshub/policy'

const visible = ref(false)
const loading = ref(false)
const modalTitle = ref('')
const variant = ref<'A' | 'B'>('A')

// Style A data
const policiesA = ref<(DealerPolicyDetailVO)[]>([])
// Style B data
const policiesB = ref<PolicyBriefVO[]>([])
const pillUnit = ref('')

// Emit for card click -> open detail modal
const emit = defineEmits<{
  (e: 'card-click', policyId: number): void
}>()

const currentPolicies = computed(() => {
  return variant.value === 'A' ? policiesA.value : policiesB.value
})

const policyTypeLabel = (val: string) => {
  const map: Record<string, string> = { rebate: '返利', promotion: '促销', other: '其他' }
  return map[val] || val
}

const statusLabel = (val: string) => {
  const map: Record<string, string> = { executing: '执行中', pending: '待执行', completed: '已完成' }
  return map[val] || val
}

const statusTone = (val: string): 'primary' | 'success' | 'warning' | 'danger' | 'info' => {
  const map: Record<string, 'primary' | 'success' | 'warning' | 'danger' | 'info'> = {
    executing: 'primary',
    pending: 'warning',
    completed: 'success'
  }
  return map[val] || 'info'
}

const isAchieved = (policy: PolicyBriefVO) => {
  // For style B, check if achievedValue >= targetValue
  return policy.achievedValue >= policy.targetValue
}

const handleCardClick = (policy: DealerPolicyDetailVO) => {
  emit('card-click', policy.id)
}

const handleCardClickB = (policy: PolicyBriefVO) => {
  emit('card-click', policy.id)
}

/**
 * Style A: Open by indicator - load all policies for this indicator
 */
const openByIndicator = async (indicatorName: string, targetYear: number) => {
  variant.value = 'A'
  modalTitle.value = `${indicatorName} - 全部政策`
  visible.value = true
  loading.value = true
  policiesA.value = []
  policiesB.value = []

  try {
    const policies = await PolicyApi.getPoliciesByIndicator(indicatorName, targetYear)
    // Load full details for each policy
    const details = await Promise.all(
      policies.map((p: DealerPolicyVO) => PolicyApi.getDealerPolicy(p.id))
    )
    policiesA.value = details
  } finally {
    loading.value = false
  }
}

/**
 * Style B: Open by target group - use policies from the group directly
 */
const openByTargetGroup = (group: TargetGroupVO) => {
  variant.value = 'B'
  modalTitle.value = `目标值 ${group.targetValue}${group.unit || ''} - 关联政策 (${group.policyCount}条)`
  visible.value = true
  loading.value = false
  policiesA.value = []
  policiesB.value = group.policies
  pillUnit.value = group.unit || ''
}

defineExpose({ openByIndicator, openByTargetGroup })
</script>

<style scoped>
.policy-card-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  max-height: 60vh;
  overflow-y: auto;
  padding: 4px;
}

/* ========== Style A Card ========== */
.policy-card-a {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
  min-height: 160px;
}

.policy-card-a:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

.policy-card-a.is-completed {
  border: 2px solid #409eff;
}

.card-a-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.card-a-info {
  font-size: 12px;
  color: #606266;
  margin-bottom: 10px;
  line-height: 1.6;
}

.card-a-indicators {
  margin-bottom: 10px;
}

.indicators-title {
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  margin-bottom: 4px;
}

.indicator-row {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 4px;
  flex-wrap: wrap;
}

.ind-name {
  color: #606266;
}

.ind-target {
  color: #909399;
}

.ind-arrow {
  color: #909399;
}

.ind-achieved {
  font-weight: 600;
}

.ind-rate {
  font-weight: 600;
}

.text-success {
  color: #67c23a;
}

.text-danger {
  color: #f56c6c;
}

.text-muted {
  color: #909399;
}

.card-a-remark {
  font-size: 12px;
  color: #909399;
  border-top: 1px solid #f0f0f0;
  padding-top: 8px;
}

/* ========== Style B Card ========== */
.policy-card-b {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
  min-height: 130px;
}

.policy-card-b:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

.card-b-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.card-b-header .policy-name {
  font-size: 14px;
  font-weight: 700;
  color: #303133;
  flex: 1;
}

.card-b-target-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.target-badge {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  flex-shrink: 0;
}

.target-icon-red {
  background: #f56c6c;
}

.target-icon-green {
  background: #67c23a;
}

.target-text {
  font-size: 13px;
  color: #606266;
}

.date-range {
  font-size: 12px;
  color: #909399;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  padding: 2px 8px;
  border-radius: 4px;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  font-size: 11px;
}

.status-executing {
  color: #409eff;
}

.status-completed {
  color: #909399;
}

.card-b-dealer {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.card-b-hint {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 8px;
  text-align: right;
}

.policy-code {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}
</style>
