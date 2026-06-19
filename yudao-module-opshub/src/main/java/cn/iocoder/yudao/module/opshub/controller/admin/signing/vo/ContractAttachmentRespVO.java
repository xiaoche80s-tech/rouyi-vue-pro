package cn.iocoder.yudao.module.opshub.controller.admin.signing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 合同附件 Response VO")
@Data
public class ContractAttachmentRespVO {

    @Schema(description = "文件ID")
    private Long id;

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "文件子类型编码")
    private String fileType;

    @Schema(description = "文件子类型名称")
    private String fileTypeName;

    @Schema(description = "文件编号")
    private String fileNo;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "文件描述")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
