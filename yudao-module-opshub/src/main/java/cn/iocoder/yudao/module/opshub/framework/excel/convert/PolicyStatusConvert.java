package cn.iocoder.yudao.module.opshub.framework.excel.convert;

import java.util.Map;

public class PolicyStatusConvert extends AbstractMapConvert {
    private static final Map<String, String> LABEL_TO_CODE = Map.of(
            "执行中", "executing", "待执行", "pending", "已完成", "completed");
    private static final Map<String, String> CODE_TO_LABEL = Map.of(
            "executing", "执行中", "pending", "待执行", "completed", "已完成");

    @Override protected Map<String, String> getLabelToCodeMap() { return LABEL_TO_CODE; }
    @Override protected Map<String, String> getCodeToLabelMap() { return CODE_TO_LABEL; }
}
