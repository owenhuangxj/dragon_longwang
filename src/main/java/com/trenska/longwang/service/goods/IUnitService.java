package com.trenska.longwang.service.goods;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.entity.goods.Unit;
import com.trenska.longwang.model.sys.CommonResponse;

import java.util.Collection;

/**
 * @author Owen
 * @since 2019-04-07
 */
public interface IUnitService extends IService<Unit> {
	Page<Unit> getUnitPage(Page page);

	Page<Unit> getUnitPageByName(Page pageParam, String unitName);

	Page<Unit> getUnitPageByStat(Page pageParam, Boolean stat);

	Page<Unit> getUnitPageSelective(Page page, Unit unit);

	CommonResponse removeUnitByIds(Collection<Integer> unitIds);

	Unit getUnit(String unitName);

	boolean updateUnit(Unit dbUnit);
}
