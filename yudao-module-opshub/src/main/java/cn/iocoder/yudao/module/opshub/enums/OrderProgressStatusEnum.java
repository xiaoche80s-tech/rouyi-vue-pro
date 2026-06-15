package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单进度状态枚举
 */
@Getter
@AllArgsConstructor
public enum OrderProgressStatusEnum {

    PENDING("pending", "待确认"),
    CONFIRMED("confirmed", "已确认"),
    SHIPPED("shipped", "已发货"),
    SIGNED("signed", "已签收"),
    COMPLETED("completed", "已完成");

    /**
     * 状态编码
     */
    private final String code;
    /**
     * 状态中文名
     */
    private final String name;

    public static OrderProgressStatusEnum getByCode(String code) {
        for (OrderProgressStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

}
