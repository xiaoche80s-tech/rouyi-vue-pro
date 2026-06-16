package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 咨询会话状态枚举
 *
 * 流转：待处理(0) → 处理中(1) → 已完成(2) / 已关闭(3)
 */
@Getter
@AllArgsConstructor
public enum CsSessionStatusEnum {

    PENDING(0, "待处理"),
    PROCESSING(1, "处理中"),
    COMPLETED(2, "已完成"),
    CLOSED(3, "已关闭");

    /**
     * 状态编码
     */
    private final Integer code;
    /**
     * 状态中文名
     */
    private final String name;

    public static CsSessionStatusEnum getByCode(Integer code) {
        for (CsSessionStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

}
