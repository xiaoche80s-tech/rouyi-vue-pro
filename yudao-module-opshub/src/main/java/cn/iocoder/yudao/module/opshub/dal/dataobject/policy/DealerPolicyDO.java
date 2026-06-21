package cn.iocoder.yudao.module.opshub.dal.dataobject.policy;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 经销商政策 DO
 */
@TableName("ops_dealer_policy")
@KeySequence("ops_dealer_policy_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class DealerPolicyDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 经销商 ID
     */
    private Long dealerId;
    /**
     * 经销商编码（数据权限用）
     */
    private String dealerCode;
    /**
     * 产品线编码（数据权限用）
     */
    private String productLineCode;
    /**
     * 产品线名称（冗余，便于展示）
     */
    private String productLineName;
    /**
     * 政策编码（唯一）
     */
    private String policyCode;
    /**
     * 政策名称
     */
    private String policyName;
    /**
     * 政策类型
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.PolicyTypeEnum}
     */
    private String policyType;
    /**
     * 达成类型
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.PolicyAchievementTypeEnum}
     */
    private String achievementType;
    /**
     * 政策状态
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.PolicyStatusEnum}
     */
    private String policyStatus;
    /**
     * 政策开始日期
     */
    private LocalDate startDate;
    /**
     * 政策结束日期
     */
    private LocalDate endDate;
    /**
     * 来源合同编码
     */
    private String contractCode;
    /**
     * 来源合同名称
     */
    private String contractName;
    /**
     * 政策描述
     */
    private String policyDesc;
    /**
     * 来源政策合同 ID
     */
    private Long sourceContractId;
    /**
     * 备注
     */
    private String remark;

}
