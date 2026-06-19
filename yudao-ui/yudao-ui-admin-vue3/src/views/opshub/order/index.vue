<template>
  <!-- 统计卡片 -->
  <OrderStatisticsCards ref="statsCardsRef" @filter="handleCardFilter" />

  <!-- 筛选栏 -->
  <OrderFilterBar
    :dealer-list="dealerList"
    :product-line-list="productLineList"
    @search="handleSearch"
    @reset="handleReset"
  />

  <!-- 批量操作栏 + 表格 -->
  <ContentWrap>
    <div class="mb-10px flex items-center gap-10px">
      <el-button
        v-hasPermi="['dealer:order:pay']"
        type="primary"
        plain
        :disabled="selectedRows.length === 0"
        @click="handleBatchPayment"
      >
        <Icon class="mr-5px" icon="ep:money" />
        批量付款
      </el-button>
      <el-button
        v-hasPermi="['dealer:order:invoice']"
        type="success"
        plain
        :disabled="selectedRows.length === 0"
        @click="handleBatchInvoice"
      >
        <Icon class="mr-5px" icon="ep:document" />
        批量开票
      </el-button>
      <el-button
        v-hasPermi="['dealer:order:return']"
        type="warning"
        plain
        :disabled="selectedRows.length === 0"
        @click="handleBatchReturn"
      >
        <Icon class="mr-5px" icon="ep:refresh-left" />
        批量退货
      </el-button>
      <el-button
        v-hasPermi="['dealer:order:consult']"
        plain
        :disabled="selectedRows.length === 0"
        @click="handleBatchConsult"
      >
        <Icon class="mr-5px" icon="ep:chat-dot-round" />
        批量咨询
      </el-button>
    </div>

    <OrderTable
      v-loading="loading"
      :list="list"
      @selection-change="handleSelectionChange"
      @view-detail="handleViewDetail"
      @apply-payment="handleApplyPayment"
      @apply-invoice="handleApplyInvoice"
      @consult="handleConsult"
    />

    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 弹窗 -->
  <OrderDetailModal ref="detailModalRef" />
  <ApplyPaymentModal ref="paymentModalRef" @success="handleOperationSuccess" />
  <ApplyInvoiceModal ref="invoiceModalRef" @success="handleOperationSuccess" />

  <!-- 咨询聊天窗口 -->
  <ChatWindow v-model="chatVisible" :session-id="currentSessionId" mode="dealer" />
</template>

<script lang="ts" setup>
import * as OrderApi from '@/api/opshub/order'
import * as DealerApi from '@/api/opshub/dealer'
import OrderStatisticsCards from './components/OrderStatisticsCards.vue'
import OrderFilterBar from './components/OrderFilterBar.vue'
import OrderTable from './components/OrderTable.vue'
import OrderDetailModal from './components/OrderDetailModal.vue'
import ApplyPaymentModal from './components/ApplyPaymentModal.vue'
import ApplyInvoiceModal from './components/ApplyInvoiceModal.vue'
import ChatWindow from '@/components/CsChatWindow/ChatWindow.vue'
import { useCsConsult } from '@/hooks/useCsConsult'

defineOptions({ name: 'OpshubOrder' })

const message = useMessage()

// ========== 咨询集成 ==========
const { chatVisible, currentSessionId, openConsult, openBatchConsult } = useCsConsult()

// ========== 数据 ==========
const loading = ref(false)
const total = ref(0)
const list = ref<OrderApi.OrderSimpleVO[]>([])
const selectedRows = ref<OrderApi.OrderSimpleVO[]>([])
const dealerList = ref<any[]>([])
const productLineList = ref<any[]>([])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 50,
  quickTime: undefined as string | undefined,
  quarters: undefined as number[] | undefined,
  months: undefined as number[] | undefined,
  productLineCodes: undefined as string[] | undefined,
  dealerCodes: undefined as string[] | undefined,
  progressStatus: undefined as string | undefined,
  payStatus: undefined as string | undefined,
  invStatus: undefined as string | undefined,
  keyword: undefined as string | undefined
})

// ========== 列表 ==========
const getList = async () => {
  loading.value = true
  try {
    const data = await OrderApi.getOrderPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = (filters: any) => {
  Object.assign(queryParams, filters)
  queryParams.pageNo = 1
  getList()
}

const handleReset = () => {
  queryParams.quickTime = undefined
  queryParams.quarters = undefined
  queryParams.months = undefined
  queryParams.productLineCodes = undefined
  queryParams.dealerCodes = undefined
  queryParams.progressStatus = undefined
  queryParams.payStatus = undefined
  queryParams.invStatus = undefined
  queryParams.keyword = undefined
  queryParams.pageNo = 1
  getList()
}

const handleCardFilter = (filters: any) => {
  // 重置分页与过滤
  queryParams.pageNo = 1
  queryParams.progressStatus = filters.progressStatus
  queryParams.payStatus = filters.payStatus
  queryParams.invStatus = filters.invStatus
  getList()
}

const handleSelectionChange = (rows: OrderApi.OrderSimpleVO[]) => {
  selectedRows.value = rows
}

// ========== 单个操作 ==========
const detailModalRef = ref()
const handleViewDetail = (row: OrderApi.OrderSimpleVO) => {
  detailModalRef.value.open(row.id)
}

const paymentModalRef = ref()
const handleApplyPayment = (row: OrderApi.OrderSimpleVO) => {
  paymentModalRef.value.open(row.id, row.orderCode)
}

const invoiceModalRef = ref()
const handleApplyInvoice = (row: OrderApi.OrderSimpleVO) => {
  invoiceModalRef.value.open(row.id, row.orderCode)
}

const handleConsult = (row: OrderApi.OrderSimpleVO) => {
  openConsult({
    consultType: 'order',
    sourceModule: 'order',
    context: `订单 ${row.orderCode} 咨询`,
    contextId: row.id,
    contextCode: row.orderCode,
    productLineCode: row.productLineCode,
    productLineName: row.productLineName,
    dealerCode: row.dealerCode,
    dealerName: row.dealerName
  })
}

// ========== 批量操作 ==========
const handleBatchPayment = async () => {
  const unpaidRows = selectedRows.value.filter((r) => r.payStatus === 'unpaid')
  if (unpaidRows.length === 0) {
    message.warning('所选订单中没有未付款的订单')
    return
  }
  try {
    await message.confirm(`确认批量申请付款 ${unpaidRows.length} 条订单？`)
    const ids = unpaidRows.map((r) => r.id)
    await OrderApi.batchApplyPayment(ids)
    message.success(`已提交 ${unpaidRows.length} 条付款申请`)
    await refreshAll()
  } catch (e: any) {
    if (e !== 'cancel') message.error('批量付款失败')
  }
}

const handleBatchInvoice = async () => {
  const rows = selectedRows.value.filter((r) => r.invStatus !== 'invoiced')
  if (rows.length === 0) {
    message.warning('所选订单中没有可开票的订单')
    return
  }
  try {
    await message.confirm(`确认批量申请开票 ${rows.length} 条订单？`)
    const ids = rows.map((r) => r.id)
    await OrderApi.batchApplyInvoice(ids)
    message.success(`已提交 ${rows.length} 条开票申请`)
    await refreshAll()
  } catch (e: any) {
    if (e !== 'cancel') message.error('批量开票失败')
  }
}

const handleBatchReturn = async () => {
  const eligibleRows = selectedRows.value.filter((r) =>
    ['signed', 'completed'].includes(r.progressStatus)
  )
  if (eligibleRows.length === 0) {
    message.warning('所选订单中没有已签收/可退货的订单')
    return
  }
  try {
    await message.confirm(`确认批量申请退货 ${eligibleRows.length} 条订单？`)
    const items = eligibleRows.map((r) => ({ orderId: r.id, items: [] }))
    await OrderApi.batchApplyReturn(items)
    message.success(`已提交 ${eligibleRows.length} 条退货申请`)
    await refreshAll()
  } catch (e: any) {
    if (e !== 'cancel') message.error('批量退货失败')
  }
}

const handleBatchConsult = async () => {
  await openBatchConsult(selectedRows.value, (row) => ({
    consultType: 'order',
    sourceModule: 'order',
    context: `订单 ${row.orderCode} 咨询`,
    contextId: row.id,
    contextCode: row.orderCode,
    productLineCode: row.productLineCode,
    productLineName: row.productLineName,
    dealerCode: row.dealerCode,
    dealerName: row.dealerName
  }))
}

// ========== 刷新 ==========
const statsCardsRef = ref()
const refreshAll = async () => {
  await getList()
  statsCardsRef.value?.refresh()
}

const handleOperationSuccess = () => {
  refreshAll()
}

// ========== 初始化 ==========
onMounted(async () => {
  await getList()
  try {
    dealerList.value = await DealerApi.getSimpleDealerList()
  } catch (e) {
    console.error('加载经销商列表失败', e)
  }
  try {
    const plList = await DealerApi.getSimpleProductLineList()
    productLineList.value = plList.map((pl: any) => ({
      productLineCode: pl.productLineCode,
      productLineName: pl.productLineName
    }))
  } catch (e) {
    console.error('加载产品线列表失败', e)
  }
})
</script>
