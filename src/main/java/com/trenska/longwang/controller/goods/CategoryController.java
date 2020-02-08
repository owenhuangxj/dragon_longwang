package com.trenska.longwang.controller.goods;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.annotation.CheckDuplicateSubmit;
import com.trenska.longwang.entity.PageHelper;
import com.trenska.longwang.entity.goods.Category;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.goods.ICategoryService;
import com.trenska.longwang.util.NumberUtil;
import com.trenska.longwang.util.PageUtils;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;

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
	@CheckDuplicateSubmit
	@ApiImplicitParams({
			@ApiImplicitParam(name = "catName", value = "分类名", paramType = "body", required = true, dataType = "string"),
			@ApiImplicitParam(name = "pid", value = "父级分类的id，如果为主分类此参数设置为 0", paramType = "body", required = true, dataType = "int"),
	})
	@ApiOperation("添加主分类/子分类，返回对象中data属性是添加成功的分类的id")
	public ResponseModel addCategory(@Valid @RequestBody Category category) {
		if (category == null){
			return ResponseModel.getInstance().succ(false).msg("无效的分类.");
		}
		@NotNull String catName = category.getCatName();
		if (catName == null){
			return ResponseModel.getInstance().succ(false).msg("分类名称不能为空.");
		}
		Category dbCategory = categoryService.getOne(
				new LambdaQueryWrapper<Category>()
					.eq(Category::getCatName,catName)
		);
		if (dbCategory != null){
			return ResponseModel.getInstance().succ(false).msg("分类已经存在.");
		}
		boolean isSuccess = categoryService.save(category);
		return ResponseModel.getInstance().succ(isSuccess).msg(isSuccess ? "分类添加成功" : "分类添加失败");
	}

	@CheckDuplicateSubmit
	@DeleteMapping("/delete/{catId}")
	@ApiImplicitParams({@ApiImplicitParam(name = "catId", required = true, paramType = "path", dataType = "int")})
	@ApiOperation("删除商品分类,参数categoryId必须大于 0 ")
	public ResponseModel deleteCategory(@ApiParam(name = "catId", value = "商品分类id", required = true) @PathVariable("catId") Integer catId) {
		if (NumberUtil.isIntegerUsable(catId)) {
			List<Category> subCategories = categoryService.list(
				new LambdaQueryWrapper<Category>()
					.eq(Category::getPid,catId)
			);
			subCategories.forEach(subCategory->subCategory.deleteById());
			categoryService.removeById(catId);
		} else {
			return ResponseModel.getInstance().succ(false).msg("无效的商品分类id.");
		}
		return ResponseModel.getInstance().succ(true).msg("商品分类删除成功.");
	}

	@CheckDuplicateSubmit
	@DeleteMapping("/delete/batch")
	@ApiOperation("批量删除商品分类,参数categoryIds必须大于 0 ")
	public ResponseModel batchDeleteCategory(@ApiParam(name = "categoryIds", value = "商品分类id的集合/数组", required = true) @RequestParam("categoryIds") Collection<Integer> categoryIds) {
		if (categoryIds.isEmpty()) {
			return ResponseModel.getInstance().succ(false).msg("无效的分类");
		}
		return categoryService.removeCategoryByIds(categoryIds);
	}

	@CheckDuplicateSubmit
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
		Page<Category> pageInfo;
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
}
