package com.trenska.longwang.dao.indent;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;
import com.trenska.longwang.entity.indent.IndentDetail;
import com.trenska.longwang.model.report.SingleGoodsSalesIndentDetailModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 订货单 Mapper 接口
 *
 * @author Owen
 * @since 2019-04-23
 */
//@CacheNamespace(implementation = RedisCacheMybatis.class, eviction = RedisCacheMybatis.class)
public interface IndentDetailMapper extends BaseMapper<IndentDetail> {
	List<IndentDetail> selectByIndentNo(String IndentNo);

	/**
	 * 真实删除 订货详情
	 * @param indentNo
	 * @return
	 */
	boolean actualDeleteIndentDetail(String indentNo);

	List<SingleGoodsSalesIndentDetailModel> selectSingleGoodsIndentDetail(Pagination page, Map<String ,Object> params);

	int selectSingleGoodsIndentDetailCount(Map<String,Object> params);

	@Update("update t_indent_detail set stockout = stockout + #{stockoutNum} where detail_id = #{detailId}")
	boolean updateStockoutNum(@Param("detailId") Long detailId, @Param("stockoutNum") int stockoutNum);

	List<IndentDetail> selectUndeletable(Integer goodsId);

	@Select("select * from t_indent_detail where detail_id = #{detailId} for update")
	IndentDetail selectByDetailId(Long detailId);
}
