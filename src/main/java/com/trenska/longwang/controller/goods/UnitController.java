package com.trenska.longwang.controller.goods;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.annotation.CheckDuplicateSubmit;
import com.trenska.longwang.entity.PageHelper;
import com.trenska.longwang.entity.goods.Unit;
import com.trenska.longwang.model.sys.CommonResponse;
import com.trenska.longwang.service.goods.IUnitService;
import com.trenska.longwang.util.PageUtils;
import io.swagger.annotations.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 商品单位管理前端控制器
 * @author Owen
 * @since 2019-04-07
 */
@CrossOrigin
@RestController
@RequestMapping("/unit")
@Api(description = "商品-单位管理接口")
public class UnitController {
	@Autowired
	private IUnitService unitService;

	@PostMapping("/add")
	@CheckDuplicateSubmit
	@ApiImplicitParams({
			@ApiImplicitParam(name = "unitName", value = "单位名", paramType = "body", required = true, dataType = "string")
	})
	@ApiOperation("添加单位")
	public CommonResponse addUnit(@Valid @RequestBody Unit unit) {
		if(null == unit){
			return CommonResponse.getInstance().succ(false).msg("无效的单位信息");
		}
		@NotNull String unitName = unit.getUnitName();

		Unit dbUnit = unitService.getUnit(unitName);

		if(null != dbUnit && true == dbUnit.getDeleted()){
			dbUnit.setDeleted(false);
			unitService.updateUnit(dbUnit);
		}else {
			unitService.save(unit);
		}

		return CommonResponse.getInstance().succ(true).msg("单位添加成功");
	}

	@CheckDuplicateSubmit
	@DeleteMapping("/delete/{unitId}")
	@ApiOperation("删除商品单位")
	public CommonResponse deleteUnit(@ApiParam(name = "unitId", required = true) @PathVariable("unitId") Integer unitId) {
		unitService.removeById(unitId);
		return CommonResponse.getInstance().succ(true).msg("单位删除成功");
	}

	@CheckDuplicateSubmit
	@DeleteMapping("/delete/batch")
	@ApiOperation("批量删除商品单位")
	public CommonResponse batchDeleteUnit(@ApiParam(name = "unitIds", value = "需要批量删除的商品单位id集合/数组", required = true) @RequestParam(value = "unitIds") Collection<Integer> unitIds) {
		return unitService.removeUnitByIds(unitIds);
	}

	@CheckDuplicateSubmit
	@PutMapping("/update")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "stat", paramType = "body", dataType = "boolean"),
			@ApiImplicitParam(name = "unitName", paramType = "body", dataType = "string"),
			@ApiImplicitParam(name = "unitId", paramType = "body", required = true, dataType = "int")
	})
	@ApiOperation("根据单位id即unitId修改商品单位,该接口可以同时修改三个属性，也可两两组合或者只修改一个属性")
	public CommonResponse updateUnit(@Valid @RequestBody Unit unit) {
		unitService.updateById(unit);
		return CommonResponse.getInstance().succ(true).msg("修改单位成功" );
	}

	@GetMapping("/list/page/common/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "stat", value = "商品单位状态", paramType = "query", dataType = "boolean"),
			@ApiImplicitParam(name = "unitName", value = "商品单位名称", paramType = "query", dataType = "string"),
			@ApiImplicitParam(name = "current", value = "当前页",required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数",required = true, paramType = "path", dataType = "int")
	})
	@ApiOperation("通用分页")
	public PageHelper<Unit> listUnitPageSelective(
			@PathVariable("size") Integer size,
			@PathVariable("current") Integer current,
			@RequestParam(value = "stat",required = false) Boolean stat,
			@RequestParam(value = "unitName",required = false) String unitName
	) {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Page<Unit> pageInfo;
		if (ObjectUtils.anyNotNull(unitName,stat)) {
			Unit unit = new Unit();
			unit.setUnitName(unitName);
			unit.setStat(stat);
			pageInfo = unitService.getUnitPageSelective(page, unit);
		} else {
			pageInfo = unitService.getUnitPage(page);
		}
		return PageHelper.getInstance().pageData(pageInfo);
	}

	@GetMapping("/list/page/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true, dataType = "int")
	})
	@ApiOperation("分页商品单位")
	public PageHelper<Unit> listUnitPage(@PathVariable("current") Integer current, @PathVariable("size") Integer size) {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Page<Unit> pageInfo = unitService.getUnitPage(page);
		return PageHelper.getInstance().pageData(pageInfo);
	}

	@GetMapping("/exists/{unitName}")
	@ApiOperation("查询单位是否存在")
	public CommonResponse checkSpec(@PathVariable("unitName") String unitName) {
		Unit dbUnit = unitService.getOne(
				new LambdaQueryWrapper<Unit>()
						.eq(Unit::getUnitName, unitName)
		);
		String msg = "";
		if(dbUnit != null) {
			if(!dbUnit.getStat()){
				msg = "单位已经存在，为无效状态，可以重新设置为有效";
			}else{
				msg = "单位已经存在,不需要重新创建";
			}
			return CommonResponse.getInstance().succ(false).msg(msg);
		}else{

		}
		Map<String,Boolean> data = new HashMap<>();
		data.put("exists",true);
		return CommonResponse.getInstance().succ(true).data(data);
	}

//	@GetMapping("/list/page/search/{current}/{size}")
//	@ApiImplicitParams({
//			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
//			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true, dataType = "int"),
//			@ApiImplicitParam(name = "unitName", value = "商品单位名称", paramType = "body", dataType = "string"),
//			@ApiImplicitParam(name = "stat", value = "商品单位状态", paramType = "body", dataType = "boolean")
//	})
//	@ApiOperation("多条件查询分页")
//	public PageHelper<Unit> listUnitPageSelective2(@PathVariable("current") Integer current, @PathVariable("size") Integer size, @RequestBody Unit unit) {
//		logger.debug("unit : {}", unit);
//		Page<Unit> pageData = PropertiesUtil.allPropertiesNull(unit) ?
//				unitService.getUnitPage(PageUtils.getPageParam(new PageHelper(current, size))) : unitService.getUnitPageSelective(PageUtils.getPageParam(new PageHelper(current, size)), unit);
//		return PageHelper.getInstance().pageData(pageData);
//	}
//
//	@GetMapping("/list/page/name/{current}/{size}/{unitName}")
//	@ApiImplicitParams({
//			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
//			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true, dataType = "int"),
//			@ApiImplicitParam(name = "unitName", value = "商品单位名称", paramType = "path", dataType = "string")
//	})
//	@ApiOperation("根据名称查询商品单位并分页")
//	public PageHelper<Unit> listUnitPageByName(@PathVariable("current") Integer current, @PathVariable("size") Integer size, @PathVariable("unitName") String unitName) {
//		logger.debug("unitName : {}", unitName);
//		Page<Unit> pageData = unitService.getUnitPageByName(PageUtils.getPageParam(new PageHelper(current, size)), unitName);
//		return PageHelper.getInstance().pageData(pageData);
//	}
//
//	@GetMapping("/list/page/stat/{current}/{size}/{stat}")
//	@ApiImplicitParams({
//			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
//			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true, dataType = "int"),
//			@ApiImplicitParam(name = "stat", value = "商品单位状态", paramType = "path", dataType = "boolean")
//	})
//	@ApiOperation("根据状态查询商品单位并分页")
//	public PageHelper<Unit> listUnitPageByStat(@PathVariable("current") Integer current, @PathVariable("size") Integer size, @PathVariable("stat") Boolean stat) {
//		Page<Unit> pageData = unitService.getUnitPageByStat(PageUtils.getPageParam(new PageHelper(current, size)), stat);
//		return PageHelper.getInstance().pageData(pageData);
//	}
}