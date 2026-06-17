<template>
  <el-drawer v-model="dialogVisible" title="管理经销商" size="700px" direction="rtl">
    <el-alert v-if="!productLineCode" title="缺少产品线信息" type="error" />

    <!-- 已绑定经销商列表 -->
    <el-table v-loading="tableLoading" :data="boundDealers">
      <el-table-column align="center" label="经销商ID" prop="id" width="100" />
      <el-table-column align="center" label="名称" prop="dealerName" />
      <el-table-column align="center" label="编码" prop="dealerCode" />
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
          v-model="selectedDealerCode"
          class="!w-300px"
          filterable
          placeholder="请选择经销商"
        >
          <el-option
            v-for="item in availableDealers"
            :key="item.id"
            :label="item.dealerName"
            :value="item.dealerCode"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button :disabled="!selectedDealerCode" type="primary" @click="handleBind">
          <Icon class="mr-5px" icon="ep:plus" />
          绑定
        </el-button>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="dialogVisible = false">关 闭</el-button>
    </template>
  </el-drawer>
</template>

<script lang="ts" setup>
import * as ProductLineApi from '@/api/opshub/productLine'
import * as DealerApi from '@/api/opshub/dealer'

defineOptions({ name: 'ProductLineDealerBindForm' })

const message = useMessage()

const dialogVisible = ref(false)
const tableLoading = ref(false)
const productLineCode = ref<string | undefined>(undefined)
const boundDealers = ref<any[]>([])
const allDealers = ref<DealerApi.DealerSimpleVO[]>([])
const selectedDealerCode = ref<string | undefined>(undefined)

/** 计算可选经销商（排除已绑定的） */
const availableDealers = computed(() => {
  const boundCodes = new Set(boundDealers.value.map((d) => d.dealerCode))
  return allDealers.value.filter((d) => !boundCodes.has(d.dealerCode))
})

/** 打开抽屉 */
const open = async (code: string) => {
  dialogVisible.value = true
  productLineCode.value = code
  selectedDealerCode.value = undefined
  await loadData()
}
defineExpose({ open })

/** 加载数据 */
const loadData = async () => {
  if (!productLineCode.value) return
  tableLoading.value = true
  try {
    const [dealers, all] = await Promise.all([
      ProductLineApi.getProductLineDealers(productLineCode.value),
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
  if (!productLineCode.value || !selectedDealerCode.value) return
  try {
    await ProductLineApi.bindDealer(productLineCode.value, selectedDealerCode.value)
    message.success('绑定成功')
    selectedDealerCode.value = undefined
    await loadData()
  } catch {}
}

/** 解绑经销商 */
const handleUnbind = async (row: any) => {
  if (!productLineCode.value) return
  try {
    await message.confirm(`确认解绑经销商「${row.dealerName}」？`)
    await ProductLineApi.unbindDealer(productLineCode.value, row.dealerCode)
    message.success('解绑成功')
    await loadData()
  } catch {}
}
</script>
