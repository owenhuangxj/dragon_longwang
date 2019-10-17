package com.trenska.longwang.service.goods;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.entity.goods.Category;
import com.trenska.longwang.model.sys.ResponseModel;

import java.util.Collection;

/**
 * <p>
 * 商品分类表 服务类
 * </p>
 *
 * @author Owen
 * @since 2019-04-07
 */
public interface ICategoryService extends IService<Category> {

	Boolean removeCategoryById(Integer categoryId);

	ResponseModel removeCategoryByIds(Collection<Integer> categoryIds);

	Page<Category> getSubCategoryPageByCatId(Page page, Integer catId,Boolean stat);

	Page<Category> getCategoryPageByName(Page pageParam, String catName);

	Page<Category> getSubCategoryPageByStat(Page pageParam, Boolean stat);

	Page<Category> getCategoryPageSelective(Page pageParam, Category category);

	Page<Category> getCategoryPage(Page page);

	ResponseModel updateCategory(Category category);
}

