package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单付款状态枚举
 */
@Getter
@AllArgsConstructor
public enum OrderPayStatusEnum {

    UNPAID("unpaid", "未付款"),
    PAID("paid", "已付款");

    /**
     * 状态编码
     */
    private final String code;
    /**
     * 状态中文名
     */
    private final String name;

    public static OrderPayStatusEnum getByCode(String code) {
        for (OrderPayStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

}
