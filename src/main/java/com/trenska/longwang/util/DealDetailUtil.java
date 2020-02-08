package com.trenska.longwang.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.trenska.longwang.constant.Constant;
import com.trenska.longwang.entity.financing.DealDetail;
import com.trenska.longwang.entity.financing.Receipt;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 2019/6/21
 * 创建人:Owen
 */
public class DealDetailUtil {

	/**
	 *
	 * @param custId 客户id
	 * @param nameNo
	 * @param time
	 * @param amount 变动量
	 * @param newDebt 客户当前欠款
	 * @param oper
	 * @return
	 */
	public static boolean saveDealDetail(Integer custId ,String nameNo, String time, String amount, String newDebt, String oper,String payway,String remarks){
		return new DealDetail(custId,nameNo,time,amount,newDebt,oper,payway,remarks).insert();
	}

	public static boolean saveDealDetail(Integer custId ,String nameNo, String time, String amount, String newDebt, String oper,String payway,String remarks,String auditRemarks){
		return new DealDetail(custId,nameNo,time,amount,newDebt,oper,payway,remarks,auditRemarks).insert();
	}

	/**
	 * 标注/修改订货单的财审备注
	 * @param auditRemarks
	 * @param indentNo
	 */

	public static void saveOrUpdateAuditRemarks(String auditRemarks, String indentNo) {

		String nameNo = StringUtil.makeNameNo(Constant.DHD_CHINESE,indentNo);

		List<DealDetail> dealDetails = new DealDetail().selectList(
				new LambdaQueryWrapper<DealDetail>()
						.eq(DealDetail::getNameNo, nameNo)

		);

		// 如果有欠款明细则将审核备注标注在最后一个欠款明细上
		if(CollectionUtils.isNotEmpty(dealDetails)){
			dealDetails.sort(Comparator.comparing(DealDetail::getTime));
			/********************************** 将审核备注标注在最后一个欠款明细上************************************/
			DealDetail lastDealDetail = dealDetails.get(dealDetails.size() - 1);
			lastDealDetail.setAuditRemarks(auditRemarks);
			lastDealDetail.updateById();

			dealDetails.remove(dealDetails.size() - 1);

			if(CollectionUtils.isNotEmpty(dealDetails)){
				List<Long> updatingDealDetailsIds = dealDetails.stream()
						.filter(dealDetail-> StringUtils.isNotEmpty(dealDetail.getAuditRemarks()))
						.map(DealDetail::getId).collect(Collectors.toList());

				if(CollectionUtils.isNotEmpty(updatingDealDetailsIds)){
					new DealDetail().update(
							new LambdaUpdateWrapper<DealDetail>()
									.in(DealDetail::getId,updatingDealDetailsIds)
					);
				}
			}
		}
	}

	/*public static void saveOrUpdateAuditRemarks(String auditRemarks, String indentNo) {

		String nameNo = StringUtil.makeNameNo(Constant.DHD_CHINESE,indentNo);
		List<DealDetail> dealDetails = new ArrayList<>();

		List<DealDetail> dealDetailsOfIndent = new DealDetail().selectList(
				new LambdaQueryWrapper<DealDetail>()
						.eq(DealDetail::getNameNo, nameNo)

		);

		// 如果在欠款明细里面找不到对应的记录代表还没有出库->出库完成才记账，查找是否已经收款
		if(CollectionUtils.isNotEmpty(dealDetailsOfIndent)) {
			dealDetails.addAll(dealDetailsOfIndent);
		}

		List<Receipt> receipts = new Receipt().selectList(
				new LambdaQueryWrapper<Receipt>()
						.eq(Receipt::getBusiNo, indentNo)
		);
		// 如果订货单有收款记录就在欠款明细中找出对应的记录
		if(CollectionUtils.isNotEmpty(receipts)){
			List<String> nameNosOfReceipt = new ArrayList<>();
			// 遍历拼接 name-no
			for (Receipt receipt : receipts) {
				nameNosOfReceipt.add(
						StringUtil.makeNameNo(receipt.getType(),receipt.getReceiptNo())
				);
			}
			// 查找订货单或收货单的欠款明细记录
			List<DealDetail> dealDetailsOfReceipt = new DealDetail().selectList(
					new LambdaQueryWrapper<DealDetail>()
							.in(DealDetail::getNameNo, nameNosOfReceipt)
			);
			if(CollectionUtils.isNotEmpty(dealDetailsOfReceipt)){
				dealDetails.addAll(dealDetailsOfReceipt);
			}
		}

		// 如果有欠款明细则将审核备注标注在最后一个欠款明细上
		if(CollectionUtils.isNotEmpty(dealDetails)){
			dealDetails.sort(Comparator.comparing(DealDetail::getTime));
			*//********************************** 将审核备注标注在最后一个欠款明细上************************************//*
			DealDetail lastDealDetail = dealDetails.get(dealDetails.size() - 1);
			lastDealDetail.setAuditRemarks(auditRemarks);
			lastDealDetail.updateById();

			dealDetails.remove(dealDetails.size() - 1);

			if(CollectionUtils.isNotEmpty(dealDetails)){
				List<Long> updatingDealDetailsIds = dealDetails.stream()
						.filter(dealDetail-> StringUtils.isNotEmpty(dealDetail.getAuditRemarks()))
						.map(DealDetail::getId).collect(Collectors.toList());

				if(CollectionUtils.isNotEmpty(updatingDealDetailsIds)){
					new DealDetail().update(
							new LambdaUpdateWrapper<DealDetail>()
									.in(DealDetail::getId,updatingDealDetailsIds)
					);
				}
			}
		}
	}*/
}