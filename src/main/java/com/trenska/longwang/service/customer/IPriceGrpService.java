package com.trenska.longwang.service.customer;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.entity.customer.PriceGrp;
import com.trenska.longwang.model.sys.CommonResponse;

/**
 * 2019/4/6
 * 创建人:Owen
 */
public interface IPriceGrpService extends IService<PriceGrp> {

	Page<PriceGrp> getPriceGrpPage(Page page);

	Page<PriceGrp> getPriceGrpPageByName(Page page, String grpName);

	CommonResponse savePriceGrp(PriceGrp priceGrp);
}
