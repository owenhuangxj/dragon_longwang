package com.trenska.longwang.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trenska.longwang.constant.Constant;
import com.trenska.longwang.dao.indent.IndentMapper;
import com.trenska.longwang.entity.customer.Customer;
import com.trenska.longwang.entity.indent.Indent;
import com.trenska.longwang.entity.indent.IndentDetail;
import com.trenska.longwang.entity.stock.Stock;
import com.trenska.longwang.entity.stock.StockDetail;
import com.trenska.longwang.enums.IndentStat;
import com.trenska.longwang.enums.PaymentStat;
import com.trenska.longwang.model.sys.ResponseModel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 2019/4/24
 * 创建人:Owen
 */
public class IndentUtil {

	public static String getIndentNo(String indentType , String prefix , IndentMapper indentMapper) {

		// 查询订货单表中的最后一条记录
		Indent indent = indentMapper.selectRecordOfMaxId(indentType);

		String indentNo = "";
		if(indent == null || (indent != null && !new SimpleDateFormat("yyyyMMdd").format(new Date()).equals(BillsUtil.getDate(indent.getIndentNo())))){
			indentNo = BillsUtil.makeBillNo(prefix,1);
		}else{
			indentNo = indent.getIndentNo();
			Integer num = BillsUtil.getSerialNumber(indentNo) ;
			indentNo = BillsUtil.makeBillNo(prefix,num + 1);
		}
		return indentNo;
	}

	/**
	 * 处理订货单信息
	 * 保存订货单商品信息
	 * 处理订单数据 :
	 * 	订货单总金额 odrAmnt 订货单金额合计:总金额之和
	 * 	数量合计 sum
	 * 	赠品数量之和 giftSum
	 * 	收款金额合计 indentTotal
	 * 	优惠金额合计 discountTotal
	 */
	public static Indent saveIndentInfos(Indent indent) {

		String indentNo = indent.getIndentNo();

		// 订单的商品总数量 ->与单位无关，单纯的数量合计，方便前端显示
		Integer sum = 0;
		// 赠品总数量
		Integer giftSum = 0;
		// 收款金额合计:扣除扣点的金额合计
		BigDecimal indentTotal = new BigDecimal(0);
		// 优惠金额合计:金额*数量-收款金额合计
		BigDecimal discountTotal = new BigDecimal(0);

		List<IndentDetail> indentDetails = indent.getIndentDetails();

		/***************************************************************************************************************/
		for (IndentDetail indentDetail : indentDetails) {
			// 本品
			if (indentDetail.getIsGift() == false) {
				// 单一商品收款金额
				BigDecimal sglGoodsIndentTotal = new BigDecimal(indentDetail.getPrice()).multiply(new BigDecimal(indentDetail.getNum()));
				// 扣点金额
				BigDecimal dicountAmount = sglGoodsIndentTotal.multiply(new BigDecimal(indentDetail.getDiscount())).divide(new BigDecimal(100));
				indentDetail.setDiscountAmount(dicountAmount.toString());
				//增加扣点金额合计
				discountTotal = discountTotal.add(dicountAmount);
				// 收款金额 = 总额 - 优惠金额
				sglGoodsIndentTotal = sglGoodsIndentTotal.subtract(dicountAmount);
				// 增加收款金额合计
				indentTotal = indentTotal.add(sglGoodsIndentTotal);
				// 设置扣点金额
				indentDetail.setAmount(sglGoodsIndentTotal.toString());
				// 商品本品总数量(主单位) +
				sum += indentDetail.getNum() * indentDetail.getMulti();
			} else { // 赠品
//				indentDetail.setRemarks("满赠活动赠品");
				// 赠品总数量(主单位)+
				giftSum += indentDetail.getNum() * indentDetail.getMulti();
			}
			indentDetail.setIndentNo(indentNo);
			indentDetail.setEmpId(indent.getEmpId());
			indentDetail.insert();
		}
		indent.setSum(sum);
		indent.setGiftSum(giftSum);
		indent.setIndentTotal(indentTotal.toString());
		indent.setDiscountTotal(discountTotal.toString());
		indent.setOdrAmnt(indentTotal.add(discountTotal).toString());

		return indent;
	}

	/**
	 * 处理订货单状态 : 订单状态的判断与处理
	 * @param indent
	 * @param indent
	 * @return
	 */
	public static ResponseModel refreshIndent(Indent indent){
		if(Objects.isNull(indent)){
			return ResponseModel.getInstance().succ(false).msg("订货单不能为空");
		}

		String stat = indent.getStat();
		String receiptStat = indent.getReceiptStat();
		boolean audited = indent.getAuditStat();
		boolean finished = IndentStat.FINISHED.getName().equals(stat);
		if(finished){
			return ResponseModel.getInstance().succ(false).msg(Constant.INDENT_FORBIDDEN);
		}

		boolean stockouted = IndentStat.STOCKOUTED.getName().equals(stat);
		boolean received = PaymentStat.RECEIPRED.getName().equals(receiptStat);

		BigDecimal indentTotal = new BigDecimal(indent.getIndentTotal());

		BigDecimal payedAmnt = new BigDecimal(indent.getPayedAmnt());

		BigDecimal receivedAmnt = new BigDecimal(indent.getReceivedAmnt());

		BigDecimal iouAmnt = new BigDecimal(indent.getIouAmnt());

		BigDecimal total = receivedAmnt.add(payedAmnt).add(iouAmnt);

		String indentNo = indent.getIndentNo();

		// 处理是否可以审核
		// 如果已付款+已收款+欠条 >=应收款-->设置可财审
		if(total.compareTo(indentTotal) >= 0){
			indent.setAuditable(true);
		}else{
			// 如果发生的款项总额已经小于应收总额并且已经审核，退回审核状态为未审核,并且不可审核
			if(audited){
				indent.setAuditStat(false);
			}
			indent.setAuditable(false);
		}
		// 处理收款状态
		// 如果已付款+已收款 == 应收款 -->状态变成已收款
		if(receivedAmnt.add(payedAmnt).compareTo(indentTotal) == 0){
			indent.setReceiptStat(PaymentStat.RECEIPRED.getName());
		// 如果已付款+已收款 < 应收款 -->状态变成未收款
		}else if(receivedAmnt.add(payedAmnt).compareTo(indentTotal) < 0){
			indent.setReceiptStat(PaymentStat.WAIT_RECEIPT.getName());
		}

		indent.updateById();
		return ResponseModel.getInstance().succ(true).msg("successful");
	}

	/**
	 * 处理订货单的状态
	 * @param indent
	 */
	public static boolean handleIndentStat(Indent indent) {

		BigDecimal indentTotal = new BigDecimal(indent.getIndentTotal());
		BigDecimal iouAmnt = new BigDecimal(indent.getIouAmnt());
		BigDecimal payedAmnt = new BigDecimal(indent.getPayedAmnt());
		BigDecimal receivedAmnt = new BigDecimal(indent.getReceivedAmnt());

		String stat = indent.getStat();
		Boolean audited = indent.getAuditStat();
		String receiptStat = indent.getReceiptStat();
		boolean stockouted = IndentStat.STOCKOUTED.getName().equals(stat);

		boolean received = PaymentStat.RECEIPRED.getName().equals(receiptStat);
		// 如果状态不为已收款-->比较已收款+已付款 == 应收款
		received = received || indentTotal.compareTo(receivedAmnt.add(payedAmnt)) == 0 ;

		if(true == received){
			indent.setAuditable(true);
		}else{
			BigDecimal total = receivedAmnt.add(payedAmnt).add(iouAmnt);
			// 如果状态不为已收款-->比较已收款+已付款+欠条金额 >= 应收款
			boolean auditable = total.compareTo(indentTotal) >= 0;
			indent.setAuditable(auditable);
		}
		if(true == audited && true == received && true == stockouted){
			indent.setStat(IndentStat.FINISHED.getName());
		}

		return indent.updateById();
	}

	/**
	 * 核准修改订货单->任意修改
	 */
	public static void changeIndent(Indent indent, Indent dbIndent , int empId){

		Integer custId = dbIndent.getCustId();
		String oldStockoutStat = dbIndent.getStat();
		String indentNo = dbIndent.getIndentNo();

		// 应收金额合计
		BigDecimal indentTotal = new BigDecimal(0);
		// 扣点金额合计
		BigDecimal discountTotal = new BigDecimal(0);
		// 本品数量合计
		int sum = 0 ;
		// 赠品数量合计
		int giftSum = 0;

		List<IndentDetail> indentDetails = indent.getIndentDetails();
		List<IndentDetail> dbIndentDetails = dbIndent.getIndentDetails();

		// 计算新订单优惠总额、应收总额和本品数量、赠品数量
		for (IndentDetail indentDetail : indentDetails) {
			BigDecimal discount = new BigDecimal(indentDetail.getDiscount());
			BigDecimal num = new BigDecimal(indentDetail.getNum());
			BigDecimal multi = new BigDecimal(indentDetail.getMulti());
			BigDecimal price = new BigDecimal(indentDetail.getPrice());
			BigDecimal discountAmnt = price.multiply(num).multiply(multi).multiply(discount).divide(new BigDecimal(100));
			BigDecimal amount = price.multiply(num).multiply(multi).subtract(discountAmnt);
			indentDetail.setAmount(amount.toString());
			indentDetail.setDiscountAmount(discountAmnt.toString());
			discountTotal = discountTotal.add(discountAmnt);
			indentTotal = indentTotal.add(amount);
			Boolean isGift = indentDetail.getIsGift();
			if(isGift){
				giftSum += num.multiply(multi).intValue();
			}else{
				sum += num.multiply(multi).intValue();
			}
		}
		// 删除数据库中旧的订货单商品信息
		new IndentDetail().delete(
				new LambdaQueryWrapper<IndentDetail>()
						.eq(IndentDetail::getIndentNo,indentNo)
		);

		// 旧的应收款总额
		String dbIndentTotal = dbIndent.getIndentTotal();
		// 旧的已收金额
		String dbReceivedAmnt = dbIndent.getReceivedAmnt();
		// 新订单的订单总额
		BigDecimal odrAmnt = indentTotal.add(discountTotal);

		indent.setSum(sum);
		indent.setEmpId(empId);
		indent.setGiftSum(giftSum);
		indent.setReceivedAmnt(dbReceivedAmnt);
		indent.setOdrAmnt(odrAmnt.toString());
		indent.setIouAmnt(dbIndent.getIouAmnt());
		indent.setIouStat(dbIndent.getIouStat());
		indent.setAuditable(dbIndent.getAuditable());
		indent.setAuditStat(dbIndent.getAuditStat());
		indent.setIndentTotal(indentTotal.toString());
		indent.setReceiptStat(dbIndent.getReceiptStat());
		indent.setDiscountTotal(discountTotal.toString());

		List<IndentDetail> valueCopyOfIndentDetails = new ArrayList<>();

		// addAll()只会复制地址
		// valueCopyOfIndentDetails.addAll(indent.getIndentDetails());

		indentDetails.forEach(indentDetail -> {
			IndentDetail detail = new IndentDetail();
			ObjectCopier.copyProperties(indentDetail,detail);
			valueCopyOfIndentDetails.add(detail);
		});

		// 由于需要统计和去重，而商品实际数量是num*multi,所以需要将num*multi 移除到num属性中去然后再对num进行统计
		valueCopyOfIndentDetails.forEach(indentDetail -> indentDetail.setNum(indentDetail.getNum() * indentDetail.getMulti()));
		// 统计商品数量
		Map<Integer, Integer> indentGoodsIdNumMap = valueCopyOfIndentDetails.stream().collect(Collectors.groupingBy(IndentDetail::getGoodsId, Collectors.summingInt(IndentDetail::getNum)));

		//去重商品
		ArrayList<IndentDetail> uniqueGoodsIdIndentDetails = valueCopyOfIndentDetails.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(o -> o.getGoodsId()))), ArrayList::new));

		// 去重数据库商品
		ArrayList<IndentDetail> uniqueDbGoodsIdIndentDetails = dbIndentDetails.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(o -> o.getGoodsId()))), ArrayList::new));

		indentGoodsIdNumMap.forEach((key,value)->
			uniqueGoodsIdIndentDetails.forEach(uniqueGoodsIdIndentDetail->{
				if (uniqueGoodsIdIndentDetail.getGoodsId() == key){
					uniqueGoodsIdIndentDetail.setNum(value);
				}
			})
		);

		// 从数据库中获取商品的出库信息
		List<StockDetail> stockoutDetails = new StockDetail().selectList(
				new LambdaQueryWrapper<StockDetail>()
						.eq(StockDetail::getBusiNo,indentNo)
		);

		List<StockDetail> dbStockoutDetails = new ArrayList<>();

		stockoutDetails.forEach(stockoutDetail->{
			StockDetail stockDetail = new StockDetail();
			ObjectCopier.copyProperties(stockoutDetail,stockDetail);
			dbStockoutDetails.add(stockDetail);
		});

		// 统计每一个商品的出库量
		Map<Integer, Integer> goodsStockoutHistoryMap =
				stockoutDetails.stream().collect(Collectors.groupingBy(StockDetail::getGoodsId, Collectors.summingInt(StockDetail::getHistory)));

		// 去重出库商品
		ArrayList<StockDetail> uniqueGoodsIdStockDetails =
				stockoutDetails.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(o -> o.getGoodsId()))), ArrayList::new));

		goodsStockoutHistoryMap.forEach((key,value)->
			uniqueGoodsIdStockDetails.forEach(uniqueGoodsIdStockDetail->{
				if(uniqueGoodsIdStockDetail.getGoodsId() == key){
					uniqueGoodsIdStockDetail.setHistory(value); // 将每一个商品的出库数量赋值到history属性中方便比较
				}
			})
		);

		// 获取减少的商品 ->旧订货单商品详情记录中有 新订货单中没有的商品
		List<Integer> uniqueGoodsIds = valueCopyOfIndentDetails.stream().collect(
				Collectors.collectingAndThen(
						Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(IndentDetail::getGoodsId))), ArrayList::new
				)
		).stream().map(IndentDetail::getGoodsId).collect(Collectors.toList());
		List<IndentDetail> deletedIndentDetails =
				uniqueDbGoodsIdIndentDetails.stream().filter(uniqueDbGoodsIdIndentDetail -> !uniqueGoodsIds.contains(uniqueDbGoodsIdIndentDetail.getGoodsId())).collect(Collectors.toList());

		// 获取增加的商品 ->新订货单中有，旧订货单中没有的商品
		List<Integer> uniqueDbGoodsIds = dbIndentDetails.stream().collect(
				Collectors.collectingAndThen(
						Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(IndentDetail::getGoodsId))), ArrayList::new
				)
		).stream().map(IndentDetail::getGoodsId).collect(Collectors.toList());
		List<IndentDetail> newAddedIndentDetails =
				uniqueGoodsIdIndentDetails.stream().filter(uniqueGoodsIdIndentDetail -> !uniqueDbGoodsIds.contains(uniqueGoodsIdIndentDetail.getGoodsId())).collect(Collectors.toList());


		// 出库记录中的商品id集合
		// List<Integer> uniqueStockGoodsIds = uniqueGoodsIdStockDetails.stream().map(StockDetail::getGoodsId).collect(Collectors.toList());

		// 删除已经被移除的商品的出库记录，还回库存
		if(CollectionUtils.isNotEmpty(deletedIndentDetails)){
			uniqueGoodsIdIndentDetails.removeAll(deletedIndentDetails); // 移除已经删除的商品->不需要再比较库存，直接删除==>作废库存记录
			List<Integer> deletedGoodsIds = deletedIndentDetails.stream().map(IndentDetail::getGoodsId).collect(Collectors.toList());

			// 找出已经被删除了的商品的出库记录->这里是统计(按照商品id统计出库数量)后的出库记录，不包括每一条出库记录，所以不能通过统计后的出库记录的detailId来删除==>作废
			List<StockDetail> deletedStockDetails =
					uniqueGoodsIdStockDetails.stream().filter(uniqueGoodsIdStockDetail -> deletedGoodsIds.contains(uniqueGoodsIdStockDetail.getGoodsId())).collect(Collectors.toList());

			// 移除被删除商品的出库记录
			stockoutDetails.removeAll(deletedStockDetails);

			/********************************************保存库存明细**********************************************/

			List<StockDetail> stockDetails = new StockDetail().selectList(
					new LambdaQueryWrapper<StockDetail>()
							.eq(StockDetail::getBusiNo, indentNo)
							.in(StockDetail::getGoodsId, deletedGoodsIds)
			);

			stockDetails.forEach(stockDetail -> {
				String stockType = stockDetail.getStockType();
				stockType = stockType.concat(Constant.ZF);
				stockDetail.setStockType(stockType);
				StockDetailsUtil.saveStockDetails(stockDetail);
			});

			// 删除已经被移除的商品的出库记录 -> 只能以此方式进行删除，因为是多批次出库，而这里的出库记录是统计后的数据所以必须是以商品id和订单no进行匹配
			new StockDetail().delete(
					new LambdaQueryWrapper<StockDetail>()
							.eq(StockDetail::getBusiNo,indentNo)
							.in(StockDetail::getGoodsId,deletedGoodsIds)
			);

			// 还回库存，保存一条还回库存的记录
			deletedStockDetails.forEach(stockDetail -> StockUtil.returnStock(stockDetail,empId));
		}

		// 移除新增的商品->新增的商品没有出库记录
		if(CollectionUtils.isNotEmpty(newAddedIndentDetails)){
			uniqueGoodsIdIndentDetails.removeAll(newAddedIndentDetails);
		}

		// 处理改变了数量的商品 数量改变并且有出库记录
		if(CollectionUtils.isNotEmpty(uniqueGoodsIdIndentDetails) && CollectionUtils.isNotEmpty(uniqueGoodsIdStockDetails)){
			for(IndentDetail indentDetail : uniqueGoodsIdIndentDetails){
				Integer goodsId = indentDetail.getGoodsId();
				List<StockDetail> stockDetails = dbStockoutDetails.stream().filter(stockoutDetail -> stockoutDetail.getGoodsId() == goodsId).collect(Collectors.toList());
				// 商品历史出库数量
				int stockoutHistory = stockDetails.stream().mapToInt(StockDetail::getHistory).sum();

				Integer indentDetailNum = indentDetail.getNum();

				if(stockoutHistory > indentDetailNum){
					indent.setStat(IndentStat.WAIT_STOCKOUT.getName()); // 订单状态重置为待出库
					// 将商品id相同的indentDetails中的商品出库量修改为0
					for (IndentDetail detail : indentDetails) {
						if(detail.getGoodsId() == goodsId){
							detail.setStockout(0);
						}
					}
					// 删除出库记录
					new StockDetail().delete(
							new LambdaQueryWrapper<StockDetail>()
									.eq(StockDetail::getBusiNo,indentNo)
									.eq(StockDetail::getGoodsId,goodsId)
					);
					stockDetails.forEach(stockDetail -> {
						stockDetail.setStockType(stockDetail.getStockType().concat(Constant.ZF));
						// 还回库存并保存库存明细
						StockUtil.returnStock(stockDetail,empId);
						// 移除出库记录
						stockoutDetails.remove(stockDetail);
					});
				}
			}
		}

		// 从数据库中获取商品的出库记录
		List<Stock> stocks = new Stock().selectList(
				new LambdaQueryWrapper<Stock>()
						.eq(Stock::getBusiNo, indentNo)
		);

		// 比较t_stock中有而t_stock_detail中没有的stock_no,如果存在则需要删除t_stock中的对应记录
		List<String> stockNos1 = stocks.stream().map(Stock::getStockNo).collect(Collectors.toList());
		List<String> stockNos2 = stockoutDetails.stream().map(StockDetail::getStockNo).collect(Collectors.toList());
		stockNos1.removeAll(stockNos2);

		//删除出库记录
		if(CollectionUtils.isNotEmpty(stockNos1)){
			new Stock().delete(
					new LambdaQueryWrapper<Stock>()
							.in(Stock::getStockNo,stockNos1)
			);
		}

		int stockoutSum = stockoutDetails.stream().mapToInt(StockDetail::getHistory).sum();

		/** 如果减少商品种类并且订单修改前已经出库完成
		 * 	如果只是减少商品种类并且所有商品都已经出库就会出现修改后状态为已出库的情况
		 * */
		String nameNo = StringUtil.makeNameNo(Constant.DHD_CHINESE,indentNo);
		String currentTime = TimeUtil.getCurrentTime(Constant.TIME_FORMAT);
		Customer dbCustomer = new Customer(custId).selectById();
		String oldDebt = dbCustomer.getDebt();
		if(sum + giftSum == stockoutSum){
			indent.setStat(IndentStat.STOCKOUTED.getName());
			// 如果旧订单已经出库完成并且新订单也出库完成(现在的需求是出库之后才能核准修改)，客户欠款修改为新的欠款
			if(oldStockoutStat.equals(IndentStat.STOCKOUTED.getName())){
				// 新的应收款和旧的应收款之差 新-旧 为负数
				BigDecimal difference = indentTotal.subtract(new BigDecimal(dbIndentTotal));
				// 如果两个应收款的差不为0则需要更新客户欠款
				if(difference.compareTo(new BigDecimal(0)) != 0){
					CustomerUtil.addCustomerDebt(custId,difference);
				}
				BigDecimal newDebt = new BigDecimal(oldDebt).add(difference);

				// 如果是减少商品总类，并且订单修改之前已经出库完成，需要记录一条交易明细
				DealDetailUtil.saveDealDetail(custId,nameNo,currentTime,difference.toPlainString(),newDebt.toString(),Constant.DHD_ZF_CHINESE_CHANGE,"","");
				indent.setSalesTime(currentTime); // 更新销售时间
			}
		}else if(sum + giftSum > stockoutSum){
			// 如果旧订单已经出库完成并且新订单未出库完成，那么交易明细会产生一条记录，客户欠款需要减去旧订单的欠款，因为在订单出库完成时对客户做了记账，客户欠款+，
			// 这里状态变成未出库需要减少客户欠款，否则客户的欠款就会一直添加；同时要增加一条客户欠款减少的交易明细
			if(oldStockoutStat.equals(IndentStat.STOCKOUTED.getName())){
				BigDecimal amount = new BigDecimal(dbIndent.getIndentTotal());
				String newDebt = new BigDecimal(oldDebt).subtract(amount).toString();
				CustomerUtil.subtractCustomerDebt(custId,oldDebt,amount);
				// 增加交易明细-->客户欠款减少
				DealDetailUtil.saveDealDetail(custId,nameNo,currentTime,"-".concat(amount.toString()),newDebt,Constant.DHD_ZF_CHINESE_CHANGE,"","");
			}
			indent.setStat(IndentStat.WAIT_STOCKOUT.getName());
			indent.setSalesTime(null); // 销售时间置空
		}
		/********************************************* 处理收款状态 *********************************************/
		// 已收+已付
		BigDecimal receivedAndPayedAmnt = new BigDecimal(dbIndent.getReceivedAmnt()).add(new BigDecimal(dbIndent.getPayedAmnt()));

		if(indentTotal.compareTo(receivedAndPayedAmnt) > 0){ // 已收+已付 < 应收 --> 待收款
			indent.setReceiptStat(PaymentStat.WAIT_RECEIPT.getName());
		}else{
			indent.setReceiptStat(PaymentStat.RECEIPRED.getName()); // 已收+已付 >= 应收 --> 已收款
		}

		indent.updateById();
		indentDetails.forEach(indentDetail -> {
			indentDetail.setEmpId(empId);
			indentDetail.setIndentNo(indentNo);
			indentDetail.insert();
		});
	}


}