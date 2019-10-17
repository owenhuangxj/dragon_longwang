package com.trenska.longwang.service.impl.goods;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.trenska.longwang.dao.goods.GoodsSpecMapper;
import com.trenska.longwang.dao.goods.SpecMapper;
import com.trenska.longwang.dao.goods.SpecPropertyMapper;
import com.trenska.longwang.entity.goods.GoodsSpec;
import com.trenska.longwang.entity.goods.Spec;
import com.trenska.longwang.entity.goods.SpecProperty;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.goods.ISpecService;
import com.trenska.longwang.util.NumberUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.Min;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Owen
 * @since 2019-04-07
 */
@Service
@SuppressWarnings("all")
public class SpecServiceImpl extends ServiceImpl<SpecMapper, Spec> implements ISpecService {
	@Autowired
	private SpecPropertyMapper specPropertyMapper;

	@Autowired
	private GoodsSpecMapper goodsSpecMapper;

	@Override
	@Transactional
	public ResponseModel saveSpec(Spec spec) {

		int insertedSpecId = 0;
		if (null == spec.getSpecId()){ // 如果传入的specId为null表示需要保存商品规格信息
			insertedSpecId = super.baseMapper.insert(spec);
		}
		else {
			insertedSpecId = spec.getSpecId(); // 否则 不需要保存商品规格信息
		}
		int specId = insertedSpecId;
		Set<SpecProperty> specProperties = spec.getSpecProperties();

		if (CollectionUtils.isNotEmpty(specProperties)) {
			specProperties.forEach(specProperty -> {
				specProperty.setSpecId(specId);
				specPropertyMapper.insert(specProperty);
			});
		}
		return ResponseModel.getInstance().succ(true).msg("保存成功");
	}

	@Override
	@Transactional
	/**
	 * 删除规格
	 * 		1.查询规格是否被商品使用
	 * 		2.根据查询结果判断是否是物理删除还是逻辑删除
	 */
	public ResponseModel removeSpecById(Integer specId) {

		List<GoodsSpec> goodsSpecs = goodsSpecMapper.selectList(
				new LambdaQueryWrapper<GoodsSpec>()
						.eq(GoodsSpec::getSpecId, specId)
		);

		// 如果规格被使用则进行逻辑删除
		if(CollectionUtils.isNotEmpty(goodsSpecs)){
			// 先删除t_spec_property表中所有spec_id为specId的记录
			specPropertyMapper.delete(
					new LambdaQueryWrapper<SpecProperty>()
							.eq(SpecProperty::getSpecId, specId)
			);

		this.removeById(specId);
		}else { // 否则进行物理删除
			specPropertyMapper.deleteSpecProperty(specId);
			super.baseMapper.deleteSpecById(specId);
		}

		return ResponseModel.getInstance().succ(true).msg("规格删除成功");

	}

	@Override
	@Transactional
	public ResponseModel removeSpecByIds(Collection<Integer> specIds) {

		Collection<Spec> specs = this.listByIds(specIds);

		// 筛选出没有被使用的规格
		Set<Spec> unusingSpecs = specs.stream().filter(spec -> spec.getDeletable() == true).collect(Collectors.toSet());

		if(CollectionUtils.isNotEmpty(unusingSpecs)){
			List<Integer> unusingSpecIds = unusingSpecs.stream().map(Spec::getSpecId).collect(Collectors.toList());
			unusingSpecIds.forEach(specId->removeSpecById(specId));
			return ResponseModel.getInstance().succ(true).msg("规格删除成功");
		}else{
			return ResponseModel.getInstance().succ(true).msg("规格正在使用");
		}
	}

	@Override
	@Transactional
	public ResponseModel updateSpecById(Spec spec) {

		if (StringUtils.isNotEmpty(spec.getSpecName())) {

			Spec oldSpec = this.getOne(
					new LambdaQueryWrapper<Spec>()
							.eq(Spec::getSpecName,spec.getSpecName())
			);

			if(null != oldSpec){
				return ResponseModel.getInstance().succ(false).msg( "规格已经存在");
			}

			GoodsSpec goodsSpec = new GoodsSpec();
			goodsSpec.setSpecName(spec.getSpecName());
			goodsSpecMapper.update(goodsSpec,
					new LambdaUpdateWrapper<GoodsSpec>()
							.eq(GoodsSpec::getSpecId, spec.getSpecId())
			);
		}
		updateById(spec);
		return ResponseModel.getInstance().succ(true).msg( "规格修改成功");
	}


	@Override
	public Page<Spec> getSpecPage(Page page) {
		List<Spec> records = super.baseMapper.selectSpecPage(page);
		page.setRecords(records);
		page.setTotal(count());
		return page;
	}

//	@Override
//	public Page<Spec> getSpecPageSelective(Page page, Spec spec) {
//		List<Spec> records = super.baseMapper.selectSpecPageSelective(page, spec);
//		page.setRecords(records);
//		page.setTotal(super.baseMapper.selectCountSelective(spec));
//		return page;
//	}

	@Override
	public Page<Spec> getSpecPageSelective(Page page, Map<String, Object> params) {
		List<Spec> records = super.baseMapper.selectSpecPageSelective(page,params);
		int total = super.baseMapper.selectSpcePageSelectiveCount(params);
		page.setTotal(total);
		page.setRecords(records);
		return page;
	}

	@Override
	public Page<Spec> getSpecPageByStat(Page page, Boolean stat) {
		List<Spec> records = super.baseMapper.selectSpecPageByStat(page, stat);
		page.setRecords(records);
		int total = count(
				new LambdaQueryWrapper<Spec>()
						.eq(Spec::getStat, stat)
		);
		page.setTotal(total);
		return page;
	}

	@Override
	public Page<Spec> getSpecPageByName(Page page, String specName) {
		List<Spec> records = super.baseMapper.selectSpecPageByName(page, specName);
		page.setRecords(records);
		int total = count(
				new LambdaQueryWrapper<Spec>()
						.like(Spec::getSpecName, specName)
		);
		page.setTotal(total);
		return page;
	}

}
