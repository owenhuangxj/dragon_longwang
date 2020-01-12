package com.trenska.longwang.dao.sys;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trenska.longwang.entity.sys.SysPerm;
import com.trenska.longwang.model.sys.PermModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;
//@CacheNamespace(implementation = RedisCacheMybatis.class,eviction = RedisCacheMybatis.class)
public interface SysPermMapper extends BaseMapper<SysPerm> {

    Set<SysPerm> selectPermsByEmpId(Integer empId);

	List<PermModel> selectSubPermsByPval(String pval);

	List<PermModel> selectSubPermsByRidAndPval(@Param("rid") Integer rid, @Param("pval") String pval);

	List<PermModel> selectSubPermsByRid(Integer rid);

}
