package com.trenska.longwang.controller.stock;


import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.entity.PageHelper;
import com.trenska.longwang.entity.goods.Goods;
import com.trenska.longwang.entity.stock.GoodsStock;
import com.trenska.longwang.entity.stock.Stock;
import com.trenska.longwang.entity.stock.StockDetail;
import com.trenska.longwang.entity.stock.StockDetails;
import com.trenska.longwang.model.stock.GoodsStockModel;
import com.trenska.longwang.model.stock.StockWarningModel;
import com.trenska.longwang.service.goods.IGoodsService;
import com.trenska.longwang.service.stock.IGoodsStockService;
import com.trenska.longwang.service.stock.IStockDetailService;
import com.trenska.longwang.service.stock.IStockService;
import com.trenska.longwang.util.PageUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Owen
 * @since 2019-04-17
 */
@Slf4j
@RestController
@RequestMapping("/stock")
@Api(description = "库存接口")
@CrossOrigin
public class StockController {

	@Autowired
	private IStockService stockService;

	@Autowired
	private IGoodsService goodsService;

	@Autowired
	private IStockDetailService stockDetailService;

	@Autowired
	private IGoodsStockService goodsStockService;

	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "brandName", value = "品牌", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "propName", value = "规格", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "combine", value = "商品编号/名称/条码", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "frtCatName", value = "商品一级分类", paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "scdCatName", value = "商品二级分类", paramType = "body", dataType = "string")
	})
	@GetMapping("/list/page/{current}/{size}")
	@ApiOperation("商品库存通用分页")
	public PageHelper<Goods> listStockStatusPage(
			@PathVariable("current") Integer current, @PathVariable("size") Integer size,
			@RequestParam(required = false, name = "brandName") String brandName,
			@RequestParam(required = false, name = "combine") String combine,
			@RequestParam(required = false, name = "propName") String propName,
			@RequestParam(required = false, name = "frtCatName") String frtCatName,
			@RequestParam(required = false, name = "scdCatName") String scdCatName
	) {
		Map<String, Object> params = new HashMap<>();
		params.put("brandName", brandName);
		params.put("propName", propName);
		params.put("combine", combine);
		params.put("frtCatName", frtCatName);
		params.put("scdCatName", scdCatName);
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Page<Goods> pageInfo = stockService.getStockStatusPageSelective(params, page);
		return PageHelper.getInstance().pageData(pageInfo);
	}

	@GetMapping("/list/page/detail/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "beginTime", value = "时间段查询条件-开始", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "endTime", value = "时间段查询条件-结束", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "stockType", value = "变更类型", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "operType", value = "操作类型", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "busiNo", value = "业务单号", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "goodsId", value = "商品id", paramType = "query", dataType = "int")
	})
	@ApiOperation("库存明细通用分页,查看商品库存时调用此接口并传递goodsId")
	public PageHelper<StockDetails> listGoodsStockDetailPage(
			@PathVariable(value = "current") Integer current, @PathVariable(value = "size") Integer size,
			@RequestParam(required = false, name = "beginTime") String beginTime,
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "stockType") String stockType,
			@RequestParam(required = false, name = "operType") String operType,
			@RequestParam(required = false, name = "busiNo") String busiNo,
			@RequestParam(required = false, name = "goodsId") Integer goodsId
	) {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Map<String, Object> params = new HashMap<>();
		params.put("beginTime", beginTime);
		params.put("endTime", endTime);
		params.put("stockType", stockType);
		params.put("operType", operType);
		params.put("busiNo", busiNo);
		params.put("goodsId", goodsId);
		Page<StockDetails> pageInfo = stockService.getStockDetaislPage(params, page);
		Goods goods = goodsService.getGoodsByGoodsId(goodsId);
		return PageHelper.getInstance().pageData(pageInfo).summarizing(goods);
	}

	/*********************************************获取商品的批次库存分页***********************************************/
	@GetMapping("/list/page/goods-stock/all/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int")
	})
	@ApiOperation("获取商品的批次库存分页")
	public PageHelper<GoodsStockModel> listGoodsStocksPage(@PathVariable(value = "current") Integer current,@PathVariable(value = "size") Integer size) {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Page<GoodsStockModel> pageInfo = goodsStockService.getGoodsMadeDates(page);
		return PageHelper.getInstance().pageData(pageInfo);
	}

	@GetMapping("/list/page/goods-stock/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "goodsId", value = "商品id", required = false, paramType = "query", dataType = "int")
	})
	@ApiOperation("获取商品的批次库存分页,查询所有商品不传递goodsId或者goodsId传空")
	public PageHelper<GoodsStock> listGoodsStockPage(
			@PathVariable(value = "current") Integer current,
			@PathVariable(value = "size") Integer size,
			@RequestParam(name = "goodsId",required = false) Integer goodsId
	) {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Map<String, Object> params = new HashMap<>();
		params.put("goodsId", goodsId);
		Page<GoodsStock> pageInfo = goodsStockService.getGoodsMadeDate(params, page);
		return PageHelper.getInstance().pageData(pageInfo);
	}

	/*********************************入库/出库通用分页,包括报溢报损*********************************/
	@GetMapping("/list/page/stock/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "stat", value = "出库/入库/报溢/报损状态", paramType = "query", dataType = "boolean"),
			@ApiImplicitParam(name = "beginTime", value = "时间段查询条件-开始", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "endTime", value = "时间段查询条件-结束", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "stockNo", value = "出库/入库/报溢/报损单号", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "stockType", value = "出库单/入库单", paramType = "query", dataType = "string", required = true),
			@ApiImplicitParam(name = "operType", value = "操作类型:采购入库/退货入库...；销售出库/其它出库...", paramType = "query", dataType = "string")
	})
	@ApiOperation("入库/出库通用分页")
	public PageHelper<Stock> listStockPage(
			@PathVariable(value = "current") Integer current,
			@PathVariable(value = "size") Integer size,
			@RequestParam(value = "stockType") String stockType,
			@RequestParam(required = false, name = "stat") Boolean stat,
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "stockNo") String stockNo,
			@RequestParam(required = false, name = "operType") String operType,
			@RequestParam(required = false, name = "beginTime") String beginTime
	) {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Map<String, Object> params = new HashMap<>();
		params.put("endTime", endTime);
		params.put("beginTime", beginTime);
		params.put("operType", operType);
		params.put("stockType", stockType);
		params.put("stockNo", stockNo);
		params.put("stat", stat);
		Page<Stock> pageInfo = stockService.getStockPageSelective(params, page);
		return PageHelper.getInstance().pageData(pageInfo);
	}

	@GetMapping("/list/page/stock/detail/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "stockTime", value = "库存操作时间",paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "stat", value = "出库/入库/报溢/报损状态",paramType = "query", dataType = "boolean"),
			@ApiImplicitParam(name = "stockType", value = "出库/入库", required = true, paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "stockNo", value = "出库/入库/报溢/报损单号", required = true, paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "operType", value = "操作类型:采购入库/退货入库...；销售出库/其它出库...", paramType = "query", dataType = "string"),
	})
	@ApiOperation("入库/出库详情通用分页")
	public PageHelper<Stock> listStockDetailPage(
			@PathVariable(value = "current") Integer current,
			@PathVariable(value = "size") Integer size,
			@RequestParam(name = "stockNo") String stockNo,
			@RequestParam(value = "stockType") String stockType,
			@RequestParam(required = false,name = "stat") Boolean stat,
			@RequestParam(required = false,value = "operType") String operType,
			@RequestParam(required = false,name = "stockTime") String stockTime
	) {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Map<String, Object> params = new HashMap<>();
		params.put("stat", stat);
		params.put("operType", operType);
		params.put("stockType", stockType);
		params.put("stockTime", stockTime);
		params.put("stockNo", stockNo);
		Page<Stock> pageInfo = stockDetailService.getStockDetailPageSelective(params, page);
		return PageHelper.getInstance().pageData(pageInfo);
	}

	@GetMapping("/list/page/warning/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "combine", value = "商品名称/编号/条码", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "label", value = "标签 : 新货/滞销风险商品/滞销商品/临期商品/过期商品", paramType = "query", dataType = "string")
	})
	@ApiOperation("商品预警分页")
	public PageHelper<StockWarningModel> listStockWarningPage(
			@PathVariable(value = "current") Integer current,
			@PathVariable(value = "size") Integer size,
			@RequestParam(name = "combine",required = false) String combine,
			@RequestParam(name = "label",required = false) String label
	) {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Map<String,Object> params = new HashMap<>();
		params.put("combine",combine);
		params.put("label",label);
		Page<StockWarningModel> pageInfo = stockDetailService.getStockWarningPage(params, page);
		return PageHelper.getInstance().pageData(pageInfo);
	}

	/**如果max不为null表示要找出库存量小于max的库存明细*/
	@GetMapping("/list/page/made-date/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "stockType", value = "出库单/入库单",paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "max", value = "找出库存量小于max的库存明细",paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "goodsId", value = "商品id", required = true, paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "madeDate", value = "商品生产批次", required = true, paramType = "query", dataType = "string"),
	})
	@ApiOperation("查询商品生产批次出/入库详情,如果不传stockType就是查商品的所有类型出入库详情")
	public PageHelper<StockDetail> listStockDetailPage(
			@PathVariable(value = "current") Integer current,
			@PathVariable(value = "size") Integer size,
			@RequestParam(name = "max") Integer max,
			@RequestParam(name = "goodsId") Integer goodsId,
			@RequestParam(name = "madeDate") String madeDate,
			@RequestParam(name = "stockType",required = false) String stockType
	){
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Map<String,Object> params = new HashMap<>();
		params.put("max",max);
		params.put("goodsId",goodsId);
		params.put("madeDate",madeDate);
		params.put("stockType",stockType);
		Page<StockDetail> pageInfo = stockDetailService.getGoodsMadeDateStockInfo(params, page);
		return PageHelper.getInstance().pageData(pageInfo);
	}

}