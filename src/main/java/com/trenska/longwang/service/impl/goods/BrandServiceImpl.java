package com.trenska.longwang.service.impl.goods;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.dao.goods.BrandMapper;
import com.trenska.longwang.dao.goods.GoodsMapper;
import com.trenska.longwang.entity.goods.Brand;
import com.trenska.longwang.entity.goods.Goods;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.goods.IBrandService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Owen
 * @since 2019-04-07
 */
@Service
@SuppressWarnings("all")
public class BrandServiceImpl extends ServiceImpl<BrandMapper, Brand> implements IBrandService {

	@Autowired
	private GoodsMapper goodsMapper;

	@Override
	public Page<Brand> getBrandPage(Page page) {
		List<Brand> records = super.baseMapper.selectBrandPage(page);
		page.setRecords(records);
		page.setTotal(count());
		return page;
	}
	@Override
	public Page<Brand> getBrandPageSelective(Page page, Brand brand) {
		List<Brand> brands = super.baseMapper.selectBrandPageSelective(page,brand);
		page.setRecords(brands);
		page.setTotal(super.baseMapper.selectCountSelective(brand));
		return page;
	}

	@Override
	public ResponseModel updateBrand(Brand brand) {

		Brand oldBrand = this.getById(brand.getBrandId());

		if(null == oldBrand){
			return ResponseModel.getInstance().succ(false).msg("无效的品牌信息");
		}

		// 如果更新品牌的名称，需要同步商品的品牌名称
		if(StringUtils.isNotEmpty(brand.getBrandName()) && !oldBrand.getBrandName().equals(brand.getBrandName())){
			Goods goods = new Goods();
			goods.setBrandName(brand.getBrandName());
			goodsMapper.update(
					goods,
					new LambdaQueryWrapper<Goods>()
							.eq(Goods::getBrandName,oldBrand.getBrandName())
			);
		}

		this.updateById(brand);
		return ResponseModel.getInstance().succ(true).msg("修改品牌成功");

	}

	@Override
	public ResponseModel removeBrandByIds(Collection<Integer> brandIds) {

		Collection<Brand> brands = this.listByIds(brandIds);

		// 筛选出正在使用的品牌
		Set<Brand> brandSet = brands.stream().filter(brand -> brand.getDeletable() == false).collect(Collectors.toSet());

		if(!brandSet.isEmpty() && brandSet.size() == brandIds.size()){
			return ResponseModel.getInstance().succ(false).msg("不可删除正在使用的品牌");
		}

		this.remove(
				new LambdaQueryWrapper<Brand>()
						.eq(Brand::getDeletable,true)
						.in(Brand::getBrandId,brandIds)
		);
		return ResponseModel.getInstance().succ(true).msg("商品品牌删除成功");
	}

	@Override
	public Page<Brand> getBrandPageByName(Page page, String brandName) {
		List<Brand> records = super.baseMapper.selectBrandPageByName(page, brandName);
		page.setTotal(count(new QueryWrapper<Brand>().like("brand_name",brandName)));
		page.setRecords(records);
		return page;
	}

	@Override
	public Page<Brand> getBrandPageByStat(Page page, Boolean stat) {
		List<Brand> brands = super.baseMapper.selectBrandPageByStat(page,stat);
		page.setRecords(brands);
		page.setTotal(count(new QueryWrapper<Brand>().eq("stat",stat)));
		return page;
	}

}
