package cn.iocoder.yudao.module.opshub.framework.security.config;

import cn.iocoder.yudao.framework.security.config.AuthorizeRequestsCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * OpsHub 模块的 Security 配置
 */
@Configuration(proxyBeanMethods = false, value = "opshubSecurityConfiguration")
public class SecurityConfiguration {

    @Bean("opshubAuthorizeRequestsCustomizer")
    public AuthorizeRequestsCustomizer authorizeRequestsCustomizer() {
        return new AuthorizeRequestsCustomizer() {

            @Override
            public void customize(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
                // opshub 模块暂时无需放行的公开接口
                // 如有需要在此添加，例如：registry.requestMatchers("/opshub/public/**").permitAll();
            }

        };
    }

}
