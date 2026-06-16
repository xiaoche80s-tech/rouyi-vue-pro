package cn.iocoder.yudao.module.opshub.dal.dataobject.cs;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 咨询消息记录 DO
 */
@TableName("ops_cs_message")
@KeySequence("ops_cs_message_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class CsMessageDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 关联会话 ID
     */
    private Long sessionId;
    /**
     * 关联会话编号
     */
    private String sessionNo;
    /**
     * 发送人用户 ID
     */
    private Long senderId;
    /**
     * 发送人姓名
     */
    private String senderName;
    /**
     * 发送人角色
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.CsSenderRoleEnum}
     */
    private String senderRole;
    /**
     * 消息类型
     *
     * 枚举 {@link cn.iocoder.yudao.module.opshub.enums.CsMessageTypeEnum}
     */
    private String messageType;
    /**
     * 文本内容
     */
    private String content;
    /**
     * 附件 ID 列表（逗号分隔）
     */
    private String attachmentIds;
    /**
     * 链接地址
     */
    private String linkUrl;
    /**
     * 链接标题
     */
    private String linkTitle;
    /**
     * 是否已读
     */
    private Integer isRead;
    /**
     * 备注
     */
    private String remark;

}
