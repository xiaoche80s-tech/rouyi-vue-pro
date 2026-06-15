<template>
  <ContentWrap>
    <el-form :inline="true" :model="filters" class="-mb-15px" label-width="80px">
      <!-- 时间维度 -->
      <el-form-item label="时间维度">
        <el-select v-model="filters.timeDimension" class="!w-120px" clearable placeholder="全部" @change="handleTimeDimensionChange">
          <el-option label="月度" value="month" />
          <el-option label="季度" value="quarter" />
          <el-option label="年度" value="year" />
        </el-select>
      </el-form-item>

      <!-- 月份/季度/年份 -->
      <el-form-item v-if="filters.timeDimension === 'month'" label="月份">
        <el-select v-model="filters.months" class="!w-200px" clearable multiple collapse-tags placeholder="选择月份">
          <el-option v-for="m in 12" :key="m" :label="m + '月'" :value="m" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="filters.timeDimension === 'quarter'" label="季度">
        <el-select v-model="filters.quarters" class="!w-200px" clearable multiple collapse-tags placeholder="选择季度">
          <el-option label="Q1" :value="1" />
          <el-option label="Q2" :value="2" />
          <el-option label="Q3" :value="3" />
          <el-option label="Q4" :value="4" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="filters.timeDimension === 'year'" label="年份">
        <el-select v-model="filters.years" class="!w-200px" clearable multiple collapse-tags placeholder="选择年份">
          <el-option v-for="y in yearOptions" :key="y" :label="y + '年'" :value="y" />
        </el-select>
      </el-form-item>

      <!-- 产品线 -->
      <el-form-item label="产品线">
        <el-select v-model="filters.productLineCodes" class="!w-160px" clearable multiple collapse-tags placeholder="全部">
          <el-option v-for="pl in productLineList" :key="pl.productLineCode" :label="pl.productLineName" :value="pl.productLineCode" />
        </el-select>
      </el-form-item>

      <!-- 合同类型 -->
      <el-form-item label="合同类型">
        <el-select v-model="filters.contractTypes" class="!w-160px" clearable multiple collapse-tags placeholder="全部">
          <el-option label="主合同" value="main" />
          <el-option label="政策合同" value="policy" />
          <el-option label="补充协议" value="supplement" />
          <el-option label="终止协议" value="termination" />
        </el-select>
      </el-form-item>

      <!-- 状态 -->
      <el-form-item label="状态">
        <el-select v-model="filters.statuses" class="!w-140px" clearable multiple collapse-tags placeholder="全部">
          <el-option label="已签署" value="signed" />
          <el-option label="未签署" value="unsigned" />
        </el-select>
      </el-form-item>

      <!-- 经销商（仅管理员可见） -->
      <el-form-item v-hasPermi="['dealer:signing:create']" label="经销商">
        <el-select v-model="filters.dealerCodes" class="!w-160px" clearable multiple collapse-tags placeholder="全部">
          <el-option v-for="d in dealerList" :key="d.dealerCode" :label="d.dealerName" :value="d.dealerCode" />
        </el-select>
      </el-form-item>

      <!-- 搜索 -->
      <el-form-item label="搜索">
        <el-input
          v-model="filters.keyword"
          class="!w-200px"
          clearable
          placeholder="编码/名称/经销商"
          @keyup.enter="handleSearch"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleSearch">
          <Icon class="mr-5px" icon="ep:search" />
          搜索
        </el-button>
        <el-button @click="handleReset">
          <Icon class="mr-5px" icon="ep:refresh" />
          重置
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>
</template>

<script lang="ts" setup>
const props = defineProps<{
  dealerList: any[]
  productLineList: any[]
}>()

const emit = defineEmits<{
  (e: 'search', filters: any): void
  (e: 'reset'): void
}>()

const currentYear = new Date().getFullYear()
const yearOptions = computed(() => {
  const years: number[] = []
  for (let y = currentYear - 2; y <= currentYear + 1; y++) {
    years.push(y)
  }
  return years
})

const filters = reactive({
  timeDimension: undefined as string | undefined,
  months: undefined as number[] | undefined,
  quarters: undefined as number[] | undefined,
  years: undefined as number[] | undefined,
  productLineCodes: undefined as string[] | undefined,
  contractTypes: undefined as string[] | undefined,
  statuses: undefined as string[] | undefined,
  dealerCodes: undefined as string[] | undefined,
  keyword: ''
})

const handleTimeDimensionChange = () => {
  filters.months = undefined
  filters.quarters = undefined
  filters.years = undefined
}

const handleSearch = () => {
  emit('search', { ...filters })
}

const handleReset = () => {
  filters.timeDimension = undefined
  filters.months = undefined
  filters.quarters = undefined
  filters.years = undefined
  filters.productLineCodes = undefined
  filters.contractTypes = undefined
  filters.statuses = undefined
  filters.dealerCodes = undefined
  filters.keyword = ''
  emit('reset')
}
</script>
