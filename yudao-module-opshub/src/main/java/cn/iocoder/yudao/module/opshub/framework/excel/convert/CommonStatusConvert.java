package cn.iocoder.yudao.module.opshub.framework.excel.convert;

import java.util.Map;

public class CommonStatusConvert extends AbstractMapConvert {
    private static final Map<String, String> LABEL_TO_CODE = Map.of("正常", "0", "停用", "1");
    private static final Map<String, String> CODE_TO_LABEL = Map.of("0", "正常", "1", "停用");

    @Override protected Map<String, String> getLabelToCodeMap() { return LABEL_TO_CODE; }
    @Override protected Map<String, String> getCodeToLabelMap() { return CODE_TO_LABEL; }
}
