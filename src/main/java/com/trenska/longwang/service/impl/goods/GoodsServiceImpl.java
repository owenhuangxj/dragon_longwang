package com.trenska.longwang.service.impl.goods;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.constant.DragonConstant;
import com.trenska.longwang.context.ApplicationContextHolder;
import com.trenska.longwang.dao.goods.*;
import com.trenska.longwang.dao.indent.IndentDetailMapper;
import com.trenska.longwang.dao.stock.StockMapper;
import com.trenska.longwang.entity.goods.*;
import com.trenska.longwang.entity.indent.IndentDetail;
import com.trenska.longwang.entity.sys.SysConfig;
import com.trenska.longwang.model.goods.GoodsExportModel;
import com.trenska.longwang.model.sys.CommonResponse;
import com.trenska.longwang.service.goods.*;
import com.trenska.longwang.util.GoodsUtil;
import com.trenska.longwang.util.NumberUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	public CommonResponse saveGoods(Goods goods) {

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
			return CommonResponse.getInstance().succ(false).msg("生产批次不能为空.");
		}
		//GoodsUtil.dealGoodsInitStock(goods, stockMapper, request);
		if(initStock > 0) { // 如果客户选择了期初入库
			CommonResponse commonResponse = GoodsUtil.initGoodsStock(goods, stockMapper);
			if (!commonResponse.getSucc()) {
				return commonResponse;
			}
		}
		return CommonResponse.getInstance().succ(true).msg("新建商品成功.");
	}

	@Override
	@Transactional
	public CommonResponse removeGoodsById(Integer goodsId) {

		// 如果商品还有没有完成的订单，商品不能被删除
		List<IndentDetail> indentDetails = indentDetailMapper.selectUndeletable(goodsId);

		if (CollectionUtils.isNotEmpty(indentDetails)) {
			return CommonResponse.getInstance().succ(false).msg("商品还有没有完成的订单，不能删除.");
		}

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

		return CommonResponse.getInstance().succ(true).msg("删除商品成功");

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
	public CommonResponse updateGoods(Goods goods) {

		if (goods == null) {
			CommonResponse.getInstance().succ(false).msg("商品信息不能为空");
		}

		Integer goodsId = goods.getGoodsId();
		if (!NumberUtil.isIntegerUsable(goodsId)) {
			CommonResponse.getInstance().succ(false).msg("无效的商品信息");
		}

		Goods oldGoods = this.getGoodsByGoodsId(goodsId);
		if (null == oldGoods) {
			CommonResponse.getInstance().succ(false).msg("无效的商品信息");
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

		return CommonResponse.getInstance().succ(true).msg("修改商品成功");

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
			if (CollectionUtils.isNotEmpty(goodsSpecs)) {
				for (GoodsSpec goodsSpec : goodsSpecs) {
					propName += (goodsSpec.getPropName() + "\r\n");
				}
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
				goods.setStatus(DragonConstant.ON_SALE);
			}else{
				goods.setStatus(DragonConstant.OFF_SALE);
			}
		}
		page.setTotal(total);
		page.setRecords(records);
		return page;
	}

	@Override
	@Transactional
	public CommonResponse batchImportGoods(List<Goods> goods) {

		List<Unit> usedUnits = new ArrayList<>();
		List<Category> usedCategories = new ArrayList<>();

		for (Goods good : goods) {

			// 处理期初库存
			if(good.getInitStock() == null || good.getInitStock() < 0)
				good.setInitStock(0);

			// 处理总库存
			good.setStock(good.getInitStock());

			// 处理复合字段 goodsNo,goodsName,barcode
			String combine = GoodsUtil.dealGoodsCombineProperty(good);
			good.setCombine(combine);

			// 处理主单位
			String mainUnit = good.getMainUnit();
			if(StringUtils.isNotEmpty(mainUnit)) {
				Unit unit = new Unit().selectOne(
						new LambdaQueryWrapper<Unit>()
								.eq(Unit::getUnitName, mainUnit)
				);
				// 如果没有主单位不进行导入==>从链表中移除
				if (unit == null)
					goods.remove(good);
				else {
					good.setMainUnitId(unit.getUnitId());
					// 如果单位可以被删除（没有被使用），存入已使用集合中去
					if (unit.getDeletable() == true)
						usedUnits.add(unit);
				}
			}
			// 处理辅助单位
			String subUnit = good.getSubUnit();
			if(StringUtils.isNotEmpty(subUnit)){
				Unit unit = new Unit().selectOne(
						new LambdaQueryWrapper<Unit>()
								.eq(Unit::getUnitName,subUnit)
				);
				// 如果输入的辅助单位不存在==>设置辅助单位为null
				if(unit == null) {
					good.setSubUnitId(null);
					good.setSubUnit(null);
				}
			}

			// 处理主/辅 单位的乘积因子 ,如果 < 0 ，设置倍率为
			Integer multi = good.getMulti();
			if(NumberUtil.isIntegerNotUsable(multi))
				good.setMulti(1);

			// 处理分类
			String frtCatName = good.getFrtCatName();
			if(StringUtils.isNotEmpty(frtCatName)){
				Category category = new Category().selectOne(
						new LambdaQueryWrapper<Category>()
								.eq(Category::getCatName,frtCatName)
				);

				// 如果分类不存在，设置为无分类
				if(category == null)
					good.setFrtCatName("无分类");
				else{
					// 有一级分类
					usedCategories.add(category);
					// 处理二级分类
					String scdCatName = good.getScdCatName();
					if(StringUtils.isNotEmpty(scdCatName)){
						Category scdCategory = new Category().selectOne(
								new LambdaQueryWrapper<Category>()
										.eq(Category::getCatName,scdCatName)
										.eq(Category::getPid,category.getCatId())
						);
						// 如果二级分类为空或者没有匹配的二级分类，设置二级分类为null
						if(scdCategory == null)
							good.setScdCatName(null);
						else {
							// 如果分类还没有被使用
							if(scdCategory.getDeletable() == true)
								usedCategories.add(scdCategory);
						}
					}
				}
			}else{ // 为空==>设置为无分类
				good.setFrtCatName("无分类");
			}

			// 处理过期时间,如果为 < 0 设置为null
			Integer expire = good.getExpire();
			if(NumberUtil.isIntegerNotUsable(expire))
				good.setExpire(null);
		}
		return CommonResponse.getInstance().succ(true).msg("导入成功！");
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
	private CommonResponse dealGoodsParams(Goods goods){

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
		if (CollectionUtils.isNotEmpty(goods.getGoodsSpecs())) {

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
		if (CollectionUtils.isNotEmpty(goods.getPriceGrps())) {
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
		if (CollectionUtils.isNotEmpty(goods.getSpecialPrices())) {
			List<GoodsCustSpecify> insertingGoodsCustSpecifies = new ArrayList<>();
			goods.getSpecialPrices().forEach(specialPrice -> {
				specialPrice.setGoodsId(goodsId);
				insertingGoodsCustSpecifies.add(specialPrice);
			});
			goodsCustSpecialService.saveBatch(insertingGoodsCustSpecifies);
		}

		return CommonResponse.getInstance().succ(true).msg("OK");
	}

}