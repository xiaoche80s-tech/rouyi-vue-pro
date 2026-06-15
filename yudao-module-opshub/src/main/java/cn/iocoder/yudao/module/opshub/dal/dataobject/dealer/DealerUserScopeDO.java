package cn.iocoder.yudao.module.opshub.dal.dataobject.dealer;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户-经销商授权 DO
 */
@TableName("ops_dealer_user_scope")
@KeySequence("ops_dealer_user_scope_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class DealerUserScopeDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 用户 ID
     */
    private Long userId;
    /**
     * 经销商编码
     */
    private String dealerCode;

}
