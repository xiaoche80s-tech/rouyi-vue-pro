package cn.iocoder.yudao.module.opshub.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 售后进度节点编码枚举
 */
@Getter
@AllArgsConstructor
public enum AfterSaleNodeCodeEnum {

    SUBMITTED("submitted", "申请提交"),
    REVIEWED("reviewed", "审核通过"),
    RETURNED("returned", "商品退回"),
    REFUND_DONE("refund_done", "退款完成"),
    RED_INVOICE("red_invoice", "红字发票"),
    EXCHANGE_SENT("exchange_sent", "新商品发出"),
    RECEIVED("received", "确认收货");

    /**
     * 节点编码
     */
    private final String code;
    /**
     * 节点中文名
     */
    private final String name;

    public static AfterSaleNodeCodeEnum getByCode(String code) {
        for (AfterSaleNodeCodeEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

}
