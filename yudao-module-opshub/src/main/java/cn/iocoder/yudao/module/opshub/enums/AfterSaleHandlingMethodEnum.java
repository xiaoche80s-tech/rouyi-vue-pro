package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 售后处理方式枚举
 */
@Getter
@AllArgsConstructor
public enum AfterSaleHandlingMethodEnum {

    RETURN("return", "退货"),
    EXCHANGE("exchange", "退换货"),
    RETURN_REFUND("return_refund", "退货退款");

    /**
     * 编码
     */
    private final String code;
    /**
     * 中文名
     */
    private final String name;

    public static AfterSaleHandlingMethodEnum getByCode(String code) {
        for (AfterSaleHandlingMethodEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

}
