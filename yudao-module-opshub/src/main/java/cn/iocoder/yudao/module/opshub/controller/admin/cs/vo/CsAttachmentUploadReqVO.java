package cn.iocoder.yudao.module.opshub.controller.admin.cs.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 上传附件 Request VO")
@Data
public class CsAttachmentUploadReqVO {

    @Schema(description = "关联模块：task/opreq/basedata/signing/order/aftersale", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "模块不能为空")
    private String module;

    @Schema(description = "业务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "业务ID不能为空")
    private Long businessId;

    @Schema(description = "业务编号")
    private String businessCode;

    @Schema(description = "文件URL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "文件URL不能为空")
    private String fileUrl;

    @Schema(description = "原始文件名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "文件名不能为空")
    private String fileName;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "MIME类型")
    private String fileType;

    @Schema(description = "备注")
    private String remark;

}
