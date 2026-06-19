package cn.iocoder.yudao.module.opshub.framework.excel.convert;

import java.util.Map;

public class OrderPayStatusConvert extends AbstractMapConvert {
    private static final Map<String, String> LABEL_TO_CODE = Map.of("未付款", "unpaid", "已付款", "paid");
    private static final Map<String, String> CODE_TO_LABEL = Map.of("unpaid", "未付款", "paid", "已付款");

    @Override protected Map<String, String> getLabelToCodeMap() { return LABEL_TO_CODE; }
    @Override protected Map<String, String> getCodeToLabelMap() { return CODE_TO_LABEL; }
}
