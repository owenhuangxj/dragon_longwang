package com.trenska.longwang.service.impl.stock;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.dao.stock.ShiftMapper;
import com.trenska.longwang.entity.stock.Shift;
import com.trenska.longwang.service.stock.IShiftService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Owen
 * @since 2019-07-04
 */
@Service
public class ShiftServiceImpl extends ServiceImpl<ShiftMapper, Shift> implements IShiftService {

	@Override
	public Page<Shift> getShiftPage(Page page) {
		List<Shift> records = super.baseMapper.selectShiftPage(page);
		page.setRecords(records);
		page.setTotal(count());
		return page;
	}
}
