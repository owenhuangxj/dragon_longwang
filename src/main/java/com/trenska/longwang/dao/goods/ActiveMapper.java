package com.trenska.longwang.dao.goods;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.trenska.longwang.entity.goods.Active;

import java.util.List;

/**
 * 商品活动 Mapper接口
 * @author Owen
 * @since 2019-04-12
 */
//@CacheNamespace(implementation = RedisCacheMybatis.class, eviction = RedisCacheMybatis.class)
public interface ActiveMapper extends BaseMapper<Active> {

	List<Active> selectActivePage(Pagination page);

	int selectCountSelective(Active active);

	boolean invalidateActives();

	List<Active> selectActivePageSelective(Active active, Pagination page);

//	@ResultMap("BaseResultMap")
//	@Select("select * from t_active where active_id = #{activeId}")
	Active selectActiveById(Integer activeId);
}
