package cn.iocoder.yudao.module.opshub.controller.admin.basedata.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Schema(description = "管理后台 - 基础数据文件创建/修改 Request VO")
@Data
public class BasedataFileSaveReqVO {

    @Schema(description = "文件ID（修改时必填）", example = "1")
    private Long id;

    @Schema(description = "经销商ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "经销商ID不能为空")
    private Long dealerId;

    @Schema(description = "经销商编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "HK")
    @NotEmpty(message = "经销商编码不能为空")
    private String dealerCode;

    @Schema(description = "文件分类", requiredMode = Schema.RequiredMode.REQUIRED, example = "qualification")
    @NotEmpty(message = "文件分类不能为空")
    private String category;

    @Schema(description = "文件名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "营业执照-华康")
    @NotEmpty(message = "文件名称不能为空")
    private String fileName;

    @Schema(description = "文件子类型编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "BL")
    @NotEmpty(message = "文件子类型不能为空")
    private String fileType;

    @Schema(description = "文件编号", example = "BL-2024-001")
    private String fileNo;

    @Schema(description = "文件地址", example = "https://example.com/file.pdf")
    private String fileUrl;

    @Schema(description = "文件大小（字节）", example = "1024000")
    private Long fileSize;

    @Schema(description = "有效期至", example = "2027-12-31")
    private LocalDate expireDate;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "文件描述", example = "华康医疗器械营业执照")
    private String description;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
