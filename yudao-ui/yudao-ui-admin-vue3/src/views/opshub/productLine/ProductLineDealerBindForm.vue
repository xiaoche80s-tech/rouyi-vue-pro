<template>
  <Dialog v-model="dialogVisible" title="管理经销商" width="700px">
    <el-alert v-if="!productLineId" title="缺少产品线信息" type="error" />

    <!-- 已绑定经销商列表 -->
    <el-table v-loading="tableLoading" :data="boundDealers">
      <el-table-column align="center" label="经销商ID" prop="id" width="100" />
      <el-table-column align="center" label="名称" prop="name" />
      <el-table-column align="center" label="编码" prop="code" />
      <el-table-column align="center" label="联系人" prop="contactName" />
      <el-table-column align="center" label="操作" width="100">
        <template #default="scope">
          <el-button link type="danger" @click="handleUnbind(scope.row)">解绑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加经销商 -->
    <el-divider content-position="left">添加经销商</el-divider>
    <el-form :inline="true" class="mb-10px">
      <el-form-item label="经销商">
        <el-select
          v-model="selectedDealerId"
          class="!w-300px"
          filterable
          placeholder="请选择经销商"
        >
          <el-option
            v-for="item in availableDealers"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button :disabled="!selectedDealerId" type="primary" @click="handleBind">
          <Icon class="mr-5px" icon="ep:plus" />
          绑定
        </el-button>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="dialogVisible = false">关 闭</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import * as ProductLineApi from '@/api/opshub/productLine'
import * as DealerApi from '@/api/opshub/dealer'

defineOptions({ name: 'ProductLineDealerBindForm' })

const message = useMessage()

const dialogVisible = ref(false)
const tableLoading = ref(false)
const productLineId = ref<number | undefined>(undefined)
const boundDealers = ref<any[]>([])
const allDealers = ref<DealerApi.DealerSimpleVO[]>([])
const selectedDealerId = ref<number | undefined>(undefined)

/** 计算可选经销商（排除已绑定的） */
const availableDealers = computed(() => {
  const boundIds = new Set(boundDealers.value.map((d) => d.id))
  return allDealers.value.filter((d) => !boundIds.has(d.id))
})

/** 打开弹窗 */
const open = async (id: number) => {
  dialogVisible.value = true
  productLineId.value = id
  selectedDealerId.value = undefined
  await loadData()
}
defineExpose({ open })

/** 加载数据 */
const loadData = async () => {
  if (!productLineId.value) return
  tableLoading.value = true
  try {
    const [dealers, all] = await Promise.all([
      ProductLineApi.getProductLineDealers(productLineId.value),
      DealerApi.getSimpleDealerList()
    ])
    boundDealers.value = dealers
    allDealers.value = all
  } finally {
    tableLoading.value = false
  }
}

/** 绑定经销商 */
const handleBind = async () => {
  if (!productLineId.value || !selectedDealerId.value) return
  try {
    await ProductLineApi.bindDealer(productLineId.value, selectedDealerId.value)
    message.success('绑定成功')
    selectedDealerId.value = undefined
    await loadData()
  } catch {}
}

/** 解绑经销商 */
const handleUnbind = async (row: any) => {
  if (!productLineId.value) return
  try {
    await message.confirm(`确认解绑经销商「${row.name}」？`)
    await ProductLineApi.unbindDealer(productLineId.value, row.id)
    message.success('解绑成功')
    await loadData()
  } catch {}
}
</script>
