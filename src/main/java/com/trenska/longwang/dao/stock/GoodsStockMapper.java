package com.trenska.longwang.dao.stock;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.trenska.longwang.entity.indent.StockMadedate;
import com.trenska.longwang.entity.stock.GoodsStock;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author Owen
 * @since 2019-06-15
 */
//@CacheNamespace(implementation = RedisCacheMybatis.class, eviction = RedisCacheMybatis.class)
public interface GoodsStockMapper extends BaseMapper<GoodsStock> {

	/**
	 * 更新商品批次库存，增加库存 加"-"
	 * @param madeDate
	 * @param num
	 * @param goodsId
	 * @return
	 */
	boolean updateGoodsMadeDateStock(@Param("goodsId")Integer goodsId, @Param("history") int num,@Param("madeDate") String madeDate,@Param("stockPrice") String stockPrice);

	/**
	 * 获取商品库存
	 * @param page
	 * @param params
	 * @return
	 */
	List<GoodsStock> selectGoodsStock( Pagination page,Map<String, Object> params);

	int selectGoodsStockCount(Map<String, Object> params);

	List<GoodsStock> selectGoodsStockPageSelective(Pagination page, Map<String, Object> params);

	List<StockMadedate> selectGoodsMadeDates(int goodsId);
}
