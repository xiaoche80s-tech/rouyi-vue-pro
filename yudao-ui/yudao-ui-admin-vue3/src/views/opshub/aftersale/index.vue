<template>
  <!-- 统计卡片 -->
  <div class="flex gap-10px mb-10px">
    <el-card shadow="hover" class="stat-card" @click="handleCardFilter('all')">
      <div class="stat-value">{{ stats.totalCount }}</div>
      <div class="stat-label">售后订单总数</div>
    </el-card>
    <el-card shadow="hover" class="stat-card" @click="handleCardFilter('refund')">
      <div class="stat-value">{{ stats.refundCount }}</div>
      <div class="stat-label">退款</div>
    </el-card>
    <el-card shadow="hover" class="stat-card" @click="handleCardFilter('return')">
      <div class="stat-value">{{ stats.returnCount }}</div>
      <div class="stat-label">退货</div>
    </el-card>
    <el-card shadow="hover" class="stat-card" @click="handleCardFilter('completed')">
      <div class="stat-value">{{ stats.completedCount }} <span class="stat-rate">({{ stats.completedRate }}%)</span></div>
      <div class="stat-label">已完成</div>
    </el-card>
    <el-card shadow="hover" class="stat-card" @click="handleCardFilter('incomplete')">
      <div class="stat-value">{{ stats.inProgressCount }}</div>
      <div class="stat-label">未完成</div>
    </el-card>
  </div>

  <!-- 筛选栏 -->
  <ContentWrap>
    <el-form :model="queryParams" inline>
      <el-form-item label="售后类型">
        <el-select v-model="queryParams.handlingMethod" clearable placeholder="全部" style="width: 130px">
          <el-option label="退货" value="return" />
          <el-option label="退换货" value="exchange" />
          <el-option label="退货退款" value="return_refund" />
        </el-select>
      </el-form-item>
      <el-form-item label="售后原因">
        <el-select v-model="queryParams.reason" clearable placeholder="全部" style="width: 120px">
          <el-option label="投诉" value="complaint" />
          <el-option label="召回" value="recall" />
          <el-option label="破损" value="damage" />
        </el-select>
      </el-form-item>
      <el-form-item label="进度">
        <el-select v-model="queryParams.progressStatus" clearable placeholder="全部" style="width: 120px">
          <el-option label="待处理" value="pending" />
          <el-option label="进行中" value="in_progress" />
          <el-option label="换货中" value="exchanging" />
          <el-option label="已完成" value="completed" />
        </el-select>
      </el-form-item>
      <el-form-item label="产品线">
        <el-select v-model="queryParams.productLineCodes" multiple clearable placeholder="全部" style="width: 180px">
          <el-option v-for="pl in productLineList" :key="pl.productLineCode" :label="pl.productLineName" :value="pl.productLineCode" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="isAdminRole" label="经销商">
        <el-select v-model="queryParams.dealerCodes" multiple clearable placeholder="全部" style="width: 180px">
          <el-option v-for="d in dealerList" :key="d.dealerCode" :label="d.dealerName" :value="d.dealerCode" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-input v-model="queryParams.keyword" clearable placeholder="搜索售后单号/订单号" style="width: 200px" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch"><Icon icon="ep:search" class="mr-4px" />搜索</el-button>
        <el-button @click="handleReset"><Icon icon="ep:refresh" class="mr-4px" />重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 批量操作栏 + 表格 -->
  <ContentWrap>
    <div class="mb-10px flex items-center gap-10px">
      <el-button
        v-hasPermi="['dealer:aftersale:consult']"
        plain
        :disabled="selectedRows.length === 0"
        @click="handleBatchConsult"
      >
        <Icon class="mr-5px" icon="ep:chat-dot-round" />
        批量咨询
      </el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="list"
      @selection-change="handleSelectionChange"
      @sort-change="handleSortChange"
      border
      stripe
    >
      <el-table-column v-if="showCheckbox" type="selection" width="40" />
      <el-table-column v-if="isAdminRole" label="经销商" prop="dealerName" min-width="140" sortable="custom" />
      <el-table-column label="售后单号" prop="aftersaleCode" min-width="160" sortable="custom" />
      <el-table-column label="关联订单" prop="orderCode" min-width="180" sortable="custom" />
      <el-table-column label="时间" prop="applyTime" min-width="160" sortable="custom" />
      <el-table-column label="类型" min-width="140" sortable="custom" prop="handlingMethod">
        <template #default="{ row }">
          <el-tag :type="getTypeTagType(row.handlingMethod)" size="small">
            {{ handlingMethodMap[row.handlingMethod] }}-{{ reasonMap[row.reason] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="产品" prop="productName" min-width="140" sortable="custom" />
      <el-table-column label="当前进度" min-width="140" sortable="custom" prop="progressStatus">
        <template #default="{ row }">
          <span>{{ row.currentStep }}/5 {{ getCurrentStepName(row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="进度详情" width="100" align="center">
        <template #default="{ row }">
          <el-popover trigger="hover" width="300" placement="left">
            <template #reference>
              <el-button link type="primary" size="small">查看</el-button>
            </template>
            <el-steps :active="row.currentStep" direction="vertical" :space="30">
              <el-step
                v-for="node in row.progressNodes"
                :key="node.nodeCode"
                :title="node.nodeName"
                :status="node.isCompleted ? 'finish' : 'wait'"
                :description="formatNodeTime(node.nodeTime)"
              />
            </el-steps>
          </el-popover>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button
            v-hasPermi="['dealer:aftersale:update-progress']"
            link
            type="primary"
            size="small"
            :disabled="row.progressStatus === 'completed'"
            @click="handleUpdateProgress(row)"
          >更新进度</el-button>
          <el-button
            v-hasPermi="['dealer:aftersale:consult']"
            link
            type="primary"
            size="small"
            @click="handleConsult(row)"
          >💬 客服</el-button>
          <el-button link type="primary" size="small" @click="handleViewDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="flex justify-between items-center mt-10px">
      <span class="text-gray-500">共 {{ total }} 条售后单</span>
      <el-pagination
        v-model:current-page="queryParams.pageNo"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="sizes, prev, pager, next, jumper"
        @size-change="getList"
        @current-change="getList"
      />
    </div>
  </ContentWrap>

  <!-- 售后详情弹窗 -->
  <el-dialog v-model="detailVisible" title="售后详情" width="700px" destroy-on-close>
    <div v-if="detail">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="售后单号">{{ detail.aftersaleCode }}</el-descriptions-item>
        <el-descriptions-item label="关联订单">{{ detail.orderCode }}</el-descriptions-item>
        <el-descriptions-item label="经销商">{{ detail.dealerName }}</el-descriptions-item>
        <el-descriptions-item label="产品线">{{ detail.productLineName }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ detail.handlingMethodName }}-{{ detail.reasonName }}</el-descriptions-item>
        <el-descriptions-item label="产品">{{ detail.productName }} × {{ detail.quantity }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.refundAmount" label="退款金额">¥{{ detail.refundAmount }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.refundStatus !== 'none'" label="退款状态">{{ refundStatusMap[detail.refundStatus] }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.redInvoiceStatus !== 'none'" label="红字发票">{{ redInvoiceStatusMap[detail.redInvoiceStatus] }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.logisticsCompany" label="退回物流">{{ detail.logisticsCompany }} {{ detail.logisticsNo }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.exchangeLogisticsCompany" label="换货物流">{{ detail.exchangeLogisticsCompany }} {{ detail.exchangeLogisticsNo }}</el-descriptions-item>
      </el-descriptions>
      <div class="mt-20px">
        <h4>进度时间线</h4>
        <el-steps :active="detail.currentStep" finish-status="success">
          <el-step
            v-for="node in detail.progressNodes"
            :key="node.nodeCode"
            :title="node.nodeName"
            :description="formatNodeTime(node.nodeTime)"
          />
        </el-steps>
      </div>
      <div v-if="detail.remark" class="mt-10px text-gray-500">备注：{{ detail.remark }}</div>
    </div>
  </el-dialog>

  <!-- 更新进度弹窗 -->
  <el-dialog v-model="progressVisible" title="更新进度" width="500px" destroy-on-close>
    <div v-if="progressTarget">
      <el-form :model="progressForm" label-width="100px">
        <el-form-item label="当前步骤">
          <span>{{ progressTarget.currentStep + 1 }}/{{ 5 }} — {{ getNextNodeName(progressTarget) }}</span>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="progressForm.remark" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item v-if="isLogisticsNode(progressTarget)" label="物流公司">
          <el-input v-model="progressForm.logisticsCompany" />
        </el-form-item>
        <el-form-item v-if="isLogisticsNode(progressTarget)" label="物流单号">
          <el-input v-model="progressForm.logisticsNo" />
        </el-form-item>
        <el-form-item v-if="isRefundNode(progressTarget)" label="退款金额">
          <el-input-number v-model="progressForm.refundAmount" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item v-if="isInvoiceNode(progressTarget)" label="红字发票号">
          <el-input v-model="progressForm.redInvoiceNo" />
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button @click="progressVisible = false">取消</el-button>
      <el-button type="primary" @click="submitProgress">确认推进</el-button>
    </template>
  </el-dialog>

  <!-- 咨询聊天窗口 -->
  <ChatWindow v-model="chatVisible" :session-id="currentSessionId" mode="dealer" />
</template>

<script lang="ts" setup>
import * as AfterSaleApi from '@/api/opshub/aftersale'
import * as DealerApi from '@/api/opshub/dealer'
import ChatWindow from '@/components/CsChatWindow/ChatWindow.vue'
import { useCsConsult } from '@/hooks/useCsConsult'

defineOptions({ name: 'OpshubAfterSale' })

const message = useMessage()

// ========== 咨询集成 ==========
const { chatVisible, currentSessionId, openConsult, openBatchConsult } = useCsConsult()

// ========== 枚举映射 ==========
const handlingMethodMap: Record<string, string> = { return: '退货', exchange: '退换货', return_refund: '退货退款' }
const reasonMap: Record<string, string> = { complaint: '投诉', recall: '召回', damage: '破损' }
const refundStatusMap: Record<string, string> = { none: '无', pending: '退款中', refunded: '已退款' }
const redInvoiceStatusMap: Record<string, string> = { none: '无', pending: '待开', issued: '已开' }

const formatNodeTime = (time: string | number | null | undefined): string => {
  if (!time) return '未完成'
  const d = typeof time === 'number' ? new Date(time) : new Date(time)
  if (isNaN(d.getTime())) return String(time)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const getTypeTagType = (method: string): 'success' | 'warning' | 'danger' | 'info' | 'primary' => {
  if (method === 'return') return 'primary'
  if (method === 'exchange') return 'warning'
  if (method === 'return_refund') return 'danger'
  return 'info'
}

const getCurrentStepName = (row: AfterSaleApi.AfterSaleSimpleVO) => {
  const nodes = row.progressNodes
  if (!nodes || nodes.length === 0) return ''
  const current = nodes[row.currentStep - 1]
  return current ? current.nodeName : ''
}

const getNextNodeName = (row: AfterSaleApi.AfterSaleSimpleVO) => {
  const nodes = row.progressNodes
  if (!nodes || nodes.length === 0) return ''
  const next = nodes.find((_: any, i: number) => i === row.currentStep)
  return next ? next.nodeName : ''
}

const isLogisticsNode = (row: AfterSaleApi.AfterSaleSimpleVO) => {
  const nodes = row.progressNodes
  if (!nodes) return false
  const next = nodes[row.currentStep]
  return next && (next.nodeCode === 'returned' || next.nodeCode === 'exchange_sent')
}

const isRefundNode = (row: AfterSaleApi.AfterSaleSimpleVO) => {
  const nodes = row.progressNodes
  if (!nodes) return false
  const next = nodes[row.currentStep]
  return next && next.nodeCode === 'refund_done'
}

const isInvoiceNode = (row: AfterSaleApi.AfterSaleSimpleVO) => {
  const nodes = row.progressNodes
  if (!nodes) return false
  const next = nodes[row.currentStep]
  return next && next.nodeCode === 'red_invoice'
}

// ========== 角色判断 ==========
import { checkPermi } from '@/utils/permission'

const isAdminRole = computed(() => checkPermi(['dealer:aftersale:update-progress']))

const showCheckbox = computed(() =>
  checkPermi(['dealer:aftersale:consult']) || checkPermi(['dealer:aftersale:update-progress'])
)

// ========== 数据 ==========
const loading = ref(false)
const total = ref(0)
const list = ref<AfterSaleApi.AfterSaleSimpleVO[]>([])
const selectedRows = ref<AfterSaleApi.AfterSaleSimpleVO[]>([])
const dealerList = ref<any[]>([])
const productLineList = ref<any[]>([])

const stats = reactive<AfterSaleApi.AfterSaleStatisticsVO>({
  totalCount: 0, refundCount: 0, returnCount: 0,
  completedCount: 0, completedRate: 0, inProgressCount: 0
})

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  handlingMethod: undefined as string | undefined,
  reason: undefined as string | undefined,
  progressStatus: undefined as string | undefined,
  productLineCodes: undefined as string[] | undefined,
  dealerCodes: undefined as string[] | undefined,
  keyword: undefined as string | undefined,
  sortField: undefined as string | undefined,
  sortOrder: undefined as string | undefined
})

// ========== 列表 ==========
const getList = async () => {
  loading.value = true
  try {
    const data = await AfterSaleApi.getAfterSalePage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const data = await AfterSaleApi.getAfterSaleStatistics()
    Object.assign(stats, data)
  } catch (e) {
    console.error('加载统计失败', e)
  }
}

const handleSearch = () => {
  queryParams.pageNo = 1
  getList()
}

const handleReset = () => {
  queryParams.handlingMethod = undefined
  queryParams.reason = undefined
  queryParams.progressStatus = undefined
  queryParams.productLineCodes = undefined
  queryParams.dealerCodes = undefined
  queryParams.keyword = undefined
  queryParams.sortField = undefined
  queryParams.sortOrder = undefined
  queryParams.pageNo = 1
  getList()
}

const handleCardFilter = (type: string) => {
  queryParams.pageNo = 1
  if (type === 'all') {
    queryParams.handlingMethod = undefined
    queryParams.progressStatus = undefined
  } else if (type === 'refund') {
    queryParams.handlingMethod = 'return_refund'
    queryParams.progressStatus = undefined
  } else if (type === 'return') {
    queryParams.handlingMethod = undefined
    queryParams.progressStatus = undefined
  } else if (type === 'completed') {
    queryParams.handlingMethod = undefined
    queryParams.progressStatus = 'completed'
  } else if (type === 'incomplete') {
    queryParams.handlingMethod = undefined
    queryParams.progressStatus = undefined
    // 未完成需要特殊处理，前端不传 progressStatus，后端无法做 IN 查询
    // 简化：点击未完成时不做筛选，仅提示
    message.info('显示未完成售后单')
  }
  getList()
}

const handleSortChange = ({ prop, order }: any) => {
  queryParams.sortField = prop
  queryParams.sortOrder = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : undefined
  getList()
}

const handleSelectionChange = (rows: AfterSaleApi.AfterSaleSimpleVO[]) => {
  selectedRows.value = rows
}

// ========== 详情 ==========
const detailVisible = ref(false)
const detail = ref<AfterSaleApi.AfterSaleDetailVO | null>(null)
const handleViewDetail = async (row: AfterSaleApi.AfterSaleSimpleVO) => {
  try {
    detail.value = await AfterSaleApi.getAfterSaleDetail(row.id)
    detailVisible.value = true
  } catch (e: any) {
    message.error(e.message || '获取详情失败')
  }
}

// ========== 更新进度 ==========
const progressVisible = ref(false)
const progressTarget = ref<AfterSaleApi.AfterSaleSimpleVO | null>(null)
const progressForm = reactive({
  remark: '',
  logisticsCompany: '',
  logisticsNo: '',
  refundAmount: undefined as number | undefined,
  redInvoiceNo: ''
})

const handleUpdateProgress = async (row: AfterSaleApi.AfterSaleSimpleVO) => {
  // 获取最新详情以拿到进度节点
  try {
    const detailData = await AfterSaleApi.getAfterSaleDetail(row.id)
    // 构造一个带 progressNodes 的 VO 传给弹窗
    progressTarget.value = { ...row, progressNodes: detailData.progressNodes } as any
    // 重置表单
    progressForm.remark = ''
    progressForm.logisticsCompany = ''
    progressForm.logisticsNo = ''
    progressForm.refundAmount = undefined
    progressForm.redInvoiceNo = ''
    progressVisible.value = true
  } catch (e: any) {
    message.error(e.message || '获取详情失败')
  }
}

const submitProgress = async () => {
  if (!progressTarget.value) return
  try {
    await AfterSaleApi.updateAfterSaleProgress({
      aftersaleId: progressTarget.value.id,
      stepOrder: progressTarget.value.currentStep + 1,
      remark: progressForm.remark || undefined,
      logisticsCompany: progressForm.logisticsCompany || undefined,
      logisticsNo: progressForm.logisticsNo || undefined,
      refundAmount: progressForm.refundAmount,
      redInvoiceNo: progressForm.redInvoiceNo || undefined
    })
    message.success('进度更新成功')
    progressVisible.value = false
    await refreshAll()
  } catch (e: any) {
    message.error(e.message || '操作失败')
  }
}

// ========== 咨询 ==========
const handleConsult = (row: AfterSaleApi.AfterSaleSimpleVO) => {
  openConsult({
    consultType: 'aftersale',
    sourceModule: 'aftersale',
    context: `售后单 ${row.aftersaleCode} 咨询`,
    contextId: row.id,
    contextCode: row.aftersaleCode,
    productLineName: row.productLineName,
    dealerName: row.dealerName
  })
}

const handleBatchConsult = async () => {
  await openBatchConsult(selectedRows.value, (row) => ({
    consultType: 'aftersale',
    sourceModule: 'aftersale',
    context: `售后单 ${row.aftersaleCode} 咨询`,
    contextId: row.id,
    contextCode: row.aftersaleCode,
    productLineName: row.productLineName,
    dealerName: row.dealerName
  }))
}

// ========== 刷新 ==========
const refreshAll = async () => {
  await getList()
  await loadStatistics()
}

// ========== 初始化 ==========
onMounted(async () => {
  await getList()
  await loadStatistics()
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

<style scoped>
.stat-card {
  flex: 1;
  cursor: pointer;
  text-align: center;
  transition: all 0.3s;
}
.stat-card:hover {
  transform: translateY(-2px);
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}
.stat-rate {
  font-size: 14px;
  color: #909399;
  font-weight: normal;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}
</style>
