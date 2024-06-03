package com.trenska.longwang.config.shiro;

import com.trenska.longwang.config.shiro.cache.RedisCacheManager;
import com.trenska.longwang.config.shiro.filter.AccessControlTokenFilter;
import com.trenska.longwang.config.shiro.filter.RolesOrAuthorizationFilter;
import com.trenska.longwang.config.shiro.session.RedisSessionManager;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {
    @Value("${check.login.close:false}")
    private boolean closeLoginCheck;

    @Value("#{@environment['shiro.loginUrl'] ?: '/login.jsp'}")
    protected String loginUrl;

    @Value("#{@environment['shiro.successUrl'] ?: '/'}")
    protected String successUrl;

    @Value("#{@environment['shiro.unauthorizedUrl'] ?: null}")
    protected String unauthorizedUrl;

    @Bean
    public SysUserRealm realm() {
        return new SysUserRealm();
    }

    @Bean
    public RedisCacheManager redisCacheManager() {
        return new RedisCacheManager();
    }
    
    /**
     * Shiro默认的SessionDAO为MemorySessionDAO,自定义RedisSessionDao使用@Primary注解，否则此处多个SessionDAO实例会报错
     */
    @Bean
    protected RedisSessionManager sessionManager(SessionDAO sessionDao) {
        // DefaultWebSessionManager一次请求会多次请求Redis，RedisDefaultWebSessionManager重写retrieveSession实现多次请求只会请求redis一次
        // DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        RedisSessionManager sessionManager = new RedisSessionManager();
        sessionManager.setSessionDAO(sessionDao);
        return sessionManager;
    }

    /**
     * The SecurityManager is the heart of Shiro’s architecture and acts as a sort of 'umbrella’ object
     * that coordinates its internal security components that together form an object graph.
     * However, once the SecurityManager and its internal object graph is configured for an application,
     * it is usually left alone and application developers spend almost all of their time with the Subject API.
     * SecurityManager(此方法提供的其WEB实现给Springboot框架)是Shiro框架的核心，其管理Shiro所有组件的
     * 交互工作(参照Shiro架构图src/main/resources/static/Shiro Detailed Architecture.png)，包括如图所示组件：
     * Subject、Authenticator、Authorizer、SessionManager、CacheManager、Cryptography、Realms。所以其拥有如下方法可以设置
     * 相应的对象setAuthenticator、setAuthorizer、setCacheManager、setRealm、setSessionManager
     * 注意：方法入参参数的类型声明都是使用的顶层接口是为了提高代码可维护性，如果系统万一要改变对应实现只需要提供最新的实现并用注解
     * <code>@Primary</code>进行标明即可,而不需要再修改这里
     *
     * @param realm          Realm的具体实现类对象，本工程是ShiroRealm(里面提供具体的认证数据源数据的获取和提供用户相应的权限信息)
     * @param sessionManager SessionManager的具体实现类对象，本工程是Redis的相应实现类RedisDefaultWebSessionManager
     * @param cacheManager   CacheManager的具体实现类对象，本工程是Redis的相应实现类RedisCacheManager
     * @return SecurityManager的具体实现类对象
     */
    @Bean
    public DefaultWebSecurityManager securityManager(SysUserRealm realm, RedisSessionManager sessionManager,
                                                     RedisCacheManager cacheManager) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager(realm);
        securityManager.setSessionManager(sessionManager);
        securityManager.setCacheManager(cacheManager);
        return securityManager;
    }

    /**
     * 此方法最终向Springboot注册的ShiroFilterChainDefinition实现类Bean参照https://shiro.apache.org/spring-boot.html即可
     */
    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        Map<String, String> pathDefinitions = new LinkedHashMap<>();
        pathDefinitions.put("/swagger-ui.html", "anon");
        pathDefinitions.put("/swagger-resources", "anon");
        pathDefinitions.put("/swagger-resources/configuration/security", "anon");
        pathDefinitions.put("/swagger-resources/configuration/ui", "anon");
        pathDefinitions.put("/v2/api-docs", "anon");
        pathDefinitions.put("/webjars/springfox-swagger-ui/**", "anon");
        pathDefinitions.put("/user/**", "visitable[超级管理员,运营]");
        pathDefinitions.put("/**", "authc");
        DefaultShiroFilterChainDefinition chainDefinition = new DefaultShiroFilterChainDefinition();
        chainDefinition.addPathDefinitions(pathDefinitions);
        return chainDefinition;
    }

    /**
     * 本方法向Springboot提供的ShiroFilterFactoryBean类型Bean参照shiro-spring-boot-web-starter包中的spring.factories文件中的
     * org.apache.shiro.spring.config.web.autoconfigure.ShiroWebFilterConfiguration的
     * 父类AbstractShiroWebFilterConfiguration对ShiroFilterFactoryBean的处理
     * 注意：有自定义Filter时需要向Shiro提供ShiroFilterFactoryBean
     *
     * @param securityManager            SecurityManager：securityManager方法提供的DefaultWebSecurityManager
     * @param shiroFilterChainDefinition ShiroFilterChainDefinition：shiroFilterChainDefinition
     *                                   方法提供的DefaultShiroFilterChainDefinition
     * @return ShiroFilterFactoryBean
     */
    @Bean
    protected ShiroFilterFactoryBean shiroFilterFactoryBean(@Lazy DefaultWebSecurityManager securityManager,
                                                            ShiroFilterChainDefinition shiroFilterChainDefinition) {
        ShiroFilterFactoryBean filterFactoryBean = new ShiroFilterFactoryBean();
        filterFactoryBean.setLoginUrl(loginUrl);
        filterFactoryBean.setSuccessUrl(successUrl);
        filterFactoryBean.setUnauthorizedUrl(unauthorizedUrl);
        filterFactoryBean.setSecurityManager(securityManager);

        // 此处是Shiro注册自定义过滤器，注意：一定要new，如果让Spring管理这个过滤器会造成SecurityUtils.getSubject()获取到null的情况
        Map<String, Filter> filters = filterFactoryBean.getFilters();
        filters.put("rolesOr", new RolesOrAuthorizationFilter());
        filters.put("visitable", new AccessControlTokenFilter(closeLoginCheck));
        filterFactoryBean.setFilterChainDefinitionMap(shiroFilterChainDefinition.getFilterChainMap());
        return filterFactoryBean;
    }
}