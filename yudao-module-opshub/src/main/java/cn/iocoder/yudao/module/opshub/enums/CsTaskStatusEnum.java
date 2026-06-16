package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 客服工单状态枚举
 *
 * 流转：待接单(0) → 处理中(1) → 已交付(2) → 已关闭(3)
 *                                       ↘ 已退回(4) → 处理中(1)
 */
@Getter
@AllArgsConstructor
public enum CsTaskStatusEnum {

    PENDING(0, "待接单"),
    IN_PROGRESS(1, "处理中"),
    DELIVERED(2, "已交付"),
    CLOSED(3, "已关闭"),
    REJECTED(4, "已退回");

    /**
     * 状态编码
     */
    private final Integer code;
    /**
     * 状态中文名
     */
    private final String name;

    public static CsTaskStatusEnum getByCode(Integer code) {
        for (CsTaskStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

}
