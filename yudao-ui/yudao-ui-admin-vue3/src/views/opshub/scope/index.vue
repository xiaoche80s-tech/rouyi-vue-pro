<template>
  <ContentWrap>
    <el-tabs v-model="activeTab">
      <!-- Tab 1: 服务单执行员 -->
      <el-tab-pane label="服务单执行员" name="executor">
        <div class="mb-15px">
          <el-select
            v-model="executorUserId"
            class="!w-300px"
            filterable
            placeholder="请选择执行员用户"
            @change="onExecutorUserChange"
          >
            <el-option
              v-for="u in userList"
              :key="u.id"
              :label="u.nickname || u.username"
              :value="u.id"
            />
          </el-select>
        </div>
        <!-- 已授权产品线 -->
        <el-table v-loading="executorLoading" :data="executorProductLines">
          <el-table-column align="center" label="产品线ID" prop="id" width="100" />
          <el-table-column align="center" label="名称" prop="productLineName" />
          <el-table-column align="center" label="编码" prop="productLineCode" />
          <el-table-column align="center" label="操作" width="100">
            <template #default="scope">
              <el-button link type="danger" @click="removeExecutorScope(scope.row)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-divider content-position="left">添加授权产品线</el-divider>
        <el-form :inline="true" class="mb-10px">
          <el-form-item label="产品线">
            <el-select v-model="selectedProductLineCode" class="!w-300px" filterable placeholder="请选择产品线">
              <el-option
                v-for="pl in availableProductLines"
                :key="pl.id"
                :label="pl.productLineName"
                :value="pl.productLineCode"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :disabled="!executorUserId || !selectedProductLineCode" type="primary" @click="addExecutorScope">
              <Icon class="mr-5px" icon="ep:plus" />
              添加
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- Tab 2: 经销商 -->
      <el-tab-pane label="经销商" name="dealer">
        <div class="mb-15px">
          <el-select
            v-model="dealerUserId"
            class="!w-300px"
            filterable
            placeholder="请选择经销商代理人用户"
            @change="onDealerUserChange"
          >
            <el-option
              v-for="u in userList"
              :key="u.id"
              :label="u.nickname || u.username"
              :value="u.id"
            />
          </el-select>
        </div>
        <!-- 已授权经销商 -->
        <el-table v-loading="dealerLoading" :data="dealerScopeDealers">
          <el-table-column align="center" label="经销商ID" prop="id" width="100" />
          <el-table-column align="center" label="名称" prop="dealerName" />
          <el-table-column align="center" label="编码" prop="dealerCode" />
          <el-table-column align="center" label="操作" width="100">
            <template #default="scope">
              <el-button link type="danger" @click="removeDealerScope(scope.row)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-divider content-position="left">添加授权经销商</el-divider>
        <el-form :inline="true" class="mb-10px">
          <el-form-item label="经销商">
            <el-select v-model="selectedDealerCode" class="!w-300px" filterable placeholder="请选择经销商">
              <el-option
                v-for="d in availableDealers"
                :key="d.id"
                :label="d.dealerName"
                :value="d.dealerCode"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :disabled="!dealerUserId || !selectedDealerCode" type="primary" @click="addDealerScope">
              <Icon class="mr-5px" icon="ep:plus" />
              添加
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </ContentWrap>
</template>

<script lang="ts" setup>
import * as DealerScopeApi from '@/api/opshub/dealerScope'
import * as DealerApi from '@/api/opshub/dealer'
import * as ProductLineApi from '@/api/opshub/productLine'
import * as UserApi from '@/api/system/user'

defineOptions({ name: 'OpsHubScopeManagement' })

const message = useMessage()

const activeTab = ref('executor')

// ========== 公共：用户列表 ==========
const userList = ref<UserApi.UserVO[]>([])

// ========== Tab 1: 服务单执行员 ==========
const executorUserId = ref<number | undefined>(undefined)
const executorLoading = ref(false)
const executorProductLineCodes = ref<Set<string>>(new Set())
const executorProductLines = ref<any[]>([])
const allProductLines = ref<ProductLineApi.ProductLineSimpleVO[]>([])
const selectedProductLineCode = ref<string | undefined>(undefined)

/** 可选产品线（排除已授权的） */
const availableProductLines = computed(() => {
  return allProductLines.value.filter((pl) => !executorProductLineCodes.value.has(pl.productLineCode))
})

/** 执行员用户切换 */
const onExecutorUserChange = async () => {
  if (!executorUserId.value) return
  executorLoading.value = true
  try {
    const [codes, productLines] = await Promise.all([
      DealerScopeApi.getProductLineCodesByUserId(executorUserId.value),
      allProductLines.value.length ? Promise.resolve(allProductLines.value) : ProductLineApi.getSimpleProductLineList()
    ])
    allProductLines.value = productLines
    executorProductLineCodes.value = new Set(codes)
    executorProductLines.value = productLines.filter((pl) => codes.includes(pl.productLineCode))
    selectedProductLineCode.value = undefined
  } finally {
    executorLoading.value = false
  }
}

/** 添加执行员产品线授权 */
const addExecutorScope = async () => {
  if (!executorUserId.value || !selectedProductLineCode.value) return
  try {
    const newCodes = [...executorProductLineCodes.value, selectedProductLineCode.value]
    await DealerScopeApi.assignProductLineScope(executorUserId.value, newCodes)
    message.success('授权成功')
    selectedProductLineCode.value = undefined
    await onExecutorUserChange()
  } catch {}
}

/** 移除执行员产品线授权 */
const removeExecutorScope = async (row: any) => {
  if (!executorUserId.value) return
  try {
    await message.confirm(`确认移除产品线「${row.productLineName}」的授权？`)
    const newCodes = [...executorProductLineCodes.value].filter((code) => code !== row.productLineCode)
    await DealerScopeApi.assignProductLineScope(executorUserId.value, newCodes)
    message.success('移除成功')
    await onExecutorUserChange()
  } catch {}
}

// ========== Tab 2: 经销商 ==========
const dealerUserId = ref<number | undefined>(undefined)
const dealerLoading = ref(false)
const dealerScopeDealerCodes = ref<Set<string>>(new Set())
const dealerScopeDealers = ref<any[]>([])
const allDealers = ref<DealerApi.DealerSimpleVO[]>([])
const selectedDealerCode = ref<string | undefined>(undefined)

/** 可选经销商（排除已授权的） */
const availableDealers = computed(() => {
  return allDealers.value.filter((d) => !dealerScopeDealerCodes.value.has(d.dealerCode))
})

/** 经销商用户切换 */
const onDealerUserChange = async () => {
  if (!dealerUserId.value) return
  dealerLoading.value = true
  try {
    const [codes, dealers] = await Promise.all([
      DealerScopeApi.getDealerCodesByUserId(dealerUserId.value),
      allDealers.value.length ? Promise.resolve(allDealers.value) : DealerApi.getSimpleDealerList()
    ])
    allDealers.value = dealers
    dealerScopeDealerCodes.value = new Set(codes)
    dealerScopeDealers.value = dealers.filter((d) => codes.includes(d.dealerCode))
    selectedDealerCode.value = undefined
  } finally {
    dealerLoading.value = false
  }
}

/** 添加经销商授权 */
const addDealerScope = async () => {
  if (!dealerUserId.value || !selectedDealerCode.value) return
  try {
    const newCodes = [...dealerScopeDealerCodes.value, selectedDealerCode.value]
    await DealerScopeApi.assignDealerScope(dealerUserId.value, newCodes)
    message.success('授权成功')
    selectedDealerCode.value = undefined
    await onDealerUserChange()
  } catch {}
}

/** 移除经销商授权 */
const removeDealerScope = async (row: any) => {
  if (!dealerUserId.value) return
  try {
    await message.confirm(`确认移除经销商「${row.dealerName}」的授权？`)
    const newCodes = [...dealerScopeDealerCodes.value].filter((code) => code !== row.dealerCode)
    await DealerScopeApi.assignDealerScope(dealerUserId.value, newCodes)
    message.success('移除成功')
    await onDealerUserChange()
  } catch {}
}

// ========== 初始化 ==========
onMounted(async () => {
  const [users, productLines, dealers] = await Promise.all([
    UserApi.getSimpleUserList(),
    ProductLineApi.getSimpleProductLineList(),
    DealerApi.getSimpleDealerList()
  ])
  userList.value = users
  allProductLines.value = productLines
  allDealers.value = dealers
})
</script>
