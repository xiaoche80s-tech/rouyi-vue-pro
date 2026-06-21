<template>
  <div class="kpi-section mb-24px">
    <!-- 标题区 -->
    <div class="flex items-center justify-between mb-14px">
      <div class="flex items-center gap-10px">
        <div class="flex items-center gap-6px">
          <span class="font-bold text-18px">{{ panel.indicatorName }}</span>
          <el-tag size="small" type="info" effect="plain">{{ panel.unit }}</el-tag>
        </div>
        <span class="text-gray-400 text-13px">({{ panel.policyCount }}条政策 · {{ dimensionText }})</span>
      </div>
    </div>

    <!-- 季度/月份卡片行 -->
    <div class="period-grid" :class="{ 'is-month': isMonthDimension, 'is-quarter': isQuarterDimension }">
      <div
        v-for="period in panel.periods"
        :key="period.label"
        class="period-card"
        :class="{ 'is-over': period.rate >= 100, 'is-under': period.rate < 100 }"
        @click="handleCardClick(period)"
      >
        <!-- 达成值 -->
        <div class="period-value">{{ formatNumber(period.achieved) }}</div>
        <div class="period-unit-label">{{ panel.unit }}</div>

        <!-- 目标值 -->
        <div class="period-target">满点 {{ formatNumber(period.target) }}{{ panel.unit }}</div>

        <!-- 迷你柱状图 -->
        <div class="mini-bar-wrap">
          <div class="mini-bar-bg">
            <div
              class="mini-bar-fill"
              :style="{ height: getBarHeight(period) + '%' }"
            ></div>
          </div>
        </div>

        <!-- 季度标签 -->
        <div class="period-label">{{ period.label }}</div>

        <!-- 达成率 -->
        <div class="period-rate" :class="period.rate >= 100 ? 'text-green-500' : 'text-red-500'">
          达成率 {{ period.rate }}%
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import type { KpiPanelVO, PeriodVO } from '@/api/opshub/policy'

const props = defineProps<{
  panel: KpiPanelVO
}>()

const emit = defineEmits<{
  (e: 'policy-count-click', panel: KpiPanelVO): void
  (e: 'bar-click', data: { indicatorName: string; label: string }): void
}>()

const formatNumber = (val: number) => {
  if (val == null) return '-'
  return val.toLocaleString()
}

const isQuarterDimension = computed(() => props.panel.periods.some(p => p.label.startsWith('Q')))
const isMonthDimension = computed(() => props.panel.periods.some(p => p.label.endsWith('月')))

const dimensionText = computed(() => {
  if (isQuarterDimension.value) return '全年4季度'
  if (isMonthDimension.value) return '全年12个月'
  return '全年数据'
})

const getBarHeight = (period: PeriodVO) => {
  if (!period.target || period.target === 0) return 0
  const pct = (period.achieved / period.target) * 100
  return Math.min(Math.max(pct, 0), 100)
}

const handleCardClick = (period: PeriodVO) => {
  emit('bar-click', {
    indicatorName: props.panel.indicatorName,
    label: period.label
  })
}
</script>

<style scoped>
.kpi-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.period-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.period-grid.is-month {
  grid-template-columns: repeat(12, 1fr);
}

@media (max-width: 1200px) {
  .period-grid.is-month {
    grid-template-columns: repeat(6, 1fr);
  }
}

@media (max-width: 768px) {
  .period-grid.is-month {
    grid-template-columns: repeat(4, 1fr);
  }
}

.period-card {
  background: #fafafa;
  border-radius: 8px;
  padding: 16px 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #f0f0f0;
}

.period-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-color: #d9d9d9;
}

.period-card.is-over {
  border-left: 3px solid #67c23a;
}

.period-card.is-under {
  border-left: 3px solid #f56c6c;
}

.period-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.period-unit-label {
  font-size: 13px;
  color: #409eff;
  font-weight: 500;
  margin-top: 2px;
}

.period-target {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.mini-bar-wrap {
  display: flex;
  justify-content: center;
  margin: 10px 0 6px;
}

.mini-bar-bg {
  width: 24px;
  height: 50px;
  background: #f0f0f0;
  border-radius: 12px;
  position: relative;
  overflow: hidden;
}

.mini-bar-fill {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  border-radius: 12px;
  transition: height 0.6s ease;
}

.period-card.is-over .mini-bar-fill {
  background: linear-gradient(to top, #67c23a, #95de64);
}

.period-card.is-under .mini-bar-fill {
  background: linear-gradient(to top, #f56c6c, #f89898);
}

.period-label {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  margin-top: 4px;
}

.period-rate {
  font-size: 13px;
  font-weight: 500;
  margin-top: 2px;
}
</style>
