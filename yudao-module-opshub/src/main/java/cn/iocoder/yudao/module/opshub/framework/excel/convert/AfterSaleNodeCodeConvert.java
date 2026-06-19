package cn.iocoder.yudao.module.opshub.framework.excel.convert;

import java.util.Map;

public class AfterSaleNodeCodeConvert extends AbstractMapConvert {
    private static final Map<String, String> LABEL_TO_CODE = Map.of(
            "申请提交", "submitted", "审核通过", "reviewed", "商品退回", "returned",
            "退款完成", "refund_done", "红字发票", "red_invoice", "新商品发出", "exchange_sent", "确认收货", "received");
    private static final Map<String, String> CODE_TO_LABEL = Map.of(
            "submitted", "申请提交", "reviewed", "审核通过", "returned", "商品退回",
            "refund_done", "退款完成", "red_invoice", "红字发票", "exchange_sent", "新商品发出", "received", "确认收货");

    @Override protected Map<String, String> getLabelToCodeMap() { return LABEL_TO_CODE; }
    @Override protected Map<String, String> getCodeToLabelMap() { return CODE_TO_LABEL; }
}
