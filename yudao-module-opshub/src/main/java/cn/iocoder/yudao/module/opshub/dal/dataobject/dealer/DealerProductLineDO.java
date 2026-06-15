package cn.iocoder.yudao.module.opshub.dal.dataobject.dealer;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产品线 DO
 */
@TableName("ops_dealer_product_line")
@KeySequence("ops_dealer_product_line_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class DealerProductLineDO extends TenantBaseDO {

    /**
     * 产品线 ID
     */
    @TableId
    private Long id;
    /**
     * 产品线名称
     */
    private String productLineName;
    /**
     * 产品线编码
     */
    private String productLineCode;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 状态（0=正常, 1=停用）
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
