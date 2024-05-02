package com.trenska.longwang.framework;

import com.trenska.longwang.converters.StringToBrandConverter;
import com.trenska.longwang.entity.goods.Brand;
import org.junit.Assert;
import org.junit.Test;

public class StringToBrandConverterTest extends Assert {
    @Test
    public void test() {
        StringToBrandConverter stringToBrandConverter = new StringToBrandConverter();
        Brand brand = stringToBrandConverter.convert("123,brand123;true false");
        assertNotNull("Target brand is null", brand);
        assertEquals("Target brandId is not 123", Integer.valueOf("123"), brand.getBrandId());
        assertEquals("Target brandName is not brand123", "brand123", brand.getBrandName());
        assertEquals("Target stat is not true", true, brand.getStat());
        assertEquals("Target deletable is not false", false, brand.getDeletable());
    }
}
