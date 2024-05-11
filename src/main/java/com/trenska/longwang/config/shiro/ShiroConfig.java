package com.trenska.longwang.config.shiro;

import com.trenska.longwang.filter.AccessControlTokenFilter;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {
    @Value("${check.login.close:true}")
    private boolean closeLoginCheck;

    @Bean
    public static LifecycleBeanPostProcessor getLifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    /**
     * DefaultAdvisorAutoProxyCreator 和 AuthorizationAttributeSourceAdvisor :
     * 用于解决无法识别@RequiresPermissons和@RequiresRoles注解
     */
    @Bean
    public static DefaultAdvisorAutoProxyCreator getDefaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);
        return creator;
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor =
                new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }

    @Bean
    public SecurityManager securityManager(Realm realm, SessionManager sessionManager, CacheManager cacheManager) {
        DefaultSecurityManager securityManager = new DefaultWebSecurityManager(realm);
        securityManager.setCacheManager(cacheManager);
        securityManager.setSessionManager(sessionManager);
        return securityManager;
    }

    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition chainDefinition = new DefaultShiroFilterChainDefinition();
        chainDefinition.addPathDefinition("/swagger-ui.html", "anon");
        chainDefinition.addPathDefinition("/swagger-resources", "anon");
        chainDefinition.addPathDefinition("/swagger-resources/configuration/security", "anon");
        chainDefinition.addPathDefinition("/swagger-resources/configuration/ui", "anon");
        chainDefinition.addPathDefinition("/v2/api-docs", "anon");
        chainDefinition.addPathDefinition("/webjars/springfox-swagger-ui/**", "anon");
        chainDefinition.addPathDefinition("/user/login", "anon");
        chainDefinition.addPathDefinition("/user/logout", "anon");
        chainDefinition.addPathDefinition("/**", "authc");
        return chainDefinition;
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager,
                                                         ShiroFilterChainDefinition filterChainDefinition) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinition.getFilterChainMap());

        // 向Shiro注册自定义拦截器，此处只是注册，下面还要明确告诉Shiro 该拦截器要处理的url
        Map<String, Filter> filtersMap = new LinkedHashMap();
        filtersMap.put("authc", new AccessControlTokenFilter(closeLoginCheck));
        shiroFilterFactoryBean.setFilters(filtersMap);
        return shiroFilterFactoryBean;
    }
}