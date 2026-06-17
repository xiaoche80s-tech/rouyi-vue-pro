<script lang="tsx">
import { computed, defineComponent, unref, ref } from 'vue'
import { useAppStore } from '@/store/modules/app'
import { Backtop } from '@/components/Backtop'
import { Setting } from '@/layout/components/Setting'
import { useRenderLayout } from './components/useRenderLayout'
import { useDesign } from '@/hooks/web/useDesign'
import { getLayoutRenderMode } from '@/utils/layout'
import ChatFloatingButton from '@/components/CsChatWindow/ChatFloatingButton.vue'
import ChatWindow from '@/components/CsChatWindow/ChatWindow.vue'
import CsExecutorNotifier from '@/components/CsChatWindow/CsExecutorNotifier.vue'
import { useCsConsult } from '@/hooks/useCsConsult'
import { useUserStore } from '@/store/modules/user'

const { getPrefixCls } = useDesign()

const prefixCls = getPrefixCls('layout')

const appStore = useAppStore()

// 是否是移动端
const mobile = computed(() => appStore.getMobile)

// 菜单折叠
const collapse = computed(() => appStore.getCollapse)

const layout = computed(() => appStore.getLayout)

const handleClickOutside = () => {
  appStore.setCollapse(true)
}

const renderLayout = () => {
  switch (getLayoutRenderMode(unref(layout))) {
    case 'classic':
      const { renderClassic } = useRenderLayout()
      return renderClassic()
    case 'topLeft':
      const { renderTopLeft } = useRenderLayout()
      return renderTopLeft()
    case 'top':
      const { renderTop } = useRenderLayout()
      return renderTop()
    case 'cutMenu':
      const { renderCutMenu } = useRenderLayout()
      return renderCutMenu()
    default:
      break
  }
}

export default defineComponent({
  name: 'Layout',
  setup() {
    const { chatVisible, currentSessionId, openByCategory } = useCsConsult()
    const userStore = useUserStore()
    /** 浮动按钮仅经销商角色可见 */
    const isDealer = computed(() => userStore.getRoles.includes('dealer'))
    /** 执行员角色：全局通知组件可见 */
    const isExecutor = computed(() => userStore.getRoles.includes('service_executor') || userStore.getRoles.includes('brand_admin'))
    const unreadCount = ref(0)
    const handleFloatingClick = () => {
      openByCategory('other')
    }
    return () => (
      <section
        class={[
          prefixCls,
          `${prefixCls}__${layout.value}`,
          `${prefixCls}__${getLayoutRenderMode(layout.value)}`,
          'w-[100%] h-[100%] relative'
        ]}
      >
        {mobile.value && !collapse.value ? (
          <div
            class="absolute left-0 top-0 z-99 h-full w-full bg-[var(--el-color-black)] opacity-30"
            onClick={handleClickOutside}
          ></div>
        ) : undefined}

        {renderLayout()}

        <Backtop></Backtop>

        <Setting></Setting>

        {isDealer.value ? (
          <>
            <ChatFloatingButton unreadCount={unreadCount.value} onClick={handleFloatingClick} />
            <ChatWindow
              modelValue={chatVisible.value}
              onUpdate:modelValue={(v: boolean) => { chatVisible.value = v }}
              sessionId={currentSessionId.value}
              mode="dealer"
            />
          </>
        ) : undefined}

        {isExecutor.value ? <CsExecutorNotifier /> : undefined}
      </section>
    )
  }
})
</script>

<style lang="scss" scoped>
$prefix-cls: #{$namespace}-layout;

.#{$prefix-cls} {
  background-color: var(--app-content-bg-color);
}
</style>
