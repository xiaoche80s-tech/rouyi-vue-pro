<template>
  <ContentWrap>
    <el-form :inline="true" :model="filters" class="-mb-15px" label-width="80px">
      <!-- 年度 -->
      <el-form-item label="年度">
        <el-select v-model="filters.targetYear" class="!w-120px" :clearable="false" placeholder="请选择">
          <el-option v-for="y in yearOptions" :key="y" :label="y + '年'" :value="y" />
        </el-select>
      </el-form-item>

      <!-- 政策类型 -->
      <el-form-item label="政策类型">
        <el-select v-model="filters.policyTypes" class="!w-160px" clearable multiple collapse-tags placeholder="全部">
          <el-option label="返利" value="rebate" />
          <el-option label="促销" value="promotion" />
          <el-option label="其他" value="other" />
        </el-select>
      </el-form-item>

      <!-- 达成类型 -->
      <el-form-item label="达成类型">
        <el-select v-model="filters.achievementTypes" class="!w-160px" clearable multiple collapse-tags placeholder="全部">
          <el-option label="季度政策" value="quarter" />
          <el-option label="月度政策" value="month" />
        </el-select>
      </el-form-item>

      <!-- 指标类型 -->
      <el-form-item label="指标类型">
        <el-select v-model="filters.indicatorNames" class="!w-200px" clearable multiple collapse-tags placeholder="全部">
          <el-option v-for="name in indicatorNameOptions" :key="name" :label="name" :value="name" />
        </el-select>
      </el-form-item>

      <!-- 月度 -->
      <el-form-item label="月度">
        <el-select v-model="filters.months" class="!w-200px" clearable multiple collapse-tags placeholder="全部">
          <el-option v-for="m in 12" :key="m" :label="m + '月'" :value="m" />
        </el-select>
      </el-form-item>

      <!-- 产品线 -->
      <el-form-item label="产品线">
        <el-select v-model="filters.productLineCodes" class="!w-160px" clearable multiple collapse-tags placeholder="全部">
          <el-option v-for="pl in productLineList" :key="pl.productLineCode" :label="pl.productLineName" :value="pl.productLineCode" />
        </el-select>
      </el-form-item>

      <!-- 经销商 -->
      <el-form-item v-hasPermi="['dealer:policy:create']" label="经销商">
        <el-select v-model="filters.dealerIds" class="!w-160px" clearable multiple collapse-tags placeholder="全部">
          <el-option v-for="d in dealerList" :key="d.id" :label="d.dealerName" :value="d.id" />
        </el-select>
      </el-form-item>

      <!-- 政策编码 -->
      <el-form-item label="政策编码">
        <el-input
          v-model="filters.policyCode"
          class="!w-200px"
          clearable
          placeholder="如 POL-2026-001"
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
  indicatorNameOptions: string[]
}>()

const currentYear = new Date().getFullYear()
const yearOptions = Array.from({ length: 7 }, (_, i) => currentYear - 3 + i)

const emit = defineEmits<{
  (e: 'search', filters: any): void
  (e: 'reset'): void
}>()

const filters = reactive({
  targetYear: currentYear as number | undefined,
  policyTypes: undefined as string[] | undefined,
  achievementTypes: undefined as string[] | undefined,
  indicatorNames: undefined as string[] | undefined,
  months: undefined as number[] | undefined,
  productLineCodes: undefined as string[] | undefined,
  dealerIds: undefined as number[] | undefined,
  policyCode: ''
})

const handleSearch = () => {
  emit('search', { ...filters })
}

const handleReset = () => {
  filters.targetYear = currentYear
  filters.policyTypes = undefined
  filters.achievementTypes = undefined
  filters.indicatorNames = undefined
  filters.months = undefined
  filters.productLineCodes = undefined
  filters.dealerIds = undefined
  filters.policyCode = ''
  emit('reset')
}
</script>
