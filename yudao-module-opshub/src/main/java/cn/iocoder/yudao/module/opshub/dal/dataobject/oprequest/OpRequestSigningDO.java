package cn.iocoder.yudao.module.opshub.dal.dataobject.oprequest;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作请求-签约子表 DO
 */
@TableName("ops_op_request_signing")
@KeySequence("ops_op_request_signing_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class OpRequestSigningDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 关联 ops_op_request.id
     */
    private Long requestId;
    /**
     * 关联 ops_signing_contract.id
     */
    private Long contractId;
    /**
     * 合同编码
     */
    private String contractCode;
    /**
     * 合同名称
     */
    private String contractName;

}
