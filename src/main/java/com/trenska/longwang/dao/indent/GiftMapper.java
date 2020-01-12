package com.trenska.longwang.dao.indent;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trenska.longwang.entity.indent.Gift;

import java.util.List;

/**
 * @author Owen
 * @since 2019-05-07
 */

//@CacheNamespace(implementation = RedisCacheMybatis.class, eviction = RedisCacheMybatis.class)
public interface GiftMapper extends BaseMapper<Gift> {
	List<Gift> selectGiftsInfoById(Integer detailId);

}
