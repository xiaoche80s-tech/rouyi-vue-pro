<template>
  <!-- 角色感知子标签 -->
  <ContentWrap v-if="showSubTabs" class="!pb-0">
    <el-tabs v-model="activeSubTab" @tab-change="handleSubTabChange">
      <el-tab-pane
        v-for="tab in subTabs"
        :key="tab.value"
        :name="tab.value"
      >
        <template #label>
          {{ tab.label }}
          <el-badge v-if="tabCounts[tab.value] !== undefined && tabCounts[tab.value] > 0"
            :value="tabCounts[tab.value]" :max="999" class="ml-4px" />
        </template>
      </el-tab-pane>
    </el-tabs>
  </ContentWrap>

  <!-- 筛选栏 -->
  <ContentWrap>
    <el-form :model="queryParams" inline>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" clearable placeholder="全部" style="width: 120px">
          <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="紧急程度">
        <el-select v-model="queryParams.urgency" clearable placeholder="全部" style="width: 120px">
          <el-option v-for="u in urgencyOptions" :key="u.value" :label="u.label" :value="u.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="分类">
        <el-select v-model="queryParams.category" clearable placeholder="全部" style="width: 120px">
          <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="来源模块">
        <el-select v-model="queryParams.sourceModule" clearable placeholder="全部" style="width: 120px">
          <el-option v-for="m in sourceModuleOptions" :key="m.value" :label="m.label" :value="m.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-input v-model="queryParams.keyword" clearable placeholder="搜索工单号/内容" style="width: 200px" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch"><Icon icon="ep:search" class="mr-4px" />搜索</el-button>
        <el-button @click="handleReset"><Icon icon="ep:refresh" class="mr-4px" />重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 操作栏 + 表格 -->
  <ContentWrap>
    <div class="mb-10px flex items-center gap-10px">
      <el-button v-if="side !== 'handler'" v-hasPermi="['dealer:cs-task:create']" type="primary" plain @click="handleCreate">
        <Icon class="mr-5px" icon="ep:plus" />提工单
      </el-button>
    </div>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column label="工单编号" prop="taskNo" min-width="180">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleDetail(row)">{{ row.taskNo }}</el-button>
        </template>
      </el-table-column>
      <el-table-column label="内容" prop="content" min-width="200" show-overflow-tooltip />
      <el-table-column label="分类" prop="category" min-width="80" align="center">
        <template #default="{ row }">
          <el-tag size="small">{{ getCategoryLabel(row.category) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="紧急程度" prop="urgency" min-width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="getUrgencyTagType(row.urgency)" size="small">{{ getUrgencyLabel(row.urgency) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" min-width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="来源" prop="sourceModule" min-width="80" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.sourceModule" size="small" type="info">{{ getSourceModuleLabel(row.sourceModule) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="处理人" prop="assigneeName" min-width="100" />
      <el-table-column label="经销商" prop="dealerName" min-width="120" show-overflow-tooltip />
      <el-table-column label="产品线" prop="productLineName" min-width="100" />
      <el-table-column label="SLA截止" prop="slaDeadline" min-width="160" />
      <el-table-column label="创建时间" prop="createTime" min-width="160" />
      <el-table-column label="操作" fixed="right" min-width="280" align="center">
        <template #default="{ row }">
          <el-button v-if="canAccept(row)" v-hasPermi="['dealer:cs-task:accept']" link type="primary" @click="handleAccept(row)">接单</el-button>
          <template v-if="row.status === 1">
            <el-button v-if="canSubmitApproval(row)" v-hasPermi="['dealer:cs-task:deliver']" link type="success" @click="handleSubmitApproval(row)">提交审批</el-button>
            <el-button v-if="canTransfer(row)" v-hasPermi="['dealer:cs-task:transfer']" link type="warning" @click="handleTransfer(row)">转单</el-button>
          </template>
          <template v-if="row.status === 2">
            <el-button v-if="canVerify(row)" v-hasPermi="['dealer:cs-task:verify']" link type="success" @click="handleVerify(row)">验收通过</el-button>
            <el-button v-if="canVerify(row)" v-hasPermi="['dealer:cs-task:verify']" link type="danger" @click="handleRejectDialog(row)">退回</el-button>
          </template>
          <el-button v-if="canReprocess(row)" v-hasPermi="['dealer:cs-task:reprocess']" link type="primary" @click="handleReprocess(row)">重新处理</el-button>
          <el-button v-if="canCancel(row)" v-hasPermi="['dealer:cs-task:cancel']" link type="danger" @click="handleCancel(row)">取消</el-button>
          <el-button v-if="canUrge(row)" v-hasPermi="['dealer:cs-task:urge']" link type="info" @click="handleUrge(row)">催办</el-button>
        </template>
      </el-table-column>
    </el-table>

    <Pagination v-model:limit="queryParams.pageSize" v-model:page="queryParams.pageNo" :total="total" @pagination="getList" />
  </ContentWrap>

  <!-- 创建工单弹窗 -->
  <el-dialog v-model="createDialogVisible" title="提工单" width="600px">
    <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
      <el-form-item label="工单内容" prop="content">
        <el-input v-model="createForm.content" type="textarea" :rows="4" placeholder="请描述工单内容" />
      </el-form-item>
      <el-form-item label="紧急程度" prop="urgency">
        <el-select v-model="createForm.urgency" placeholder="请选择" style="width: 100%">
          <el-option v-for="u in urgencyOptions" :key="u.value" :label="u.label" :value="u.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="分类" prop="category">
        <el-select v-model="createForm.category" placeholder="请选择" style="width: 100%">
          <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="处理人">
        <el-select v-model="createForm.assigneeId" filterable clearable placeholder="可选，不选则进入抢单模式" style="width: 100%">
          <el-option v-for="u in userList" :key="u.id" :label="u.nickname" :value="u.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="SLA截止" prop="slaDeadline">
        <el-date-picker v-model="createForm.slaDeadline" type="datetime" placeholder="选择截止时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
      </el-form-item>
      <el-form-item label="经销商编码">
        <el-input v-model="createForm.dealerCode" placeholder="可选" />
      </el-form-item>
      <el-form-item label="产品线编码">
        <el-input v-model="createForm.productLineCode" placeholder="可选" />
      </el-form-item>
      <el-form-item label="来源模块">
        <el-select v-model="createForm.sourceModule" clearable placeholder="可选" style="width: 100%">
          <el-option v-for="m in sourceModuleOptions" :key="m.value" :label="m.label" :value="m.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="createForm.remark" type="textarea" :rows="2" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="createDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="createLoading" @click="submitCreate">提交</el-button>
    </template>
  </el-dialog>

  <!-- 转单弹窗 -->
  <el-dialog v-model="transferDialogVisible" title="转单" width="400px">
    <el-form :model="transferForm" label-width="80px">
      <el-form-item label="新处理人">
        <el-select v-model="transferForm.newAssigneeId" filterable placeholder="请选择" style="width: 100%" :loading="candidateLoading">
          <el-option v-for="u in candidateUserList" :key="u.id" :label="u.nickname" :value="u.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="转单原因">
        <el-input v-model="transferForm.reason" type="textarea" :rows="2" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="transferDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="transferLoading" @click="submitTransfer">确认</el-button>
    </template>
  </el-dialog>

  <!-- 退回弹窗 -->
  <el-dialog v-model="rejectDialogVisible" title="验收退回" width="400px">
    <el-form :model="rejectForm" label-width="80px">
      <el-form-item label="退回原因">
        <el-input v-model="rejectForm.rejectReason" type="textarea" :rows="3" placeholder="请说明退回原因" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="rejectDialogVisible = false">取消</el-button>
      <el-button type="danger" :loading="rejectLoading" @click="submitReject">确认退回</el-button>
    </template>
  </el-dialog>

  <!-- 工单详情弹窗 -->
  <el-dialog v-model="detailDialogVisible" title="工单详情" width="900px" destroy-on-close>
    <TaskDetail v-if="detailDialogVisible" :id="detailTaskId" />
  </el-dialog>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import * as CsTaskApi from '@/api/opshub/csTask'
import { getSimpleUserList } from '@/api/system/user'
import { useUserStore } from '@/store/modules/user'
import { useCsWebSocket, type CsTaskEventPayload } from '@/hooks/useCsWebSocket'
import TaskDetail from '../task-detail.vue'

// ========== 枚举常量 ==========
const statusOptions = [
  { value: 0, label: '待接单' }, { value: 1, label: '处理中' },
  { value: 2, label: '已交付' }, { value: 3, label: '已关闭' }, { value: 4, label: '已退回' }
]
const urgencyOptions = [
  { value: 0, label: '紧急' }, { value: 1, label: '高' }, { value: 2, label: '中' }, { value: 3, label: '低' }
]
const categoryOptions = [
  { value: 0, label: '签约' }, { value: 1, label: '政策' }, { value: 2, label: '售后' },
  { value: 3, label: '订单' }, { value: 4, label: '数据' }, { value: 5, label: '其他' }
]
const sourceModuleOptions = [
  { value: 'aftersale', label: '售后' }, { value: 'order', label: '订单' },
  { value: 'signing', label: '签约进度' }, { value: 'basedata', label: '基础数据' }, { value: 'manual', label: '手动创建' }
]

const getStatusLabel = (val: number) => statusOptions.find(s => s.value === val)?.label || ''
const getStatusTagType = (val: number) => (['warning', 'primary', 'success', 'info', 'danger'] as const)[val] || ''
const getUrgencyLabel = (val: number) => urgencyOptions.find(u => u.value === val)?.label || ''
const getUrgencyTagType = (val: number) => (['danger', 'warning', '', 'info'] as any)[val] || ''
const getCategoryLabel = (val: number) => categoryOptions.find(c => c.value === val)?.label || ''
const getSourceModuleLabel = (val: string) => sourceModuleOptions.find(m => m.value === val)?.label || val || ''

// ========== Props ==========
const props = defineProps<{
  side: 'initiator' | 'handler' | 'admin'
}>()

// ========== 当前用户 ==========
const userStore = useUserStore()
const currentUserId = computed(() => userStore.getUser?.id)

// ========== 子标签配置（基于 side prop） ==========
const subTabsMap: Record<string, Array<{ value: string; label: string }>> = {
  initiator: [
    { value: 'all', label: '全部' },
    { value: 'pending', label: '待办' }
  ],
  handler: [
    { value: 'claimable', label: '可领取' },
    { value: 'pending', label: '待办' },
    { value: 'done', label: '已办' }
  ],
  admin: []
}

const defaultTabFilter = computed(() =>
  props.side === 'initiator' ? 'all' : props.side === 'handler' ? 'claimable' : undefined
)

// ========== 列表查询 ==========
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1, pageSize: 20,
  status: undefined as number | undefined,
  urgency: undefined as number | undefined,
  category: undefined as number | undefined,
  sourceModule: undefined as string | undefined,
  keyword: '',
  tabFilter: defaultTabFilter.value as string | undefined
})

const getList = async () => {
  loading.value = true
  try {
    const data = await CsTaskApi.getCsTaskPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally { loading.value = false }
  // 同步刷新角标计数
  loadTabCounts()
}

const handleSearch = () => { queryParams.pageNo = 1; getList() }
const handleReset = () => {
  queryParams.status = undefined; queryParams.urgency = undefined
  queryParams.category = undefined; queryParams.sourceModule = undefined
  queryParams.keyword = ''; handleSearch()
}

// ========== 子标签 ========== 
const showSubTabs = computed(() => props.side !== 'admin')
const subTabs = computed(() => subTabsMap[props.side] || [])
const activeSubTab = ref(defaultTabFilter.value)
const tabCounts = ref<Record<string, number>>({})

const loadTabCounts = async () => {
  try {
    const counts = await CsTaskApi.getTabCounts()
    tabCounts.value = counts || {}
  } catch (e) { /* ignore */ }
}

const handleSubTabChange = (tab: string) => {
  queryParams.tabFilter = tab
  queryParams.pageNo = 1
  getList()
}

// 操作按钮可见性计算
const canAccept = (row: any) => row.status === 0 && props.side === 'handler' && (row.assigneeId === null || row.assigneeId === currentUserId.value)
const canSubmitApproval = (row: any) => row.status === 1 && row.assigneeId === currentUserId.value
const canTransfer = (row: any) => row.status === 1 && (props.side === 'admin' || row.assigneeId === currentUserId.value)
const canVerify = (row: any) => row.status === 2 && row.creatorUserId === currentUserId.value
const canReprocess = (row: any) => row.status === 4 && row.assigneeId === currentUserId.value
const canCancel = (row: any) => {
  if (row.status === 3) return false
  if (props.side === 'admin') return true
  return (row.status === 0 || row.status === 1) && row.creatorUserId === currentUserId.value
}
const canUrge = (row: any) => row.status !== 3

// ========== 用户列表 ==========
const userList = ref<any[]>([])
const loadUserList = async () => {
  try { userList.value = await getSimpleUserList() } catch (e) { /* ignore */ }
}

// ========== 创建工单 ==========
const createDialogVisible = ref(false)
const createLoading = ref(false)
const createFormRef = ref<FormInstance>()
const createForm = reactive({
  content: '', urgency: 2, category: 5, assigneeId: undefined as number | undefined,
  slaDeadline: '', dealerCode: '', productLineCode: '', sourceModule: '' as string, remark: ''
})
const createRules: FormRules = {
  content: [{ required: true, message: '请输入工单内容', trigger: 'blur' }],
  urgency: [{ required: true, message: '请选择紧急程度', trigger: 'change' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  slaDeadline: [{ required: true, message: '请选择SLA截止时间', trigger: 'change' }]
}

const handleCreate = () => {
  Object.assign(createForm, { content: '', urgency: 2, category: 5, assigneeId: undefined, slaDeadline: '', dealerCode: '', productLineCode: '', sourceModule: '', remark: '' })
  createDialogVisible.value = true
}

const submitCreate = async () => {
  await createFormRef.value?.validate()
  createLoading.value = true
  try {
    await CsTaskApi.createCsTask(createForm)
    ElMessage.success('工单创建成功'); createDialogVisible.value = false; getList()
  } finally { createLoading.value = false }
}

// ========== 接单 ==========
const handleAccept = async (row: any) => {
  await ElMessageBox.confirm('确认接单？', '提示', { type: 'info' })
  await CsTaskApi.acceptTask(row.id); ElMessage.success('接单成功'); getList()
}

// ========== 提交审批 ==========
const handleSubmitApproval = async (row: any) => {
  await ElMessageBox.confirm('确认提交审批？', '提示', { type: 'info' })
  await CsTaskApi.submitForApproval(row.id); ElMessage.success('已提交审批'); getList()
}

// ========== 转单 ==========
const transferDialogVisible = ref(false)
const transferLoading = ref(false)
const candidateLoading = ref(false)
const candidateUserList = ref<any[]>([])
const transferForm = reactive({ id: 0, newAssigneeId: undefined as number | undefined, reason: '' })

const loadCandidateUsers = async (taskId: number) => {
  candidateLoading.value = true
  try {
    const userIds: number[] = await CsTaskApi.getTaskCandidateUsers(taskId)
    if (!userIds || userIds.length === 0) {
      candidateUserList.value = []
      return
    }
    // 从已加载的 userList 中筛选候选人，避免额外请求
    const idSet = new Set(userIds)
    candidateUserList.value = userList.value.filter((u: any) => idSet.has(u.id))
    // 如果 userList 为空或没有匹配，用 userId 构造简单对象
    if (candidateUserList.value.length === 0) {
      candidateUserList.value = userIds.map((id: number) => ({ id, nickname: `用户${id}` }))
    }
  } catch (e) {
    console.warn('[loadCandidateUsers] 加载候选人失败:', e)
    candidateUserList.value = []
  } finally {
    candidateLoading.value = false
  }
}

const handleTransfer = async (row: any) => {
  transferForm.id = row.id; transferForm.newAssigneeId = undefined; transferForm.reason = ''
  transferDialogVisible.value = true
  await loadCandidateUsers(row.id)
}
const submitTransfer = async () => {
  if (!transferForm.newAssigneeId) { ElMessage.warning('请选择新处理人'); return }
  transferLoading.value = true
  try {
    await CsTaskApi.transferTask(transferForm); ElMessage.success('转单成功'); transferDialogVisible.value = false; getList()
  } finally { transferLoading.value = false }
}

// ========== 验收 ==========
const handleVerify = async (row: any) => {
  await ElMessageBox.confirm('确认验收通过？', '提示', { type: 'success' })
  await CsTaskApi.verifyTask({ id: row.id, passed: true }); ElMessage.success('验收通过'); getList()
}

// ========== 退回 ==========
const rejectDialogVisible = ref(false)
const rejectLoading = ref(false)
const rejectForm = reactive({ id: 0, rejectReason: '' })
const handleRejectDialog = (row: any) => { rejectForm.id = row.id; rejectForm.rejectReason = ''; rejectDialogVisible.value = true }
const submitReject = async () => {
  if (!rejectForm.rejectReason) { ElMessage.warning('请填写退回原因'); return }
  rejectLoading.value = true
  try {
    await CsTaskApi.verifyTask({ id: rejectForm.id, passed: false, rejectReason: rejectForm.rejectReason })
    ElMessage.success('已退回'); rejectDialogVisible.value = false; getList()
  } finally { rejectLoading.value = false }
}

// ========== 重新处理 ==========
const handleReprocess = async (row: any) => {
  await ElMessageBox.confirm('确认重新处理此工单？', '提示', { type: 'info' })
  await CsTaskApi.reprocessTask(row.id); ElMessage.success('已重新处理'); getList()
}

// ========== 催办 ==========
const handleUrge = async (row: any) => {
  await CsTaskApi.urgeTask(row.id); ElMessage.success('催办成功')
}

// ========== WebSocket 监听 ==========
useCsWebSocket(
  undefined, // onChatMessage
  undefined, // onSessionEvent
  undefined, // onNewConsult
  (event: CsTaskEventPayload) => {
    // 收到工单事件时刷新列表和计数
    console.log('[TaskTab] 收到工单事件:', event.type, event.taskId)
    getList()
    loadTabCounts()
  }
)

// ========== 取消/关闭 ==========
const handleCancel = async (row: any) => {
  const confirmMsg = row.status === 1
    ? '取消后流程将终止，工单将被关闭。请输入取消原因（可选）'
    : '取消后工单将被关闭。请输入取消原因（可选）'
  const { value: reason } = await ElMessageBox.prompt(confirmMsg, '取消工单', {
    confirmButtonText: '确认取消', cancelButtonText: '返回', inputType: 'textarea'
  }).catch(() => { throw new Error('cancel') })
  try {
    await CsTaskApi.cancelTask(row.id, reason || undefined)
    ElMessage.success('工单已关闭'); getList()
  } catch (e) { /* 已处理 */ }
}

// ========== 工单详情 ==========
const detailDialogVisible = ref(false)
const detailTaskId = ref<number>(0)
const handleDetail = (row: any) => {
  detailTaskId.value = row.id
  detailDialogVisible.value = true
}

// ========== 初始化 ==========
onMounted(() => { getList(); loadUserList(); loadTabCounts() })
</script>
