package com.trenska.longwang.dao.stock;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.trenska.longwang.entity.indent.StockMadedate;
import com.trenska.longwang.entity.stock.StockDetail;
import com.trenska.longwang.model.report.SingleGoodsSalesIndentStockoutDetailModel;
import com.trenska.longwang.model.stock.StockWarningModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 库存明细Mapper接口
 * @author Owen
 * @since 2019-04-15
 */
//@CacheNamespace(implementation = RedisCacheMybatis.class, eviction = RedisCacheMybatis.class)
public interface StockDetailMapper extends BaseMapper<StockDetail> {

	// 报溢报损分页查询
	List<Map<String,Object>> selectStockOverBrkSelective(Map<String,Object> params, Pagination page);
	int selectStockOverBrkCountSelective(Map<String, Object> params);

	int selectStockDetailCountSelective(Map<String, Object> params);

	List<StockDetail> selectStockDetailPage(Map<String, Object> params, Pagination page);
	int selectStockDetailCount(Map<String, Object> params);

	/*查询商品各个批次的库存*/
	List<StockMadedate> selectGoodsMadeDateByGoodsId(Map<String , Object> params, Pagination page);
	Integer selectGoodsMadeDateCount(Map<String, Object> params);

	/*查询商品单个批次的入库详情*/
	List<StockDetail> selectGoodsMadeDateStockInfo(Map<String, Object> params, Pagination page);
	int selectGoodsMadeDateStockCount(Map<String, Object> params);

	List<StockWarningModel> selectStockWarningPageSelective(Map<String, Object> params, Pagination page);

	List<Integer> selectStockWarningCount(Map<String, Object> params);

	List<StockMadedate> selectStockoutDetailInfo(@Param("goodsId") Integer goodsId, @Param("busiNo") String indentNo);

	List<StockDetail> selectByBusiNo(String indentNo);

	List<StockDetail> selectByParams(Map<String, Object> params);

	List<StockDetail> selectByStockNo(String stockNo);

	Set<String> getUniqueMadeDatesByIndentNo(@Param("indentNo") String indentNo,@Param("goodsId") Integer goodsId);

	Set<SingleGoodsSalesIndentStockoutDetailModel> selectSingleGoodsStockoutDetails(Map<String,Object> params);

}
