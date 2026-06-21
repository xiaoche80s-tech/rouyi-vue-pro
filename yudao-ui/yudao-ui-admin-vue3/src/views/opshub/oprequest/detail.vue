<template>
  <!-- 页面头部 -->
  <ContentWrap>
    <div class="page-header">
      <div class="page-header-left">
        <el-button @click="router.back()">
          <Icon icon="ep:arrow-left" class="mr-4px" />返回
        </el-button>
        <span class="page-title">操作请求详情</span>
      </div>
      <div class="page-header-right">
        <el-tag v-if="detailData.requestTypeName" :type="getTypeTagType(detailData.requestType)" size="large">
          {{ detailData.requestTypeName }}
        </el-tag>
        <el-tag v-if="detailData.requestStatus" :type="getStatusTagType(detailData.requestStatus)" size="large">
          {{ getStatusLabel(detailData.requestStatus) }}
        </el-tag>
      </div>
    </div>
  </ContentWrap>

  <el-skeleton :loading="detailLoading" animated :rows="8">
    <template #default>
      <!-- 区域1：请求信息 -->
      <ContentWrap>
        <div class="card-title">请求信息</div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="请求编号">
            <span class="font-mono font-medium">{{ detailData.requestNo }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="请求类型">
            <el-tag :type="getTypeTagType(detailData.requestType)" size="small">{{ detailData.requestTypeName }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="请求状态">
            <el-tag :type="getStatusTagType(detailData.requestStatus)">{{ getStatusLabel(detailData.requestStatus) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="经销商编码">{{ detailData.dealerCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatNullableDate(detailData.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ detailData.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </ContentWrap>

      <!-- 区域2：合同信息（仅签约类型） -->
      <ContentWrap v-if="detailData.requestType === 'signing' && detailData.contractId">
        <div class="card-title">合同信息</div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="合同编码">
            <span class="font-mono">{{ detailData.contractCode }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="合同名称">{{ detailData.contractName || '-' }}</el-descriptions-item>
        </el-descriptions>
      </ContentWrap>

      <!-- 区域3：附件列表 -->
      <ContentWrap v-if="attachments.length > 0">
        <div class="card-title">附件（{{ attachments.length }}）</div>
        <div class="attachment-list">
          <div v-for="att in attachments" :key="att.id" class="attachment-item">
            <Icon icon="ep:document" class="text-blue-400 mr-6px" />
            <span class="flex-1 text-13px truncate">{{ att.fileName }}</span>
            <span class="text-12px text-gray-400 mr-12px">{{ formatFileSize(att.fileSize) }}</span>
            <span class="text-12px text-gray-400 mr-12px">{{ formatNullableDate(att.createTime) }}</span>
            <el-link :href="att.fileUrl" target="_blank" type="primary" :underline="false">预览</el-link>
          </div>
        </div>
      </ContentWrap>

      <!-- 区域4：审批记录 -->
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
    </template>
  </el-skeleton>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as OpRequestApi from '@/api/opshub/oprequest'
import * as CsAttachmentApi from '@/api/opshub/csAttachment'
import * as ProcessInstanceApi from '@/api/bpm/processInstance'
import { formatNullableDate } from '@/utils/formatTime'

defineOptions({ name: 'OpsHubOpRequestDetail' })

const props = defineProps<{ id: string }>()
const router = useRouter()

const requestId = computed(() => Number(props.id))

// ========== 状态枚举 ==========
const statusOptions: Record<string, { label: string; type: string }> = {
  WAITING: { label: '待处理', type: 'warning' },
  IN_PROGRESS: { label: '处理中', type: 'primary' },
  DELIVERED: { label: '已交付', type: 'success' },
  CLOSED: { label: '已关闭', type: 'info' },
  REJECTED: { label: '已退回', type: 'danger' },
  CANCEL: { label: '已取消', type: 'info' }
}
const typeTagMap: Record<string, string> = {
  signing: 'success', payment: 'warning', invoice: 'primary', return: 'danger'
}

const getStatusLabel = (val: string) => statusOptions[val]?.label || val || ''
const getStatusTagType = (val: string) => statusOptions[val]?.type || ''
const getTypeTagType = (val: string) => typeTagMap[val] || ''

// ========== BPM 节点状态 ==========
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
  return String(time)
}

// ========== 附件工具 ==========
const formatFileSize = (bytes: number) => {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

// ========== 数据加载 ==========
const detailLoading = ref(false)
const detailData = ref<any>({})
const activityNodes = ref<any[]>([])
const attachments = ref<any[]>([])

const getInfo = async () => {
  detailLoading.value = true
  try {
    detailData.value = await OpRequestApi.getOpRequest(requestId.value)
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

const loadAttachments = async () => {
  const id = detailData.value?.id
  if (!id) { attachments.value = []; return }
  try {
    const data = await CsAttachmentApi.getAttachmentList('op-request', id)
    attachments.value = data || []
  } catch (e) {
    console.error('加载附件失败', e)
    attachments.value = []
  }
}

onMounted(() => { if (requestId.value) getInfo() })
watch(requestId, (val) => { if (val) getInfo() })
</script>

<style lang="scss" scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.page-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.page-title {
  font-size: 16px;
  font-weight: 600;
}
.card-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #303133;
}
.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.attachment-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 6px;
}
.timeline-node-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.timeline-task {
  margin-top: 6px;
}
.timeline-task-user {
  display: flex;
  align-items: center;
  gap: 8px;
}
.timeline-task-reason {
  margin-top: 4px;
  margin-left: 32px;
  color: #606266;
  font-size: 12px;
}
</style>
