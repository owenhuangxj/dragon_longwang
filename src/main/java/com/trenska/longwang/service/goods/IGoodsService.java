package com.trenska.longwang.service.goods;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.entity.goods.Goods;
import com.trenska.longwang.model.goods.GoodsExportModel;
import com.trenska.longwang.model.goods.GoodsQueryModel;
import com.trenska.longwang.model.sys.ResponseModel;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商品业务层
 * </p>
 *
 * @author Owen
 * @since 2019-04-11
 */
public interface IGoodsService extends IService<Goods> {

	ResponseModel saveGoods(Goods goods);

	ResponseModel removeGoodsById(Integer goodsId);

	Boolean removeGoodsByIds(Collection<Integer> goodsIds);

	Page<Goods> getGoodsPage(Page page);

//	Page<Goods> getGoodsPageSelective(Goods goods, Page page);

//	Page<Goods> getGoodsPageByQueryModelSelective(GoodsQueryModel goodsQueryModel, Page page);

	boolean batchUpStatByIds(Collection<Integer> goodsIds);

	boolean batchDownStatByIds(Collection<Integer> goodsIds);

	Goods getGoodsByGoodsId(Integer goodsId);

	ResponseModel updateGoods(Goods goods);

	String getGoodsPropsByGoodsId(Integer goodsId);

	Page<String> getGoodsNamesPage(Page page);

	Page<Goods> getGoodsPageSelective(Page page, Map<String, Object> params);

	Page<GoodsExportModel> getGoodsExcelPageSelective(Page page, Map<String, Object> params);

	ResponseModel batchImportGoods(List<Goods> goods);
}

