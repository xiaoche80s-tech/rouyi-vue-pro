<template>
  <div class="dashboard-container" v-loading="loading">
    <!-- 欢迎横幅 + 角色标签 -->
    <DashboardHeader
      :role="dashboard.role"
      :readonly-mode="isSales"
    />

    <!-- ════════════════════════════════════════════════ -->
    <!-- 管理员 / 超级管理员：全局业务健康度 + 客服负荷 -->
    <!-- ════════════════════════════════════════════════ -->
    <template v-if="isAdmin">
      <!-- KPI 行：4 个跨模块卡片 -->
      <el-row :gutter="16" class="mt-16px">
        <el-col :xs="12" :sm="6">
          <StatCard
            title="合同总数"
            :value="dashboard.signing?.statistics?.totalCount ?? 0"
            icon="ep:document"
            color="#7c3aed"
            bg-color="#f5f3ff"
            :suffix="`签署率 ${dashboard.signing?.statistics?.signedRate ?? 0}%`"
          />
        </el-col>
        <el-col :xs="12" :sm="6">
          <StatCard
            title="订单总量"
            :value="dashboard.order?.statistics?.totalCount ?? 0"
            icon="ep:shopping-cart"
            color="#10b981"
            bg-color="#ecfdf5"
            :suffix="`总金额 ¥${formatAmount(dashboard.order?.statistics?.totalAmount)}`"
          />
        </el-col>
        <el-col :xs="12" :sm="6">
          <StatCard
            title="售后完成率"
            :value="dashboard.aftersale?.statistics?.completedRate ?? 0"
            icon="ep:circle-check"
            color="#f59e0b"
            bg-color="#fffbeb"
            suffix="%"
            :subtitle="`退款 ${dashboard.aftersale?.statistics?.refundCount ?? 0} 单`"
          />
        </el-col>
        <el-col :xs="12" :sm="6">
          <StatCard
            title="待处理工单"
            :value="dashboard.csTask?.tabCounts?.pending ?? 0"
            icon="ep:ticket"
            color="#ef4444"
            bg-color="#fef2f2"
          />
        </el-col>
      </el-row>

      <!-- 图表行1：签约趋势 + 签约类型分布 -->
      <el-row :gutter="16" class="mt-16px">
        <el-col :xs="24" :lg="14" v-if="dashboard.signing?.trend">
          <SigningTrendChart :data="dashboard.signing.trend" />
        </el-col>
        <el-col :xs="24" :lg="10" v-if="dashboard.signing?.statistics">
          <SigningPieChart :statistics="dashboard.signing.statistics" />
        </el-col>
      </el-row>

      <!-- 图表行2：订单状态 + 售后进度 -->
      <el-row :gutter="16" class="mt-16px">
        <el-col :xs="24" :lg="12" v-if="dashboard.order?.statistics">
          <OrderBarChart :statistics="dashboard.order.statistics" />
        </el-col>
        <el-col :xs="24" :lg="12" v-if="dashboard.aftersale?.statistics">
          <AftersaleDonutChart :statistics="dashboard.aftersale.statistics" />
        </el-col>
      </el-row>

      <!-- 客服工作负荷：3 个简化 KPI 卡片 -->
      <el-row :gutter="16" class="mt-16px">
        <el-col :xs="24" :sm="8">
          <StatCard
            title="工单待办"
            :value="dashboard.csTask?.tabCounts?.pending ?? 0"
            icon="ep:ticket"
            color="#ef4444"
            bg-color="#fef2f2"
          />
        </el-col>
        <el-col :xs="24" :sm="8">
          <StatCard
            title="咨询待处理"
            :value="dashboard.csConsult?.statistics?.pendingCount ?? 0"
            icon="ep:chat-dot-round"
            color="#f59e0b"
            bg-color="#fffbeb"
          />
        </el-col>
        <el-col :xs="24" :sm="8">
          <StatCard
            title="操作请求待处理"
            :value="dashboard.csOpReq?.statistics?.pendingCount ?? 0"
            icon="ep:document"
            color="#3b82f6"
            bg-color="#eff6ff"
          />
        </el-col>
      </el-row>
    </template>

    <!-- ════════════════════════════════════════════════ -->
    <!-- 品牌销售：只读观察型 -->
    <!-- ════════════════════════════════════════════════ -->
    <template v-else-if="isSales">
      <!-- KPI 行 -->
      <el-row :gutter="16" class="mt-16px">
        <el-col :xs="12" :sm="6">
          <StatCard
            title="合同总数"
            :value="dashboard.signing?.statistics?.totalCount ?? 0"
            icon="ep:document"
            color="#7c3aed"
            bg-color="#f5f3ff"
            :suffix="`签署率 ${dashboard.signing?.statistics?.signedRate ?? 0}%`"
          />
        </el-col>
        <el-col :xs="12" :sm="6">
          <StatCard
            title="订单总量"
            :value="dashboard.order?.statistics?.totalCount ?? 0"
            icon="ep:shopping-cart"
            color="#10b981"
            bg-color="#ecfdf5"
            :suffix="`总金额 ¥${formatAmount(dashboard.order?.statistics?.totalAmount)}`"
          />
        </el-col>
        <el-col :xs="12" :sm="6">
          <StatCard
            title="售后完成率"
            :value="dashboard.aftersale?.statistics?.completedRate ?? 0"
            icon="ep:circle-check"
            color="#f59e0b"
            bg-color="#fffbeb"
            suffix="%"
            :subtitle="`退款 ${dashboard.aftersale?.statistics?.refundCount ?? 0} 单`"
          />
        </el-col>
        <el-col :xs="12" :sm="6">
          <StatCard
            title="基础数据文件"
            :value="0"
            icon="ep:folder"
            color="#6b7280"
            bg-color="#f3f4f6"
            subtitle="暂无统计"
          />
        </el-col>
      </el-row>

      <!-- 图表行1 -->
      <el-row :gutter="16" class="mt-16px">
        <el-col :xs="24" :lg="14" v-if="dashboard.signing?.trend">
          <SigningTrendChart :data="dashboard.signing.trend" />
        </el-col>
        <el-col :xs="24" :lg="10" v-if="dashboard.signing?.statistics">
          <SigningPieChart :statistics="dashboard.signing.statistics" />
        </el-col>
      </el-row>

      <!-- 图表行2 -->
      <el-row :gutter="16" class="mt-16px">
        <el-col :xs="24" :lg="12" v-if="dashboard.order?.statistics">
          <OrderBarChart :statistics="dashboard.order.statistics" />
        </el-col>
        <el-col :xs="24" :lg="12" v-if="dashboard.aftersale?.statistics">
          <AftersaleDonutChart :statistics="dashboard.aftersale.statistics" />
        </el-col>
      </el-row>
    </template>

    <!-- ════════════════════════════════════════════════ -->
    <!-- 服务执行员：工作台型 -->
    <!-- ════════════════════════════════════════════════ -->
    <template v-else-if="isExecutor">
      <!-- 我的待办 -->
      <div class="mt-16px">
        <MyTodoPanel
          :pending-tasks="dashboard.csTask?.tabCounts?.pending ?? 0"
          :pending-consults="dashboard.csConsult?.statistics?.pendingCount ?? 0"
          :pending-op-reqs="dashboard.csOpReq?.statistics?.pendingCount ?? 0"
        />
      </div>

      <!-- 概览 KPI -->
      <el-row :gutter="16" class="mt-16px">
        <el-col :xs="12" :sm="6">
          <StatCard
            title="合同总数"
            :value="dashboard.signing?.statistics?.totalCount ?? 0"
            icon="ep:document"
            color="#7c3aed"
            bg-color="#f5f3ff"
            :suffix="`签署率 ${dashboard.signing?.statistics?.signedRate ?? 0}%`"
          />
        </el-col>
        <el-col :xs="12" :sm="6">
          <StatCard
            title="售后总数"
            :value="dashboard.aftersale?.statistics?.totalCount ?? 0"
            icon="ep:refresh-left"
            color="#10b981"
            bg-color="#ecfdf5"
            :suffix="`完成率 ${dashboard.aftersale?.statistics?.completedRate ?? 0}%`"
          />
        </el-col>
        <el-col :xs="12" :sm="6">
          <StatCard
            title="工单总数"
            :value="taskTotal"
            icon="ep:ticket"
            color="#3b82f6"
            bg-color="#eff6ff"
          />
        </el-col>
        <el-col :xs="12" :sm="6">
          <StatCard
            title="咨询总数"
            :value="dashboard.csConsult?.statistics?.totalCount ?? 0"
            icon="ep:chat-dot-round"
            color="#6b7280"
            bg-color="#f3f4f6"
          />
        </el-col>
      </el-row>

      <!-- 图表：工单状态分布 + 咨询队列状态 -->
      <el-row :gutter="16" class="mt-16px">
        <el-col :xs="24" :lg="12" v-if="dashboard.csTask?.tabCounts">
          <TaskStatusPieChart :tab-counts="dashboard.csTask.tabCounts" />
        </el-col>
        <el-col :xs="24" :lg="12" v-if="dashboard.csConsult?.statistics">
          <ConsultQueueBarChart :statistics="dashboard.csConsult.statistics" />
        </el-col>
      </el-row>
    </template>

    <!-- ════════════════════════════════════════════════ -->
    <!-- 经销商：自助型 -->
    <!-- ════════════════════════════════════════════════ -->
    <template v-else-if="isDealer">
      <!-- 混合 KPI -->
      <el-row :gutter="16" class="mt-16px">
        <el-col :xs="12" :sm="6">
          <StatCard
            title="我的合同"
            :value="dashboard.signing?.statistics?.totalCount ?? 0"
            icon="ep:document"
            color="#7c3aed"
            bg-color="#f5f3ff"
            :suffix="`签署中 ${dashboard.signing?.statistics?.signingCount ?? 0} 份`"
            route="/opshub/signing"
          />
        </el-col>
        <el-col :xs="12" :sm="6">
          <StatCard
            title="待付款订单"
            :value="dashboard.order?.statistics?.unpaidCount ?? 0"
            icon="ep:money"
            color="#ef4444"
            bg-color="#fef2f2"
            :suffix="`¥${formatAmount(dashboard.order?.statistics?.totalAmount)}`"
            route="/opshub/order"
          />
        </el-col>
        <el-col :xs="12" :sm="6">
          <StatCard
            title="进行中售后"
            :value="dashboard.aftersale?.statistics?.inProgressCount ?? 0"
            icon="ep:refresh-left"
            color="#f59e0b"
            bg-color="#fffbeb"
            :suffix="`完成率 ${dashboard.aftersale?.statistics?.completedRate ?? 0}%`"
            route="/opshub/aftersale"
          />
        </el-col>
        <el-col :xs="12" :sm="6">
          <StatCard
            title="我的工单"
            :value="taskTotal"
            icon="ep:ticket"
            color="#10b981"
            bg-color="#ecfdf5"
            :suffix="`待验收 ${dashboard.csOpReq?.statistics?.pendingVerifyCount ?? 0} 单`"
            route="/opshub/workorder-service"
          />
        </el-col>
      </el-row>

      <!-- 签约状态 + 订单状态 -->
      <el-row :gutter="16" class="mt-16px">
        <el-col :xs="24" :sm="12" v-if="dashboard.signing?.statistics">
          <StatusListCard
            title="签约状态"
            :items="[
              { label: '已签署', value: dashboard.signing.statistics.signedCount, highlight: true, color: '#10b981' },
              { label: '签署中', value: dashboard.signing.statistics.signingCount, color: '#3b82f6' },
              { label: '待签署', value: dashboard.signing.statistics.unsignedCount, color: '#f59e0b' }
            ]"
          />
        </el-col>
        <el-col :xs="24" :sm="12" v-if="dashboard.order?.statistics">
          <StatusListCard
            title="订单状态"
            :items="[
              { label: '未付款', value: dashboard.order.statistics.unpaidCount, highlight: true, color: '#ef4444' },
              { label: '未开票', value: dashboard.order.statistics.uninvoicedCount, color: '#f59e0b' },
              { label: '已完成', value: dashboard.order.statistics.completedCount, color: '#10b981' }
            ]"
          />
        </el-col>
      </el-row>

      <!-- 图表：签约趋势 + 售后进度 -->
      <el-row :gutter="16" class="mt-16px">
        <el-col :xs="24" :lg="14" v-if="dashboard.signing?.trend">
          <SigningTrendChart :data="dashboard.signing.trend" />
        </el-col>
        <el-col :xs="24" :lg="10" v-if="dashboard.aftersale?.statistics">
          <AftersaleDonutChart :statistics="dashboard.aftersale.statistics" />
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script lang="ts" setup>
import * as DashboardApi from '@/api/opshub/dashboard'
import DashboardHeader from './components/DashboardHeader.vue'
import StatCard from './components/StatCard.vue'
import SigningTrendChart from './components/SigningTrendChart.vue'
import SigningPieChart from './components/SigningPieChart.vue'
import OrderBarChart from './components/OrderBarChart.vue'
import AftersaleDonutChart from './components/AftersaleDonutChart.vue'
import TaskStatusPieChart from './components/TaskStatusPieChart.vue'
import ConsultQueueBarChart from './components/ConsultQueueBarChart.vue'
import MyTodoPanel from './components/MyTodoPanel.vue'
import StatusListCard from './components/StatusListCard.vue'

defineOptions({ name: 'Index' })

const loading = ref(true)
const dashboard = ref<DashboardApi.DashboardVO>({
  role: '',
  signing: undefined,
  order: undefined,
  aftersale: undefined,
  csTask: undefined,
  csConsult: undefined,
  csOpReq: undefined,
  quickActions: []
})

// 角色判断 computed
const role = computed(() => dashboard.value.role)
const isAdmin = computed(() => role.value === 'super_admin' || role.value === 'brand_admin')
const isSales = computed(() => role.value === 'brand_sales')
const isExecutor = computed(() => role.value === 'service_executor')
const isDealer = computed(() => role.value === 'dealer')

// 工单总数（从 tabCounts 求和）
const taskTotal = computed(() => {
  const counts = dashboard.value.csTask?.tabCounts
  if (!counts) return 0
  return Object.entries(counts)
    .filter(([key]) => key !== 'all')
    .reduce((sum, [, val]) => sum + val, 0)
})

// 格式化金额（万为单位）
const formatAmount = (amount?: number) => {
  if (!amount) return '0'
  if (amount >= 10000) return `${(amount / 10000).toFixed(2)}万`
  return amount.toLocaleString()
}

const loadDashboard = async () => {
  loading.value = true
  try {
    dashboard.value = await DashboardApi.getDashboard()
  } catch (e) {
    console.error('加载仪表盘数据失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(loadDashboard)
</script>

<style scoped>
.dashboard-container {
  padding: 16px;
}
</style>
