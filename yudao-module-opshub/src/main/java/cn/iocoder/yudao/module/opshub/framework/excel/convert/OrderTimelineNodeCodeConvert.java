package cn.iocoder.yudao.module.opshub.framework.excel.convert;

import java.util.Map;

public class OrderTimelineNodeCodeConvert extends AbstractMapConvert {
    private static final Map<String, String> LABEL_TO_CODE = Map.of(
            "下单", "created", "已确认", "confirmed", "已发货", "shipped", "已签收", "signed", "已完成", "completed");
    private static final Map<String, String> CODE_TO_LABEL = Map.of(
            "created", "下单", "confirmed", "已确认", "shipped", "已发货", "signed", "已签收", "completed", "已完成");

    @Override protected Map<String, String> getLabelToCodeMap() { return LABEL_TO_CODE; }
    @Override protected Map<String, String> getCodeToLabelMap() { return CODE_TO_LABEL; }
}
