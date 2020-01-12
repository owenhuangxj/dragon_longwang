package com.trenska.longwang.controller.goods;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.annotation.CheckDuplicateSubmit;
import com.trenska.longwang.entity.PageHelper;
import com.trenska.longwang.entity.goods.Spec;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.goods.ISpecService;
import com.trenska.longwang.util.NumberUtil;
import com.trenska.longwang.util.PageUtils;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 商品规格管理前端控制器
 * @author Owen
 * @since 2019-04-07
 */
@CrossOrigin
@RestController
@RequestMapping("/spec")
@Api(description = "规格管理")
public class SpecController {

	@Autowired
	private ISpecService specService;

	@PostMapping("/add")
	@CheckDuplicateSubmit
	@ApiImplicitParams({
			@ApiImplicitParam(name = "specName", value = "商品规格名", paramType = "body", required = true, dataType = "string"),
			@ApiImplicitParam(name = "specId", value = "商品规格id,在已经存在的商品规格下添加商品规格属性时，此参数必须回传", paramType = "body", dataType = "int"),
			@ApiImplicitParam(name = "specProperties", value = "商品规格属性集合", paramType = "body", dataType = "set")
	})
	@ApiOperation("添加商品规格,保存商品规格同时保存了商品规格参数时调用此接口,即该接口适用于：1.单独新建商品规格，2.新建商品规格的同时添加商品规格属性，3.已存在的商品规格下添加商品规格属性 ,但是情况 3 时必须传入specId，举例" +
			"{\n" +
			"\"specId\":1,\n" +
			"  \"specProperties\": [\n" +
			"    {\n" +
			"      \"propName\": \"一打\",\n" +
			"      \"specId\": 1,\n" +
			"    }\n" +
			"  ]\n" +
			"}")
	public ResponseModel addSpec(@Valid @RequestBody Spec spec) {
		if(null == spec){
			return ResponseModel.getInstance().succ(false).msg("规格添加成功");
		}
		return specService.saveSpec(spec);
	}

	@CheckDuplicateSubmit
	@DeleteMapping("/delete/{specId}")
	@ApiImplicitParams({@ApiImplicitParam(name = "specId", required = true, paramType = "path", dataType = "int")})
	@ApiOperation("删除规格 ")
	public ResponseModel deleteSpec(@ApiParam(name = "specId", value = "商品规格id", required = true) @PathVariable("specId") Integer specId) {
		if (NumberUtil.isIntegerNotUsable(specId)) {
			return ResponseModel.getInstance().succ(false).msg("无效的规格信息");
		}
		return specService.removeSpecById(specId);
	}

	@CheckDuplicateSubmit
	@DeleteMapping("/delete/batch")
	@ApiOperation("批量删除商品规格,参数specId必须大于 0 ")
	public ResponseModel batchDeleteSpec(@ApiParam(name = "specIds", value = "商品规格id的集合/数组", required = true) @RequestParam("specIds") Collection<Integer> specIds) {
		if (specIds.isEmpty()) {
			return ResponseModel.getInstance().succ(false).msg("无效的规格信息");
		}
		return  specService.removeSpecByIds(specIds);
	}

	@CheckDuplicateSubmit
	@PutMapping("/update")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "specId", paramType = "body", required = true, dataType = "int"),
			@ApiImplicitParam(name = "specName", paramType = "body", required = true, dataType = "string"),
			@ApiImplicitParam(name = "stat", paramType = "body", dataType = "boolean")
	})
	@ApiOperation("修改商品规格")
	public ResponseModel updateSpec(@Valid @RequestBody Spec spec) {

		if(null == spec || NumberUtil.isIntegerNotUsable(spec.getSpecId())) {
			return ResponseModel.getInstance().succ(false).msg("不存在的规格信息");
		}
		return specService.updateSpecById(spec);
	}

	@GetMapping("/list/page/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true, dataType = "int")
	})
	@ApiOperation("规格分页")
	public PageHelper<Spec> listSpecPage(@PathVariable("current") Integer current, @PathVariable("size") Integer size) {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Page<Spec> pageInfo = specService.getSpecPage(page);
		return PageHelper.getInstance().pageData(pageInfo);
	}

	@GetMapping("/list/page/common/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path",dataType = "int"),
			@ApiImplicitParam(name = "specName", value = "商品规格名称", paramType = "query",dataType = "string"),
			@ApiImplicitParam(name = "stat", value = "商品规格状态", paramType = "query",dataType = "boolean")
	})
	@ApiOperation("通用分页")
	public PageHelper<Spec> listSpecPageSelective(
			@PathVariable("current") Integer current,
			@PathVariable("size") Integer size,
			@RequestParam(value = "specName",required = false) String specName,
			@RequestParam(value = "stat",required = false) Boolean stat
	) {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));

		Map<String , Object> params = new HashMap<>();

		params.put("specName",specName);
		params.put("stat",stat);

		Page<Spec> pageInfo = specService.getSpecPageSelective(page,params);

		return PageHelper.getInstance().pageData(pageInfo);
	}

	@GetMapping("/exists/{specName}")
	@ApiOperation("查询规格名称是否存在")
	public ResponseModel checkSpec(@PathVariable("specName") String specName) {
		Spec dbSpec = specService.getOne(
				new LambdaQueryWrapper<Spec>()
						.eq(Spec::getSpecName, specName)
		);
		String msg = "规格不存在,可以创建";
		Map<String, Object> data = new HashMap<>();
		data.put("exists", false);
		if(null == dbSpec) {
			return ResponseModel.getInstance().succ(true).msg(msg).data(data);
		}
		data.put("exists",true);

		if(false == dbSpec.getStat()){
			msg = "规格已经存在，为无效状态，可以重新设置为有效";
		}else{
			msg = "规格已经存在,不需要重新创建";
		}
		return ResponseModel.getInstance().succ(false).msg(msg).data(data);

	}

}