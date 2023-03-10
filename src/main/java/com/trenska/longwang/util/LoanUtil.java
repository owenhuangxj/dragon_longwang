package com.trenska.longwang.util;

import com.trenska.longwang.constant.DragonConstant;
import com.trenska.longwang.dao.financing.LoanMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 * 2019/7/30
 * 创建人:Owen
 */
public class LoanUtil {

	/**
	 * 生产调帐单no
	 */
	public static String getLoanNo(LoanMapper loanMapper) {

		String loanNoOfMaxId = loanMapper.getLastLoanNo();

		if (StringUtils.isEmpty(loanNoOfMaxId)){
			return BillsUtil.makeBillNo(DragonConstant.TZD_PREFIX,1);
		}
		String todayDate = TimeUtil.getCurrentTime(DragonConstant.BILL_TIME_FORMAT);
		String dateOfMaxLoanId = "";

		if (StringUtils.isNotEmpty(loanNoOfMaxId)){
			dateOfMaxLoanId = BillsUtil.getDateOfBillNo(Optional.of(loanNoOfMaxId));
		}

		String lastLoanNo = loanMapper.getLastLoanNo();
		String loanNo;

		boolean isLastLoanNoEmpty = StringUtils.isEmpty(lastLoanNo);

		if (isLastLoanNoEmpty || !todayDate.equals(dateOfMaxLoanId) ){
			loanNo = BillsUtil.makeBillNo(DragonConstant.TZD_PREFIX,1);
		}else {
			int serialNumber = BillsUtil.getSerialNumberOfBillNo(Optional.of(loanNoOfMaxId)) + 1;
			loanNo = BillsUtil.makeBillNo(DragonConstant.TZD_PREFIX,serialNumber);
		}
		return loanNo;
	}
}