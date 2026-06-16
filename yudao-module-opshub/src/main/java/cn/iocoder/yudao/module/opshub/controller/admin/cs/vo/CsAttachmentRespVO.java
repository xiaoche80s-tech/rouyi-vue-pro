package cn.iocoder.yudao.module.opshub.controller.admin.cs.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 附件 Response VO")
@Data
public class CsAttachmentRespVO {

    @Schema(description = "附件ID")
    private Long id;

    @Schema(description = "关联模块")
    private String module;

    @Schema(description = "业务ID")
    private Long businessId;

    @Schema(description = "业务编号")
    private String businessCode;

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "文件URL")
    private String fileUrl;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "MIME类型")
    private String fileType;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
