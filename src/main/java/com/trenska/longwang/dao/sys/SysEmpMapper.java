package com.trenska.longwang.dao.sys;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.trenska.longwang.entity.sys.SysEmp;
import org.apache.ibatis.annotations.Select;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.Map;

//@CacheNamespace(implementation = RedisCacheMybatis.class, eviction = RedisCacheMybatis.class)

public interface SysEmpMapper extends BaseMapper<SysEmp> {

//	@Cacheable(keyGenerator = "redisCacheKeyGenerator",cacheNames = {"selectSysEmpPageSelective"})
	List<SysEmp> selectSysEmpPageSelective(Map<String,Object> params,Pagination page);

//	@Cacheable(keyGenerator = "redisCacheKeyGenerator",cacheNames = {"selectSysEmpCountSelective"})
	int selectSysEmpCountSelective(Map<String, Object> params);

//	@Cacheable(keyGenerator = "redisCacheKeyGenerator",cacheNames = {"selectSimpleInfoById"})
	SysEmp selectSimpleInfoById(Integer empId);

//	@Cacheable(keyGenerator = "redisCacheKeyGenerator",cacheNames = {"selectNameById"})
	@Select("select emp_name as empName from sys_emp where emp_id = #{empId}")
	String selectNameById(Integer empId);
}
