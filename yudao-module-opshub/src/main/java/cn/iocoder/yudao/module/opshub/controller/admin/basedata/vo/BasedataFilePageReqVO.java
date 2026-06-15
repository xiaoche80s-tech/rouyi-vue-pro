package cn.iocoder.yudao.module.opshub.controller.admin.basedata.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 基础数据文件分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BasedataFilePageReqVO extends PageParam {

    @Schema(description = "文件分类", example = "qualification")
    private String category;

    @Schema(description = "文件名称", example = "营业执照")
    private String fileName;

    @Schema(description = "经销商ID", example = "1")
    private Long dealerId;

    @Schema(description = "有效期状态", example = "valid")
    private String expireStatus;

}
