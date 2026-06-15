package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 合同子状态枚举（仅 unsigned 时有效）
 */
@Getter
@AllArgsConstructor
public enum ContractSubStatusEnum {

    PENDING("pending", "待签署"),
    SIGNING("signing", "签署中");

    /**
     * 子状态编码
     */
    private final String code;
    /**
     * 子状态中文名
     */
    private final String name;

    public static ContractSubStatusEnum getByCode(String code) {
        for (ContractSubStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

}
