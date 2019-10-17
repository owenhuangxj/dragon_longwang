package com.trenska.longwang.dao.sys;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trenska.longwang.config.RedisCacheMybatis;
import com.trenska.longwang.entity.sys.SysRolePerm;
import org.apache.ibatis.annotations.CacheNamespace;

/**
 * @author Owen
 * @since 2019-05-17
 */
//@CacheNamespace(implementation = RedisCacheMybatis.class, eviction = RedisCacheMybatis.class)
public interface SysRolePermMapper extends BaseMapper<SysRolePerm> {

}
