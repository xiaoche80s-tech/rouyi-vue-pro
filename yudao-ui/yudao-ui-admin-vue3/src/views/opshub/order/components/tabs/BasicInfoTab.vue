<template>
  <div v-if="detail">
    <!-- 基本信息 -->
    <el-descriptions :column="3" border>
      <el-descriptions-item label="订单号">{{ detail.orderCode }}</el-descriptions-item>
      <el-descriptions-item label="经销商">{{ detail.dealerName }}</el-descriptions-item>
      <el-descriptions-item label="产品线">{{ detail.productLineName }}</el-descriptions-item>
      <el-descriptions-item label="订单金额">
        <span class="font-bold">¥{{ formatAmount(detail.totalAmount) }}</span>
      </el-descriptions-item>
      <el-descriptions-item label="订单日期">{{ formatDate(detail.orderDate, 'YYYY-MM-DD') }}</el-descriptions-item>
      <el-descriptions-item label="进度状态">
        <el-tag :type="progressTagType(detail.progressStatus)">{{ progressLabel(detail.progressStatus) }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="付款状态">
        <el-tag :type="detail.payStatus === 'paid' ? 'success' : 'warning'" size="small">
          {{ detail.payStatus === 'paid' ? '已付款' : '未付款' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="开票状态">
        <el-tag :type="invTagType(detail.invStatus)" size="small">{{ invLabel(detail.invStatus) }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="备注" :span="3">{{ detail.remark || '-' }}</el-descriptions-item>
    </el-descriptions>

    <!-- 时间线（5 节点进度） -->
    <div class="mt-20px mb-10px font-bold text-14px">订单进度</div>
    <el-steps :active="activeStep" finish-status="success" align-center>
      <el-step
        v-for="node in detail.timeline"
        :key="node.nodeCode"
        :title="node.nodeName"
        :description="node.nodeTime ? formatDate(node.nodeTime, 'YYYY-MM-DD HH:mm') : ''"
        :status="node.isCompleted ? 'finish' : 'wait'"
      />
    </el-steps>

    <!-- 产品明细 -->
    <div class="mt-20px mb-10px font-bold text-14px">产品明细</div>
    <el-table :data="detail.products" border size="small">
      <el-table-column align="center" label="产品名称" prop="productName" min-width="150" show-overflow-tooltip />
      <el-table-column align="center" label="产品编码" prop="productCode" width="120" />
      <el-table-column align="center" label="规格型号" prop="specModel" width="120" />
      <el-table-column align="right" label="单价" width="100">
        <template #default="{ row }">¥{{ formatAmount(row.unitPrice) }}</template>
      </el-table-column>
      <el-table-column align="center" label="数量" prop="quantity" width="80" />
      <el-table-column align="center" label="单位" prop="unit" width="60" />
      <el-table-column align="right" label="金额" width="120">
        <template #default="{ row }">¥{{ formatAmount(row.amount) }}</template>
      </el-table-column>
      <el-table-column align="center" label="可退货数量" prop="returnableQty" width="100" />
    </el-table>
  </div>
</template>

<script lang="ts" setup>
import type { OrderDetailVO } from '@/api/opshub/order'
import { formatDate } from '@/utils/formatTime'

const props = defineProps<{ detail: OrderDetailVO | null }>()

const formatAmount = (val: number | undefined) => {
  if (val == null) return '0.00'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const activeStep = computed(() => {
  if (!props.detail?.timeline) return 0
  return props.detail.timeline.filter((n) => n.isCompleted).length
})

const progressTagType = (s: string) => ({ pending: 'info', confirmed: 'warning', shipped: '', signed: 'success', completed: 'success' }[s] || 'info')
const progressLabel = (s: string) => ({ pending: '待确认', confirmed: '已确认', shipped: '已发货', signed: '已签收', completed: '已完成' }[s] || s)
const invTagType = (s: string) => ({ uninvoiced: 'warning', partial: '', invoiced: 'success' }[s] || 'info')
const invLabel = (s: string) => ({ uninvoiced: '未开票', partial: '部分开票', invoiced: '已开票' }[s] || s)
</script>
