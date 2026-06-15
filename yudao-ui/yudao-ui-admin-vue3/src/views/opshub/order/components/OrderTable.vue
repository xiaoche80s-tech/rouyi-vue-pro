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
    <el-table-column align="center" label="操作" width="180" fixed="right">
      <template #default="{ row }">
        <el-dropdown trigger="click" @command="(cmd: string) => handleCommand(cmd, row)">
          <el-button link type="primary">
            操作<Icon class="ml-4px" icon="ep:arrow-down" />
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="detail">查看详情</el-dropdown-item>
              <!-- 付款：dealer:order:pay + 未付款 -->
              <el-dropdown-item
                v-if="row.payStatus === 'unpaid' && checkPermi(['dealer:order:pay'])"
                command="pay"
              >申请付款</el-dropdown-item>
              <!-- 开票：dealer:order:invoice + 未全部开票 -->
              <el-dropdown-item
                v-if="row.invStatus !== 'invoiced' && checkPermi(['dealer:order:invoice'])"
                command="invoice"
              >申请开票</el-dropdown-item>
              <!-- 退货：dealer:order:return + 已签收/已完成 -->
              <el-dropdown-item
                v-if="['signed', 'completed'].includes(row.progressStatus) && checkPermi(['dealer:order:return'])"
                command="return"
              >申请退货</el-dropdown-item>
              <!-- 咨询 -->
              <el-dropdown-item
                v-if="checkPermi(['dealer:order:consult'])"
                command="consult"
              >咨询</el-dropdown-item>
              <!-- 更新进度：dealer:order:update + 非已完成 -->
              <el-dropdown-item
                v-if="row.progressStatus !== 'completed' && checkPermi(['dealer:order:update'])"
                command="progress"
              >更新进度</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
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
  (e: 'applyReturn', row: OrderSimpleVO): void
  (e: 'consult', row: OrderSimpleVO): void
  (e: 'updateProgress', row: OrderSimpleVO): void
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

// ========== 操作分发 ==========
const handleCommand = (cmd: string, row: OrderSimpleVO) => {
  switch (cmd) {
    case 'detail': emit('viewDetail', row); break
    case 'pay': emit('applyPayment', row); break
    case 'invoice': emit('applyInvoice', row); break
    case 'return': emit('applyReturn', row); break
    case 'consult': emit('consult', row); break
    case 'progress': emit('updateProgress', row); break
  }
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
