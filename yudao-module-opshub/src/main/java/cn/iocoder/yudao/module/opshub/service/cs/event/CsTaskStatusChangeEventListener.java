package cn.iocoder.yudao.module.opshub.service.cs.event;

import org.springframework.context.ApplicationListener;

/**
 * 工单状态变更事件的抽象监听器
 * <p>
 * 各业务模块继承此类并注册为 Spring Bean，即可按 category 过滤监听工单状态变更事件。
 * <p>
 * 示例：
 * <pre>
 * &#64;Component
 * public class SigningTaskStatusListener extends CsTaskStatusChangeEventListener {
 *     &#64;Override
 *     protected Integer getCategory() {
 *         return CsTaskCategoryEnum.SIGNING.getCode(); // 仅关心签约工单
 *     }
 *     &#64;Override
 *     protected void onStatusChange(CsTaskStatusChangeEvent event) {
 *         if (CsTaskStatusEnum.CLOSED.getCode().equals(event.getNewStatus())) {
 *             // 签约工单验收通过，更新签约进度...
 *         }
 *     }
 * }
 * </pre>
 * <p>
 * 参考 {@link cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEventListener} 模式。
 */
public abstract class CsTaskStatusChangeEventListener implements ApplicationListener<CsTaskStatusChangeEvent> {

    @Override
    public final void onApplicationEvent(CsTaskStatusChangeEvent event) {
        // 子类可覆盖 getCategory() 过滤关心的分类；返回 null 表示监听所有分类
        Integer targetCategory = getCategory();
        if (targetCategory != null && !targetCategory.equals(event.getCategory())) {
            return;
        }
        onStatusChange(event);
    }

    /**
     * 返回监听的工单分类编码。返回 null 表示监听所有分类。
     *
     * @see cn.iocoder.yudao.module.opshub.enums.CsTaskCategoryEnum
     */
    protected abstract Integer getCategory();

    /**
     * 处理工单状态变更事件
     *
     * @param event 事件
     */
    protected abstract void onStatusChange(CsTaskStatusChangeEvent event);

}
