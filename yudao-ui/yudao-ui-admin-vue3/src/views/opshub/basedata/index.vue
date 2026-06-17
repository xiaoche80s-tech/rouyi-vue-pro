<template>
  <!-- 分类 Tab -->
  <ContentWrap>
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="资质文件" name="qualification" />
      <el-tab-pane label="授权文件" name="authorization" />
      <el-tab-pane label="合同文件" name="contract" />
      <el-tab-pane label="产品文件" name="product" />
    </el-tabs>
  </ContentWrap>

  <!-- 搜索栏 -->
  <ContentWrap>
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="-mb-15px"
      label-width="100px"
    >
      <el-form-item label="文件名称" prop="fileName">
        <el-input
          v-model="queryParams.fileName"
          class="!w-240px"
          clearable
          placeholder="请输入文件名称"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="经销商" prop="dealerId">
        <el-select
          v-model="queryParams.dealerId"
          class="!w-240px"
          clearable
          placeholder="请选择经销商"
        >
          <el-option
            v-for="item in dealerList"
            :key="item.id"
            :label="item.dealerName"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="有效期状态" prop="expireStatus">
        <el-select
          v-model="queryParams.expireStatus"
          class="!w-240px"
          clearable
          placeholder="请选择有效期状态"
        >
          <el-option label="有效" value="valid" />
          <el-option label="即将到期" value="expiring_soon" />
          <el-option label="已过期" value="expired" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon class="mr-5px" icon="ep:search" />
          搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon class="mr-5px" icon="ep:refresh" />
          重置
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 操作栏 -->
  <ContentWrap>
    <el-button
      v-hasPermi="['dealer:basedata:download']"
      :disabled="selectedIds.length === 0"
      plain
      type="primary"
      @click="handleBatchDownload"
    >
      <Icon class="mr-5px" icon="ep:download" />
      批量下载
    </el-button>
    <el-button
      v-hasPermi="['dealer:basedata:stamp']"
      :disabled="selectedIds.length === 0"
      plain
      type="warning"
      @click="handleBatchStamp"
    >
      <Icon class="mr-5px" icon="ep:stamp" />
      批量申请盖章
    </el-button>
    <el-button
      v-hasPermi="['dealer:basedata:consult']"
      :disabled="selectedRows.length === 0"
      plain
      @click="handleBatchConsult"
    >
      <Icon class="mr-5px" icon="ep:chat-dot-round" />
      批量咨询
    </el-button>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table
      v-loading="loading"
      :data="list"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column align="center" label="文件名称" min-width="180" prop="fileName" show-overflow-tooltip />
      <el-table-column align="center" label="编号" prop="fileNo" width="140" />
      <el-table-column align="center" label="类型" prop="fileTypeName" width="120" />
      <el-table-column align="center" label="经销商" prop="dealerName" width="140" />
      <el-table-column
        :formatter="dateFormatter"
        align="center"
        label="有效期至"
        prop="expireDate"
        width="120"
      />
      <el-table-column align="center" label="有效期状态" width="100">
        <template #default="scope">
          <el-tag
            v-if="scope.row.expireStatus"
            :type="getExpireStatusType(scope.row.expireStatus)"
          >
            {{ getExpireStatusLabel(scope.row.expireStatus) }}
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column align="center" fixed="right" label="操作" width="280">
        <template #default="scope">
          <el-button
            v-hasPermi="['dealer:basedata:query']"
            link
            type="primary"
            @click="handleDetail(scope.row)"
          >
            明细
          </el-button>
          <el-button
            v-hasPermi="['dealer:basedata:ai']"
            link
            type="info"
            @click="handleAiInterpret(scope.row)"
          >
            AI解读
          </el-button>
          <el-button
            v-hasPermi="['dealer:basedata:download']"
            link
            type="success"
            @click="handleDownload(scope.row)"
          >
            下载
          </el-button>
          <el-button
            v-hasPermi="['dealer:basedata:stamp']"
            link
            type="warning"
            @click="handleStamp"
          >
            申请盖章
          </el-button>
          <el-button
            v-hasPermi="['dealer:basedata:consult']"
            link
            type="primary"
            @click="handleConsult(scope.row)"
          >
            💬 咨询
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 弹窗 -->
  <FileDetailModal ref="fileDetailModalRef" />
  <AiInterpretModal ref="aiInterpretModalRef" />

  <!-- 咨询聊天窗口 -->
  <ChatWindow v-model="chatVisible" :session-id="currentSessionId" mode="dealer" />
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as BasedataApi from '@/api/opshub/basedata'
import * as DealerApi from '@/api/opshub/dealer'
import FileDetailModal from './components/FileDetailModal.vue'
import AiInterpretModal from './components/AiInterpretModal.vue'
import ChatWindow from '@/components/CsChatWindow/ChatWindow.vue'
import { useCsConsult } from '@/hooks/useCsConsult'
import type { TabsPaneContext } from 'element-plus'

defineOptions({ name: 'OpshubBasedata' })

const message = useMessage()

// ========== 咨询集成 ==========
const { chatVisible, currentSessionId, openConsult, openBatchConsult } = useCsConsult()

// ========== 数据 ==========
const loading = ref(false)
const total = ref(0)
const list = ref<BasedataApi.BasedataFileVO[]>([])
const dealerList = ref<DealerApi.DealerSimpleVO[]>([])
const selectedIds = ref<number[]>([])
const selectedRows = ref<BasedataApi.BasedataFileVO[]>([])
const activeTab = ref('all')

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  category: undefined as string | undefined,
  fileName: '',
  dealerId: undefined as number | undefined,
  expireStatus: undefined as string | undefined
})
const queryFormRef = ref()

// ========== 方法 ==========

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await BasedataApi.getBasedataFilePage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 搜索 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置 */
const resetQuery = () => {
  queryFormRef.value?.resetFields()
  activeTab.value = 'all'
  queryParams.category = undefined
  handleQuery()
}

/** Tab 切换 */
const handleTabClick = (tab: TabsPaneContext) => {
  queryParams.category = tab.paneName === 'all' ? undefined : String(tab.paneName)
  handleQuery()
}

/** 多选变化 */
const handleSelectionChange = (rows: BasedataApi.BasedataFileVO[]) => {
  selectedIds.value = rows.map((row) => row.id)
  selectedRows.value = rows
}

/** 明细 */
const fileDetailModalRef = ref()
const handleDetail = (row: BasedataApi.BasedataFileVO) => {
  fileDetailModalRef.value.open(row)
}

/** AI 解读 */
const aiInterpretModalRef = ref()
const handleAiInterpret = (row: BasedataApi.BasedataFileVO) => {
  aiInterpretModalRef.value.open(row)
}

/** 下载 */
const handleDownload = async (row: BasedataApi.BasedataFileVO) => {
  try {
    const url = await BasedataApi.getDownloadUrl(row.id)
    if (url) {
      window.open(url, '_blank')
    } else {
      message.warning('该文件暂无下载地址')
    }
  } catch (e) {
    message.error('获取下载链接失败')
  }
}

/** 批量下载 */
const handleBatchDownload = async () => {
  for (const id of selectedIds.value) {
    try {
      const url = await BasedataApi.getDownloadUrl(id)
      if (url) {
        window.open(url, '_blank')
      }
    } catch (e) {
      console.error('下载失败', e)
    }
  }
}

/** 申请盖章（占位） */
const handleStamp = () => {
  message.info('盖章申请功能将在 Step 3 客户服务模块中实现')
}

/** 批量申请盖章（占位） */
const handleBatchStamp = () => {
  message.info('批量盖章申请功能将在 Step 3 客户服务模块中实现')
}

/** 咨询 */
const handleConsult = (row: BasedataApi.BasedataFileVO) => {
  openConsult({
    consultType: 'basedata',
    sourceModule: 'basedata',
    context: `文件 ${row.fileName || row.fileNo} 咨询`,
    contextId: row.id,
    contextCode: row.fileNo,
    dealerCode: row.dealerCode,
    dealerName: row.dealerName
  })
}

/** 批量咨询 */
const handleBatchConsult = async () => {
  await openBatchConsult(selectedRows.value, (row) => ({
    consultType: 'basedata',
    sourceModule: 'basedata',
    context: `文件 ${row.fileName || row.fileNo} 咨询`,
    contextId: row.id,
    contextCode: row.fileNo,
    dealerCode: row.dealerCode,
    dealerName: row.dealerName
  }))
}

/** 有效期状态标签 */
const getExpireStatusType = (status: string) => {
  switch (status) {
    case 'valid':
      return 'success'
    case 'expiring_soon':
      return 'warning'
    case 'expired':
      return 'danger'
    default:
      return 'info'
  }
}

const getExpireStatusLabel = (status: string) => {
  switch (status) {
    case 'valid':
      return '有效'
    case 'expiring_soon':
      return '即将到期'
    case 'expired':
      return '已过期'
    default:
      return status
  }
}

/** 初始化 */
onMounted(async () => {
  await getList()
  // 加载经销商列表
  dealerList.value = await DealerApi.getSimpleDealerList()
})
</script>
