package cn.iocoder.yudao.module.opshub.service.cs.event;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link CsTaskStatusChangeEventListener} 单元测试
 * <p>
 * 验证 category 过滤机制和事件分发逻辑
 */
class CsTaskStatusChangeEventListenerTest {

    @Test
    @DisplayName("category 匹配时调用 onStatusChange")
    void testCategoryMatch() {
        // 准备：监听 category=1 的监听器
        TestListener listener = new TestListener(1);
        CsTaskStatusChangeEvent event = buildEvent(1, 2); // category=1, newStatus=DELIVERED

        listener.onApplicationEvent(event);

        assertThat(listener.handled).isTrue();
        assertThat(listener.receivedStatus).isEqualTo(2);
    }

    @Test
    @DisplayName("category 不匹配时不调用 onStatusChange")
    void testCategoryMismatch() {
        TestListener listener = new TestListener(1); // 仅监听 category=1
        CsTaskStatusChangeEvent event = buildEvent(2, 2); // category=2

        listener.onApplicationEvent(event);

        assertThat(listener.handled).isFalse();
    }

    @Test
    @DisplayName("getCategory 返回 null 时监听所有 category")
    void testNullCategoryListenAll() {
        TestListener listener = new TestListener(null); // null = 监听所有
        CsTaskStatusChangeEvent event = buildEvent(5, 3);

        listener.onApplicationEvent(event);

        assertThat(listener.handled).isTrue();
    }

    @Test
    @DisplayName("多个监听器各自独立过滤")
    void testMultipleListenersIndependent() {
        TestListener listener1 = new TestListener(1);
        TestListener listener2 = new TestListener(2);

        CsTaskStatusChangeEvent eventCat1 = buildEvent(1, 2);
        CsTaskStatusChangeEvent eventCat2 = buildEvent(2, 3);

        listener1.onApplicationEvent(eventCat1);
        listener1.onApplicationEvent(eventCat2);
        listener2.onApplicationEvent(eventCat1);
        listener2.onApplicationEvent(eventCat2);

        assertThat(listener1.handled).isTrue();
        assertThat(listener1.receivedStatus).isEqualTo(2); // 仅处理了 cat=1 的事件
        assertThat(listener2.handled).isTrue();
        assertThat(listener2.receivedStatus).isEqualTo(3); // 仅处理了 cat=2 的事件
    }

    // ========== 辅助方法 ==========

    private CsTaskStatusChangeEvent buildEvent(Integer category, Integer newStatus) {
        return new CsTaskStatusChangeEvent(this)
                .setTaskId(1L)
                .setTaskNo("TASK-20260617-001")
                .setCategory(category)
                .setNewStatus(newStatus)
                .setSourceModule("test")
                .setDealerCode("D001")
                .setProductLineCode("PL001");
    }

    /**
     * 测试用监听器
     */
    static class TestListener extends CsTaskStatusChangeEventListener {

        private final Integer category;
        boolean handled = false;
        Integer receivedStatus;

        TestListener(Integer category) {
            this.category = category;
        }

        @Override
        protected Integer getCategory() {
            return category;
        }

        @Override
        protected void onStatusChange(CsTaskStatusChangeEvent event) {
            handled = true;
            receivedStatus = event.getNewStatus();
        }
    }

}
