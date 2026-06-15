package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 合同类型枚举
 */
@Getter
@AllArgsConstructor
public enum ContractTypeEnum {

    MAIN("main", "主合同", "MC"),
    POLICY("policy", "政策合同", "POL"),
    SUPPLEMENT("supplement", "补充协议", "SA"),
    TERMINATION("termination", "终止协议", "TA");

    /**
     * 类型编码
     */
    private final String code;
    /**
     * 类型中文名
     */
    private final String name;
    /**
     * 编码前缀
     */
    private final String prefix;

    public static ContractTypeEnum getByCode(String code) {
        for (ContractTypeEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

}
