package com.trenska.longwang.service.impl.stock;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.constant.Constant;
import com.trenska.longwang.dao.stock.StockMapper;
import com.trenska.longwang.entity.goods.Goods;
import com.trenska.longwang.entity.goods.Unit;
import com.trenska.longwang.entity.stock.Stock;
import com.trenska.longwang.entity.stock.StockDetail;
import com.trenska.longwang.entity.stock.StockDetails;
import com.trenska.longwang.entity.sys.SysEmp;
import com.trenska.longwang.model.report.GoodsStockSummarizingModel;
import com.trenska.longwang.model.report.GoodsStockSummationModel;
import com.trenska.longwang.model.report.GoodsStockinStatisticsModel;
import com.trenska.longwang.model.report.GoodsStockinSummationModel;
import com.trenska.longwang.service.goods.IGoodsService;
import com.trenska.longwang.service.goods.IUnitService;
import com.trenska.longwang.service.stock.IStockDetailService;
import com.trenska.longwang.service.stock.IStockService;
import com.trenska.longwang.service.sys.ISysEmpService;
import com.trenska.longwang.util.RMBUtil;
import com.trenska.longwang.util.SysConfigUtil;
import com.trenska.longwang.util.SysUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存明细 服务实现类
 *
 * @author Owen
 * @since 2019-04-17
 */
@Service
@SuppressWarnings("all")
public class StockServiceImpl extends ServiceImpl<StockMapper, Stock> implements IStockService {

	private final static Logger logger = LoggerFactory.getLogger(StockServiceImpl.class);

	@Autowired
	private IStockDetailService stockoutService;

	@Autowired
	private ISysEmpService empService;

	@Autowired
	private IGoodsService goodsService;

	@Autowired
	private IUnitService unitService;

	@Override
	public Page<StockDetails> getStockDetaislPage(Map<String, Object> params, Page page) {
		page.setRecords(super.baseMapper.selectStockDetailPageSelective(params, page));
		page.setTotal(super.baseMapper.selectStockDetailCountSelective(params));
		return page;
	}

	/**
	 * 如果不选择库存时间，实际
	 *
	 * @param page上就是商品的普通分页 + 可用库存处理
	 * @return
	 */
	@Override
	public Page<Goods> getStockStatusPageSelective(Map<String, Object> params, Page page) {
		List<Goods> goodsStockPage = super.baseMapper.selectStockStatusPage(params, page);
		// 处理可用库存
		goodsStockPage.forEach(goodsStock -> {
			goodsStock.setAvbStock(goodsStock.getStock() - goodsStock.getStockout());
		});
		page.setRecords(goodsStockPage);
		page.setTotal(super.baseMapper.selectStockStatusPageCount(params));
		return page;
	}

	@Override
	public Page<Stock> getStockPageSelective(Map<String, Object> params, Page page) {
		page.setRecords(super.baseMapper.selectStockSelective(params, page));
		page.setTotal(super.baseMapper.selectStockCountSelective(params));
		return page;
	}

	@Override
	public Boolean cancelStockin(Map<String, Object> params) {
		// 作废退货入库单
		return null;
	}

	@Override
	public Map<String, Object> prinAndPdf(Long stockId) {

		Stock stock = this.getById(stockId);

		Map<String, Object> params = new HashMap<>();
		params.put("stockNo", stock.getStockNo());
		params.put("type", stock.getOperType());
		params.put("outTime", stock.getStockTime());
		params.put("ddremark", stock.getStockRemarks());//订单备注

		SysEmp emp = empService.getOne(new QueryWrapper<SysEmp>().eq("emp_id", stock.getEmpId()));
		if (null != emp) {
			params.put("person", emp.getEmpName());
			params.put("empName", emp.getEmpName());
		}


		List<Map<String, Object>> recordsList = stockoutService.listMaps(new QueryWrapper<StockDetail>().eq("stock_no", stock.getStockNo()));

		Double totalPrice = 0.0;
		int totalNum = 0;
		for (Map<String, Object> detail : recordsList) {
			Goods goods = goodsService.getById(Long.valueOf((Integer) detail.get("goods_id")));

			Unit unit = unitService.getById(Long.valueOf((Integer) detail.get("unit_id")));
			detail.put("gcode", goods.getGoodsNo());// 编号
			detail.put("gname", goods.getGoodsName());//名称
			detail.put("guige", goodsService.getGoodsPropsByGoodsId((Integer) detail.get("goods_id")));//规格
			detail.put("unit", unit.getUnitName());//单位

			int num = (int) detail.get("history");
			Double price = Double.valueOf((String) detail.get("stock_price"));
			detail.put("amount", num * price); //金额
			totalNum += num;

			totalPrice += num * price; //总价格
		}

		params.put("flow_list", recordsList); //详细数据

		params.put("totalPrice", totalPrice); //总金额
		params.put("totalNum", totalNum);//总数量

		params.put("lowAmount", totalPrice);
		params.put("capAmount", RMBUtil.toUpper(totalPrice.toString()));//金额大写
		return params;
	}

	@Override
	public Page<GoodsStockSummarizingModel> getGoodsStockSummarizing(Map<String, Object> params, Page page) {

		long start = System.currentTimeMillis();
		List<GoodsStockSummarizingModel> records = super.baseMapper.selectGoodsStockSummarizing(params, page);
		if (CollectionUtils.isEmpty(records)) {
			return null;
		}
		long end = System.currentTimeMillis();
		logger.info("get page records spend {} seconds in StockService.", (end - start) / 1000);
		for (GoodsStockSummarizingModel record : records) {
			// 判断期初库存是否为空(数据库默认值为0)，如果为空，表示时间段之前没有库存操作，需要获取时间段内最小时间的库存
			String initStock = record.getInitStock();
			if (StringUtils.isEmpty(initStock) || Constant.ZERO_STR.equals(initStock)) {
				int goodsId = record.getGoodsId();
				initStock = super.baseMapper.selectQckcStock(goodsId);
				if (StringUtils.isEmpty(initStock)) {
					initStock = Constant.ZERO_STR;
				}
				record.setInitStock(initStock);
			}
			BigDecimal makeIn = new BigDecimal(record.getMakeIn()).setScale(Constant.ZERO);
			BigDecimal purcharseIn = new BigDecimal(record.getPurchaseIn()).setScale(Constant.ZERO);
			BigDecimal otherIn = new BigDecimal(record.getOtherIn()).setScale(Constant.ZERO);
			BigDecimal otherOut = new BigDecimal(record.getOtherOut()).setScale(Constant.ZERO);
			BigDecimal returnin = new BigDecimal(record.getReturnsIn()).setScale(Constant.ZERO);
			BigDecimal returnOut = new BigDecimal(record.getReturnsOut()).setScale(Constant.ZERO);
			BigDecimal salesOut = new BigDecimal(record.getSalesOut()).setScale(Constant.ZERO);
			BigDecimal overFlow = new BigDecimal(record.getOverflow()).setScale(Constant.ZERO);
			BigDecimal breakage = new BigDecimal(record.getBreakage()).setScale(Constant.ZERO);

			// 期末库存由期初和所有库存类型的库存计算得到
			BigDecimal overStock = new BigDecimal(record.getInitStock())
					.add(makeIn).add(otherIn).add(returnin).add(overFlow)
					.add(salesOut).add(otherOut).add(breakage).setScale(Constant.ZERO);
			if (salesOut.compareTo(BigDecimal.ZERO) != 0) {
				salesOut = salesOut.negate();
			}
			if (otherOut.compareTo(BigDecimal.ZERO) != 0) {
				otherOut = otherOut.negate();
			}
			if (breakage.compareTo(BigDecimal.ZERO) != 0) {
				breakage = breakage.negate();
			}
			record.setMakeIn(makeIn.toString());
			record.setPurchaseIn(purcharseIn.toString());
			record.setOtherIn(otherIn.toString());
			record.setOtherOut(otherOut.toString());
			record.setReturnsIn(returnin.toString());
			record.setReturnsOut(returnOut.toString());
			record.setSalesOut(salesOut.toString());
			record.setOverflow(overFlow.toString());
			record.setBreakage(breakage.toString());
			record.setOverStock(overStock.toString());
		}
		logger.info("calculate spend {} seconds in StockService.", (System.currentTimeMillis() - end) / 1000);
		end = System.currentTimeMillis();
		int total = super.baseMapper.selectGoodsStockSummarizingCount(params);

		logger.info("spend {} seconds getting count in StockService.", (System.currentTimeMillis() - end) / 1000);
		page.setRecords(records);
		page.setTotal(total);
		return page;
	}

	@Override
	public GoodsStockSummationModel getGoodsStockSummation(Map<String, Object> params) {
		GoodsStockSummationModel goodsStockSummation =
				super.baseMapper.selectGoodsStockSummation(params);
		if (goodsStockSummation != null) {
			BigDecimal initStockSum =
					new BigDecimal(super.baseMapper.selectInitStockSum(params)).setScale(Constant.ZERO);
			BigDecimal makeInSum = new BigDecimal(goodsStockSummation.getMakeInSum()).setScale(Constant.ZERO);
			BigDecimal purchaseInSum = new BigDecimal(goodsStockSummation.getPurchaseInSum()).setScale(Constant.ZERO);
			BigDecimal returnsInSum = new BigDecimal(goodsStockSummation.getReturnsInSum()).setScale(Constant.ZERO);
			BigDecimal returnsOutSum = new BigDecimal(goodsStockSummation.getReturnsOutSum()).setScale(Constant.ZERO);
			BigDecimal salesOutSum = new BigDecimal(goodsStockSummation.getSalesOutSum()).setScale(Constant.ZERO);
			BigDecimal otherInSum = new BigDecimal(goodsStockSummation.getOtherInSum()).setScale(Constant.ZERO);
			BigDecimal otherOutSum = new BigDecimal(goodsStockSummation.getOtherOutSum()).setScale(Constant.ZERO);
			BigDecimal overflowSum = new BigDecimal(goodsStockSummation.getOverflowSum()).setScale(Constant.ZERO);
			BigDecimal breakageSum = new BigDecimal(goodsStockSummation.getBreakageSum()).setScale(Constant.ZERO);
			BigDecimal overStockSum = initStockSum.add(makeInSum).add(purchaseInSum).add(returnsInSum).add(returnsOutSum)
					.add(salesOutSum).add(otherInSum).add(otherOutSum).add(overflowSum).add(breakageSum).setScale(Constant.ZERO);

			if (salesOutSum.compareTo(BigDecimal.ZERO) != 0) {
				salesOutSum = salesOutSum.negate();
			}
			if (otherOutSum.compareTo(BigDecimal.ZERO) != 0) {
				otherOutSum = otherOutSum.negate();
			}
			if (breakageSum.compareTo(BigDecimal.ZERO) != 0) {
				breakageSum = breakageSum.negate();
			}
			goodsStockSummation.setInitStockSum(initStockSum.toString());
			goodsStockSummation.setMakeInSum(makeInSum.toString());
			goodsStockSummation.setPurchaseInSum(purchaseInSum.toString());
			goodsStockSummation.setReturnsInSum(returnsInSum.toString());
			goodsStockSummation.setReturnsOutSum(returnsOutSum.toString());
			goodsStockSummation.setSalesOutSum(salesOutSum.toString());
			goodsStockSummation.setOtherInSum(otherInSum.toString());
			goodsStockSummation.setOtherOutSum(otherOutSum.toString());
			goodsStockSummation.setOverflowSum(overflowSum.toString());
			goodsStockSummation.setBreakageSum(breakageSum.toString());
			goodsStockSummation.setOverStockSum(overStockSum.toString());
		}
		return goodsStockSummation;
	}

	@Override
	public Page<GoodsStockinStatisticsModel> getGoodsStockinStatistics(Map<String, Object> params, Page page) {
		List<GoodsStockinStatisticsModel> records = super.baseMapper.selectGoodsStockinStatistic(params, page);
		if (CollectionUtils.isNotEmpty(records)) {
			int sysConfigRetain = SysUtil.getSysConfigRetain();
			for (GoodsStockinStatisticsModel record : records) {
				String stockinNum = new BigDecimal(record.getStockinNum()).setScale(Constant.ZERO).toString();
				String avgPrice = new BigDecimal(record.getAvgPrice()).setScale(sysConfigRetain, RoundingMode.HALF_UP).toString();
				String stockinAmnt = new BigDecimal(record.getStockinAmnt()).setScale(sysConfigRetain,
						RoundingMode.HALF_UP).toString();
				record.setAvgPrice(avgPrice);
				record.setStockinNum(stockinNum);
				record.setStockinAmnt(stockinAmnt);
			}
		}
		int total = super.baseMapper.selectGoodsStockinStatisticsCount(params).size();
		page.setRecords(records);
		page.setTotal(total);
		return page;
	}

	@Override
	public GoodsStockinSummationModel getGoodsStockinSummation(Map<String, Object> params) {
		GoodsStockinSummationModel goodsStockinSummationModel = super.baseMapper.selectGoodsStockinSummation(params);
		if (goodsStockinSummationModel != null) {
			int sysConfigRetain = SysUtil.getSysConfigRetain();
			String stockinNumSum = new BigDecimal(goodsStockinSummationModel.getStockinNumSum()).setScale(Constant.ZERO).toString();
			String sotkcinAmntSum =
					new BigDecimal(goodsStockinSummationModel.getStockinAmntSum()).setScale(sysConfigRetain
							, RoundingMode.HALF_UP).toString();
			goodsStockinSummationModel.setStockinNumSum(stockinNumSum);
			goodsStockinSummationModel.setStockinAmntSum(sotkcinAmntSum);
		}
		return goodsStockinSummationModel;
	}

	public int getGoodsBeginningStock(Map<String, Object> params) {
		return -1;
	}
}