package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 售后原因枚举
 */
@Getter
@AllArgsConstructor
public enum AfterSaleReasonEnum {

    COMPLAINT("complaint", "投诉"),
    RECALL("recall", "召回"),
    DAMAGE("damage", "破损");

    /**
     * 编码
     */
    private final String code;
    /**
     * 中文名
     */
    private final String name;

    public static AfterSaleReasonEnum getByCode(String code) {
        for (AfterSaleReasonEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

}
