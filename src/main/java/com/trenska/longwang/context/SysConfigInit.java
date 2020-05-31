package com.trenska.longwang.context;

import com.trenska.longwang.constant.DragonConstant;
import com.trenska.longwang.entity.sys.SysConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * 系统初始化类
 */
@Component
public class SysConfigInit implements ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {
	private ConfigurableApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = (ConfigurableApplicationContext) applicationContext;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
		/* sys_emp_id为10000的记录为系统默认配置*/
		SysConfig sysConfig = new SysConfig().selectById(DragonConstant.DEFAULT_CONFIG_NUMBER);
		DefaultListableBeanFactory beanFactory =
				(DefaultListableBeanFactory) contextRefreshedEvent.getApplicationContext().getAutowireCapableBeanFactory();
		beanFactory.registerSingleton(DragonConstant.SYS_CONFIG_IDENTIFIER.concat(String.valueOf(DragonConstant.DEFAULT_CONFIG_NUMBER)), sysConfig);
	}
}