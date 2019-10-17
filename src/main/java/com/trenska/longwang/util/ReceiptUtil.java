package com.trenska.longwang.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trenska.longwang.constant.Constant;
import com.trenska.longwang.dao.financing.ReceiptMapper;
import com.trenska.longwang.entity.customer.Customer;
import com.trenska.longwang.entity.financing.DealDetail;
import com.trenska.longwang.entity.financing.Receipt;
import com.trenska.longwang.entity.indent.Indent;
import com.trenska.longwang.enums.IndentStat;
import com.trenska.longwang.enums.PaymentStat;
import com.trenska.longwang.model.finaning.ReceiptModel;
import com.trenska.longwang.model.sys.ResponseModel;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 2019/4/19
 * 创建人:Owen
 */
public class ReceiptUtil {

	/**
	 * @param type
	 * 	入库；出库；
	 * @return
	 */
	public static String getReceiptNo(String prefix , String type , ReceiptMapper receiptMapper){

		Receipt lastReceiptRecord = receiptMapper.selectRecordOfMaxId(type);

		String receiptNo = "";
		/**
		 * 处理流水号的问题
		 * 如果库存表中还没有任何记录，则需要生成第一个单号 或者 如果有记录，需要比较最后一条记录的日期是否是当天，如果不是则流水号需要从1开始
		 */

		boolean isLastReceiptRecordNull = lastReceiptRecord == null;
		String formatDate = new SimpleDateFormat(Constant.DATE_FORMAT).format(new Date());
		String dbReceiptNo = "";
		boolean equals = false;
		if(ObjectUtils.isNotEmpty(lastReceiptRecord)) {
			dbReceiptNo = lastReceiptRecord.getReceiptNo();
			String dbReceiptNoDateStr = BillsUtil.getDate(dbReceiptNo);
			equals = formatDate.equals(dbReceiptNoDateStr);
		}

		if (isLastReceiptRecordNull || !equals) {
			receiptNo = BillsUtil.makeBillNo(prefix, 1);
		} else {
			// 如果有记录并且最后一条记录的日期是当天，则流水号为最大值 + 1
			// 首先获取最后一条库存的单号
			receiptNo = lastReceiptRecord.getReceiptNo();
			Integer num = BillsUtil.getSerialNumber(receiptNo) + 1;
			// 通过最后一条的库存单号生成新的库存单号
			receiptNo = BillsUtil.makeBillNo(prefix, num);
		}
		return receiptNo;
	}

	/**
	 * 批量作废收/付款单 ->作废收款单，客户欠款增加
	 * @param receipts
	 * @param dbCustomer
	 * @param nameNo 收款单-SKDxxx、订货单-DHDxxx
	 * @return
	 */
	public static boolean cancelReceipts(List<Receipt> receipts, Customer dbCustomer, String nameNo){
		BigDecimal increasingDebt = new BigDecimal(dbCustomer.getDebt());
		Integer custId = dbCustomer.getCustId();
		for(Receipt receipt : receipts){
			BigDecimal amount = new BigDecimal(receipt.getReceiptAmount());
			DealDetail insertingDealDetail = new DealDetail();
			insertingDealDetail.setCustId(custId);
			insertingDealDetail.setNameNo(nameNo);
			insertingDealDetail.setAmount("+" + amount.toString());
			insertingDealDetail.setNewDebt(increasingDebt.toString());
			increasingDebt = increasingDebt.add(amount);
			insertingDealDetail.setNewDebt(increasingDebt.toString());
			insertingDealDetail.setOper(receipt.getAccountType().concat(Constant.ZF));
			insertingDealDetail.setPayway(receipt.getPayway());
			insertingDealDetail.setTime(TimeUtil.getCurrentTime(Constant.TIME_FORMAT));
			insertingDealDetail.insert(); // 插入一条交易明细
			new Receipt(receipt.getReceiptId(),false).updateById(); // 作废收款单
		}
		Customer updatingCustomer = new Customer();
		updatingCustomer.setCustId(custId);
		updatingCustomer.setDebt(increasingDebt.toString());
		updatingCustomer.updateById(); // 更新客户欠款
		return true;
	}

	/**
	 * 批量收/付款
	 * @param receipt
	 * @param request
	 * @param receiptMapper
	 * @param billType 订货单/退货单
	 * @param receiptType 收款单/付款单
	 * @return
	 */
	public static ResponseModel saveReceipt(Receipt receipt, HttpServletRequest request, ReceiptMapper receiptMapper,String billType,String receiptType) {
		String msg = "";

		if(StringUtils.isNotEmpty(billType)){
			msg = billType;
		}

		// 获取收/付款金额和收/付款方式集合
		Set<ReceiptModel> receiptSet = receipt.getReceiptSet();

		Integer empIdInRedis = SysUtil.getEmpIdInRedis(request);

		Integer custId = receipt.getCustId();
		//更新客户表  客户欠款减少
		// 获取客户信息
		Customer customer = new Customer().selectById(custId);

		// 获取客户欠款
		BigDecimal oldDebt = new BigDecimal(customer.getDebt());
		//收/付款总额
		BigDecimal currentReceived = new BigDecimal(0);
		BigDecimal currentPayed = new BigDecimal(0);

		// 计算收/付款总额
		for(ReceiptModel receiptModel : receiptSet){
			BigDecimal receiptAmount = new BigDecimal(receiptModel.getReceiptAmount());
			if(Constant.SK_CHINESE.equals(receiptType)){
				currentReceived = currentReceived.add(receiptAmount);
			}else if(Constant.FK_CHINESE.equals(receiptType)) {
				currentPayed = currentPayed.add(receiptAmount);
			}
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
		//有订货单号收款单 需要 到客户表(newDebt)-欠款数  订货单表(received_amnt + 收款金额 )
		if(null != dbIndent){
			//订单状态
			String stat = dbIndent.getStat();

			if(IndentStat.WAIT_CONFIRM.getName().equals(stat)){
				return ResponseModel.getInstance().succ(false).msg("订单未审核");
			}
			if(IndentStat.CANCELLED.getName().equals(stat)){
				return ResponseModel.getInstance().succ(false).msg("订单已取消");
			}
			if(IndentStat.INVALID.getName().equals(stat)){
				return ResponseModel.getInstance().succ(false).msg("订单已作废");
			}
			// 财审状态
			Boolean audited = dbIndent.getAuditStat();

			// 历史已收金额
			BigDecimal historyReceivedAmnt = new BigDecimal(dbIndent.getReceivedAmnt());
			// 历史已付金额
			BigDecimal historyPayedAmnt = new BigDecimal(dbIndent.getPayedAmnt());

			// 订货单应收总额
			BigDecimal indentTotal = new BigDecimal(dbIndent.getIndentTotal());

			// 欠条金额
			BigDecimal iouAmnt = new BigDecimal(dbIndent.getIouAmnt());

			// 本次收款款后的的收款总额
			BigDecimal totalReceivedAmnt = currentReceived.add(historyReceivedAmnt);

			// 本次付款后的的付款总额
			BigDecimal totalPayedAmnt = currentPayed.add(historyPayedAmnt);

			/******************************* 如果收款额+收款额已经大于应收款额，返回错误*******************************/
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
				if(audited){ //如果已审核状态为true
					// 如果收款完成、已出库完成并且已通过财务审核，则将订单状态设置为已完成
					if (IndentStat.STOCKOUTED.getName().equals(stat)) {
						updatingIndent.setStat(IndentStat.FINISHED.getName());
					}
				}else{ //如果已财审状态为false
					// 如果 已收款+已付款+欠条金额 >=订单总额 -->可以进行财务审核
					if(totalReceivedAmnt.add(totalPayedAmnt).add(iouAmnt).compareTo(indentTotal) >= 0){
						updatingIndent.setAuditable(true);
					}
				}
			}
			// 更新订货单
			updatingIndent.updateById();
			msg = "订货单";
		}
		BigDecimal newDebt = oldDebt;
		// 一次多个收/付款
		for(ReceiptModel receiptModel : receiptSet){

			String thisReceiptAmount = receiptModel.getReceiptAmount();

			Receipt insertingReceipt = new Receipt();
			// 复制公共信息
			ObjectCopier.copyProperties(receipt,insertingReceipt);

			insertingReceipt.setStat(true); // 收/付款单 新建后 直接完成

			// 收/付款方式
			insertingReceipt.setPayway(receiptModel.getPayway());

			// 收/付款单号
			// 默认为收款单
			String title = Constant.SK_TITLE;
			if(Constant.FK_CHINESE.equals(receiptType)){
				title = Constant.FK_TITLE;
			}
			/***************************************************************************************************/
			String receiptNo = ReceiptUtil.getReceiptNo(title, receiptType, receiptMapper);
			insertingReceipt.setReceiptNo(receiptNo);

			insertingReceipt.setEmpId(empIdInRedis);

			// 收/付款时间
			String currentTime = TimeUtil.getCurrentTime(Constant.TIME_FORMAT);
			insertingReceipt.setCreateTime(currentTime); // 创建收款单的时间为系统时间
			insertingReceipt.setReceiptTime(currentTime); // 龙旺的收款时间和创建时间一样，都是系统时间
			//收/付款金额
			insertingReceipt.setReceiptAmount(thisReceiptAmount);

			insertingReceipt.insert(); // 保存收/付款记录

			// 保存交易明细,客户欠款减少 加 - 前缀 ->每个收/付款单号都对应一个交易明细
			String oper = receipt.getAccountType();
			String ammount = "-".concat(receiptModel.getReceiptAmount());
			String nameNo = StringUtil.makeNameNo(receipt.getType(), receiptNo);
//			if(!Objects.isNull(busiNo)){
//				nameNo = StringUtil.makeNameNo(Constant.DHD_CHINESE,busiNo);
//			}
			String payway = receiptModel.getPayway();
			String remarks = receipt.getReceiptRemarks();
			newDebt = newDebt.subtract(new BigDecimal(thisReceiptAmount));
			DealDetailUtil.saveDealDetail(custId,nameNo,currentTime,ammount,newDebt.toString(),oper,payway,remarks);

		}

		// 多个收/付款->但是客户表更新一次即可
		new Customer(custId,newDebt.toString()).updateById();

		return ResponseModel.getInstance().succ(true).msg(msg.concat(receiptType).concat("成功"));
	}
}
