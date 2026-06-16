package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 附件关联模块枚举
 */
@Getter
@AllArgsConstructor
public enum CsAttachmentModuleEnum {

    TASK("task", "工单"),
    OPREQ("opreq", "操作请求"),
    BASEDATA("basedata", "基础数据"),
    SIGNING("signing", "签约进度"),
    ORDER("order", "订单"),
    AFTERSALE("aftersale", "售后"),
    MESSAGE("message", "咨询消息");

    /**
     * 模块编码
     */
    private final String code;
    /**
     * 模块中文名
     */
    private final String name;

    public static CsAttachmentModuleEnum getByCode(String code) {
        for (CsAttachmentModuleEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

}
