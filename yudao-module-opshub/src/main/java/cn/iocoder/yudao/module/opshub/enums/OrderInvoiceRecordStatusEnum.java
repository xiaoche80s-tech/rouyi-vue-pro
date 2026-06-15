package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 开票记录状态枚举（ops_order_invoice.status）
 */
@Getter
@AllArgsConstructor
public enum OrderInvoiceRecordStatusEnum {

    PENDING("pending", "待开票"),
    INVOICED("invoiced", "已开票");

    /**
     * 状态编码
     */
    private final String code;
    /**
     * 状态中文名
     */
    private final String name;

    public static OrderInvoiceRecordStatusEnum getByCode(String code) {
        for (OrderInvoiceRecordStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

}
