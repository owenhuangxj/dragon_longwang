package com.trenska.longwang.controller.goods;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.annotation.CheckDuplicateSubmit;
import com.trenska.longwang.entity.PageHelper;
import com.trenska.longwang.entity.goods.Brand;
import com.trenska.longwang.model.sys.ExistModel;
import com.trenska.longwang.model.sys.CommonResponse;
import com.trenska.longwang.service.goods.IBrandService;
import com.trenska.longwang.util.PageUtils;
import io.swagger.annotations.*;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;

/**
 *  品牌前端控制器
 * @author Owen
 * @since 2019-04-07
 */
@CrossOrigin
@RestController
@RequestMapping("/brand")
@Api(description = "商品-品牌管理接口")
public class BrandController {

	private Logger logger = LoggerFactory.getLogger(BrandController.class);

	@Autowired
	private IBrandService brandService;

	@CheckDuplicateSubmit
	@PostMapping("/add")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "brandName", value = "商品品牌名称", paramType = "body", required = true, dataType = "string")
	})
	@ApiOperation("添加品牌，返回对象中data属性是添加成功的品牌的id")
//	@RequiresPermissions({"10203001"})
	public CommonResponse addBrand(@Valid @RequestBody Brand brand) {
		boolean successful = brandService.save(brand);
		return CommonResponse.getInstance().succ(successful).msg(successful ? "品牌添加成功" : "品牌添加失败");
	}

	@CheckDuplicateSubmit
	@DeleteMapping("/delete/{brandId}")
	@ApiOperation("删除商品品牌")
	public CommonResponse deleteBrand(@ApiParam(name = "brandId", required = true) @PathVariable("brandId") Integer brandId) {
		logger.debug("brandId : {} ", brandId);
		Boolean removed = brandService.removeById(brandId);
		return CommonResponse.getInstance().succ(removed).msg(removed ? "商品品牌删除成功" : "商品品牌删除失败");

	}

	@CheckDuplicateSubmit
	@DeleteMapping("/delete/batch")
	@ApiOperation("批量删除商品品牌")
	public CommonResponse batchDeleteBrand(
			@ApiParam(name = "brandIds", value = "需要批量删除的商品品牌id集合/数组", required = true) @RequestParam(value = "brandIds") Collection<Integer> brandIds) {
		if(brandIds.isEmpty()){
			return CommonResponse.getInstance().succ(false).msg("无效的品牌信息");
		}
		return brandService.removeBrandByIds(brandIds);

	}

	@CheckDuplicateSubmit
	@PutMapping("/update")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "brandId", paramType = "body", required = true, dataType = "int"),
			@ApiImplicitParam(name = "brandName", paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "stat", paramType = "body", dataType = "boolean")
	})
	@ApiOperation("根据品牌id即brandId修改商品品牌,该接口可以同时修改三个属性，也可两两组合或者只修改一个属性")
	public CommonResponse updateBrand(@Valid @RequestBody Brand brand) {
		return brandService.updateBrand(brand);
	}

	@GetMapping("/list/page/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true, dataType = "int")
	})
	@ApiOperation("分页商品品牌")
	public PageHelper<Brand> listBrandPage(@PathVariable("current") Integer current, @PathVariable("size") Integer size) {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Page<Brand> pageInfo = brandService.getBrandPage(page);
		return PageHelper.getInstance().pageData(pageInfo);
	}

	@GetMapping("/list/page/common/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页",required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数",required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "brandName", value = "商品品牌名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "stat", value = "商品品牌状态", paramType = "query", dataType = "boolean")
	})
	@ApiOperation("通用分页")
	public PageHelper<Brand> listBrandPageSelective(
			@PathVariable("current") Integer current, @PathVariable("size") Integer size,
			@RequestParam(value = "brandName",required = false) String brandName,
			@RequestParam(value = "stat",required = false) Boolean stat)
	{
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Page<Brand> pageInfo = null;
		if (ObjectUtils.anyNotNull(brandName,stat)) {
			Brand brand = new Brand();
			brand.setBrandName(brandName);
			brand.setStat(stat);
			pageInfo = brandService.getBrandPageSelective(page, brand);
		} else {
			pageInfo = brandService.getBrandPage(page);
		}
		return PageHelper.getInstance().pageData(pageInfo);
	}

	@GetMapping("/exists/{brandName}")
	@ApiOperation("查询品牌是否存在")
	public ExistModel checkBrand(@PathVariable("brandName") String brandName) {
		Brand brand = brandService.getOne(
				new LambdaQueryWrapper<Brand>()
						.eq(Brand::getBrandName, brandName)
		);
		String msg = "";
		if(brand != null) {
			if(!brand.getStat()){
				msg = "品牌已经存在，为无效状态，可以重新设置为有效";
			}else{
				msg = "品牌已经存在,不需要重新创建";
			}
		}
		ExistModel existModel = new ExistModel();
		existModel.setExists(brand != null);
		existModel.setMsg(msg);
		return existModel;
	}
	@GetMapping("/add")
	public boolean addBrand2(@RequestParam Brand brand) {
		System.out.println(brand);
		return true;
	}

//	@GetMapping("/list/page/search/{current}/{size}")
//	@ApiImplicitParams({
//			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", dataType = "int"),
//			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path",dataType = "int"),
//			@ApiImplicitParam(name = "brandName", value = "商品品牌名称", paramType = "body",dataType = "string"),
//			@ApiImplicitParam(name = "stat", value = "商品品牌状态", paramType = "body",dataType = "boolean")
//	})
//	@ApiOperation("多条件查询分页")
//	public PageHelper<Brand> listBrandPageSelective(@PathVariable("current") Integer current,@PathVariable("size") Integer size,@RequestBody Brand brand) {
//		logger.debug("brand : {}",brand);
//		Page<Brand> pageData = brandService.getBrandPageSelective(PageUtils.getPageParam(new PageHelper(current,size)),brand);
//		return PageHelper.getInstance().pageData(pageData);
//	}
//
//	@GetMapping("/list/page/name/{current}/{size}/{brandName}")
//	@ApiImplicitParams({
//			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", dataType = "int"),
//			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path",dataType = "int"),
//			@ApiImplicitParam(name = "brandName", value = "商品品牌名称", paramType = "path",dataType = "string")
//	})
//	@ApiOperation("根据名称查询商品品牌并分页")
//	public PageHelper<Brand> listBrandPageByName(@PathVariable("current") Integer current,@PathVariable("size") Integer size,@PathVariable("brandName") String brandName) {
//		logger.debug("brandName : {}",brandName);
//		Page<Brand> pageData = brandService.getBrandPageByName(PageUtils.getPageParam(new PageHelper(current,size)),brandName);
//		return PageHelper.getInstance().pageData(pageData);
//	}
//	@GetMapping("/list/page/stat/{current}/{size}/{stat}")
//	@ApiImplicitParams({
//			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", dataType = "int"),
//			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path",dataType = "int"),
//			@ApiImplicitParam(name = "stat", value = "商品品牌状态", paramType = "path",dataType = "boolean")
//	})
//	@ApiOperation("根据状态查询商品品牌并分页")
//	public PageHelper<Brand> listBrandPageByStat(@PathVariable("current") Integer current, @PathVariable("size") Integer size, @PathVariable("stat") Boolean stat) {
//		Page<Brand> pageData = brandService.getBrandPageByStat(PageUtils.getPageParam(new PageHelper(current,size)),stat);
//		return PageHelper.getInstance().pageData(pageData);
//	}
}