package com.trenska.longwang.service.impl.stock;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.dao.goods.GoodsMapper;
import com.trenska.longwang.dao.stock.GoodsStockMapper;
import com.trenska.longwang.dao.stock.StockDetailMapper;
import com.trenska.longwang.entity.goods.Goods;
import com.trenska.longwang.entity.indent.StockMadedate;
import com.trenska.longwang.entity.stock.GoodsStock;
import com.trenska.longwang.model.stock.GoodsStockModel;
import com.trenska.longwang.service.stock.IGoodsStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Owen
 * @since 2019-06-15
 */
@Service
@SuppressWarnings("all")
public class GoodsStockServiceImpl extends ServiceImpl<GoodsStockMapper, GoodsStock> implements IGoodsStockService {

	@Autowired
	private GoodsMapper goodsMapper;
	@Autowired
	private StockDetailMapper stockDetailMapper;

	@Override
	public Page<GoodsStock> getGoodsMadeDate(Map<String, Object> params, Page page) {
		Serializable goodsId = (Serializable) params.get("goodsId");
		List<GoodsStock> goodsStocks = super.baseMapper.selectGoodsStockPageSelective(page,params);
		int total;
		if(Objects.isNull(goodsId)){
			total = this.count();
		}else{
			total = count(
					new LambdaQueryWrapper<GoodsStock>()
							.eq(true,GoodsStock::getGoodsId,goodsId)
			);
		}
		page.setRecords(goodsStocks);
		page.setTotal(total);
		return page;
	}

	@Override
	public Page<GoodsStockModel> getGoodsMadeDates(Page page) {
		List<Goods> goods = goodsMapper.selectGoodsRealSimpleInfo(page);
		List<GoodsStockModel> goodsStockModels = new ArrayList<>();
		for(Goods good : goods){
			Integer goodsId = good.getGoodsId();
			List<StockMadedate> madeDates = super.baseMapper.selectGoodsMadeDates(goodsId);
			GoodsStockModel goodsStockModel = new GoodsStockModel();
			goodsStockModel.setGoods(good);
			goodsStockModel.setMadedates(madeDates);
			goodsStockModels.add(goodsStockModel);

		}
		page.setRecords(goodsStockModels);
		page.setTotal(goods.size());
		return page;
	}
}
