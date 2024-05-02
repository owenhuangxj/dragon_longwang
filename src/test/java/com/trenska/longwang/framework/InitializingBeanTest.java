package com.trenska.longwang.framework;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InitializingBeanTest {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext =
                new AnnotationConfigApplicationContext(InitializingBeanTest.class);
    }

    @Bean(initMethod = "init")
    public UserInitializingBean userInitializingBean() {
        return new UserInitializingBean();
    }
}
