package com.trenska.longwang.service.customer;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.entity.customer.Customer;
import com.trenska.longwang.model.report.CustomerInfoModel;
import com.trenska.longwang.model.customer.GoodsActiveInfoModel;
import com.trenska.longwang.model.sys.ResponseModel;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 2019/4/3
 * 创建人:Owen
 */
public interface ICustomerService extends IService<Customer> {
	Page<Customer> getCustomerPage(Page page);

	Page<Customer> getCustomerPageSelective(Map<String,Object> params, Page page);

	ResponseModel addCustomer(Customer customer);

	String getCustomerSpecialPrice(Map<String, Object> params);

	List<GoodsActiveInfoModel> getCustomerActiveInfo(Map<String, Object> params);

	Page<Customer> getCustomerPageNoParams(Page pageParam);

	ResponseModel deleteCustomerById(Integer custId);

	ResponseModel deleteCustomerByIds(Collection<Integer> custIds);

	List<CustomerInfoModel> getCustomerInfoSelective(Map<String, Object> params);

	Set<Integer> getCustIdsOfSalesman(Integer salesmanId);
}
