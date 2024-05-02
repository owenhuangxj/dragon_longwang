package com.trenska.longwang.framework;

import com.trenska.longwang.service.goods.IGoodsService;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

@Component
public class GoodsServiceImpl implements FactoryBean<IGoodsService> {
    @Override
    public IGoodsService getObject() throws Exception {
        return new com.trenska.longwang.service.impl.goods.GoodsServiceImpl();
    }

    @Override
    public Class<?> getObjectType() {
        return IGoodsService.class;
    }
}
