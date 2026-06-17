package cn.iocoder.yudao.module.opshub.controller.admin.dealer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 执行员授权用户响应 VO")
@Data
public class ExecutorScopeUserRespVO {

    @Schema(description = "用户ID", example = "100")
    private Long userId;

    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    @Schema(description = "已授权的产品线列表")
    private List<ProductLineItemVO> productLines;

}
