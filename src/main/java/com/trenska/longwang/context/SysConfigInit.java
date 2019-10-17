package com.trenska.longwang.context;

import com.trenska.longwang.constant.Constant;
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

//	private Logger logger = LoggerFactory.getLogger(SysConfigInit.class);
//	@Value("${spring.datasource.url}")
//	private String url;
//	@Value("${spring.datasource.username}")
//	private String username;
//	@Value("${spring.datasource.password}")
//	private String password;
//	@Value("${spring.datasource.driver-class-name}")
//	private String driverClassName;

private ConfigurableApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = (ConfigurableApplicationContext) applicationContext;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
//		DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getBeanFactory();//定义spring相关信息
//		JdbcTemplate jdbcTemplate = new JdbcTemplate();
//		jdbcTemplate.setDataSource(ApplicationContextHolder.getBean("druidDataSource"));
//		beanFactory.registerSingleton(Constant.SYS_CONFIG_IDENTIFIER,jdbcTemplate.queryForObject("select * from t_sys_config where sys_emp_id = ?",new SysConfigRowMapper(),10000));
		/**
		 * sys_emp_id为10000的记录为系统默认配置
		 */
		// mybatis-plus就是爽啊......
		SysConfig sysConfig = new SysConfig().selectById(10000);
		((DefaultListableBeanFactory)contextRefreshedEvent.getApplicationContext().getAutowireCapableBeanFactory()).registerSingleton(Constant.SYS_CONFIG_IDENTIFIER,sysConfig);
	}
}