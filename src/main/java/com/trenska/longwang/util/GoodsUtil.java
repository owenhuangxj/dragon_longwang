package com.trenska.longwang.util;

import com.trenska.longwang.constant.DragonConstant;
import com.trenska.longwang.dao.stock.StockMapper;
import com.trenska.longwang.entity.goods.Goods;
import com.trenska.longwang.entity.stock.GoodsStock;
import com.trenska.longwang.entity.stock.Stock;
import com.trenska.longwang.entity.stock.StockDetail;
import com.trenska.longwang.model.sys.CommonResponse;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 2019/5/15
 * 创建人:Owen
 */
public class GoodsUtil {
	private final static Lock lock = new ReentrantLock();

	/**
	 * 改变商品库存
	 *
	 * @param goodsId 商品ID
	 * @param change 商品变动量
	 * @return 变动后的商品库存
	 */
	public static int changeGoodsStock(int goodsId, int change) {
		lock.lock();
		try {
			Goods goods = new Goods().selectById(goodsId);
			int newStock = goods.getStock() + change;
			new Goods(goodsId, newStock).updateById();
			return newStock;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 新建商品时处理商品的期初库存
	 */
	public static boolean dealGoodsInitStock(Goods goods, StockMapper stockMapper) {

		// 确保客户有期初库存->没有输入期初库存默认为 0
		if (!NumberUtil.isIntegerUsable(goods.getInitStock())) {
			goods.setInitStock(0);
		}
		Stock stock = new Stock();
		/**************************** empId ***************************/
		Integer empIdInToken = SysUtil.getEmpIdInToken();
		stock.setEmpId(empIdInToken);

		String stockTime = TimeUtil.getCurrentTime(DragonConstant.TIME_FORMAT);
		/**************************** 期初入库的operType为期初入库 ***************************/
		String stockNo = StockUtil.getStockNo(DragonConstant.RK_TITILE, DragonConstant.RKD_CHINESE, stockMapper);
		stock.setStockType(DragonConstant.RKD_CHINESE);

		stock.setOperType(DragonConstant.QCRK_CHINESE);
		stock.setStockNo(stockNo);
		stock.setStockTime(stockTime);
		stock.insert();

		/*********************************************保存期初入库记录*********************************************/
		StockDetail stockDetail = new StockDetail();
		ObjectCopier.copyProperties(stock, stockDetail);

		stockDetail.setGoodsId(goods.getGoodsId());
		stockDetail.setMulti(1);
		stockDetail.setNum(goods.getInitStock());
		stockDetail.setHistory(goods.getInitStock());
		stockDetail.setStock(goods.getInitStock());
//		stockDetail.setLeftStock(goods.getInitStock());
		stockDetail.setUnitId(goods.getMainUnitId());
		stockDetail.setStockPrice(goods.getPrice());
		stockDetail.setMadeDate(DragonConstant.QCRK_CHINESE);
		stockDetail.insert();
		/******************************************* 处理期初入库的库存批次 ********************************************/
		new GoodsStock(goods.getGoodsId(), DragonConstant.QCRK_CHINESE, goods.getInitStock(), goods.getPrice()).insert();
		return true;
	}


	/**
	 * 新建商品时处理商品的期初库存
	 */
	public static CommonResponse initGoodsStock(Goods goods, StockMapper stockMapper) {

		// 确保客户有期初库存->没有输入期初库存默认为 0
		Stock stock = new Stock();
		int empId = SysUtil.getEmpIdInToken();
		if (empId <= 0) {
			return CommonResponse.getInstance().succ(false).msg("登陆超时，请重新登陆");
		}
		stock.setEmpId(empId);
		String stockTime = TimeUtil.getCurrentTime(DragonConstant.TIME_FORMAT);
		/* 期初入库的operType为期初入库 */
		String stockNo = StockUtil.getStockNo(DragonConstant.RK_TITILE, DragonConstant.RKD_CHINESE, stockMapper);
		stock.setStockNo(stockNo);
		stock.setStockTime(stockTime);
		stock.setStockType(DragonConstant.RKD_CHINESE);
		stock.setOperType(DragonConstant.QCRK_CHINESE);
		stock.insert();

		/* 保存期初入库记录 */
		StockDetail stockDetail = new StockDetail();
		ObjectCopier.copyProperties(stock, stockDetail);

		Integer initStock = goods.getInitStock();
		String initMadeDate = goods.getInitMadeDate();
		stockDetail.setGoodsId(goods.getGoodsId());
		stockDetail.setMulti(1);
		stockDetail.setNum(initStock);
		stockDetail.setStock(initStock);
		stockDetail.setHistory(initStock);
		stockDetail.setMadeDate(initMadeDate);
		stockDetail.setStockPrice(goods.getPrice());
		stockDetail.setUnitId(goods.getMainUnitId());
		stockDetail.insert();
		/* 保存库存明细 */
		StockDetailsUtil.dbLogStockDetail(stockDetail);
		return CommonResponse.getInstance().succ(true);
	}

	/**
	 * 处理combine : combine是用于goodsName、goodsNo、goodsBarcode的三合一模糊匹配字段
	 *
	 * @param goods
	 * @return
	 */
	public static String dealGoodsCombineProperty(Goods goods) {
		String combine = "";
		if (goods.getGoodsName() != null && goods.getGoodsName() != "") {
			combine += goods.getGoodsName() + " ";
		}
		if (goods.getGoodsNo() != null && goods.getGoodsNo() != "") {
			combine += goods.getGoodsNo() + " ";
		}
		if (goods.getBarcode() != null && goods.getBarcode() != "") {
			combine += goods.getBarcode();
		}
		return combine;
	}
}