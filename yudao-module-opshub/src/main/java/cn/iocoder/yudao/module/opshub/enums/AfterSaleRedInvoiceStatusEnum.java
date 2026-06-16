package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 售后红字发票状态枚举
 */
@Getter
@AllArgsConstructor
public enum AfterSaleRedInvoiceStatusEnum {

    NONE("none", "无"),
    PENDING("pending", "待开"),
    ISSUED("issued", "已开");

    /**
     * 状态编码
     */
    private final String code;
    /**
     * 状态中文名
     */
    private final String name;

    public static AfterSaleRedInvoiceStatusEnum getByCode(String code) {
        for (AfterSaleRedInvoiceStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

}
