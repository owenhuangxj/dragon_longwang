package com.trenska.longwang.service.impl.goods;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.dao.goods.GoodsSpecMapper;
import com.trenska.longwang.dao.goods.SpecPropertyMapper;
import com.trenska.longwang.entity.goods.GoodsSpec;
import com.trenska.longwang.entity.goods.SpecProperty;
import com.trenska.longwang.model.sys.CommonResponse;
import com.trenska.longwang.service.goods.ISpecPropertyService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 商品规格属性业务层，与商品规格表是 1：n 的关系 服务实现类
 * </p>
 *
 * @author Owen
 * @since 2019-04-09
 */
@Service
@SuppressWarnings("all")
public class SpecPropertyServiceImpl extends ServiceImpl<SpecPropertyMapper, SpecProperty> implements ISpecPropertyService {

	@Autowired
	private GoodsSpecMapper goodsSpecMapper;

	@Override
	public Page<SpecProperty> getSpecPropertiesPage(Page page) {
		List<SpecProperty> specProperties = super.baseMapper.selectSpecPropertiesPage(page);
		page.setRecords(specProperties);
		page.setTotal(count());
		return page;
	}

	@Override
	public Page<SpecProperty> getSpecPropertiesPageSelective(Page page, SpecProperty specProperty) {
		List<SpecProperty> specProperties = super.baseMapper.selectSpecPropertiesPageSelective(page, specProperty);
		page.setRecords(specProperties);
		page.setTotal(super.baseMapper.selectCountSelective(specProperty));
		return page;
	}

	@Override
	@Transactional
	public boolean updateSpecPropertyById(SpecProperty specProperty) {

		if(StringUtils.isNotEmpty(specProperty.getPropName())){
			GoodsSpec goodsSpec = new GoodsSpec();
			goodsSpec.setPropName(specProperty.getPropName());
			goodsSpecMapper.update(goodsSpec,
					new UpdateWrapper<GoodsSpec>()
							.eq("spec_prop_id", specProperty.getSpecPropId())
			);
		}
		updateById(specProperty);
		return true;
	}

	@Override
	public CommonResponse removeSpecPropertyByIds(Collection<Integer> specPropIds) {

		Collection<SpecProperty> specProperties = this.listByIds(specPropIds);

		Set<SpecProperty> specPropertySet = specProperties.stream().filter(specProperty -> specProperty.getDeletable() == false).collect(Collectors.toSet());

		if(!specPropertySet.isEmpty() && specPropertySet.size() == specPropIds.size()){
			return CommonResponse.getInstance().succ(false).msg("不能删除正在使用的商品规格属性");
		}

		this.remove(
				new LambdaQueryWrapper<SpecProperty>()
						.eq(SpecProperty::getDeletable,true)
						.in(SpecProperty::getSpecPropId,specPropIds)
		);

		return CommonResponse.getInstance().succ(true).msg("商品规格属性删除成功");

	}

	@Override
	public Page<SpecProperty> getSpecPropertiesPageBySpecId(Page page, Integer specId, Boolean stat) {
		if(stat == null) {
			stat = true;
		}
		List<SpecProperty> specProperties = super.baseMapper.selectSpecPropertiesPageBySpecId(page, specId ,stat);
		page.setRecords(specProperties);
		page.setTotal(
				count(
						new LambdaQueryWrapper<SpecProperty>()
								.eq(SpecProperty::getSpecId,specId)
								.eq(SpecProperty::getStat,stat)
				)
		);
		return page;
	}

	@Override
	public Page<SpecProperty> getSpecPropertiesPageByName(Page page, String propName) {
		List<SpecProperty> specProperties = super.baseMapper.selectSpecPropertiesPageByName(page, propName);
		page.setRecords(specProperties);
		page.setTotal(count(new QueryWrapper<SpecProperty>().like("prop_name",propName)));
		return page;
	}

	@Override
	public Page<SpecProperty> getSpecPropertiesPageByStat(Page page, Boolean stat) {
		List<SpecProperty> specProperties = super.baseMapper.selectSpecPropertiesPageBySpecStat(page, stat);
		page.setRecords(specProperties);
		page.setTotal(count(new QueryWrapper<SpecProperty>().eq("stat",stat)));
		return page;
	}

}