package com.trenska.longwang.controller.goods;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.annotation.CheckDuplicateSubmit;
import com.trenska.longwang.entity.PageHelper;
import com.trenska.longwang.entity.goods.SpecProperty;
import com.trenska.longwang.model.sys.ExistModel;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.goods.ISpecPropertyService;
import com.trenska.longwang.util.PageUtils;
import io.swagger.annotations.*;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;

/**
 * 商品规格属性管理前端控制器
 * @author Owen
 * @since 2019-04-09
 */
@RestController
@CrossOrigin
@RequestMapping("/spec-property")
@Api(description = "规格值接口")
public class SpecPropertyController {
	
	@Autowired
	private ISpecPropertyService specPropertyService;

	@PostMapping("/add")
	@CheckDuplicateSubmit
	@ApiImplicitParams({
			@ApiImplicitParam(name = "specId", value = "商品规格id", required = true, paramType = "body", dataType = "int"),
			@ApiImplicitParam(name = "propName", value = "商品规格属性名", paramType = "body", required = true, dataType = "string")
	})
	@ApiOperation("添加单个商品规格属性")
	public ResponseModel addSpecProperty(@Valid @RequestBody SpecProperty specProperty) {
		Boolean successful = specPropertyService.save(specProperty);
		return ResponseModel.getInstance().succ(successful).msg(successful ? "添加商品规格属性成功" : "添加商品规格属性失败");
	}

	@CheckDuplicateSubmit
	@DeleteMapping("/delete/{specPropId}")
	@ApiImplicitParams({@ApiImplicitParam(name = "specPropId", required = true, paramType = "path", dataType = "int")})
	@ApiOperation("删除商品规格属性 ")
	public ResponseModel deleteSpecProperty(@ApiParam(name = "specPropId", value = "商品规格属性id", required = true) @PathVariable("specPropId") Integer specPropId) {
		if (BooleanUtils.toBoolean(specPropId)) {
			Boolean successful = specPropertyService.removeById(specPropId);
			return ResponseModel.getInstance().succ(successful).msg(successful ? "商品规格属性删除成功" : "商品规格属性删除失败");
		} else {
			return ResponseModel.getInstance().succ(false).msg("商品规格属性id不能小于 1");
		}
	}

	@CheckDuplicateSubmit
	@DeleteMapping("/delete/batch")
	@ApiOperation("批量删除商品规格属性 ")
	public ResponseModel batchDeleteSpecProperty(@ApiParam(name = "specPropIds", value = "商品规格属性id的集合/数组", required = true) @RequestParam("specPropIds") Collection<Integer> specPropIds) {
		if (specPropIds.isEmpty()) {
			return ResponseModel.getInstance().succ(false).msg("无效的商品规格属性");
		}
		return specPropertyService.removeSpecPropertyByIds(specPropIds);
	}

	@CheckDuplicateSubmit
	@PutMapping("/update")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "stat", paramType = "body", required = true,dataType = "boolean"),
			@ApiImplicitParam(name = "specPropId", paramType = "body", required = true, dataType = "int"),
			@ApiImplicitParam(name = "propName", paramType = "body", required = true, dataType = "string")
	})
	@ApiOperation("修改商品规格属性,前端需要控制商品规格属性名称或状态有改变才允许请求")
	public ResponseModel updateSpecProperty(@Valid @RequestBody SpecProperty specProperty) {
		if (specProperty == null) {
			return ResponseModel.getInstance().succ(false).msg( "不存在该规格值");
		}

		boolean successful = specPropertyService.updateSpecPropertyById(specProperty);
		return ResponseModel.getInstance().succ(successful).msg(successful ? "修改商品规格属性成功" : "修改商品规格属性失败");
	}

	@GetMapping("/list/page/common/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "stat", value = "商品规格属性状态", paramType = "query", dataType = "boolean"),
			@ApiImplicitParam(name = "PropName", value = "商品规格属性名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "current", value = "当前页",required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数",required = true, paramType = "path", dataType = "int")
	})
	@ApiOperation("商品规格值通用分页")
	public PageHelper<SpecProperty> listSpecPropertyPageSelective(@PathVariable("current") Integer current, @PathVariable("size") Integer size, @RequestParam(value = "PropName",required = false) String PropName, @RequestParam(value = "stat",required = false) Boolean stat) {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Page<SpecProperty> pageInfo;
		if (ObjectUtils.anyNotNull(PropName,stat)) {
			SpecProperty specProperty = new SpecProperty();
			specProperty.setPropName(PropName);
			specProperty.setStat(stat);
			pageInfo = specPropertyService.getSpecPropertiesPageSelective(page, specProperty);
		} else {
			pageInfo = specPropertyService.getSpecPropertiesPage(page);
		}
		return PageHelper.getInstance().pageData(pageInfo);
	}

	@GetMapping("/list/page/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true, dataType = "int")
	})
	@ApiOperation("分页商品规格值")
	public PageHelper<SpecProperty> listSpecPropertiesPage(@PathVariable("current") Integer current, @PathVariable("size") Integer size) {
		Page<SpecProperty> pageInfo = specPropertyService.getSpecPropertiesPage(PageUtils.getPageParam( new PageHelper(current,size)));
		return PageHelper.getInstance().pageData(pageInfo);
	}
	@GetMapping("/list/sub/page/{current}/{size}/{specId}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "stat", value = "属性状态", paramType = "query", required = true, dataType = "boolean"),
			@ApiImplicitParam(name = "specId", value = "商品规格的id", paramType = "path", required = true, dataType = "int")

	})
	@ApiOperation("通过商品规格的 specId 分页商品规格属性,点击商品规格详情时调用该接口")
	public PageHelper<SpecProperty> listSpecPropertiesBySpecId(@PathVariable("current") Integer current, @PathVariable("size") Integer size, @PathVariable("specId") Integer specId, @RequestParam(value = "stat",required = false) Boolean stat) {
		Page<SpecProperty> pageInfo = specPropertyService.getSpecPropertiesPageBySpecId(PageUtils.getPageParam( new PageHelper(current,size)),specId , stat);
		return PageHelper.getInstance().pageData(pageInfo);
	}

	@GetMapping("/exists/{specId}/{propName}")
	@ApiOperation("查询规格的属性是否存在")
	public ExistModel checkSpec(@PathVariable("specId") Integer specId, @PathVariable("propName") String propName) {
		SpecProperty specProperty = specPropertyService.getOne(new QueryWrapper<SpecProperty>().eq("prop_name", propName).eq("spec_id",specId));
		String msg = "";
		if(specProperty != null) {
			if(!specProperty.getStat()){
				msg = "规格值已经存在，为无效状态，可以重新设置为有效";
			}else{
				msg = "规格值已经存在,不需要重新创建";
			}
		}
		ExistModel existModel = new ExistModel();
		existModel.setExists(specProperty != null);
		existModel.setMsg(msg);
		return existModel;
	}
//	@GetMapping("/list/page/name/{current}/{size}/{propName}")
//	@ApiImplicitParams({
//			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
//			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true,dataType = "int"),
//			@ApiImplicitParam(name = "propName", value = "商品规格属性名称", paramType = "path", required = true, dataType = "string")
//	})
//	@ApiOperation("根据商品规格属性名称查询商品规格属性")
//	public PageHelper<SpecProperty> listSpecPropertiesPageByName(
//			@PathVariable("current") Integer current,
//			@PathVariable("size") Integer size,
//			@PathVariable("propName") String propName) {
//		Page<SpecProperty> pageData = specPropertyService.getSpecPropertiesPageByName(PageUtils.getPageParam(new PageHelper(current,size)),propName);
//		return PageHelper.getInstance().pageData(pageData);
//	}
//
//	@GetMapping("/list/page/stat/{current}/{size}/{stat}")
//	@ApiImplicitParams({
//			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
//			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true,dataType = "int"),
//			@ApiImplicitParam(name = "stat", value = "商品规格属性状态", paramType = "path", required = true, dataType = "boolean")
//	})
//	@ApiOperation("根据商品规格属性状态查询商品规格属性")
//	public PageHelper<SpecProperty> listSpecPropertiesPageByStat(
//			@PathVariable("current") Integer current,
//			@PathVariable("size") Integer size,
//			@PathVariable("stat") Boolean stat) {
//		Page<SpecProperty> pageData = specPropertyService.getSpecPropertiesPageByStat(PageUtils.getPageParam(new PageHelper(current,size)),stat);
//		return PageHelper.getInstance().pageData(pageData);
//	}
}