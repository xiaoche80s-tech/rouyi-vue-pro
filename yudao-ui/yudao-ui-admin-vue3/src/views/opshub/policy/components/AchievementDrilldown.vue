<template>
  <el-dialog v-model="visible" title="达成明细下钻" width="650px" destroy-on-close>
    <div v-loading="loading">
      <!-- 面包屑导航 -->
      <el-breadcrumb separator="/" class="mb-16px">
        <el-breadcrumb-item>
          <el-link :underline="false" @click="drillToLevel('province')">{{ indicatorName }}</el-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item v-if="currentProvince">
          <el-link :underline="false" @click="drillToLevel('hospital')">{{ currentProvince }}</el-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item v-if="currentHospital">
          <el-link :underline="false" @click="drillToLevel('product')">{{ currentHospital }}</el-link>
        </el-breadcrumb-item>
      </el-breadcrumb>

      <!-- 当前层级标签 -->
      <div class="mb-8px text-12px text-gray-500">
        当前层级：{{ levelLabel }}
      </div>

      <!-- 数据列表 -->
      <el-table :data="items" size="small" stripe>
        <el-table-column prop="name" :label="nameLabel" min-width="200">
          <template #default="{ row }">
            <el-link
              v-if="canDrill"
              type="primary"
              :underline="false"
              @click="handleDrill(row)"
            >
              {{ row.name }}
            </el-link>
            <span v-else>{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column label="达成值" width="150" align="right">
          <template #default="{ row }">{{ row.achievedValue?.toLocaleString() }}</template>
        </el-table-column>
      </el-table>

      <el-empty v-if="items.length === 0 && !loading" description="暂无达成明细数据" />
    </div>
  </el-dialog>
</template>

<script lang="ts" setup>
import type { IndicatorVO, AchievementVO } from '@/api/opshub/policy'
import * as PolicyApi from '@/api/opshub/policy'

const visible = ref(false)
const loading = ref(false)
const items = ref<AchievementVO[]>([])
const indicatorId = ref(0)
const indicatorName = ref('')
const targetYear = ref(new Date().getFullYear())
const currentLevel = ref('province')
const currentProvince = ref('')
const currentHospital = ref('')

const levelLabel = computed(() => {
  const map: Record<string, string> = { province: '省级', hospital: '医院级', product: '产品级' }
  return map[currentLevel.value] || currentLevel.value
})

const nameLabel = computed(() => {
  const map: Record<string, string> = { province: '省份', hospital: '医院', product: '产品' }
  return map[currentLevel.value] || '名称'
})

const canDrill = computed(() => currentLevel.value !== 'product')

const loadData = async () => {
  loading.value = true
  try {
    const params: any = {
      targetYear: targetYear.value,
      indicatorId: indicatorId.value,
      level: currentLevel.value
    }
    if (currentLevel.value === 'hospital' || currentLevel.value === 'product') {
      params.province = currentProvince.value
    }
    if (currentLevel.value === 'product') {
      params.hospital = currentHospital.value
    }
    items.value = await PolicyApi.getAchievementDrilldown(params)
  } finally {
    loading.value = false
  }
}

const handleDrill = (row: AchievementVO) => {
  if (currentLevel.value === 'province') {
    currentProvince.value = row.name
    currentLevel.value = 'hospital'
  } else if (currentLevel.value === 'hospital') {
    currentHospital.value = row.name
    currentLevel.value = 'product'
  }
  loadData()
}

const drillToLevel = (level: string) => {
  if (level === 'province') {
    currentLevel.value = 'province'
    currentProvince.value = ''
    currentHospital.value = ''
  } else if (level === 'hospital') {
    currentLevel.value = 'hospital'
    currentHospital.value = ''
  }
  loadData()
}

const open = (indicator: IndicatorVO, year: number) => {
  indicatorId.value = indicator.id
  indicatorName.value = indicator.indicatorName
  targetYear.value = year
  currentLevel.value = 'province'
  currentProvince.value = ''
  currentHospital.value = ''
  visible.value = true
  loadData()
}

defineExpose({ open })
</script>
