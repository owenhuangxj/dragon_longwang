package com.trenska.longwang.dao.financing;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.trenska.longwang.entity.financing.Payway;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 收款/付款方式 Mapper 接口
 * </p>
 *
 * @author Owen
 * @since 2019-05-19
 */
//@CacheNamespace(implementation = RedisCacheMybatis.class, eviction = RedisCacheMybatis.class)
public interface PaywayMapper extends BaseMapper<Payway> {

	@Select("select * from t_payway")
	List<Payway> selectPaywayPage(Pagination page);

	@Select("select payway from t_payway where payway_id = #{paywayId}")
	String selectPaywayById(int id);
}
