package com.trenska.longwang.service.impl.goods;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trenska.longwang.dao.goods.GoodsSpecMapper;
import com.trenska.longwang.entity.goods.GoodsSpec;
import com.trenska.longwang.service.goods.IGoodsSpecService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品-规格 服务实现类
 * @author Owen
 * @since 2019-04-11
 */
@Service
public class GoodsSpecServiceImpl extends ServiceImpl<GoodsSpecMapper, GoodsSpec> implements IGoodsSpecService {

	@Override
	public boolean updateGoodsSpecs(List<GoodsSpec> goodsSpecs) {
		/**
		 * 为了方便处理 ，在修改商品的规格信息之前删除所有的旧的商品规格信息
		 */
		remove(new QueryWrapper<GoodsSpec>().eq("goods_id",goodsSpecs.get(0).getGoodsId()));
		return saveBatch(goodsSpecs);
	}
}