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

}
