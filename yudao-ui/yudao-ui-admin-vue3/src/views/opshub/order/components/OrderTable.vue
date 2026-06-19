<template>
  <el-table
    :data="list"
    @selection-change="(rows: any[]) => emit('selectionChange', rows)"
    show-summary
    :summary-method="getSummary"
  >
    <el-table-column type="selection" width="55" />
    <el-table-column align="center" label="订单号" min-width="160" show-overflow-tooltip>
      <template #default="{ row }">
        <span class="cursor-pointer text-blue-600" @click="emit('viewDetail', row)">
          {{ row.orderCode }}
        </span>
      </template>
    </el-table-column>
    <!-- 经销商列：仅非经销商角色可见 -->
    <el-table-column
      v-if="checkPermi(['dealer:order:update'])"
      align="center" label="经销商" min-width="120" show-overflow-tooltip
    >
      <template #default="{ row }">
        {{ row.dealerName || row.dealerCode || '-' }}
      </template>
    </el-table-column>
    <el-table-column align="center" label="产品线" min-width="100" show-overflow-tooltip>
      <template #default="{ row }">
        {{ row.productLineName || row.productLineCode || '-' }}
      </template>
    </el-table-column>
    <el-table-column align="right" label="订单金额" min-width="120">
      <template #default="{ row }">
        ¥{{ formatAmount(row.totalAmount) }}
      </template>
    </el-table-column>
    <el-table-column align="center" label="订单日期" width="110">
      <template #default="{ row }">
        {{ formatDate(row.orderDate, 'YYYY-MM-DD') }}
      </template>
    </el-table-column>
    <el-table-column align="center" label="进度" width="90">
      <template #default="{ row }">
        <el-tag :type="progressTagType(row.progressStatus)">
          {{ progressLabel(row.progressStatus) }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column align="center" label="付款" width="90">
      <template #default="{ row }">
        <el-tag :type="row.payStatus === 'paid' ? 'success' : 'warning'" size="small">
          {{ row.payStatus === 'paid' ? '已付款' : '未付款' }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column align="center" label="开票" width="100">
      <template #default="{ row }">
        <el-tag :type="invTagType(row.invStatus)" size="small">
          {{ invLabel(row.invStatus) }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column align="right" label="已付金额" min-width="110">
      <template #default="{ row }">
        ¥{{ formatAmount(row.paidAmount) }}
      </template>
    </el-table-column>
    <el-table-column align="right" label="已开票金额" min-width="110">
      <template #default="{ row }">
        ¥{{ formatAmount(row.invoicedAmount) }}
      </template>
    </el-table-column>
    <el-table-column align="center" label="操作" width="140" fixed="right">
      <template #default="{ row }">
        <el-button
          v-if="row.payStatus === 'unpaid' && checkPermi(['dealer:order:pay'])"
          link type="primary" @click="emit('applyPayment', row)"
        >申请付款</el-button>
        <span
          v-if="((row.payStatus === 'unpaid' && checkPermi(['dealer:order:pay'])) || (row.payStatus === 'paid' && row.invStatus !== 'invoiced' && checkPermi(['dealer:order:invoice']))) && checkPermi(['dealer:order:consult'])"
          class="action-sep"
        >/</span>
        <el-button
          v-if="row.payStatus === 'paid' && row.invStatus !== 'invoiced' && checkPermi(['dealer:order:invoice'])"
          link type="primary" @click="emit('applyInvoice', row)"
        >申请开票</el-button>
        <span
          v-if="row.payStatus === 'paid' && row.invStatus !== 'invoiced' && checkPermi(['dealer:order:invoice']) && checkPermi(['dealer:order:consult'])"
          class="action-sep"
        >/</span>
        <el-button
          v-if="checkPermi(['dealer:order:consult'])"
          link type="primary" @click="emit('consult', row)"
        >客服</el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script lang="ts" setup>
import type { OrderSimpleVO } from '@/api/opshub/order'
import { formatDate } from '@/utils/formatTime'
import { checkPermi } from '@/utils/permission'

defineProps<{
  list: OrderSimpleVO[]
}>()

const emit = defineEmits<{
  (e: 'selectionChange', rows: OrderSimpleVO[]): void
  (e: 'viewDetail', row: OrderSimpleVO): void
  (e: 'applyPayment', row: OrderSimpleVO): void
  (e: 'applyInvoice', row: OrderSimpleVO): void
  (e: 'consult', row: OrderSimpleVO): void
}>()

// ========== 格式化 ==========
const formatAmount = (val: number | undefined) => {
  if (val == null) return '0.00'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

// ========== 标签映射 ==========
const progressTagType = (status: string) => {
  const map: Record<string, string> = {
    pending: 'info', confirmed: 'warning', shipped: '', signed: 'success', completed: 'success'
  }
  return map[status] || 'info'
}
const progressLabel = (status: string) => {
  const map: Record<string, string> = {
    pending: '待确认', confirmed: '已确认', shipped: '已发货', signed: '已签收', completed: '已完成'
  }
  return map[status] || status
}
const invTagType = (status: string) => {
  const map: Record<string, string> = { uninvoiced: 'warning', partial: '', invoiced: 'success' }
  return map[status] || 'info'
}
const invLabel = (status: string) => {
  const map: Record<string, string> = { uninvoiced: '未开票', partial: '部分开票', invoiced: '已开票' }
  return map[status] || status
}

// ========== 合计行 ==========
const getSummary = (param: { columns: any[]; data: OrderSimpleVO[] }) => {
  const { columns, data } = param
  const sums: string[] = []
  columns.forEach((col, idx) => {
    if (idx === 0) { sums[idx] = '合计'; return }
    const sumFields = ['totalAmount', 'paidAmount', 'invoicedAmount']
    if (sumFields.includes(col.property)) {
      const total = data.reduce((sum, row) => sum + (Number((row as any)[col.property]) || 0), 0)
      sums[idx] = '¥' + formatAmount(total)
    } else {
      sums[idx] = ''
    }
  })
  return sums
}
</script>

<style scoped>
.action-sep {
  color: #dcdfe6;
  margin: 0 4px;
  font-size: 13px;
  user-select: none;
}
</style>
