package com.trenska.longwang.config;

import org.apache.shiro.realm.Realm;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import com.trenska.longwang.filter.AccessControlTokenFilter;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.web.session.mgt.ServletContainerSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.HashMap;
import javax.servlet.Filter;
import java.util.LinkedHashMap;

@Configuration
public class ShiroConfig {

	@Bean
	public static LifecycleBeanPostProcessor getLifecycleBeanPostProcessor() {
		return new LifecycleBeanPostProcessor();
	}

	/**
	 * DefaultAdvisorAutoProxyCreator 和 AuthorizationAttributeSourceAdvisor : 用于解决无法识别@RequiresPermissons和@RequiresRoles注解
	 */
	@Bean
	public static DefaultAdvisorAutoProxyCreator getDefaultAdvisorAutoProxyCreator() {
		DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
		creator.setProxyTargetClass(true);
		return creator;
	}

	@Bean
	public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor() {
		AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
		authorizationAttributeSourceAdvisor.setSecurityManager(securityManager());
		return authorizationAttributeSourceAdvisor;
	}

	/**
	 * 用于用户名密码登录时认证的realm
	 */
	@Bean("realm")
	public Realm realm() {
		return new SysUserRealm();
	}

	@Bean
	public SessionManager sessionManager(){
		return new ServletContainerSessionManager();
	}

	@Bean("securityManager")
	public SecurityManager securityManager() {
		DefaultSecurityManager securityManager = new DefaultWebSecurityManager();
		securityManager.setSessionManager(sessionManager());
		securityManager.setRealm(realm());
		return securityManager;
	}

	@Bean
	public ShiroFilterFactoryBean shiroFilterFactoryBean() {
		ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
		shiroFilterFactoryBean.setSecurityManager(securityManager());
		// 向Shiro注册自定义拦截器，此处只是注册，下面还要明确告诉Shiro 该拦截器要处理的url
		Map<String, Filter> filtersMap = new LinkedHashMap();
		filtersMap.put("authc", new AccessControlTokenFilter());
		shiroFilterFactoryBean.setFilters(filtersMap);
		/**
		 * LinkedHashMap 是有序的map，和队列的作用相同，先进先出
		 */
		HashMap<String, String> filterChainDefinitionMap = new LinkedHashMap();

		filterChainDefinitionMap.put("/swagger-ui.html", "anon");
		filterChainDefinitionMap.put("/swagger-resources", "anon");
		filterChainDefinitionMap.put("/swagger-resources/configuration/security", "anon");
		filterChainDefinitionMap.put("/swagger-resources/configuration/ui", "anon");
		filterChainDefinitionMap.put("/v2/api-docs", "anon");
		filterChainDefinitionMap.put("/webjars/springfox-swagger-ui/**", "anon");
		filterChainDefinitionMap.put("/user/login", "anon");
		filterChainDefinitionMap.put("/user/logout", "anon");

//		filterChainDefinitionMap.put("/**", "anon"); //所有请求都不拦截
		// 告诉Shiro自定义拦截器处理的url
		filterChainDefinitionMap.put("/**", "authc");
		shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);

		return shiroFilterFactoryBean;
	}

}