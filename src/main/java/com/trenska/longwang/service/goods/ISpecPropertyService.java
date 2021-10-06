package com.trenska.longwang.service.goods;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.entity.goods.SpecProperty;
import com.trenska.longwang.model.sys.CommonResponse;

import java.util.Collection;

/**
 * <p>
 * 商品规格属性业务层，与商品规格表是 1：n 的关系 服务类
 * </p>
 *
 * @author Owen
 * @since 2019-04-09
 */
public interface ISpecPropertyService extends IService<SpecProperty> {

	Page<SpecProperty> getSpecPropertiesPageByName(Page page, String propName);

	Page<SpecProperty> getSpecPropertiesPageByStat(Page page, Boolean stat);

	Page<SpecProperty> getSpecPropertiesPage(Page page);

	/**
	 * 商品规格模块在列出下级商品规格属性时使用了该功能
	 */
	Page<SpecProperty> getSpecPropertiesPageBySpecId(Page page, Integer specId, Boolean stat);

	Page<SpecProperty> getSpecPropertiesPageSelective(Page page, SpecProperty specProperty);

	boolean updateSpecPropertyById(SpecProperty specProperty);

	CommonResponse removeSpecPropertyByIds(Collection<Integer> specPropIds);
}
