package cn.iocoder.yudao.module.opshub.service.cs.websocket.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 客服工单 WebSocket 通知 DTO
 */
@Data
@Accessors(chain = true)
public class CsTaskNotification {

    /**
     * 工单ID
     */
    private Long taskId;
    /**
     * 工单编号
     */
    private String taskNo;
    /**
     * 消息类型
     */
    private String type;
    /**
     * 消息内容（中文描述）
     */
    private String message;
    /**
     * 工单当前状态
     */
    private Integer status;
    /**
     * 紧急程度
     */
    private Integer urgency;
    /**
     * 操作人用户ID
     */
    private Long operatorUserId;
    /**
     * 操作人姓名
     */
    private String operatorUserName;

    // ========== 消息类型常量 ==========

    public static final String TYPE_TASK_CREATED = "cs-task-created";
    public static final String TYPE_TASK_ACCEPTED = "cs-task-accepted";
    public static final String TYPE_TASK_TRANSFERRED = "cs-task-transferred";
    public static final String TYPE_TASK_DELIVERED = "cs-task-delivered";
    public static final String TYPE_TASK_VERIFIED = "cs-task-verified";
    public static final String TYPE_TASK_REJECTED = "cs-task-rejected";
    public static final String TYPE_TASK_URGING = "cs-task-urging";
    public static final String TYPE_SLA_WARNING = "cs-sla-warning";
    public static final String TYPE_SLA_ALERT = "cs-sla-alert";

    // ========== 操作请求通知类型 ==========
    public static final String TYPE_OPREQ_CREATED = "cs-opreq-created";
    public static final String TYPE_OPREQ_ACCEPTED = "cs-opreq-accepted";
    public static final String TYPE_OPREQ_SUBMITTED = "cs-opreq-submitted";
    public static final String TYPE_OPREQ_VERIFIED = "cs-opreq-verified";

}
