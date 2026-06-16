package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 咨询消息类型枚举
 */
@Getter
@AllArgsConstructor
public enum CsMessageTypeEnum {

    TEXT("text", "纯文本"),
    ATTACHMENT("attachment", "文件附件"),
    SYSTEM("system", "系统消息"),
    LINK("link", "链接分享");

    /**
     * 类型编码
     */
    private final String code;
    /**
     * 类型中文名
     */
    private final String name;

    public static CsMessageTypeEnum getByCode(String code) {
        for (CsMessageTypeEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

}
