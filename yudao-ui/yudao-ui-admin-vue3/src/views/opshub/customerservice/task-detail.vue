<template>
  <!-- 页面头部 -->
  <ContentWrap>
    <div class="page-header">
      <div class="page-header-left">
        <el-button @click="router.back()">
          <Icon icon="ep:arrow-left" class="mr-4px" />返回
        </el-button>
        <span class="page-title">工单详情</span>
      </div>
      <div class="page-header-right">
        <el-tag v-if="detailData.status !== undefined" :type="getStatusTagType(detailData.status)" size="large">
          {{ getStatusLabel(detailData.status) }}
        </el-tag>
        <el-tag v-if="detailData.urgency !== undefined" :type="getUrgencyTagType(detailData.urgency)" size="large">
          紧急度: {{ getUrgencyLabel(detailData.urgency) }}
        </el-tag>
      </div>
    </div>
  </ContentWrap>

  <el-skeleton :loading="detailLoading" animated :rows="12">
    <template #default>
      <!-- 区域1：工单信息卡片 -->
      <ContentWrap>
        <div class="card-title">工单信息</div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="工单编号">
            <span class="font-mono font-medium">{{ detailData.taskNo }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="工单状态">
            <el-tag :type="getStatusTagType(detailData.status)">{{ getStatusLabel(detailData.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="紧急程度">
            <el-tag :type="getUrgencyTagType(detailData.urgency)">{{ getUrgencyLabel(detailData.urgency) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="工单分类">{{ getCategoryLabel(detailData.category) }}</el-descriptions-item>
          <el-descriptions-item label="经销商">{{ detailData.dealerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="产品线">{{ detailData.productLineName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="来源模块">{{ getSourceModuleLabel(detailData.sourceModule) }}</el-descriptions-item>
          <el-descriptions-item label="SLA 截止时间">
            <span :class="{ 'text-red-500': detailData.slaDeadline }">{{ formatNullableDate(detailData.slaDeadline) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="创建人">{{ detailData.creatorUserName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="执行员">{{ detailData.assigneeName || '未分配' }}</el-descriptions-item>
          <el-descriptions-item label="接单时间">{{ formatNullableDate(detailData.acceptTime) }}</el-descriptions-item>
          <el-descriptions-item label="交付时间">{{ formatNullableDate(detailData.deliverTime) }}</el-descriptions-item>
          <el-descriptions-item label="任务内容" :span="2">{{ detailData.content }}</el-descriptions-item>
          <el-descriptions-item label="特殊备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
        <!-- 退回原因警示条 -->
        <el-alert
          v-if="detailData.status === 4 && detailData.rejectReason"
          type="error"
          :closable="false"
          show-icon
          class="mt-12px"
        >
          <template #title>退回原因</template>
          <template #default>{{ detailData.rejectReason }}</template>
        </el-alert>
      </ContentWrap>

      <!-- 区域2：审批记录时间线 -->
      <ContentWrap>
        <div class="card-title flex items-center justify-between">
          <span>审批记录</span>
          <span v-if="detailData.processInstanceId" class="text-12px text-gray-400">
            流程实例: {{ detailData.processInstanceId }}
          </span>
        </div>
        <el-timeline v-if="activityNodes.length > 0">
          <el-timeline-item
            v-for="(node, index) in activityNodes"
            :key="index"
            :color="getNodeColor(node.status)"
            :timestamp="getNodeTime(node)"
            placement="top"
          >
            <div class="timeline-node">
              <div class="timeline-node-header">
                <span class="font-semibold text-14px">{{ node.name }}</span>
                <el-tag :type="getNodeStatusTagType(node.status)" size="small">
                  {{ getNodeStatusLabel(node.status) }}
                </el-tag>
              </div>
              <div v-for="(task, tIdx) in node.tasks" :key="tIdx" class="timeline-task">
                <div class="timeline-task-user">
                  <el-avatar :size="24" class="bg-blue-400 text-white">
                    {{ task.assigneeUser?.nickname?.substring(0, 1) || '?' }}
                  </el-avatar>
                  <span class="text-13px">{{ task.assigneeUser?.nickname || '未分配' }}</span>
                </div>
                <div v-if="task.reason" class="timeline-task-reason">
                  审批意见：{{ task.reason }}
                </div>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无审批记录" />
      </ContentWrap>

      <!-- 区域3：执行操作区（仅当前用户是执行人员时显示） -->
      <ContentWrap v-if="showExecutorOps">
        <div class="card-title flex items-center justify-between">
          <span>执行操作区</span>
          <el-tag type="success" size="small">当前用户为执行人员</el-tag>
        </div>

        <!-- 凭证附件 -->
        <div class="mb-16px">
          <div class="text-13px font-semibold text-gray-600 mb-8px">上传凭证附件</div>
          <el-upload
            :action="uploadUrl"
            :http-request="handleUpload"
            :show-file-list="false"
            :before-upload="beforeUpload"
          >
            <el-button type="primary" plain>
              <Icon icon="ep:upload-filled" class="mr-4px" />上传文件
            </el-button>
          </el-upload>
          <div v-if="attachments.length > 0" class="mt-12px">
            <div class="text-13px font-semibold text-gray-600 mb-6px">
              已上传附件（{{ attachments.length }}）
            </div>
            <div class="attachment-list">
              <div v-for="att in attachments" :key="att.id" class="attachment-item">
                <Icon icon="ep:document" class="text-blue-400 mr-6px" />
                <span class="flex-1 text-13px truncate">{{ att.fileName }}</span>
                <span class="text-12px text-gray-400 mr-12px">{{ formatFileSize(att.fileSize) }}</span>
                <span class="text-12px text-gray-400 mr-12px">{{ formatNullableDate(att.createTime) }}</span>
                <el-link :href="att.fileUrl" target="_blank" type="primary" :underline="false" class="mr-8px">预览</el-link>
                <el-button link type="danger" @click="handleDeleteAttachment(att.id)">删除</el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 操作按钮栏 -->
        <div class="action-bar">
          <el-button v-if="canAccept" type="success" @click="handleAccept">接单</el-button>
          <el-button v-if="canSubmitApproval" type="primary" @click="approvalDialogVisible = true">提交审批</el-button>
          <el-button v-if="canTransfer" type="warning" plain @click="transferDialogVisible = true">转单</el-button>
          <el-button v-if="canVerify" type="success" @click="handleVerify">验收通过</el-button>
          <el-button v-if="canVerify" type="danger" plain @click="rejectDialogVisible = true">退回</el-button>
          <el-button v-if="canReprocess" type="primary" plain @click="handleReprocess">重新处理</el-button>
          <el-button v-if="canUrge" @click="handleUrge">催办</el-button>
          <el-button v-if="canCancel" type="danger" @click="handleCancel">取消</el-button>
        </div>
      </ContentWrap>
    </template>
  </el-skeleton>

  <!-- 审批意见弹窗 -->
  <el-dialog v-model="approvalDialogVisible" title="提交审批" width="500px">
    <el-form :model="approvalForm" label-width="80px">
      <el-form-item label="审批意见" required>
        <el-input v-model="approvalForm.reason" type="textarea" :rows="4" placeholder="请输入审批意见（必填）" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="approvalDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="approvalLoading" @click="submitApproval">确认提交</el-button>
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
</template>

<script lang="ts" setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as CsTaskApi from '@/api/opshub/csTask'
import * as TaskApi from '@/api/bpm/task'
import * as CsAttachmentApi from '@/api/opshub/csAttachment'
import * as ProcessInstanceApi from '@/api/bpm/processInstance'
import { formatNullableDate } from '@/utils/formatTime'
import { useUserStore } from '@/store/modules/user'
import { getSimpleUserList } from '@/api/system/user'
import { useUpload, getUploadUrl } from '@/components/UploadFile/src/useUpload'

defineOptions({ name: 'OpsHubTaskDetail' })

const props = defineProps<{ id: string }>()
const router = useRouter()
const userStore = useUserStore()
const currentUserId = computed(() => userStore.getUser?.id)

const taskId = computed(() => Number(props.id))

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

// ========== BPM 节点状态映射 ==========
// status: -1=未开始, 1=审批中/运行中, 2=审批通过, 3=审批不通过, 4=已取消, 5=已退回
const getNodeColor = (status: number) => {
  const map: Record<number, string> = { [-1]: '#C0C4CC', 1: '#409EFF', 2: '#67C23A', 3: '#F56C6C', 4: '#C0C4CC', 5: '#F56C6C' }
  return map[status] || '#C0C4CC'
}
const getNodeStatusTagType = (status: number): string => {
  const map: Record<number, string> = { [-1]: 'info', 1: 'primary', 2: 'success', 3: 'danger', 4: 'info', 5: 'danger' }
  return map[status] || 'info'
}
const getNodeStatusLabel = (status: number): string => {
  const map: Record<number, string> = { [-1]: '未开始', 1: '进行中', 2: '已完成', 3: '不通过', 4: '已取消', 5: '已退回' }
  return map[status] || '未知'
}
const getNodeTime = (node: any): string => {
  const time = node.endTime || node.startTime
  if (!time) return '进行中...'
  if (typeof time === 'string') return time
  if (time instanceof Date) return time.toLocaleString()
  return String(time)
}

// ========== 数据加载 ==========
const detailLoading = ref(false)
const detailData = ref<any>({})
const activityNodes = ref<any[]>([])

const getInfo = async () => {
  detailLoading.value = true
  try {
    detailData.value = await CsTaskApi.getCsTask(taskId.value)
    await Promise.all([loadBpmTimeline(), loadAttachments()])
  } finally {
    detailLoading.value = false
  }
}

const loadBpmTimeline = async () => {
  const processInstanceId = detailData.value?.processInstanceId
  if (!processInstanceId) { activityNodes.value = []; return }
  try {
    const data = await ProcessInstanceApi.getApprovalDetail({ processInstanceId })
    activityNodes.value = data?.activityNodes || []
  } catch (e) {
    console.error('加载 BPM 审批时间线失败', e)
    activityNodes.value = []
  }
}

// ========== 权限判断 ==========
const showExecutorOps = computed(() => {
  if (!detailData.value) return false
  return detailData.value.assigneeId === currentUserId.value
})

const canAccept = computed(() => detailData.value.status === 0 && !detailData.value.assigneeId)
const canSubmitApproval = computed(() => detailData.value.status === 1 && detailData.value.assigneeId === currentUserId.value)
const canTransfer = computed(() => detailData.value.status === 1 && detailData.value.assigneeId === currentUserId.value)
const canVerify = computed(() => detailData.value.status === 2 && detailData.value.creatorUserId === currentUserId.value)
const canReprocess = computed(() => detailData.value.status === 4 && detailData.value.assigneeId === currentUserId.value)
const canUrge = computed(() => detailData.value.status !== undefined && detailData.value.status !== 3)
const canCancel = computed(() => {
  const s = detailData.value.status
  if (s === undefined || s === 3) return false
  return s === 0 || s === 1
})

// ========== 附件管理 ==========
const uploadUrl = getUploadUrl()
const { httpRequest } = useUpload()
const attachments = ref<CsAttachmentApi.CsAttachmentVO[]>([])

const beforeUpload = (file: any) => {
  const isLt20M = file.size / 1024 / 1024 < 20
  if (!isLt20M) { ElMessage.warning('文件大小不能超过 20MB'); return false }
  return true
}

const handleUpload = async (options: any) => {
  try {
    const res = await httpRequest(options) as any
    const fileUrl = res?.data || res
    await CsAttachmentApi.uploadAttachment({
      module: 'task',
      businessId: taskId.value,
      fileUrl: typeof fileUrl === 'string' ? fileUrl : String(fileUrl),
      fileName: options.file.name,
      fileSize: options.file.size,
      fileType: options.file.type
    })
    ElMessage.success('文件上传成功')
    await loadAttachments()
  } catch (e) {
    console.error('上传附件失败', e)
    ElMessage.error('上传失败，请重试')
  }
}

const loadAttachments = async () => {
  try {
    attachments.value = await CsAttachmentApi.getAttachmentList('task', taskId.value)
  } catch (e) { attachments.value = [] }
}

const handleDeleteAttachment = async (id: number) => {
  await ElMessageBox.confirm('确认删除该附件？', '提示', { type: 'warning' })
  await CsAttachmentApi.deleteAttachment(id)
  ElMessage.success('已删除')
  await loadAttachments()
}

const formatFileSize = (bytes: number) => {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1048576).toFixed(1) + ' MB'
}

// ========== 接单 ==========
const handleAccept = async () => {
  await ElMessageBox.confirm('确认接单？', '提示', { type: 'info' })
  await CsTaskApi.acceptTask(taskId.value)
  ElMessage.success('接单成功')
  await getInfo()
}

// ========== 提交审批 ==========
const approvalDialogVisible = ref(false)
const approvalLoading = ref(false)
const approvalForm = reactive({ reason: '' })

const submitApproval = async () => {
  if (!approvalForm.reason?.trim()) { ElMessage.warning('请输入审批意见'); return }
  approvalLoading.value = true
  try {
    await CsTaskApi.submitForApproval(taskId.value, approvalForm.reason)
    ElMessage.success('已提交审批')
    approvalDialogVisible.value = false
    approvalForm.reason = ''
    await getInfo()
  } finally { approvalLoading.value = false }
}

// ========== 转单 ==========
const transferDialogVisible = ref(false)
const transferLoading = ref(false)
const candidateLoading = ref(false)
const candidateUserList = ref<any[]>([])
const transferForm = reactive({ newAssigneeId: undefined as number | undefined, reason: '' })
const userList = ref<any[]>([])

const loadCandidateUsers = async () => {
  candidateLoading.value = true
  try {
    const userIds: number[] = await CsTaskApi.getTaskCandidateUsers(taskId.value)
    if (!userIds || userIds.length === 0) { candidateUserList.value = []; return }
    if (userList.value.length === 0) { userList.value = await getSimpleUserList() }
    const idSet = new Set(userIds)
    candidateUserList.value = userList.value.filter((u: any) => idSet.has(u.id))
    if (candidateUserList.value.length === 0) {
      candidateUserList.value = userIds.map((id: number) => ({ id, nickname: `用户${id}` }))
    }
  } catch (e) { candidateUserList.value = [] } finally { candidateLoading.value = false }
}

const submitTransfer = async () => {
  if (!transferForm.newAssigneeId) { ElMessage.warning('请选择新处理人'); return }
  transferLoading.value = true
  try {
    // 1. 解析 BPM 任务 ID
    const bpmTaskId = await CsTaskApi.getBpmTaskId(taskId.value)
    if (!bpmTaskId) {
      ElMessage.warning('当前工单状态不支持转单'); transferLoading.value = false; return
    }
    // 2. 调用 BPM 转派接口
    await TaskApi.transferTask({ id: bpmTaskId, assigneeUserId: transferForm.newAssigneeId, reason: transferForm.reason })
    ElMessage.success('转单成功')
    transferDialogVisible.value = false
    transferForm.newAssigneeId = undefined; transferForm.reason = ''
    await getInfo()
  } finally { transferLoading.value = false }
}

// ========== 验收 ==========
const handleVerify = async () => {
  await ElMessageBox.confirm('确认验收通过？', '提示', { type: 'success' })
  await CsTaskApi.verifyTask({ id: taskId.value, passed: true })
  ElMessage.success('验收通过')
  await getInfo()
}

// ========== 退回 ==========
const rejectDialogVisible = ref(false)
const rejectLoading = ref(false)
const rejectForm = reactive({ rejectReason: '' })

const submitReject = async () => {
  if (!rejectForm.rejectReason?.trim()) { ElMessage.warning('请填写退回原因'); return }
  rejectLoading.value = true
  try {
    await CsTaskApi.verifyTask({ id: taskId.value, passed: false, rejectReason: rejectForm.rejectReason })
    ElMessage.success('已退回')
    rejectDialogVisible.value = false
    rejectForm.rejectReason = ''
    await getInfo()
  } finally { rejectLoading.value = false }
}

// ========== 重新处理 ==========
const handleReprocess = async () => {
  await ElMessageBox.confirm('确认重新处理此工单？', '提示', { type: 'info' })
  await CsTaskApi.reprocessTask(taskId.value)
  ElMessage.success('已重新处理')
  await getInfo()
}

// ========== 催办 ==========
const handleUrge = async () => {
  await CsTaskApi.urgeTask(taskId.value)
  ElMessage.success('催办成功')
}

// ========== 取消 ==========
const handleCancel = async () => {
  const { value: reason } = await ElMessageBox.prompt(
    '取消后工单将被关闭。请输入取消原因（可选）', '取消工单',
    { confirmButtonText: '确认取消', cancelButtonText: '返回', inputType: 'textarea' }
  ).catch(() => { throw new Error('cancel') })
  try {
    await CsTaskApi.cancelTask(taskId.value, reason || undefined)
    ElMessage.success('工单已关闭')
    await getInfo()
  } catch (e) { /* 已处理 */ }
}

// ========== 监听转单弹窗打开 ==========
watch(transferDialogVisible, (val) => { if (val) loadCandidateUsers() })

onMounted(() => { getInfo() })
</script>

<style lang="scss" scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .page-header-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .page-header-right {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .page-title {
    font-size: 20px;
    font-weight: 700;
    color: #303133;
  }
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 14px;
  color: #303133;
}

.timeline-node {
  .timeline-node-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 6px;
  }

  .timeline-task {
    margin-top: 6px;

    .timeline-task-user {
      display: flex;
      align-items: center;
      gap: 6px;
    }

    .timeline-task-reason {
      margin-top: 6px;
      padding: 8px 12px;
      background: #f8f8fa;
      border-radius: 6px;
      font-size: 13px;
      color: #606266;
    }
  }
}

.attachment-list {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  overflow: hidden;

  .attachment-item {
    display: flex;
    align-items: center;
    padding: 10px 14px;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }
  }
}

.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}
</style>
