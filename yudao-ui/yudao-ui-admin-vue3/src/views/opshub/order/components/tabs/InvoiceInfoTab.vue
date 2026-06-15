<template>
  <div v-if="detail">
    <!-- 开票概要 -->
    <el-descriptions :column="3" border class="mb-16px">
      <el-descriptions-item label="订单金额">¥{{ formatAmount(detail.totalAmount) }}</el-descriptions-item>
      <el-descriptions-item label="已开票金额">
        <span :class="detail.invoicedAmount >= detail.totalAmount ? 'text-green-600' : 'text-purple-600'">
          ¥{{ formatAmount(detail.invoicedAmount) }}
        </span>
      </el-descriptions-item>
      <el-descriptions-item label="开票状态">
        <el-tag :type="invTagType(detail.invStatus)" size="small">{{ invLabel(detail.invStatus) }}</el-tag>
      </el-descriptions-item>
    </el-descriptions>

    <!-- 申请开票按钮 -->
    <div class="mb-12px">
      <el-button
        v-hasPermi="['dealer:order:invoice']"
        type="success"
        :disabled="detail.invStatus === 'invoiced'"
        @click="emit('applyInvoice')"
      >
        <Icon class="mr-5px" icon="ep:document" />申请开票
      </el-button>
    </div>

    <!-- 开票记录表 -->
    <el-table :data="detail.invoices" border size="small">
      <el-table-column align="right" label="开票金额" width="120">
        <template #default="{ row }">¥{{ formatAmount(row.invoiceAmount) }}</template>
      </el-table-column>
      <el-table-column align="center" label="发票号" prop="invoiceNo" width="140">
        <template #default="{ row }">{{ row.invoiceNo || '-' }}</template>
      </el-table-column>
      <el-table-column align="center" label="开票日期" width="110">
        <template #default="{ row }">{{ row.invoiceDate ? formatDate(row.invoiceDate, 'YYYY-MM-DD') : '-' }}</template>
      </el-table-column>
      <el-table-column align="center" label="发票类型" prop="invoiceType" width="100" />
      <el-table-column align="center" label="抬头" prop="companyName" min-width="150" show-overflow-tooltip />
      <el-table-column align="center" label="税号" prop="taxNo" width="160" show-overflow-tooltip />
      <el-table-column align="center" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 'invoiced' ? 'success' : 'warning'" size="small">
            {{ row.status === 'invoiced' ? '已开票' : '待开票' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="特殊需求" prop="specialRequest" min-width="120" show-overflow-tooltip />
      <el-table-column align="center" label="申请时间" width="160">
        <template #default="{ row }">{{ formatDate(row.applyTime, 'YYYY-MM-DD HH:mm') }}</template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script lang="ts" setup>
import type { OrderDetailVO } from '@/api/opshub/order'
import { formatDate } from '@/utils/formatTime'

defineProps<{ detail: OrderDetailVO | null }>()
const emit = defineEmits<{ (e: 'applyInvoice'): void }>()

const formatAmount = (val: number | undefined) => {
  if (val == null) return '0.00'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const invTagType = (s: string) => ({ uninvoiced: 'warning', partial: '', invoiced: 'success' }[s] || 'info')
const invLabel = (s: string) => ({ uninvoiced: '未开票', partial: '部分开票', invoiced: '已开票' }[s] || s)
</script>
