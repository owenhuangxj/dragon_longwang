package com.trenska.longwang.service.impl.goods;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.dao.goods.UnitMapper;
import com.trenska.longwang.entity.goods.Unit;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.goods.IUnitService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * @author Owen
 * @since 2019-04-07
 */
@Service
public class UnitServiceImpl extends ServiceImpl<UnitMapper, Unit> implements IUnitService {

	@Override
	public Page<Unit> getUnitPage(Page page) {
		List<Unit> records = super.baseMapper.selectUnitPage(page);
		page.setRecords(records);
		int total = super.baseMapper.selectUnitPageCount();
		page.setTotal(total);
		return page;
	}

	@Override
	public Page<Unit> getUnitPageByName(Page page, String unitName) {
		List<Unit> records = super.baseMapper.selectUnitPageByName(page, unitName);
		page.setRecords(records);
		page.setTotal(count(new QueryWrapper<Unit>().like("unit_name",unitName)));
		return page;
	}

	@Override
	public Page<Unit> getUnitPageByStat(Page page, Boolean stat) {
		List<Unit> records = super.baseMapper.selectUnitPageByStat(page, stat);
		page.setRecords(records);
		page.setTotal(count(new QueryWrapper<Unit>().eq("stat",stat)));
		return page;
	}

	@Override
	public Page<Unit> getUnitPageSelective(Page page, Unit unit) {
		List<Unit> units = super.baseMapper.selectUnitPageSelective(page,unit);
		page.setRecords(units);
		page.setTotal(super.baseMapper.selectCountSelective(unit));
		return page;
	}

	@Override
	public ResponseModel removeUnitByIds(Collection<Integer> unitIds) {

		return ResponseModel.getInstance().succ(true).msg("商品单位删除成功");
	}

	@Override
	public Unit getUnit(String unitName) {
		return super.baseMapper.selectUnitByName(unitName);
	}

	@Override
	public boolean updateUnit(Unit dbUnit) {
		return super.baseMapper.updateUnitById(dbUnit);
	}

}
