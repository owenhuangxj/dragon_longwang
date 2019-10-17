package com.trenska.longwang.dao.goods;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.trenska.longwang.entity.goods.Brand;

import java.util.List;

/**
 *  商品品牌Mapper 接口
 * @author Owen
 * @since 2019-04-07
 */
//@CacheNamespace(implementation = RedisCacheMybatis.class, eviction = RedisCacheMybatis.class)
public interface BrandMapper extends BaseMapper<Brand> {
//	@Select("select * from t_brand")
	List<Brand> selectBrandPage(Pagination page);
//	@Select("select * from t_brand where brand_name like concat('%',#{brandName},'%')")
	List<Brand> selectBrandPageByName(Pagination page, String brandName);
//	@Select("select * from t_brand where stat= #{stat}")
	List<Brand> selectBrandPageByStat(Pagination page ,Boolean state);

	List<Brand> selectBrandPageSelective(Pagination page, Brand brand);

	int selectCountSelective(Brand brand);

	boolean setDeletable();

	boolean setUndeletable(String brandName);
}
