package com.trenska.longwang.service.stock;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.entity.indent.StockMadedate;
import com.trenska.longwang.entity.stock.Stock;
import com.trenska.longwang.entity.stock.StockDetail;
import com.trenska.longwang.model.stock.StockWarningModel;
import com.trenska.longwang.model.sys.ResponseModel;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

/**
 * 入库明细 服务接口
 * @author Owen
 * @since 2019-04-15
 */
public interface IStockDetailService extends IService<StockDetail> {

	ResponseModel stockin(Stock stock);

	ResponseModel stockout(Stock stock, HttpServletRequest request);

	Page<Stock> getStockDetailPageSelective(Map<String, Object> params, Page page);

	Page<StockMadedate> getGoodsMadeDateDetail(Map<String,Object> params, Page page);

	Page<StockDetail> getGoodsMadeDateStockInfo(Map<String, Object> params, Page page);

	Page<StockWarningModel> getStockWarningPage(Map<String, Object> params, Page page);

	ResponseModel cancelStockin(String stockNo);

	ResponseModel cancelStockout(String stockNo);

	ResponseModel changeStockin(Stock stock) throws IOException;

	//	Page<List<OverBrkModel>> getOverBrkStockPage(Map<String, Object> params, Page page);
}
