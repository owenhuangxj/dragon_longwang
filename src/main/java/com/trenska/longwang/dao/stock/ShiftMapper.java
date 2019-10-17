package com.trenska.longwang.dao.stock;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.trenska.longwang.entity.stock.Shift;

import java.util.List;

/**
 * @author Owen
 * @since 2019-07-04
 */
public interface ShiftMapper extends BaseMapper<Shift> {

	List<Shift> selectShiftPage(Pagination page);
}
