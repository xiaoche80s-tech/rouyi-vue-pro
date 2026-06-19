package cn.iocoder.yudao.module.opshub.framework.excel.convert;

import cn.idev.excel.converters.Converter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;

import java.util.Map;

/**
 * 基于 Map 的 FastExcel 枚举转换器基类
 * <p>
 * 子类只需提供 labelToCode / codeToLabel 两个 Map，即可实现中文↔编码的双向转换。
 */
public abstract class AbstractMapConvert implements Converter<String> {

    /**
     * 中文 → 编码（Excel → Java）
     */
    protected abstract Map<String, String> getLabelToCodeMap();

    /**
     * 编码 → 中文（Java → Excel）
     */
    protected abstract Map<String, String> getCodeToLabelMap();

    @Override
    public Class<?> supportJavaTypeKey() {
        return String.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    /**
     * Excel 中文值 → Java 编码值
     */
    @Override
    public String convertToJavaData(ReadCellData readCellData, ExcelContentProperty contentProperty,
                                    GlobalConfiguration globalConfiguration) {
        String label = readCellData.getStringValue();
        if (label == null || label.isEmpty()) {
            return null;
        }
        String code = getLabelToCodeMap().get(label.trim());
        return code != null ? code : label; // 如果映射不到，原样返回
    }

    /**
     * Java 编码值 → Excel 中文值
     */
    @Override
    public WriteCellData<String> convertToExcelData(String value, ExcelContentProperty contentProperty,
                                                     GlobalConfiguration globalConfiguration) {
        if (value == null || value.isEmpty()) {
            return new WriteCellData<>("");
        }
        String label = getCodeToLabelMap().get(value);
        return new WriteCellData<>(label != null ? label : value);
    }
}
