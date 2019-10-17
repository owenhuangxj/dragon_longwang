package com.trenska.longwang.dao.goods;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trenska.longwang.entity.goods.GoodsPriceGrp;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Owen
 * @since 2019-04-23
 */
//@CacheNamespace(implementation = RedisCacheMybatis.class, eviction = RedisCacheMybatis.class)
public interface GoodsPriceGrpMapper extends BaseMapper<GoodsPriceGrp> {
	List<GoodsPriceGrp> selectByGoodsId(Integer goodsId);

	GoodsPriceGrp selectByGoodsIdAndCustId(@Param("goodsId") Integer goodsId, @Param("custId") Integer custId);
}
