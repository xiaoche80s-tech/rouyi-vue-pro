package cn.iocoder.yudao.module.opshub.dal.dataobject.policy;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 政策达成明细 DO
 */
@TableName("ops_dealer_policy_achievement")
@KeySequence("ops_dealer_policy_achievement_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class DealerPolicyAchievementDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 关联指标 ID
     */
    private Long indicatorId;
    /**
     * 指标名称（冗余，便于查询）
     */
    private String indicatorName;
    /**
     * 省份
     */
    private String province;
    /**
     * 省份编码
     */
    private String provinceCode;
    /**
     * 医院名称
     */
    private String hospital;
    /**
     * 医院编码
     */
    private String hospitalCode;
    /**
     * 产品名称
     */
    private String productName;
    /**
     * 年度
     */
    private Integer targetYear;
    /**
     * 达成值
     */
    private BigDecimal achievedValue;
    /**
     * 层级（province/hospital/product）
     */
    private String achieveLevel;

}
