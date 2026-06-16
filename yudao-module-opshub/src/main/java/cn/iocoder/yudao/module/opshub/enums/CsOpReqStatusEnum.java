package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作请求状态枚举
 *
 * 流转：待处理(0) → 处理中(1) → 等待验收(2) → 已完成(3)
 * 注：无退回状态，退回需求通过工单系统处理
 */
@Getter
@AllArgsConstructor
public enum CsOpReqStatusEnum {

    PENDING(0, "待处理"),
    IN_PROGRESS(1, "处理中"),
    DELIVERED(2, "等待验收"),
    CLOSED(3, "已完成");

    /**
     * 状态编码
     */
    private final Integer code;
    /**
     * 状态中文名
     */
    private final String name;

    public static CsOpReqStatusEnum getByCode(Integer code) {
        for (CsOpReqStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

}
