<template>
  <!-- 筛选栏 -->
  <ContentWrap>
    <el-form :model="queryParams" inline>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" clearable placeholder="全部" style="width: 120px">
          <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作类型">
        <el-select v-model="queryParams.opType" clearable placeholder="全部" style="width: 120px">
          <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="来源模块">
        <el-select v-model="queryParams.sourceModule" clearable placeholder="全部" style="width: 120px">
          <el-option v-for="m in sourceModuleOptions" :key="m.value" :label="m.label" :value="m.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-input v-model="queryParams.keyword" clearable placeholder="搜索编号/内容" style="width: 200px" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch"><Icon icon="ep:search" class="mr-4px" />搜索</el-button>
        <el-button @click="handleReset"><Icon icon="ep:refresh" class="mr-4px" />重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 表格 -->
  <ContentWrap>
    <div class="mb-10px flex items-center gap-10px">
      <el-button v-hasPermi="['dealer:cs-opreq:create']" type="primary" plain @click="handleCreate">
        <Icon class="mr-5px" icon="ep:plus" />发起请求
      </el-button>
    </div>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column label="编号" prop="opreqCode" min-width="170" />
      <el-table-column label="操作类型" prop="opType" min-width="90" align="center">
        <template #default="{ row }">
          <el-tag size="small">{{ getTypeLabel(row.opType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="经销商" prop="dealerName" min-width="140" show-overflow-tooltip />
      <el-table-column label="关联内容" prop="content" min-width="180" show-overflow-tooltip />
      <el-table-column label="产品线" prop="productLineName" min-width="90" />
      <el-table-column label="状态" prop="status" min-width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" min-width="160">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" min-width="200" align="center">
        <template #default="{ row }">
          <!-- 待处理：执行员可接单 -->
          <el-button v-if="row.status === 0" v-hasPermi="['dealer:cs-opreq:accept']" link type="primary" @click="handleAccept(row)">接单</el-button>
          <!-- 处理中：执行员可提交 -->
          <el-button v-if="row.status === 1" v-hasPermi="['dealer:cs-opreq:submit']" link type="success" @click="handleSubmit(row)">提交</el-button>
          <!-- 等待验收：经销商可验收 -->
          <el-button v-if="row.status === 2" v-hasPermi="['dealer:cs-opreq:verify']" link type="success" @click="handleVerify(row)">验收</el-button>
          <!-- 详情 -->
          <el-button link type="info" @click="handleDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <Pagination v-model:limit="queryParams.pageSize" v-model:page="queryParams.pageNo" :total="total" @pagination="getList" />
  </ContentWrap>

  <!-- 创建弹窗 -->
  <el-dialog v-model="createDialogVisible" title="发起操作请求" width="600px">
    <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
      <el-form-item label="操作类型" prop="opType">
        <el-select v-model="createForm.opType" placeholder="请选择" style="width: 100%">
          <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="经销商编码" prop="dealerCode">
        <el-input v-model="createForm.dealerCode" placeholder="如 D001" />
      </el-form-item>
      <el-form-item label="产品线编码">
        <el-input v-model="createForm.productLineCode" placeholder="可选" />
      </el-form-item>
      <el-form-item label="来源模块" prop="sourceModule">
        <el-select v-model="createForm.sourceModule" placeholder="请选择" style="width: 100%">
          <el-option v-for="m in sourceModuleOptions" :key="m.value" :label="m.label" :value="m.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="来源编号">
        <el-input v-model="createForm.sourceCode" placeholder="可选，关联业务编号" />
      </el-form-item>
      <el-form-item label="请求内容" prop="content">
        <el-input v-model="createForm.content" type="textarea" :rows="4" placeholder="请描述操作请求内容" />
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

  <!-- 提交弹窗 -->
  <el-dialog v-model="submitDialogVisible" title="提交处理结果" width="500px">
    <el-form :model="submitForm" label-width="80px">
      <el-form-item label="处理留言">
        <el-input v-model="submitForm.submitRemark" type="textarea" :rows="3" placeholder="请填写处理留言（可选）" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="doSubmit">确认提交</el-button>
    </template>
  </el-dialog>

  <!-- 详情弹窗 -->
  <el-dialog v-model="detailDialogVisible" title="操作请求详情" width="700px">
    <el-descriptions v-if="detailData" :column="2" border>
      <el-descriptions-item label="编号">{{ detailData.opreqCode }}</el-descriptions-item>
      <el-descriptions-item label="操作类型">{{ getTypeLabel(detailData.opType) }}</el-descriptions-item>
      <el-descriptions-item label="状态">{{ getStatusLabel(detailData.status) }}</el-descriptions-item>
      <el-descriptions-item label="经销商">{{ detailData.dealerName }}</el-descriptions-item>
      <el-descriptions-item label="产品线">{{ detailData.productLineName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="来源模块">{{ getSourceModuleLabel(detailData.sourceModule) }}</el-descriptions-item>
      <el-descriptions-item label="来源编号">{{ detailData.sourceCode || '-' }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ formatNullableDate(detailData.createTime) }}</el-descriptions-item>
      <el-descriptions-item label="内容" :span="2">{{ detailData.content }}</el-descriptions-item>
      <el-descriptions-item label="接单时间">{{ formatNullableDate(detailData.acceptTime) }}</el-descriptions-item>
      <el-descriptions-item label="提交时间">{{ formatNullableDate(detailData.submitTime) }}</el-descriptions-item>
      <el-descriptions-item label="处理留言" :span="2">{{ detailData.submitRemark || '-' }}</el-descriptions-item>
      <el-descriptions-item label="验收时间">{{ formatNullableDate(detailData.verifyTime) }}</el-descriptions-item>
      <el-descriptions-item label="完成时间">{{ formatNullableDate(detailData.completedTime) }}</el-descriptions-item>
      <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
    </el-descriptions>
  </el-dialog>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import * as CsOpReqApi from '@/api/opshub/csOpReq'
import { formatDate, formatNullableDate } from '@/utils/formatTime'

// ========== 枚举常量 ==========
const statusOptions = [
  { value: 0, label: '待处理' }, { value: 1, label: '处理中' },
  { value: 2, label: '等待验收' }, { value: 3, label: '已完成' }
]
const typeOptions = [
  { value: 'sign', label: '签约' }, { value: 'invoice', label: '开票' },
  { value: 'stamp', label: '盖章' }, { value: 'aftersale', label: '售后' }
]
const sourceModuleOptions = [
  { value: 'signing', label: '签约进度' }, { value: 'order', label: '订单' },
  { value: 'aftersale', label: '售后' }, { value: 'basedata', label: '基础数据' }
]

const getStatusLabel = (val: number) => statusOptions.find(s => s.value === val)?.label || ''
const getStatusTagType = (val: number) => (['warning', 'primary', 'success', 'info'] as any)[val] || ''
const getTypeLabel = (val: string) => typeOptions.find(t => t.value === val)?.label || val || ''
const getSourceModuleLabel = (val: string) => sourceModuleOptions.find(m => m.value === val)?.label || val || ''

// ========== 列表查询 ==========
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1, pageSize: 20,
  status: undefined as number | undefined,
  opType: undefined as string | undefined,
  sourceModule: undefined as string | undefined,
  keyword: ''
})

const getList = async () => {
  loading.value = true
  try {
    const data = await CsOpReqApi.getOpReqPage(queryParams)
    list.value = data.list; total.value = data.total
  } finally { loading.value = false }
}

const handleSearch = () => { queryParams.pageNo = 1; getList() }
const handleReset = () => {
  queryParams.status = undefined; queryParams.opType = undefined
  queryParams.sourceModule = undefined; queryParams.keyword = ''; handleSearch()
}

// ========== 创建 ==========
const createDialogVisible = ref(false)
const createLoading = ref(false)
const createFormRef = ref<FormInstance>()
const createForm = reactive({
  opType: '' as string, dealerCode: '', productLineCode: '',
  sourceModule: '' as string, sourceCode: '', content: '', remark: ''
})
const createRules: FormRules = {
  opType: [{ required: true, message: '请选择操作类型', trigger: 'change' }],
  dealerCode: [{ required: true, message: '请输入经销商编码', trigger: 'blur' }],
  sourceModule: [{ required: true, message: '请选择来源模块', trigger: 'change' }],
  content: [{ required: true, message: '请输入请求内容', trigger: 'blur' }]
}

const handleCreate = () => {
  Object.assign(createForm, { opType: '', dealerCode: '', productLineCode: '', sourceModule: '', sourceCode: '', content: '', remark: '' })
  createDialogVisible.value = true
}

const submitCreate = async () => {
  await createFormRef.value?.validate()
  createLoading.value = true
  try {
    await CsOpReqApi.createOpReq(createForm)
    ElMessage.success('操作请求创建成功'); createDialogVisible.value = false; getList()
  } finally { createLoading.value = false }
}

// ========== 接单 ==========
const handleAccept = async (row: any) => {
  await ElMessageBox.confirm('确认接单？', '提示', { type: 'info' })
  await CsOpReqApi.acceptOpReq(row.id); ElMessage.success('接单成功'); getList()
}

// ========== 提交 ==========
const submitDialogVisible = ref(false)
const submitLoading = ref(false)
const submitForm = reactive({ id: 0, submitRemark: '' })

const handleSubmit = (row: any) => {
  submitForm.id = row.id; submitForm.submitRemark = ''; submitDialogVisible.value = true
}

const doSubmit = async () => {
  submitLoading.value = true
  try {
    await CsOpReqApi.submitOpReq(submitForm)
    ElMessage.success('提交成功'); submitDialogVisible.value = false; getList()
  } finally { submitLoading.value = false }
}

// ========== 验收 ==========
const handleVerify = async (row: any) => {
  await ElMessageBox.confirm('确认验收通过？', '提示', { type: 'success' })
  await CsOpReqApi.verifyOpReq(row.id); ElMessage.success('验收通过'); getList()
}

// ========== 详情 ==========
const detailDialogVisible = ref(false)
const detailData = ref<any>(null)

const handleDetail = async (row: any) => {
  detailData.value = await CsOpReqApi.getOpReq(row.id)
  detailDialogVisible.value = true
}

// ========== 初始化 ==========
onMounted(() => { getList() })
</script>
