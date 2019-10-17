package com.trenska.longwang.service.financing;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.entity.financing.DealDetailSummarizing;
import com.trenska.longwang.entity.financing.Loan;
import com.trenska.longwang.model.sys.ResponseModel;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 *  调账单服务类
 * </p>
 *
 * @author Owen
 * @since 2019-07-30
 */
public interface ILoanService extends IService<Loan> {

	ResponseModel addLoan(Loan loan, HttpServletRequest request);

	Page<Loan> getLoanPageSelective(Page page, Map<String, Object> params);

	ResponseModel invalidLoanById(Long loanId);

}
