package cn.iocoder.yudao.module.opshub.controller.admin.excel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Schema(description = "管理后台 - Excel 导入响应")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExcelImportRespVO {

    @Schema(description = "成功处理行数")
    private int successCount;

    @Schema(description = "新增行数")
    private int insertCount;

    @Schema(description = "更新行数")
    private int updateCount;

    @Schema(description = "失败行数")
    private int failureCount;

    @Schema(description = "失败明细：行号 → 错误原因")
    private Map<Integer, String> failureRows;
}
