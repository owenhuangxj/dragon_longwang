package com.trenska.longwang.controller.goods;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.annotation.CheckDuplicateSubmit;
import com.trenska.longwang.constant.Constant;
import com.trenska.longwang.entity.PageHelper;
import com.trenska.longwang.entity.goods.Goods;
import com.trenska.longwang.entity.goods.GoodsSpec;
import com.trenska.longwang.excel_import.GoodsImportListener;
import com.trenska.longwang.model.goods.GoodsExportModel;
import com.trenska.longwang.model.sys.ExistModel;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.goods.IGoodsService;
import com.trenska.longwang.service.goods.IGoodsSpecService;
import com.trenska.longwang.util.*;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * 商品前端控制器
 *
 * @author Owen
 * @since 2019-04-11
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/goods")
@Api(description = "商品接口")
public class GoodsController {

	@Autowired
	private IGoodsService goodsService;
	@Autowired
	private IGoodsSpecService goodsSpecService;

	//	@ApiImplicitParams({
//			@ApiImplicitParam(name = "goodsName", value = "商品名称", paramType = "body", required = true, dataType = "string"),
//			@ApiImplicitParam(name = "goodsNo", value = "商品编号", paramType = "body", dataType = "string"),
//			@ApiImplicitParam(name = "barcode", value = "商品条码", paramType = "body", required = true, dataType = "string"),
//			@ApiImplicitParam(name = "price", value = "商品价格，以字符串的形式接收", paramType = "body", required = true, dataType = "string"),
//			@ApiImplicitParam(name = "initStock", value = "商品期初库存", paramType = "body", required = true, dataType = "int"),
//			@ApiImplicitParam(name = "mainUnit", value = "商品主单位", paramType = "body", required = true, dataType = "string"),
//			@ApiImplicitParam(name = "subUnit", value = "商品辅单位", paramType = "body", dataType = "string"),
//			@ApiImplicitParam(name = "minOdrQtt", value = "商品最小订单量", paramType = "body", dataType = "string"),
//			@ApiImplicitParam(name = "multi", value = "商品主单位和辅助单位的乘积因子", paramType = "body", dataType = "int"),
//			@ApiImplicitParam(name = "frtCatName", value = "商品一级类型名称", paramType = "body", required = true, dataType = "string"),
//			@ApiImplicitParam(name = "scdCatName", value = "商品二级类型名称", paramType = "body", dataType = "string"),
//			@ApiImplicitParam(name = "brandName", value = "品牌名称", paramType = "body", required = true, dataType = "string"),
//			@ApiImplicitParam(name = "expire", value = "商品过期时间，以字符串的形式接收", paramType = "body", required = true, dataType = "string"),
//			@ApiImplicitParam(name = "img", value = "商品图片路径", paramType = "body", required = true, dataType = "string"),
//			@ApiImplicitParam(name = "price", value = "商品价格,以字符串的形式接收", paramType = "body", required = true, dataType = "string"),
//			@ApiImplicitParam(name = "remarks", value = "商品备注，12字以内", paramType = "body", required = true, dataType = "string"),
//			@ApiImplicitParam(name = "goodsSpecs", value = "商品规格(参数)", paramType = "body", required = true, dataType = "set"),
//			@ApiImplicitParam(name = "priceGrps", value = "商品-价格分组", paramType = "body", required = true, dataType = "list"),
//			@ApiImplicitParam(name = "specialPrices", value = "商品-客户特价", paramType = "body", required = true, dataType = "list")
//
//	})
//	@ApiOperation("添加商品，返回对象中data属性是添加成功的商品的id")
	@PostMapping("/add")
	@CheckDuplicateSubmit
	public ResponseModel addGoods(@RequestBody @Valid @ApiParam Goods goods, HttpServletRequest request) {
		if (goods == null) {
			ResponseModel.getInstance().succ(false).msg("无效商品，请完善商品信息");
		}
		return goodsService.saveGoods(goods, request);
	}

	@CheckDuplicateSubmit
	@ApiOperation("删除商品")
	@DeleteMapping("/delete/{goodsId}")
	public ResponseModel deleteGoods(@ApiParam(name = "goodsId", required = true) @PathVariable("goodsId") Integer goodsId) {

		if (!NumberUtil.isIntegerUsable(goodsId)) {
			return ResponseModel.getInstance().succ(false).msg("商品删除失败:无此商品");
		}

		return goodsService.removeGoodsById(goodsId);
	}

	@CheckDuplicateSubmit
	@DeleteMapping("/delete/batch")
	@ApiOperation("批量删除商品")
	public ResponseModel batchDeleteGoods(@ApiParam(name = "goodsIds", value = "需要批量删除的商品id集合/数组", required = true) @RequestParam(value = "goodsIds") Collection<Integer> goodsIds) {
		Boolean removed = goodsService.removeGoodsByIds(goodsIds);
		return ResponseModel.getInstance().succ(removed).msg(removed ? "商品删除成功" : "商品删除失败");
	}

	@CheckDuplicateSubmit
	@PutMapping("/update")
//	@ApiImplicitParams({
//			@ApiImplicitParam(name = "goodsId", value = "商品id", paramType = "body", dataType = "int"),
//			@ApiImplicitParam(name = "goodsName", value = "商品名称", paramType = "body", dataType = "string"),
//			@ApiImplicitParam(name = "goodsNo", value = "商品编号", paramType = "body", dataType = "string"),
//			@ApiImplicitParam(name = "barcode", value = "商品条码", paramType = "body", dataType = "string"),
//			@ApiImplicitParam(name = "price", value = "商品价格，以字符串的形式接收", paramType = "body", dataType = "string"),
//			@ApiImplicitParam(name = "mainUnit", value = "商品主单位", paramType = "body", dataType = "string"),
//			@ApiImplicitParam(name = "subUnit", value = "商品辅单位", paramType = "body", dataType = "string"),
//			@ApiImplicitParam(name = "minOdrQtt", value = "商品最小订单量", paramType = "body", dataType = "int"),
//			@ApiImplicitParam(name = "multi", value = "商品主单位和辅助单位的乘积因子", paramType = "body", dataType = "int"),
//			@ApiImplicitParam(name = "frtCatName", value = "商品一级分类名称", paramType = "body", dataType = "string"),
//			@ApiImplicitParam(name = "scdCatName", value = "商品二级分类名称", paramType = "body", dataType = "string"),
//			@ApiImplicitParam(name = "brandName", value = "品牌名称", paramType = "body", dataType = "string"),
//			@ApiImplicitParam(name = "stat", value = "商品上架/下架", paramType = "body", dataType = "boolean"),
//			@ApiImplicitParam(name = "img", value = "商品图片路径", paramType = "body", dataType = "string"),
//			@ApiImplicitParam(name = "remarks", value = "商品备注，12字以内", paramType = "body", dataType = "string"),
//			@ApiImplicitParam(name = "goodsSpecs", value = "商品规格(参数)", paramType = "body", dataType = "set"),
//			@ApiImplicitParam(name = "priceGrps", value = "商品-价格分组,不用传递goodsId,但是修改旧值必须传递good_price_grp_id", paramType = "body", dataType = "list"),
//			@ApiImplicitParam(name = "specialPrices", value = "商品-客户特价,不用传递goodsId,但是修改旧值必须传递specifyId", paramType = "body", dataType = "list")
//	})
	@ApiOperation("修改商品")
	public ResponseModel updateGoods(@RequestBody Goods goods) {
		if (goods == null) {
			return ResponseModel.getInstance().succ(false).msg("商品不能为空");
		}
		/**
		 * 由于使用的组合字段，所以需要特殊处理
		 */
		return goodsService.updateGoods(goods);
	}

	@CheckDuplicateSubmit
	@PutMapping("/update/stat/up")
	@ApiOperation("批量上架商品")
	public ResponseModel batchUpGoodsStat(@NotNull @RequestParam Collection<Integer> goodsIds) {
		boolean successful = goodsService.batchUpStatByIds(goodsIds);
		return ResponseModel.getInstance().succ(successful).msg(successful ? "批量上架商品成功" : "批量上架商品失败");
	}

	@CheckDuplicateSubmit
	@PutMapping("/update/stat/down")
	@ApiOperation("批量上架商品")
	public ResponseModel batchDownGoodsStat(@Valid @RequestParam Collection<Integer> goodsIds) {
		boolean successful = goodsService.batchDownStatByIds(goodsIds);
		return ResponseModel.getInstance().succ(successful).msg(successful ? "批量下架商品成功" : "批量下架商品失败");
	}

	@CheckDuplicateSubmit
	@PutMapping("/spec/update")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "goodsId", paramType = "body", required = true, dataType = "int"),
			@ApiImplicitParam(name = "specId", paramType = "body", required = true, dataType = "int"),
			@ApiImplicitParam(name = "specPropId", paramType = "body", required = true, dataType = "int"),
			@ApiImplicitParam(name = "specName", paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "propName", paramType = "body", required = true, dataType = "string")
	})
	@ApiOperation("修改商品规格")
	public ResponseModel updateGoodsSpec(@Valid @RequestBody List<GoodsSpec> goodsSpecs) {
		boolean successful = goodsSpecService.updateGoodsSpecs(goodsSpecs);
		return ResponseModel.getInstance().succ(successful).msg(successful ? "商品规格修改成功" : "商品规格修改失败");
	}

	@GetMapping("/list/page/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true, dataType = "int")
	})
	@ApiOperation("普通分页")
	public PageHelper<Goods> listGoodsPage(@PathVariable("current") Integer current, @PathVariable("size") Integer size) {
		Page<Goods> pageInfo = goodsService.getGoodsPage(PageUtils.getPageParam(new PageHelper(current, size)));
		return PageHelper.getInstance().pageData(pageInfo);
	}

//	@PostMapping("/list/page/search/{current}/{size}")
//	@ApiImplicitParams({
//			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
//			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
//			@ApiImplicitParam(name = "combine", value = "商品名称/编号/条码", paramType = "query", dataType = "string"),
//			@ApiImplicitParam(name = "frtCatName", value = "商品一级分类名称", paramType = "body", dataType = "string"),
//			@ApiImplicitParam(name = "scdCatName", value = "商品二级分类名称", paramType = "body", dataType = "string"),
//			@ApiImplicitParam(name = "brandName", value = "品牌名称", paramType = "body", dataType = "string"),
//			@ApiImplicitParam(name = "propName", value = "商品规格值", paramType = "body", dataType = "string"),
//			@ApiImplicitParam(name = "stat", value = "商品状态", paramType = "body", dataType = "boolean")
//	})
//	@ApiOperation("条件查询分页")
//	public PageHelper<Goods> listGoodsPageSelective(@PathVariable("current") Integer current, @PathVariable("size") Integer size, @RequestBody GoodsQueryModel goodsQueryModel) {
//		Page<Goods> pageInfo = null;
//		Page page = PageUtils.getPageParam(new PageHelper(current, size));
//		if (goodsQueryModel.getPropName() == null) {
//			Goods goods = new Goods();
//			ObjectCopier.copyProperties(goodsQueryModel, goods);
//			pageInfo = goodsService.getGoodsPageSelective(goods, page);
//		} else {
//			pageInfo = goodsService.getGoodsPageByQueryModelSelective(goodsQueryModel, page);
//		}
//		return PageHelper.getInstance().pageData(pageInfo);
//	}

	@GetMapping("/list/page/search/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "combine", value = "商品名称/编号/条码", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "frtCatName", value = "商品一级分类名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "scdCatName", value = "商品二级分类名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "brandName", value = "品牌名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "propName", value = "商品单规格值", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "stat", value = "商品状态", paramType = "query", dataType = "boolean"),
			@ApiImplicitParam(name = "minOdrQtt", value = "最小起订量", paramType = "query", dataType = "int")
	})
	@ApiOperation("查询")
	public PageHelper<Goods> listGoodsPageQueryParamSelective(
			@PathVariable("current") Integer current, @PathVariable("size") Integer size,
			@RequestParam(required = false, name = "stat") Boolean stat,
			@RequestParam(required = false, name = "combine") String combine,
			@RequestParam(required = false, name = "propName") String propName,
			@RequestParam(required = false, name = "brandName") String brandName,
			@RequestParam(required = false, name = "minOdrQtt") Integer minOdrQtt,
			@RequestParam(required = false, name = "frtCatName") String frtCatName,
			@RequestParam(required = false, name = "scdCatName") String scdCatName
	) {
		Map<String, Object> params = new HashMap<>();
		params.put("stat", stat);
		params.put("combine", combine);
		params.put("propName", propName);
		params.put("brandName", brandName);
		params.put("minOdrQtt", minOdrQtt);
		params.put("frtCatName", frtCatName);
		params.put("scdCatName", scdCatName);
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Page<Goods> pageInfo = goodsService.getGoodsPageSelective(page, params);

//		GoodsQueryModel goodsQueryModel = new GoodsQueryModel(combine, brandName, frtCatName, scdCatName, stat, propName, minOdrQtt);
//		if (PropertiesUtil.allPropertiesNull(goodsQueryModel)) {
//			pageInfo = goodsService.getGoodsPage(page);
//		} else {
//			if (StringUtils.isEmpty(goodsQueryModel.getPropName())) {
//				Goods goods = new Goods();
//				ObjectCopier.copyProperties(goodsQueryModel, goods);
//				pageInfo = goodsService.getGoodsPageSelective(goods, page);
//			} else {
//				pageInfo = goodsService.getGoodsPageByQueryModelSelective(goodsQueryModel, page);
//			}
//		}
		return PageHelper.getInstance().pageData(pageInfo);
	}

	@GetMapping("/excel/page/search/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "combine", value = "商品名称/编号/条码", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "frtCatName", value = "商品一级分类名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "scdCatName", value = "商品二级分类名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "brandName", value = "品牌名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "propName", value = "商品单规格值", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "stat", value = "商品状态", paramType = "query", dataType = "boolean"),
			@ApiImplicitParam(name = "minOdrQtt", value = "最小起订量", paramType = "query", dataType = "int")
	})
	@ApiOperation("excel导出")
	public ResponseEntity<byte[]> emportExcelGoodsPage(
			@RequestParam(required = false, name = "stat") Boolean stat,
			@RequestParam(required = false, name = "combine") String combine,
			@RequestParam(required = false, name = "propName") String propName,
			@RequestParam(required = false, name = "brandName") String brandName,
			@RequestParam(required = false, name = "minOdrQtt") Integer minOdrQtt,
			@RequestParam(required = false, name = "frtCatName") String frtCatName,
			@RequestParam(required = false, name = "scdCatName") String scdCatName,
			@PathVariable("current") Integer current, @PathVariable("size") Integer size
	) throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("stat", stat);
		params.put("combine", combine);
		params.put("propName", propName);
		params.put("minOdrQtt", minOdrQtt);
		params.put("brandName", brandName);
		params.put("scdCatName", scdCatName);
		params.put("frtCatName", frtCatName);

		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Page<GoodsExportModel> pageInfo = goodsService.getGoodsExcelPageSelective(page, params);

		Map<String, Object> query = new LinkedHashMap<>();

		if (StringUtils.isNotEmpty(combine)) {
			query.put("商品名称/编号/条码", combine);
		}
		if (StringUtils.isNotEmpty(frtCatName)) {
			query.put("一级分类", frtCatName);
		}
		if (StringUtils.isNotEmpty(scdCatName)) {
			query.put("二级分类", scdCatName);
		}
		if (StringUtils.isNotEmpty(brandName)) {
			query.put("品牌", brandName);
		}
		if (StringUtils.isNotEmpty(propName)) {
			query.put("规格", propName);
		}
		if (ObjectUtils.isNotEmpty(stat)) {
			if (stat) {
				query.put("状态", Constant.ON_SALE);
			} else {
				query.put("状态", Constant.OFF_SALE);
			}
		}
		if (NumberUtil.isIntegerUsable(minOdrQtt)) {
			query.put("最小起订量", minOdrQtt);
		}

		Map<String, String> title = new LinkedHashMap<>();

		title.put("goodsNo", "商品编号");
		title.put("goodsName", "商品名称");
		title.put("brandName", "品牌");
		title.put("frtCatName", "分类");
		title.put("propName", "规格");
		title.put("mainUnit", "单位");
		title.put("price", "销售单价");
		title.put("avbStock", "可用库存");
		title.put("stock", "系统库存");
		title.put("status", "状态");

		List<GoodsExportModel> contents = pageInfo.getRecords();

		HSSFWorkbook wb = ExcelUtil.getHSSFWorkbook("商品信息", false, null, query, title, contents, null);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentDispositionFormData("attachment", new String("商品信息.xls".getBytes(Constant.srcEncoding), Constant.destEncoding));
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		wb.write(baos);
		return new ResponseEntity<byte[]>(baos.toByteArray(), headers, HttpStatus.CREATED);
	}

	@GetMapping("/info/{goodsId}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "goodsId", value = "商品id", paramType = "body", dataType = "int")
	})
	@ApiOperation("通过商品id获取商品信息")
	public Goods getGoodsByBoodsId(@PathVariable("goodsId") Integer goodsId) {
		return goodsService.getGoodsByGoodsId(goodsId);
	}

	@GetMapping("/exists")
	@ApiOperation("查询商品的属性name对应的值value是否存在,暂时包括商品编号:goodsNo，商品条码:barcode，商品名称:goodsName")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "name", value = "商品属性:goodsNo、barcode、goodsName", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "value", value = "商品属性值", paramType = "query", dataType = "string")
	})
	public ExistModel checkGoodsProperExists(@RequestParam(value = "name") String name, @RequestParam("value") String value) {
		String prefix = "";
		/**
		 * 暂时实现功能...
		 */
		if (name != null) {
			if (name.equals("goodsNo")) {
				prefix = "商品编号";
			} else if (name.equals("barcode")) {
				prefix = "商品条码";
			} else if (name.equals("goodsName")) {
				prefix = "商品名称";
			}
		}
		String column = StringUtil.transferCamelToUnderline(name);

		Goods goods = goodsService.getOne(new QueryWrapper<Goods>().eq(column, value));
		String msg = "";
		if (goods != null) {
			if (!goods.getStat()) {
				msg = prefix + "已经存在，但是商品为无效状态，可以重新设置为有效";
			} else {
				msg = prefix + "已经存在,不能创建";
			}
		}
		ExistModel existModel = new ExistModel();
		existModel.setExists(goods != null);
		existModel.setMsg(msg);
		return existModel;
	}


	@DeleteMapping("/clear/{goodsId}")
	@ApiOperation("删除商品所有规格")
	public ResponseModel clearGoodSpec(@PathVariable("goodsId") Integer goodsId) {
		Boolean successful = true;
		if (goodsId != null) {
			successful = goodsSpecService.remove(new QueryWrapper<GoodsSpec>().eq("goods_id", goodsId));
		}
		return ResponseModel.getInstance().succ(successful).msg(successful ? "清除规格成功" : "清除规格失败");
	}


	@GetMapping("/spec/get/{goodsId}")
	@ApiOperation("获取商品的规格值")
	public ResponseModel getGoodSpecs(@PathVariable("goodsId") Integer goodsId) {

		if (null == goodsId || goodsId <= 0) {
			return ResponseModel.getInstance().succ(false).msg("无效的商品信息");
		}
		return ResponseModel.getInstance().succ(true).msg(goodsService.getGoodsPropsByGoodsId(goodsId));

	}

	@GetMapping("/get/names/{current}/{size}")
	@ApiOperation("获取所有商品的名称")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", required = true, paramType = "path", dataType = "int")
	})
	public PageHelper<String> getGoodNames(
			@PathVariable("current") Integer current,
			@PathVariable("size") Integer size
	) {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Page<String> pageInfo = goodsService.getGoodsNamesPage(page);

		return PageHelper.getInstance().pageData(pageInfo);

	}

	@PostMapping(value = "/excel/batch/import")
	public ResponseModel readExcel(MultipartFile excel) throws IOException {
		ExcelReaderBuilder readerBuilder = EasyExcel.read(excel.getInputStream(),new GoodsImportListener(goodsService));
		ExcelReader excelReader = readerBuilder.build();
		ReadSheet readSheet =
				EasyExcel.readSheet(0).headRowNumber(1).build();
		excelReader.read(readSheet);
		excelReader.finish();
		return ResponseModel.getInstance().succ(true).msg("导入成功！");
	}
}