package com.trenska.longwang.controller.goods;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.annotation.DuplicateSubmitToken;
import com.trenska.longwang.entity.PageHelper;
import com.trenska.longwang.entity.goods.Category;
import com.trenska.longwang.model.sys.ExistModel;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.goods.ICategoryService;
import com.trenska.longwang.util.NumberUtil;
import com.trenska.longwang.util.PageUtils;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;

/**
 * 商品分类表 前端控制器
 *
 * @author Owen
 * @since 2019-04-07
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/category")
@Api(description = "商品-分类管理接口")
public class CategoryController {


	@Autowired
	private ICategoryService categoryService;

	@PostMapping("/add")
	@DuplicateSubmitToken
	@ApiImplicitParams({
			@ApiImplicitParam(name = "catName", value = "分类名", paramType = "body", required = true, dataType = "string"),
			@ApiImplicitParam(name = "pid", value = "父级分类的id，如果为主分类此参数设置为 0", paramType = "body", required = true, dataType = "int"),
	})
	@ApiOperation("添加主分类/子分类，返回对象中data属性是添加成功的分类的id")
	public ResponseModel addCategory(@Valid @RequestBody Category category) {
		boolean successful = categoryService.save(category);
		return ResponseModel.getInstance().succ(successful).msg(successful ? "分类添加成功" : "分类添加失败");
	}

	@DuplicateSubmitToken
	@DeleteMapping("/delete/{catId}")
	@ApiImplicitParams({@ApiImplicitParam(name = "catId", required = true, paramType = "path", dataType = "int")})
	@ApiOperation("删除商品分类,参数categoryId必须大于 0 ")
	public ResponseModel deleteCategory(@ApiParam(name = "catId", value = "商品分类id", required = true) @PathVariable("catId") Integer catId) {
		if (BooleanUtils.toBoolean(catId)) {
			Boolean removed = categoryService.removeCategoryById(catId);
			return ResponseModel.getInstance().succ(removed).msg(removed ? "商品分类删除成功" : "商品分类删除失败");
		} else {
			return ResponseModel.getInstance().succ(false).msg("商品分类id不能小于 1");
		}
	}

	@DuplicateSubmitToken
	@DeleteMapping("/delete/batch")
	@ApiOperation("批量删除商品分类,参数categoryIds必须大于 0 ")
	public ResponseModel batchDeleteCategory(@ApiParam(name = "categoryIds", value = "商品分类id的集合/数组", required = true) @RequestParam("categoryIds") Collection<Integer> categoryIds) {
		if (categoryIds.isEmpty()) {
			return ResponseModel.getInstance().succ(false).msg("无效的分类");
		}
		return categoryService.removeCategoryByIds(categoryIds);
	}

	@DuplicateSubmitToken
	@PutMapping("/update")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "catId", paramType = "body", required = true, dataType = "int"),
			@ApiImplicitParam(name = "catName", paramType = "body", required = true, dataType = "string"),
			@ApiImplicitParam(name = "stat", paramType = "body", dataType = "boolean")
	})
	@ApiOperation("修改商品分类")
	public ResponseModel updateCategory(@Valid @RequestBody Category category) {
		if(null == category){
			return ResponseModel.getInstance().succ(false).msg("商品分类信息不能为空");
		}
		if(!NumberUtil.isIntegerUsable(category.getCatId())){
			return ResponseModel.getInstance().succ(false).msg("商品分类信息id不能为空");
		}
		return categoryService.updateCategory(category);
	}

	@GetMapping("/list/page/sub/{current}/{size}/{catId}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true,dataType = "int"),
			@ApiImplicitParam(name = "catId", value = "上级分类id", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "stat", value = "分类状态", paramType = "query", required = true, dataType = "boolean")
	})
	@ApiOperation("根据上级分类id获取获取下级商品分类，如果要获取一级商品分类，catId属性设置为 0，<b>进入商品分类时调用此接口，即设置catId为 0 可对一级商品分类进行分页</b>")
	public PageHelper<Category> listSubCategory(
			@PathVariable("current") Integer current,
			@PathVariable("size") Integer size,
			@PathVariable("catId") Integer catId,
			@RequestParam(value = "stat",required = false) Boolean stat
	) {
		Page<Category> pageInfo = categoryService.getSubCategoryPageByCatId(PageUtils.getPageParam(new PageHelper(current,size)),catId,stat);
		return PageHelper.getInstance().pageData(pageInfo);
	}

	@GetMapping("/list/page/common/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path",dataType = "int"),
			@ApiImplicitParam(name = "catName", value = "商品分类名称", paramType = "query",dataType = "string"),
			@ApiImplicitParam(name = "stat", value = "商品分类状态", paramType = "query",dataType = "boolean")
	})
	@ApiOperation("通用分页")
	public PageHelper<Category> listCategoryPageSelective(@PathVariable("current") Integer current, @PathVariable("size") Integer size, @RequestParam(value = "catName",required = false) String catName, @RequestParam(value = "stat",required = false) Boolean stat) {

		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Page<Category> pageInfo = null;
		if (ObjectUtils.anyNotNull(catName,stat)) {
			Category category = new Category();
			category.setCatName(catName);
			category.setStat(stat);
			pageInfo = categoryService.getCategoryPageSelective(page, category);
		} else {
			pageInfo = categoryService.getCategoryPage(page);
		}
		return PageHelper.getInstance().pageData(pageInfo);
	}

	@ApiOperation("查询分类是否存在")
	@GetMapping("/exists/{catName}")
	public ExistModel checkCategory(@PathVariable("catName") String catName) {
		Category category = categoryService.getOne(new QueryWrapper<Category>().eq("cat_name", catName));
		String msg = "";
		if(category != null) {
			if(!category.getStat()){
				msg = "分类已经存在，为无效状态，可以重新设置为有效";
			}else{
				msg = "分类已经存在,不需要重新创建";
			}
		}
		ExistModel existModel = new ExistModel();
		existModel.setExists(category != null);
		existModel.setMsg(msg);
		return existModel;
	}
	
//	@GetMapping("/list/page/name/{current}/{size}/{catName}")
//	@ApiImplicitParams({
//			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
//			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true,dataType = "int"),
//			@ApiImplicitParam(name = "catName", value = "分类名称", paramType = "path", required = true, dataType = "string")
//	})
//	@ApiOperation("根据分类名称查询商品分类")
//	public PageHelper<Category> listCategoryByName(
//			@PathVariable("current") Integer current,
//			@PathVariable("size") Integer size,
//			@PathVariable("catName") String catName) {
//		Page<Category> pageData = categoryService.getCategoryPageByName(PageUtils.getPageParam(new PageHelper(current,size)),catName);
//		return PageHelper.getInstance().pageData(pageData);
//	}
//
//	@GetMapping("/list/page/stat/{current}/{size}/{stat}")
//	@ApiImplicitParams({
//			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
//			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true,dataType = "int"),
//			@ApiImplicitParam(name = "stat", value = "分类状态", paramType = "path", required = true, dataType = "boolean")
//	})
//	@ApiOperation("根据分类状态查询商品分类")
//	public PageHelper<Category> listCategoryByStat(
//			@PathVariable("current") Integer current,
//			@PathVariable("size") Integer size,
//			@PathVariable("stat") Boolean stat) {
//		Page<Category> pageData = categoryService.getSubCategoryPageByStat(PageUtils.getPageParam(new PageHelper(current,size)),stat);
//		return PageHelper.getInstance().pageData(pageData);
//	}
}

