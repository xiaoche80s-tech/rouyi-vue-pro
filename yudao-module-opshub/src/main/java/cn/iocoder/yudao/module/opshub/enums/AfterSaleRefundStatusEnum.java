package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 售后退款状态枚举
 */
@Getter
@AllArgsConstructor
public enum AfterSaleRefundStatusEnum {

    NONE("none", "无"),
    PENDING("pending", "退款中"),
    REFUNDED("refunded", "已退款");

    /**
     * 状态编码
     */
    private final String code;
    /**
     * 状态中文名
     */
    private final String name;

    public static AfterSaleRefundStatusEnum getByCode(String code) {
        for (AfterSaleRefundStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

}
