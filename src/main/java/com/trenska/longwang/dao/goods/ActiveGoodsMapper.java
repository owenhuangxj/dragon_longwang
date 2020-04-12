package com.trenska.longwang.dao.goods;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trenska.longwang.entity.goods.ActiveGoods;

import java.util.List;

/**
 * @author Owen
 * @since 2019-04-15
 */
//@CacheNamespace(implementation = RedisCacheMybatis.class, eviction = RedisCacheMybatis.class)
public interface ActiveGoodsMapper extends BaseMapper<ActiveGoods> {

	List<ActiveGoods> selectActivesByGoodsId(Integer goodsId);

	int deleteInvalidateActives();
}
