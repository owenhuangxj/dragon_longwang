package com.trenska.longwang.service.impl.financing;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.dao.financing.PaywayMapper;
import com.trenska.longwang.entity.financing.Payway;
import com.trenska.longwang.service.financing.IPaywayService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 收款/付款方式 服务实现类
 * </p>
 *
 * @author Owen
 * @since 2019-05-19
 */
@Service
public class PaywayServiceImpl extends ServiceImpl<PaywayMapper, Payway> implements IPaywayService {

	@Override
	public Page<Payway> getPaywayPage(Page page) {
		page.setRecords(super.baseMapper.selectPaywayPage(page));
		page.setTotal(this.count());
		return page;
	}
}
