package com.trenska.longwang.service.goods;

import com.baomidou.mybatisplus.extension.service.IService;
import com.trenska.longwang.entity.goods.GoodsSpec;

import java.util.List;

/**
 * <p>
 * 商品-规格 服务类接口
 * </p>
 *
 * @author Owen
 * @since 2019-04-11
 */
public interface IGoodsSpecService extends IService<GoodsSpec> {

	boolean updateGoodsSpecs(List<GoodsSpec> goodsSpecs);


}
