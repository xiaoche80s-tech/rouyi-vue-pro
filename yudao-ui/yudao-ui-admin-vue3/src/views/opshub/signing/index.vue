<template>
  <!-- 统计卡片 -->
  <StatisticsCards ref="statsCardsRef" />

  <!-- 筛选栏 -->
  <SigningFilterBar
    :dealer-list="dealerList"
    :product-line-list="productLineList"
    @search="handleSearch"
    @reset="handleReset"
  />

  <!-- 操作栏 + 表格 -->
  <ContentWrap>
    <div class="mb-10px flex items-center gap-10px">
      <el-button
        v-hasPermi="['dealer:signing:create']"
        type="primary"
        plain
        @click="handleCreate"
      >
        <Icon class="mr-5px" icon="ep:plus" />
        新建合同
      </el-button>
      <el-button
        v-hasPermi="['dealer:signing:stamp']"
        type="warning"
        plain
        :disabled="selectedRows.length === 0"
        @click="handleBatchStamp"
      >
        <Icon class="mr-5px" icon="ep:stamp" />
        批量盖章
      </el-button>
      <el-button
        v-hasPermi="['dealer:signing:consult']"
        plain
        :disabled="selectedRows.length === 0"
        @click="handleBatchConsult"
      >
        <Icon class="mr-5px" icon="ep:chat-dot-round" />
        批量咨询
      </el-button>
    </div>

    <ContractTable
      v-loading="loading"
      :list="list"
      @selection-change="handleSelectionChange"
      @edit="handleEdit"
      @sign="handleSign"
      @upload-proof="handleUploadProof"
      @verify="handleVerify"
      @view-process="handleViewProcess"
      @view-detail="handleViewDetail"
      @consult="handleConsult"
    />

    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 趋势图 -->
  <SigningTrendChart ref="trendChartRef" />

  <!-- 弹窗 -->
  <ContractFormModal ref="contractFormModalRef" @success="getList" />
  <ContractPreviewModal ref="previewModalRef" />
  <CreateOpRequestModal ref="createOpRequestModalRef" @success="getList" />
  <ProcessOpRequestModal ref="processModalRef" @success="getList" />
  <VerifyOpRequestModal ref="verifyModalRef" @success="getList" />

  <!-- 咨询聊天窗口 -->
  <ChatWindow v-model="chatVisible" :session-id="currentSessionId" mode="dealer" />
</template>

<script lang="ts" setup>
import * as SigningApi from '@/api/opshub/signing'
import * as DealerApi from '@/api/opshub/dealer'
import StatisticsCards from './components/StatisticsCards.vue'
import SigningFilterBar from './components/SigningFilterBar.vue'
import ContractTable from './components/ContractTable.vue'
import SigningTrendChart from './components/SigningTrendChart.vue'
import ContractFormModal from './components/ContractFormModal.vue'
import ContractPreviewModal from './components/ContractPreviewModal.vue'
import CreateOpRequestModal from '@/views/opshub/oprequest/components/CreateOpRequestModal.vue'
import ProcessOpRequestModal from '@/views/opshub/oprequest/components/ProcessOpRequestModal.vue'
import VerifyOpRequestModal from '@/views/opshub/oprequest/components/VerifyOpRequestModal.vue'
import ChatWindow from '@/components/CsChatWindow/ChatWindow.vue'
import { useCsConsult } from '@/hooks/useCsConsult'
import { useRouter } from 'vue-router'

defineOptions({ name: 'OpshubSigning' })

const message = useMessage()

// ========== 数据 ==========
const loading = ref(false)
const total = ref(0)
const list = ref<SigningApi.SigningContractVO[]>([])
const selectedRows = ref<SigningApi.SigningContractVO[]>([])
const dealerList = ref<any[]>([])
const productLineList = ref<any[]>([])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 50,
  timeDimension: undefined as string | undefined,
  months: undefined as number[] | undefined,
  quarters: undefined as number[] | undefined,
  years: undefined as number[] | undefined,
  productLineCodes: undefined as string[] | undefined,
  contractTypes: undefined as string[] | undefined,
  statuses: undefined as string[] | undefined,
  dealerCodes: undefined as string[] | undefined,
  keyword: undefined as string | undefined
})

// ========== 方法 ==========
const getList = async () => {
  loading.value = true
  try {
    const data = await SigningApi.getSigningContractPage(queryParams)
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
  queryParams.timeDimension = undefined
  queryParams.months = undefined
  queryParams.quarters = undefined
  queryParams.years = undefined
  queryParams.productLineCodes = undefined
  queryParams.contractTypes = undefined
  queryParams.statuses = undefined
  queryParams.dealerCodes = undefined
  queryParams.keyword = undefined
  queryParams.pageNo = 1
  getList()
}

const handleSelectionChange = (rows: SigningApi.SigningContractVO[]) => {
  selectedRows.value = rows
}

// ========== 操作 ==========
const contractFormModalRef = ref()
const handleCreate = () => {
  contractFormModalRef.value.open('create')
}
const handleEdit = (row: SigningApi.SigningContractVO) => {
  contractFormModalRef.value.open('update', row)
}

const createOpRequestModalRef = ref()
const handleSign = (row: SigningApi.SigningContractVO) => {
  createOpRequestModalRef.value.open('signing', row)
}

const processModalRef = ref()
const handleUploadProof = (row: SigningApi.SigningContractVO) => {
  processModalRef.value.open(row)
}

const verifyModalRef = ref()
const handleVerify = (row: SigningApi.SigningContractVO) => {
  verifyModalRef.value.open(row)
}

const router = useRouter()
const handleViewProcess = (row: any) => {
  if (row.processInstanceId) {
    router.push({ name: 'BpmProcessInstanceDetail', query: { id: row.processInstanceId } })
  }
}

const previewModalRef = ref()
const handleViewDetail = (row: SigningApi.SigningContractVO) => {
  previewModalRef.value.open(row)
}

const handleBatchStamp = () => {
  message.info('批量盖章功能开发中')
}

// ========== 咨询集成 ==========
const { chatVisible, currentSessionId, openConsult, openBatchConsult } = useCsConsult()

const handleConsult = (row: SigningApi.SigningContractVO) => {
  openConsult({
    consultType: 'signing',
    sourceModule: 'signing',
    context: `${row.contractName || row.contractCode} 签署流程咨询`,
    contextId: row.id,
    contextCode: row.contractCode,
    productLineCode: row.productLineCode,
    productLineName: row.productLineName,
    dealerCode: row.dealerCode,
    dealerName: row.dealerName
  })
}

const handleBatchConsult = async () => {
  await openBatchConsult(selectedRows.value, (row) => ({
    consultType: 'signing',
    sourceModule: 'signing',
    context: `${row.contractName || row.contractCode} 签署流程咨询`,
    contextId: row.id,
    contextCode: row.contractCode,
    productLineCode: row.productLineCode,
    productLineName: row.productLineName,
    dealerCode: row.dealerCode,
    dealerName: row.dealerName
  }))
}

// ========== 引用 ==========
const statsCardsRef = ref()
const trendChartRef = ref()

onMounted(async () => {
  await getList()
  // 加载经销商列表
  try {
    dealerList.value = await DealerApi.getSimpleDealerList()
  } catch (e) {
    console.error('加载经销商列表失败', e)
  }
  // 加载产品线列表
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
