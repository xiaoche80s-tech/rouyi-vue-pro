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
        // Step 2：注册基础数据表的经销商维度过滤
        rule.addDealerColumn("ops_basedata_file");
        // 基础数据与产品线无关，不注册 productLineColumn

        // Step 3：注册签约进度表
        rule.addDealerColumn("ops_signing_contract");
        rule.addProductLineColumn("ops_signing_contract");
        return rule;
    }

}
