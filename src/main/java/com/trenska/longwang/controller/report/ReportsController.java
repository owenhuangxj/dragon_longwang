package com.trenska.longwang.controller.report;

import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.dao.customer.AreaGrpMapper;
import com.trenska.longwang.entity.PageHelper;
import com.trenska.longwang.entity.sys.SysEmp;
import com.trenska.longwang.model.report.*;
import com.trenska.longwang.service.customer.ICustomerService;
import com.trenska.longwang.service.financing.IReceiptService;
import com.trenska.longwang.service.indent.IIndentService;
import com.trenska.longwang.service.stock.IStockService;
import com.trenska.longwang.util.PageUtils;
import com.trenska.longwang.util.SysUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 2019/5/18
 * 创建人:Owen
 */
@Slf4j
@CrossOrigin
@RestController
@SuppressWarnings("all")
@RequestMapping("/reports")
@Api(description = "报表接口")

public class ReportsController {

	@Autowired
	private ResourceLoader resourceLoader;//见名知义：资源加载器

	@Autowired
	private IIndentService indentService;

	@Autowired
	private IStockService stockService;

	@Autowired
	private IReceiptService receiptService;

	@Autowired
	private ICustomerService customerService;

	@Autowired
	private AreaGrpMapper areaGrpMapper;

	@GetMapping("/cust-sales-bill/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "custName", value = "客户名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "custId", value = "客户id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "beginTime", value = "时间段-开始", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "endTime", value = "时间段-结束", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "areaGrpId", value = "区域id", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "brandName", value = "品牌", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "salesmanId", value = "所属员工id", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "shipmanId", value = "送货人id", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "goodsScope", value = "正品/赠品/所有", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "frtCatName", value = "商品一级分类", paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "scdCatName", value = "商品二级分类", paramType = "body", dataType = "string")
	})
	@ApiOperation("客户销售总账")
	public PageHelper<CustSalesBillModel> custSalesBillAmount(
			@RequestParam(required = false, name = "custId") Integer custId,
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "custName") String custName,
			@RequestParam(required = false, name = "brandName") String brandName,
			@RequestParam(required = false, value = "beginTime") String beginTime,
			@RequestParam(required = false, name = "areaGrpId") Integer areaGrpId,
			@RequestParam(required = false, name = "frtCatName") String frtCatName,
			@RequestParam(required = false, name = "scdCatName") String scdCatName,
			@RequestParam(required = false, name = "shipmanId") Integer shipmanId,
			@RequestParam(required = false, name = "salesmanId") Integer salesmanId,
			@RequestParam(required = false, name = "goodsScope") Integer goodsScope,
			@PathVariable(value = "current") Integer current, @PathVariable(value = "size") Integer size
	) {
		Map<String, Object> params = new HashMap<>();
		params.put("custId", custId);
		params.put("endTime", endTime);
		params.put("custName", custName);
		params.put("frtCatName", frtCatName);
		params.put("scdCatName", scdCatName);
		params.put("brandName", brandName);
		params.put("areaGrpId", areaGrpId);
		params.put("beginTime", beginTime);
		params.put("goodsScope", goodsScope);
		params.put("shipmanId", shipmanId);
		params.put("salesmanId", salesmanId);
		params.put("employeeId",SysUtil.getEmpIdInToken());
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		if (areaGrpId != null) {
			List<Integer> areaGrpIds = areaGrpMapper.selectAllChildrenByAreaGrpId(areaGrpId);
			params.put("areaGrpIds",areaGrpIds);
		}
		Page<CustSalesBillModel> pageInfo = indentService.getCustSales(params, page);
		return PageHelper.getInstance().pageData(pageInfo);
	}

	@GetMapping("/cust-sales-summarizing/{current}/{size}")
	@ApiOperation("客户销售汇总")
	public PageHelper<CustSalesSummarizingModel> custSalesSummarizing(
			CustSalesSummarizingSearchModel searchModel,
			@PathVariable(value = "size") int size, @PathVariable(value = "current") int current
	) {
//		SysEmp sysEmp = (SysEmp) SecurityUtils.getSubject().getPrincipal();
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		searchModel.setEmployeeId(SysUtil.getEmpIdInToken());
		if (searchModel.getAreaGrpId() != null) {
			List<Integer> areaGrpIds = areaGrpMapper.selectAllChildrenByAreaGrpId(searchModel.getAreaGrpId());
			searchModel.setAreaGrpIds(areaGrpIds);
		}
		CustSalesSummationModel summarizing = indentService.getCustSalesSummation(searchModel);
		Page<CustSalesSummarizingModel> pageInfo = indentService.getCustSalesSummarizing(searchModel, page);
		return PageHelper.getInstance().pageData(pageInfo).summarizing(summarizing);
	}

	@GetMapping("/cust-sales-statistic/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "shipmanId", value = "送货人id", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "custId", value = "客户id", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "custName", value = "客户名称", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "beginTime", value = "时间段-开始", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "endTime", value = "时间段-结束", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "frtCatName", value = "商品一级分类", paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "scdCatName", value = "商品二级分类", paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "goodsScope", value = "-1：只看赠品 0：只看商品 1：看所有", paramType = "query", dataType = "int")
	})
	@ApiOperation("客户销售统计-->销售账本的商品汇总")
	public PageHelper<CustSalesStatisticsModel> custSalesStatistic(
			@RequestParam(required = false, name = "custId") Integer custId,
			@RequestParam(required = false, name = "custName") String custName,
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "goodsScope") int goodsScope,
			@RequestParam(required = false, name = "beginTime") String beginTime,
			@RequestParam(required = false, name = "shipmanId") Integer shipmanId,
			@RequestParam(required = false, name = "frtCatName") String frtCatName,
			@RequestParam(required = false, name = "scdCatName") String scdCatName,
			@PathVariable(value = "current") Integer current, @PathVariable(value = "size") Integer size

	) {
		Map<String, Object> params = new HashMap<>();
		params.put("custId", custId);
		params.put("endTime", endTime);
		params.put("custName", custName);
		params.put("shipmanId", shipmanId);
		params.put("beginTime", beginTime);
		params.put("goodsScope", goodsScope);
		params.put("frtCatName", frtCatName);
		params.put("scdCatName", scdCatName);
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		CustSalesStatisticsSummationModel summarizing = indentService.selectCustSalesStatisticsSummation(params);
		Page<CustSalesStatisticsModel> pageInfo = indentService.getCustSalesStatistics(params, page);
		return PageHelper.getInstance().pageData(pageInfo).summarizing(summarizing);
	}

	@GetMapping("/cust-sales-detail/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "brandName", value = "品牌", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "custId", value = "客户", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "custName", value = "客户名称", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "beginTime", value = "时间段-开始", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "endTime", value = "时间段-结束", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "areaGrpId", value = "区域id", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "salesmanId", value = "所属员工id", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "frtCatName", value = "商品一级分类", paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "scdCatName", value = "商品二级分类", paramType = "body", dataType = "string")
	})
	@ApiOperation("客户销售明细 --> 销售账本的客户订单汇总")
	public PageHelper<CustSalesDetailModel> custSalesDetail(
			@RequestParam(required = false, name = "custId") Integer custId,
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "brandName") String brandName,
			@RequestParam(required = false, name = "areaGrpId") Integer areaGrpId,
			@RequestParam(required = false, value = "beginTime") String beginTime,
			@RequestParam(required = false, name = "frtCatName") String frtCatName,
			@RequestParam(required = false, name = "scdCatName") String scdCatName,
			@RequestParam(required = false, name = "salesmanId") Integer salesmanId,
			@PathVariable(value = "current") Integer current, @PathVariable(value = "size") Integer size
	) {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Map<String, Object> params = new HashMap<>();
		params.put("custId", custId);
		params.put("endTime", endTime);
		params.put("brandName", brandName);
		params.put("areaGrpId", areaGrpId);
		params.put("beginTime", beginTime);
		params.put("frtCatName", frtCatName);
		params.put("scdCatName", scdCatName);
		params.put("salesmanId", salesmanId);
		Page<CustSalesDetailModel> pageInfo = indentService.getCustSalesDetail(params, page);
		CustSalesDetailSummarizingModel summarizing = indentService.getCustSalesDetailSummarizing(params);

		if (ObjectUtils.isEmpty(summarizing)) {
			summarizing = new CustSalesDetailSummarizingModel();
		}
		BigDecimal salesAmntSum = new BigDecimal(summarizing.getSalesAmntSum());
		BigDecimal receivableAmntSum = new BigDecimal(summarizing.getReceivableAmntSum());
		summarizing.setSalesDiscountSum(salesAmntSum.subtract(receivableAmntSum).toString());

		return PageHelper.getInstance().pageData(pageInfo).summarizing(summarizing);
	}

	@GetMapping("/area-sales-rank/first-level")
	@ApiOperation("一级区域销售排名")
	public PageHelper<AreaSalesRankModel> getFirstLevelAreaSalesRank() {
		Page page = PageUtils.getPageParam(new PageHelper(1, 10));
		Page<AreaSalesRankModel> pageInfo = indentService.getFirstLevelAreaSalesRank(page);
		return PageHelper.getInstance().pageData(pageInfo);
	}

	@GetMapping("/area-sales-rank/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "areaGrpId", value = "区域分组id", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "endTime", value = "时间段-结束", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "beginTime", value = "时间段-开始", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "current", value = "当前页", required = true, dataType = "int", paramType = "path"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, dataType = "int", paramType = "path"),
			@ApiImplicitParam(name = "frtCatName", value = "商品一级分类", paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "scdCatName", value = "商品二级分类", paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "byAmount", value = "true:按销售金额排名;false:按销售数量排名 ", dataType = "boolean", paramType = "query")
	})
	@ApiOperation("区域销售排名")
	public PageHelper<AreaSalesRankModel> areaSalesRank(
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "areaGrpId") Integer areaGrpId,
			@RequestParam(required = false, value = "beginTime") String beginTime,
			@RequestParam(required = false, name = "frtCatName") String frtCatName,
			@RequestParam(required = false, name = "scdCatName") String scdCatName,
			@RequestParam(required = false, name = "byAmount", defaultValue = "true") boolean byAmount,
			@PathVariable(value = "current") Integer current, @PathVariable(value = "size") Integer size
	) {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Map<String, Object> params = new HashMap<>();
		params.put("endTime", endTime);
		params.put("byAmount", byAmount);
		params.put("areaGrpId", areaGrpId);
		params.put("beginTime", beginTime);
		params.put("frtCatName", frtCatName);
		params.put("scdCatName", scdCatName);
		Page<AreaSalesRankModel> pageInfo = indentService.getAreaSalesRank(params, page);
		CommonSummation summarizing = indentService.getAreaSalesRankSummation(params);
		return PageHelper.getInstance().pageData(pageInfo).summarizing(summarizing);
	}

	@GetMapping("/cust-sales-rank/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "salesmanId", value = "业务员", paramType = "string", dataType = "int"),
			@ApiImplicitParam(name = "areaGrpId", value = "区域分组id", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "endTime", value = "时间段-结束", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "beginTime", value = "时间段-开始", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "frtCatName", value = "商品一级分类", paramType = "string", dataType = "string"),
			@ApiImplicitParam(name = "scdCatName", value = "商品二级分类", paramType = "string", dataType = "string"),
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "byAmount", value = "true:按销售金额排名;false:按销售数量排名 ", dataType = "boolean", paramType = "query")
	})
	@ApiOperation("客户销售排名")
	public PageHelper<CustSalesRankModel> custSalesRank(
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, value = "beginTime") String beginTime,
			@RequestParam(required = false, name = "areaGrpId") Integer areaGrpId,
			@RequestParam(required = false, name = "frtCatName") String frtCatName,
			@RequestParam(required = false, name = "scdCatName") String scdCatName,
			@RequestParam(required = false, value = "salesmanId") Integer salesmanId,
			@RequestParam(required = false, name = "byAmount", defaultValue = "true") boolean byAmount,
			@PathVariable(value = "current") Integer current, @PathVariable(value = "size") Integer size
	) {
		Map<String, Object> params = new HashMap<>();
		params.put("endTime", endTime);
		params.put("byAmount", byAmount);
		params.put("areaGrpId", areaGrpId);
		params.put("beginTime", beginTime);
		params.put("frtCatName", frtCatName);
		params.put("scdCatName", scdCatName);
		params.put("salesmanId", salesmanId);
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Page<CustSalesRankModel> pageInfo = indentService.getCustSalesRank(params, page);
		CommonSummation summation = indentService.getCustSalesRankSummation(params);
		return PageHelper.getInstance().pageData(pageInfo).summarizing(summation);
	}

	////////////////////////////////////////////////////////商品销售统计\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\


	/**
	 * 商品销售汇总不做数据权限控制
	 */
	@GetMapping("/goods-sales-summarizing/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "combine", value = "商品编号/名称/条码", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "specPropId", value = "规格", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "frtCatName", value = "商品一级分类", paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "scdCatName", value = "商品二级分类", paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "beginTime", value = "时间段开始", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "endTime", value = "时间段结束", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "areaGrpId", value = "区域", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "custId", value = "客户id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "custName", value = "客户名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "brandName", value = "品牌", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "shipmanId", value = "送货人", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "salesmanId", value = "所属员工", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "empId", value = "制单人", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "goodsScope", value = "-1：只看赠品 0：只看商品 1：看所有", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "discount", value = "扣点", paramType = "query", dataType = "string")
	})
	@ApiOperation("商品销售汇总")
	public PageHelper<GoodsSalesSummarizingModel> goodsSalesSummarizing(
			HttpServletRequest request,
			@RequestParam(required = false, name = "empId") Integer empId,
			@RequestParam(required = false, name = "custId") Integer custId,
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "combine") String combine,
			@RequestParam(required = false, name = "custName") String custName,
			@RequestParam(required = false, name = "discount") String discount,
			@RequestParam(required = false, name = "brandName") String brandName,
			@RequestParam(required = false, name = "beginTime") String beginTime,
			@RequestParam(required = false, name = "areaGrpId") Integer areaGrpId,
			@RequestParam(required = false, name = "shipmanId") Integer shipmanId,
			@RequestParam(required = false, name = "frtCatName") String frtCatName,
			@RequestParam(required = false, name = "scdCatName") String scdCatName,
			@RequestParam(required = false, name = "goodsScope") Integer goodsScope,
			@RequestParam(required = false, name = "specPropId") Integer specPropId,
			@RequestParam(required = false, name = "salesmanId") Integer salesmanId,
			@PathVariable(value = "current") Integer current, @PathVariable(value = "size") Integer size
	) {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Map<String, Object> params = new HashMap<>();
		params.put("empId", empId);
		params.put("custId", custId);
		params.put("combine", combine);
		params.put("endTime", endTime);
		params.put("custName", custName);
		params.put("discount", discount);
		params.put("brandName", brandName);
		params.put("shipmanId", shipmanId);
		params.put("beginTime", beginTime);
		params.put("areaGrpId", areaGrpId);
		params.put("frtCatName", frtCatName);
		params.put("scdCatName", scdCatName);
		params.put("goodsScope", goodsScope);
		params.put("specPropId", specPropId);
		params.put("salesmanId", salesmanId);
		if (areaGrpId != null) {
			List<Integer> areaGrpIds = areaGrpMapper.selectAllChildrenByAreaGrpId(areaGrpId);
			params.put("areaGrpIds",areaGrpIds);
		}
		CommonSummation summarizing = indentService.getGoodsSalesSummation(params);
		Page<GoodsSalesSummarizingModel> pageInfo = indentService.getGoodsSalesSummarizing(params, page);
		return PageHelper.getInstance().pageData(pageInfo).summarizing(summarizing);
	}

	/**
	 * 商品销售明细不做数据权限控制
	 */
	@GetMapping("/goods-sales-details/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "goodsId", value = "商品id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "combine", value = "商品编号/名称/条码", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "specPropId", value = "规格", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "frtCatName", value = "商品一级分类", paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "scdCatName", value = "商品二级分类", paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "beginTime", value = "时间段开始", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "endTime", value = "时间段结束", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "areaGrpId", value = "区域", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "custId", value = "客户", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "brandName", value = "品牌", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "shipmanId", value = "送货人", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "salesmanId", value = "所属员工", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "empId", value = "制单人", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "goodsScope", value = "-1：只看赠品 0：只看商品 1：看所有", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "discount", value = "扣点", paramType = "query", dataType = "int")
	})
	@ApiOperation("商品销售明细")
	public PageHelper<SingleGoodsSalesDetailModel> singleGoodsSalesDetail(
			@RequestParam(name = "goodsId") Integer goodsId,
			@RequestParam(required = false, name = "goodsScope") Integer goodsScope,
			@RequestParam(required = false, name = "empId") Integer empId,
			@RequestParam(required = false, name = "custId") Integer custId,
			@RequestParam(required = false, name = "combine") String combine,
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "discount") Integer discount,
			@RequestParam(required = false, name = "brandName") String brandName,
			@RequestParam(required = false, name = "beginTime") String beginTime,
			@RequestParam(required = false, name = "areaGrpId") Integer areaGrpId,
			@RequestParam(required = false, name = "shipmanId") Integer shipmanId,
			@RequestParam(required = false, name = "frtCatName") String frtCatName,
			@RequestParam(required = false, name = "scdCatName") String scdCatName,
			@RequestParam(required = false, name = "specPropId") Integer specPropId,
			@RequestParam(required = false, name = "salesmanId") Integer salesmanId,
			@PathVariable(value = "current") Integer current, @PathVariable(value = "size") Integer size
	) {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Map<String, Object> params = new HashMap<>();
		params.put("empId", empId);
		params.put("custId", custId);
		params.put("combine", combine);
		params.put("endTime", endTime);
		params.put("goodsId", goodsId);
		params.put("discount", discount);
		params.put("brandName", brandName);
		params.put("beginTime", beginTime);
		params.put("areaGrpId", areaGrpId);
		params.put("shipmanId", shipmanId);
		params.put("goodsScope", goodsScope);
		params.put("specPropId", specPropId);
		params.put("salesmanId", salesmanId);
		params.put("frtCatName", frtCatName);
		params.put("scdCatName", scdCatName);
		if (areaGrpId != null) {
			List<Integer> areaGrpIds = areaGrpMapper.selectAllChildrenByAreaGrpId(areaGrpId);
			params.put("areaGrpIds",areaGrpIds);
		}
		/**
		 * 实际上外层只有一条记录，内层的订货单信息为分页信息
		 */
		Page<SingleGoodsSalesDetailModel> pageInfo = indentService.getSingleGoodsSalesDetail(params, page);

		return PageHelper.getInstance().pageData(pageInfo);
	}

	/**
	 * 商品销售排名不做数据权限控制
	 */
	@GetMapping("/goods-sales-rank/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "areaGrpId", value = "区域", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "brandName", value = "品牌", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "salesmanId", value = "业务员id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "custName", value = "客户名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "endTime", value = "时间段结束", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "beginTime", value = "时间段开始", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "frtCatName", value = "商品一级分类", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "scdCatName", value = "商品二级分类", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "statisticsWay", value = "统计方式 : 商品 1 , 品牌 2 ，分类 3", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "byAmount", value = "true:按销售金额排名;false:按销售数量排名 ", paramType = "query", dataType = "boolean")
	})
	@ApiOperation("商品销售排名")
	public PageHelper<GoodsSalesRankModel> goodsSalesRank(
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "custName") String custName,
			@RequestParam(required = false, name = "beginTime") String beginTime,
			@RequestParam(required = false, name = "brandName") String brandName,
			@RequestParam(required = false, name = "areaGrpId") Integer areaGrpId,
			@RequestParam(required = false, name = "frtCatName") String frtCatName,
			@RequestParam(required = false, name = "scdCatName") String scdCatName,
			@RequestParam(required = false, name = "salesmanId") Integer salesmanId,
			@RequestParam(required = false, name = "byAmount", defaultValue = "true") boolean byAmount,
			@PathVariable(value = "current") Integer current, @PathVariable(value = "size") Integer size,
			@RequestParam(required = false, name = "statisticsWay", defaultValue = "1") Integer statisticsWay
	) {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Map<String, Object> params = new HashMap<>();
		params.put("endTime", endTime);
		params.put("byAmount", byAmount);
		params.put("custName", custName);
		params.put("beginTime", beginTime);
		params.put("brandName", brandName);
		params.put("areaGrpId", areaGrpId);
		params.put("salesmanId", salesmanId);
		params.put("frtCatName", frtCatName);
		params.put("scdCatName", scdCatName);
		params.put("statisticsWay", statisticsWay);
		if (areaGrpId != null) {
			List<Integer> areaGrpIds = areaGrpMapper.selectAllChildrenByAreaGrpId(areaGrpId);
			params.put("areaGrpIds",areaGrpIds);
		}
		Page<GoodsSalesRankModel> pageInfo = indentService.getGoodsSalesRank(params, page);
		CommonSummation summation = indentService.getGoodsSalesRankSummation(params);
		return PageHelper.getInstance().pageData(pageInfo).summarizing(summation);
	}
	//////////////////////////////////////////////////////商品销售统计结束\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

	/**
	 * 业务员销售排名不做数据权限控制
	 */
	@GetMapping("/salesman-sales-rank/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "areaGrpId", value = "区域", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "brandName", value = "品牌", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "endTime", value = "时间段结束", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "beginTime", value = "时间段开始", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "frtCatName", value = "商品一级分类", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "scdCatName", value = "商品二级分类", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "byAmount", value = "true:按销售金额排名;false:按销售数量排名 ", paramType = "query", dataType = "boolean")
	})
	@ApiOperation("业务员销售排名")
	public PageHelper<SalesmanSalesRankModel> salesmanSalesRank(
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "beginTime") String beginTime,
			@RequestParam(required = false, name = "brandName") String brandName,
			@RequestParam(required = false, name = "areaGrpId") Integer areaGrpId,
			@RequestParam(required = false, name = "frtCatName") String frtCatName,
			@RequestParam(required = false, name = "scdCatName") String scdCatName,
			@RequestParam(required = false, name = "byAmount", defaultValue = "true") boolean byAmount,
			@PathVariable(value = "current") Integer current, @PathVariable(value = "size") Integer size
	) {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Map<String, Object> params = new HashMap<>();
		params.put("endTime", endTime);
		params.put("byAmount", byAmount);
		params.put("beginTime", beginTime);
		params.put("areaGrpId", areaGrpId);
		params.put("brandName", brandName);
		params.put("frtCatName", frtCatName);
		params.put("scdCatName", scdCatName);
		Page<SalesmanSalesRankModel> pageInfo = indentService.getSalesmanSalesRank(params, page);
		CommonSummation summation = indentService.getSalesmanSalesRankSummation(params);
		return PageHelper.getInstance().pageData(pageInfo).summarizing(summation);
	}


	@GetMapping("/goods-stock-summarizing/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "beginTime", value = "时间段开始", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "endTime", value = "时间段结束", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "brandName", value = "品牌", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "combine", value = "商品名称/编号/条码", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "specPropId", value = "规格", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "operType", value = "操作类型", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "frtCatName", value = "商品一级分类", paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "scdCatName", value = "商品二级分类", paramType = "body", dataType = "string")
	})
	@ApiOperation("进出库数量汇总")
	public PageHelper<GoodsStockSummarizingModel> goodsStockSummarizing(
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "combine") String combine,
			@RequestParam(required = false, name = "operType") String operType,
			@RequestParam(required = false, name = "beginTime") String beginTime,
			@RequestParam(required = false, name = "brandName") String brandName,
			@RequestParam(required = false, name = "frtCatName") String frtCatName,
			@RequestParam(required = false, name = "scdCatName") String scdCatName,
			@RequestParam(required = false, name = "specPropId") Integer specPropId,
			@PathVariable(value = "current") Integer current, @PathVariable(value = "size") Integer size
	) {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Map<String, Object> params = new HashMap<>();
		params.put("combine", combine);
		params.put("endTime", endTime);
		params.put("operType", operType);
		params.put("brandName", brandName);
		params.put("beginTime", beginTime);
		params.put("frtCatName", frtCatName);
		params.put("scdCatName", scdCatName);
		params.put("specPropId", specPropId);
		long start = System.currentTimeMillis();
		Page<GoodsStockSummarizingModel> pageInfo = stockService.getGoodsStockSummarizing(params, page);
		long end = System.currentTimeMillis();
		log.info("get page records spend {} seconds in ReportsController.", (end - start) / 1000);
		GoodsStockSummationModel summarizing = stockService.getGoodsStockSummation(params);
		log.info("get summation info spend {} seconds in ReportsController.",
				(System.currentTimeMillis() - end) / 1000);
		return PageHelper.getInstance().pageData(pageInfo).summarizing(summarizing);
	}

	@GetMapping("/goods-stockin/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "beginTime", value = "时间段查询条件-开始", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "endTime", value = "时间段查询条件-结束", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "combine", value = "商品名称/编号/条码", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "frtCatName", value = "商品一级分类", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "scdCatName", value = "商品二级分类", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "operType", value = "入库类型", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "brandName", value = "品牌", paramType = "query", dataType = "string")
	})
	@ApiOperation("商品入库报表")
	public PageHelper<GoodsStockinStatisticsModel> goodsStockinStatistics(
			@RequestParam(required = false, name = "combine") String combine,
			@RequestParam(required = false, name = "frtCatName") String frtCatName,
			@RequestParam(required = false, name = "scdCatName") String scdCatName,
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "beginTime") String beginTime,
			@RequestParam(required = false, name = "brandName") String brandName,
			@RequestParam(required = false, name = "operType") String operType,
			@PathVariable(value = "current") Integer current, @PathVariable(value = "size") Integer size
	) {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Map<String, Object> params = new HashMap<>();
		params.put("endTime", endTime);
		params.put("combine", combine);
		params.put("operType", operType);
		params.put("brandName", brandName);
		params.put("beginTime", beginTime);
		params.put("scdCatName", scdCatName);
		params.put("frtCatName", frtCatName);
		Page<GoodsStockinStatisticsModel> pageInfo = stockService.getGoodsStockinStatistics(params, page);
		GoodsStockinSummationModel summarizing = stockService.getGoodsStockinSummation(params);
		return PageHelper.getInstance().pageData(pageInfo).summarizing(summarizing);
	}

	@GetMapping("/indent-statistics/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "brandName", value = "品牌", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "areaGrpId", value = "所属区域", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "salesmanId", value = "业务员id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "endTime", value = "时间段查询条件-结束", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "beginTime", value = "时间段查询条件-开始", paramType = "query", dataType = "string")
	})
	@ApiOperation("订单统计")
	public PageHelper<IndentStatisticsModel> indentStatistics(
			@RequestParam(name = "endTime") String endTime,
			@RequestParam(name = "beginTime") String beginTime,
			@RequestParam(required = false, name = "brandName") String brandName,
			@RequestParam(required = false, name = "areaGrpId") Integer areaGrpId,
			@RequestParam(required = false, name = "salesmanId") Integer salesmanId,
			@PathVariable(value = "size") Integer size, @PathVariable(value = "current") Integer current
	) {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Map<String, Object> params = new HashMap<>();
		params.put("endTime", endTime);
		params.put("beginTime", beginTime);
		params.put("areaGrpId", areaGrpId);
		params.put("brandName", brandName);
		params.put("salesmanId", salesmanId);
		Page<IndentStatisticsModel> pageInfo = indentService.getIndentStatistics(params, page);
		IndentStatisticsSummationModel summation = indentService.getIndentStatisticsSummation(params);
		return PageHelper.getInstance().pageData(pageInfo).summarizing(summation);
	}

	/**
	 * ************************************************** 收款统计报表 **************************************************
	 */
	@GetMapping(value = "/receipt/statistics/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "salesmanId", value = "业务员", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "areaGrpId", value = "所属区域", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "type", value = "收款单/付款单", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "endTime", value = "时间段查询条件-结束", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "beginTime", value = "时间段查询条件-开始", paramType = "query", dataType = "string")
	})
	@ApiOperation("收款/付款统计报表")
	public PageHelper<Map<String, Object>> listReceiptPage(
			HttpServletRequest request,
			@RequestParam(name = "type") String type,
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "beginTime") String beginTime,
			@RequestParam(required = false, name = "areaGrpId") Integer areaGrpId,
			@RequestParam(required = false, name = "salesmanId") Integer salesmanId,
			@PathVariable("current") Integer current, @PathVariable("size") Integer size
	) {
		Map<String, Object> params = new HashMap<>();
		params.put("type", type);
		params.put("endTime", endTime);
		params.put("beginTime", beginTime);
		params.put("areaGrpId", areaGrpId);
		params.put("salesmanId", salesmanId);
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Page<Map<String, List<Map<String, String>>>> receiptStatics = receiptService.getReceiptStatics(params, page);

		return PageHelper.getInstance().pageData(receiptStatics);

	}

	/**
	 * 商品送货统计
	 */
	@GetMapping("/goods-delivery-statics/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "beginTime", value = "时间段开始", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "endTime", value = "时间段结束", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "custName", value = "客户名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "shipmanId", value = "送货人", paramType = "query", dataType = "int"),
	})
	@ApiOperation("商品送货统计")
	public PageHelper<DeliveryStaticsModel> goodsDeliveryStatics(
			HttpServletRequest request,
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "custName") String custName,
			@RequestParam(required = false, name = "beginTime") String beginTime,
			@RequestParam(required = false, name = "shipmanId") Integer shipmanId,
			@PathVariable(value = "current") Integer current, @PathVariable(value = "size") Integer size
	) {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Map<String, Object> params = new HashMap<>();
		params.put("endTime", endTime);
		params.put("custName", custName);
		params.put("beginTime", beginTime);
		params.put("shipmanId", shipmanId);
		CommonSummation summarizing = indentService.getGoodsDeliveryStaticsSummarizing(params);
		Page<DeliveryStaticsModel> pageInfo = indentService.getGoodsDeliveryStatics(page, params);
		return PageHelper.getInstance().pageData(pageInfo).summarizing(summarizing);
	}

	/**
	 * 商品送货明细统计
	 */
	@GetMapping("/goods-delivery-detail-statics/{shipmanId}/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "beginTime", value = "时间段开始", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "endTime", value = "时间段结束", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "custName", value = "客户名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "shipmanId", value = "送货人", paramType = "path", dataType = "int"),
	})
	@ApiOperation("商品送货明细统计")
	public PageHelper<DeliveryDetailsStaticsModel> goodsDeliveryDetailStatics(
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "custName") String custName,
			@RequestParam(required = false, name = "beginTime") String beginTime,
			@PathVariable(value = "shipmanId") Integer shipmanId,
			@PathVariable(value = "current") Integer current,
			@PathVariable(value = "size") Integer size
	) {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Map<String, Object> params = new HashMap<>();
		params.put("endTime", endTime);
		params.put("custName", custName);
		params.put("beginTime", beginTime);
		params.put("shipmanId", shipmanId);
		DeliveryStaticsModel summarizing = indentService.getGoodsDeliveryDetailsStaticsSummarizing(params);
		Page<DeliveryDetailsStaticsModel> pageInfo = indentService.getGoodsDeliveryDetailsStatics(params, page);
		return PageHelper.getInstance().pageData(pageInfo).summarizing(summarizing);
	}
}