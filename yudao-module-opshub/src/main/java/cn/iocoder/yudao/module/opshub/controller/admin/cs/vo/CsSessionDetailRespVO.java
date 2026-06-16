package cn.iocoder.yudao.module.opshub.controller.admin.cs.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Schema(description = "管理后台 - 咨询会话详情 Response VO（含消息列表）")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CsSessionDetailRespVO extends CsSessionRespVO {

    @Schema(description = "历史消息列表")
    private List<CsMessageRespVO> messages;

}
