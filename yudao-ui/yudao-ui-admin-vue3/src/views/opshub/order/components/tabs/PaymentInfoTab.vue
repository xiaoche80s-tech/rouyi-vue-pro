<template>
  <div v-if="detail">
    <!-- 付款概要 -->
    <el-descriptions :column="3" border class="mb-16px">
      <el-descriptions-item label="订单金额">¥{{ formatAmount(detail.totalAmount) }}</el-descriptions-item>
      <el-descriptions-item label="已付金额">
        <span :class="detail.paidAmount >= detail.totalAmount ? 'text-green-600' : 'text-orange-500'">
          ¥{{ formatAmount(detail.paidAmount) }}
        </span>
      </el-descriptions-item>
      <el-descriptions-item label="未付金额">
        <span class="text-red-500">¥{{ formatAmount(Math.max(0, detail.totalAmount - detail.paidAmount)) }}</span>
      </el-descriptions-item>
    </el-descriptions>

    <!-- 申请付款按钮 -->
    <div class="mb-12px">
      <el-button
        v-hasPermi="['dealer:order:pay']"
        type="primary"
        :disabled="detail.payStatus === 'paid'"
        @click="emit('applyPayment')"
      >
        <Icon class="mr-5px" icon="ep:money" />申请付款
      </el-button>
    </div>

    <!-- 付款记录表 -->
    <el-table :data="detail.payments" border size="small">
      <el-table-column align="right" label="付款金额" width="120">
        <template #default="{ row }">¥{{ formatAmount(row.payAmount) }}</template>
      </el-table-column>
      <el-table-column align="center" label="付款日期" width="110">
        <template #default="{ row }">{{ row.payDate ? formatDate(row.payDate, 'YYYY-MM-DD') : '-' }}</template>
      </el-table-column>
      <el-table-column align="center" label="付款方式" prop="payMethod" width="100" />
      <el-table-column align="center" label="凭证号" prop="voucherNo" width="140" show-overflow-tooltip />
      <el-table-column align="center" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="paymentStatusType(row.status)" size="small">{{ paymentStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="申请时间" width="160">
        <template #default="{ row }">{{ formatDate(row.applyTime, 'YYYY-MM-DD HH:mm') }}</template>
      </el-table-column>
      <el-table-column align="center" label="审批时间" width="160">
        <template #default="{ row }">{{ row.approveTime ? formatDate(row.approveTime, 'YYYY-MM-DD HH:mm') : '-' }}</template>
      </el-table-column>
      <el-table-column align="center" label="备注" prop="remark" min-width="120" show-overflow-tooltip />
    </el-table>
  </div>
</template>

<script lang="ts" setup>
import type { OrderDetailVO } from '@/api/opshub/order'
import { formatDate } from '@/utils/formatTime'

defineProps<{ detail: OrderDetailVO | null }>()
const emit = defineEmits<{ (e: 'applyPayment'): void }>()

const formatAmount = (val: number | undefined) => {
  if (val == null) return '0.00'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const paymentStatusType = (s: string) => ({ pending: 'warning', approved: 'success', rejected: 'danger' }[s] || 'info')
const paymentStatusLabel = (s: string) => ({ pending: '审批中', approved: '已通过', rejected: '已拒绝' }[s] || s)
</script>
