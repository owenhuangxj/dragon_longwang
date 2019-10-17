package com.trenska.longwang.service.stock;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.entity.stock.Shift;

/**
 * @author Owen
 * @since 2019-07-04
 */
public interface IShiftService extends IService<Shift> {

	Page<Shift> getShiftPage(Page page);
}
