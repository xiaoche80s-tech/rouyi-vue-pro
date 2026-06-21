package cn.iocoder.yudao.module.opshub.controller.admin.oprequest.vo;

import cn.iocoder.yudao.module.opshub.dal.dataobject.oprequest.OpRequestSigningDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 操作请求 Response VO")
@Data
public class OpRequestRespVO {

    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "请求编号")
    private String requestNo;

    @Schema(description = "请求类型")
    private String requestType;

    @Schema(description = "请求类型中文名")
    private String requestTypeName;

    @Schema(description = "经销商 ID")
    private Long dealerId;

    @Schema(description = "经销商编码")
    private String dealerCode;

    @Schema(description = "状态")
    private String requestStatus;

    @Schema(description = "BPM 流程实例 ID")
    private String processInstanceId;

    @Schema(description = "当前处理人 ID")
    private Long assigneeId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    // ========== 签约子表字段（仅 signing 类型） ==========

    @Schema(description = "合同 ID")
    private Long contractId;

    @Schema(description = "合同编码")
    private String contractCode;

    @Schema(description = "合同名称")
    private String contractName;

    /**
     * 从签约子表 DO 填充到 RespVO
     */
    public void fillSigning(OpRequestSigningDO signing) {
        if (signing != null) {
            this.contractId = signing.getContractId();
            this.contractCode = signing.getContractCode();
            this.contractName = signing.getContractName();
        }
    }

}
