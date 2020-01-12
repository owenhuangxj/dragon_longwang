package com.trenska.longwang.controller.customer;

import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.annotation.CheckDuplicateSubmit;
import com.trenska.longwang.entity.PageHelper;
import com.trenska.longwang.entity.customer.CustType;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.customer.ICustTypeService;
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
@RequestMapping("/cust-type")
@Api(description = "客户类型接口")
public class CustTypeController{
	@Autowired
	private ICustTypeService custTypeService;
	@PostMapping("/add")
	@CheckDuplicateSubmit
	@ApiOperation(value = "添加客户类型", notes = "返回的数据中data属性是添加成功的客户类型id，即custTypeId")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "custTypeName", value = "客户类型名称", paramType = "body", required = true, dataType = "String"),
			@ApiImplicitParam(name = "descr", value = "客户类型备注", paramType = "body", dataType = "String")
	})
	public ResponseModel addCustType(@RequestBody @Valid CustType custType){
		return ResponseModel.getInstance().succ(custTypeService.save(custType)).msg("客户信息添加成功");
	}

	@CheckDuplicateSubmit
	@DeleteMapping("/delete/{custTypeId}")
	@ApiOperation(value = "删除客户类型")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "custTypeId", value = "客户类型id", paramType = "path", required = true, dataType = "int")
	})
	public ResponseModel deleteCustType(@PathVariable  Integer custTypeId){
		return ResponseModel.getInstance().succ(custTypeService.removeById(custTypeId)).msg("客户类型信息删除成功");
	}

	@CheckDuplicateSubmit
	@DeleteMapping("/delete/batch")
	@ApiOperation(value = "批量删除客户类型信息")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "custTypeIds", value = "需要批量删除的客户类型id集合/数组", paramType = "query", required = true, dataType = "int")
	})
	public ResponseModel batchDeleteCustType(@RequestParam(value = "custTypeIds") Collection<Integer> custTypeIds){
		return ResponseModel.getInstance().succ(custTypeService.removeByIds(custTypeIds)).msg("批量删除客户类型信息成功");
	}

	@CheckDuplicateSubmit
	@PutMapping("/update")
	@ApiOperation(value = "修改客户类型")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "custTypeId", value = "客户类型id", paramType = "body", required = true, dataType = "int"),
			@ApiImplicitParam(name = "custTypeName", value = "客户类型名称", paramType = "body", required = true, dataType = "string"),
			@ApiImplicitParam(name = "descr", value = "客户类型备注", paramType = "body", dataType = "string")
	})
	public ResponseModel updateCustType(@RequestBody CustType custType){
		boolean succ = custTypeService.updateById(custType);
		return ResponseModel.getInstance().succ(succ).msg("客户类型信息更新成功");
	}

	@GetMapping("/list/all")
	@ApiOperation("获取所有客户类型信息，可用于下拉框等")
	public List<CustType> listAll(){
		return custTypeService.list();
	}

	@GetMapping("/list/page/{current}/{size}")
	@ApiOperation("分页获取客户类型信息")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true, dataType = "int")
	})
	public PageHelper<CustType> listCustTypePage(@PathVariable("current")Integer current, @PathVariable("size")Integer size){
		PageHelper page = PageHelper.getInstance();
		page.setCurrent(current);
		page.setSize(size);
		Page<CustType> pageInfo = custTypeService.getCustTypePage(PageUtils.getPageParam(page));
		return PageHelper.getInstance().pageData(pageInfo);
	}

	@GetMapping("/list/page/name/{current}/{size}/{custTypeName}")
	@ApiOperation("根据客户类型名称获取客户类型信息")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "custTypeName", value = "客户类型名称", paramType = "path", required = true, dataType = "string")
	})
	public PageHelper<CustType> listCustTypeByName(@PathVariable("current") Integer current,@PathVariable("size") Integer size,@PathVariable("custTypeName") String custTypeName){
		Page<CustType> pageInfo = custTypeService.getCustTypePageByName(PageUtils.getPageParam(new PageHelper(current,size)),custTypeName);
		return PageHelper.getInstance().pageData(pageInfo);
	}

}