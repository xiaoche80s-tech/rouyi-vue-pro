package cn.iocoder.yudao.module.opshub.framework.excel.convert;

import java.util.Map;

public class PolicyAchievementTypeConvert extends AbstractMapConvert {
    private static final Map<String, String> LABEL_TO_CODE = Map.of(
            "季度政策", "quarter", "月度政策", "month");
    private static final Map<String, String> CODE_TO_LABEL = Map.of(
            "quarter", "季度政策", "month", "月度政策");

    @Override protected Map<String, String> getLabelToCodeMap() { return LABEL_TO_CODE; }
    @Override protected Map<String, String> getCodeToLabelMap() { return CODE_TO_LABEL; }
}
