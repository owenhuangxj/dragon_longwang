package com.trenska.longwang.service.impl.goods;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.constant.Constant;
import com.trenska.longwang.context.ApplicationContextHolder;
import com.trenska.longwang.dao.goods.*;
import com.trenska.longwang.dao.indent.IndentDetailMapper;
import com.trenska.longwang.dao.stock.StockMapper;
import com.trenska.longwang.entity.goods.*;
import com.trenska.longwang.entity.indent.IndentDetail;
import com.trenska.longwang.entity.sys.SysConfig;
import com.trenska.longwang.enums.IndentStat;
import com.trenska.longwang.model.goods.GoodsExportModel;
import com.trenska.longwang.model.goods.GoodsQueryModel;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.goods.*;
import com.trenska.longwang.util.GoodsUtil;
import com.trenska.longwang.util.NumberUtil;
import com.trenska.longwang.util.ObjectCopier;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品业务实现层
 *
 * @author Owen
 * @since 2019-04-11
 */
@SuppressWarnings("all")
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {
	@Autowired
	private GoodsSpecMapper goodsSpecMapper;

	@Autowired
	private IGoodsSpecService goodsSpecService;

	@Autowired
	private SpecMapper specMapper;

	@Autowired
	private ISpecService specService;

	@Autowired
	private SpecPropertyMapper specPropertyMapper;

	@Autowired
	private ISpecPropertyService specPropertyService;

	@Autowired
	private CategoryMapper categoryMapper;

	@Autowired
	private UnitMapper unitMapper;

	@Autowired
	private BrandMapper brandMapper;

	@Autowired
	private StockMapper stockMapper;

	@Autowired
	GoodsPriceGrpMapper goodsPriceGrpMapper;

	@Autowired
	private IGoodsPriceGrpService goodsPriceGrpService;

	@Autowired
	private GoodsCustSpecialMapper goodsCustSpecialMapper;

	@Autowired
	private IGoodsCustSpecialService goodsCustSpecialService;

	@Autowired
	private IndentDetailMapper indentDetailMapper;

	@Autowired
	private RedisTemplate<String,String> redisTemplate;

	@Override
	@Transactional
	public ResponseModel saveGoods(Goods goods, HttpServletRequest request) {

		if (StringUtils.isEmpty(goods.getFrtCatName())){
			goods.setFrtCatName("无分类");
		}

		// 处理商品的复合字段->combine
		goods.setCombine(GoodsUtil.dealGoodsCombineProperty(goods));

		//保存商品
		this.save(goods);

		// 处理商品的规格，规格值，特价等参数
		dealGoodsParams(goods);

		Integer initStock = goods.getInitStock();
		if (ObjectUtils.isEmpty(initStock)){
			initStock = 0 ;
		}
		// 处理商品的期初库存
		if(initStock > 0 && ObjectUtils.isEmpty(goods.getInitMadeDate())){
			return ResponseModel.getInstance().succ(false).msg("生产批次不能为空.");
		}
		//GoodsUtil.dealGoodsInitStock(goods, stockMapper, request);
		if(initStock > 0) { // 如果客户选择了期初入库
			ResponseModel responseModel = GoodsUtil.initGoodsStock(goods, stockMapper, request);
			if (!responseModel.getSucc()) {
				return responseModel;
			}
		}
		return ResponseModel.getInstance().succ(true).msg("新建商品成功.");
	}

	@Override
	@Transactional
	public ResponseModel removeGoodsById(Integer goodsId) {

		// 如果商品还有没有完成的订单，商品不能被删除
		List<IndentDetail> indentDetails = indentDetailMapper.selectUndeletable(goodsId);

		if (CollectionUtils.isNotEmpty(indentDetails)) {
			return ResponseModel.getInstance().succ(false).msg("商品还有没有完成的订单，不能删除.");
		}

		// 商品的规格信息不能删除，因为查看订货单信息的时候需要获取规格信息
//		goodsSpecMapper.delete(
//				new QueryWrapper<GoodsSpec>()
//						.eq("goods_id", goodsId)
//		);

		goodsCustSpecialMapper.delete(
				new LambdaQueryWrapper<GoodsCustSpecify>()
						.eq(GoodsCustSpecify::getCustId, goodsId)
		);
		goodsPriceGrpMapper.delete(
				new LambdaQueryWrapper<GoodsPriceGrp>()
						.eq(GoodsPriceGrp::getGoodsId, goodsId)
		);

		this.removeById(goodsId);

		this.setDeletable();

		return ResponseModel.getInstance().succ(true).msg("删除商品成功");

	}

	@Override
	@Transactional
	public Boolean removeGoodsByIds(Collection<Integer> goodsIds) {
		if (goodsIds != null && !goodsIds.isEmpty()) {
			goodsSpecMapper.delete(new QueryWrapper<GoodsSpec>().in("goods_id", goodsIds));
			new GoodsCustSpecify().delete(
					new QueryWrapper<GoodsCustSpecify>()
							.in("goods_id", goodsIds)
			);
			new GoodsPriceGrp().delete(
					new QueryWrapper<GoodsPriceGrp>()
							.in("goods_id", goodsIds)
			);

			new GoodsSpec().delete(
					new QueryWrapper<GoodsSpec>()
							.in("goods_id", goodsIds)
			);
		}
		removeByIds(goodsIds);

		setDeletable();

		return true;
	}

	@Override
	public Page<Goods> getGoodsPage(Page page) {
		List<Goods> goods = super.baseMapper.selectGoodsPage(page);
		// 如果系统参数设置为不显示图片，则图片配置为null
		if (!ApplicationContextHolder.getBean(SysConfig.class).getGoodsImgable()) {
			goods.forEach(gds -> {
				gds.setImg(null);
			});
		}
		page.setRecords(goods);
		page.setTotal(count());
		return page;
	}

//	@Override
//	public Page<Goods> getGoodsPageSelective(Goods goods, Page page) {
//		List<Goods> records = super.baseMapper.selectGoodsPageSelective(goods, page);
//		page.setRecords(records);
//		page.setTotal(super.baseMapper.selectCountSelective(goods));
//		return page;
//	}

//	@Override
//	public Page<Goods> getGoodsPageByQueryModelSelective(GoodsQueryModel goodsQueryModel, Page page) {
//		Goods goods = new Goods();
//		ObjectCopier.copyProperties(goodsQueryModel, goods);
//		List<Goods> records = super.baseMapper.selectGoodsPageSelective(goods, page);
//		for (Goods gds : records) {
//			List<GoodsSpec> goodsSpecs = goodsSpecMapper.selectGoodsSpecByGoodsIdAndPropName(gds.getGoodsId(), goodsQueryModel.getPropName());
//			gds.setGoodsSpecs(goodsSpecs);
//		}
//		page.setRecords(records);
//		page.setTotal(super.baseMapper.selectCountSelective(goods));
//		return page;
//	}

	@Override
	@Transactional
	public boolean batchUpStatByIds(Collection<Integer> goodsIds) {
		return update(new UpdateWrapper<Goods>().in("goods_id", goodsIds).set("stat", true));
	}

	@Override
	@Transactional
	public boolean batchDownStatByIds(Collection<Integer> goodsIds) {
		return update(new UpdateWrapper<Goods>().in("goods_id", goodsIds).set("stat", false));
	}

	@Override
	public Goods getGoodsByGoodsId(Integer goodsId) {
		return baseMapper.selectUnDeletedGoodsByGoodsId(goodsId);
	}

	@Override
	@Transactional
	public ResponseModel updateGoods(Goods goods) {

		if (goods == null) {
			ResponseModel.getInstance().succ(false).msg("商品信息不能为空");
		}

		Integer goodsId = goods.getGoodsId();
		if (!NumberUtil.isIntegerUsable(goodsId)) {
			ResponseModel.getInstance().succ(false).msg("无效的商品信息");
		}

		Goods oldGoods = this.getGoodsByGoodsId(goodsId);
		if (null == oldGoods) {
			ResponseModel.getInstance().succ(false).msg("无效的商品信息");
		}

		// 期初库存不可修改 需要保留旧值
		if(NumberUtil.isIntegerUsable(oldGoods.getInitStock())) {
			goods.setInitStock(oldGoods.getInitStock());
		}
		// 乘积因子不可修改 需要保留旧值
		if(NumberUtil.isIntegerUsable(oldGoods.getMulti())) {
			goods.setMulti(oldGoods.getMulti());
		}

		// 二级分类需要特殊处理
		if(StringUtils.isEmpty(goods.getScdCatName())){
			goods.setScdCatName("");
		}

		// 删除商品规格信息
		goodsSpecService.remove(
				new LambdaQueryWrapper<GoodsSpec>()
						.eq(GoodsSpec::getGoodsId, goodsId)
		);

		// 删除商品的价格分组信息
		goodsPriceGrpService.remove(
				new LambdaQueryWrapper<GoodsPriceGrp>()
						.eq(GoodsPriceGrp::getGoodsId, goodsId)
		);

		// 删除商品的特价信息
		goodsCustSpecialService.remove(
				new LambdaQueryWrapper<GoodsCustSpecify>()
						.eq(GoodsCustSpecify::getGoodsId,goodsId)
		);

		// 处理商品的规格等参数
		dealGoodsParams(goods);

		//处理商品的复合查询字段
		goods.setCombine(GoodsUtil.dealGoodsCombineProperty(goods));

		// 保存新的商品信息 新商品信息的id、期初库存、乘积因子为旧商品信息
		this.updateById(goods);

		setDeletable();

		return ResponseModel.getInstance().succ(true).msg("修改商品成功");

	}

	@Override
	public String getGoodsPropsByGoodsId(Integer goodsId) {
		if(null == goodsId || goodsId <= 0){
			return "";
		}
		List<GoodsSpec> goodsSpecs = goodsSpecService.list(
				new LambdaQueryWrapper<GoodsSpec>()
						.eq(GoodsSpec::getGoodsId, goodsId)
		);

		String props = "";
		Set<@NotNull String> propNames = goodsSpecs.stream().map(GoodsSpec::getPropName).collect(Collectors.toSet());

		for(String propName : propNames){
			if(null != propName) {
				props =  props.concat(propName).concat(" ");
			}
		}
		return props.trim();
	}

	@Override
	public Page<String> getGoodsNamesPage(Page page) {
		List<String> records = super.baseMapper.selectGoodsNamesPage(page);
		page.setRecords(records);
		page.setTotal(count());
		return page;
	}

	@Override
	public Page<Goods> getGoodsPageSelective(Page page, Map<String, Object> params) {
		List<Goods> records = super.baseMapper.selectGoodsPageSelective(page, params);
		int total = super.baseMapper.selectGoodsPageSelectiveCount(params);
		for (Goods goods : records){
			Collection<GoodsSpec> goodsSpecs = goods.getGoodsSpecs();
			String propName = "";
			for(GoodsSpec goodsSpec : goodsSpecs){
				propName += (goodsSpec.getPropName() + "\r\n");
			}
			goods.setPropName(propName);
			int avbStock = goods.getStock() - goods.getStockout();
			goods.setAvbStock(avbStock);
		}
		page.setTotal(total);
		page.setRecords(records);
		return page;
	}

	@Override
	public Page<GoodsExportModel> getGoodsExcelPageSelective(Page page, Map<String, Object> params) {
		List<GoodsExportModel> records = super.baseMapper.selectGoodsExcelPageSelective(page, params);
		int total = super.baseMapper.selectGoodsPageSelectiveCount(params);
		for (GoodsExportModel goods : records){
			Collection<GoodsSpec> goodsSpecs = goods.getGoodsSpecs();
			String propName = "";
			for(GoodsSpec goodsSpec : goodsSpecs){
				propName += (goodsSpec.getPropName() + "\r\n");
			}
			goods.setPropName(propName);
			if(true == goods.getStat()){
				goods.setStatus(Constant.ON_SALE);
			}else{
				goods.setStatus(Constant.OFF_SALE);
			}
		}
		page.setTotal(total);
		page.setRecords(records);
		return page;
	}


	private void setDeletable() {
		/**
		 * 处理所有没有被使用的规格，将没有被使用的规格设置为可以被删除
		 */

		specMapper.setDeletable();
		/**
		 * 处理所有没有被使用的规格值，将没有被使用的规格值设置为可以被删除
		 */
		specPropertyMapper.setDeletable();

		/**
		 * 处理所有没有被使用的分类，将没有被使用的分类设置为可以被删除
		 */
		categoryMapper.setDeletable();

		/**
		 * 处理所有没有被使用的单位，将没有被使用的单位设置为可以被删除
		 */
		unitMapper.setDeletable();
		/**
		 * 处理所有没有被使用的品牌，将没有被使用的品牌设置为可以被删除
		 */
		brandMapper.setDeletable();
	}

	/**
	 * 处理商品的规格、单位等商品信息
	 * @param goods
	 * @return
	 */
	private ResponseModel dealGoodsParams(Goods goods){

		if (null == goods.getRemarks()){
			goods.setRemarks("");
		}

		if (StringUtils.isNotEmpty(goods.getBrandName()) && !"无品牌".equals(goods.getBrandName())) {
			//如果用户选择了品牌则处理品牌，让对应品牌不可以删除
			brandMapper.setUndeletable(goods.getBrandName());
		}

		if (NumberUtil.isIntegerUsable(goods.getCatId())) {
			// 如果用户选择了分类则处理分类，让对应分类不可以删除
			categoryMapper.updateById(new Category(goods.getCatId(), false));
		}

		if(StringUtils.isNotEmpty(goods.getScdCatName())){
			categoryMapper.setUndeletable(goods.getScdCatName());
		}

		// 如果选择了单位则处理单位，让对应单位不可以删除
		if (NumberUtil.isIntegerUsable(goods.getMainUnitId())) {
			unitMapper.updateById(new Unit(goods.getMainUnitId(), false));
		}

		if (NumberUtil.isIntegerUsable(goods.getSubUnitId())) {
			unitMapper.updateById(new Unit(goods.getSubUnitId(), false));
		}

		Integer goodsId = goods.getGoodsId();

		Collection<GoodsSpec> insertingGoodsSpecs = new ArrayList<>();

		Collection<Spec> updatingSpecs = new ArrayList<>();
		Collection<SpecProperty> updatingSpecProperties = new ArrayList<>();

		// 如果新建商品选择了规格，则需要存储商品规格 goods : spec = 1:n
		if (!goods.getGoodsSpecs().isEmpty()) {

			for (GoodsSpec goodsSpec : goods.getGoodsSpecs()) {

				goodsSpec.setGoodsId(goodsId);
				insertingGoodsSpecs.add(goodsSpec);

				// 处理分类，让对应规格不可以删除
				updatingSpecs.add(new Spec(goodsSpec.getSpecId(), false));

				// 处理分类，让对应规格属性不可以删除
				updatingSpecProperties.add(new SpecProperty(goodsSpec.getSpecPropId(), false));
			}

			goodsSpecService.saveBatch(insertingGoodsSpecs);

			specService.updateBatchById(updatingSpecs);

			specPropertyService.updateBatchById(updatingSpecProperties);

		}
		/**
		 * 处理商品的价格分组信息
		 */
		if (!goods.getPriceGrps().isEmpty()) {
			List<GoodsPriceGrp> insertingPriceGrps = new ArrayList<>();
			goods.getPriceGrps().forEach(priceGrp -> {
				priceGrp.setGoodsId(goodsId);
				insertingPriceGrps.add(priceGrp);
			});
			goodsPriceGrpService.saveBatch(insertingPriceGrps);
		}

		/**
		 * 处理商品的客户特价/指定价
		 */
		if (!goods.getSpecialPrices().isEmpty()) {
			List<GoodsCustSpecify> insertingGoodsCustSpecifies = new ArrayList<>();
			goods.getSpecialPrices().forEach(specialPrice -> {
				specialPrice.setGoodsId(goodsId);
				insertingGoodsCustSpecifies.add(specialPrice);
			});
			goodsCustSpecialService.saveBatch(insertingGoodsCustSpecifies);
		}

		return ResponseModel.getInstance().succ(true).msg("OK");
	}

}