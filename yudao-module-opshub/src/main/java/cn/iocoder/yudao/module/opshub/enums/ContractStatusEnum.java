package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 合同签署状态枚举
 */
@Getter
@AllArgsConstructor
public enum ContractStatusEnum {

    SIGNED("signed", "已签署"),
    UNSIGNED("unsigned", "未签署");

    /**
     * 状态编码
     */
    private final String code;
    /**
     * 状态中文名
     */
    private final String name;

    public static ContractStatusEnum getByCode(String code) {
        for (ContractStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

}
