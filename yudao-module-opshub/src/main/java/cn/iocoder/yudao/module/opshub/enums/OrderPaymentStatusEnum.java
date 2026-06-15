package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 付款记录状态枚举（ops_order_payment.status）
 */
@Getter
@AllArgsConstructor
public enum OrderPaymentStatusEnum {

    PENDING("pending", "审批中"),
    APPROVED("approved", "已通过"),
    REJECTED("rejected", "已拒绝");

    /**
     * 状态编码
     */
    private final String code;
    /**
     * 状态中文名
     */
    private final String name;

    public static OrderPaymentStatusEnum getByCode(String code) {
        for (OrderPaymentStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

}
