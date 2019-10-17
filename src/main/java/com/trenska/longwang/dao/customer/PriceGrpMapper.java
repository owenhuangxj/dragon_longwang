package com.trenska.longwang.dao.customer;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.trenska.longwang.entity.customer.PriceGrp;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 2019/4/6
 * 创建人:Owen
 */
//@CacheNamespace(implementation = RedisCacheMybatis.class, eviction = RedisCacheMybatis.class)
public interface PriceGrpMapper extends BaseMapper<PriceGrp> {

	@Select("select price_grp_name from t_price_grp where price_grp_id = #{priceGrpId}")
	String selectNameById(Integer priceGrpId);

	@Select("select * from t_price_grp")
	List<PriceGrp> getPriceGrpPage(Pagination pagination);

	@Select("select * from t_price_grp where price_grp_name like concat( '%', #{priceGrpName},'%')")
	List<PriceGrp> getPriceGrpPageByName(Pagination pagination, @Param("priceGrpName") String priceGrpName);

}
