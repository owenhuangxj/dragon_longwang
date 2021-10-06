package com.trenska.longwang.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.trenska.longwang.constant.DragonConstant;
import com.trenska.longwang.dao.goods.GoodsMapper;
import com.trenska.longwang.dao.stock.StockMapper;
import com.trenska.longwang.entity.goods.Goods;
import com.trenska.longwang.entity.indent.Indent;
import com.trenska.longwang.entity.indent.IndentDetail;
import com.trenska.longwang.entity.indent.StockMadedate;
import com.trenska.longwang.entity.stock.GoodsStock;
import com.trenska.longwang.entity.stock.Stock;
import com.trenska.longwang.entity.stock.StockDetail;
import com.trenska.longwang.enums.IndentStat;
import com.trenska.longwang.exception.ServiceException;
import com.trenska.longwang.model.sys.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 2019/4/19
 * 创建人:Owen
 */
@Slf4j
public class StockUtil {

	private final static int LOCK_TIME = 10;
	private final static Lock lock = new ReentrantLock();

	/**
	 * @param stockType 入库单/出库单；
	 * @return
	 */
	public static String getStockNo(String prefix, String stockType, StockMapper stockMapper) throws ServiceException {
		try {
			if (lock.tryLock(LOCK_TIME, TimeUnit.MILLISECONDS)) {
				try {
					// 查询库存表中的库存类型为stockType的最后一条记录
					// Stock lastStockRecord = stockMapper.selectRecordOfMaxId(stockType);
					String stockNoOfMaxId = stockMapper.selectStockNoOfMaxId(stockType);
					/**
					 * 处理流水号的问题
					 * 如果库存表中还没有任何记录，则需要生成第一个单号 或者 如果有记录，需要比较最后一条记录的日期是否是当天，如果不是则流水号需要从1开始
					 */
					String currentDate = TimeUtil.getCurrentTime(DragonConstant.BILL_TIME_FORMAT);

					boolean isStockNoOfMaxIdEmpty = StringUtils.isEmpty(stockNoOfMaxId);

					if (isStockNoOfMaxIdEmpty || !currentDate.equals(BillsUtil.getDateOfBillNo(Optional.of(stockNoOfMaxId)))) {
						return BillsUtil.makeBillNo(prefix, 1);
					} else {
						// 如果有记录并且最后一条记录的日期是当天，则流水号为最大值 + 1
						// 首先获取最后一条库存的单号
						int num = BillsUtil.getSerialNumberOfBillNo(Optional.of(stockNoOfMaxId)) + 1;
						// 通过最后一条的库存单号生成新的库存单号
						return BillsUtil.makeBillNo(prefix, num);
					}
				} finally {
					lock.unlock();
				}
			}
		} catch (InterruptedException e) {
			log.debug("exception:", e);
			throw new ServiceException(HttpServletResponse.SC_BAD_REQUEST, "lock exception", "The server is blocked,please try again later", "trenska.utils.stock.bill.001");
		}
		return null;
	}


	/**
	 * 获取商品的批次-库存
	 * 没有结果返回空集合
	 *
	 * @param goodsId
	 * @return
	 */
	public static List<StockMadedate> getGoodsStockMadeDate(int goodsId) {

		List<GoodsStock> goodsStocks = new GoodsStock().selectList(
				new LambdaQueryWrapper<GoodsStock>()
						.ge(GoodsStock::getNum, 0)
						.eq(GoodsStock::getGoodsId, goodsId)
		);
		if (CollectionUtils.isEmpty(goodsStocks)) {
			return new ArrayList<>();
		} else {
			List<StockMadedate> stockMadedates = new ArrayList<>();
			goodsStocks.forEach(goodsStock -> stockMadedates.add(new StockMadedate(goodsStock.getMadeDate(), goodsStock.getNum())));
			return stockMadedates;
		}
	}

	/**
	 * 商品入库操作 :
	 * 处理商品总库存
	 * 处理商品批次库存
	 * 保存入库记录
	 *
	 * @param stock
	 * @param stockMapper
	 * @param goodsMapper
	 * @return
	 */

	public static CommonResponse stockin(Stock stock, StockMapper stockMapper, GoodsMapper goodsMapper) {
		String stockNo = StockUtil.getStockNo(stock.getPrefix(), DragonConstant.RKD_CHINESE, stockMapper);
		//入库时间
		String stockTime = TimeUtil.getCurrentTime(DragonConstant.TIME_FORMAT);
		List<StockDetail> stockinDetailList = stock.getStockins();

		for (StockDetail stockinDetail : stockinDetailList) {
			/**
			 * 以最小单位为计量单位算出需要入库的量
			 * 入库数量 = 基数 * 系数
			 */
			int stockinNum = stockinDetail.getNum() * stockinDetail.getMulti();
			Integer history = stockinNum;
			// 保存变更量->实际入库量(主单位)
			stockinDetail.setHistory(history);
			/**
			 * 保存入库记录
			 */
			stockinDetail.setStockNo(stockNo);
			stockinDetail.setBusiNo(stock.getBusiNo());
			stockinDetail.setOperType(stock.getOperType());
			stockinDetail.setStockTime(stockTime);
			stockinDetail.setEmpId(stock.getEmpId());
			stockinDetail.setStockType(DragonConstant.RKD_CHINESE);

			// 锁特定商品
			synchronized (new Integer(stockinDetail.getGoodsId())) {
				// 获取商品
				Goods dbGoods = goodsMapper.selectGoodsByGoodsId(stockinDetail.getGoodsId());
				// 同步商品库存
				stockinDetail.setStock(dbGoods.getStock() + stockinNum);
				//保存入库记录
				stockinDetail.insert();
				//保存库存明细
				StockDetailsUtil.dbLogStockDetail(stockinDetail);
				//更新商品总库存
				new Goods(dbGoods.getGoodsId(), dbGoods.getStock() + stockinNum).updateById();
			}
		}
		stock.setStockNo(stockNo);
		stock.setStockTime(stockTime);
		synchronized (stockNo) {
			stockMapper.insert(stock);
		}
		return CommonResponse.getInstance().succ(true).msg(DragonConstant.STOCKIN_SUCC);
	}

	/**
	 * @param stock
	 * @param stockDetailList
	 * @param goodsMapper
	 * @return
	 */
	public static CommonResponse changeStockin(Stock stock, List<StockDetail> stockDetailList, GoodsMapper goodsMapper) {

		//入库时间
		String stockTime = TimeUtil.getCurrentTime(DragonConstant.TIME_FORMAT);

		for (StockDetail stockDetail : stockDetailList) {
			/**
			 * 以最小单位为计量单位算出需要入库的量
			 * 入库数量 = 基数 * 系数
			 */
			int stockinNum = stockDetail.getNum() * stockDetail.getMulti();
			Integer history = stockinNum;
			/* 保存变更量->实际入库量(主单位) */
			stockDetail.setHistory(history);
			/* 保存入库记录 */
			stockDetail.setStockTime(stockTime);
			stockDetail.setEmpId(stock.getEmpId());
			stockDetail.setBusiNo(stock.getBusiNo());
			stockDetail.setStockNo(stock.getStockNo());
			stockDetail.setOperType(stock.getOperType());
			stockDetail.setStockType(DragonConstant.RKD_CHINESE);

			/* 特定商品锁 */
			synchronized (new Integer(stockDetail.getGoodsId())) {
				/* 获取商品 */
				Goods dbGoods = goodsMapper.selectGoodsByGoodsId(stockDetail.getGoodsId());
				/* 同步商品库存 */
				stockDetail.setStock(dbGoods.getStock() + stockinNum);
				/* 保存入库记录 */
				stockDetail.insert();
				/* 保存库存明细 */
				StockDetailsUtil.dbLogStockDetail(stockDetail);
				/* 更新商品总库存 */
				new Goods(dbGoods.getGoodsId(), dbGoods.getStock() + stockinNum).updateById();
			}
		}
		stock.setStockTime(stockTime);
		stock.updateById();
		return CommonResponse.getInstance().succ(true).msg(DragonConstant.STOCKIN_SUCC);
	}

	/**
	 * 保存出库记录
	 * 减少商品的总库存
	 *
	 * @param stock
	 * @param goodsMapper
	 * @return
	 */
	public static CommonResponse stockout(Stock stock, GoodsMapper goodsMapper) {
		List<StockDetail> stockoutDetails = stock.getStockouts();
		String stockNo = stock.getStockNo();
		String stockTime = stock.getStockTime();
		// 商品纬度
		for (StockDetail stockoutDetail : stockoutDetails) {
			// 商品出库量
			int totalStockoutNumForOneGood = 0;
			List<StockMadedate> stockMadeDates = stockoutDetail.getStockoutMadedates();
			if (CollectionUtils.isEmpty(stockMadeDates)) {
				return CommonResponse.getInstance().succ(false).msg("请选择批次！");
			}
			Integer goodsId = stockoutDetail.getGoodsId();
			synchronized (goodsId) {
				Goods dbGoods = goodsMapper.selectGoodsByGoodsId(goodsId);
				// 批次纬度
				for (StockMadedate stockMadedate : stockMadeDates) {
					Integer multi = stockoutDetail.getMulti();
					if (Objects.isNull(multi)) {
						multi = 1;
					}
					Integer num = stockMadedate.getNum();
					// 商品出库量+(以最小单位进行处理的，所以要要处理multi)
					totalStockoutNumForOneGood += num * multi;
					//处理出库记录
					stockoutDetail.setStockNo(stockNo);
					stockoutDetail.setStockTime(stockTime);
					stockoutDetail.setNum(stockMadedate.getNum()); // 出库数量
					stockoutDetail.setHistory(num * multi); // 设置本次出库历史数量 = history * multi
					stockoutDetail.setStock(dbGoods.getStock() - totalStockoutNumForOneGood); // 同步批次出库后的商品库存
					stockoutDetail.setMadeDate(stockMadedate.getMadeDate()); // 设置出库批次
					stockoutDetail.setStockPrice(stockMadedate.getStockPrice());
					stockoutDetail.insert(); // 保存出库记录
					//保存库存详情
					StockDetailsUtil.dbLogStockDetail(stockoutDetail);
				}
				//更新商品库存信息
				new Goods(goodsId, dbGoods.getStock() - totalStockoutNumForOneGood).updateById();
			}
		}
		stock.insert();
		return CommonResponse.getInstance().succ(true).msg(DragonConstant.STOCKOUT_SUCC);
	}


	/**
	 * 批量作废出库单
	 *
	 * @param stockouts
	 * @return
	 */
	public static CommonResponse invalidStockout(List<Stock> stockouts) {

		// 获取所有需要作废的出库单的关联订货单号->以便一次性取出所有订货单和订货单商品详情->以免多次从数据获取订单信息
		List<String> indentNos = stockouts.stream().map(Stock::getBusiNo).distinct().collect(Collectors.toList());

		List<Indent> dbIndents = null;
		// 查看是否有已经完成或已经出库的订单，不允许作废"已出库"或"已完成"订单的出库单
		if (CollectionUtils.isNotEmpty(indentNos)) {
			// 一次性获取-->减少sql
			dbIndents = new Indent().selectList(
					new LambdaQueryWrapper<Indent>()
							.in(Indent::getIndentNo, indentNos)
			);
			if (CollectionUtils.isNotEmpty(dbIndents)) {
				List<Indent> finishedIndents = dbIndents.stream().filter(indent ->
						IndentStat.FINISHED.getName().equals(indent.getStat())
								|| IndentStat.STOCKOUTED.getName().equals(indent.getStat())
				).collect(Collectors.toList());
				if (CollectionUtils.isNotEmpty(finishedIndents)) {
					return CommonResponse.getInstance().succ(false).msg("请先作废关联订单");
				}
			}
		}

		// 一次性获取-->减少sql
		List<IndentDetail> dbIndentDetails = new IndentDetail().selectList(
				new LambdaQueryWrapper<IndentDetail>()
						.in(IndentDetail::getIndentNo, indentNos)
		);

		boolean successful = true;
		String msg = "作废出库单成功";

		for (Stock stockout : stockouts) {

			/************************************************处理关联订货单*********************************************/
			String indentNo = stockout.getBusiNo();

			if (StringUtils.isNotEmpty(indentNo)) {
				// 通过indentNo将指定订货单过滤出来
				List<IndentDetail> filteredIndentDetails = dbIndentDetails.stream().filter(indentDetail -> indentDetail.getIndentNo().equals(indentNo)).collect(Collectors.toList());
				List<StockDetail> stockoutDetails = stockout.getStockDetails();
				stockoutDetails.forEach(stockDetail -> {
					// 匹配goodsId，将修改了的订单筛选出来->修改已出库数量
					IndentDetail matchedIndentDetail = filteredIndentDetails.stream().filter(indentDetail -> indentDetail.getGoodsId() == stockDetail.getGoodsId()).findFirst().get();
					int stockoutNum = matchedIndentDetail.getStockout() - stockDetail.getHistory();
					Long detailId = matchedIndentDetail.getDetailId();
					IndentDetail updatingIndentDetail = new IndentDetail();
					updatingIndentDetail.setDetailId(detailId);
					updatingIndentDetail.setStockout(stockoutNum);
					updatingIndentDetail.updateById();
				});
			}
			/**********************************************处理关联订货单结束******************************************/

			Long stockId = stockout.getStockId();
			new Stock(stockId, false).updateById();//作废出库单
			List<StockDetail> stockoutDetails = stockout.getStockouts();
			String stockNo = stockout.getStockNo();
			// 如果stockouts中不包含stockDetails就从数据库中获取
			if (Objects.isNull(stockoutDetails) || stockoutDetails.isEmpty()) {
				stockoutDetails = new StockDetail().selectList(
						new LambdaQueryWrapper<StockDetail>()
								.eq(StockDetail::getStockNo, stockNo)
				);
			}
			// 1.增加商品库存;2.作废出库详情3.保存出库单作废记录
			for (StockDetail stockoutDetail : stockoutDetails) {
				Integer goodsId = stockoutDetail.getGoodsId();
				String stockType = stockoutDetail.getStockType().concat(DragonConstant.ZF);
				Goods dbGoods = new Goods().selectById(goodsId); // 获取商品信息，主要为了获取商品总库存
				Integer history = stockoutDetail.getHistory();
				int newStock = dbGoods.getStock() + history;
				// 作废出库记录
				Long detailId = stockoutDetail.getDetailId();
				StockDetail stockDetail = new StockDetail();
				stockDetail.setDetailId(detailId);
				stockDetail.setStat(false);
				stockDetail.updateById();

				/************************************* 保存库存明细 ***********************************/
				int empIdInToken = SysUtil.getEmpIdInToken();
				stockoutDetail.setStock(newStock);
				stockoutDetail.setEmpId(empIdInToken);
				stockoutDetail.setStockType(stockType);
				StockDetailsUtil.dbLogStockDetail(stockoutDetail); // 保存库存明细

				/************************************* 还回商品库存 ***********************************/
				Goods updatingGoods = new Goods();
				updatingGoods.setGoodsId(goodsId);
				updatingGoods.setStock(newStock);
				updatingGoods.updateById();
			}
		}

		// 作废出库单
		List<Long> stockIds = stockouts.stream().map(Stock::getStockId).collect(Collectors.toList());
		new Stock().update(
				new LambdaUpdateWrapper<Stock>()
						.in(Stock::getStockId, stockIds)
						.set(Stock::getStat, false)
		);

		return CommonResponse.getInstance().succ(successful).msg(msg);
	}

	/**
	 * 批量删除出库单
	 *
	 * @param stockouts
	 */
	public static CommonResponse deleteStockout(List<Stock> stockouts, int empId) {

		for (Stock stockout : stockouts) {
			List<StockDetail> stockDetailList = stockout.getStockouts();
			for (StockDetail stockDetail : stockDetailList) {
				GoodsStockUtil.deleteStockDetailAndReturnGoodsStock(stockDetail, empId);
			}
			stockout.deleteById();
		}
		return CommonResponse.getInstance().succ(true).msg("删除成功");
	}


	/**
	 * @param stockins 入库单
	 * @param stockType 入库单(作废)/退货单(作废)
	 * @return
	 */
	public static CommonResponse cancelStockin(List<Stock> stockins, String stockType) {
		int empIdInToken = SysUtil.getEmpIdInToken();
		if (Objects.isNull(empIdInToken)) {
			return CommonResponse.getInstance().succ(false).msg(DragonConstant.ACCESS_TIMEOUT_MSG).code(DragonConstant.ACCESS_TIMEOUT);
		}
		String msg = "作废入库单成功";
		boolean successful = true;

		// 1.作废入库单；2.保存入库单作废详情(作废入库单后，在库存明细中增加一条记录(变更类型:入库单(作废)，操作类型：与原类型一致))
		for (Stock stockin : stockins) {
			String stockNo = stockin.getStockNo();
			// 作废入库单
			stockin.setStat(false);
			stockin.setStockType(stockType);
			stockin.updateById();
			List<StockDetail> stockinDetails = stockin.getStockins();
			if (Objects.isNull(stockinDetails) || stockinDetails.isEmpty()) {
				stockinDetails = new StockDetail().selectList(
						new LambdaQueryWrapper<StockDetail>()
								.eq(StockDetail::getStockNo, stockNo)
				);
			}
			for (StockDetail stockinDetail : stockinDetails) {
				Integer goodsId = stockinDetail.getGoodsId();
				Goods dbGoods = new Goods().selectById(goodsId); // 获取商品信息，主要为了获取商品总库存
				Integer history = stockinDetail.getHistory();
				int stock = dbGoods.getStock() - history;
				new StockDetail(stockinDetail.getDetailId(), false, stockType).updateById(); // 作废入库记录

				/************************************* 保存库存明细 ***********************************/
				stockinDetail.setStock(stock);
				stockinDetail.setEmpId(empIdInToken);
				stockinDetail.setStockType(stockType);
				StockDetailsUtil.dbLogStockDetail(stockinDetail);

				/************************************* 减少商品库存 ***********************************/
				Goods updatingGoods = new Goods(goodsId, dbGoods.getBrandName(), stock);
				updatingGoods.updateById();
			}

		}

		return CommonResponse.getInstance().succ(successful).msg(msg);
	}

	public static int returnStock(StockDetail stockDetail, int empId) {
		Integer goodsId = stockDetail.getGoodsId();
		int history = stockDetail.getHistory();
		// 修改商品总库存,返回商品的最新总库存
		int newStock = GoodsUtil.changeGoodsStock(goodsId, history);
		stockDetail.setStock(newStock);
		stockDetail.setEmpId(empId);
		// 保存库存明细
		StockDetailsUtil.dbLogStockDetail(stockDetail);
		return newStock;
	}

	public static void invalidIndentStockout(List<Stock> stockouts) {
		for (Stock stockout : stockouts) {
			List<StockDetail> stockoutDetailList = stockout.getStockDetails();
			for (StockDetail stockoutDetail : stockoutDetailList) {
				Integer goodsId = stockoutDetail.getGoodsId();
				String madeDate = stockoutDetail.getMadeDate();
				String stockPrice = stockoutDetail.getStockPrice();
				Goods dbGoods = new Goods().selectById(goodsId); // 获取商品信息，主要为了获取商品总库存
				Integer history = stockoutDetail.getHistory();
				int newStock = dbGoods.getStock() + history;

				// 作废出库记录
				Long detailId = stockoutDetail.getDetailId();
				StockDetail stockDetail = new StockDetail();
				stockDetail.setDetailId(detailId);
				stockDetail.setStat(false);
				stockDetail.updateById();

				/************************************* 保存库存明细 ***********************************/
				stockoutDetail.setStock(newStock);
				stockoutDetail.setEmpId(SysUtil.getEmpIdInToken());
				stockoutDetail.setStockType(stockoutDetail.getStockType().concat(DragonConstant.ZF));
				StockDetailsUtil.dbLogStockDetail(stockoutDetail); // 保存库存明细

				/************************************* 还回商品库存 ***********************************/
				Goods updatingGoods = new Goods();
				updatingGoods.setGoodsId(goodsId);
				updatingGoods.setStock(newStock);
				updatingGoods.updateById();
			}
		}
		// 作废出库单
		List<Long> stockIds = stockouts.stream().map(Stock::getStockId).collect(Collectors.toList());
		new Stock().update(
				new LambdaUpdateWrapper<Stock>()
						.in(Stock::getStockId, stockIds)
						.set(Stock::getStat, false)
		);
	}
}