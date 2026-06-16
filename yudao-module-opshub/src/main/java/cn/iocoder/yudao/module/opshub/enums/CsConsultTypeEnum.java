package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 咨询类型枚举
 */
@Getter
@AllArgsConstructor
public enum CsConsultTypeEnum {

    SIGNING("signing", "签约咨询"),
    POLICY("policy", "政策咨询"),
    AFTERSALE("aftersale", "售后咨询"),
    ORDER("order", "订单咨询"),
    BASEDATA("basedata", "基础数据咨询"),
    OTHER("other", "其他咨询");

    /**
     * 类型编码
     */
    private final String code;
    /**
     * 类型中文名
     */
    private final String name;

    public static CsConsultTypeEnum getByCode(String code) {
        for (CsConsultTypeEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

}
