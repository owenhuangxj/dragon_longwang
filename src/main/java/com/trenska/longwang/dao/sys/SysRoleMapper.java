package com.trenska.longwang.dao.sys;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.config.RedisCacheMybatis;
import com.trenska.longwang.entity.sys.SysRole;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
//@CacheNamespace(implementation = RedisCacheMybatis.class, eviction = RedisCacheMybatis.class)
public interface SysRoleMapper extends BaseMapper<SysRole> {
	Set<SysRole> selectRolesByEmpId(Integer empId);

	List<SysRole> selectSysRolesSelective(Map<String, Object> params, Page page);

	int selectSysRolesCountSelective(Map<String, Object> params);
}
