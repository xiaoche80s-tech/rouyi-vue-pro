package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 客服工单紧急程度枚举
 */
@Getter
@AllArgsConstructor
public enum CsTaskUrgencyEnum {

    URGENT(0, "紧急"),
    HIGH(1, "高"),
    MEDIUM(2, "中"),
    LOW(3, "低");

    /**
     * 编码
     */
    private final Integer code;
    /**
     * 中文名
     */
    private final String name;

    public static CsTaskUrgencyEnum getByCode(Integer code) {
        for (CsTaskUrgencyEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

}
