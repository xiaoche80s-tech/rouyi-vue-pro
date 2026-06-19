package cn.iocoder.yudao.module.opshub.framework.excel.convert;

import java.util.Map;

public class AfterSaleHandlingMethodConvert extends AbstractMapConvert {
    private static final Map<String, String> LABEL_TO_CODE = Map.of(
            "退货", "return", "退换货", "exchange", "退货退款", "return_refund");
    private static final Map<String, String> CODE_TO_LABEL = Map.of(
            "return", "退货", "exchange", "退换货", "return_refund", "退货退款");

    @Override protected Map<String, String> getLabelToCodeMap() { return LABEL_TO_CODE; }
    @Override protected Map<String, String> getCodeToLabelMap() { return CODE_TO_LABEL; }
}
