package com.trenska.longwang.util;

import com.trenska.longwang.constant.Constant;
import com.trenska.longwang.dao.financing.LoanMapper;
import org.apache.commons.lang3.StringUtils;

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
			return BillsUtil.makeBillNo(Constant.TZD_PREFIX,1);
		}
		String todayDate = TimeUtil.getCurrentTime(Constant.BILL_TIME_FORMAT);
		String dateOfMaxLoanId = "";

		if (StringUtils.isNotEmpty(loanNoOfMaxId)){
			dateOfMaxLoanId = BillsUtil.getDate(loanNoOfMaxId);
		}

		String lastLoanNo = loanMapper.getLastLoanNo();
		String loanNo = "";

		boolean isLastLoanNoEmpty = StringUtils.isEmpty(lastLoanNo);

		if (isLastLoanNoEmpty || !todayDate.equals(dateOfMaxLoanId) ){
			loanNo = BillsUtil.makeBillNo(Constant.TZD_PREFIX,1);
		}else {
			int serialNumber = BillsUtil.getSerialNumber(loanNoOfMaxId) + 1;
			loanNo = BillsUtil.makeBillNo(Constant.TZD_PREFIX,serialNumber);
		}

		return loanNo;
	}

}