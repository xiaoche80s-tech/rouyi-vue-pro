package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 来源模块枚举（工单 + 操作请求共用）
 */
@Getter
@AllArgsConstructor
public enum CsSourceModuleEnum {

    AFTERSALE("aftersale", "售后"),
    ORDER("order", "订单"),
    SIGNING("signing", "签约进度"),
    POLICY("policy", "政策管理"),
    BASEDATA("basedata", "基础数据"),
    MANUAL("manual", "手动创建");

    /**
     * 模块编码
     */
    private final String code;
    /**
     * 模块中文名
     */
    private final String name;

    public static CsSourceModuleEnum getByCode(String code) {
        for (CsSourceModuleEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

}
