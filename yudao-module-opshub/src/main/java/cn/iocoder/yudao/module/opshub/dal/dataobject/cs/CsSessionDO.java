package cn.iocoder.yudao.module.opshub.dal.dataobject.cs;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 咨询会话 DO
 */
@TableName("ops_cs_session")
@KeySequence("ops_cs_session_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class CsSessionDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 会话编号 CS-YYYYMMDD-NNN
     */
    private String sessionNo;
    /**
     * 咨询类型
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.CsConsultTypeEnum}
     */
    private String consultType;
    /**
     * 状态
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.CsSessionStatusEnum}
     */
    private Integer status;
    /**
     * 咨询上下文描述
     */
    private String context;
    /**
     * 上下文关联业务 ID
     */
    private Long contextId;
    /**
     * 上下文关联业务编号
     */
    private String contextCode;
    /**
     * 来源模块
     */
    private String sourceModule;
    /**
     * 经销商编码（数据权限用）
     */
    private String dealerCode;
    /**
     * 经销商名称（冗余存储）
     */
    private String dealerName;
    /**
     * 产品线编码（数据权限用）
     */
    private String productLineCode;
    /**
     * 产品线名称（冗余存储）
     */
    private String productLineName;
    /**
     * 发起人用户 ID
     */
    private Long initiatorId;
    /**
     * 发起人姓名
     */
    private String initiatorName;
    /**
     * 当前处理人（执行员）用户 ID
     */
    private Long assigneeId;
    /**
     * 当前处理人姓名
     */
    private String assigneeName;
    /**
     * 最后一条消息摘要
     */
    private String lastMessage;
    /**
     * 最后一条消息时间
     */
    private LocalDateTime lastMessageTime;
    /**
     * 消息总数
     */
    private Integer messageCount;
    /**
     * 接单时间
     */
    private LocalDateTime acceptTime;
    /**
     * 完成时间
     */
    private LocalDateTime completeTime;
    /**
     * 关闭时间
     */
    private LocalDateTime closeTime;
    /**
     * 解决方案摘要
     */
    private String solutionSummary;
    /**
     * 备注
     */
    private String remark;

}
