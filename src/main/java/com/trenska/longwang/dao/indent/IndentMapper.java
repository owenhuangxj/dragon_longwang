package com.trenska.longwang.dao.indent;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.trenska.longwang.entity.financing.DealDetailSummarizing;
import com.trenska.longwang.entity.indent.Indent;
import com.trenska.longwang.model.indent.IndentNoCustIdNameModel;
import com.trenska.longwang.model.report.*;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 订货单 Mapper 接口
 *
 * @author Owen
 * @since 2019-04-22
 */
//@CacheNamespace(implementation = RedisCacheMybatis.class, eviction = RedisCacheMybatis.class)
public interface IndentMapper extends BaseMapper<Indent> {

	List<Indent> selectIndentPageSelective(Map<String, Object> params, Pagination page);

	int selectIndentPageSelectiveCount(Map<String, Object> params);

	Indent getIndentByNo(String indentNo);

	boolean makeSureIndentStat(String indentNo);

	Indent selectRecordOfMaxId(String indentType);

	CustSalesSummationModel selectCustSalesBillSummation(Map<String, Object> params);

	List<CustSalesBillRecordsModel> selectCustSalesBillPageSelective(Map<String, Object> params, Pagination page);

	int selectCustSalesBillCountSelective(Map<String, Object> params);

	List<CustSalesSummarizingModel> selectCustSalesSummarizingPageSelective(CustSalesSummarizingSearchModel searchModel, Pagination page);

	CustSalesSummationModel selectCustSalesSummation(CustSalesSummarizingSearchModel searchModel);

	int selectCustSalesSummarizingCountSelective(CustSalesSummarizingSearchModel searchModel);

	CustSalesStatisticsSummationModel selectCustSalesStatisticsSummation(Map<String, Object> params);

	String selectCustSalesStatisticsAvgPrice(Map<String, Object> params);

	List<CustSalesStatisticsModel> selectCustSalesStatisticsPageSelective(Map<String, Object> params, Pagination page);

	int selectCustSalesStatisticsCountSelective(Map<String, Object> params);

	List<CustSalesDetailModel> selectCustSalesDetailPageSelective(Map<String, Object> params, Pagination page);

	int selectCustSalesDetailCountSelective(Map<String, Object> params);

	List<AreaSalesRankModel> selectFirstLevelAreaSalesRank(Pagination page);

	int selectFirstLevelAreaSalesRankCount();

	AreaSalesRankModel selectAreaSalesRankByAreaGrpId(Integer areaGrpId);

	AreaSalesRankModel selectSubAreaSalesRank(Map<String, Object> params);

	CommonSummation selectCustSalesRankSummation(Map<String, Object> params);

	List<CustSalesRankModel> selectCustSalesRank(Map<String, Object> params, Pagination page);

	int selectCustSalesRankCount(Map<String, Object> params);

	List<GoodsSalesSummarizingModel> selectGoodsSalesSummarizing(Map<String, Object> params, Pagination page);

	CommonSummation selectGoodsSalesSummation(Map<String, Object> params);

	int selectGoodsSalesSummarizingCount(Map<String, Object> params);

	CommonSummation selectSingleGoodsSalesDetailSummation(Map<String, Object> params);

	SingleGoodsSalesDetailModel selectSingleGoodsSalesDetail(Map<String, Object> params);

	int selectSingleGoodsSalesDetailCount(Map<String, Object> params);

	List<GoodsSalesRankModel> selectGoodsSalesRank(Map<String, Object> params, Pagination page);

	int selectGoodsSalesRankCount(Map<String, Object> params);

	CommonSummation selectGoodsSalesRankSummation(Map<String, Object> params);

	List<IndentStatisticsModel> selectIndentStatistics(Map<String, Object> params, Pagination page);

	IndentStatisticsSummationModel selectIndentStatisticsSummation(Map<String, Object> params);

	int selectIndentStatisticsCount(Map<String, Object> params);

	CustSalesDetailSummarizingModel selectCustSalesDetailSummarizing(Map<String, Object> params);

	boolean hasUnfinishedIndents(@Param("activeIds") Collection<Integer> activeIds);

	List<String> selectDiscounts(Page page, Map<String, Object> params);

	int selectDiscountsCount(Map<String, Object> params);

	DealDetailSummarizing selectDealDetailSummarizingForAdd(Map<String, Object> params);

	DealDetailSummarizing selectDealDetailSummarizingForDecrease(Map<String, Object> params);

	AccountCheckingSummationModel selectAccountCheckingSummationIndentPart(Map<String, Object> params);

	List<AreaSalesRankModel> selectAreaSalesRank(Pagination page, Map<String, Object> params);

	int selectAreaSalesRankCount(Map<String, Object> params);

	CommonSummation selectAreaSalesRankSummation(Map<String, Object> params);

	int selectGoodsDeliveryStaticsCount(Map<String, Object> params);

	List<DeliveryStaticsModel> selectGoodsDeliveryStatics(Pagination page, Map<String, Object> params);

	CommonSummation selectGoodsDeliveryStaticsSummarizing(Map<String, Object> params);

	DeliveryStaticsModel selectGoodsDeliveryDetailsStaticsSummarizing(Map<String, Object> params);

	List<DeliveryDetailsStaticsModel> selectGoodsDeliveryDetailsStatics(Pagination page, Map<String, Object> params);

	int selectGoodsDeliveryDetailStaticsCount(Map<String, Object> params);

	List<String> selectIndentNos(Pagination page, @Param("str") String str);

	int selectIndentNosCount(@Param("str") String str);

	List<IndentNoCustIdNameModel> selectIndentNoCustName(Pagination page, @Param("str") String str);

	int selectIndentNoCustNameCount(@Param("str") String str);

	int selectSalesmanSalesRankCount(Map<String, Object> params);

	List<SalesmanSalesRankModel> selectSalesmanSalesRank(Pagination page, Map<String, Object> params);

	CommonSummation selectSalesmanSalesRankSummation(Map<String, Object> params);
}