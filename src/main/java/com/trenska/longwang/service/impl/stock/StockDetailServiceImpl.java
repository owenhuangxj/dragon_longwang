package com.trenska.longwang.service.impl.stock;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.constant.Constant;
import com.trenska.longwang.constant.WarningLevel;
import com.trenska.longwang.context.ApplicationContextHolder;
import com.trenska.longwang.dao.customer.CustomerMapper;
import com.trenska.longwang.dao.goods.GoodsMapper;
import com.trenska.longwang.dao.stock.GoodsStockMapper;
import com.trenska.longwang.dao.stock.StockDetailMapper;
import com.trenska.longwang.dao.stock.StockMapper;
import com.trenska.longwang.entity.goods.Goods;
import com.trenska.longwang.entity.indent.StockMadedate;
import com.trenska.longwang.entity.stock.GoodsStock;
import com.trenska.longwang.entity.stock.Stock;
import com.trenska.longwang.entity.stock.StockDetail;
import com.trenska.longwang.entity.sys.SysConfig;
import com.trenska.longwang.model.stock.StockWarningModel;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.stock.IGoodsStockService;
import com.trenska.longwang.service.stock.IStockDetailService;
import com.trenska.longwang.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 库存明细表 服务实现类
 *
 * @author Owen
 * @since 2019-04-15
 */
@Service
@Slf4j
@SuppressWarnings("all")
public class StockDetailServiceImpl extends ServiceImpl<StockDetailMapper, StockDetail> implements IStockDetailService {
	@Autowired
	private GoodsMapper goodsMapper;
	@Autowired
	private StockMapper stockMapper;
	@Autowired
	private CustomerMapper customerMapper;
	@Autowired
	private IGoodsStockService goodsStockService;
	@Autowired
	private RedisTemplate<String, String> redisTemplate;
	@Autowired
	private StockDetailMapper stockDetailMapper;
	@Autowired
	private GoodsStockMapper goodsStockMapper;

	/**
	 * 库存单和入库记录是 1 ：n的关系，所以一个stock_no对应多条入库记录
	 * 此时需要先 生成 stock_no,每个新批次的入库都保存相同的stock_no
	 * 但是存在入库相同批次的情况，此时根据批次(生产日期)更新库存并同步t_goods表的stock字段
	 */
	@Override
	@Transactional
	public ResponseModel stockin(Stock stock, HttpServletRequest request) {
		Integer empIdInToken = SysUtil.getEmpId();
		if (Objects.isNull(empIdInToken)) {
			return ResponseModel.getInstance().succ(false).msg(Constant.ACCESS_TIMEOUT_MSG).code(Constant.ACCESS_TIMEOUT);
		}
		stock.setEmpId(empIdInToken);
		return StockUtil.stockin(stock, stockMapper, goodsMapper);
	}

	@Override
	@Transactional
	public ResponseModel stockout(Stock stock, HttpServletRequest request) {

		Integer empIdInToken = SysUtil.getEmpId();
		if (NumberUtil.isIntegerNotUsable(empIdInToken)) {
			return ResponseModel.getInstance().succ(false).msg(Constant.ACCESS_TIMEOUT_MSG).code(Constant.ACCESS_TIMEOUT);
		}

		String stockTime = TimeUtil.getCurrentTime(Constant.TIME_FORMAT);
		String stockNo = StockUtil.getStockNo(stock.getPrefix(), Constant.CKD_CHINESE, stockMapper);
		// 获取出库详情
		stock.setStockNo(stockNo);
		stock.setEmpId(empIdInToken);
		stock.setStockTime(stockTime);
		List<StockDetail> stockouts = stock.getStockouts();
		/************************************处理出库记录，主要是保存出库编号，时间，操作者************************************/
		stockouts.forEach(stockoutDetail -> {
			stockoutDetail.setStockNo(stockNo);
			stockoutDetail.setEmpId(empIdInToken);
			stockoutDetail.setStockTime(stockTime);
			stockoutDetail.setOperType(stock.getOperType());
			stockoutDetail.setStockType(stock.getStockType());
		});
		// 将封装好的出库记录设置到stockouts属性
		stock.setStockouts(stockouts);
		return StockUtil.stockout(stock, goodsMapper);
	}

	@Override
	public Page<Stock> getStockDetailPageSelective(Map<String, Object> params, Page page) {

		Stock stock = stockMapper.selectStockInfo(params);

		List<StockDetail> stockDetails = super.baseMapper.selectStockDetailPage(params, page);
		Integer retain = ((SysConfig) ApplicationContextHolder.getBean(Constant.SYS_CONFIG_IDENTIFIER)).getRetain();

		//处理金额总计
		BigDecimal total = new BigDecimal(0);

		for (StockDetail stockDetail : stockDetails) {
			BigDecimal stockPrice = new BigDecimal(stockDetail.getStockPrice());
			BigDecimal sum = stockPrice.multiply(BigDecimal.valueOf(stockDetail.getHistory() / stockDetail.getMulti()));
			total = total.add(sum);
			// 保留retain位小数位，四舍五入
			sum = sum.setScale(retain, BigDecimal.ROUND_HALF_UP);
			stockDetail.setSum(NumberFormatter.format(sum, retain));
		}

		total.setScale(retain, BigDecimal.ROUND_HALF_UP);
		stock.setTotal(NumberFormatter.format(total, retain));

		stock.setStockDetails(stockDetails);
		List<Stock> stocks = new ArrayList<>();
		stocks.add(stock);
		page.setRecords(stocks);
		page.setTotal(super.baseMapper.selectStockDetailCount(params));
		return page;
	}

	/**
	 * 列出库存详细 批次-库存量
	 *
	 * @param params
	 * @param page
	 * @return GoodsMadeDateDetail
	 */
	@Override
	public Page<StockMadedate> getGoodsMadeDateDetail(Map<String, Object> params, Page page) {
		List<GoodsStock> goodsStocks = goodsStockMapper.selectGoodsStock(page, params);
		List<StockMadedate> records = new ArrayList<>();
		goodsStocks.forEach(goodsStock -> {
			StockMadedate stockMadedate = new StockMadedate();
			ObjectCopier.copyProperties(goodsStock, stockMadedate);
			records.add(stockMadedate);
		});

		page.setRecords(records);
		int total = goodsStockMapper.selectGoodsStockCount(params);
		page.setTotal(total);
		return page;
	}

	/**
	 * 查询单个商品的出/入库详情
	 *
	 * @param params
	 * @param page
	 * @return
	 */
	@Override
	public Page<StockDetail> getGoodsMadeDateStockInfo(Map<String, Object> params, Page page) {
		page.setRecords(super.baseMapper.selectGoodsMadeDateStockInfo(params, page));
		page.setTotal(super.baseMapper.selectGoodsMadeDateStockCount(params));
		return page;
	}

	@Override
	public Page<StockWarningModel> getStockWarningPage(Map<String, Object> params, Page page) {

		List<StockWarningModel> stockWarningModels = super.baseMapper.selectStockWarningPageSelective(params, page);
		stockWarningModels.forEach(warning -> {
			if (null == warning.getLeftDays()) {
				warning.setWarningLevel(WarningLevel.INIT_STOCK);
			} else {
				int passedMonths = warning.getPassedMonths();
				String warningLevel = StockDetailsUtil.getWarningLevel(passedMonths);
				warning.setWarningLevel(warningLevel);
			}
		});
		page.setRecords(stockWarningModels);
		List<Integer> count = super.baseMapper.selectStockWarningCount(params);
		page.setTotal(count.size());
		return page;
	}

	/**
	 * 作废入库单
	 *
	 * @param stockNo 入库单号
	 * @return
	 */
	@Override
	@Transactional
	public ResponseModel cancelStockin(String stockNo, HttpServletRequest request) {

		Integer empIdInToken = SysUtil.getEmpId();
		if (Objects.isNull(empIdInToken)) {
			return ResponseModel.getInstance().succ(false).msg(Constant.ACCESS_TIMEOUT_MSG).code(Constant.ACCESS_TIMEOUT);
		}
		String msg = "作废入库单成功";
		boolean successful = true;

		Stock dbStock = stockMapper.selectOne(
				new LambdaQueryWrapper<Stock>()
						.eq(Stock::getStockNo, stockNo)
						.eq(Stock::getStockType, Constant.RKD_CHINESE)
		);
		if (Objects.isNull(dbStock)) {
			return ResponseModel.getInstance().succ(false).msg("无效的入库单");
		}
		if (BooleanUtils.isFalse(dbStock.getStat())) {
			return ResponseModel.getInstance().succ(false).msg("作废的入库单无需再作废");
		}
		// 1.作废入库单；2.保存入库单作废详情(作废入库单后，在库存明细中增加一条记录(变更类型:入库单(作废)，操作类型：与原类型一致))
		stockMapper.updateById(new Stock(dbStock.getStockId(), false));// 作废入库单
		String stockTime = TimeUtil.getCurrentTime(Constant.TIME_FORMAT);
		// 获取商品入库详情
		List<StockDetail> dbStockinDetails = stockDetailMapper.selectList(
				new LambdaQueryWrapper<StockDetail>()
						.eq(StockDetail::getStat, true)
						.eq(StockDetail::getStockNo, stockNo)
						.eq(StockDetail::getStockType, Constant.RKD_CHINESE)
		);
		// 1.减少商品库存;2.作废入库详情3.保存一条入库单作废记录
		for (StockDetail stockinDetail : dbStockinDetails) {
			Goods dbGoods = goodsMapper.selectGoodsByGoodsId(stockinDetail.getGoodsId());// 获取商品信息，主要是为了获取商品总库存
			int history = stockinDetail.getHistory();// 商品历史入库数量
			int stock = dbGoods.getStock() - history;
			goodsMapper.updateById(new Goods(stockinDetail.getGoodsId(), dbGoods.getBrandName(), stock)); // 减少商品库存
			stockDetailMapper.updateById(new StockDetail(stockinDetail.getDetailId(), false, Constant.RKDZF_CHINESE)); // 作废入库详情

			/************************************ 保存库存明细************************************/
			stockinDetail.setNum(stock);
			stockinDetail.setStock(stock);
			stockinDetail.setEmpId(empIdInToken);
			stockinDetail.setStockType(Constant.RKDZF_CHINESE);
			StockDetailsUtil.dbLogStockDetail(stockinDetail);
		}
		return ResponseModel.getInstance().succ(successful).msg(msg);

	}

	/**
	 * 作废出库单
	 *
	 * @param stockNo
	 * @param request
	 * @return
	 */
	@Override
	@Transactional
	public ResponseModel cancelStockout(String stockNo, HttpServletRequest request) {

		Stock dbStockout = stockMapper.selectOne(
				new LambdaQueryWrapper<Stock>()
						.eq(Stock::getStockNo, stockNo)
		);
		if (Objects.isNull(dbStockout)) {
			return ResponseModel.getInstance().succ(false).msg("无效的出库单");
		}
		if (BooleanUtils.isFalse(dbStockout.getStat())) {
			return ResponseModel.getInstance().succ(false).msg("作废的出库单无需再作废");
		}

		List<StockDetail> stockDetails = stockDetailMapper.selectList(
				new LambdaQueryWrapper<StockDetail>()
						.eq(StockDetail::getStockNo, stockNo)
						.eq(StockDetail::getStat, true)
		);

		dbStockout.setStockDetails(stockDetails);

		List<Stock> stockouts = new ArrayList<>();
		stockouts.add(dbStockout);

		return StockUtil.invalidStockout(stockouts);
	}

	@Override
	@Transactional
	public ResponseModel changeStockin(Stock stock, HttpServletRequest request) throws IOException {
		String stockNo = stock.getStockNo();

		Stock dbStockin = stockMapper.selectByStockNo(stockNo);
		if (Objects.isNull(dbStockin)) {
			return ResponseModel.getInstance().succ(false).msg("无效的入库单");
		}
		if (!dbStockin.getStat()) {
			return ResponseModel.getInstance().succ(false).msg("不能修改作废的入库单");
		}

		Map<String, Object> params = new HashMap<>();
		params.put("stockNo", stockNo);
		params.put("stat", Constant.VALID);
		params.put("stockType", Constant.RKD_CHINESE);
		List<StockDetail> dbStockDetailList = stockDetailMapper.selectByParams(params);
		dbStockDetailList.forEach(stockDetail -> {
			stockDetail.deleteById();
			/* 标记为作废->StockDetailsUtil#dbLogStockDetail()方法根据stockType来判断库存是增还是减 */
			stockDetail.setStockType(Constant.RKDZF_CHINESE);
			stockDetail.setStock(stockDetail.getStock() - stockDetail.getHistory());
			StockDetailsUtil.dbLogStockDetail(stockDetail);
			GoodsUtil.changeGoodsStock(stockDetail.getGoodsId(), -stockDetail.getHistory());
		});

		stock.setEmpId(SysUtil.getEmpId());

		List<StockDetail> stockDetailList = getDistinctStockDetails(stock.getStockins());
		stock.setStockId(dbStockin.getStockId());
		StockUtil.changeStockin(stock, stockDetailList, goodsMapper);
		return ResponseModel.getInstance().succ(true).msg("更改入库单成功.");
	}

	/**
	 * 将商品去重，并且转换为小单位，相同goodsId，生成批次的商品最终返回一条multi为1的记录
	 *
	 * @param newStockDetailList 入库单的新的商品详情
	 * @return 去重后的商品
	 */
	private List<StockDetail> getDistinctStockDetails(List<StockDetail> newStockDetailList) {
		Map<String, Long> map =
			newStockDetailList.stream().collect(
				Collectors.groupingBy(StockDetail::getGroupingbyKey, Collectors.summingLong(StockDetail::getStockNumber))
			);
		List<StockDetail> distinctGoodsIdAndMadedateList = map.keySet().stream().map(goodsIdAndMadedateKey -> {
			String[] arr = goodsIdAndMadedateKey.split(Constant.SPLITTER);
			int goodsId = Integer.valueOf(arr[0]);
			String madedate = arr[1];
			StockDetail stockDetail = new StockDetail();
			StockDetail source =
				newStockDetailList.stream().filter(stkDetail -> stkDetail.getGroupingbyKey().equals(goodsIdAndMadedateKey)).findFirst().get();
			ObjectCopier.copyProperties(source,stockDetail);
			int num = map.get(goodsIdAndMadedateKey).intValue();
			stockDetail.setHistory(num);
			stockDetail.setNum(num);
			stockDetail.setMulti(Constant.DEFAULT_MULTI);
			return stockDetail;
		}).collect(Collectors.toList());


		return distinctGoodsIdAndMadedateList;
	}
}