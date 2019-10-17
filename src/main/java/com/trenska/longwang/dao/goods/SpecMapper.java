package com.trenska.longwang.dao.goods;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.trenska.longwang.config.RedisCacheMybatis;
import com.trenska.longwang.entity.goods.Spec;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 产品规格Mapper 接口
 * @author Owen
 * @since 2019-04-07
 */
//@CacheNamespace(implementation = RedisCacheMybatis.class, eviction = RedisCacheMybatis.class)
public interface SpecMapper extends BaseMapper<Spec> {

	@Select("select * from t_spec where deleted = 0 and spec_name like concat('%',#{specName},'%')")
	List<Spec> selectSpecPageByName(Pagination page, String specName);

	@Select("select * from t_spec where stat = #{stat} and deleted = 0")
	List<Spec> selectSpecPageByStat(Pagination page, Boolean stat);

	@Select("select * from t_spec where deleted = 0")
	List<Spec> selectSpecPage(Pagination page);

	int selectCountSelective(Spec spec);

//	List<Spec> selectSpecPageSelective(Pagination page, Spec spec);

	List<Spec> selectSpecPageSelective(Pagination page, Map<String,Object> params);

	int selectSpcePageSelectiveCount(Map<String, Object> params);

	boolean setDeletable();

	boolean setUndeletable();

	boolean deleteSpecById(@Param("specId") Integer specId);
}
