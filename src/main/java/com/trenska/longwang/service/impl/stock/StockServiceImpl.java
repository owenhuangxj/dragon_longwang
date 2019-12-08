package com.trenska.longwang.service.impl.stock;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.context.ApplicationContextHolder;
import com.trenska.longwang.dao.stock.StockMapper;
import com.trenska.longwang.entity.goods.Goods;
import com.trenska.longwang.entity.goods.Unit;
import com.trenska.longwang.entity.stock.Stock;
import com.trenska.longwang.entity.stock.StockDetail;
import com.trenska.longwang.entity.stock.StockDetails;
import com.trenska.longwang.entity.sys.SysConfig;
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
import com.trenska.longwang.util.SysUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 库存明细 服务实现类
 * @author Owen
 * @since 2019-04-17
 */
@Service
@SuppressWarnings("all")
public class StockServiceImpl extends ServiceImpl<StockMapper, Stock> implements IStockService {

	private final static Logger logger = LoggerFactory.getLogger(StockServiceImpl.class);
//	@Autowired
//	private StockDetailMapper detailMapper;

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
		page.setRecords(super.baseMapper.selectStockDetailPageSelective(params,page));
		page.setTotal(super.baseMapper.selectStockDetailCountSelective(params));
		return page;
	}

	/**
	 * 如果不选择库存时间，实际上就是商品的普通分页 + 可用库存处理
	 * @param page
	 * @return
	 */
	@Override
	public Page<Goods> getStockStatusPageSelective(Map<String,Object> params, Page page) {
		List<Goods> goodsStockPage = super.baseMapper.selectStockStatusPage(params, page);
		// 处理可用库存
		goodsStockPage.forEach(goodsStock->{
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

		Stock stock =  this.getById(stockId);

		Map<String, Object> params = new HashMap<>();
		params.put("stockNo",stock.getStockNo() );
		params.put("type",stock.getOperType());
		params.put("outTime",stock.getStockTime());
		params.put("ddremark",stock.getStockRemarks());//订单备注

		SysEmp emp = empService.getOne(new QueryWrapper<SysEmp>().eq("emp_id", stock.getEmpId()));
		if (null != emp)
		{
			params.put("person",emp.getEmpName());
			params.put("empName",emp.getEmpName());
		}


		List<Map<String, Object>> recordsList = stockoutService.listMaps(new QueryWrapper<StockDetail>().eq("stock_no", stock.getStockNo()));

		Double totalPrice = 0.0;
		int totalNum = 0;
		for (Map<String, Object> detail : recordsList)
		{
			Goods goods = goodsService.getById(Long.valueOf((Integer)detail.get("goods_id")));

			Unit unit =  unitService.getById(Long.valueOf((Integer)detail.get("unit_id")));
			detail.put("gcode",goods.getGoodsNo());// 编号
			detail.put("gname",goods.getGoodsName());//名称
			detail.put("guige",goodsService.getGoodsPropsByGoodsId((Integer)detail.get("goods_id")));//规格
			detail.put("unit",unit.getUnitName());//单位

			int num = (int)detail.get("history");
			Double  price = Double.valueOf((String)detail.get("stock_price"));
			detail.put("amount",num * price); //金额
			totalNum += num;

			totalPrice +=  num * price; //总价格

		}

		params.put("flow_list",recordsList); //详细数据

		params.put("totalPrice",totalPrice); //总金额
		params.put("totalNum",totalNum);//总数量

		params.put("lowAmount",totalPrice);
		params.put("capAmount", RMBUtil.toUpper(totalPrice.toString()));//金额大写


		return params;
	}

	@Override
	public Page<GoodsStockSummarizingModel>  getGoodsStockSummarizing(Map<String, Object> params, Page page, HttpServletRequest request) {

		List<GoodsStockSummarizingModel> records = super.baseMapper.selectGoodsStockSummarizing(params,page);

		for(GoodsStockSummarizingModel record : records){

			// 判断期初库存是否为空(数据库默认值为0)，如果为空，表示时间段之前没有库存操作，需要获取时间段内最小时间的库存
			String initStock = record.getInitStock();
			if(StringUtils.isEmpty(initStock)){
				Integer goodsId = record.getGoodsId();
				params.put("goodsId",goodsId);
//				initStock = super.baseMapper.selectGoodsBeginningStockBetween(params);
				initStock = super.baseMapper.selectGoodsBeginningStockOfInitialization(params);
				if (StringUtils.isEmpty(initStock)){
					initStock = "0";
				}
				record.setInitStock(initStock);
			}

			// 判断期末库存，如果为空(数据库默认值为0) 表示时间段内没有库存操作，需要将期末库存设置为和期初库存一样
//			String overStock = record.getOverStock();
//			if(StringUtils.isEmpty(overStock)){
//				record.setOverStock(initStock);
//			}
			// 期末库存由期初和所有库存类型的库存计算得到
			String overStock =
					new BigDecimal(record.getInitStock())
					.add(new BigDecimal(record.getMakeIn()))
					.add(new BigDecimal(record.getOtherIn()))
					.add(new BigDecimal(record.getReturnsIn()))
					.add(new BigDecimal(record.getOverflow()))
					.subtract(new BigDecimal(record.getSalesOut()))
					.subtract(new BigDecimal(record.getOtherOut()))
					.subtract(new BigDecimal(record.getBreakage()))
					.toString();
			record.setOverStock(overStock);
		}

		int total = super.baseMapper.selectGoodsStockSummarizingCount(params).size();

		records = records.stream().filter(
				record->
					new BigDecimal(record.getInitStock()).compareTo(BigDecimal.ZERO) > 0
					|| new BigDecimal(record.getMakeIn()).compareTo(BigDecimal.ZERO) > 0
					|| new BigDecimal(record.getOtherIn()).compareTo(BigDecimal.ZERO) > 0
					|| new BigDecimal(record.getOverflow()).compareTo(BigDecimal.ZERO) > 0
					|| new BigDecimal(record.getSalesOut()).compareTo(BigDecimal.ZERO) > 0
					|| new BigDecimal(record.getOtherOut()).compareTo(BigDecimal.ZERO) > 0
					|| new BigDecimal(record.getBreakage()).compareTo(BigDecimal.ZERO) > 0
					|| new BigDecimal(record.getReturnsIn()).compareTo(BigDecimal.ZERO) > 0
					|| new BigDecimal(record.getOverStock()).compareTo(BigDecimal.ZERO) > 0

		).collect(Collectors.toList());

		page.setRecords(records);
		page.setTotal(total);
		return page;
	}

	@Override
	public GoodsStockSummationModel getGoodsStockSummartion(Map<String, Object> params) {
//		List<Map<String, String>> maps = super.baseMapper.selectGoodsStockSummationOld(params);
//		BigDecimal overStockSum = new BigDecimal(0);
//		BigDecimal initStockSum = new BigDecimal(0);
//		for(Map<String,String> map : maps){
//			String initStock = String.valueOf(map.get("initStock"));
//			String overStock = String.valueOf(map.get("overStock"));
//			initStockSum = initStockSum.add(new BigDecimal(initStock));
//			overStockSum = overStockSum.add(new BigDecimal(overStock));
//		}
//		return new GoodsStockSummationModel(initStockSum.toString(),overStockSum.toString());
		List<GoodsStockSummarizingModel> records = super.baseMapper.selectGoodsStockSummarizing(params);
		GoodsStockSummationModel goodsStockSummation = new GoodsStockSummationModel();

		BigDecimal initStockSum = new BigDecimal(0);
		BigDecimal makeInSum = new BigDecimal(0);
		BigDecimal purchaseInSum = new BigDecimal(0);
		BigDecimal returnsOutSum = new BigDecimal(0);
		BigDecimal returnsInSum = new BigDecimal(0);
		BigDecimal salesOutSum = new BigDecimal(0);
		BigDecimal otherInSum = new BigDecimal(0);
		BigDecimal otherOutSum = new BigDecimal(0);
		BigDecimal overflowSum = new BigDecimal(0);
		BigDecimal breakageSum = new BigDecimal(0);
		BigDecimal overStockSum = new BigDecimal(0);
		for(GoodsStockSummarizingModel record : records){
			String initStock = record.getInitStock();
			if(StringUtils.isEmpty(initStock)){
				Integer goodsId = record.getGoodsId();
				params.put("goodsId",goodsId);
				initStock = super.baseMapper.selectGoodsBeginningStockOfInitialization(params);
				if (StringUtils.isEmpty(initStock)){
					initStock = "0";
				}
				record.setInitStock(initStock);
			}
			String overStock =
					new BigDecimal(record.getInitStock())
							.add(new BigDecimal(record.getMakeIn()))
							.add(new BigDecimal(record.getOtherIn()))
							.add(new BigDecimal(record.getReturnsIn()))
							.add(new BigDecimal(record.getOverflow()))
							.subtract(new BigDecimal(record.getSalesOut()))
							.subtract(new BigDecimal(record.getOtherOut()))
							.subtract(new BigDecimal(record.getBreakage()))
							.toString();
			record.setOverStock(overStock);

			initStockSum = initStockSum.add(new BigDecimal(record.getInitStock()));
			makeInSum = makeInSum.add(new BigDecimal(record.getMakeIn()));
			purchaseInSum = purchaseInSum.add(new BigDecimal(record.getPurchaseIn()));
			returnsInSum = returnsInSum.add(new BigDecimal(record.getReturnsIn()));
			returnsOutSum = returnsOutSum.add(new BigDecimal(record.getReturnsOut()));
			salesOutSum = salesOutSum.add(new BigDecimal(record.getSalesOut()));
			otherInSum = otherInSum.add(new BigDecimal(record.getOtherIn()));
			otherOutSum = otherOutSum.add(new BigDecimal(record.getOtherOut()));
			overflowSum = overflowSum.add(new BigDecimal(record.getOverflow()));
			breakageSum = breakageSum.add(new BigDecimal(record.getBreakage()));
			overStockSum = overStockSum.add(new BigDecimal(overStock));
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
		return goodsStockSummation;
	}

	@Override
	public Page<GoodsStockinStatisticsModel> getGoodsStockinStatistics(Map<String, Object> params, Page page) {


		List<GoodsStockinStatisticsModel> records = super.baseMapper.selectGoodsStockinStatistic(params,page);
//		int retain = SysUtil.getSysConfigRetain();
//		records.forEach(record->{
//			String avgPrice = new BigDecimal(record.getAvgPrice()).setScale(retain, RoundingMode.HALF_UP).toString();
//			record.setAvgPrice(avgPrice);
//		});
		int total = super.baseMapper.selectGoodsStockinStatisticsCount(params).size();
		page.setRecords(records);
		page.setTotal(total);
		return page;
	}

	@Override
	public GoodsStockinSummationModel getGoodsStockinSummation(Map<String, Object> params) {
		return super.baseMapper.selectGoodsStockinSummation(params);
	}


	public int getGoodsBeginningStock(Map<String,Object> params){

		return -1;

	}

}