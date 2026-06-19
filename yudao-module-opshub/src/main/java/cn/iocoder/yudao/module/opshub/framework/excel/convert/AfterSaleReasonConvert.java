package cn.iocoder.yudao.module.opshub.framework.excel.convert;

import java.util.Map;

public class AfterSaleReasonConvert extends AbstractMapConvert {
    private static final Map<String, String> LABEL_TO_CODE = Map.of(
            "投诉", "complaint", "召回", "recall", "破损", "damage");
    private static final Map<String, String> CODE_TO_LABEL = Map.of(
            "complaint", "投诉", "recall", "召回", "damage", "破损");

    @Override protected Map<String, String> getLabelToCodeMap() { return LABEL_TO_CODE; }
    @Override protected Map<String, String> getCodeToLabelMap() { return CODE_TO_LABEL; }
}
