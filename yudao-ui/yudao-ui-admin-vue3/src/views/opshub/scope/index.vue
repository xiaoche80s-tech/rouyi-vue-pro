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
          <el-table-column align="center" label="名称" prop="name" />
          <el-table-column align="center" label="编码" prop="code" />
          <el-table-column align="center" label="操作" width="100">
            <template #default="scope">
              <el-button link type="danger" @click="removeExecutorScope(scope.row)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-divider content-position="left">添加授权产品线</el-divider>
        <el-form :inline="true" class="mb-10px">
          <el-form-item label="产品线">
            <el-select v-model="selectedProductLineId" class="!w-300px" filterable placeholder="请选择产品线">
              <el-option
                v-for="pl in availableProductLines"
                :key="pl.id"
                :label="pl.name"
                :value="pl.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :disabled="!executorUserId || !selectedProductLineId" type="primary" @click="addExecutorScope">
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
          <el-table-column align="center" label="名称" prop="name" />
          <el-table-column align="center" label="编码" prop="code" />
          <el-table-column align="center" label="操作" width="100">
            <template #default="scope">
              <el-button link type="danger" @click="removeDealerScope(scope.row)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-divider content-position="left">添加授权经销商</el-divider>
        <el-form :inline="true" class="mb-10px">
          <el-form-item label="经销商">
            <el-select v-model="selectedDealerId" class="!w-300px" filterable placeholder="请选择经销商">
              <el-option
                v-for="d in availableDealers"
                :key="d.id"
                :label="d.name"
                :value="d.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :disabled="!dealerUserId || !selectedDealerId" type="primary" @click="addDealerScope">
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
const executorProductLineIds = ref<Set<number>>(new Set())
const executorProductLines = ref<any[]>([])
const allProductLines = ref<ProductLineApi.ProductLineSimpleVO[]>([])
const selectedProductLineId = ref<number | undefined>(undefined)

/** 可选产品线（排除已授权的） */
const availableProductLines = computed(() => {
  return allProductLines.value.filter((pl) => !executorProductLineIds.value.has(pl.id))
})

/** 执行员用户切换 */
const onExecutorUserChange = async () => {
  if (!executorUserId.value) return
  executorLoading.value = true
  try {
    const [ids, productLines] = await Promise.all([
      DealerScopeApi.getProductLineIdsByUserId(executorUserId.value),
      allProductLines.value.length ? Promise.resolve(allProductLines.value) : ProductLineApi.getSimpleProductLineList()
    ])
    allProductLines.value = productLines
    executorProductLineIds.value = new Set(ids)
    executorProductLines.value = productLines.filter((pl) => ids.includes(pl.id))
    selectedProductLineId.value = undefined
  } finally {
    executorLoading.value = false
  }
}

/** 添加执行员产品线授权 */
const addExecutorScope = async () => {
  if (!executorUserId.value || !selectedProductLineId.value) return
  try {
    const newIds = [...executorProductLineIds.value, selectedProductLineId.value]
    await DealerScopeApi.assignProductLineScope(executorUserId.value, newIds)
    message.success('授权成功')
    selectedProductLineId.value = undefined
    await onExecutorUserChange()
  } catch {}
}

/** 移除执行员产品线授权 */
const removeExecutorScope = async (row: any) => {
  if (!executorUserId.value) return
  try {
    await message.confirm(`确认移除产品线「${row.name}」的授权？`)
    const newIds = [...executorProductLineIds.value].filter((id) => id !== row.id)
    await DealerScopeApi.assignProductLineScope(executorUserId.value, newIds)
    message.success('移除成功')
    await onExecutorUserChange()
  } catch {}
}

// ========== Tab 2: 经销商 ==========
const dealerUserId = ref<number | undefined>(undefined)
const dealerLoading = ref(false)
const dealerScopeDealerIds = ref<Set<number>>(new Set())
const dealerScopeDealers = ref<any[]>([])
const allDealers = ref<DealerApi.DealerSimpleVO[]>([])
const selectedDealerId = ref<number | undefined>(undefined)

/** 可选经销商（排除已授权的） */
const availableDealers = computed(() => {
  return allDealers.value.filter((d) => !dealerScopeDealerIds.value.has(d.id))
})

/** 经销商用户切换 */
const onDealerUserChange = async () => {
  if (!dealerUserId.value) return
  dealerLoading.value = true
  try {
    const [ids, dealers] = await Promise.all([
      DealerScopeApi.getDealerIdsByUserId(dealerUserId.value),
      allDealers.value.length ? Promise.resolve(allDealers.value) : DealerApi.getSimpleDealerList()
    ])
    allDealers.value = dealers
    dealerScopeDealerIds.value = new Set(ids)
    dealerScopeDealers.value = dealers.filter((d) => ids.includes(d.id))
    selectedDealerId.value = undefined
  } finally {
    dealerLoading.value = false
  }
}

/** 添加经销商授权 */
const addDealerScope = async () => {
  if (!dealerUserId.value || !selectedDealerId.value) return
  try {
    const newIds = [...dealerScopeDealerIds.value, selectedDealerId.value]
    await DealerScopeApi.assignDealerScope(dealerUserId.value, newIds)
    message.success('授权成功')
    selectedDealerId.value = undefined
    await onDealerUserChange()
  } catch {}
}

/** 移除经销商授权 */
const removeDealerScope = async (row: any) => {
  if (!dealerUserId.value) return
  try {
    await message.confirm(`确认移除经销商「${row.name}」的授权？`)
    const newIds = [...dealerScopeDealerIds.value].filter((id) => id !== row.id)
    await DealerScopeApi.assignDealerScope(dealerUserId.value, newIds)
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
