package com.trenska.longwang.service.impl.financing;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trenska.longwang.constant.DragonConstant;
import com.trenska.longwang.dao.financing.ReceiptMapper;
import com.trenska.longwang.entity.customer.Customer;
import com.trenska.longwang.entity.financing.DealDetail;
import com.trenska.longwang.entity.financing.Receipt;
import com.trenska.longwang.entity.indent.Indent;
import com.trenska.longwang.enums.IndentStat;
import com.trenska.longwang.enums.PaymentStat;
import com.trenska.longwang.model.finaning.ReceiptModel;
import com.trenska.longwang.model.sys.CommonResponse;
import com.trenska.longwang.service.financing.IDealDetailService;
import com.trenska.longwang.service.financing.IReceiptService;
import com.trenska.longwang.util.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.trenska.longwang.constant.DragonConstant.FKD_CHINESE;

/**
 * 2019/4/19
 * 创建人:Owen
 */
@Service
public class ReceiptService  {
	@Autowired
	private ReceiptMapper receiptMapper;

	@Autowired
	private IDealDetailService dealDetailService;

	/**
	 * @param type 收款单/付款单
	 * @return
	 */
	private String getReceiptNo(String prefix, String type) {

		Receipt lastReceiptRecord = receiptMapper.selectRecordOfMaxId(type);

		String receiptNo;
		/**
		 * 处理流水号的问题
		 * 如果库存表中还没有任何记录，则需要生成第一个单号 或者 如果有记录，
		 * 需要比较最后一条记录的日期是否是当天，如果不是则流水号需要从1开始
		 */
		boolean isLastReceiptRecordNull = (lastReceiptRecord == null);
		//当前时间yyyyMMdd格式的日期字符串
		String currentDateStr = new SimpleDateFormat(DragonConstant.BILL_TIME_FORMAT).format(new Date());
		String dbReceiptNo;
		boolean isTwoDateEquals = false;
		if (!isLastReceiptRecordNull) {
			dbReceiptNo = lastReceiptRecord.getReceiptNo();
			String dbReceiptNoDateStr = BillsUtil.getDateOfBillNo(Optional.of(dbReceiptNo));
			isTwoDateEquals = currentDateStr.equals(dbReceiptNoDateStr);
		}

		if (isLastReceiptRecordNull || !isTwoDateEquals) {
			receiptNo = BillsUtil.makeBillNo(prefix, 1);
		} else {
			// 如果有记录并且最后一条记录的日期是当天，则流水号为最大值 + 1
			// 首先获取最后一条库存的单号
			receiptNo = lastReceiptRecord.getReceiptNo();
			int num = BillsUtil.getSerialNumberOfBillNo(Optional.of(receiptNo)) + 1;
			// 通过最后一条的库存单号生成新的库存单号
			receiptNo = BillsUtil.makeBillNo(prefix, num);
		}
		return receiptNo;
	}

	/**
	 * 批量作废收/付款单 ->作废收款单，客户欠款增加
	 *
	 * @param receipts
	 * @param dbCustomer
	 * @return
	 */
	public boolean cancelReceipts(List<Receipt> receipts, Customer dbCustomer) {
		BigDecimal increasingDebt = new BigDecimal(dbCustomer.getDebt());
		Integer custId = dbCustomer.getCustId();
		for (Receipt receipt : receipts) {
			BigDecimal amount = new BigDecimal(receipt.getReceiptAmount());
			DealDetail insertingDealDetail = new DealDetail();
			insertingDealDetail.setCustId(custId);
			String nameNo = StringUtil.makeNameNo(receipt.getType(), receipt.getReceiptNo());
			insertingDealDetail.setNameNo(nameNo);
			insertingDealDetail.setAmount(DragonConstant.PLUS + amount.toString());
			insertingDealDetail.setNewDebt(increasingDebt.toString());
			increasingDebt = increasingDebt.add(amount);
			insertingDealDetail.setNewDebt(increasingDebt.toString());
			insertingDealDetail.setOper(receipt.getAccountType().concat(DragonConstant.ZF));
			insertingDealDetail.setPayway(receipt.getPayway());
			insertingDealDetail.setTime(TimeUtil.getCurrentTime(DragonConstant.TIME_FORMAT));
			insertingDealDetail.insert(); // 插入一条交易明细
			new Receipt(receipt.getReceiptId(), false).updateById(); // 作废收款单
		}
		Customer updatingCustomer = new Customer();
		updatingCustomer.setCustId(custId);
		updatingCustomer.setDebt(increasingDebt.toString());
		updatingCustomer.updateById(); // 更新客户欠款
		return true;
	}

	/**
	 * 批量收/付款
	 *
	 * @param receipt
	 * @param billType    订货单/退货单
	 * @param receiptType 收款/付款
	 * @return
	 */
	public synchronized CommonResponse saveReceipt(Receipt receipt, String billType, String receiptType) {
		// 获取收/付款金额和收/付款方式集合
		//Set<ReceiptModel> receiptSet = receipt.getReceiptSet();

		for (ReceiptModel receiptModel : receipt.getReceiptSet()) {
			if (!StringUtil.isNumeric(receiptModel.getReceiptAmount(), false)) {
				return CommonResponse.getInstance().succ(false).msg("收款中包含无效的金额！");
			}
		}
		int custId = receipt.getCustId();
		//更新客户表  客户欠款减少
		// 获取客户信息
		Customer customer = new Customer().selectById(custId);

		// 获取客户欠款
		BigDecimal oldDebt = new BigDecimal(customer.getDebt());
		//收/付款总额
		BigDecimal currentReceived = BigDecimal.ZERO;
		BigDecimal currentPayed = BigDecimal.ZERO;

		// 计算收/付款总额
		BigDecimal amount = receipt.getReceiptSet().stream().map(ReceiptModel::getReceiptAmount)
				.map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add);
		if (DragonConstant.SK_CHINESE.equals(receiptType)) {
			currentReceived = currentReceived.add(amount);
		} else if (DragonConstant.FK_CHINESE.equals(receiptType)) {
			currentPayed = currentPayed.add(amount);
		}

		String busiNo = StringUtils.trim(receipt.getBusiNo());
		Indent dbIndent = null;
		if (StringUtils.isNotEmpty(busiNo)) {
			//订货单信息
			dbIndent = new Indent().selectOne(
					new LambdaQueryWrapper<Indent>()
							.eq(Indent::getIndentNo, busiNo)
			);
		}
		//判断是否是对关联订货单进行收款
		//新建收款单 有两种方式 1 手动新建  2 通过订货单号新建
		//对订货单进行收款--> 需要 到客户表debt-本次收/付款数  订货单表(received_amnt + 收款金额 )
		if (null != dbIndent) {
			//订单状态
			String stat = dbIndent.getStat();

			if (IndentStat.WAIT_CONFIRM.getName().equals(stat)) {
				return CommonResponse.getInstance().succ(false).msg("订单未审核！");
			}
			if (IndentStat.CANCELLED.getName().equals(stat)) {
				return CommonResponse.getInstance().succ(false).msg("订单已取消！");
			}
			if (IndentStat.INVALID.getName().equals(stat)) {
				return CommonResponse.getInstance().succ(false).msg("订单已作废！");
			}
			dealWithIndent(currentReceived, currentPayed, dbIndent);
		}

		BigDecimal newDebt = doSaveReceipt(receipt, receiptType, oldDebt);
		// 多个收/付款->但是客户表更新一次即可
		new Customer(custId, newDebt.toString()).updateById();
		return CommonResponse.getInstance().succ(true).msg(billType + receiptType.concat("成功！"));
	}

	/**
	 * 批量付款：客户欠款减少
	 *
	 * @param receipt Receipt
	 * @return CommonResponse
	 */
	public synchronized CommonResponse savePayment(Receipt receipt) {
		for (ReceiptModel receiptModel : receipt.getReceiptSet()) {
			if (!StringUtil.isNumeric(receiptModel.getReceiptAmount(), false)) {
				return CommonResponse.getInstance().succ(false).msg("付款中包含无效的金额！");
			}
		}
		int custId = receipt.getCustId();
		Customer customer = new Customer().selectById(custId);
		// 获取客户欠款
		BigDecimal oldDebt = new BigDecimal(customer.getDebt());

		Indent dbIndent = null;
		if (StringUtils.isNotEmpty(StringUtils.trim(receipt.getBusiNo()))) {
			//订货单信息
			dbIndent = new Indent().selectOne(
					new LambdaQueryWrapper<Indent>()
							.eq(Indent::getIndentNo, StringUtils.trim(receipt.getBusiNo()))
			);
		}
		//判断是否是对关联订货单进行付款
		//新建付款单 有两种方式 1 手动新建  2 通过订货单号新建
		//对订货单进行收款--> 需要 到客户表debt-本次收/付款数  订货单表(received_amnt + 收款金额 )
		if (null != dbIndent) {
			//订单状态
			String stat = dbIndent.getStat();
			if (IndentStat.WAIT_CONFIRM.getName().equals(stat)) {
				return CommonResponse.getInstance().succ(false).msg("订单未审核！");
			}
			if (IndentStat.INVALID.getName().equals(stat)) {
				return CommonResponse.getInstance().succ(false).msg("订单已作废！");
			}
			if (IndentStat.CANCELLED.getName().equals(stat)) {
				return CommonResponse.getInstance().succ(false).msg("订单已取消！");
			}
		}
		BigDecimal newDebt = doSavePayment(receipt);

		// 多个收/付款->但是客户表更新一次即可
		new Customer(custId, newDebt.toString()).updateById();

		return CommonResponse.getInstance().succ(true).msg("billType".concat("付款成功！"));
	}

	private BigDecimal doSaveReceipt(Receipt receipt, String receiptType, BigDecimal oldDebt) {
		/**
		 * 	一次多个收/付款
		 * 		1.一个收/付款一个单号
		 * 		2.每个收/付款时间相同
		 */
		// 收/付款单号
		// 默认为收款单
		String title = DragonConstant.SK_TITLE;
		if (DragonConstant.FK_CHINESE.equals(receiptType)) {
			title = DragonConstant.FK_TITLE;
		}
		String receiptNo = getReceiptNo(title, receiptType.concat("单"));
		int startNumber = BillsUtil.getSerialNumberOfBillNo(Optional.of(receiptNo));
		// 收/付款时间
		String currentTime = TimeUtil.getCurrentTime(DragonConstant.TIME_FORMAT);
		BigDecimal newDebt = oldDebt;
		Set<ReceiptModel> receiptModels = receipt.getReceiptSet();
		int empIdInToken = SysUtil.getEmpIdInToken();
		for (ReceiptModel receiptModel : receiptModels) {
			String thisReceiptAmount = receiptModel.getReceiptAmount();
			Receipt insertingReceipt = new Receipt();
			// 复制公共信息
			ObjectCopier.copyProperties(receipt, insertingReceipt);
			insertingReceipt.setEmpId(empIdInToken);
			insertingReceipt.setStat(true); // 收/付款单 新建后 直接完成
			// 收/付款方式
			insertingReceipt.setPayway(receiptModel.getPayway());
			insertingReceipt.setReceiptNo(receiptNo);
			insertingReceipt.setCreateTime(currentTime); // 创建收款单的时间为系统时间
			insertingReceipt.setReceiptTime(currentTime); // 龙旺的收款时间和创建时间一样，都是系统时间
			//收/付款金额
			insertingReceipt.setReceiptAmount(thisReceiptAmount);
			insertingReceipt.insert(); // 保存收/付款记录
			// 保存交易明细,客户欠款减少 加 - 前缀 ->每个收/付款单号都对应一个交易明细
			String oper = receipt.getAccountType();
			String amount = DragonConstant.MINUS.concat(receiptModel.getReceiptAmount());
			String nameNo = StringUtil.makeNameNo(receipt.getType(), receiptNo);
			String payway = receiptModel.getPayway();
			String remarks = receipt.getReceiptRemarks();
			newDebt = newDebt.subtract(new BigDecimal(thisReceiptAmount));
			DealDetailUtil.saveDealDetail(receipt.getCustId(), nameNo, currentTime, amount, newDebt.toString(), oper, payway, remarks);
			++startNumber;
			receiptNo = BillsUtil.makeBillNo(title, startNumber);
		}
		return newDebt;
	}

	/**
	 * 一次多个付款
	 * 1.一个付款一个单号
	 * 2.每个付款时间相同
	 */
	private BigDecimal doSavePayment(Receipt receipt) {
		// 付款单号
		String title = DragonConstant.FK_TITLE;
		String receiptNo = getReceiptNo(title, FKD_CHINESE);
		int startNumber = BillsUtil.getSerialNumberOfBillNo(Optional.of(receiptNo));
		// 付款时间
		String paymentTime = TimeUtil.getCurrentTime(DragonConstant.TIME_FORMAT);
		BigDecimal newDebt = receipt.getReceiptSet().stream().map(ReceiptModel::getReceiptAmount)
				.map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add); // 计算付款总额
		Set<ReceiptModel> receiptModels = receipt.getReceiptSet();
		List<DealDetail> dealDetails = new ArrayList<>();
		List<Receipt> receipts = new ArrayList<>();
		int empIdInToken = SysUtil.getEmpIdInToken();
		for (ReceiptModel receiptModel : receiptModels) {
			String thisReceiptAmount = receiptModel.getReceiptAmount();
			Receipt insertingReceipt = new Receipt();
			// 复制公共信息
			ObjectCopier.copyProperties(receipt, insertingReceipt);
			insertingReceipt.setStat(true); // 收/付款单 新建后 直接完成
			insertingReceipt.setEmpId(empIdInToken);
			insertingReceipt.setReceiptNo(receiptNo);
			insertingReceipt.setCreateTime(paymentTime); // 创建收款单的时间为系统时间
			insertingReceipt.setReceiptTime(paymentTime); // 龙旺的收款时间和创建时间一样，都是系统时间
			insertingReceipt.setPayway(receiptModel.getPayway());
			//收/付款金额
			insertingReceipt.setReceiptAmount(thisReceiptAmount);
			receipts.add(insertingReceipt);

			// 保存交易明细,客户欠款减少 加 - 前缀 ->每个收/付款单号都对应一个交易明细
			DealDetail dealDetail = new DealDetail();
			dealDetail.setCustId(receipt.getCustId());
			dealDetail.setTime(paymentTime);
			dealDetail.setOper(receipt.getAccountType());
			dealDetail.setPayway(receiptModel.getPayway());
			dealDetail.setRemarks(receipt.getReceiptRemarks());
			dealDetail.setNameNo(StringUtil.makeNameNo(receipt.getType(), receiptNo));
			dealDetail.setAmount(DragonConstant.MINUS.concat(receiptModel.getReceiptAmount()));
			dealDetail.setNewDebt(newDebt.subtract(new BigDecimal(thisReceiptAmount)).toString());
			dealDetails.add(dealDetail);
			receiptNo = BillsUtil.makeBillNo(title, startNumber);
		}
//		receiptService.saveBatch(receipts, DragonConstant.BATCH_INSERT_SIZE);
		dealDetailService.saveBatch(dealDetails, DragonConstant.BATCH_INSERT_SIZE);
		return newDebt;
	}

	@NotNull
	private void dealWithIndent(BigDecimal currentReceived, BigDecimal currentPayed, Indent dbIndent) {

		// 财审状态
		boolean audited = dbIndent.getAuditStat();

		// 历史已收金额
		BigDecimal historyReceivedAmnt = new BigDecimal(dbIndent.getReceivedAmnt());
		// 历史已付金额
		BigDecimal historyPayedAmnt = new BigDecimal(dbIndent.getPayedAmnt());

		// 订货单应收总额
		BigDecimal indentTotal = new BigDecimal(dbIndent.getIndentTotal());

		// 欠条金额
		BigDecimal iouAmnt = new BigDecimal(dbIndent.getIouAmnt());

		// 本次收款后的的收款总额
		BigDecimal totalReceivedAmnt = currentReceived.add(historyReceivedAmnt);

		// 本次付款后的的付款总额
		BigDecimal totalPayedAmnt = currentPayed.add(historyPayedAmnt);

		/* 如果收款额+收款额已经大于应收款额，返回错误 */
		//if (totalPayedAmnt.add(totalReceivedAmnt).compareTo(indentTotal) > 0) {
		//	return ResponseModel.getInstance().succ(false).msg("付款额+收款额不能大于应收款");
		//}

		Indent updatingIndent = new Indent();
		updatingIndent.setIndentId(dbIndent.getIndentId());
		updatingIndent.setPayedAmnt(totalPayedAmnt.toString());//已付金额
		updatingIndent.setReceivedAmnt(totalReceivedAmnt.toString());//已收金额

		// 如果已收总额 + 已付总额 >= 应收总额 --> 收款状态设置为已收款
		if (totalReceivedAmnt.add(totalPayedAmnt).compareTo(indentTotal) >= 0) {
			// 设置交账状态为true ，非交账的订货单不能通过[财审]
			updatingIndent.setIouStat(true);

			updatingIndent.setReceiptStat(PaymentStat.RECEIPRED.getName());
			if (audited) { //如果已审核状态为true
				// 如果收款完成、已出库完成并且已通过财务审核，则将订单状态设置为已完成
				if (IndentStat.STOCKOUTED.getName().equals(dbIndent.getStat())) {
					updatingIndent.setStat(IndentStat.FINISHED.getName());
				}
			} else { //如果已财审状态为false
				// 如果 已收款+已付款+欠条金额 >=订单总额 -->可以进行财务审核
				if (totalReceivedAmnt.add(totalPayedAmnt).add(iouAmnt).compareTo(indentTotal) >= 0) {
					updatingIndent.setAuditable(true);
				}
			}
		}
		// 更新订货单
		updatingIndent.updateById();
	}
}