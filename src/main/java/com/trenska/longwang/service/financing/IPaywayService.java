package com.trenska.longwang.service.financing;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.entity.financing.Payway;

/**
 * <p>
 * 收款/付款方式 服务类
 * </p>
 *
 * @author Owen
 * @since 2019-05-19
 */
public interface IPaywayService extends IService<Payway> {

	Page<Payway> getPaywayPage(Page page);
}
