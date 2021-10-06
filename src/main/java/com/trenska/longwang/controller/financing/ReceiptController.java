package com.trenska.longwang.controller.financing;

import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.dao.customer.AreaGrpMapper;
import com.trenska.longwang.dao.customer.CustomerMapper;
import com.trenska.longwang.dao.financing.DealDetailMapper;
import com.trenska.longwang.entity.PageHelper;
import com.trenska.longwang.entity.customer.Customer;
import com.trenska.longwang.entity.financing.DealDetail;
import com.trenska.longwang.entity.financing.DealDetailSummarizing;
import com.trenska.longwang.entity.financing.Receipt;
import com.trenska.longwang.entity.indent.Indent;
import com.trenska.longwang.entity.sys.SysEmp;
import com.trenska.longwang.model.finaning.AccountCheckingModel;
import com.trenska.longwang.model.prints.WebPrintModel;
import com.trenska.longwang.model.report.AccountCheckingSummationModel;
import com.trenska.longwang.model.report.CommonReceiptSummation;
import com.trenska.longwang.model.sys.CommonResponse;
import com.trenska.longwang.service.customer.ICustomerService;
import com.trenska.longwang.service.financing.IDealDetailService;
import com.trenska.longwang.service.financing.ILoanService;
import com.trenska.longwang.service.financing.IReceiptService;
import com.trenska.longwang.service.indent.IIndentService;
import com.trenska.longwang.service.sys.ISysEmpService;
import com.trenska.longwang.util.*;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * 2019/4/30
 * 创建人:Owen
 */
@Slf4j
@CrossOrigin
@RestController
@SuppressWarnings("all")
@RequestMapping("/financing")
@Api(description = "财务管理接口")
public class ReceiptController {

	@Autowired
	private IReceiptService receiptService;

	@Autowired
	private IIndentService indentService;

	@Autowired
	private ISysEmpService empService;

	@Autowired
	private ICustomerService customerService;

	@Value("${template.path}")
	private String templatePath;

	@Autowired
	private AreaGrpMapper areaGrpMapper;

	@Autowired
	private ILoanService loanService;

	@Autowired
	private IDealDetailService dealDetailService;

	@Autowired
	private DealDetailMapper dealDetailMapper;

	@Autowired
	private CustomerMapper customerMapper;

	@PostMapping("/receipt/add")
	@ApiOperation("新建收款单")
	public CommonResponse addReceipt(@ApiParam(name = "receipt", value = "收款单", required = true) @RequestBody Receipt receipt) {
		if (receipt == null) {
			return CommonResponse.getInstance().succ(false).msg("收款单不能为空！");
		}
		return receiptService.saveReceipt(receipt);
	}

	@PostMapping("/pay/add")
	@ApiOperation("新建付款单")
	public CommonResponse addPayReceipt(@ApiParam(name = "pay", value = "付款单", required = true) @RequestBody Receipt pay) {
		if (pay == null) {
			return CommonResponse.getInstance().succ(false).msg("付款单不能为空！");
		}
		return receiptService.savePayReceipt(pay);
	}

	@PostMapping("/receipt/check/amount")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "indentId", value = "订货单ID", required = true, dataType = "int"),
			@ApiImplicitParam(name = "history", value = "本次收款金额", required = true, dataType = "double")
	})
	@ApiOperation("检查收款金额是否超过了剩余订货单应收金额")
	public CommonResponse checkAmount(Integer indentId, Double amount) {
		if (null == indentId) {
			return CommonResponse.getInstance().succ(false).msg("订单ID不能为null");
		}
		Indent indent = indentService.getById(indentId);
		if (null == indent) {
			return CommonResponse.getInstance().succ(false).msg("无此订货单信息");
		}
		if (null == amount || amount < 0) {
			return CommonResponse.getInstance().succ(false).msg("收款金额不能为空或者为负数");
		}

		//已收 订货单金额
		Double receivedAmnt = Double.valueOf(indent.getReceivedAmnt());
		Double indentTotal = Double.valueOf(indent.getIndentTotal());  //订货单总金额

		if ((receivedAmnt + amount) > indentTotal) {
			return CommonResponse.getInstance().succ(false).msg("收款金额已超过订货单应收金额");
		}
		return CommonResponse.getInstance().succ(true);
	}

	@RequestMapping(value = "/list/{current}/{size}", method = RequestMethod.GET)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "custId", value = "客户id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "stat", value = "状态", paramType = "query", dataType = "boolean"),
			@ApiImplicitParam(name = "receiptNo", value = "单号", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "areaGrpId", value = "所属区域", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "salesmanId", value = "业务员id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "chargemanId", value = "收款人id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "custName", value = "客户名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "type", value = "收款单/付款单", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "payway", value = "收/付款方式", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "accountType", value = "账目类型", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "endTime", value = "时间段查询条件-结束", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "beginTime", value = "时间段查询条件-开始", paramType = "query", dataType = "string"),
	})
	@ApiOperation("收款/付款单通用分页")
	public PageHelper<Receipt> listReceiptPage(
			@PathVariable("size") Integer size,
			@PathVariable("current") Integer current,
			@RequestParam(required = false, name = "type") String type,
			@RequestParam(required = false, name = "stat") Boolean stat,
			@RequestParam(required = false, name = "payway") String payway,
			@RequestParam(required = false, name = "custId") Integer custId,
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "custName") String custName,
			@RequestParam(required = false, name = "beginTime") String beginTime,
			@RequestParam(required = false, name = "receiptNo") String receiptNo,
			@RequestParam(required = false, name = "areaGrpId") Integer areaGrpId,
			@RequestParam(required = false, name = "salesmanId") Integer salesmanId,
			@RequestParam(required = false, name = "accountType") String accountType,
			@RequestParam(required = false, name = "chargemanId") Integer chargemanId
	) {
		Map<String, Object> params = new HashMap<>();
		params.put("type", type);
		params.put("stat", stat);
		params.put("payway", payway);
		params.put("custId", custId);
		params.put("endTime", endTime);
		params.put("custName", custName);
		params.put("beginTime", beginTime);
		params.put("areaGrpId", areaGrpId);
		params.put("receiptNo", receiptNo);
		params.put("salesmanId", salesmanId);
		params.put("chargemanId", chargemanId);
		params.put("accountType", accountType);
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Page<Receipt> receiptPage = receiptService.getReceiptPageSelective(params, page);
		CommonReceiptSummation summation = receiptService.getReceiptSelectiveSummation(params);
		return PageHelper.getInstance().pageData(receiptPage).summarizing(summation);
	}

	@RequestMapping(value = "/info/{receiptId}", method = RequestMethod.GET)
	@ApiOperation("收款/付款单详情")
	public CommonResponse info(@PathVariable("receiptId") Long receiptId) {
		return CommonResponse.getInstance().succ(true).data(receiptService.getReceiptById(receiptId));
	}

	@RequestMapping(value = "/receipt/invalid/{receiptId}", method = RequestMethod.PUT)
	@ApiOperation("作废收款单")
	public CommonResponse cancelReceipt(@PathVariable("receiptId") Long receiptId, HttpServletRequest request) {
		Receipt receipt = receiptService.getById(receiptId);
		if (null == receipt) {
			return CommonResponse.getInstance().succ(false).msg("无此收款单信息.");
		}
		if (receipt.getStat() == false) {
			return CommonResponse.getInstance().succ(false).msg("收款单已作废,请勿重新操作.");
		}
		if (StringUtils.isNotEmpty(receipt.getBusiNo())) {
			return CommonResponse.getInstance().succ(false).msg("关联订货单的收款单请到订货单处作废.");
		}
		return receiptService.cancelReceipt(receipt, request);
	}

	@RequestMapping(value = "/pay/invalid/{receiptId}", method = RequestMethod.PUT)
	@ApiOperation("作废付款单")
	public CommonResponse cancelPayReceipt(@PathVariable("receiptId") Long receiptId) {
		return receiptService.cancelPayReceiptById(receiptId);
	}

	@RequestMapping(value = "/delete/{receiptId}", method = RequestMethod.DELETE)
	@ApiOperation("删除付款/收款单")
	public CommonResponse delete(@PathVariable("receiptId") Long receiptId) {

		if (!NumberUtil.isLongUsable(receiptId)) {
			return CommonResponse.getInstance().succ(false).msg("删除失败:无此单据");
		}
		Receipt receipt = receiptService.getById(receiptId);

		if (null == receipt) {
			return CommonResponse.getInstance().succ(false).msg("删除失败:无此单据");
		}

		Boolean stat = receipt.getStat();
		if (stat) {
			return CommonResponse.getInstance().succ(false).msg("删除失败:作废的".concat(receipt.getType()).concat("才可删除."));
		}
		receiptService.removeById(receiptId);
		return CommonResponse.getInstance().succ(true).msg(receipt.getType().concat("删除成功."));
	}

	@RequestMapping(value = "/account/checking/{current}/{size}", method = RequestMethod.GET)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "salesmanId", value = "业务员", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "areaGrpId", value = "所属区域", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "priceGrpId", value = "所属分组", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "custId", value = "客户id", paramType = "query", dataType = "int"),
			@ApiImplicitParam(name = "custName", value = "客户名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "endTime", value = "时间段查询条件-结束", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "beginTime", value = "时间段查询条件-开始", paramType = "query", dataType = "string")
	})
	@ApiOperation("客户对帐通用分页")
	public PageHelper<AccountCheckingModel> listAccountChecking(
			@RequestParam(required = false, name = "custId") String custId,
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "custName") String custName,
			@RequestParam(required = false, name = "beginTime") String beginTime,
			@RequestParam(required = false, name = "areaGrpId") Integer areaGrpId,
			@RequestParam(required = false, name = "priceGrpId") String priceGrpId,
			@RequestParam(required = false, name = "salesmanId") Integer salesmanId,
			@PathVariable("current") Integer current, @PathVariable("size") Integer size
	) {
		Map<String, Object> params = new HashMap<>();
		params.put("custId", custId);
		params.put("endTime", endTime);
		params.put("custName", custName);
		params.put("areaGrpId", areaGrpId);
		params.put("beginTime", beginTime);
		params.put("salesmanId", salesmanId);
		params.put("priceGrpId", priceGrpId);

		Page page = PageUtils.getPageParam(new PageHelper(current, size));

		Page<AccountCheckingModel> pageInfo = receiptService.getAccountChecking(params, page);
		List<AccountCheckingModel> records = pageInfo.getRecords();
		AccountCheckingSummationModel dbSummation = new AccountCheckingSummationModel();
		// 没有满足条件的记录就不去做统计了
		if (CollectionUtils.isNotEmpty(records)) {
			dbSummation = receiptService.getAccountCheckingSummation(params);
		}
		// 保证数据库无数据时返回的各统计为 0
		AccountCheckingSummationModel summarizing =
				(dbSummation != null) ? dbSummation : new AccountCheckingSummationModel();

		return PageHelper.getInstance().pageData(pageInfo).summarizing(summarizing);
	}

	@RequestMapping(value = "/trade/detail/{current}/{size}", method = RequestMethod.GET)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "custId", value = "客户id", paramType = "query", required = true, dataType = "int"),
			@ApiImplicitParam(name = "beginTime", value = "开始日期", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "endTime", value = "结束日期", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "payway", value = "账户类型", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "oper", value = "业务类型", paramType = "query", dataType = "string")
	})
	@ApiOperation("客户欠款明细")
	public PageHelper<DealDetail> listCustomerTradeDetail(
			@RequestParam(name = "custId") Integer custId,
			@RequestParam(required = false, name = "oper") String oper,
			@RequestParam(required = false, name = "payway") String payway,
			@RequestParam(required = false, name = "endTime") String endTime,
			@RequestParam(required = false, name = "beginTime") String beginTime,
			@PathVariable("current") Integer current, @PathVariable("size") Integer size
	) {
		Map<String, Object> params = new HashMap<>();
		params.put("oper", oper);
		params.put("custId", custId);
		params.put("payway", payway);
		params.put("endTime", endTime);
		params.put("beginTime", beginTime);
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Page<DealDetail> pageInfo = receiptService.getDealDetail(params, page);

		DealDetailSummarizing dealDetailSummarizing = dealDetailService.getDealDetailSummarizing(params);

		// 获取客户的上期结余欠款
		String lastSurplusDebt = receiptService.getLastSurplusDebt(params);
		dealDetailSummarizing.setLastSurplusDebt(lastSurplusDebt);

		BigDecimal plusDebt = new BigDecimal(dealDetailSummarizing.getPlusDebt());

		BigDecimal receiptDebt = new BigDecimal(dealDetailSummarizing.getReceiptedDetb());

		BigDecimal cutDebt = new BigDecimal(dealDetailSummarizing.getCutDebt());

		String needCollect =
				new BigDecimal(lastSurplusDebt)
						.add(plusDebt)
						.subtract(receiptDebt)
						.subtract(cutDebt)
						.toString();

		// 处理应收欠款
		dealDetailSummarizing.setNeedCollect(needCollect);
		return PageHelper.getInstance().pageData(pageInfo).summarizing(dealDetailSummarizing);
	}

	@ApiOperation(value = "打印收款单")
	@RequestMapping(value = "/printSkd/{receiptId}", method = RequestMethod.GET)
	public CommonResponse printSkd(@PathVariable Long receiptId) {

		Map<String, Object> params = new HashMap<>();
		Receipt receipt = receiptService.getById(receiptId);
		params.put("receipt_time", receipt.getReceiptTime());
		params.put("receipt_no", receipt.getReceiptNo());

		params.put("customer", "");
		if (null != receipt.getCustId()) {
			Customer customer = customerService.getById(receipt.getCustId());
			params.put("customer", customer.getCustName());
		}

		DecimalFormat df = new DecimalFormat("#0.00");
		String format = df.format(new BigDecimal(receipt.getReceiptAmount()));
		BigDecimal lowAmount = new BigDecimal(format);

		String formatAmount = NumberFormat.getCurrencyInstance(Locale.SIMPLIFIED_CHINESE).format(lowAmount.doubleValue());

//		String formatAmount = NumberFormat.getInstance().format(lowAmount.doubleValue());

		params.put("lowAmount", formatAmount); //小写
		params.put("capAmount", RMBUtil.toUpper(receipt.getReceiptAmount()));//大写金额

		params.put("receipt_type", receipt.getAccountType());
		params.put("payway", receipt.getPayway());//收款方式
		params.put("remarks", receipt.getReceiptRemarks());
		params.put("empName", "");
		if (null != receipt.getEmpId()) {
			SysEmp sysemp = empService.getById(receipt.getEmpId());
			params.put("empName", sysemp != null ? sysemp.getEmpName() : "");//制单人
		}

		String htmlContent = PDFUtil.freemarkerRender(params, templatePath + File.separator + "skdpdftpl/skd.ftl");

		WebPrintModel wm = PrintSingleton.INSTNACE.getInstance().retOk(htmlContent, "24.1", "9.31");

		return CommonResponse.getInstance().succ(true).data(wm);
	}

	@ApiOperation(value = "打印付款单")
	@RequestMapping(value = "/printFkd/{receiptId}", method = RequestMethod.GET)
	public CommonResponse printFkd(@PathVariable Long receiptId) {
		Map<String, Object> params = new HashMap<>();
		Receipt receipt = receiptService.getById(receiptId);
		params.put("receipt_no", receipt.getReceiptNo());
		params.put("receipt_time", receipt.getReceiptTime());

		params.put("customer", "");
		if (null != receipt.getCustId()) {
			Customer customer = customerService.getById(receipt.getCustId());
			params.put("customer", customer.getCustName());
		}
		params.put("empName", "");
		params.put("remarks", receipt.getReceiptRemarks());
		params.put("receipt_type", receipt.getAccountType());
		params.put("payway", receipt.getPayway());//收款方式
		params.put("lowAmount", receipt.getReceiptAmount()); //小写
		params.put("capAmount", RMBUtil.toUpper(receipt.getReceiptAmount()));//大写金额
		if (null != receipt.getEmpId()) {
			SysEmp sysemp = empService.getById(receipt.getEmpId());
			params.put("empName", sysemp != null ? sysemp.getEmpName() : "");//制单人
		}
		String htmlContent = PDFUtil.freemarkerRender(params, templatePath + File.separator + "fkdpdftpl/fkd.ftl");
		WebPrintModel wm = PrintSingleton.INSTNACE.getInstance().retOk(htmlContent, "24.1", "9.31");
		return CommonResponse.getInstance().succ(true).data(wm);
	}
}