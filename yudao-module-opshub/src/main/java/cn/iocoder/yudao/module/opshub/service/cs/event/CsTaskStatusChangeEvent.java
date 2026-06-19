package cn.iocoder.yudao.module.opshub.service.cs.event;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.context.ApplicationEvent;

/**
 * 工单状态变更事件
 * <p>
 * 当工单业务状态发生变更时发布（DELIVERED / REJECTED / CLOSED 等）。
 * 各业务模块可通过 {@link CsTaskStatusChangeEventListener} 按需监听，
 * 按 category 过滤处理自己关心的工单分类。
 * <p>
 * 参考 {@link cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent} 模式。
 */
@SuppressWarnings("ALL")
@Data
@Accessors(chain = true)
public class CsTaskStatusChangeEvent extends ApplicationEvent {

    /**
     * 工单 ID
     */
    private Long taskId;
    /**
     * 工单编号
     */
    private String taskNo;
    /**
     * 工单分类
     *
     * @see cn.iocoder.yudao.module.opshub.enums.CsTaskCategoryEnum
     */
    private Integer category;
    /**
     * 来源模块
     *
     * @see cn.iocoder.yudao.module.opshub.enums.CsSourceModuleEnum
     */
    private String sourceModule;
    /**
     * 变更后的新状态
     *
     * @see cn.iocoder.yudao.module.opshub.enums.CsTaskStatusEnum
     */
    private Integer newStatus;
    /**
     * 经销商编码
     */
    private String dealerCode;
    /**
     * 产品线编码
     */
    private String productLineCode;

    public CsTaskStatusChangeEvent(Object source) {
        super(source);
    }

}
