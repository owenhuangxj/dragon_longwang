package com.trenska.longwang.dao.stock;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.trenska.longwang.entity.goods.Goods;
import com.trenska.longwang.entity.stock.Stock;
import com.trenska.longwang.entity.stock.StockDetail;
import com.trenska.longwang.entity.stock.StockDetails;
import com.trenska.longwang.model.report.GoodsStockSummarizingModel;
import com.trenska.longwang.model.report.GoodsStockSummationModel;
import com.trenska.longwang.model.report.GoodsStockinStatisticsModel;
import com.trenska.longwang.model.report.GoodsStockinSummationModel;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 库存 Mapper 接口
 * </p>
 *
 * @author Owen
 * @since 2019-04-17
 */
//@CacheNamespace(implementation = RedisCacheMybatis.class, eviction = RedisCacheMybatis.class)
public interface StockMapper extends BaseMapper<Stock> {
	/**
	 * 商品详情分页
	 */
	List<StockDetails> selectStockDetailPageSelective(Map<String, Object> params, Pagination page);
	Integer selectStockDetailCountSelective(Map<String, Object> params);

	/**
	 * 商品库存分页
	 */
	Integer selectStockStatusPageCount(Map<String, Object> params);
	List<Goods> selectStockStatusPage(Map<String, Object> params, Pagination page);

	Stock selectDetailByStockNo(Map<String, Object> params);

	Stock selectStockInfo(Map<String,Object> params);


	@Select("select count(1) from t_stock")
	Integer selectStockCount();
	@Select("select count(1) from t_stock where stock_no like concat('%',#{stockNo},'%')")
	Integer selectStockCountByStockNo(String stockNo);
	/**
	 * 出/入库通用分页
	 */
	List<Stock> selectStockSelective(Map<String, Object> params, Pagination page);

	Integer selectStockCountSelective(Map<String, Object> params);

	List<Stock> selectByBusiNo(String busiNo);

	List<Stock> selectIndentStockoutByBusiNo(String busiNo);

	Stock selectRecordOfMaxId(String stockType);

	String selectStockNoOfMaxId(String stockType);

//	List<Map<String,String>> selectGoodsStockSummationOld(Map<String, Object> params);

	int selectGoodsStockSummarizingCount(Map<String, Object> params);

	GoodsStockSummationModel selectGoodsStockSummation(Map<String, Object> params);
	List<GoodsStockSummarizingModel> selectGoodsStockSummarizing(Map<String, Object> params, Pagination page);

	GoodsStockinSummationModel selectGoodsStockinSummation(Map<String, Object> params);

	List<GoodsStockinStatisticsModel> selectGoodsStockinStatistic(Map<String, Object> params, Pagination page);

	List<Integer> selectGoodsStockinStatisticsCount(Map<String, Object> params);

	@Select("select stock_no from t_stock where busi_no = #{busiNo}")
	Set<String> selectStockNoByBusiNo(String busiNo);

	String selectInitStockSum(Map<String,Object> params);

	Stock selectByStockNo(String stockNo);

	String selectQckcStock(int goodsId);

	String selectGoodsInitStock(Map<String, Object> params);
}

