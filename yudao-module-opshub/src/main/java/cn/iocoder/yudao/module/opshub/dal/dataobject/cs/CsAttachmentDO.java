package cn.iocoder.yudao.module.opshub.dal.dataobject.cs;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通用附件 DO
 */
@TableName("ops_cs_attachment")
@KeySequence("ops_cs_attachment_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class CsAttachmentDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 关联模块
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.CsAttachmentModuleEnum}
     */
    private String module;
    /**
     * 业务 ID
     */
    private Long businessId;
    /**
     * 业务编号（冗余存储）
     */
    private String businessCode;
    /**
     * 原始文件名
     */
    private String fileName;
    /**
     * 文件 URL
     */
    private String fileUrl;
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    /**
     * MIME 类型
     */
    private String fileType;
    /**
     * 备注
     */
    private String remark;

}
