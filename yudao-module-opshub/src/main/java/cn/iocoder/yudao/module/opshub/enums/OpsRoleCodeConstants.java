package cn.iocoder.yudao.module.opshub.enums;

import java.util.Set;

/**
 * OpsHub 业务角色标识常量
 *
 * 与 system_role.code 字段值保持一致。
 * 不修改 system 模块的 RoleCodeEnum，在 opshub 模块内通过字符串常量引用。
 */
public interface OpsRoleCodeConstants {

    String SUPER_ADMIN = "super_admin";
    String BRAND_ADMIN = "brand_admin";
    String BRAND_SALES = "brand_sales";
    String SERVICE_EXECUTOR = "service_executor";
    String DEALER = "dealer";
    String PROCESS_ADMIN = "process_admin";

    /** 基于产品线维度的角色集合（brand_admin / brand_sales / service_executor） */
    Set<String> PRODUCT_LINE_SCOPE_ROLES = Set.of(BRAND_ADMIN, BRAND_SALES, SERVICE_EXECUTOR);

    /** 基于经销商维度的角色集合（dealer） */
    Set<String> DEALER_SCOPE_ROLES = Set.of(DEALER);

}
