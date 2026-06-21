package cn.iocoder.yudao.module.opshub.framework.excel.convert;

import java.util.Map;

public class PolicyTypeConvert extends AbstractMapConvert {
    private static final Map<String, String> LABEL_TO_CODE = Map.of(
            "返利", "rebate", "促销", "promotion", "其他", "other");
    private static final Map<String, String> CODE_TO_LABEL = Map.of(
            "rebate", "返利", "promotion", "促销", "other", "其他");

    @Override protected Map<String, String> getLabelToCodeMap() { return LABEL_TO_CODE; }
    @Override protected Map<String, String> getCodeToLabelMap() { return CODE_TO_LABEL; }
}
