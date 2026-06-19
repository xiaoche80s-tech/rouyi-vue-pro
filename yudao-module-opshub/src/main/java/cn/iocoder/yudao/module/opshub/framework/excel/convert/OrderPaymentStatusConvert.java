package cn.iocoder.yudao.module.opshub.framework.excel.convert;

import java.util.Map;

public class OrderPaymentStatusConvert extends AbstractMapConvert {
    private static final Map<String, String> LABEL_TO_CODE = Map.of(
            "审批中", "pending", "已通过", "approved", "已拒绝", "rejected");
    private static final Map<String, String> CODE_TO_LABEL = Map.of(
            "pending", "审批中", "approved", "已通过", "rejected", "已拒绝");

    @Override protected Map<String, String> getLabelToCodeMap() { return LABEL_TO_CODE; }
    @Override protected Map<String, String> getCodeToLabelMap() { return CODE_TO_LABEL; }
}
