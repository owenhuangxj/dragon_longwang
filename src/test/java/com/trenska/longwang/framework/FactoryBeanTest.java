package com.trenska.longwang.framework;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class FactoryBeanTest {
    public static void main(String[] args) {
        BeanFactory beanFactory =
                new AnnotationConfigApplicationContext(AppConfig.class);
        Object goodsServiceImpl = beanFactory.getBean("&goodsServiceImpl");
        System.out.println("12345");
    }
}
