package com.trenska.longwang.dao.financing;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.trenska.longwang.entity.financing.DealDetail;
import com.trenska.longwang.entity.financing.DealDetailSummarizing;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 交易明细 Mapper 接口
 * </p>
 *
 * @author Owen
 * @since 2019-05-20
 */
//@CacheNamespace(implementation = RedisCacheMybatis.class, eviction = RedisCacheMybatis.class)
public interface DealDetailMapper extends BaseMapper<DealDetail> {

	List<DealDetail> selectDealDetailPageSelective(Map<String, Object> params, Pagination page);

	int selectDealDetailCountSelective(Map<String, Object> params);

	DealDetailSummarizing selectDealDetailSummarizingForAdd(Map<String, Object> params);

	List<DealDetail> selectLastSurplusDebtBefore(Map<String,Object> params);

	DealDetail selectLastSurplusDebtBetween(Map<String, Object> params);

	List<Integer> selectLastSurplusCustIds(Map<String, Object> params);

	DealDetailSummarizing selectDealDetailSummarizing(Map<String, Object> params);
}
