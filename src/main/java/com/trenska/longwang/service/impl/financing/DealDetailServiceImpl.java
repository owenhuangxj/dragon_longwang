package com.trenska.longwang.service.impl.financing;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.constant.DragonConstant;
import com.trenska.longwang.dao.customer.CustomerMapper;
import com.trenska.longwang.dao.financing.DealDetailMapper;
import com.trenska.longwang.entity.customer.Customer;
import com.trenska.longwang.entity.financing.DealDetail;
import com.trenska.longwang.entity.financing.DealDetailSummarizing;
import com.trenska.longwang.exception.ServiceException;
import com.trenska.longwang.service.financing.IDealDetailService;
import com.trenska.longwang.util.CustomerUtil;
import com.trenska.longwang.util.StringUtil;
import com.trenska.longwang.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <p>
 * 交易明细 服务实现类
 * </p>
 *
 * @author Owen
 * @since 2019-05-20
 */
@Slf4j
@Service
public class DealDetailServiceImpl extends ServiceImpl<DealDetailMapper, DealDetail> implements IDealDetailService {

	@Autowired
	private CustomerMapper customerMapper;

	@Autowired
	private DealDetailMapper dealDetailMapper;

	@Override
	public DealDetailSummarizing getDealDetailSummarizing(Map<String, Object> params) {
		DealDetailSummarizing dealDetailSummarizing = super.baseMapper.selectDealDetailSummarizing(params);
		return dealDetailSummarizing;
	}

	@Override
	@Transactional
	public boolean addDebt(DealDetail dealDetail) {
		String amount = DragonConstant.PLUS.concat(StringUtil.replacePrefix(Optional.of(dealDetail.getAmount())));
		dealDetail.setAmount(amount);
		dealDetail.setTime(TimeUtil.getCurrentTime(DragonConstant.TIME_FORMAT));
		Customer dbCustomer = customerMapper.selectById(dealDetail.getCustId());
		if (dbCustomer == null){
			throw new ServiceException(HttpServletResponse.SC_BAD_REQUEST,"客户信息输入有误","请输入有效的客户信息","com" +
					".trenska.longwang.debt.add");
		}
		BigDecimal oldDebt = new BigDecimal(dbCustomer.getDebt());
		BigDecimal newDebt = new BigDecimal(amount).add(oldDebt);
		dealDetail.setNewDebt(newDebt.toString());
		CustomerUtil.addCustomerDebt(dealDetail.getCustId(),dbCustomer.getDebt(),new BigDecimal(amount));
		boolean succ = dealDetail.insert();
		log.info("record is inserted into table: {}", succ);
		return succ;
	}

	@Override
	public Page<DealDetail> page(Page page, Map<String, Object> params) {
		int total = dealDetailMapper.selectDebtCount(params);
		List<DealDetail> records =  dealDetailMapper.selectDebtPage(params);
		page.setTotal(total);
		page.setRecords(records);
		return page;
	}
}
