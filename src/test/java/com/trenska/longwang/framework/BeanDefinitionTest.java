package com.trenska.longwang.framework;

import com.trenska.longwang.service.impl.sys.SysEmpServiceImpl;
import com.trenska.longwang.service.sys.ISysEmpService;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.support.GenericApplicationContext;

public class BeanDefinitionTest {
    public static void main(String[] args) {
        GenericApplicationContext applicationContext = new GenericApplicationContext();
        AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition().getBeanDefinition();
        beanDefinition.setBeanClass(SysEmpServiceImpl.class);
        // Error creating bean with name 'sysEmpService': Bean definition is abstract
        // beanDefinition.setAbstract(true);
        applicationContext.registerBeanDefinition("sysEmpService",beanDefinition);
        applicationContext.refresh();
        ISysEmpService sysEmpService = applicationContext.getBean("sysEmpService",SysEmpServiceImpl.class);
        System.out.println(sysEmpService);
    }
}
