package com.trenska.longwang.dao.customer;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.trenska.longwang.entity.customer.Customer;
import com.trenska.longwang.model.report.CustomerInfoModel;
import com.trenska.longwang.model.customer.CustomerPriceModel;
import com.trenska.longwang.model.customer.GoodsActiveInfoModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 2019/4/3
 * 创建人:Owen
 */
//@CacheNamespace(implementation = RedisCacheMybatis.class, eviction = RedisCacheMybatis.class)
public interface CustomerMapper extends BaseMapper<Customer> {

	List<Customer> selectCustomerPage(Pagination pagination ,Map<String,Object> params);

	List<Customer> selectCustomerPageSelective(Pagination pagination);
	List<Customer> selectCustomerWithDataPermPageSelective(Map<String,Object>params, Pagination pagination);

	Integer selectCustomerCountSelective(Map<String,Object> params);

	@Select("select cust_name from t_customer where cust_id = #{custId}")
	String selectNameById(Integer custId);

	CustomerPriceModel selectCustomerPriceGrp(Map<String,Object> params);

	String selectCustomerSpecialPrice(Map<String, Object> params);

	List<GoodsActiveInfoModel> selectCustomerActiveInfo(Map<String, Object> params);

	Boolean updateCustomerDebt(@Param("custId") Integer custId, @Param("variation") String variation);

	List<CustomerInfoModel> selectExportingCustomerInfoSelective(Map<String,Object> params);

	@Select("select cust_id from t_customer where emp_id = #{salesmanId}")
	Set<Integer> selectCustIdsOfSalesman(Integer salesmanId);
}
