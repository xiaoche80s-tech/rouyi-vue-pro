package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作请求类型枚举
 */
@Getter
@AllArgsConstructor
public enum OpRequestTypeEnum {

    SIGNING("signing", "申请签约"),
    PAYMENT("payment", "申请付款"),
    INVOICE("invoice", "申请开票"),
    RETURN("return", "申请退货");

    /**
     * 类型编码
     */
    private final String code;
    /**
     * 类型中文名
     */
    private final String name;

    public static OpRequestTypeEnum getByCode(String code) {
        for (OpRequestTypeEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

}
