package com.trenska.longwang.dao.customer;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.trenska.longwang.entity.customer.CustType;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 2019/4/6
 * 创建人:Owen
 */
//@CacheNamespace(implementation = RedisCacheMybatis.class, eviction = RedisCacheMybatis.class)
public interface CustTypeMapper extends BaseMapper<CustType> {

	@Select("select cust_type_name from t_cust_type where cust_type_id = #{custTypeId}")
	String selectNameById(Integer custTypeId);

	@Select("select * from t_cust_type")
	List<CustType> selectCustTypePage(Pagination page);

	@Select("select * from t_cust_type where cust_type_name like concat('%',#{custTypeName},'%')")
	List<CustType> selectCustTypePageByName(Pagination page, @Param("custTypeName") String custTypeName);
}
