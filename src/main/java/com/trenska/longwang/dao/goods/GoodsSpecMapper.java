package com.trenska.longwang.dao.goods;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trenska.longwang.entity.goods.GoodsSpec;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

/**
 * 商品-规格 Mapper 接口
 * @author Owen
 * @since 2019-04-11
 */
//@CacheNamespace(implementation = RedisCacheMybatis.class , eviction = RedisCacheMybatis.class)
public interface GoodsSpecMapper extends BaseMapper<GoodsSpec> {

	@Select("select ifnull(prop_name,'') from t_goods_spec where goods_id = #{goodsId}")
	Set<String> getPropNamesByGoodsId(Integer goodsId);

	List<GoodsSpec> selectGoodsSpecByGoodsId(Integer goodsId);

	List<GoodsSpec> selectGoodsSpecByGoodsIdAndPropName(@Param("goodsId") Integer goodsId, @Param("propName") String propName);

}
