package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作请求状态枚举
 */
@Getter
@AllArgsConstructor
public enum OpRequestStatusEnum {

    WAITING("waiting", "待处理"),
    IN_PROGRESS("in_progress", "处理中"),
    DELIVERED("delivered", "已交付"),
    CLOSED("closed", "已验收"),
    REJECTED("rejected", "已退回"),
    CANCEL("cancel", "已取消");

    /**
     * 状态编码
     */
    private final String code;
    /**
     * 状态中文名
     */
    private final String name;

    public static OpRequestStatusEnum getByCode(String code) {
        for (OpRequestStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

}
