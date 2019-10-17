package com.trenska.longwang.service.impl.customer;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.dao.customer.CustTypeMapper;
import com.trenska.longwang.entity.customer.CustType;
import com.trenska.longwang.service.customer.ICustTypeService;
import org.springframework.stereotype.Service;

/**
 * 2019/4/6
 * 创建人:Owen
 */
@Service
public class CustTypeServiceImpl extends ServiceImpl<CustTypeMapper, CustType> implements ICustTypeService {
	@Override
	public Page<CustType> getCustTypePage(Page page) {
		page.setRecords(super.baseMapper.selectCustTypePage(page));
		page.setTotal(super.baseMapper.selectCount(new QueryWrapper()));
		return page;
	}

	@Override
	public Page<CustType> getCustTypePageByName(Page page, String custTypeName) {
		page.setRecords(super.baseMapper.selectCustTypePageByName(page,custTypeName));
		page.setTotal(count(new QueryWrapper<CustType>().like("cust_type_name",custTypeName)));
		return page;
	}
}
