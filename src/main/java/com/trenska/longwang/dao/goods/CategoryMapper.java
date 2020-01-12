package com.trenska.longwang.dao.goods;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.trenska.longwang.entity.goods.Category;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品分类表 Mapper 接口
 * @author Owen
 * @since 2019-04-07
 */
//@CacheNamespace(implementation = RedisCacheMybatis.class, eviction = RedisCacheMybatis.class)
public interface CategoryMapper extends BaseMapper<Category> {
	//	@Select("select * from t_category where pid = #{catId}")
	List<Category> selectSubCategoryPageByCatId(Pagination page, Integer catId, Boolean stat);

	//	@Select("select * from t_category where cat_name like concat('%',#{catName},'%')")
	List<Category> selectCategoryPageByName(Pagination page, @Param("catName") String catName);

	//	@Select("select * from t_category where stat = #{stat}")
	List<Category> selectCategoryPageByStat(Pagination page, @Param("stat") Boolean stat);

	List<Category> selectCategoryPageSelective(Pagination page, Category category);

	int selectCountSelective(Category category);

	List<Category> selectCategoryPage(Pagination page);

	boolean setDeletable();

	boolean setUndeletable(String catName);

}
