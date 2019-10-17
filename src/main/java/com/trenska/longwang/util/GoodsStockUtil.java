package com.trenska.longwang.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trenska.longwang.constant.Constant;
import com.trenska.longwang.entity.goods.Goods;
import com.trenska.longwang.entity.stock.GoodsStock;
import com.trenska.longwang.entity.stock.StockDetail;
import com.trenska.longwang.entity.stock.StockDetails;

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
		String madeDate = stockDetail.getMadeDate();
		String stockPrice = stockDetail.getStockPrice();

		// 商品批次库存
		GoodsStock goodsStock = new GoodsStock().selectOne(
				new LambdaQueryWrapper<GoodsStock>()
						.eq(GoodsStock::getGoodsId, goodsId)
						.eq(GoodsStock::getMadeDate, madeDate)
						.eq(GoodsStock::getStockPrice, stockPrice)
		);
		if(null != goodsStock){
			Integer num = goodsStock.getNum();
			goodsStock.setNum(num + history);
			goodsStock.updateById();
		}else {
			new GoodsStock(goodsId,madeDate,history,stockPrice).insert();
		}

		// 商品总库存
		Goods dbGoods = new Goods().selectById(goodsId);
		Integer stock = dbGoods.getStock();
		dbGoods.setStock(stock + history);
		dbGoods.updateById();


		String currentTime = TimeUtil.getCurrentTime(Constant.TIME_FORMAT);

		// 库存明细
		StockDetails stockDetails = new StockDetails();
		ObjectCopier.copyProperties(stockDetail,stockDetails);
		stockDetails.setEmpId(empId);
		stockDetails.setStockTime(currentTime);
		stockDetails.setHistory("+" + history);
		stockDetails.setStock(stock + history);
		stockDetails.setStockType(stockDetail.getStockType().concat(Constant.ZF)); // 库存类型: 订货单(核改)
		stockDetails.insert();

		stockDetail.deleteById();

	}
}
