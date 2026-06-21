package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 政策状态枚举
 */
@Getter
@AllArgsConstructor
public enum PolicyStatusEnum {

    EXECUTING("executing", "执行中"),
    PENDING("pending", "待执行"),
    COMPLETED("completed", "已完成");

    private final String code;
    private final String name;

    public static PolicyStatusEnum getByCode(String code) {
        for (PolicyStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
