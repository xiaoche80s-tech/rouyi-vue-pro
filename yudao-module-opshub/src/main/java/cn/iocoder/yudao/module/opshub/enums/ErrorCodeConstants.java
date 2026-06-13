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

}
