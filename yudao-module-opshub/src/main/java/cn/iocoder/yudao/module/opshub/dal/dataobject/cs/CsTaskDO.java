package cn.iocoder.yudao.module.opshub.dal.dataobject.cs;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 客服工单 DO
 */
@TableName("ops_cs_task")
@KeySequence("ops_cs_task_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class CsTaskDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 工单编号 TASK-YYYYMMDD-NNN
     */
    private String taskNo;
    /**
     * 工单内容
     */
    private String content;
    /**
     * 紧急程度
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.CsTaskUrgencyEnum}
     */
    private Integer urgency;
    /**
     * 状态
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.CsTaskStatusEnum}
     */
    private Integer status;
    /**
     * 提单人（经销商代理人）用户ID
     */
    private Long creatorUserId;
    /**
     * 当前处理人（执行员）用户ID
     */
    private Long assigneeId;
    /**
     * 分类
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.CsTaskCategoryEnum}
     */
    private Integer category;
    /**
     * SLA 截止时间
     */
    private LocalDateTime slaDeadline;
    /**
     * 关联经销商编码
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
     * 来源模块
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.CsSourceModuleEnum}
     */
    private String sourceModule;
    /**
     * 备注
     */
    private String remark;
    /**
     * 接单时间
     */
    private LocalDateTime acceptTime;
    /**
     * 交付时间
     */
    private LocalDateTime deliverTime;
    /**
     * 验收时间
     */
    private LocalDateTime verifyTime;
    /**
     * 退回原因
     */
    private String rejectReason;
    /**
     * BPM 流程实例编号
     */
    private String processInstanceId;

}
