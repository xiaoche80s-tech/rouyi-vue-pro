package cn.iocoder.yudao.module.opshub.dal.dataobject.dealer;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 执行员-产品线授权 DO
 */
@TableName("ops_executor_product_line_scope")
@KeySequence("ops_executor_product_line_scope_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class ExecutorProductLineScopeDO extends TenantBaseDO {

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
     * 产品线编码
     */
    private String productLineCode;

}
