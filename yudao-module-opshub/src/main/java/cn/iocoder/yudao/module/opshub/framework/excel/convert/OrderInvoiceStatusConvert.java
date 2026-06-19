package cn.iocoder.yudao.module.opshub.framework.excel.convert;

import java.util.Map;

public class OrderInvoiceStatusConvert extends AbstractMapConvert {
    private static final Map<String, String> LABEL_TO_CODE = Map.of(
            "未开票", "uninvoiced", "部分开票", "partial", "已开票", "invoiced");
    private static final Map<String, String> CODE_TO_LABEL = Map.of(
            "uninvoiced", "未开票", "partial", "部分开票", "invoiced", "已开票");

    @Override protected Map<String, String> getLabelToCodeMap() { return LABEL_TO_CODE; }
    @Override protected Map<String, String> getCodeToLabelMap() { return CODE_TO_LABEL; }
}
