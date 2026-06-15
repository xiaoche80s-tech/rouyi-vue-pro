<template>
  <ContentWrap>
    <el-form :inline="true" :model="filters" class="-mb-15px" label-width="80px">
      <!-- 快捷时间 -->
      <el-form-item label="快捷时间">
        <el-checkbox-group v-model="quickTimeValue" @change="handleQuickTimeChange">
          <el-checkbox-button label="today">今日</el-checkbox-button>
          <el-checkbox-button label="week">本周</el-checkbox-button>
          <el-checkbox-button label="month">本月</el-checkbox-button>
          <el-checkbox-button label="all">全部</el-checkbox-button>
        </el-checkbox-group>
      </el-form-item>

      <!-- 季度 -->
      <el-form-item label="季度">
        <el-select v-model="filters.quarters" class="!w-160px" clearable multiple collapse-tags placeholder="全部">
          <el-option label="Q1" :value="1" />
          <el-option label="Q2" :value="2" />
          <el-option label="Q3" :value="3" />
          <el-option label="Q4" :value="4" />
        </el-select>
      </el-form-item>

      <!-- 月度 -->
      <el-form-item label="月度">
        <el-select v-model="filters.months" class="!w-160px" clearable multiple collapse-tags placeholder="全部">
          <el-option v-for="m in 12" :key="m" :label="m + '月'" :value="m" />
        </el-select>
      </el-form-item>

      <!-- 产品线 -->
      <el-form-item label="产品线">
        <el-select v-model="filters.productLineCodes" class="!w-160px" clearable multiple collapse-tags placeholder="全部">
          <el-option v-for="pl in productLineList" :key="pl.productLineCode" :label="pl.productLineName" :value="pl.productLineCode" />
        </el-select>
      </el-form-item>

      <!-- 经销商（非经销商角色可见） -->
      <el-form-item v-if="checkPermi(['dealer:order:update'])" label="经销商">
        <el-select v-model="filters.dealerCodes" class="!w-160px" clearable multiple collapse-tags placeholder="全部">
          <el-option v-for="d in dealerList" :key="d.dealerCode" :label="d.dealerName" :value="d.dealerCode" />
        </el-select>
      </el-form-item>

      <!-- 进度 -->
      <el-form-item label="进度">
        <el-select v-model="filters.progressStatus" class="!w-120px" clearable placeholder="全部">
          <el-option label="待确认" value="pending" />
          <el-option label="已确认" value="confirmed" />
          <el-option label="已发货" value="shipped" />
          <el-option label="已签收" value="signed" />
          <el-option label="已完成" value="completed" />
        </el-select>
      </el-form-item>

      <!-- 付款 -->
      <el-form-item label="付款">
        <el-select v-model="filters.payStatus" class="!w-100px" clearable placeholder="全部">
          <el-option label="未付款" value="unpaid" />
          <el-option label="已付款" value="paid" />
        </el-select>
      </el-form-item>

      <!-- 开票 -->
      <el-form-item label="开票">
        <el-select v-model="filters.invStatus" class="!w-120px" clearable placeholder="全部">
          <el-option label="未开票" value="uninvoiced" />
          <el-option label="部分开票" value="partial" />
          <el-option label="已开票" value="invoiced" />
        </el-select>
      </el-form-item>

      <!-- 搜索 -->
      <el-form-item label="搜索">
        <el-input v-model="filters.keyword" class="!w-200px" clearable placeholder="订单号/产品线/经销商" @keyup.enter="handleSearch" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleSearch">
          <Icon class="mr-5px" icon="ep:search" />搜索
        </el-button>
        <el-button @click="handleReset">
          <Icon class="mr-5px" icon="ep:refresh" />重置
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>
</template>

<script lang="ts" setup>
import { checkPermi } from '@/utils/permission'

const props = defineProps<{
  dealerList: any[]
  productLineList: any[]
}>()

const emit = defineEmits<{
  (e: 'search', filters: any): void
  (e: 'reset'): void
}>()

const quickTimeValue = ref<string[]>(['all'])

const filters = reactive({
  quickTime: 'all' as string,
  quarters: undefined as number[] | undefined,
  months: undefined as number[] | undefined,
  productLineCodes: undefined as string[] | undefined,
  dealerCodes: undefined as string[] | undefined,
  progressStatus: undefined as string | undefined,
  payStatus: undefined as string | undefined,
  invStatus: undefined as string | undefined,
  keyword: ''
})

const handleQuickTimeChange = (val: string[]) => {
  // 互斥单选行为：取最后一个选中的
  if (val.length > 0) {
    const last = val[val.length - 1]
    quickTimeValue.value = [last]
    filters.quickTime = last
  } else {
    quickTimeValue.value = ['all']
    filters.quickTime = 'all'
  }
}

const handleSearch = () => {
  emit('search', { ...filters })
}

const handleReset = () => {
  quickTimeValue.value = ['all']
  filters.quickTime = 'all'
  filters.quarters = undefined
  filters.months = undefined
  filters.productLineCodes = undefined
  filters.dealerCodes = undefined
  filters.progressStatus = undefined
  filters.payStatus = undefined
  filters.invStatus = undefined
  filters.keyword = ''
  emit('reset')
}
</script>
