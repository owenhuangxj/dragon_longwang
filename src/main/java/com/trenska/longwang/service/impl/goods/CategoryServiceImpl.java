package com.trenska.longwang.service.impl.goods;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.dao.goods.CategoryMapper;
import com.trenska.longwang.dao.goods.GoodsMapper;
import com.trenska.longwang.entity.goods.Category;
import com.trenska.longwang.entity.goods.Goods;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.goods.ICategoryService;
import com.trenska.longwang.service.goods.IGoodsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 商品分类服务实现类
 * @author Owen
 * @since 2019-04-07
 */
@Service
@SuppressWarnings("all")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

	@Autowired
	private GoodsMapper goodsMapper;
	@Autowired
	private IGoodsService goodsService;

	/**
	 * 级联删除需要排除主分类的pid为0的情况，因为pid为 0的情况不排除会删除掉所有主分类
	 * @param categoryId
	 */
	@Override
	@Transactional
	public Boolean removeCategoryById(Integer categoryId) {
		List<Category> subCategories = findSubCategoriesByPid(categoryId);
		Set<Integer> categoryIds = subCategories.stream().map(subCategory -> subCategory.getCatId()).collect(Collectors.toSet());
		categoryIds.add(categoryId);
		return removeByIds(categoryIds);
	}

	@Override
	@Transactional
	public ResponseModel removeCategoryByIds(Collection<Integer> categoryIds) {

		Collection<Category> categories = this.listByIds(categoryIds);

		// 筛选出正在使用的分类
		Set<Category> categorySet = categories.stream().filter(category -> category.getDeletable() == false).collect(Collectors.toSet());

		// 如果所有分类都在使用，提示
		if(!categorySet.isEmpty() && categories.size() == categoryIds.size()){
			return ResponseModel.getInstance().succ(false).msg("不能删除正在使用的分类");
		}

		this.remove(
				new LambdaQueryWrapper<Category>()
						.eq(Category::getDeletable,true)
						.in(Category::getCatId,categoryIds)
		);

		return ResponseModel.getInstance().succ(true).msg("批量删除分类信息成功");
	}

	@Override
	public Page<Category> getSubCategoryPageByCatId(Page page, Integer catId,Boolean stat) {
		List<Category> categories = super.baseMapper.selectSubCategoryPageByCatId(page, catId ,stat);
		page.setRecords(categories);
		if(stat == null) stat = true; // 这样设置是为了减少在xml文件中 写sql
		page.setTotal(count(new QueryWrapper<Category>().eq("pid",catId).eq("stat",stat)));
		return page;
	}

	@Override
	public Page<Category> getCategoryPageByName(Page page, String catName) {
		List<Category> categories  = super.baseMapper.selectCategoryPageByName(page,catName);
		page.setRecords(categories);
		page.setTotal(count(new QueryWrapper<Category>().like("cat_name",catName)));
		return page;
	}

	@Override
	public Page<Category> getSubCategoryPageByStat(Page page, Boolean stat) {
		List<Category> categories  = super.baseMapper.selectCategoryPageByStat(page,stat);
		page.setRecords(categories);
		page.setTotal(count(new QueryWrapper<Category>().eq("stat",stat)));
		return page;
	}

	@Override
	public Page<Category> getCategoryPageSelective(Page page, Category category) {
		List<Category> categories = super.baseMapper.selectCategoryPageSelective(page,category);
		page.setRecords(categories);
		page.setTotal(super.baseMapper.selectCountSelective(category));
		super.baseMapper.setDeletable();
		return page;
	}

	@Override
	public Page<Category> getCategoryPage(Page page) {
		List<Category> categories = super.baseMapper.selectCategoryPage(page);
		page.setRecords(categories);
		page.setTotal(count());
		return page;
	}

	@Override
	public ResponseModel updateCategory(Category category) {

		Category oldCategory = this.getById(category.getCatId());

		if(null == oldCategory){
			return ResponseModel.getInstance().succ(false).msg("无效的商品分类信息");
		}

		if(StringUtils.isNotEmpty(category.getCatName()) && !category.getCatName().equals(oldCategory.getCatName())){

			// 找出使用了指定分类的商品
			List<Goods> updatingGoods = goodsMapper.selectList(
					new LambdaQueryWrapper<Goods>()
							.eq(Goods::getFrtCatName,oldCategory.getCatName())
							.or()
							.eq(Goods::getScdCatName,oldCategory.getCatName())
			);

			// 筛选一级分类为指定分类名称的商品
			List<Goods> frtCats = updatingGoods.stream().filter(goods -> {
				return oldCategory.getCatName().equals(goods.getFrtCatName());
			}).collect(Collectors.toList());

			for (Goods frtCat : frtCats) {
				frtCat.setFrtCatName(category.getCatName());
			}

			// 筛选二级分类为指定分类名称的商品
			List<Goods> scdCats = updatingGoods.stream().filter(goods -> {
				return oldCategory.getCatName().equals(goods.getScdCatName());
			}).collect(Collectors.toList());

			for (Goods scdCat : scdCats) {
				scdCat.setScdCatName(category.getCatName());
			}

			frtCats.addAll(scdCats);

			if(!frtCats.isEmpty()) {
				goodsService.saveOrUpdateBatch(frtCats);
			}

		}
		this.updateById(category);

		return ResponseModel.getInstance().succ(true).msg("更新商品分类信息成功");

	}

	/**
	 * 商品分类只有主分类和子分类，所以只需通过主分类的id作为子分类的pid找到所有子分类，然后一起删除即可
	 * @param pid
	 */
	private List<Category> findSubCategoriesByPid(Integer pid){
		List<Category> categories = super.baseMapper.selectList(new QueryWrapper<Category>().eq("pid", pid));
		return categories;
	}
}
