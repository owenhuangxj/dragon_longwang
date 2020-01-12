package com.trenska.longwang.controller.customer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.annotation.CheckDuplicateSubmit;
import com.trenska.longwang.entity.PageHelper;
import com.trenska.longwang.entity.customer.PriceGrp;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.customer.IPriceGrpService;
import com.trenska.longwang.util.PageUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 2019/4/6
 * 创建人:Owen
 */
@CrossOrigin
@RestController
@RequestMapping("/cust-price-grp")
@Api(description = "客户价格分组接口")
public class PriceGrpController {
	@Autowired
	private IPriceGrpService priceGrpService;

	@PostMapping("/add")
	@CheckDuplicateSubmit
	@ApiOperation(value = "添加客户价格分组", notes = "返回的数据中data属性是添加成功的客户价格分组id，即priceGrpId")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "priceGrpName", value = "价格分组名称", paramType = "body", required = true, dataType = "String"),
			@ApiImplicitParam(name = "descr", value = "客户价格分组备注", paramType = "body", dataType = "String")
	})
	public ResponseModel addCustPriceGrp(@RequestBody @Valid PriceGrp priceGrp) {
		if (null == priceGrp) {
			return ResponseModel.getInstance().succ(false).msg("无效的价格分组信息");
		}
		return priceGrpService.savePriceGrp(priceGrp);
	}

	@CheckDuplicateSubmit
	@DeleteMapping("/delete/{PriceGrpId}")
	@ApiOperation(value = "删除客户价格分组")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "PriceGrpId", value = "客户价格分组类型id", paramType = "path", required = true, dataType = "int")
	})
	public ResponseModel deletePriceGrp(@PathVariable Integer PriceGrpId) {
		return ResponseModel.getInstance().succ(priceGrpService.removeById(PriceGrpId)).msg("客户价格分组删除成功");
	}

	@CheckDuplicateSubmit
	@DeleteMapping("/delete/batch")
	@ApiOperation(value = "批量删除客户价格分组")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "priceGrpIds", value = "需要批量删除的客户价格分组类型id集合/数组", paramType = "query", required = true, dataType = "int")
	})
	public ResponseModel batchDeletePriceGrp(@RequestParam(value = "priceGrpIds") Collection<Integer> priceGrpIds) {
		return ResponseModel.getInstance().succ(priceGrpService.removeByIds(priceGrpIds)).msg("批量删除客户价格分组成功");
	}

	@PutMapping("/update")
	@CheckDuplicateSubmit
	@ApiOperation(value = "修改客户价格分组")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "priceGrpId", value = "客户价格分组id", paramType = "body", required = true, dataType = "int"),
			@ApiImplicitParam(name = "priceGrpName", value = "客户价格分组名称", paramType = "body", required = true, dataType = "string"),
			@ApiImplicitParam(name = "descr", value = "客户价格分组备注", paramType = "body", dataType = "string")
	})
	public ResponseModel updatePriceGrp(@RequestBody PriceGrp priceGrp) {
		boolean succ = priceGrpService.updateById(priceGrp);
		return ResponseModel.getInstance().succ(succ).msg("客户价格分组信息更新成功");
	}

	@GetMapping("/list/all")
	@ApiOperation("获取所有客户价格分组信息，可用于下拉框等")
	public List<PriceGrp> listAll() {
		return priceGrpService.list();
	}

	@GetMapping("/list/page/{current}/{size}")
	@ApiOperation("分页获取客户价格分组信息")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true, dataType = "int")
	})
	public PageHelper<PriceGrp> listPriceGrpPage(@PathVariable("current") Integer current, @PathVariable("size") Integer size) {
		PageHelper page = PageHelper.getInstance();
		page.setCurrent(current);
		page.setSize(size);
			Page<PriceGrp> pageInfo = priceGrpService.getPriceGrpPage(PageUtils.getPageParam(page));
		return PageHelper.getInstance().pageData(pageInfo);
	}

	@GetMapping("/list/name/{current}/{size}/{priceGrpName}")
	@ApiOperation("根据价格分组名称获取价格分组信息")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "priceGrpName", value = "价格分组名称", paramType = "path", required = true, dataType = "string")
	})
	public PageHelper<PriceGrp> listPriceGrpByName(@PathVariable("current") Integer current, @PathVariable("size") Integer size, @PathVariable("priceGrpName") String priceGrpName) {
		PageHelper page = PageHelper.getInstance();
		page.setCurrent(current);
		page.setSize(size);
		Page<PriceGrp> pageInfo = priceGrpService.getPriceGrpPageByName(PageUtils.getPageParam(page), priceGrpName);
		return PageHelper.getInstance().pageData(pageInfo);
	}

	@GetMapping("/check/exists/{priceGrpName}")
	@ApiOperation("查询价格分组名称是否重复，存在succ返回true，不存在succ返回false")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "priceGrpName", value = "价格分组名称", paramType = "path", required = true, dataType = "string")
	})
	public ResponseModel checkPriceGrpNameExists(@PathVariable("priceGrpName") String priceGrpName) {
		if (null == priceGrpName) {
			return ResponseModel.getInstance().succ(false).msg("无效的价格分组名称");
		}
		PriceGrp priceGrp = priceGrpService.getOne(
				new LambdaQueryWrapper<PriceGrp>()
						.eq(PriceGrp::getPriceGrpName, priceGrpName)
		);
		boolean exists = null != priceGrp;
		return ResponseModel.getInstance().succ(exists).msg(exists ? "价格分组名称已经存在" : "价格分组名称不存在，可以使用");
	}

}