<template>
  <el-dialog v-model="visible" title="订单详情" width="900px" destroy-on-close>
    <el-skeleton :loading="loading" animated :rows="12">
      <template #default>
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本信息" name="basic">
            <BasicInfoTab :detail="detail" />
          </el-tab-pane>
          <el-tab-pane label="付款信息" name="payment">
            <PaymentInfoTab :detail="detail" @apply-payment="handleApplyPayment" />
          </el-tab-pane>
          <el-tab-pane label="开票信息" name="invoice">
            <InvoiceInfoTab :detail="detail" @apply-invoice="handleApplyInvoice" />
          </el-tab-pane>
          <el-tab-pane label="退货管理" name="return">
            <ReturnInfoTab :detail="detail" @apply-return="handleApplyReturn" />
          </el-tab-pane>
          <el-tab-pane label="物流轨迹" name="logistics">
            <LogisticsTab :detail="detail" />
          </el-tab-pane>
          <el-tab-pane label="沟通记录" name="communication">
            <CommunicationTab />
          </el-tab-pane>
        </el-tabs>
      </template>
    </el-skeleton>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 操作弹窗 -->
  <ApplyPaymentModal ref="paymentModalRef" @success="emit('refresh')" />
  <ApplyInvoiceModal ref="invoiceModalRef" @success="emit('refresh')" />
  <ApplyReturnModal ref="returnModalRef" @success="emit('refresh')" />
</template>

<script lang="ts" setup>
import * as OrderApi from '@/api/opshub/order'
import BasicInfoTab from './tabs/BasicInfoTab.vue'
import PaymentInfoTab from './tabs/PaymentInfoTab.vue'
import InvoiceInfoTab from './tabs/InvoiceInfoTab.vue'
import ReturnInfoTab from './tabs/ReturnInfoTab.vue'
import LogisticsTab from './tabs/LogisticsTab.vue'
import CommunicationTab from './tabs/CommunicationTab.vue'
import ApplyPaymentModal from './ApplyPaymentModal.vue'
import ApplyInvoiceModal from './ApplyInvoiceModal.vue'
import ApplyReturnModal from './ApplyReturnModal.vue'

const emit = defineEmits<{ (e: 'refresh'): void }>()

const visible = ref(false)
const loading = ref(false)
const activeTab = ref('basic')
const detail = ref<OrderApi.OrderDetailVO | null>(null)

const open = async (id: number) => {
  visible.value = true
  loading.value = true
  activeTab.value = 'basic'
  try {
    detail.value = await OrderApi.getOrderDetail(id)
  } catch (e) {
    console.error('加载订单详情失败', e)
  } finally {
    loading.value = false
  }
}

// 子 Tab 触发操作
const paymentModalRef = ref()
const invoiceModalRef = ref()
const returnModalRef = ref()

const handleApplyPayment = () => {
  if (!detail.value) return
  paymentModalRef.value.open(detail.value.id, detail.value.orderCode)
}
const handleApplyInvoice = () => {
  if (!detail.value) return
  invoiceModalRef.value.open(detail.value.id, detail.value.orderCode)
}
const handleApplyReturn = () => {
  if (!detail.value) return
  returnModalRef.value.open(
    detail.value.id,
    detail.value.orderCode,
    detail.value.returnableProducts
  )
}

defineExpose({ open })
</script>
