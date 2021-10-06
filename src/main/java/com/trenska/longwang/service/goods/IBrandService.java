package com.trenska.longwang.service.goods;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.entity.goods.Brand;
import com.trenska.longwang.model.sys.CommonResponse;

import java.util.Collection;

/**
 * @author Owen
 * @since 2019-04-07
 */
public interface IBrandService extends IService<Brand> {

	Page<Brand> getBrandPage(Page page);

	Page<Brand> getBrandPageByName(Page page, String brandName);

	Page<Brand> getBrandPageByStat(Page pageParam, Boolean stat);

	Page<Brand> getBrandPageSelective(Page page, Brand brand);

	CommonResponse updateBrand(Brand brand);

	CommonResponse removeBrandByIds(Collection<Integer> brandIds);

}
