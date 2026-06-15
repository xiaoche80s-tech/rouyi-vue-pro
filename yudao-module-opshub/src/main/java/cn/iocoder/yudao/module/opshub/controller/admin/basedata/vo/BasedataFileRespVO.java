package cn.iocoder.yudao.module.opshub.controller.admin.basedata.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 基础数据文件 Response VO")
@Data
public class BasedataFileRespVO {

    @Schema(description = "文件ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "经销商ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long dealerId;

    @Schema(description = "经销商编码", example = "HK")
    private String dealerCode;

    @Schema(description = "经销商名称", example = "华康医疗器械")
    private String dealerName;

    @Schema(description = "文件分类", requiredMode = Schema.RequiredMode.REQUIRED, example = "qualification")
    private String category;

    @Schema(description = "文件名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "营业执照-华康")
    private String fileName;

    @Schema(description = "文件子类型编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "BL")
    private String fileType;

    @Schema(description = "文件子类型名称", example = "营业执照")
    private String fileTypeName;

    @Schema(description = "文件编号", example = "BL-2024-001")
    private String fileNo;

    @Schema(description = "文件地址", example = "https://example.com/file.pdf")
    private String fileUrl;

    @Schema(description = "文件大小（字节）", example = "1024000")
    private Long fileSize;

    @Schema(description = "有效期至", example = "2027-12-31")
    private LocalDate expireDate;

    @Schema(description = "有效期状态", example = "valid")
    private String expireStatus;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "文件描述", example = "华康医疗器械营业执照")
    private String description;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
