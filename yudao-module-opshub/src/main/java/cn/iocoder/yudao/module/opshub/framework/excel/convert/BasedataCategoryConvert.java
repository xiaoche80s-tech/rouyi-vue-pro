package cn.iocoder.yudao.module.opshub.framework.excel.convert;

import java.util.Map;

public class BasedataCategoryConvert extends AbstractMapConvert {
    private static final Map<String, String> LABEL_TO_CODE = Map.of(
            "资质文件", "qualification", "授权文件", "authorization", "合同文件", "contract", "产品文件", "product");
    private static final Map<String, String> CODE_TO_LABEL = Map.of(
            "qualification", "资质文件", "authorization", "授权文件", "contract", "合同文件", "product", "产品文件");

    @Override protected Map<String, String> getLabelToCodeMap() { return LABEL_TO_CODE; }
    @Override protected Map<String, String> getCodeToLabelMap() { return CODE_TO_LABEL; }
}
