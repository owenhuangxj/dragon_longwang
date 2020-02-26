package com.trenska.longwang.service.stock;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.entity.goods.Goods;
import com.trenska.longwang.entity.stock.Stock;
import com.trenska.longwang.entity.stock.StockDetail;
import com.trenska.longwang.entity.stock.StockDetails;
import com.trenska.longwang.model.report.GoodsStockSummarizingModel;
import com.trenska.longwang.model.report.GoodsStockSummationModel;
import com.trenska.longwang.model.report.GoodsStockinStatisticsModel;
import com.trenska.longwang.model.report.GoodsStockinSummationModel;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 2019/4/17
 * 创建人:Owen
 */
public interface IStockService extends IService<Stock> {

	Page<StockDetails> getStockDetaislPage(Map<String, Object> params, Page page);

	Page<Goods> getStockStatusPageSelective(Map<String,Object> params, Page page);

	Page<Stock> getStockPageSelective(Map<String, Object> params, Page page);

	Boolean cancelStockin(Map<String, Object> params);

	Map<String,Object>  prinAndPdf(Long stockId);

	Page<GoodsStockSummarizingModel> getGoodsStockSummarizing(Map<String, Object> params, Page page);

	GoodsStockSummationModel getGoodsStockSummation(Map<String, Object> params);

	Page<GoodsStockinStatisticsModel> getGoodsStockinStatistics(Map<String, Object> params, Page page);

	GoodsStockinSummationModel getGoodsStockinSummation(Map<String, Object> params);

//	Page<Stock> getDetailPageSelective(Map<String, Object> params, Page page);

}
