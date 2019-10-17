package com.trenska.longwang.dao.goods;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.trenska.longwang.entity.goods.Goods;
import com.trenska.longwang.model.goods.GoodsExportModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author Owen
 * @since 2019-04-11
 */
//@CacheNamespace(implementation = RedisCacheMybatis.class , eviction = RedisCacheMybatis.class)
public interface GoodsMapper extends BaseMapper<Goods> {

	List<Goods> selectGoodsPage(Page page);

//	List<Goods> selectGoodsPageSelective(Goods goods, Pagination page);

	List<Goods> selectGoodsPageSelective(Pagination page, Map<String,Object> params);

	Integer selectCountSelective(Goods goods);

	Goods selectGoodsSimpleInfo(Integer goodsId);

	Goods selectUnDeletedGoodsByGoodsId(Integer goodsId);

	Goods selectGoodsByGoodsId(Integer goodsId);

	boolean deleteGoodsByGoodsId(Integer goodsId);

	/**
	 * 需要改变的库存，如果是减少传正数，增加传负数
	 * @param goodsId
	 * @param stock
	 * @return
	 */
	boolean updateGoodsStock(@Param("goodsId") Integer goodsId,@Param("stock") int stock);

	@Select("select good_name from t_goods where deleted = 0")
	List<String> selectGoodsNamesPage(Pagination page);

	List<Goods> selectGoodsRealSimpleInfo(Pagination page);

	int selectGoodsPageSelectiveCount(Map<String, Object> params);

	List<GoodsExportModel> selectGoodsExcelPageSelective(Page page, Map<String, Object> params);

//	boolean updateGoods(Goods goods);
}
