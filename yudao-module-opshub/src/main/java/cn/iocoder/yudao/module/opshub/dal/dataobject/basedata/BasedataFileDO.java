package cn.iocoder.yudao.module.opshub.dal.dataobject.basedata;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 基础数据文件 DO
 */
@TableName("ops_basedata_file")
@KeySequence("ops_basedata_file_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class BasedataFileDO extends TenantBaseDO {

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
     * 经销商编码（数据权限用）
     */
    private String dealerCode;
    /**
     * 文件分类
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.BasedataCategoryEnum}
     */
    private String category;
    /**
     * 文件名称
     */
    private String fileName;
    /**
     * 文件子类型编码
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.BasedataFileTypeEnum}
     */
    private String fileType;
    /**
     * 文件编号
     */
    private String fileNo;
    /**
     * 文件地址
     */
    private String fileUrl;
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    /**
     * 有效期至
     */
    private LocalDate expireDate;
    /**
     * 状态（0=正常, 1=停用）
     */
    private Integer status;
    /**
     * 文件描述
     */
    private String description;
    /**
     * 备注
     */
    private String remark;

}
