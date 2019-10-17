package com.trenska.longwang.dao.goods;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.trenska.longwang.entity.goods.SpecProperty;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Owen
 * @since 2019-04-09
 */
//@CacheNamespace(implementation = RedisCacheMybatis.class, eviction = RedisCacheMybatis.class)
public interface SpecPropertyMapper extends BaseMapper<SpecProperty> {

	@Select("select * from t_spec_property")
	List<SpecProperty> selectSpecPropertiesPage(Pagination page);

//	@Select("select * from t_spec_property where deleted = 0 and spec_id = #{specId}")
	List<SpecProperty> selectSpecPropertiesPageBySpecId(Pagination page, @Param("specId") Integer specId,Boolean stat);

	@Select("select * from t_spec_property where prop_name like concat( '%',#{propName},'%')")
	List<SpecProperty> selectSpecPropertiesPageByName(Pagination page, @Param("propName") String propName);

	@Select("select * from t_spec_property where stat = #{stat}")
	List<SpecProperty> selectSpecPropertiesPageBySpecStat(Pagination page, @Param("stat") Boolean stat);

	int selectCountSelective(SpecProperty specProperty);

	List<SpecProperty> selectSpecPropertiesPageSelective(Pagination page, SpecProperty specProperty);

	boolean setDeletable();

	boolean deleteSpecProperty(@Param("specId") Integer specId);
}
