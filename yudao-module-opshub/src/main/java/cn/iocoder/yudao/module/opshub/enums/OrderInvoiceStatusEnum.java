package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单开票状态枚举
 */
@Getter
@AllArgsConstructor
public enum OrderInvoiceStatusEnum {

    UNINVOICED("uninvoiced", "未开票"),
    PARTIAL("partial", "部分开票"),
    INVOICED("invoiced", "已开票");

    /**
     * 状态编码
     */
    private final String code;
    /**
     * 状态中文名
     */
    private final String name;

    public static OrderInvoiceStatusEnum getByCode(String code) {
        for (OrderInvoiceStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

}
