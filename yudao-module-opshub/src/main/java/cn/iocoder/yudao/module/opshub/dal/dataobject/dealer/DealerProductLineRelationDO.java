package cn.iocoder.yudao.module.opshub.dal.dataobject.dealer;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 经销商-产品线关联 DO
 * <p>
 * 
 */
@TableName("ops_dealer_product_line_relation")
@KeySequence("ops_dealer_product_line_relation_seq")
@Data
public class DealerProductLineRelationDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 经销商编码
     */
    private String dealerCode;
    /**
     * 产品线编码
     */
    private String productLineCode;

}
