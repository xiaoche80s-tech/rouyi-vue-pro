package cn.iocoder.yudao.module.opshub.dal.dataobject.dealer;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 经销商-产品线关联 DO
 */
@TableName("ops_dealer_product_line_relation")
@KeySequence("ops_dealer_product_line_relation_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class DealerProductLineRelationDO extends TenantBaseDO {

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
     * 产品线 ID
     */
    private Long productLineId;

}
