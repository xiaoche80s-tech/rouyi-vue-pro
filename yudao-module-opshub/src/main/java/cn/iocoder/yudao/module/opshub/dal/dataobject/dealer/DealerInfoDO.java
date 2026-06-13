package cn.iocoder.yudao.module.opshub.dal.dataobject.dealer;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 经销商信息 DO
 */
@TableName("ops_dealer_info")
@KeySequence("ops_dealer_info_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class DealerInfoDO extends TenantBaseDO {

    /**
     * 经销商 ID
     */
    @TableId
    private Long id;
    /**
     * 经销商名称
     */
    private String name;
    /**
     * 经销商编码
     */
    private String code;
    /**
     * 联系人
     */
    private String contactName;
    /**
     * 联系电话
     */
    private String contactPhone;
    /**
     * 地址
     */
    private String address;
    /**
     * 状态（0=正常, 1=停用）
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
