package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 售后进度状态枚举
 */
@Getter
@AllArgsConstructor
public enum AfterSaleProgressStatusEnum {

    PENDING("pending", "待处理"),
    IN_PROGRESS("in_progress", "进行中"),
    EXCHANGING("exchanging", "换货中"),
    COMPLETED("completed", "已完成");

    /**
     * 状态编码
     */
    private final String code;
    /**
     * 状态中文名
     */
    private final String name;

    public static AfterSaleProgressStatusEnum getByCode(String code) {
        for (AfterSaleProgressStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

}
