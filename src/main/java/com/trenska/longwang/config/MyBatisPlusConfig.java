package com.trenska.longwang.config;

import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.injector.LogicSqlInjector;
import com.baomidou.mybatisplus.extension.plugins.IllegalSQLInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PerformanceInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Properties;

@Configuration
@MapperScan("com.trenska.longwang.dao")
public class MyBatisPlusConfig {

    /**
     * 分页插件，自动识别数据库类型
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }

    /**
     * 逻辑删除插件
     * 效果: 使用Mapper自带方法删除和查找都会附带逻辑删除功能 (自己写的xml不会)
     * 同时在application.xml文件中配置
     * mybatis-plus:
     *  global-config:
     *      db-config:
     *          logic-delete-value: 0  #配置逻辑删除字段为0是删除
     *          logic-not-delete-value: 1 #配置逻辑删除字段为1是未删除
     * example
     * 删除时 update user set deleted=1 where id =1 and deleted=0
     * 查找时 select * from user where deleted=0
     */
    @Bean
    public ISqlInjector sqlInjector(){
        return new LogicSqlInjector();
    }

    /**
     * SQL执行效率插件[生产环境时关闭]
     * 打印 sql
     * properties.setProperty("format", "true"); 格式化sql语句
     */
    @Bean
    @Profile("dev")
    public PerformanceInterceptor performanceInterceptor() {
        PerformanceInterceptor performanceInterceptor = new PerformanceInterceptor();
        Properties properties = new Properties();
        properties.setProperty("format", "true");
        performanceInterceptor.setProperties(properties);
        return performanceInterceptor;
    }
}