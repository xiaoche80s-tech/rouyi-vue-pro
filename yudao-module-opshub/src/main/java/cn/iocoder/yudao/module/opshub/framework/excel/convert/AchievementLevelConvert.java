package cn.iocoder.yudao.module.opshub.framework.excel.convert;

import java.util.Map;

public class AchievementLevelConvert extends AbstractMapConvert {
    private static final Map<String, String> LABEL_TO_CODE = Map.of(
            "省级", "province", "医院级", "hospital", "产品级", "product");
    private static final Map<String, String> CODE_TO_LABEL = Map.of(
            "province", "省级", "hospital", "医院级", "product", "产品级");

    @Override protected Map<String, String> getLabelToCodeMap() { return LABEL_TO_CODE; }
    @Override protected Map<String, String> getCodeToLabelMap() { return CODE_TO_LABEL; }
}
