package cn.iocoder.yudao.module.opshub.framework.excel.convert;

import java.util.Map;

public class BooleanConvert extends AbstractMapConvert {
    private static final Map<String, String> LABEL_TO_CODE = Map.of("是", "true", "否", "false");
    private static final Map<String, String> CODE_TO_LABEL = Map.of("true", "是", "false", "否");

    @Override protected Map<String, String> getLabelToCodeMap() { return LABEL_TO_CODE; }
    @Override protected Map<String, String> getCodeToLabelMap() { return CODE_TO_LABEL; }
}
