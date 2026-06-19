package cn.iocoder.yudao.module.opshub.framework.excel.convert;

import java.util.Map;

public class ContractTypeConvert extends AbstractMapConvert {
    private static final Map<String, String> LABEL_TO_CODE = Map.of(
            "主合同", "main", "政策合同", "policy", "补充协议", "supplement", "终止协议", "termination");
    private static final Map<String, String> CODE_TO_LABEL = Map.of(
            "main", "主合同", "policy", "政策合同", "supplement", "补充协议", "termination", "终止协议");

    @Override protected Map<String, String> getLabelToCodeMap() { return LABEL_TO_CODE; }
    @Override protected Map<String, String> getCodeToLabelMap() { return CODE_TO_LABEL; }
}
