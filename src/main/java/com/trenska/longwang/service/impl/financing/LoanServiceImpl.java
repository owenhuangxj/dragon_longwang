package com.trenska.longwang.service.impl.financing;

import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.constant.DragonConstant;
import com.trenska.longwang.dao.customer.CustomerMapper;
import com.trenska.longwang.dao.financing.LoanMapper;
import com.trenska.longwang.entity.customer.Customer;
import com.trenska.longwang.entity.financing.Loan;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trenska.longwang.entity.sys.SysConfig;
import com.trenska.longwang.model.sys.CommonResponse;
import com.trenska.longwang.service.financing.ILoanService;
import com.trenska.longwang.util.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Owen
 * @since 2019-07-30
 */
@Service
@SuppressWarnings("all")
public class LoanServiceImpl extends ServiceImpl<LoanMapper, Loan> implements ILoanService {

	@Autowired
	private CustomerMapper customerMapper;

	@Resource(name = DragonConstant.REDIS_JSON_TEMPLATE_NAME)
	private RedisTemplate<String,Object> jsonRedisTemplate;

	/**
	 * 新建调帐单
	 * 借方减少欠款，贷方增加欠款
	 * 记录交易明细
	 * 记录对账
	 *
	 * @param loan
	 * @return
	 */
	@Override
	@Transactional
	public CommonResponse addLoan(Loan loan) {

		Integer empIdInToken = SysUtil.getEmpIdInToken();

		Integer borrowCustId = loan.getBorrowCustId();
		Integer lendCustId = loan.getLendCustId();

		if (ObjectUtils.isEmpty(borrowCustId) || ObjectUtils.isEmpty(lendCustId)) {
			return CommonResponse.getInstance().succ(false).msg("无效的客户信息");
		}

		if (borrowCustId.intValue() == lendCustId.intValue()) {
			return CommonResponse.getInstance().succ(false).msg("不能调账给自己");
		}

		BigDecimal amount = new BigDecimal(loan.getAmount());

		String currentTime = TimeUtil.getCurrentTime(DragonConstant.TIME_FORMAT);
		String loanNo = LoanUtil.getLoanNo(super.baseMapper);
		String nameNo = DragonConstant.TZD_CHINESE.concat(DragonConstant.MINUS).concat(loanNo);

		String remarks = loan.getRemarks();

		// 借方
		Customer borrowCustomer = customerMapper.selectById(borrowCustId);
		BigDecimal borrowCustomerNewDebt = new BigDecimal(borrowCustomer.getDebt()).subtract(amount);
		// 借方减少欠款
		CustomerUtil.subtractCustomerDebt(borrowCustId, borrowCustomer.getDebt(), amount);
		DealDetailUtil.saveDealDetail(borrowCustId, nameNo, currentTime, DragonConstant.MINUS.concat(amount.toString()), borrowCustomerNewDebt.toString(), DragonConstant.TZ_SUBSTRACT_OPER, "", remarks);

		// 贷方
		Customer lendCustomer = customerMapper.selectById(lendCustId);
		BigDecimal lendCustomerNewDebt = new BigDecimal(lendCustomer.getDebt()).add(amount);
		// 贷方增加欠款
		CustomerUtil.addCustomerDebt(lendCustId, amount);
		DealDetailUtil.saveDealDetail(lendCustId, nameNo, currentTime, DragonConstant.PLUS.concat(amount.toString()), lendCustomerNewDebt.toString(), DragonConstant.TZ_ADD_OPER, "", remarks);

		loan.setLoanNo(loanNo);
		loan.setEmpId(empIdInToken);
		loan.setLoanTime(currentTime);
		super.baseMapper.insert(loan);

		return CommonResponse.getInstance().succ(true).msg("调账成功");
	}

	@Override
	public Page<Loan> getLoanPageSelective(Page page, Map<String, Object> params) {

		SysConfig sysConfig = SysUtil.getSysConfig(SysUtil.getEmpIdInToken());
		Integer retain = sysConfig.getRetain();

		List<Loan> records = super.baseMapper.selectLoanPageSelective(page, params);
		for (Loan record : records) {
			BigDecimal amount = new BigDecimal(record.getAmount());
			amount = amount.setScale(retain, RoundingMode.HALF_UP);
			record.setAmount(amount.toString());
		}
		int total = super.baseMapper.selectLoanPageSelectiveCount(params);
		page.setTotal(total);
		page.setRecords(records);
		return page;
	}

	/**
	 * 增加借方欠款，记录交易明细
	 * 减少贷方欠款，记录交易明细
	 * 作废调帐单
	 *
	 * @param loanId
	 * @return
	 */
	@Override
	@Transactional
	public CommonResponse invalidLoanById(Long loanId) {
		Loan dbLoan = super.baseMapper.selectById(loanId);
		if (ObjectUtils.isEmpty(dbLoan)) {
			return CommonResponse.getInstance().succ(false).msg("无效的调账单");
		}

		String amount = dbLoan.getAmount();
		String loanNo = dbLoan.getLoanNo();

		String nameNo = DragonConstant.TZD_CHINESE.concat(DragonConstant.MINUS).concat(loanNo);
		String operAddInvalid = DragonConstant.TZ_ADD_OPER.concat(DragonConstant.ZF);
		String operSubstractInvalid = DragonConstant.TZ_SUBSTRACT_OPER.concat(DragonConstant.ZF);
		String currentTime = TimeUtil.getCurrentTime(DragonConstant.TIME_FORMAT);

		// 增加借方欠款(OPER为"调账减少(作废)")，记录交易明细
		Integer borrowCustId = dbLoan.getBorrowCustId();
		Customer borrowCustomer = customerMapper.selectById(borrowCustId);
		String borrowCustomerDebt = borrowCustomer.getDebt();
		BigDecimal newBorrowCustomerDebt = new BigDecimal(borrowCustomerDebt).add(new BigDecimal(amount));
		CustomerUtil.addCustomerDebt(borrowCustId, new BigDecimal(amount));
		DealDetailUtil.saveDealDetail(borrowCustId, nameNo, currentTime, DragonConstant.PLUS.concat(amount), newBorrowCustomerDebt.toString(), operSubstractInvalid, "", "");

		// 减少贷方欠款(OPER为"调账增加(作废)")，记录交易明细
		Integer lendCustId = dbLoan.getLendCustId();
		Customer lendCustomer = customerMapper.selectById(lendCustId);
		String lendCustomerDebt = lendCustomer.getDebt();
		BigDecimal newLendCustomerDebt = new BigDecimal(lendCustomerDebt).subtract(new BigDecimal(amount));
		CustomerUtil.subtractCustomerDebt(lendCustId, amount);
		DealDetailUtil.saveDealDetail(lendCustId, nameNo, currentTime, DragonConstant.MINUS.concat(amount), newLendCustomerDebt.toString(), operAddInvalid, "", "");

		// 作废调帐单
		dbLoan.setStat(false);
		dbLoan.updateById();
		return CommonResponse.getInstance().succ(true).msg("调账单作废成功");
	}

}
