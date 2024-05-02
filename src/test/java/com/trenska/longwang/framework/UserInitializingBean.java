package com.trenska.longwang.framework;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class UserInitializingBean implements InitializingBean {
    // 第二执行......
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("afterPropertiesSet>>>>>>");
    }

    // 最先执行......
    @PostConstruct
    public void postConstruct() {
        System.out.println("postConstruct>>>>>>");
    }

    // 最后执行......
    public void init() {
        System.out.println("init>>>>>>");
    }
}
