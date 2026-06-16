package cn.iocoder.yudao.module.opshub.dal.dataobject.cs;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 操作请求 DO
 */
@TableName("ops_cs_opreq")
@KeySequence("ops_cs_opreq_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class CsOpReqDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 操作请求编号 OPR-YYYYMMDD-NNN
     */
    private String opreqCode;
    /**
     * 操作类型
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.CsOpReqTypeEnum}
     */
    private String opType;
    /**
     * 状态
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.CsOpReqStatusEnum}
     */
    private Integer status;
    /**
     * 经销商编码（数据权限用）
     */
    private String dealerCode;
    /**
     * 经销商名称
     */
    private String dealerName;
    /**
     * 产品线编码（数据权限用，可空）
     */
    private String productLineCode;
    /**
     * 产品线名称
     */
    private String productLineName;
    /**
     * 来源模块
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.CsSourceModuleEnum}
     */
    private String sourceModule;
    /**
     * 来源业务 ID
     */
    private Long sourceId;
    /**
     * 来源业务编号（展示用）
     */
    private String sourceCode;
    /**
     * 请求内容描述
     */
    private String content;
    /**
     * 发起人用户 ID
     */
    private Long creatorUserId;
    /**
     * 当前处理人用户 ID
     */
    private Long assigneeId;
    /**
     * 接单时间
     */
    private LocalDateTime acceptTime;
    /**
     * 提交时间
     */
    private LocalDateTime submitTime;
    /**
     * 处理留言
     */
    private String submitRemark;
    /**
     * 验收时间
     */
    private LocalDateTime verifyTime;
    /**
     * 完成时间
     */
    private LocalDateTime completedTime;
    /**
     * 备注
     */
    private String remark;

}
