package cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 经销商授权用户响应 VO")
@Data
public class DealerScopeUserRespVO {

    @Schema(description = "用户ID", example = "200")
    private Long userId;

    @Schema(description = "用户昵称", example = "李四")
    private String nickname;

    @Schema(description = "已授权的经销商列表")
    private List<DealerItemVO> dealers;

}
