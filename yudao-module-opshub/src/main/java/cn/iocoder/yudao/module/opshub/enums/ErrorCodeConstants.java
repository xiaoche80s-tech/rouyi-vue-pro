package cn.iocoder.yudao.module.opshub.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * OpsHub 模块错误码常量
 *
 * opshub 模块编号段：1-050-xxx-xxx
 */
public interface ErrorCodeConstants {

    // ========== 经销商管理 1-050-001-xxx ==========
    ErrorCode DEALER_NOT_EXISTS = new ErrorCode(1_050_001_000, "经销商不存在");
    ErrorCode DEALER_CODE_DUPLICATE = new ErrorCode(1_050_001_001, "经销商编码已存在");

    // ========== 产品线管理 1-050-002-xxx ==========
    ErrorCode PRODUCT_LINE_NOT_EXISTS = new ErrorCode(1_050_002_000, "产品线不存在");
    ErrorCode PRODUCT_LINE_CODE_DUPLICATE = new ErrorCode(1_050_002_001, "产品线编码已存在");

    // ========== 关联 1-050-003-xxx ==========
    ErrorCode DEALER_PRODUCT_LINE_EXISTS = new ErrorCode(1_050_003_000, "经销商已绑定该产品线");
    ErrorCode DEALER_USER_SCOPE_EXISTS = new ErrorCode(1_050_003_001, "用户已绑定该经销商");

    // ========== 基础数据 1-050-004-xxx ==========
    ErrorCode BASEDATA_FILE_NOT_EXISTS = new ErrorCode(1_050_004_000, "文件不存在");

    // ========== 签约进度 1-050-005-xxx ==========
    ErrorCode SIGNING_CONTRACT_NOT_EXISTS        = new ErrorCode(1_050_005_000, "合同不存在");
    ErrorCode SIGNING_CONTRACT_CODE_DUPLICATE    = new ErrorCode(1_050_005_001, "合同编码已存在");
    ErrorCode SIGNING_CONTRACT_ALREADY_SIGNED    = new ErrorCode(1_050_005_002, "合同已签署，不可重复签署");
    ErrorCode SIGNING_CONTRACT_NOT_UNSIGNED      = new ErrorCode(1_050_005_003, "仅未签署合同可发起签署");
    ErrorCode SIGNING_CONTRACT_DEALER_NOT_EXISTS = new ErrorCode(1_050_005_004, "关联经销商不存在");

    // ========== 订单模块 1-050-006-xxx ==========
    ErrorCode ORDER_NOT_EXISTS             = new ErrorCode(1_050_006_000, "订单不存在");
    ErrorCode ORDER_CODE_DUPLICATE         = new ErrorCode(1_050_006_001, "订单号已存在");
    ErrorCode ORDER_DEALER_NOT_EXISTS      = new ErrorCode(1_050_006_002, "关联经销商不存在");
    ErrorCode ORDER_ALREADY_PAID           = new ErrorCode(1_050_006_003, "订单已付款，不可重复申请");
    ErrorCode ORDER_ALREADY_INVOICED       = new ErrorCode(1_050_006_004, "订单已全部开票");
    ErrorCode ORDER_NOT_SIGNED             = new ErrorCode(1_050_006_005, "订单未签收，不可申请退货");
    ErrorCode ORDER_PRODUCT_NOT_EXISTS     = new ErrorCode(1_050_006_006, "订单产品明细不存在");
    ErrorCode ORDER_RETURN_QTY_EXCEED      = new ErrorCode(1_050_006_007, "退货数量超过可退货数量");
    ErrorCode ORDER_PAYMENT_PENDING        = new ErrorCode(1_050_006_008, "存在审批中的付款申请");
    ErrorCode ORDER_INVOICE_PENDING        = new ErrorCode(1_050_006_009, "存在待开票的开票申请");

    // ========== 售后模块 1-050-007-xxx ==========
    ErrorCode AFTERSALE_NOT_EXISTS              = new ErrorCode(1_050_007_000, "售后单不存在");
    ErrorCode AFTERSALE_CODE_DUPLICATE          = new ErrorCode(1_050_007_001, "售后单号已存在");
    ErrorCode AFTERSALE_DEALER_NOT_EXISTS       = new ErrorCode(1_050_007_002, "关联经销商不存在");
    ErrorCode AFTERSALE_ALREADY_COMPLETED       = new ErrorCode(1_050_007_003, "售后单已完成，不可继续操作");
    ErrorCode AFTERSALE_PROGRESS_NOT_EXISTS     = new ErrorCode(1_050_007_004, "进度节点不存在");
    ErrorCode AFTERSALE_PROGRESS_ALREADY_DONE   = new ErrorCode(1_050_007_005, "该进度节点已完成");
    ErrorCode AFTERSALE_PROGRESS_ORDER_ERROR    = new ErrorCode(1_050_007_006, "进度节点顺序错误，需按序完成");
    ErrorCode AFTERSALE_REFUND_NOT_APPLICABLE   = new ErrorCode(1_050_007_007, "该售后类型不涉及退款");
    ErrorCode AFTERSALE_INVOICE_NOT_APPLICABLE  = new ErrorCode(1_050_007_008, "该售后类型不涉及红字发票");

    // ========== 客服工单 1-050-008-xxx ==========
    ErrorCode CS_TASK_NOT_EXISTS        = new ErrorCode(1_050_008_000, "工单不存在");
    ErrorCode CS_TASK_NOT_PENDING       = new ErrorCode(1_050_008_001, "仅待接单状态可接单");
    ErrorCode CS_TASK_NOT_IN_PROGRESS   = new ErrorCode(1_050_008_002, "仅处理中状态可交付或转单");
    ErrorCode CS_TASK_NOT_DELIVERED     = new ErrorCode(1_050_008_003, "仅已交付状态可验收");
    ErrorCode CS_TASK_ALREADY_CLOSED    = new ErrorCode(1_050_008_004, "工单已关闭，不可操作");
    ErrorCode CS_TASK_NOT_ASSIGNEE      = new ErrorCode(1_050_008_005, "非当前处理人，无权操作");
    ErrorCode CS_TASK_NOT_CREATOR       = new ErrorCode(1_050_008_006, "非提单人，无权验收");
    ErrorCode CS_TASK_TRANSFER_SAME     = new ErrorCode(1_050_008_007, "不可转单给自己");
    ErrorCode CS_TASK_NOT_PENDING_OR_IN_PROGRESS = new ErrorCode(1_050_008_008, "仅待接单或处理中状态可取消");

    // ========== 操作请求 1-050-008-0xx ==========
    ErrorCode CS_OPREQ_NOT_EXISTS       = new ErrorCode(1_050_008_010, "操作请求不存在");
    ErrorCode CS_OPREQ_NOT_PENDING      = new ErrorCode(1_050_008_011, "仅待处理状态可接单");
    ErrorCode CS_OPREQ_NOT_IN_PROGRESS  = new ErrorCode(1_050_008_012, "仅处理中状态可提交");
    ErrorCode CS_OPREQ_NOT_DELIVERED    = new ErrorCode(1_050_008_013, "仅等待验收状态可验收");
    ErrorCode CS_OPREQ_ALREADY_CLOSED   = new ErrorCode(1_050_008_014, "操作请求已完成");
    ErrorCode CS_OPREQ_NOT_ASSIGNEE     = new ErrorCode(1_050_008_015, "非当前处理人");
    ErrorCode CS_OPREQ_CODE_DUPLICATE   = new ErrorCode(1_050_008_016, "编号重复");
    ErrorCode CS_OPREQ_SOURCE_NOT_EXISTS= new ErrorCode(1_050_008_017, "来源业务不存在");

    // ========== 附件 1-050-008-0xx ==========
    ErrorCode CS_ATTACHMENT_NOT_EXISTS  = new ErrorCode(1_050_008_020, "附件不存在");
    ErrorCode CS_ATTACHMENT_UPLOAD_FAIL = new ErrorCode(1_050_008_021, "附件上传失败");

    // ========== 咨询会话 1-050-009-xxx ==========
    ErrorCode CS_SESSION_NOT_EXISTS      = new ErrorCode(1_050_009_000, "咨询会话不存在");
    ErrorCode CS_SESSION_NOT_PENDING     = new ErrorCode(1_050_009_001, "仅待处理状态可接单");
    ErrorCode CS_SESSION_NOT_PROCESSING  = new ErrorCode(1_050_009_002, "仅处理中状态可完成");
    ErrorCode CS_SESSION_NOT_COMPLETED   = new ErrorCode(1_050_009_003, "仅已完成状态可操作");
    ErrorCode CS_SESSION_ALREADY_CLOSED  = new ErrorCode(1_050_009_004, "咨询已关闭");
    ErrorCode CS_SESSION_NOT_ASSIGNEE    = new ErrorCode(1_050_009_005, "非当前处理人，无权操作");
    ErrorCode CS_SESSION_NOT_INITIATOR   = new ErrorCode(1_050_009_006, "非发起人，无权关闭");
    ErrorCode CS_SESSION_DUPLICATE       = new ErrorCode(1_050_009_007, "已存在相同咨询");

    // ========== 政策看板 1-050-010-xxx ==========
    ErrorCode POLICY_NOT_EXISTS      = new ErrorCode(1_050_010_000, "政策不存在");
    ErrorCode POLICY_CODE_EXISTS     = new ErrorCode(1_050_010_001, "政策编码已存在");
    ErrorCode POLICY_INDICATOR_INVALID = new ErrorCode(1_050_010_002, "指标数据不合法");

    // ========== 操作请求 1-050-011-xxx ==========
    ErrorCode OP_REQUEST_NOT_EXISTS          = new ErrorCode(1_050_011_000, "操作请求不存在");
    ErrorCode OP_REQUEST_TYPE_NOT_SUPPORTED  = new ErrorCode(1_050_011_001, "不支持的操作请求类型");
    ErrorCode OP_REQUEST_STATUS_INVALID      = new ErrorCode(1_050_011_002, "操作请求状态不合法");
    ErrorCode OP_REQUEST_NOT_ASSIGNEE        = new ErrorCode(1_050_011_003, "非当前处理人，无权操作");

}
