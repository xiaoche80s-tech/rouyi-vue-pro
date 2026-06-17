<template>
  <ContentWrap>
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <!-- Tab 1: 服务单执行员 -->
      <el-tab-pane label="服务单执行员" name="executor">
        <div class="mb-15px">
          <el-button type="primary" @click="openExecutorDrawer">
            <Icon class="mr-5px" icon="ep:plus" />
            新增
          </el-button>
        </div>
        <el-table v-loading="executorLoading" :data="executorScopeUsers">
          <el-table-column align="center" label="用户昵称" prop="nickname" width="150" />
          <el-table-column align="center" label="已授权产品线">
            <template #default="scope">
              <el-tag
                v-for="item in scope.row.productLines"
                :key="item.productLineCode"
                closable
                class="mr-5px mb-3px"
                @close="removeExecutorProductLine(scope.row, item.productLineCode)"
              >
                {{ item.productLineName }}({{ item.productLineCode }})
              </el-tag>
              <el-button
                v-if="scope.row.productLines && scope.row.productLines.length"
                link
                type="danger"
                size="small"
                class="ml-5px"
                @click="clearExecutorScope(scope.row)"
              >
                清空
              </el-button>
            </template>
          </el-table-column>
          <el-table-column align="center" label="操作" width="80">
            <template #default="scope">
              <el-button link type="primary" @click="editExecutorScope(scope.row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- Tab 2: 经销商 -->
      <el-tab-pane label="经销商" name="dealer">
        <div class="mb-15px">
          <el-button type="primary" @click="openDealerDrawer">
            <Icon class="mr-5px" icon="ep:plus" />
            新增
          </el-button>
        </div>
        <el-table v-loading="dealerLoading" :data="dealerScopeUsers">
          <el-table-column align="center" label="用户昵称" prop="nickname" width="150" />
          <el-table-column align="center" label="已授权经销商">
            <template #default="scope">
              <el-tag
                v-for="item in scope.row.dealers"
                :key="item.dealerCode"
                closable
                class="mr-5px mb-3px"
                @close="removeDealerScope(scope.row, item.dealerCode)"
              >
                {{ item.dealerName }}({{ item.dealerCode }})
              </el-tag>
              <el-button
                v-if="scope.row.dealers && scope.row.dealers.length"
                link
                type="danger"
                size="small"
                class="ml-5px"
                @click="clearDealerScope(scope.row)"
              >
                清空
              </el-button>
            </template>
          </el-table-column>
          <el-table-column align="center" label="操作" width="80">
            <template #default="scope">
              <el-button link type="primary" @click="editDealerScope(scope.row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 执行员产品线授权抽屉 -->
    <el-drawer v-model="executorDrawerVisible" :title="executorDrawerTitle" direction="rtl" size="400px">
      <el-form :model="executorDrawerForm" label-width="100px">
        <el-form-item label="执行员用户">
          <el-select
            v-model="executorDrawerForm.userId"
            class="!w-full"
            filterable
            placeholder="请选择执行员用户"
            :disabled="isEditMode"
            @change="onExecutorDrawerUserChange"
          >
            <el-option
              v-for="u in executorUserList"
              :key="u.id"
              :label="u.nickname || u.username"
              :value="u.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="产品线">
          <el-select
            v-model="executorDrawerForm.productLineCodes"
            class="!w-full"
            filterable
            multiple
            placeholder="请选择产品线"
          >
            <el-option
              v-for="pl in allProductLines"
              :key="pl.productLineCode"
              :label="`${pl.productLineName}(${pl.productLineCode})`"
              :value="pl.productLineCode"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="executorDrawerVisible = false">取消</el-button>
        <el-button :loading="executorDrawerLoading" type="primary" @click="submitExecutorScope">确定</el-button>
      </template>
    </el-drawer>

    <!-- 经销商授权抽屉 -->
    <el-drawer v-model="dealerDrawerVisible" :title="dealerDrawerTitle" direction="rtl" size="400px">
      <el-form :model="dealerDrawerForm" label-width="100px">
        <el-form-item label="经销商用户">
          <el-select
            v-model="dealerDrawerForm.userId"
            class="!w-full"
            filterable
            placeholder="请选择经销商代理人用户"
            :disabled="isEditMode"
            @change="onDealerDrawerUserChange"
          >
            <el-option
              v-for="u in dealerUserList"
              :key="u.id"
              :label="u.nickname || u.username"
              :value="u.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="经销商">
          <el-select
            v-model="dealerDrawerForm.dealerCodes"
            class="!w-full"
            filterable
            multiple
            placeholder="请选择经销商"
          >
            <el-option
              v-for="d in allDealers"
              :key="d.dealerCode"
              :label="`${d.dealerName}(${d.dealerCode})`"
              :value="d.dealerCode"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dealerDrawerVisible = false">取消</el-button>
        <el-button :loading="dealerDrawerLoading" type="primary" @click="submitDealerScope">确定</el-button>
      </template>
    </el-drawer>
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

// ========== Tab 1: 服务单执行员 ==========
const executorLoading = ref(false)
const executorScopeUsers = ref<DealerScopeApi.ExecutorScopeUserRespVO[]>([])

const loadExecutorScopeUsers = async () => {
  executorLoading.value = true
  try {
    executorScopeUsers.value = await DealerScopeApi.getExecutorScopeUsers()
  } finally {
    executorLoading.value = false
  }
}

const removeExecutorProductLine = async (
  row: DealerScopeApi.ExecutorScopeUserRespVO,
  productLineCode: string
) => {
  try {
    await message.confirm(`确认移除产品线「${productLineCode}」的授权？`)
    const remainingCodes = row.productLines
      .filter((pl) => pl.productLineCode !== productLineCode)
      .map((pl) => pl.productLineCode)
    await DealerScopeApi.assignProductLineScope(row.userId, remainingCodes)
    message.success('移除成功')
    await loadExecutorScopeUsers()
  } catch {}
}

const clearExecutorScope = async (row: DealerScopeApi.ExecutorScopeUserRespVO) => {
  try {
    await message.confirm(`确认清空该用户所有产品线授权？`)
    await DealerScopeApi.assignProductLineScope(row.userId, [])
    message.success('清空成功')
    await loadExecutorScopeUsers()
  } catch {}
}

// ========== Tab 2: 经销商 ==========
const dealerLoading = ref(false)
const dealerScopeUsers = ref<DealerScopeApi.DealerScopeUserRespVO[]>([])

const loadDealerScopeUsers = async () => {
  dealerLoading.value = true
  try {
    dealerScopeUsers.value = await DealerScopeApi.getDealerScopeUsers()
  } finally {
    dealerLoading.value = false
  }
}

const removeDealerScope = async (row: DealerScopeApi.DealerScopeUserRespVO, dealerCode: string) => {
  try {
    await message.confirm(`确认移除经销商「${dealerCode}」的授权？`)
    const remainingCodes = row.dealers.filter((d) => d.dealerCode !== dealerCode).map((d) => d.dealerCode)
    await DealerScopeApi.assignDealerScope(row.userId, remainingCodes)
    message.success('移除成功')
    await loadDealerScopeUsers()
  } catch {}
}

const clearDealerScope = async (row: DealerScopeApi.DealerScopeUserRespVO) => {
  try {
    await message.confirm(`确认清空该用户所有经销商授权？`)
    await DealerScopeApi.assignDealerScope(row.userId, [])
    message.success('清空成功')
    await loadDealerScopeUsers()
  } catch {}
}

// ========== Tab 切换 ==========
const onTabChange = (tab: string | number) => {
  if (tab === 'executor') {
    loadExecutorScopeUsers()
  } else {
    loadDealerScopeUsers()
  }
}

// ========== 执行员授权抽屉 ==========
const executorDrawerVisible = ref(false)
const executorDrawerTitle = ref('新增执行员授权')
const executorDrawerLoading = ref(false)
const isEditMode = ref(false)
const executorUserList = ref<UserApi.UserVO[]>([])
const allProductLines = ref<ProductLineApi.ProductLineSimpleVO[]>([])

const executorDrawerForm = ref<{ userId: number | undefined; productLineCodes: string[] }>({
  userId: undefined,
  productLineCodes: []
})

const openExecutorDrawer = async () => {
  isEditMode.value = false
  executorDrawerTitle.value = '新增执行员授权'
  executorDrawerForm.value = { userId: undefined, productLineCodes: [] }
  executorDrawerVisible.value = true
  // 加载执行员角色用户列表和产品线列表
  const [users, productLines] = await Promise.all([
    UserApi.getSimpleUserList('service_executor'),
    allProductLines.value.length
      ? Promise.resolve(allProductLines.value)
      : ProductLineApi.getSimpleProductLineList()
  ])
  executorUserList.value = users
  allProductLines.value = productLines
}

const editExecutorScope = async (row: DealerScopeApi.ExecutorScopeUserRespVO) => {
  isEditMode.value = true
  executorDrawerTitle.value = '编辑执行员授权'
  executorDrawerForm.value = {
    userId: row.userId,
    productLineCodes: row.productLines.map((pl) => pl.productLineCode)
  }
  executorDrawerVisible.value = true
  // 加载执行员角色用户列表和产品线列表
  const [users, productLines] = await Promise.all([
    UserApi.getSimpleUserList('service_executor'),
    allProductLines.value.length
      ? Promise.resolve(allProductLines.value)
      : ProductLineApi.getSimpleProductLineList()
  ])
  executorUserList.value = users
  allProductLines.value = productLines
}

const onExecutorDrawerUserChange = async () => {
  if (!executorDrawerForm.value.userId) {
    executorDrawerForm.value.productLineCodes = []
    return
  }
  const existingCodes: string[] = await DealerScopeApi.getProductLineCodesByUserId(
    executorDrawerForm.value.userId
  )
  executorDrawerForm.value.productLineCodes = existingCodes
}

const submitExecutorScope = async () => {
  if (!executorDrawerForm.value.userId) {
    message.warning('请选择执行员用户')
    return
  }
  executorDrawerLoading.value = true
  try {
    await DealerScopeApi.assignProductLineScope(
      executorDrawerForm.value.userId,
      executorDrawerForm.value.productLineCodes
    )
    message.success(executorDrawerTitle.value === '新增执行员授权' ? '新增成功' : '编辑成功')
    executorDrawerVisible.value = false
    await loadExecutorScopeUsers()
  } finally {
    executorDrawerLoading.value = false
  }
}

// ========== 经销商授权抽屉 ==========
const dealerDrawerVisible = ref(false)
const dealerDrawerTitle = ref('新增经销商授权')
const dealerDrawerLoading = ref(false)
const dealerUserList = ref<UserApi.UserVO[]>([])
const allDealers = ref<DealerApi.DealerSimpleVO[]>([])

const dealerDrawerForm = ref<{ userId: number | undefined; dealerCodes: string[] }>({
  userId: undefined,
  dealerCodes: []
})

const openDealerDrawer = async () => {
  isEditMode.value = false
  dealerDrawerTitle.value = '新增经销商授权'
  dealerDrawerForm.value = { userId: undefined, dealerCodes: [] }
  dealerDrawerVisible.value = true
  // 加载经销商角色用户列表和经销商列表
  const [users, dealers] = await Promise.all([
    UserApi.getSimpleUserList('dealer'),
    allDealers.value.length
      ? Promise.resolve(allDealers.value)
      : DealerApi.getSimpleDealerList()
  ])
  dealerUserList.value = users
  allDealers.value = dealers
}

const editDealerScope = async (row: DealerScopeApi.DealerScopeUserRespVO) => {
  isEditMode.value = true
  dealerDrawerTitle.value = '编辑经销商授权'
  dealerDrawerForm.value = {
    userId: row.userId,
    dealerCodes: row.dealers.map((d) => d.dealerCode)
  }
  dealerDrawerVisible.value = true
  // 加载经销商角色用户列表和经销商列表
  const [users, dealers] = await Promise.all([
    UserApi.getSimpleUserList('dealer'),
    allDealers.value.length
      ? Promise.resolve(allDealers.value)
      : DealerApi.getSimpleDealerList()
  ])
  dealerUserList.value = users
  allDealers.value = dealers
}

const onDealerDrawerUserChange = async () => {
  if (!dealerDrawerForm.value.userId) {
    dealerDrawerForm.value.dealerCodes = []
    return
  }
  const existingCodes: string[] = await DealerScopeApi.getDealerCodesByUserId(
    dealerDrawerForm.value.userId
  )
  dealerDrawerForm.value.dealerCodes = existingCodes
}

const submitDealerScope = async () => {
  if (!dealerDrawerForm.value.userId) {
    message.warning('请选择经销商用户')
    return
  }
  dealerDrawerLoading.value = true
  try {
    await DealerScopeApi.assignDealerScope(
      dealerDrawerForm.value.userId,
      dealerDrawerForm.value.dealerCodes
    )
    message.success(dealerDrawerTitle.value === '新增经销商授权' ? '新增成功' : '编辑成功')
    dealerDrawerVisible.value = false
    await loadDealerScopeUsers()
  } finally {
    dealerDrawerLoading.value = false
  }
}

// ========== 初始化 ==========
onMounted(() => {
  loadExecutorScopeUsers()
  loadDealerScopeUsers()
})
</script>
