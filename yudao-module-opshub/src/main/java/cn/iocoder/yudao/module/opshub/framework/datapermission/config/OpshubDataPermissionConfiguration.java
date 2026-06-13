package cn.iocoder.yudao.module.opshub.framework.datapermission.config;

import cn.iocoder.yudao.framework.common.biz.system.permission.PermissionCommonApi;
import cn.iocoder.yudao.module.opshub.dal.mysql.dealer.DealerUserScopeMapper;
import cn.iocoder.yudao.module.opshub.dal.mysql.dealer.ExecutorProductLineScopeMapper;
import cn.iocoder.yudao.module.opshub.framework.datapermission.rule.DealerDataPermissionRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpsHub 数据权限配置
 * <p>
 * 注册 DealerDataPermissionRule Bean。
 * Step 1 暂不配置业务表过滤（后续 Step 添加表名映射），
 * 业务模块使用时通过 @DataPermission(includeRules = DealerDataPermissionRule.class) 注解启用。
 */
@Configuration(proxyBeanMethods = false)
public class OpshubDataPermissionConfiguration {

    @Bean
    public DealerDataPermissionRule dealerDataPermissionRule(PermissionCommonApi permissionApi,
                                                              DealerUserScopeMapper dealerUserScopeMapper,
                                                              ExecutorProductLineScopeMapper executorProductLineScopeMapper) {
        DealerDataPermissionRule rule = new DealerDataPermissionRule(
                permissionApi, dealerUserScopeMapper, executorProductLineScopeMapper);
        // Step 1：暂不注册表名映射，后续 Step 中按需调用 addDealerColumn / addProductLineColumn
        // 示例：rule.addDealerColumn("ops_signing_contract");
        // 示例：rule.addProductLineColumn("ops_aftersale_order");
        return rule;
    }

}
