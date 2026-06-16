package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 咨询消息发送人角色枚举
 */
@Getter
@AllArgsConstructor
public enum CsSenderRoleEnum {

    DEALER("dealer", "经销商"),
    EXECUTOR("executor", "执行员"),
    ADMIN("admin", "管理员"),
    SYSTEM("system", "系统");

    /**
     * 角色编码
     */
    private final String code;
    /**
     * 角色中文名
     */
    private final String name;

    public static CsSenderRoleEnum getByCode(String code) {
        for (CsSenderRoleEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

}
