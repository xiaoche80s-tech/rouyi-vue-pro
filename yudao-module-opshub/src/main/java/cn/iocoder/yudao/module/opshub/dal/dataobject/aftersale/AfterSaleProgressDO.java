package cn.iocoder.yudao.module.opshub.dal.dataobject.aftersale;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 售后进度节点子表 DO
 */
@TableName("ops_aftersale_progress")
@KeySequence("ops_aftersale_progress_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class AfterSaleProgressDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 关联售后单 ID
     */
    private Long aftersaleId;
    /**
     * 关联售后单号（冗余）
     */
    private String aftersaleCode;
    /**
     * 节点编码
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.AfterSaleNodeCodeEnum}
     */
    private String nodeCode;
    /**
     * 节点名称（中文）
     */
    private String nodeName;
    /**
     * 节点完成时间（为空表示未完成）
     */
    private LocalDateTime nodeTime;
    /**
     * 是否完成
     */
    private Boolean isCompleted;
    /**
     * 排序序号（1-5）
     */
    private Integer sortOrder;
    /**
     * 节点备注
     */
    private String remark;

}
