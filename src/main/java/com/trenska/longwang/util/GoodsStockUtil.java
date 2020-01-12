package com.trenska.longwang.util;

import com.trenska.longwang.constant.Constant;
import com.trenska.longwang.entity.goods.Goods;
import com.trenska.longwang.entity.stock.StockDetail;

/**
 * 2019/9/4
 * 创建人:Owen
 */
public class GoodsStockUtil {
	/**
	 * 返回商品库存（批次库存和总库存）
	 * 删除库存商品信息 t_stock_detail中的信息
	 * 保存库存明细 插入一条信息到t_stock_details中
	 * @param stockDetail
	 */
	public static void deleteStockDetailAndReturnGoodsStock(StockDetail stockDetail,int empId){
		Integer goodsId = stockDetail.getGoodsId();
		Integer history = stockDetail.getHistory();
		// 商品库存
		Goods dbGoods = new Goods().selectById(goodsId);
		Integer stock = dbGoods.getStock();
		dbGoods.setStock(stock + history);
		dbGoods.updateById();
		stockDetail.deleteById();
		stockDetail.setEmpId(empId);
		stockDetail.setStock(stock + history);
		stockDetail.setStockType(stockDetail.getStockType().concat(Constant.ZF)); // 库存类型: 订货单(核改)
		StockDetailsUtil.dbLogStockDetail(stockDetail);
	}
}