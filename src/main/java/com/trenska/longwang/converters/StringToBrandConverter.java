package com.trenska.longwang.converters;

import com.trenska.longwang.entity.goods.Brand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;


public class StringToBrandConverter implements Converter<String, Brand> {
    /**
     * 参数分隔符
     */
    String PARAMETER_DELIMITERS = ",|;| |\t|\n";

    @Override
    public Brand convert(String source) {
        if (StringUtils.isBlank(source)) {
            return null;
        }
        String[] split = source.split(PARAMETER_DELIMITERS);
        if (split == null && split.length == 0) {
            return null;
        }
        Brand brand = new Brand();
        brand.setBrandId(Integer.valueOf(split[0]));
        brand.setBrandName(split[1]);
        brand.setStat(Boolean.valueOf(split[2]));
        brand.setDeletable(Boolean.valueOf(split[3]));
        return brand;
    }
}
