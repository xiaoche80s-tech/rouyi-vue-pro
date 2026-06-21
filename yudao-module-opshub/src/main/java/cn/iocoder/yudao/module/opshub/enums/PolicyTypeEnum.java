package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 政策类型枚举
 */
@Getter
@AllArgsConstructor
public enum PolicyTypeEnum {

    REBATE("rebate", "返利"),
    PROMOTION("promotion", "促销"),
    OTHER("other", "其他");

    private final String code;
    private final String name;

    public static PolicyTypeEnum getByCode(String code) {
        for (PolicyTypeEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
