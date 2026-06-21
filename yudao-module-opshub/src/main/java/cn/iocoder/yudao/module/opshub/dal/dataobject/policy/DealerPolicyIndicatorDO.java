package cn.iocoder.yudao.module.opshub.dal.dataobject.policy;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 政策指标 DO
 */
@TableName("ops_dealer_policy_indicator")
@KeySequence("ops_dealer_policy_indicator_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class DealerPolicyIndicatorDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 关联政策 ID
     */
    private Long policyId;
    /**
     * 政策编码（冗余，便于查询）
     */
    private String policyCode;
    /**
     * 指标名称
     */
    private String indicatorName;
    /**
     * 年度
     */
    private Integer targetYear;
    /**
     * 月份（季度政策：3/6/9/12，月度政策：1-12）
     */
    private Integer targetMonth;
    /**
     * 目标值
     */
    private BigDecimal targetValue;
    /**
     * 达成值
     */
    private BigDecimal achievedValue;
    /**
     * 单位
     */
    private String unit;

}
