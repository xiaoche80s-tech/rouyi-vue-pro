package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 客服工单分类枚举
 */
@Getter
@AllArgsConstructor
public enum CsTaskCategoryEnum {

    SIGNING(0, "签约"),
    POLICY(1, "政策"),
    AFTER_SALE(2, "售后"),
    PURCHASE_ORDER(3, "订单"),
    DATA(4, "数据"),
    OTHER(5, "其他");

    /**
     * 编码
     */
    private final Integer code;
    /**
     * 中文名
     */
    private final String name;

    public static CsTaskCategoryEnum getByCode(Integer code) {
        for (CsTaskCategoryEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

}
