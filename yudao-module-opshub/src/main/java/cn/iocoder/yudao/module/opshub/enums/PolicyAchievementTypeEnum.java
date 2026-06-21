package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 政策达成类型枚举
 */
@Getter
@AllArgsConstructor
public enum PolicyAchievementTypeEnum {

    QUARTER("quarter", "季度政策"),
    MONTH("month", "月度政策");

    private final String code;
    private final String name;

    public static PolicyAchievementTypeEnum getByCode(String code) {
        for (PolicyAchievementTypeEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
