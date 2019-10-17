package com.trenska.longwang.dao.goods;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trenska.longwang.entity.goods.GoodsCustSpecify;

import java.util.List;

/**
 * 会员特价 Mapper 接口
 *
 * @author Owen
 * @since 2019-04-23
 */
//@CacheNamespace(implementation = RedisCacheMybatis.class, eviction = RedisCacheMybatis.class)
public interface GoodsCustSpecialMapper extends BaseMapper<GoodsCustSpecify> {
	List<GoodsCustSpecify> selectByGoodsId(Integer goodsId);
}
