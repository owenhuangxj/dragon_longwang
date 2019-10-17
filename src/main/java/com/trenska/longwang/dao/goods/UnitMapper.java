package com.trenska.longwang.dao.goods;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.trenska.longwang.entity.goods.Unit;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Owen
 * @since 2019-04-07
 */
//@CacheNamespace(implementation = RedisCacheMybatis.class, eviction = RedisCacheMybatis.class)
public interface UnitMapper extends BaseMapper<Unit> {

	@Select("select unit_name from t_unit where unit_id = #{unitId}")
	String selectUnitNameById(Integer unitId);

	@Select("select * from t_unit where deleted = 0")
	List<Unit> selectUnitPage(Pagination page);

	@Select("select count(1) from t_unit where deleted = 0")
	int selectUnitPageCount();

	@Select("select * from t_unit where unit_name like concat('%',#{unitName},'%')")
	List<Unit> selectUnitPageByName(Pagination page, @Param("unitName") String unitName);

	@Select("select * from t_unit where stat = #{stat} ")
	List<Unit> selectUnitPageByStat(Pagination page, @Param("stat") Boolean stat);

	List<Unit> selectUnitPageSelective(Pagination page, Unit unit);

	int selectCountSelective(Unit unit);

	boolean setDeletable();

	Unit selectUnitByName(@Param("unitName") String unitName);

	boolean updateUnitById(Unit dbUnit);
}
