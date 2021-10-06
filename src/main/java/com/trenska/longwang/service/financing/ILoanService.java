package com.trenska.longwang.service.financing;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.entity.financing.Loan;
import com.trenska.longwang.model.sys.CommonResponse;

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

	CommonResponse addLoan(Loan loan);

	Page<Loan> getLoanPageSelective(Page page, Map<String, Object> params);

	CommonResponse invalidLoanById(Long loanId);

}
