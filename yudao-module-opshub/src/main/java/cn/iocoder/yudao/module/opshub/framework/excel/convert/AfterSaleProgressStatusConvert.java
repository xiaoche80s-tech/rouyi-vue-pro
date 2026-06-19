package cn.iocoder.yudao.module.opshub.framework.excel.convert;

import java.util.Map;

public class AfterSaleProgressStatusConvert extends AbstractMapConvert {
    private static final Map<String, String> LABEL_TO_CODE = Map.of(
            "待处理", "pending", "进行中", "in_progress", "换货中", "exchanging", "已完成", "completed");
    private static final Map<String, String> CODE_TO_LABEL = Map.of(
            "pending", "待处理", "in_progress", "进行中", "exchanging", "换货中", "completed", "已完成");

    @Override protected Map<String, String> getLabelToCodeMap() { return LABEL_TO_CODE; }
    @Override protected Map<String, String> getCodeToLabelMap() { return CODE_TO_LABEL; }
}
