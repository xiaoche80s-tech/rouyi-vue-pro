package cn.iocoder.yudao.module.opshub.framework.excel.convert;

import java.util.Map;

public class OrderInvoiceRecordStatusConvert extends AbstractMapConvert {
    private static final Map<String, String> LABEL_TO_CODE = Map.of("待开票", "pending", "已开票", "invoiced");
    private static final Map<String, String> CODE_TO_LABEL = Map.of("pending", "待开票", "invoiced", "已开票");

    @Override protected Map<String, String> getLabelToCodeMap() { return LABEL_TO_CODE; }
    @Override protected Map<String, String> getCodeToLabelMap() { return CODE_TO_LABEL; }
}
