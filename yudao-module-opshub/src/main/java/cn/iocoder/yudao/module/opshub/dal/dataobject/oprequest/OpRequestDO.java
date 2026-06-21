package cn.iocoder.yudao.module.opshub.dal.dataobject.oprequest;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.opshub.enums.OpRequestStatusEnum;
import cn.iocoder.yudao.module.opshub.enums.OpRequestTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作请求主表 DO
 */
@TableName("ops_op_request")
@KeySequence("ops_op_request_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class OpRequestDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 请求编号（自动生成，如 OP-20260621-001）
     */
    private String requestNo;
    /**
     * 类型：signing/payment/invoice/return
     *
     * 枚举 {@link OpRequestTypeEnum}
     */
    private String requestType;
    /**
     * 类型中文名
     */
    private String requestTypeName;
    /**
     * 经销商 ID
     */
    private Long dealerId;
    /**
     * 经销商编码
     */
    private String dealerCode;
    /**
     * 状态：waiting/in_progress/delivered/closed/rejected/cancel
     *
     * 枚举 {@link OpRequestStatusEnum}
     */
    private String requestStatus;
    /**
     * BPM 流程实例 ID
     */
    private String processInstanceId;
    /**
     * 当前处理人（执行员）ID
     */
    private Long assigneeId;
    /**
     * 备注
     */
    private String remark;

}
