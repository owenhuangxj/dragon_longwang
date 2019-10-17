package com.trenska.longwang.dao.sys;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trenska.longwang.config.RedisCacheMybatis;
import com.trenska.longwang.entity.sys.SysConfig;
import org.apache.ibatis.annotations.CacheNamespace;

/**
 * 2019/4/14
 * 创建人:Owen
 */
//@CacheNamespace(implementation = RedisCacheMybatis.class, eviction = RedisCacheMybatis.class)
public interface SysConfigMapper extends BaseMapper<SysConfig> {
}
