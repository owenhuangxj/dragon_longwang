package com.trenska.longwang.dao.financing;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.trenska.longwang.entity.financing.DealDetailSummarizing;
import com.trenska.longwang.entity.financing.Loan;
import com.trenska.longwang.model.finaning.AccountCheckingModel;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  调账单Mapper 接口
 * </p>
 *
 * @author Owen
 * @since 2019-07-30
 */
public interface LoanMapper extends BaseMapper<Loan> {

	String getLastLoanNo();

	List<Loan> selectLoanPageSelective(Pagination page, Map<String, Object> params);

	int selectLoanPageSelectiveCount(Map<String, Object> params);

//	List<AccountCheckingModel> selectAccountCheckingPageSelective(Map<String, Object> params, Page page);

	@Select("select sum(amount) from t_loan where stat = 1 and borrow_cust_id = #{custId}" )
	String selectBorrowAmount(int custId);

	@Select("select sum(amount) from t_loan where stat = 1 and lend_cust_id = #{custId}" )
	String selectLendAmount(int custId);

}
