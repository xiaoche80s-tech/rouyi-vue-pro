package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作请求类型枚举
 */
@Getter
@AllArgsConstructor
public enum CsOpReqTypeEnum {

    SIGN("sign", "签约"),
    INVOICE("invoice", "开票"),
    STAMP("stamp", "盖章"),
    AFTERSALE("aftersale", "售后");

    /**
     * 类型编码
     */
    private final String code;
    /**
     * 类型中文名
     */
    private final String name;

    public static CsOpReqTypeEnum getByCode(String code) {
        for (CsOpReqTypeEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

}
