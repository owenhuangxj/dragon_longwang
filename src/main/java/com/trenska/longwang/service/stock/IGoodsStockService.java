package com.trenska.longwang.service.stock;

import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.entity.stock.GoodsStock;
import com.baomidou.mybatisplus.extension.service.IService;
import com.trenska.longwang.model.stock.GoodsStockModel;

import java.util.Map;

/**
 * @author Owen
 * @since 2019-06-15
 */
public interface IGoodsStockService extends IService<GoodsStock> {

	Page<GoodsStock> getGoodsMadeDate(Map<String, Object> params, Page page);

	Page<GoodsStockModel> getGoodsMadeDates(Page page);
}
