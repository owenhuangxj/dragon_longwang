package com.trenska.longwang.service.customer;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.entity.customer.CustType;

/**
 * 2019/4/6
 * 创建人:Owen
 */
public interface ICustTypeService extends IService<CustType> {
	Page<CustType> getCustTypePage(Page page);

	Page<CustType> getCustTypePageByName(Page pageParam, String custTypeName);
}
