package com.trenska.longwang;

import com.trenska.longwang.config.DbRetrieveThreadPool;
import com.trenska.longwang.config.DbRetrieveThreadPoolProperties;
import com.trenska.longwang.converters.StringToBrandConverter;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Collections;

@EnableWebMvc
@EnableCaching
@SpringBootApplication
@EnableTransactionManagement
@EnableEncryptableProperties
@EnableConfigurationProperties
// 支持处理用AspectJ的@Aspect注释标记的组件
@EnableAspectJAutoProxy
// MapperScan扫描java的Mapper文件
@MapperScan(basePackages = {"com.trenska.longwang.dao"})
public class DragonApplication {
	public static void main(String[] args) {
		SpringApplication.run(DragonApplication.class, args);
	}

	@Bean
	public DbRetrieveThreadPool dbRetrieveThreadPool(DbRetrieveThreadPoolProperties properties) {
		return new DbRetrieveThreadPool(properties);
	}
}