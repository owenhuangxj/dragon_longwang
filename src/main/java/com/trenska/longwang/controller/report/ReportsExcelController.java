package com.trenska.longwang.controller.report;

import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.constant.DragonConstant;
import com.trenska.longwang.dao.customer.AreaGrpMapper;
import com.trenska.longwang.entity.PageHelper;
import com.trenska.longwang.entity.financing.DealDetail;
import com.trenska.longwang.entity.financing.DealDetailSummarizing;
import com.trenska.longwang.model.finaning.AccountCheckingModel;
import com.trenska.longwang.model.finaning.DealDetailModel;
import com.trenska.longwang.model.report.*;
import com.trenska.longwang.service.financing.IDealDetailService;
import com.trenska.longwang.service.financing.IReceiptService;
import com.trenska.longwang.service.indent.IIndentService;
import com.trenska.longwang.service.stock.IStockService;
import com.trenska.longwang.util.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * 2019/6/9
 * 创建人:Owen
 */
@Slf4j
@CrossOrigin
@Api(description = "报表excel导出接口")
@RequestMapping("/excel")
@RestController
@SuppressWarnings("all")
public class ReportsExcelController {

	@Autowired
	private IIndentService indentService;

	@Autowired
	private IStockService stockService;

	@Autowired
	private IReceiptService receiptService;

	@Autowired
	private AreaGrpMapper areaGrpMapper;

	@Autowired
	private IDealDetailService dealDetailService;

	@GetMapping("/cust-sales-bill-excel/{current}/{size}")
	@ApiOperation("客户销售账本excel导出")
	public ResponseEntity<byte[]> custSalesBillAmountExcel(
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, value = "beginTime") String beginTime,
			@RequestParam(required = false, name = "brandName") String brandName,
			@RequestParam(required = false, name = "frtCatName") String frtCatName,
			@RequestParam(required = false, name = "scdCatName") String scdCatName,
			@RequestParam(required = false, name = "salesman") String salesman,
			@RequestParam(required = false, name = "salesmanId") Integer salesmanId,
			@RequestParam(required = false, name = "areaGrpId") Integer areaGrpId,
			@RequestParam(required = false, name = "areaGrpName") String areaGrpName,
			@PathVariable(value = "current") Integer current, @PathVariable(value = "size") Integer size
	) throws NoSuchFieldException, IllegalAccessException, IOException {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Map<String, Object> params = new HashMap<>();
		params.put("endTime", endTime);
		params.put("beginTime", beginTime);
		params.put("areaGrpId", areaGrpId);
		params.put("brandName", brandName);
		params.put("frtCatName", frtCatName);
		params.put("scdCatName", scdCatName);
		params.put("salesmanId", salesmanId);
		if (areaGrpId != null) {
			List<Integer> areaGrpIds = areaGrpMapper.selectAllChildrenByAreaGrpId(areaGrpId);
			params.put("areaGrpIds",areaGrpIds);
		}
		Page<CustSalesBillModel> pageInfo = indentService.getCustSales(params, page);
		List<CustSalesBillRecordsModel> contents = new ArrayList<>();
		List<CustSalesBillRecordsModel> records = pageInfo.getRecords().get(0).getRecords();
		contents.addAll(records);

		Map<String, String> title = new LinkedHashMap<>();

		title.put("custNo", "客户编号");
		title.put("custName", "客户名称");
		title.put("odrAmnt", "销售货款");
		title.put("indentTotal", "实收货款");
		title.put("discountTotal", "优惠金额");

		Map<String, String> summarizing = new LinkedHashMap<>();

		CustSalesSummationModel summation = pageInfo.getRecords().get(0).getSummation();

		BigDecimal salesAmntSum = new BigDecimal(summation.getSalesAmntSum());
		BigDecimal salesDiscountSum = new BigDecimal(summation.getSalesDiscountSum());
		BigDecimal receivableAmntSum = salesAmntSum.subtract(salesDiscountSum);

		summarizing.put("销售数量合计", summation.getSalesNumSum());
		summarizing.put("销售货款合计", salesAmntSum.toString());
		summarizing.put("销售应收合计", receivableAmntSum.toString());
		summarizing.put("优惠金额合计", salesDiscountSum.toString());

		Map<String, Object> query = new LinkedHashMap<>();

		if (StringUtils.isNotEmpty(beginTime) && StringUtils.isNotEmpty(endTime)) {
			query.put("时间周期", beginTime.concat("至").concat(endTime));
		}
		if (StringUtils.isNotEmpty(brandName)) {
			query.put("品牌", brandName);
		}
		if (StringUtils.isNotEmpty(areaGrpName)) {
			query.put("区域分组", areaGrpName);
		}
		if (StringUtils.isNotEmpty(frtCatName)) {
			query.put("一级分类", frtCatName);
		}
		if (StringUtils.isNotEmpty(scdCatName)) {
			query.put("二级分类", scdCatName);
		}
		if (StringUtils.isNotEmpty(salesman)) {
			query.put("业务员", salesman);
		}

		HSSFWorkbook wb = ExcelUtil.getHSSFWorkbook("客户销售账本", true, summarizing, query, title, contents, null);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentDispositionFormData("attachment", new String("客户销售账本.xls".getBytes(DragonConstant.srcEncoding), DragonConstant.destEncoding));
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		//将Excel写到ByteArrayOutputStream中
		wb.write(baos);
		// 通过ResponseEntity将Excel输出到客户端
		return new ResponseEntity<byte[]>(baos.toByteArray(), headers, HttpStatus.CREATED);
	}

	@GetMapping("/cust-sales-summarizing-excel/{current}/{size}")
	@ApiOperation("客户销售汇总excel导出")
	public ResponseEntity<byte[]> custSalesSummarizingExcel(
			CustSalesSummarizingSearchModel searchModel,
			@PathVariable(value = "current") Integer current, @PathVariable(value = "size") Integer size
	) throws NoSuchFieldException, IllegalAccessException, IOException {
		searchModel.setEmployeeId(SysUtil.getEmpIdInToken());
		Map<String, Object> query = assembleCustomerSasleSummarizingQuery(searchModel);

		if (searchModel.getAreaGrpId() != null) {
			List<Integer> areaGrpIds = areaGrpMapper.selectAllChildrenByAreaGrpId(searchModel.getAreaGrpId());
			searchModel.setAreaGrpIds(areaGrpIds);
		}

		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Page<CustSalesSummarizingModel> pageInfo = indentService.getCustSalesSummarizing(searchModel, page);
		List<CustSalesSummarizingModel> contents = pageInfo.getRecords();

		Map<String, String> title = new LinkedHashMap<>();

		title.put("custName", "客户名称");
		title.put("goodsName", "商品名称");
		title.put("propNames", "规格");
		title.put("unitName", "单位");
		title.put("salesNum", "销售数量");
		title.put("avgPrice", "平均单价");
		title.put("salesAmnt", "销售金额");
		title.put("amount", "实收金额");

		Map<String, String> summarizing = new LinkedHashMap<>();
		CustSalesSummationModel custSalesSummation = indentService.getCustSalesSummation(searchModel);
		summarizing.put("销售金额合计", custSalesSummation.getSalesAmntSum());
		summarizing.put("实收金额合计", custSalesSummation.getReceivableAmntSum());
		summarizing.put("销售数量合计", custSalesSummation.getSalesNumSum());

		HSSFWorkbook wb = ExcelUtil.getHSSFWorkbook("客户销售汇总", true, summarizing, query, title, contents, null);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentDispositionFormData("attachment", new String("客户销售汇总.xls".getBytes(DragonConstant.srcEncoding), DragonConstant.destEncoding));
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		wb.write(baos);
		return new ResponseEntity<byte[]>(baos.toByteArray(), headers, HttpStatus.CREATED);
	}

	private Map<String, Object> assembleCustomerSasleSummarizingQuery(CustSalesSummarizingSearchModel searchModel) {
		Map<String, Object> query = new LinkedHashMap<>();
		if (searchModel.getBeginTime() != null && searchModel.getEndTime() != null) {
			query.put("时间周期",
					searchModel.getBeginTime().toString().concat("至").concat(searchModel.getEndTime().toString()));
		}
		if (StringUtils.isNotEmpty(searchModel.getBrandName())) {
			query.put("品牌", searchModel.getBrandName());
		}
		if (StringUtils.isNotEmpty(searchModel.getAreaGrpName())) {
			query.put("区域分组", searchModel.getAreaGrpName());
		}
		if (StringUtils.isNotEmpty(searchModel.getCustName())) {
			query.put("客户", searchModel.getCustName());
		}
		if (StringUtils.isNotEmpty(searchModel.getSalesman())) {
			query.put("业务员", searchModel.getSalesman());
		}
		if (StringUtils.isNotEmpty(searchModel.getSalesman())) {
			query.put("送货人", searchModel.getShipman());
		}
		if (StringUtils.isNotEmpty(searchModel.getFrtCatName())) {
			query.put("一级分类", searchModel.getFrtCatName());
		}
		if (StringUtils.isNotEmpty(searchModel.getScdCatName())) {
			query.put("二级分类", searchModel.getScdCatName());
		}
		if (StringUtils.isNotEmpty(searchModel.getRemarks())) {
			query.put("备注", searchModel.getRemarks());
		}
		if (null != searchModel.getGoodsScope()) {
			switch (searchModel.getGoodsScope()) {
				case 1:
					query.put("包含赠品", "是");
					break;
				case 0:
					query.put("不包含赠品", "是");
					break;
				case -1:
					query.put("只看赠品", "是");
					break;
			}
		}
		return query;
	}

	@GetMapping("/cust-sales-statistic-excel/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "catName", value = "商品分类", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "shipmanId", value = "送货人id", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "shipman", value = "送货人", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "custId", value = "客户id", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "custName", value = "客户名称", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "beginTime", value = "时间段-开始", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "endTime", value = "时间段-结束", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "goodsScope", value = "-1：只看赠品 0：只看商品 1：看所有", paramType = "query", dataType = "int")
	})
	@ApiOperation("客户销售统计报表导出")
	public ResponseEntity<byte[]> custSalesStatistic(
			@RequestParam(required = false, name = "shipman") String shipman,
			@RequestParam(required = false, name = "shipmanId") Integer shipmanId,
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "beginTime") String beginTime,
			@RequestParam(required = false, name = "custId") Integer custId,
			@RequestParam(required = false, name = "custName") String custName,
			@RequestParam(required = false, name = "goodsScope") int goodsScope,
			@RequestParam(required = false, name = "frtCatName") String frtCatName,
			@RequestParam(required = false, name = "scdCatName") String scdCatName,
			@PathVariable(value = "current") Integer current, @PathVariable(value = "size") Integer size
	) throws NoSuchFieldException, IllegalAccessException, IOException {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Map<String, Object> params = new HashMap<>();
		params.put("custId", custId);
		params.put("custName", custName);
		params.put("endTime", endTime);
		params.put("beginTime", beginTime);
		params.put("shipmanId", shipmanId);
		params.put("goodsScope", goodsScope);
		params.put("frtCatName", frtCatName);
		params.put("scdCatName", scdCatName);
		CustSalesStatisticsSummationModel custSalesStatisticsSummarizing = indentService.selectCustSalesStatisticsSummation(params);
		Page<CustSalesStatisticsModel> pageInfo = indentService.getCustSalesStatistics(params, page);

		Map<String, String> summarizing = new LinkedHashMap<>();
		summarizing.put("销售数量合计", custSalesStatisticsSummarizing.getSalesNumSum());
		summarizing.put("销售金额合计", custSalesStatisticsSummarizing.getSalesAmntSum());
		summarizing.put("实收金额合计", custSalesStatisticsSummarizing.getIndentTotalSum());

		Map<String, Object> query = new LinkedHashMap<>();

		if (StringUtils.isNotEmpty(beginTime) && StringUtils.isNotEmpty(endTime)) {
			query.put("周期", beginTime.concat("至").concat(endTime));
		}
		if (ObjectUtils.isNotEmpty(frtCatName)) {
			query.put("一级分类", frtCatName);
		}
		if (ObjectUtils.isNotEmpty(scdCatName)) {
			query.put("二级分类", scdCatName);
		}
		if (StringUtils.isNotEmpty(shipman)) {
			query.put("送货员", shipman);
		}
		if (StringUtils.isNotEmpty(custName)) {
			query.put("客户", custName);
		}
		// -1：只看赠品 0：只看商品 1：所有商品

		switch (goodsScope) {
			case -1:
				query.put("只看赠品", "yes");
				break;
			case 0:
				query.put("只看商品", "yes");
				break;
			case 1:
				query.put("所有商品", "yes");
				break;
		}

		Map<String, String> title = new LinkedHashMap<>();

		title.put("goodsNo", "商品编号");
		title.put("goodsName", "商品名称");
		title.put("propNames", "规格");
		title.put("unitName", "单位");
		title.put("salesNum", "销售数量");
		title.put("avgPrice", "平均单价");
		title.put("salesAmnt", "销售金额");
		title.put("indentTotal", "实收金额");

		List<CustSalesStatisticsModel> contents = pageInfo.getRecords();

		HSSFWorkbook wb = ExcelUtil.getHSSFWorkbook("商品汇总", true, summarizing, query, title, contents, null);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentDispositionFormData("attachment", new String("商品汇总.xls".getBytes(DragonConstant.srcEncoding), DragonConstant.destEncoding));
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		wb.write(baos);
		return new ResponseEntity<byte[]>(baos.toByteArray(), headers, HttpStatus.CREATED);
	}

	@GetMapping("/cust-sales-detail-excel/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "brandName", value = "品牌", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "custId", value = "客户", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "custName", value = "客户名称", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "beginTime", value = "时间段-开始", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "endTime", value = "时间段-结束", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "areaGrpId", value = "区域id", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "areaGrpName", value = "区域", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "salesmanId", value = "所属员工id", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "salesman", value = "所属员工", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "frtCatName", value = "一级产品分类", dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "scdCatName", value = "二级产品分类", dataType = "string", paramType = "query")
	})
	@ApiOperation("客户销售明细报表导出 ==> 销售账本的订单汇总")
	public ResponseEntity<byte[]> custSalesDetail(
			HttpServletRequest request,
			@RequestParam(required = false, name = "custId") Integer custId,
			@RequestParam(required = false, name = "custName") String custName,
			@RequestParam(required = false, name = "salesman") String salesman,
			@RequestParam(required = false, name = "salesmanId") Integer salesmanId,
			@RequestParam(required = false, name = "brandName") String brandName,
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, value = "beginTime") String beginTime,
			@RequestParam(required = false, name = "frtCatName") String frtCatName,
			@RequestParam(required = false, name = "scdCatName") String scdCatName,
			@RequestParam(required = false, name = "areaGrpId") Integer areaGrpId,
			@RequestParam(required = false, name = "areaGrpName") String areaGrpName,
			@PathVariable(value = "current") Integer current, @PathVariable(value = "size") Integer size
	) throws NoSuchFieldException, IllegalAccessException, IOException {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Map<String, Object> params = new HashMap<>();

		params.put("custId", custId);
		params.put("endTime", endTime);
		params.put("custName", custName);
		params.put("beginTime", beginTime);
		params.put("brandName", brandName);
		params.put("areaGrpId", areaGrpId);
		params.put("salesmanId", salesmanId);
		params.put("frtCatName", frtCatName);
		params.put("scdCatName", scdCatName);
		Map<String, Object> query = new LinkedHashMap<>();

		if (StringUtils.isNotEmpty(beginTime) && StringUtils.isNotEmpty(endTime)) {
			query.put("周期", beginTime.concat("至").concat(endTime));
		}
		if (StringUtils.isNotEmpty(brandName)) {
			query.put("品牌", brandName);
		}
		if (StringUtils.isNotEmpty(custName)) {
			query.put("客户", custName);
		}
		if (StringUtils.isNotEmpty(areaGrpName)) {
			query.put("区域分组", areaGrpName);
		}
		if (StringUtils.isNotEmpty(salesman)) {
			query.put("业务员", salesman);
		}
		if (StringUtils.isNotEmpty(frtCatName)) {
			query.put("一级分类", frtCatName);
		}
		if (StringUtils.isNotEmpty(scdCatName)) {
			query.put("二级分类", scdCatName);
		}
		Map<String, String> title = new LinkedHashMap<>();

		title.put("indentTime", "下单日期");
		title.put("indentNo", "订单号");
		title.put("goodsNo", "商品编号");
		title.put("goodsName", "商品名称");
		title.put("propNames", "规格");
		title.put("unitName", "单位");
		title.put("salesNum", "销售数量");
		title.put("price", "单价");
		title.put("salesAmnt", "销售金额");
		title.put("indentTotal", "实收金额");
		title.put("madeDates", "生产批次");
		Page<CustSalesDetailModel> pageInfo = indentService.getCustSalesDetail(params, page);

		CustSalesDetailSummarizingModel custSalesDetailSummarizing = indentService.getCustSalesDetailSummarizing(params);

		BigDecimal salesAmntSum = new BigDecimal(custSalesDetailSummarizing.getSalesAmntSum());

		BigDecimal receivableAmntSum = new BigDecimal(custSalesDetailSummarizing.getReceivableAmntSum());

		custSalesDetailSummarizing.setSalesDiscountSum(salesAmntSum.subtract(receivableAmntSum).toString());

		Map<String, String> summarizing = new LinkedHashMap<>();

		summarizing.put("销售数量合计", custSalesDetailSummarizing.getSalesNumSum());
		summarizing.put("销售金额合计", custSalesDetailSummarizing.getSalesAmntSum());
		summarizing.put("实收金额合计", custSalesDetailSummarizing.getReceivableAmntSum());
//		summarizing.put("优惠金额合计", custSalesDetailSummarizing.getSalesDiscountSum());

		List contents = pageInfo.getRecords();

		HSSFWorkbook wb = ExcelUtil.getHSSFWorkbook("订单汇总", true, summarizing, query, title, contents, null);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentDispositionFormData("attachment", new String("订单汇总.xls".getBytes(DragonConstant.srcEncoding), DragonConstant.destEncoding));
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		wb.write(baos);
		return new ResponseEntity<byte[]>(baos.toByteArray(), headers, HttpStatus.CREATED);
	}

	@GetMapping("/cust-sales-rank-excel/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "salesman", value = "业务员", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "salesmanId", value = "业务员id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "areaGrpId", value = "区域分组id", dataType = "query", paramType = "int"),
			@ApiImplicitParam(name = "areaGrpName", value = "区域分组", dataType = "query", paramType = "string"),
			@ApiImplicitParam(name = "endTime", value = "时间段-结束", dataType = "query", paramType = "string"),
			@ApiImplicitParam(name = "beginTime", value = "时间段-开始", dataType = "query", paramType = "string"),
			@ApiImplicitParam(name = "frtCatName", value = "商品一级分类", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "scdCatName", value = "商品二级分类", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "byAmount", value = "true:按销售金额排名;false:按销售数量排名 ", dataType = "boolean", paramType = "query")
	})
	@ApiOperation("客户销售排名excel导出")
	public ResponseEntity<byte[]> custSalesRankExecel(
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, value = "salesman") String salesman,
			@RequestParam(required = false, value = "beginTime") String beginTime,
			@RequestParam(required = false, name = "areaGrpId") Integer areaGrpId,
			@RequestParam(required = false, name = "areaGrpName") String areaGrpName,
			@RequestParam(required = false, name = "frtCatName") String frtCatName,
			@RequestParam(required = false, name = "scdCatName") String scdCatName,
			@RequestParam(required = false, value = "salesmanId") Integer salesmanId,
			@RequestParam(required = false, name = "byAmount", defaultValue = "true") boolean byAmount,
			@PathVariable(value = "current") Integer current, @PathVariable(value = "size") Integer size
	) throws NoSuchFieldException, IllegalAccessException, IOException {
		Map<String, Object> params = new HashMap<>();
		params.put("endTime", endTime);
		params.put("byAmount", byAmount);
		params.put("areaGrpId", areaGrpId);
		params.put("beginTime", beginTime);
		params.put("frtCatName", frtCatName);
		params.put("scdCatName", scdCatName);
		params.put("salesmanId", salesmanId);
		Map<String, Object> query = new LinkedHashMap<>();

		if (StringUtils.isNotEmpty(beginTime) && StringUtils.isNotEmpty(endTime)) {
			query.put("时间周期", beginTime.concat("至").concat(endTime));
		}
		if (StringUtils.isNotEmpty(areaGrpName)) {
			query.put("区域分组", areaGrpName);
		}
		if (StringUtils.isNotEmpty(frtCatName)) {
			query.put("一级分类", frtCatName);
		}
		if (StringUtils.isNotEmpty(scdCatName)) {
			query.put("二级分类", scdCatName);
		}
		if (StringUtils.isNotEmpty(salesman)) {
			query.put("业务员", salesman);
		}
		if (byAmount) {
			query.put("排序方式", "销售金额");
		} else {
			query.put("排序方式", "销售数量");
		}
		Map<String, String> title = new LinkedHashMap<>();

		title.put("rankNum", "排名");
		title.put("custName", "客户名称");
		title.put("salesAmnt", "销售金额");
		title.put("indentTotal", "实收金额");
		title.put("salesNum", "销售数量");
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Page<CustSalesRankModel> pageInfo = indentService.getCustSalesRank(params, page);
		CommonSummation summation = indentService.getCustSalesRankSummation(params);
		Map<String, String> summarizing = new LinkedHashMap<>();
		summarizing.put("销售金额合计", summation.getSalesAmntSum());
		summarizing.put("实收金额合计", summation.getIndentTotalSum());
		summarizing.put("销售数量合计", summation.getSalesNumSum());

		List contents = pageInfo.getRecords();

		HSSFWorkbook wb = ExcelUtil.getHSSFWorkbook("客户销售排名", true, summarizing, query, title, contents, null);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentDispositionFormData("attachment", new String("客户销售排名.xls".getBytes(DragonConstant.srcEncoding), DragonConstant.destEncoding));
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		wb.write(baos);
		return new ResponseEntity<byte[]>(baos.toByteArray(), headers, HttpStatus.CREATED);
	}

	////////////////////////////////////////////////////商品销售统计报表\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

	@GetMapping("/goods-sales-summarizing-excel/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "combine", value = "商品编号/名称/条码", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "specPropId", value = "规格id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "propName", value = "规格", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "catName", value = "商品分类", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "beginTime", value = "时间段开始", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "endTime", value = "时间段结束", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "areaGrpId", value = "区域id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "areaGrpName", value = "区域", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "custId", value = "客户id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "custName", value = "客户名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "brandName", value = "品牌", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "shipmanId", value = "送货人id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "shipman", value = "送货人", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "salesmanId", value = "所属员工", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "empId", value = "制单人id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "empName", value = "制单人", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "discount", value = "扣点", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "goodsScope", value = "-1：只看赠品 0：只看商品 1：看所有", paramType = "query", dataType = "int")
	})
	@ApiOperation("商品销售汇总报表导出")
	public ResponseEntity<byte[]> goodsSalesSummarizing(
			HttpServletRequest request,
			@RequestParam(required = false, name = "empId") Integer empId,
			@RequestParam(required = false, name = "empName") String empName,
			@RequestParam(required = false, name = "combine") String combine,
			@RequestParam(required = false, name = "custId") Integer custId,
			@RequestParam(required = false, name = "custName") String custName,
			@RequestParam(required = false, name = "discount") String discount,
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "beginTime") String beginTime,
			@RequestParam(required = false, name = "brandName") String brandName,
			@RequestParam(required = false, name = "shipman") String shipman,
			@RequestParam(required = false, name = "shipmanId") Integer shipmanId,
			@RequestParam(required = false, name = "frtCatName") String frtCatName,
			@RequestParam(required = false, name = "scdCatName") String scdCatName,
			@RequestParam(required = false, name = "PropName") String propName,
			@RequestParam(required = false, name = "specPropId") Integer specPropId,
			@RequestParam(required = false, name = "salesman") String salesman,
			@RequestParam(required = false, name = "salesmanId") Integer salesmanId,
			@RequestParam(required = false, name = "goodsScope") Integer goodsScope,
			@RequestParam(required = false, name = "areaGrpId") Integer areaGrpId,
			@RequestParam(required = false, name = "areaGrpName") String areaGrpName,
			@PathVariable(value = "current") Integer current, @PathVariable(value = "size") Integer size
	) throws Exception {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Map<String, Object> params = new HashMap<>();
		params.put("empId", empId);
		params.put("custId", custId);
		params.put("combine", combine);
		params.put("endTime", endTime);
		params.put("custName", custName);
		params.put("discount", discount);
		params.put("beginTime", beginTime);
		params.put("areaGrpId", areaGrpId);
		params.put("brandName", brandName);
		params.put("shipmanId", shipmanId);
		params.put("specPropId", specPropId);
		params.put("salesmanId", salesmanId);
		params.put("goodsScope", goodsScope);
		params.put("frtCatName", frtCatName);
		params.put("scdCatName", scdCatName);
		if (areaGrpId != null) {
			List<Integer> areaGrpIds = areaGrpMapper.selectAllChildrenByAreaGrpId(areaGrpId);
			params.put("areaGrpIds",areaGrpIds);
		}
		Page<GoodsSalesSummarizingModel> pageInfo = indentService.getGoodsSalesSummarizing(params, page);
		///////////////////////////////////////////// 处理合计部分 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		CommonSummation goodsSalesSummation = indentService.getGoodsSalesSummation(params);
		Map<String, String> summarizing = new LinkedHashMap<>();
		summarizing.put("销售金额合计", goodsSalesSummation.getSalesAmntSum());
		summarizing.put("实收金额合计", goodsSalesSummation.getIndentTotalSum());
		summarizing.put("销售数量合计", goodsSalesSummation.getSalesNumSum());
		/////////////////////////////////////////// 处理合计部分结束 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

		///////////////////////////////////////////// 处理查询条件部分 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		Map<String, Object> query = new LinkedHashMap<>();
		if (StringUtils.isNotEmpty(combine)) {
			query.put("商品编号/名称/条码", combine);
		}
		if (StringUtils.isNotEmpty(propName)) {
			query.put("规格", propName);
		}
		if (StringUtils.isNotEmpty(frtCatName)) {
			query.put("一级分类", frtCatName);
		}
		if (StringUtils.isNotEmpty(scdCatName)) {
			query.put("二级分类", scdCatName);
		}
		if (StringUtils.isNotEmpty(beginTime) && StringUtils.isNotEmpty(endTime)) {
			query.put("时间周期", beginTime.concat("至").concat(endTime));
		}
		if (StringUtils.isNotEmpty(areaGrpName)) {
			query.put("区域", areaGrpName);
		}
		if (StringUtils.isNotEmpty(custName)) {
			query.put("客户", custName);
		}
		if (StringUtils.isNotEmpty(brandName)) {
			query.put("品牌", brandName);
		}
		if (StringUtils.isNotEmpty(shipman)) {
			query.put("送货人", shipman);
		}
		if (StringUtils.isNotEmpty(salesman)) {
			query.put("业务员", salesman);
		}
		if (StringUtils.isNotEmpty(salesman)) {
			query.put("制单人", empName);
		}
		if (goodsScope != null) {
			if (goodsScope == 1) {
				query.put("包含赠品", "是");
			} else if (goodsScope == -1) {
				query.put("只看赠品", "是");
			} else if (goodsScope == 0) {
				query.put("不包含赠品", "是");
			}
		}
		if (StringUtil.isNumeric(discount, true)) {
			query.put("扣点", discount);
		}
		/////////////////////////////////////////// 处理查询条件部分结束 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

		///////////////////////////////////////////// 处理列标题头部分 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		Map<String, String> title = new LinkedHashMap<>();
		title.put("goodsNo", "商品编号");
		title.put("goodsName", "商品名称");
		title.put("propNames", "规格");
		title.put("unit", "单位");
		title.put("num", "销售数量");
		title.put("price", "销售单价");
		title.put("salesAmnt", "销售金额");
		title.put("discount", "扣点");
		title.put("indentTotal", "实收金额");
		/////////////////////////////////////////// 处理列标题头部分结束 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

		HSSFWorkbook wb = ExcelUtil.getHSSFWorkbook("商品销售汇总", true, summarizing, query, title, pageInfo.getRecords(), null);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentDispositionFormData("attachment", new String("商品销售汇总.xls".getBytes(DragonConstant.srcEncoding), DragonConstant.destEncoding));
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		wb.write(baos);
		return new ResponseEntity<byte[]>(baos.toByteArray(), headers, HttpStatus.CREATED);
	}

	/**
	 * 此接口没有beginTime、endTime、isGift、discount查询条件
	 *
	 * @param current
	 * @param size
	 * @param goodsId
	 * @param beginTime
	 * @param endTime
	 * @param discount
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/goods-sales-details-excel/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "goodsId", value = "商品id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "goodsName", value = "商品名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "combine", value = "商品编号/名称/条码", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "PropName", value = "规格名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "specPropId", value = "规格id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "catName", value = "商品分类", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "beginTime", value = "时间段开始", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "endTime", value = "时间段结束", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "areaGrpId", value = "区域id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "areaGrpName", value = "区域名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "custId", value = "客户id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "custName", value = "客户名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "brandName", value = "品牌", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "shipman", value = "送货人", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "shipmanId", value = "送货人id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "salesman", value = "所属员工", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "salesmanId", value = "所属员工id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "empId", value = "制单人id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "empName", value = "制单人名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "goodsScope", value = "-1：只看赠品 0：只看商品 1：看所有", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "discount", value = "扣点", paramType = "query", dataType = "string")
	})
	@ApiOperation("商品销售明细")
	public ResponseEntity<byte[]> singleGoodsSalesDetail(
			@RequestParam(name = "goodsId") Integer goodsId,
			@RequestParam(required = false, name = "empId") Integer empId,
			@RequestParam(required = false, name = "custId") Integer custId,
			@RequestParam(required = false, name = "custName") String custName,
			@RequestParam(required = false, name = "combine") String combine,
			@RequestParam(required = false, name = "discount") String discount,
			@RequestParam(required = false, name = "brandName") String brandName,
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "beginTime") String beginTime,
			@RequestParam(required = false, name = "areaGrpId") Integer areaGrpId,
			@RequestParam(required = false, name = "areaGrpName") Integer areaGrpName,
			@RequestParam(required = false, name = "shipman") String shipman,
			@RequestParam(required = false, name = "shipmanId") Integer shipmanId,
			@RequestParam(required = false, name = "frtCatName") String frtCatName,
			@RequestParam(required = false, name = "scdCatName") String scdCatName,
			@RequestParam(required = false, name = "PropName") String PropName,
			@RequestParam(required = false, name = "specPropId") Integer specPropId,
			@RequestParam(required = false, name = "salesman") String salesman,
			@RequestParam(required = false, name = "salesmanId") Integer salesmanId,
			@RequestParam(required = false, name = "goodsScope") Integer goodsScope,
			@PathVariable(value = "current") Integer current, @PathVariable(value = "size") Integer size
	) throws Exception {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Map<String, Object> params = new HashMap<>();
		params.put("empId", empId);
		params.put("custId", custId);
		params.put("combine", combine);
		params.put("endTime", endTime);
		params.put("goodsId", goodsId);
		params.put("custName", custName);
		params.put("discount", discount);
		params.put("beginTime", beginTime);
		params.put("brandName", brandName);
		params.put("shipmanId", shipmanId);
		params.put("areaGrpId", areaGrpId);
		params.put("specPropId", specPropId);
		params.put("goodsScope", goodsScope);
		params.put("salesmanId", salesmanId);
		params.put("frtCatName", frtCatName);
		params.put("scdCatName", scdCatName);
		Page<SingleGoodsSalesDetailModel> pageInfo = indentService.getSingleGoodsSalesDetail(params, page);

		SingleGoodsSalesDetailModel singleGoodsSalesDetail = pageInfo.getRecords().get(0);

		Map<String, String> summarizing = new LinkedHashMap<>();

		summarizing.put("商品编号", singleGoodsSalesDetail.getGoodsNo());
		summarizing.put("商品名称", singleGoodsSalesDetail.getGoodsName());
		String propNames = "";
		for (String propName : singleGoodsSalesDetail.getPropNames()) {
			propNames.concat(propName).concat(";");
		}
		if (StringUtils.isNotEmpty(propNames)) {
			propNames = propNames.substring(0, propNames.lastIndexOf(";"));
			summarizing.put("规格", propNames);
		}
		summarizing.put("单位", singleGoodsSalesDetail.getUnit());
		summarizing.put("销售数量合计", singleGoodsSalesDetail.getSalesNumSum());
		summarizing.put("销售金额合计", singleGoodsSalesDetail.getSalesAmntSum());

		////////////////////////////////////////////// 处理标题 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		Map<String, String> title = new LinkedHashMap<>();
		title.put("indentNo", "订货单号");
		title.put("indentTime", "销售日期");
		title.put("custName", "客户名称");
//		title.put("goodsNo", "商品编号");
//		title.put("goodsName", "商品名称");
		title.put("num", "销售数量");
		title.put("price", "销售单价");
		title.put("salesAmnt", "销售金额");
		title.put("discount", "扣点");
		title.put("indentTotal", "扣点金额");
		title.put("madeDates", "出库批次\r\n生产批次/数量");
//		title.put("remarks", " 备注 ");
		////////////////////////////////////////////// 处理标题结束 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

		//////////////////////////////////////////////// 处理内容 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		List<SingleGoodsSalesIndentDetailExcelModel> contents = new ArrayList<>();

		SingleGoodsSalesDetailModel singleGoodsSalesDetailModel = pageInfo.getRecords().get(0);
		pageInfo.getRecords().get(0).getIndentDetails().forEach(indentDetail -> {
			SingleGoodsSalesIndentDetailExcelModel singleGoodsSalesIndentDetail = new SingleGoodsSalesIndentDetailExcelModel();

			ObjectCopier.copyProperties(indentDetail, singleGoodsSalesIndentDetail);
			/***********************************处理商品编号和商品名称***********************************/
			singleGoodsSalesIndentDetail.setGoodsNo(singleGoodsSalesDetailModel.getGoodsNo());
			singleGoodsSalesIndentDetail.setGoodsName(singleGoodsSalesDetailModel.getGoodsName());

			/***********************************处理出库批次************************************/
			Set<SingleGoodsSalesIndentStockoutDetailModel> madeDates = indentDetail.getMadeDates();
			String madeDatesStr = "";
			if (!madeDates.isEmpty()) {
				for (SingleGoodsSalesIndentStockoutDetailModel madeDate : madeDates) {
					madeDatesStr = madeDatesStr.concat(madeDate.getMadeDate()).concat("/").concat(madeDate.getStockoutNum()).concat("\n\r");
				}
				if (StringUtils.isNotEmpty(madeDatesStr)) {
					madeDatesStr = madeDatesStr.substring(0, madeDatesStr.lastIndexOf("\n\r"));
				}
			}
			singleGoodsSalesIndentDetail.setMadeDates(madeDatesStr);

			contents.add(singleGoodsSalesIndentDetail);
		});

		////////////////////////////////////////////// 处理内容结束 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		HSSFWorkbook wb = ExcelUtil.getHSSFWorkbook("商品销售明细", false, summarizing, null, title, contents, null);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentDispositionFormData("attachment", "商品销售明细.xls");
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		// 将Excel写到流中
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		wb.write(baos);
		return new ResponseEntity(baos.toByteArray(), headers, HttpStatus.CREATED);
	}

	@GetMapping("/goods-sales-rank-excel/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "areaGrpId", value = "区域id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "brandName", value = "品牌", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "areaGrpName", value = "区域", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "salesman", value = "业务员", paramType = "query", dataType = "string"),
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
	@ApiOperation("商品销售排名excel导出")
	public ResponseEntity<byte[]> goodsSalesRankExcel(
			HttpServletRequest request,
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "salesman") String salesman,
			@RequestParam(required = false, name = "custName") String custName,
			@RequestParam(required = false, name = "brandName") String brandName,
			@RequestParam(required = false, name = "beginTime") String beginTime,
			@RequestParam(required = false, name = "areaGrpId") Integer areaGrpId,
			@RequestParam(required = false, name = "areaGrpName") String areaGrpName,
			@RequestParam(required = false, name = "frtCatName") String frtCatName,
			@RequestParam(required = false, name = "scdCatName") String scdCatName,
			@RequestParam(required = false, name = "salesmanId") Integer salesmanId,
			@RequestParam(required = false, name = "byAmount", defaultValue = "true") boolean byAmount,
			@PathVariable(value = "current") Integer current, @PathVariable(value = "size") Integer size,
			@RequestParam(required = false, name = "statisticsWay", defaultValue = "1") Integer statisticsWay
	) throws NoSuchFieldException, IllegalAccessException, IOException {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Map<String, Object> params = new HashMap<>();
		params.put("endTime", endTime);
		params.put("byAmount", byAmount);
		params.put("custName", custName);
		params.put("beginTime", beginTime);
		params.put("areaGrpId", areaGrpId);
		params.put("brandName", brandName);
		params.put("salesmanId", salesmanId);
		params.put("frtCatName", frtCatName);
		params.put("scdCatName", scdCatName);
		params.put("statisticsWay", statisticsWay);
		if (areaGrpId != null) {
			List<Integer> areaGrpIds = areaGrpMapper.selectAllChildrenByAreaGrpId(areaGrpId);
			params.put("areaGrpIds",areaGrpIds);
		}
		Map<String, Object> query = new LinkedHashMap<>();

		if (StringUtils.isNotEmpty(beginTime) && StringUtils.isNotEmpty(endTime)) {
			query.put("时间周期", beginTime.concat("至").concat(endTime));
		}
		if (StringUtils.isNotEmpty(areaGrpName)) {
			query.put("区域分组", areaGrpName);
		}
		if (StringUtils.isNotEmpty(custName)) {
			query.put("客户名称", custName);
		}
		if (StringUtils.isNotEmpty(salesman)) {
			query.put("业务员", salesman);
		}
		if (StringUtils.isNotEmpty(frtCatName)) {
			query.put("一级分类", frtCatName);
		}
		if (StringUtils.isNotEmpty(scdCatName)) {
			query.put("二级分类", scdCatName);
		}
		if (byAmount) {
			query.put("排序方式", "销售金额");
		} else {
			query.put("排序方式", "销售数量");
		}
		if (statisticsWay == 1) {
			query.put("统计方式", "按商品");
		} else if (statisticsWay == 2) {
			query.put("统计方式", "按品牌");
		} else if (statisticsWay == 3) {
			query.put("统计方式", "按分类");
		}
		Map<String, String> title = new LinkedHashMap<>();

		title.put("rankNum", "排名");
		title.put("goodsNo", "商品编号");
		title.put("goodsName", "商品名称");
		title.put("brandName", "品牌");
		title.put("catName", "分类");
		title.put("salesAmnt", "销售金额");
		title.put("indentTotal", "实收金额");
		title.put("salesNum", "销售数量");
		Page<GoodsSalesRankModel> pageInfo = indentService.getGoodsSalesRank(params, page);
		CommonSummation summation = indentService.getGoodsSalesRankSummation(params);
		Map<String, String> summarizing = new LinkedHashMap<>();
		summarizing.put("销售金额合计", summation.getSalesAmntSum());
		summarizing.put("实收金额合计", summation.getIndentTotalSum());
		summarizing.put("销售数量合计", summation.getSalesNumSum());
		List contents = pageInfo.getRecords();
		HSSFWorkbook wb = ExcelUtil.getHSSFWorkbook("商品销售排名", true, summarizing, query, title, contents, null);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentDispositionFormData("attachment", new String("商品销售排名.xls".getBytes(DragonConstant.srcEncoding), DragonConstant.destEncoding));
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		wb.write(baos);
		return new ResponseEntity<byte[]>(baos.toByteArray(), headers, HttpStatus.CREATED);

	}

	@GetMapping("/goods-stock-summarizing-excel/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "beginTime", value = "时间段开始", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "endTime", value = "时间段结束", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "brandName", value = "品牌", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "combine", value = "商品名称/编号/条码", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "specPropId", value = "规格id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "propName", value = "规格", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "catName", value = "商品分类", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "operType", value = "操作类型", paramType = "query", dataType = "string"),
	})
	@ApiOperation("进出库数量汇总报表导出")
	public ResponseEntity<byte[]> goodsStockSummarizing(
			HttpServletRequest request,
			@RequestParam(required = false, name = "combine") String combine,
			@RequestParam(required = false, name = "operType") String operType,
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "beginTime") String beginTime,
			@RequestParam(required = false, name = "brandName") String brandName,
			@RequestParam(required = false, name = "frtCatName") String frtCatName,
			@RequestParam(required = false, name = "scdCatName") String scdCatName,
			@RequestParam(required = false, name = "propName") String propName,
			@RequestParam(required = false, name = "specPropId") Integer specPropId,
			@PathVariable(value = "current") Integer current, @PathVariable(value = "size") Integer size
	) throws Exception {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Map<String, Object> params = new HashMap<>();
		params.put("endTime", endTime);
		params.put("combine", combine);
		params.put("operType", operType);
		params.put("beginTime", beginTime);
		params.put("brandName", brandName);
		params.put("specPropId", specPropId);
		params.put("frtCatName", frtCatName);
		params.put("scdCatName", scdCatName);
		Page<GoodsStockSummarizingModel> pageInfo = stockService.getGoodsStockSummarizing(params, page);

		GoodsStockSummationModel goodsStockSummartion = stockService.getGoodsStockSummation(params);

		////////////////////////////////////////////// 处理列标题 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		Map<String, String> title = new LinkedHashMap<>();
		title.put("goodsNo", "商品编号");
		title.put("goodsName", "商品名称");
		title.put("propNames", "规格");
		title.put("unitName", "单位");
//		title.put("brandName", "品牌");
		title.put("initStock", "期初库存");
		title.put("makeIn", "生产入库");
		title.put("otherIn", "其他入库");
//		title.put("returnsOut", "退货出库");
		title.put("returnsIn", "退货入库");
		title.put("overflow", "报溢入库");
		title.put("salesOut", "销售出库");
		title.put("otherOut", "其他出库");
		title.put("breakage", "报损出库");
		title.put("overStock", "期末库存");
		////////////////////////////////////////////// 处理查询条件 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		Map<String, Object> query = new LinkedHashMap<>();
		if (StringUtils.isNotEmpty(combine)) {
			query.put("商品名称/编号/条码", combine);
		}
		if (StringUtils.isNotEmpty(beginTime) && StringUtils.isNotEmpty(endTime)) {
			query.put("周期", beginTime.concat("至").concat(endTime));
		}
		if (StringUtils.isNotEmpty(brandName)) {
			query.put("品牌", brandName);
		}
		if (StringUtils.isNotEmpty(frtCatName)) {
			query.put("一级分类", frtCatName);
		}
		if (StringUtils.isNotEmpty(scdCatName)) {
			query.put("二级分类", scdCatName);
		}
		if (StringUtils.isNotEmpty(propName)) {
			query.put("规格", propName);
		}
		if (StringUtils.isNotEmpty(operType)) {
			query.put("操作类型", operType);
		}
		////////////////////////////////////////////// 处理统计数据 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		Map<String, String> summarizing = new LinkedHashMap<>();
		summarizing.put("期初数量汇总", goodsStockSummartion.getInitStockSum());
		summarizing.put("生产入库汇总", goodsStockSummartion.getMakeInSum());
		summarizing.put("其他入库汇总", goodsStockSummartion.getOtherInSum());
		summarizing.put("退货入库汇总", goodsStockSummartion.getReturnsInSum());
		summarizing.put("报溢入库汇总", goodsStockSummartion.getOverflowSum());
		summarizing.put("销售出库汇总", goodsStockSummartion.getSalesOutSum().concat("\r\n"));
		summarizing.put("其他出库汇总", goodsStockSummartion.getOtherOutSum());
		summarizing.put("报损出库汇总", goodsStockSummartion.getBreakageSum());
//		summarizing.put("退货出库汇总", goodsStockSummartion.getReturnsOutSum());
		summarizing.put("期末库存汇总", goodsStockSummartion.getOverStockSum());
		////////////////////////////////////////////// 处理统计数据结束 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		List<GoodsStockSummarizingModel> records = pageInfo.getRecords();
		HSSFWorkbook wb = ExcelUtil.getHSSFWorkbook("进出库数量汇总", true, summarizing, query, title, records, null);
		wb.write(baos);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentDispositionFormData("attachment", new String("进出库数量汇总.xls".getBytes(DragonConstant.srcEncoding), DragonConstant.destEncoding));
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		return new ResponseEntity<byte[]>(baos.toByteArray(), headers, HttpStatus.CREATED);
	}

	@GetMapping("/goods-stockin-excel/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "beginTime", value = "时间段查询条件-开始", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "endTime", value = "时间段查询条件-结束", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "combine", value = "商品名称/编号/条码", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "frtCatName", value = "商品一级分类", paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "scdCatName", value = "商品二级分类", paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "operType", value = "入库类型", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "brandName", value = "品牌", paramType = "query", dataType = "string")
	})
	@ApiOperation("商品入库报表导出")
	public ResponseEntity<byte[]> goodsStockinStatistics(
			@RequestParam(required = false, name = "combine") String combine,
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "operType") String operType,
			@RequestParam(required = false, name = "beginTime") String beginTime,
			@RequestParam(required = false, name = "brandName") String brandName,
			@RequestParam(required = false, name = "frtCatName") String frtCatName,
			@RequestParam(required = false, name = "scdCatName") String scdCatName,
			@PathVariable(value = "current") Integer current, @PathVariable(value = "size") Integer size
	) throws Exception {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Map<String, Object> params = new HashMap<>();
		params.put("combine", combine);
		params.put("endTime", endTime);
		params.put("operType", operType);
		params.put("beginTime", beginTime);
		params.put("brandName", brandName);
		params.put("frtCatName", frtCatName);
		params.put("scdCatName", scdCatName);
		Page<GoodsStockinStatisticsModel> pageInfo = stockService.getGoodsStockinStatistics(params, page);

		GoodsStockinSummationModel summation = stockService.getGoodsStockinSummation(params);

		////////////////////////////////////////////// 处理列标题 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		Map<String, String> title = new LinkedHashMap<>();
		title.put("goodsNo", "商品编号");
		title.put("goodsName", "商品名称");
		title.put("propNames", "规格");
		title.put("unitName", "单位");
		title.put("stockinNum", "数量");
		title.put("avgPrice", "入库均价");
		title.put("stockinAmnt", "入库金额");
		////////////////////////////////////////////// 处理列标题结束 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

		////////////////////////////////////////////// 处理查询条件 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		Map<String, Object> query = new LinkedHashMap<>();
		if (StringUtils.isNotEmpty(combine)) {
			query.put("商品名称/编号/条码", combine);
		}
		if (StringUtils.isNotEmpty(beginTime) && StringUtils.isNotEmpty(endTime)) {
			query.put("周期", beginTime.concat("至").concat(endTime));
		}
		if (StringUtils.isNotEmpty(brandName)) {
			query.put("品牌", brandName);
		}
		if (StringUtils.isNotEmpty(frtCatName)) {
			query.put("一级分类", frtCatName);
		}
		if (StringUtils.isNotEmpty(scdCatName)) {
			query.put("二级分类", scdCatName);
		}
		if (StringUtils.isNotEmpty(operType)) {
			query.put("入库类型", operType);
		}


		////////////////////////////////////////////// 处理查询条件结束 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

		////////////////////////////////////////////// 处理统计数据 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		Map<String, String> summarizing = new LinkedHashMap<>();
		summarizing.put("入库数量合计", summation.getStockinNumSum());
		summarizing.put("入库金额合计", summation.getStockinAmntSum());
		////////////////////////////////////////////// 处理统计数据结束 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

		List<GoodsStockinStatisticsModel> records = pageInfo.getRecords();
		HSSFWorkbook wb = ExcelUtil.getHSSFWorkbook("商品入库报表", true, summarizing, query, title, records, null);
//		HSSFWorkbook wb = ExcelUtil.getHSSFWorkbook("商品入库报表",false, null, query, title, pageInfo.getRecords(), null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		wb.write(baos);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentDispositionFormData("attachment", new String("商品入库报表.xls".getBytes(DragonConstant.srcEncoding), DragonConstant.destEncoding));
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		return new ResponseEntity<byte[]>(baos.toByteArray(), headers, HttpStatus.CREATED);
	}

	///////////////////////////////////////////////////// 订单统计 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	// IndentStatisticsModel
	@GetMapping("/indent-statistics-excel/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "beginTime", value = "时间段查询条件-开始", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "endTime", value = "时间段查询条件-结束", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "areaGrpId", value = "所属区域", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "areaGrpName", value = "所属区域名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "salesmanId", value = "业务员id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "salesman", value = "业务员", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "goodsName", value = "商品名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "goodsNo", value = "商品编号", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "brandName", value = "品牌", paramType = "query", dataType = "string")
	})
	@ApiOperation("订单统计报表导出")
	public ResponseEntity<byte[]> indentStatistics(
			@PathVariable(value = "size") Integer size,
			@RequestParam(name = "endTime") String endTime,
			@PathVariable(value = "current") Integer current,
			@RequestParam(name = "beginTime") String beginTime,
			@RequestParam(required = false, name = "goodsNo") String goodsNo,
			@RequestParam(required = false, name = "goodsName") String goodsName,
			@RequestParam(required = false, name = "brandName") String brandName,
			@RequestParam(required = false, name = "areaGrpId") Integer areaGrpId,
			@RequestParam(required = false, name = "areaGrpName") String areaGrpName,
			@RequestParam(required = false, name = "salesman") String salesman,
			@RequestParam(required = false, name = "salesmanId") Integer salesmanId
	) throws Exception {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Map<String, Object> params = new HashMap<>();
		params.put("endTime", endTime);
		params.put("goodsNo", goodsNo);
		params.put("beginTime", beginTime);
		params.put("goodsName", goodsName);
		params.put("brandName", brandName);
		params.put("areaGrpId", areaGrpId);
		params.put("salesmanId", salesmanId);
		Page<IndentStatisticsModel> pageInfo = indentService.getIndentStatistics(params, page);
		IndentStatisticsSummationModel summation = indentService.getIndentStatisticsSummation(params);

		////////////////////////////////////////////// 处理列标题 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		Map<String, String> title = new LinkedHashMap<>();
		title.put("statisticsTime", "时间");
		title.put("orderNum", "订货单数");
		title.put("returnNum", "退货单数");
		title.put("orderCustNum", "订货客户数");
		title.put("returnCustNum", "退货客户数");
		title.put("orderAmnt", "订货金额");
		title.put("returnAmnt", "退货金额");
		title.put("total", "金额合计(订货-退货)");
		////////////////////////////////////////////// 处理列标题结束 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

		////////////////////////////////////////////// 处理查询条件 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		Map<String, Object> query = new LinkedHashMap<>();
		if (StringUtils.isNotEmpty(goodsNo)) {
			query.put("商品编号", goodsNo);
		}
		if (StringUtils.isNotEmpty(goodsName)) {
			query.put("商品名称", goodsName);
		}
		if (StringUtils.isNotEmpty(beginTime) && StringUtils.isNotEmpty(endTime)) {
			query.put("周期", beginTime.concat("至").concat(endTime));
		}
		if (StringUtils.isNotEmpty(brandName)) {
			query.put("品牌", brandName);
		}
		if (StringUtils.isNotEmpty(areaGrpName)) {
			query.put("区域分组", areaGrpName);
		}
		if (StringUtils.isNotEmpty(salesman)) {
			query.put("归属员工", salesman);
		}
		////////////////////////////////////////////// 处理查询条件结束 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

		////////////////////////////////////////////// 处理统计数据 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		Map<String, String> summarizing = new LinkedHashMap<>();
		summarizing.put("订货单数合计", summation.getOrderNumSum());
		summarizing.put("退货单数合计", summation.getReturnNumSum());
		summarizing.put("订货金额合计", summation.getOrderAmntSum());
		summarizing.put("退货金额合计", summation.getReturnAmntSum());
		summarizing.put("订货客户数合计", summation.getOrderCustNumSum());
		summarizing.put("退货客户数合计", summation.getReturnCustNumSum());
		summarizing.put("金额合计", summation.getTotal());
		summarizing.put("待收金额", summation.getOwedAmnt());
		////////////////////////////////////////////// 处理统计数据结束 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

		HSSFWorkbook wb = ExcelUtil.getHSSFWorkbook("订单统计", true, summarizing, query, title, pageInfo.getRecords(), null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		wb.write(baos);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentDispositionFormData("attachment", new String("订单统计.xls".getBytes(DragonConstant.srcEncoding), DragonConstant.destEncoding));
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		return new ResponseEntity<byte[]>(baos.toByteArray(), headers, HttpStatus.CREATED);
	}
	//////////////////////////////////////////////////// 订单统计结束 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

	@GetMapping(value = "/account-checking-excel/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "empId", value = "所属员工id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "empName", value = "业务员", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "areaGrpId", value = "所属区域id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "areaGrpName", value = "所属区域", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "priceGrpId", value = "所属分组id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "priceGrpName", value = "所属分组", paramType = "query", dataType = "string"),

	})
	@ApiOperation("客户对帐报表导出")
	public ResponseEntity<byte[]> listAccountChecking(
			@RequestParam(required = false, name = "empId") Integer empId,
			@RequestParam(required = false, name = "empName") String empName,
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "custName") String custName,
			@RequestParam(required = false, name = "beginTime") String beginTime,
			@RequestParam(required = false, name = "areaGrpId") Integer areaGrpId,
			@RequestParam(required = false, name = "priceGrpId") String priceGrpId,
			@RequestParam(required = false, name = "areaGrpName") String areaGrpName,
			@RequestParam(required = false, name = "priceGrpName") String priceGrpName,
			@PathVariable("current") Integer current, @PathVariable("size") Integer size
	) throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("empId", empId);
		params.put("endTime", endTime);
		params.put("custName", custName);
		params.put("beginTime", beginTime);
		params.put("areaGrpId", areaGrpId);
		params.put("priceGrpId", priceGrpId);

		// 处理数据权限和客户区域分组
		params = SysUtil.dealDataPermAndAreaGrp(params, areaGrpMapper);
		Page<AccountCheckingModel> pageInfo = receiptService.getAccountChecking(params, PageUtils.getPageParam(new PageHelper(current, size)));
		AccountCheckingSummationModel accountCheckingSummation = receiptService.getAccountCheckingSummation(params);

		////////////////////////////////////////////// 处理列标题 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		Map<String, String> title = new LinkedHashMap<>();
		title.put("custNo", "客户编号");
		title.put("custName", "客户名称");
		title.put("initDebt", "期初欠款");
		title.put("salesAmount", "销售应收");
		title.put("receivedAmount", "已收");
		title.put("payedAmount", "已付");
		title.put("debtAmount", "待收欠款");
		////////////////////////////////////////////// 处理列标题结束 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

		////////////////////////////////////////////// 处理查询条件 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		Map<String, Object> query = new LinkedHashMap<>();
		if (StringUtils.isNotEmpty(empName)) {
			query.put("业务员", empName);
		}
		if (StringUtils.isNotEmpty(areaGrpName)) {
			query.put("区域", areaGrpName);
		}
		if (StringUtils.isNotEmpty(priceGrpName)) {
			query.put("分组", priceGrpName);
		}
		if (StringUtils.isNotEmpty(beginTime) && StringUtils.isNotEmpty(endTime)) {
			query.put("时间周期", beginTime.concat("至").concat(endTime));
		}

		////////////////////////////////////////////// 处理查询条件结束 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

		////////////////////////////////////////////// 处理统计数据 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		Map<String, String> summarizing = new LinkedHashMap<>();
		String initDebtTotal = accountCheckingSummation.getInitDebtTotal();
		String salesAmountTotal = accountCheckingSummation.getSalesAmountTotal();
		String receivedAmountTotal = accountCheckingSummation.getReceivedAmountTotal();
		String payedAmountTotal = accountCheckingSummation.getPayedAmountTotal();
		String debtAmountTotal = accountCheckingSummation.getDebtAmountTotal();
		summarizing.put("期初欠款合计", initDebtTotal);
		summarizing.put("增加欠款合计", salesAmountTotal);
		summarizing.put("减少欠款(收款)合计", receivedAmountTotal);
		summarizing.put("减少欠款(付款)合计", payedAmountTotal);
		summarizing.put("待收金额合计", debtAmountTotal);
		////////////////////////////////////////////// 处理待收金额合计 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		/*Service中已经运算*/
//		BigDecimal debtAmountTotal = new BigDecimal(0);
//		debtAmountTotal = debtAmountTotal
//				.add(new BigDecimal(initDebtTotal))
//				.add(new BigDecimal(salesAmountTotal))
//				.subtract(new BigDecimal(receivedAmountTotal))
//				.subtract(new BigDecimal(payedAmountTotal));
//		summarizing.put("待收金额合计",debtAmountTotal.toString());
		//////////////////////////////////////////// 处理待收金额合计结束 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		////////////////////////////////////////////// 处理统计数据结束 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

		HSSFWorkbook wb = ExcelUtil.getHSSFWorkbook("客户对帐", true, summarizing, query, title, pageInfo.getRecords(), null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		wb.write(baos);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentDispositionFormData("attachment", new String("客户对帐.xls".getBytes(DragonConstant.srcEncoding), DragonConstant.destEncoding));
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		return new ResponseEntity<byte[]>(baos.toByteArray(), headers, HttpStatus.CREATED);
	}

	@GetMapping(value = "/customer-debt-detail-excel/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "custId", value = "客户id", paramType = "query", required = true, dataType = "int"),
			@ApiImplicitParam(name = "custName", value = "客户", paramType = "query", required = true, dataType = "string"),
			@ApiImplicitParam(name = "beginTime", value = "开始日期", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "endTime", value = "结束日期", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "payway", value = "账户类型", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "oper", value = "操作类型", paramType = "query", dataType = "string")
	})
	@ApiOperation("客户欠款明细")
	public ResponseEntity<byte[]> listCustomerTradeDetail(
			@RequestParam(name = "custId") Integer custId,
			@RequestParam(name = "custName") String custName,
			@RequestParam(required = false, name = "oper") String oper,
			@RequestParam(required = false, name = "payway") String payway,
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "beginTime") String beginTime,
			@PathVariable("current") Integer current, @PathVariable("size") Integer size
	) throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("oper", oper);
		params.put("custId", custId);
		params.put("custName", custName);
		params.put("payway", payway);
		params.put("endTime", endTime);
		params.put("beginTime", beginTime);
		Page page = PageUtils.getPageParam(new PageHelper(current, size));

//		Page<DealDetail> pageInfo = receiptService.getDealDetail(params, page);
//
//		DealDetailSummarizing dealDetailSummarizingForAdd = indentService.getDealDetailSummarizingForAdd(params);
//
//		DealDetailSummarizing dealDetailSummarizingForDecrease = receiptService.getDealDetailSummarizingForDecrease(params);
//
//		if (null != dealDetailSummarizingForAdd) {
//			dealDetailSummarizingForDecrease.setPlusDebt(dealDetailSummarizingForAdd.getPlusDebt());
//		}
//
//		// 获取客户的上期结余欠款
//		String lastSurplusDebt = receiptService.getLastSurplusDebt(params);
//		// 设值上期结余欠款
//		if (dealDetailSummarizingForDecrease != null) {
//			if (StringUtils.isEmpty(lastSurplusDebt)) {
//				dealDetailSummarizingForDecrease.setLastSurplusDebt("0");
//			} else {
//				dealDetailSummarizingForDecrease.setLastSurplusDebt(lastSurplusDebt);
//			}
//		} else {
//			dealDetailSummarizingForDecrease = new DealDetailSummarizing();
//		}
//		// 处理应收欠款
//		dealDetailSummarizingForDecrease.setNeedCollect(
//				new BigDecimal(dealDetailSummarizingForDecrease.getLastSurplusDebt())
//						.add(new BigDecimal(dealDetailSummarizingForDecrease.getPlusDebt()))
//						.subtract(new BigDecimal(dealDetailSummarizingForDecrease.getReceiptedDetb()))
//						.subtract(new BigDecimal(dealDetailSummarizingForDecrease.getCutDebt()))
//						.toString()
//		);
		Page<DealDetail> pageInfo = receiptService.getDealDetail(params, page);
		DealDetailSummarizing dealDetailSummarizing = dealDetailService.getDealDetailSummarizing(params);

		// 获取客户的上期结余欠款
		String lastSurplusDebt = receiptService.getLastSurplusDebt(params);
		dealDetailSummarizing.setLastSurplusDebt(lastSurplusDebt);

		BigDecimal plusDebt = new BigDecimal(dealDetailSummarizing.getPlusDebt());

		BigDecimal receiptDebt = new BigDecimal(dealDetailSummarizing.getReceiptedDetb());

		BigDecimal cutDebt = new BigDecimal(dealDetailSummarizing.getCutDebt());

		String needCollect = new BigDecimal(lastSurplusDebt)
				.add(plusDebt)
				.subtract(receiptDebt)
				.subtract(cutDebt)
				.toString();

		// 处理应收欠款
		dealDetailSummarizing.setNeedCollect(needCollect);
		Map<String, String> summarizing = new LinkedHashMap<>();
		Map<String, Object> query = new LinkedHashMap<>();
		Map<String, String> title = new LinkedHashMap<>();

		title.put("time", " 时间 ");
		title.put("nameNo", "单据名称-编号");
		title.put("oper", " 业务类型 ");
		title.put("payway", "账户");
		title.put("buy", "增加欠款");
		title.put("pay", "减少欠款");
		title.put("debt", " 欠款余额 ");
		title.put("remarks", "备注");
		title.put("auditRemarks", "财审备注");

		List<DealDetailModel> contents = new ArrayList<>();
		List<DealDetail> records = pageInfo.getRecords();

		records.forEach(dealDetail -> {
			if (dealDetail.getAmount().startsWith(DragonConstant.PLUS)) {
				contents.add(
						new DealDetailModel(dealDetail.getTime().substring(0, 11), dealDetail.getAmount().substring(1), " ",
								dealDetail.getNewDebt(), dealDetail.getOper(), dealDetail.getNameNo(), dealDetail.getPayway(), dealDetail.getRemarks(), dealDetail.getAuditRemarks())
				);

			} else if (dealDetail.getAmount().startsWith(DragonConstant.MINUS)) {
				contents.add(
						new DealDetailModel(dealDetail.getTime().substring(0, 11), " ", dealDetail.getAmount().substring(1),
								dealDetail.getNewDebt(), dealDetail.getOper(), dealDetail.getNameNo(), dealDetail.getPayway(), dealDetail.getRemarks(), dealDetail.getAuditRemarks())
				);
			}
		});
//		contents.add(new DealDetailModel("上期结欠", " ", " ", lastSurplusDebt, " ", " "," ", " "));
//		Collections.reverse(contents);

		summarizing.put("上期欠款余额", dealDetailSummarizing.getLastSurplusDebt());
		summarizing.put("增加欠款", dealDetailSummarizing.getPlusDebt());
		summarizing.put("减少欠款(收款)", dealDetailSummarizing.getReceiptedDetb());
		summarizing.put("减少欠款(付款)", dealDetailSummarizing.getCutDebt());
		summarizing.put("应收欠款", dealDetailSummarizing.getNeedCollect());

		if (StringUtils.isNotEmpty(beginTime) && StringUtils.isNotEmpty(endTime)) {
			query.put("时间", beginTime.concat("至").concat(endTime));
		}
		if (StringUtils.isNotEmpty(oper)) {
			query.put("业务类型", oper);
		}
		if (StringUtils.isNotEmpty(payway)) {
			query.put("账户", payway);
		}
		HSSFWorkbook wb = ExcelUtil.getHSSFWorkbook(custName.concat("-欠款明细"), true, summarizing, query, title, contents, null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		wb.write(baos);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentDispositionFormData("attachment", new String(custName.concat("-欠款明细.xls").getBytes(DragonConstant.srcEncoding), DragonConstant.destEncoding));
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

		return new ResponseEntity<byte[]>(baos.toByteArray(), headers, HttpStatus.CREATED);
	}

	/**
	 * 商品送货统计
	 */
	@GetMapping("/goods-delivery-statics-excel/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "beginTime", value = "时间段开始", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "endTime", value = "时间段结束", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "custName", value = "客户名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "shipmanId", value = "送货人id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "shipman", value = "送货人", paramType = "query", dataType = "string"),
	})
	@ApiOperation("商品送货统计")
	public ResponseEntity<byte[]> goodsDeliveryStaticsExcel(
			@RequestParam(required = false, name = "shipman") String shipman,
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "custName") String custName,
			@RequestParam(required = false, name = "beginTime") String beginTime,
			@RequestParam(required = false, name = "shipmanId") Integer shipmanId,
			@PathVariable(value = "current") Integer current, @PathVariable(value = "size") Integer size
	) throws NoSuchFieldException, IllegalAccessException, IOException {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Map<String, Object> params = new HashMap<>();
		params.put("endTime", endTime);
		params.put("custName", custName);
		params.put("beginTime", beginTime);
		params.put("shipmanId", shipmanId);
		CommonSummation summation = indentService.getGoodsDeliveryStaticsSummarizing(params);
		Page<DeliveryStaticsModel> pageInfo = indentService.getGoodsDeliveryStatics(page, params);
		List<DeliveryStaticsModel> records = pageInfo.getRecords();
		////////////////////////////////////////////// 处理列标题 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		Map<String, String> title = new LinkedHashMap<>();
		title.put("shipman", "送货人");
		title.put("salesAmnt", "销售金额");
		title.put("indentTotal", "实收金额");
		title.put("num", "送货数量");
		////////////////////////////////////////////// 处理列标题结束 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

		////////////////////////////////////////////// 处理查询条件 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		Map<String, Object> query = new LinkedHashMap<>();
		if (StringUtils.isNotEmpty(shipman)) {
			query.put("送货员", shipman);
		}
		if (StringUtils.isNotEmpty(custName)) {
			query.put("客户名称", custName);
		}
		if (StringUtils.isNotEmpty(beginTime) && StringUtils.isNotEmpty(endTime)) {
			query.put("时间周期", beginTime.concat("至").concat(endTime));
		}
		////////////////////////////////////////////// 处理查询条件结束 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

		////////////////////////////////////////////// 处理统计数据 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		Map<String, String> summarizing = new LinkedHashMap<>();
		String salesAmntSum = summation.getSalesAmntSum();
		String indentTotalSum = summation.getIndentTotalSum();
		String salesNumSum = summation.getSalesNumSum();
		summarizing.put("销售金额合计", salesAmntSum);
		summarizing.put("实收金额合计", indentTotalSum);
		summarizing.put("送货数量合计", salesNumSum);
		////////////////////////////////////////////// 处理统计数据结束 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

		HSSFWorkbook wb = ExcelUtil.getHSSFWorkbook("送货总账", true, summarizing, query, title, records, null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		wb.write(baos);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentDispositionFormData("attachment", new String("送货总账.xls".getBytes(DragonConstant.srcEncoding), DragonConstant.destEncoding));
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		return new ResponseEntity<byte[]>(baos.toByteArray(), headers, HttpStatus.CREATED);
	}

	/**
	 * 商品送货明细统计
	 */
	@GetMapping("/goods-delivery-detail-statics-excel/{shipmanId}/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "beginTime", value = "时间段开始", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "endTime", value = "时间段结束", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "custName", value = "客户名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "shipmanId", value = "送货人id", paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "shipman", value = "送货人", paramType = "path", dataType = "String"),
	})
	@ApiOperation("商品送货明细统计")
	public ResponseEntity<byte[]> goodsDeliveryDetailStaticsExcel(
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "custName") String custName,
			@RequestParam(required = false, name = "beginTime") String beginTime,
			@PathVariable(value = "shipmanId") Integer shipmanId,
			@PathVariable(value = "current") Integer current,
			@PathVariable(value = "size") Integer size
	) throws NoSuchFieldException, IllegalAccessException, IOException {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Map<String, Object> params = new HashMap<>();
		params.put("endTime", endTime);
		params.put("custName", custName);
		params.put("beginTime", beginTime);
		params.put("shipmanId", shipmanId);
		DeliveryStaticsModel summation = indentService.getGoodsDeliveryDetailsStaticsSummarizing(params);
		Page<DeliveryDetailsStaticsModel> pageInfo = indentService.getGoodsDeliveryDetailsStatics(params, page);
		List<DeliveryDetailsStaticsModel> records = pageInfo.getRecords();
		////////////////////////////////////////////// 处理列标题 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		Map<String, String> title = new LinkedHashMap<>();
		title.put("indentNo", "订单号");
		title.put("deliveryDate", "送货日期");
		title.put("custName", "客户名称");
		title.put("salesAmnt", "销售金额");
		title.put("indentTotal", "实收金额");
		title.put("num", "订单数量");
		////////////////////////////////////////////// 处理列标题结束 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

		////////////////////////////////////////////// 处理查询条件 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		Map<String, Object> query = new LinkedHashMap<>();
		if (StringUtils.isNotEmpty(custName)) {
			query.put("客户名称", custName);
		}
		if (StringUtils.isNotEmpty(beginTime) && StringUtils.isNotEmpty(endTime)) {
			query.put("时间周期", beginTime.concat("至").concat(endTime));
		}
		////////////////////////////////////////////// 处理查询条件结束 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

		////////////////////////////////////////////// 处理统计数据 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		Map<String, String> summarizing = new LinkedHashMap<>();
		String shipman = summation.getShipman();
		String salesAmnt = summation.getSalesAmnt();
		String indentTotal = summation.getIndentTotal();
		String num = summation.getNum();
		summarizing.put("送货员", shipman);
		summarizing.put("销售金额合计", salesAmnt);
		summarizing.put("实收金额合计", indentTotal);
		summarizing.put("送货数量合计", num);
		////////////////////////////////////////////// 处理统计数据结束 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

		HSSFWorkbook wb = ExcelUtil.getHSSFWorkbook("送货明细", true, summarizing, query, title, records, null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		wb.write(baos);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentDispositionFormData("attachment", new String("送货明细.xls".getBytes(DragonConstant.srcEncoding), DragonConstant.destEncoding));
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		return new ResponseEntity<byte[]>(baos.toByteArray(), headers, HttpStatus.CREATED);
	}
}