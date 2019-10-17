package com.trenska.longwang.service.impl.financing;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.annotation.DataAuthVerification;
import com.trenska.longwang.constant.Constant;
import com.trenska.longwang.context.ApplicationContextHolder;
import com.trenska.longwang.dao.customer.AreaGrpMapper;
import com.trenska.longwang.dao.customer.CustomerMapper;
import com.trenska.longwang.dao.financing.DealDetailMapper;
import com.trenska.longwang.dao.financing.LoanMapper;
import com.trenska.longwang.dao.financing.ReceiptMapper;
import com.trenska.longwang.dao.indent.IndentDetailMapper;
import com.trenska.longwang.dao.indent.IndentMapper;
import com.trenska.longwang.entity.customer.Customer;
import com.trenska.longwang.entity.financing.DealDetail;
import com.trenska.longwang.entity.financing.DealDetailSummarizing;
import com.trenska.longwang.entity.financing.Receipt;
import com.trenska.longwang.entity.sys.SysConfig;
import com.trenska.longwang.model.finaning.AccountCheckingModel;
import com.trenska.longwang.model.report.AccountCheckingSummationModel;
import com.trenska.longwang.model.report.CommonReceiptSummation;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.financing.IDealDetailService;
import com.trenska.longwang.service.financing.IReceiptService;
import com.trenska.longwang.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 2019/4/3
 * 创建人:Owen
 * 收款单
 */
@Slf4j
@Service
@SuppressWarnings("all")
public class ReceiptServiceImpl extends ServiceImpl<ReceiptMapper, Receipt> implements IReceiptService {

	@Autowired
	private CustomerMapper customerMapper;

	@Autowired
	private IndentMapper indentMapper;

	@Autowired
	private IndentDetailMapper indentDetailMapper;

	@Autowired
	private DealDetailMapper dealDetailMapper;

	@Autowired
	private IDealDetailService dealDetailService;

	@Autowired
	private AreaGrpMapper areaGrpMapper;

	@Autowired
	private ReceiptMapper receiptMapper;

	@Autowired
	private LoanMapper loanMapper;

	@Resource(name = Constant.REDIS_JSON_TEMPLATE_NAME)
	private RedisTemplate<String, Object> jsonRedisTemplate;

	@Override
	public Page<Receipt> getReceiptPageSelective(Map<String, Object> params, Page page) {
		page.setTotal(baseMapper.selectReceiptCountSelective(params));
		List<Receipt> receipts = baseMapper.selectReceiptPageSelective(params, page);

		SysConfig sysConfig = (SysConfig) jsonRedisTemplate.opsForValue().get(Constant.SYS_CONFIG_IDENTIFIER + SysUtil.getEmpIdInRedis());
		Integer retain = sysConfig.getRetain();

		for (Receipt receipt : receipts) {
			BigDecimal receiptAmount = new BigDecimal(receipt.getReceiptAmount());
			receiptAmount = receiptAmount.setScale(retain,RoundingMode.HALF_UP);
			receipt.setReceiptAmount(receiptAmount.toString());
		}

		page.setRecords(receipts);
		return page;
	}

	/**
	 * 新建收款单
	 * 客户欠款减少
	 *
	 * @param receipt
	 * @return
	 */
	@Override
	@Transactional
	public ResponseModel saveReceipt(Receipt receipt, HttpServletRequest request) {
		return ReceiptUtil.saveReceipt(receipt, request, receiptMapper, Constant.SKD_CHINESE, Constant.SK_CHINESE);
	}

	/**
	 * 作废收款单
	 * 订货单金额还原
	 * 客户欠款增加
	 *
	 * @param receiptId
	 * @return
	 */
	@Override
	@Transactional
	public ResponseModel cancelReceipt(Receipt receipt, HttpServletRequest request) {

		List<Receipt> receipts = new ArrayList<>();
		receipts.add(receipt);
		int custId = receipt.getCustId();
		Customer dbCustomer = customerMapper.selectById(custId);
		String nameNo = StringUtil.makeNameNo(Constant.SKD_CHINESE, receipt.getReceiptNo());
		ReceiptUtil.cancelReceipts(receipts, dbCustomer, nameNo);
		return ResponseModel.getInstance().succ(true).msg("作废收款单成功");
	}

	@Override
	public Receipt getReceiptById(Long receiptId) {
		int retain = SysUtil.getSysConfigRetain();
		Receipt receipt = super.baseMapper.selectReceiptById(receiptId);
		BigDecimal receiptAmount = new BigDecimal(receipt.getReceiptAmount());
		receiptAmount = receiptAmount.setScale(retain,RoundingMode.HALF_UP);
		receipt.setReceiptAmount(receiptAmount.toString());
		return receipt;
	}

	/**
	 * 新建付款单
	 * 客户欠款减少
	 *
	 * @param pay
	 * @return
	 */
	@Override
	@Transactional
	public ResponseModel savePayReceipt(Receipt pay, HttpServletRequest request) {

		String createTime = TimeUtil.getCurrentTime(Constant.TIME_FORMAT);
		pay.setCreateTime(createTime);

		if (Objects.isNull(pay)) {
			return ResponseModel.getInstance().succ(false).msg("付款单不能为null");
		}
		return ReceiptUtil.saveReceipt(pay, request, receiptMapper, Constant.DHD_CHINESE, Constant.FK_CHINESE);

	}

	/**
	 * 作废付款单
	 * 客户应收欠款增加
	 *
	 * @param receiptId
	 * @return
	 */
	@Override
	@Transactional
	public ResponseModel cancelPayReceiptById(Long receiptId) {

		Receipt dbPayReceipt = this.getById(receiptId);
		if (null == dbPayReceipt) {
			return ResponseModel.getInstance().succ(false).msg("无此付款单信息");
		}
		if (dbPayReceipt.getStat() == false) {
			return ResponseModel.getInstance().succ(false).msg("付款单已作废,请勿重新操作.");
		}

		// 付款单金额
		String receiptAmount = dbPayReceipt.getReceiptAmount();

		// 更新客户表  欠款增加
		Customer customer = customerMapper.selectById(dbPayReceipt.getCustId());
		String debt = new BigDecimal(customer.getDebt()).add(new BigDecimal(receiptAmount)).toString();
		new Customer(dbPayReceipt.getCustId(), debt).updateById();

		// 作废付款单
		new Receipt(receiptId, false).updateById();

		String nameNo = StringUtil.makeNameNo(dbPayReceipt.getType(), dbPayReceipt.getReceiptNo());
		Integer custId = customer.getCustId();

		String currentTime = TimeUtil.getCurrentTime(Constant.TIME_FORMAT);

		String amount = "+".concat(receiptAmount);

		String oper = dbPayReceipt.getAccountType();

		String remarks = dbPayReceipt.getReceiptRemarks();

		String payway = dbPayReceipt.getPayway();

		// 客户欠款增加 ，加前缀 +
		DealDetailUtil.saveDealDetail(custId, nameNo, currentTime, amount, debt, oper, payway, remarks);

		return ResponseModel.getInstance().succ(true).msg("作废付款单成功.");
	}

	/**
	 * 客户对账
	 *
	 * @param params
	 * @param page
	 * @return
	 */
	@Override
	@DataAuthVerification
	public Page<AccountCheckingModel> getAccountChecking(Map<String, Object> params, Page page, HttpServletRequest request) {

		// 处理业务员: 查询业务员就是查询该业务员所有的客户 ; 将业务员的客户和处数据权限的客户区交集
		Integer salesmanId = (Integer) params.get("salesmanId");
		if (NumberUtil.isIntegerUsable(salesmanId)) {
			Set<Integer> intersectionCustIds = null;
			Set<Integer> custIdsOfSalesman = customerMapper.selectCustIdsOfSalesman(salesmanId);
			Set<Integer> custIdsOfDataAuthority = (Set<Integer>) params.get("custIds");
			intersectionCustIds = custIdsOfSalesman.stream().filter(custId -> custIdsOfDataAuthority.contains(custId)).collect(Collectors.toSet());
			List<Integer> lastSurplusCustIds = dealDetailMapper.selectLastSurplusCustIds(params);
			if (CollectionUtils.isNotEmpty(lastSurplusCustIds) && CollectionUtils.isNotEmpty(intersectionCustIds)) {
				intersectionCustIds = intersectionCustIds.stream().filter(custId -> lastSurplusCustIds.contains(custId)).collect(Collectors.toSet());
			}
			params.put("custIds", intersectionCustIds);
			if (CollectionUtils.isEmpty(intersectionCustIds)) {
				return new Page<>(0, 0);

			}
		}

		SysConfig sysConfig = ApplicationContextHolder.getBean(SysConfig.class);

		List<AccountCheckingModel> records = super.baseMapper.selectAccountCheckingPageSelective(params, page);

		//////////////////////////////////////// 处理待收欠款和上期结欠 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		// 这里的客户已经是筛选之后的客户，所以不需要再对条件进行筛选
		if (CollectionUtils.isNotEmpty(records)) {
			for (AccountCheckingModel record : records) {
				//////////////////// 处理上期结欠开始 \\\\\\\\\\\\\\\\\\\
				Integer custId = record.getCustId();
				Object endTime = params.get("endTime");
				Object beginTime = params.get("beginTime");
				Map<String, Object> lastSurplusDebtParamMap = new HashMap<>();
				lastSurplusDebtParamMap.put("custId", custId);
				lastSurplusDebtParamMap.put("endTime", endTime);
				lastSurplusDebtParamMap.put("beginTime", beginTime);
				// 获取每一客户的客户的上期结余欠款
				String lastSurplusDebt = this.getInitDebt(lastSurplusDebtParamMap);
				record.setInitDebt(lastSurplusDebt);
				//////////////////// 处理上期结欠结束 \\\\\\\\\\\\\\\\\\\

				//////////////////// 处理销售应收欠款(销售应收+调帐单增加部分) 和 已付(已付 + 调帐单减少部分)开始\\\\\\\\\\\\\\\\\\\
//				BigDecimal lend = new BigDecimal(record.getLend()); // 借出，欠款增加
//				BigDecimal borrow = new BigDecimal(record.getBorrow()); // 借入，欠款减少
//				BigDecimal salesAmount = new BigDecimal(record.getSalesAmount()).add(lend);
//				BigDecimal payedAmount = new BigDecimal(record.getPayedAmount()).add(borrow);
//				record.setSalesAmount(salesAmount.toString());
//				record.setPayedAmount(payedAmount.toString());
				//////////////////// 处理销售应收欠款结束 \\\\\\\\\\\\\\\\\\\

				//////////////////// 处理待收欠款开始 \\\\\\\\\\\\\\\\\\\
				BigDecimal debtAmount = new BigDecimal(lastSurplusDebt)
						.add(new BigDecimal(record.getSalesAmount()))
						.subtract(new BigDecimal(record.getReceivedAmount()))
						.subtract(new BigDecimal(record.getPayedAmount()));
				record.setDebtAmount(debtAmount.toString());
				//////////////////// 处理待收欠款结束 \\\\\\\\\\\\\\\\\\\
			}
			//////////////////////// 去除全部是 0 的记录 \\\\\\\\\\\\\\\\\\\\\\
			records = records.stream().filter(
					record ->
							new BigDecimal(record.getInitDebt()).compareTo(BigDecimal.ZERO) > 0
						|| 	new BigDecimal(record.getDebtAmount()).compareTo(BigDecimal.ZERO) > 0
						|| 	new BigDecimal(record.getSalesAmount()).compareTo(BigDecimal.ZERO) > 0
						|| 	new BigDecimal(record.getPayedAmount()).compareTo(BigDecimal.ZERO) > 0
						|| 	new BigDecimal(record.getReceivedAmount()).compareTo(BigDecimal.ZERO) > 0
			).collect(Collectors.toList());
		}

		int total = super.baseMapper.selectAccountCheckingCount(params).size();
		page.setRecords(records);
		page.setTotal(total);
		return page;
	}

	/*@Override
	public AccountCheckingSummationModel getAccountCheckingSummation(Map<String, Object> params, HttpServletRequest request) {

		AccountCheckingSummationModel summarizing = null;

		AccountCheckingSummationModel dbAccountCheckingSummationReceiptPart = super.baseMapper.selectAccountCheckingSummationReceiptPart(params);
		AccountCheckingSummationModel dbAccountCheckingSummationIndentPart = indentMapper.selectAccountCheckingSummationIndentPart(params);

		String salesAmountTotal = dbAccountCheckingSummationIndentPart.getSalesAmountTotal();

		dbAccountCheckingSummationReceiptPart.setSalesAmountTotal(dbAccountCheckingSummationIndentPart.getSalesAmountTotal());

		// 确保 统计的每一个参数在数据库没有数据的时候都有值(默认为0)
		summarizing = dbAccountCheckingSummationReceiptPart != null ? dbAccountCheckingSummationReceiptPart : new AccountCheckingSummationModel();

//		List<Integer> lastSurplusDebtSummation = dealDetailMapper.selectLastSurplusDebtSummation(params);
//		Integer initDebtTotal = lastSurplusDebtSummation.stream().mapToInt(Integer::intValue).sum();
//		summarizing.setInitDebtTotal(String.valueOf(initDebtTotal));

		List<Integer> custIds = dealDetailMapper.selectLastSurplusCustIds(params);
		BigDecimal initDebtTotal = new BigDecimal(0);
		for (Integer custId : custIds) {
			params.put("custId",custId);
			String lastSurplusDebt = this.getLastSurplusDebt(params);
			initDebtTotal = initDebtTotal.add(new BigDecimal(lastSurplusDebt));
		}
		summarizing.setInitDebtTotal(initDebtTotal.toString());

		Set<Integer> intersectionCustIds = (Set<Integer>) params.get("custIds");

		if (CollectionUtils.isEmpty(intersectionCustIds)){
			return summarizing;
		}

		////////////////////////////////////////////// 处理待收金额合计开始 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		// 待收金额合计 = 期初欠款合计+销售应收合计-已收合计-已付合计
		BigDecimal debtAmountTotal = initDebtTotal
				.add(new BigDecimal(summarizing.getSalesAmountTotal()))
				.subtract(new BigDecimal(summarizing.getReceivedAmountTotal()))
				.subtract(new BigDecimal(summarizing.getPayedAmountTotal()));
		summarizing.setDebtAmountTotal(debtAmountTotal.toString());
		////////////////////////////////////////////// 处理待收金额合计结束 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		return summarizing;
	}*/

	@Override
	public AccountCheckingSummationModel getAccountCheckingSummation(Map<String, Object> params, HttpServletRequest request) {

		// 处理业务员: 查询业务员就是查询该业务员所有的客户 ; 将业务员的客户和处数据权限的客户区交集 ==> 由于#getAccountChecking()方法已经处理了，所以这里不需要再处理业务员和数据权限
//		Integer salesmanId = (Integer) params.get("salesmanId");
//		if (NumberUtil.isIntegerUsable(salesmanId)) {
//			Set<Integer> intersectionCustIds = null;
//			Set<Integer> custIdsOfSalesman = customerMapper.selectCustIdsOfSalesman(salesmanId);
//			Set<Integer> custIdsOfDataAuthority = (Set<Integer>) params.get("custIds");
//			intersectionCustIds = custIdsOfSalesman.stream().filter(custId -> custIdsOfDataAuthority.contains(custId)).collect(Collectors.toSet());
//			List<Integer> lastSurplusCustIds = dealDetailMapper.selectLastSurplusCustIds(params);
//			if(CollectionUtils.isNotEmpty(lastSurplusCustIds) && CollectionUtils.isNotEmpty(intersectionCustIds)){
//				intersectionCustIds = intersectionCustIds.stream().filter(custId -> lastSurplusCustIds.contains(custId)).collect(Collectors.toSet());
//			}
//			if (CollectionUtils.isEmpty(intersectionCustIds)) {
//				return new AccountCheckingSummationModel();
//
//			}
//			params.put("custIds",intersectionCustIds);
//		}
		Set<Integer> custIds = (Set<Integer>) params.get("custIds");
		// 没有满足条件的记录(没有客户信息)就不去做统计了
		AccountCheckingSummationModel accountCheckingSummation = new AccountCheckingSummationModel();
		if (CollectionUtils.isNotEmpty(custIds)) {
			accountCheckingSummation = super.baseMapper.selectAccountCheckingSummation(params);
		}

		// 这里的custIds是 "归属员工"和"数据权限" 条件筛选之后的结果
		BigDecimal initDebtTotal = new BigDecimal(0);
		for (Integer custId : custIds) {
			params.put("custId", custId);
			String lastSurplusDebt = this.getInitDebt(params);
			initDebtTotal = initDebtTotal.add(new BigDecimal(lastSurplusDebt));
		}
		accountCheckingSummation.setInitDebtTotal(initDebtTotal.toString());

		////////////////////////////////////////////// 处理待收金额合计开始 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		// 待收金额合计 = 期初欠款合计+销售应收合计-已收合计-已付合计
		BigDecimal debtAmountTotal = initDebtTotal
				.add(new BigDecimal(accountCheckingSummation.getSalesAmountTotal()))
				.subtract(new BigDecimal(accountCheckingSummation.getReceivedAmountTotal()))
				.subtract(new BigDecimal(accountCheckingSummation.getPayedAmountTotal()));
		accountCheckingSummation.setDebtAmountTotal(debtAmountTotal.toString());
		////////////////////////////////////////////// 处理待收金额合计结束 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
		return accountCheckingSummation;
	}

	/**
	 * 获取用户交易明细
	 *
	 * @param params
	 * @param page
	 * @return
	 */
	@Override
	public Page<DealDetail> getDealDetail(Map<String, Object> params, Page page) {

		SysConfig sysConfig = SysConfigUtil.getSysConfig();
		Integer retain = sysConfig.getRetain();

		int total = dealDetailMapper.selectDealDetailCountSelective(params);
		List<DealDetail> records = dealDetailMapper.selectDealDetailPageSelective(params, page);
		for (DealDetail record : records) {
			BigDecimal amount = new BigDecimal(record.getAmount());
			amount = amount.setScale(retain, RoundingMode.HALF_UP);
			if (amount.doubleValue() > 0) {
				record.setAmount("+" + amount.toPlainString());
			} else {
				record.setAmount(amount.toPlainString());
			}
		}
		page.setTotal(total);
		page.setRecords(records);
		return page;
	}


	public String getInitDebt(Map<String, Object> params) {
		/*同一时刻可能有多条欠款明细*/
		List<DealDetail> dealDetails = dealDetailMapper.selectLastSurplusDebtBefore(params);

		DealDetail dealDetail = null;
		/*如果有多条记录，取id最小的*/
		if (CollectionUtils.isNotEmpty(dealDetails) && dealDetails.size() >= 1) {
			dealDetails.sort(Comparator.comparing(DealDetail::getId));
			dealDetail = dealDetails.get(0);
		}

		if (ObjectUtils.isNotEmpty(dealDetail)) {
			return dealDetail.getNewDebt();
		}
		dealDetail = dealDetailMapper.selectLastSurplusDebtBetween(params);

		if (null != dealDetail) {
			if (Constant.QCQK_CHINESE.equals(dealDetail.getOper())) {
				return dealDetail.getNewDebt();
			}
			String newDebt = dealDetail.getNewDebt();
			String amount = dealDetail.getAmount();
			BigDecimal lastSurplusDebt = new BigDecimal(newDebt).subtract(new BigDecimal(amount));
			return lastSurplusDebt.toString();
		}
		return "0.00";
	}

	@Override
	public String getLastSurplusDebt(Map<String, Object> params) {
		/*同一时刻可能有多条欠款明细*/
		List<DealDetail> dealDetails = dealDetailMapper.selectLastSurplusDebtBefore(params);

		DealDetail dealDetail = null;
		/*如果有多条记录，取id最小的*/
		if (CollectionUtils.isNotEmpty(dealDetails) && dealDetails.size() >= 1) {
			dealDetails.sort(Comparator.comparing(DealDetail::getId));
			dealDetail = dealDetails.get(0);
		}

		if (null != dealDetail) {
			return dealDetail.getNewDebt();
		}
//		dealDetail = dealDetailMapper.selectLastSurplusDebtBetween(params);
//
//		if(ObjectUtils.isNotEmpty(dealDetail)) {
//			if (Constant.QCQK_CHINESE.equals(dealDetail.getOper())) {
//				return dealDetail.getNewDebt();
//			}
//			String newDebt = dealDetail.getNewDebt();
//			String amount = dealDetail.getAmount();
//			BigDecimal lastSurplusDebt = new BigDecimal(newDebt).subtract(new BigDecimal(amount));
//			return lastSurplusDebt.toString();
//		}
		return "0.00";
	}

	@Override
	public DealDetailSummarizing getDealDetailSummarizingForDecrease(Map<String, Object> params) {
		DealDetailSummarizing dealDetailSummarizing1 = indentMapper.selectDealDetailSummarizingForDecrease(params);
		DealDetailSummarizing dealDetailSummarizing2 = super.baseMapper.selectDealDetailSummarizingForDecrease(params);
		String cutDebt1 = dealDetailSummarizing1.getCutDebt();
		String cutDebt2 = dealDetailSummarizing2.getCutDebt();
		String cutDebt = new BigDecimal(cutDebt1).add(new BigDecimal(cutDebt2)).toString();
		dealDetailSummarizing2.setCutDebt(cutDebt);
		return dealDetailSummarizing2;
	}

	@Override
	@DataAuthVerification
	public Page<Map<String, List<Map<String, String>>>> getReceiptStatics(Map<String, Object> params, Page page, HttpServletRequest request) {

		String type = String.valueOf(params.get("type"));

		List<Receipt> receipts = this.list(
				new LambdaQueryWrapper<Receipt>()
						.eq(Receipt::getType, type)
						.eq(Receipt::getStat, true)
						.select(Receipt::getAccountType, Receipt::getPayway)
						.groupBy(Receipt::getAccountType, Receipt::getPayway)
		);

		List<Map<String, List<Map<String, String>>>> records = new ArrayList<>();

		Set<String> accountTypes = receipts.stream().map(Receipt::getAccountType).collect(Collectors.toSet());

		Set<String> payways = receipts.stream().map(Receipt::getPayway).collect(Collectors.toSet());

		params.put("payways", payways);

		int retain = SysUtil.getSysConfigRetain();

		for (String accountType : accountTypes) {

			params.put("accountType", accountType);

			List<Map<String, String>> receiptStatics = super.baseMapper.selectReceiptStatics(params, page);

			Map<String, List<Map<String, String>>> record = new HashMap<>();

			record.put(accountType, receiptStatics);

			records.add(record);
		}

		int total = super.baseMapper.selectReceiptStaticsCount(params);
		page.setRecords(records);
		page.setTotal(total);
		return page;
	}

	@Override
	public List<Integer> getLastSurplusCustIds(Map<String, Object> params) {
		return dealDetailMapper.selectLastSurplusCustIds(params);
	}

	@Override
	public CommonReceiptSummation getReceiptSelectiveSummation(Map<String, Object> params) {

		SysConfig sysConfig = (SysConfig) jsonRedisTemplate.opsForValue().get(Constant.SYS_CONFIG_IDENTIFIER + SysUtil.getEmpIdInRedis());

		Integer retain = sysConfig.getRetain();

		CommonReceiptSummation commonReceiptSummation = super.baseMapper.selectReceiptSelectiveSummation(params);
		BigDecimal receiptSum = new BigDecimal(commonReceiptSummation.getReceiptSum());
		receiptSum = receiptSum.setScale(retain, RoundingMode.HALF_UP);
		commonReceiptSummation.setReceiptSum(receiptSum.toString());
		return commonReceiptSummation;
	}


}
