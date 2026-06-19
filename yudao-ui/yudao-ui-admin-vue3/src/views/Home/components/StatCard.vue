<template>
  <el-card
    shadow="hover"
    class="stat-card"
    :class="{ 'stat-card--clickable': route }"
    :body-style="{ padding: '16px 20px' }"
    @click="handleClick"
  >
    <div class="flex items-center justify-between">
      <div class="flex-1">
        <div class="text-13px text-gray-500 mb-8px">{{ title }}</div>
        <div class="text-24px font-700" :style="{ color: color }">
          <CountTo :end-val="value" :start-val="0" :duration="1500" :separator="','" />
          <span v-if="suffix" class="text-14px font-400 text-gray-400 ml-4px">{{ suffix }}</span>
        </div>
        <div v-if="subtitle" class="text-12px text-gray-400 mt-4px">{{ subtitle }}</div>
      </div>
      <div
        class="stat-icon flex items-center justify-center rounded-8px"
        :style="{ backgroundColor: bgColor, width: '48px', height: '48px' }"
      >
        <Icon :icon="icon" :size="24" :color="color" />
      </div>
    </div>
  </el-card>
</template>

<script lang="ts" setup>
import { CountTo } from '@/components/CountTo'

defineOptions({ name: 'StatCard' })

const router = useRouter()

const props = withDefaults(
  defineProps<{
    title: string
    value: number
    icon: string
    color: string
    bgColor?: string
    suffix?: string
    subtitle?: string
    route?: string
  }>(),
  {
    bgColor: undefined,
    suffix: '',
    subtitle: '',
    route: undefined
  }
)

const handleClick = () => {
  if (props.route) router.push(props.route)
}
</script>

<style scoped>
.stat-card {
  transition: transform 0.2s ease;
}
.stat-card:hover {
  transform: translateY(-2px);
}
.stat-card--clickable {
  cursor: pointer;
}
.stat-card--clickable:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
</style>
