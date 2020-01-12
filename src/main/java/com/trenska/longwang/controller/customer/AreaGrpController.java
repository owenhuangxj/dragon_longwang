package com.trenska.longwang.controller.customer;

import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.annotation.CheckDuplicateSubmit;
import com.trenska.longwang.entity.PageHelper;
import com.trenska.longwang.entity.customer.AreaGrp;
import com.trenska.longwang.model.customer.AreaGrpModel;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.customer.IAreaGrpService;
import com.trenska.longwang.util.PageUtils;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Set;

/**
 * 2019/4/3
 * 创建人:Owen
 */
@CrossOrigin
@RestController
@RequestMapping("/area-grp")
@Api(description = "客户区域分组接口")
public class AreaGrpController {

	@Autowired
	private IAreaGrpService areaGrpService;

	@PostMapping("/add")
	@CheckDuplicateSubmit
	@ApiImplicitParams({
			@ApiImplicitParam(name = "pid", value = "父节点areaGrpId,如果是一级区域时设置areaGrpId为 0", paramType = "body", defaultValue = "0", required = true, dataType = "int"),
			@ApiImplicitParam(name = "areaGrpName", value = "区域分组名", paramType = "body", required = true, dataType = "string"),
			@ApiImplicitParam(name = "areaGrpDeep", value = "区域节点深度 1:一级区域;2:二级区域;3:三级区域", required = true, paramType = "body", dataType = "int")
	})
	@ApiOperation("添加区域")
	public ResponseModel addSubCustArea(@ApiParam(name = "custAreaGrp", value = "提交参数", required = true) @Valid @RequestBody AreaGrp areaGrp) {

		if(null == areaGrp){
			return ResponseModel.getInstance().succ(false).msg("区域分组信息不能为空");
		}
		return areaGrpService.addSubAreaGrp(areaGrp);
	}

	/**
	 * 删除区域分组信息需要删除t_emp_area_grp表里面的关系记录
	 * @param areaGrpId
	 * @param areaGrpDeep
	 * @return
	 */
	@CheckDuplicateSubmit
	@DeleteMapping("/delete/{areaGrpDeep}/{areaGrpId}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "custId", required = true, paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "areaGrpDeep", value = "节点深度 1:一级区域;2:二级区域;3:三级区域", required = true, paramType = "path", dataType = "int")
	})
	@ApiOperation("删除区域分组")
	public ResponseModel deleteAreaGrp(
			@ApiParam(name = "custId", value = "区域分组id", required = true) @PathVariable("areaGrpId") Integer areaGrpId,
			@ApiParam(name = "areaGrpDeep", value = "区域分组深度", required = true) @PathVariable("areaGrpDeep") Integer areaGrpDeep) {
		return areaGrpService.removeAreaGrp(areaGrpId, areaGrpDeep);
	}
	@CheckDuplicateSubmit
	@PutMapping("/update")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "custId", paramType = "body", required = true, dataType = "int"),
			@ApiImplicitParam(name = "areaGrpName", value = "节点名", paramType = "body", required = true, dataType = "string")
	})
	@ApiOperation("修改区域分组")
	public ResponseModel updateAreaGrp(@ApiParam(name = "custAreaGrp", value = "区域实体", required = true) @Valid @RequestBody AreaGrp areaGrp) {
		return ResponseModel.getInstance().succ(areaGrpService.updateAreaGrp(areaGrp)).msg("修改成功");
	}

	@GetMapping("/list/{areaGrpId}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "custId", value = "上级区域id", paramType = "path", required = true, dataType = "int")
	})
	@ApiOperation("根据上级区域id(custId)获取获取下级区域信息，如果是一级区域，属性 areaGrpId设置为 0")
	public List<AreaGrp> listSub(@PathVariable("areaGrpId") Integer areaGrpId) {
		return areaGrpService.getSubAreaGrp(areaGrpId);
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@GetMapping("/list/first/{current}/{size}")
	@ApiOperation("获取一级区域信息,添加区域后调用此接口")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true, dataType = "int")
	})
	public PageHelper<AreaGrp> listFirstLevel( @PathVariable("current") Integer current, @PathVariable("size") Integer size ) {
		Page page = PageUtils.getPageParam(new PageHelper(current, size));
		Page<AreaGrp> pageInfo = areaGrpService.getFirstLevelAreaGrp(page);
		return PageHelper.getInstance().pageData(pageInfo);
	}

	@GetMapping("/list/page/name/{current}/{size}/{areaGrpName}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "areaGrpName", value = "区域分组名称", paramType = "path", required = true, dataType = "string"),
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true, dataType = "int")
	})
	@ApiOperation("根据区域分组名称查询并分页，没有级联查询子区域分组，使用 列表按钮触发事件 + (GET /list/{custId}) 接口 进行获取")
	public PageHelper<AreaGrp> listAreaGrpByName(@PathVariable(value = "areaGrpName") String areaGrpName, @PathVariable("current") Integer current, @PathVariable("size") Integer size) {
		Page<AreaGrp> pageInfo = areaGrpService.getAreaGrpPageByName(PageUtils.getPageParam(new PageHelper(current, size)), areaGrpName);
		return PageHelper.getInstance().pageData(pageInfo);
	}

	@GetMapping("/list/page/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", required = true, dataType = "int")
	})
	@ApiOperation("分页区域分组")
	public PageHelper<AreaGrp> listAreaGrpPage(@PathVariable("current") Integer current, @PathVariable("size") Integer size) {
		Page<AreaGrp> pageInfo = areaGrpService.getAreaGrpPage(PageUtils.getPageParam(new PageHelper(current, size)));
		return PageHelper.getInstance().pageData(pageInfo);
	}

	@GetMapping("/list/page/county/{current}/{size}")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "current", value = "当前页", paramType = "path", dataType = "int"),
			@ApiImplicitParam(name = "size", value = "每页记录数", paramType = "path", dataType = "int")
	})
	@ApiOperation("获取三级区域分组所有信息并分页")
	public PageHelper<AreaGrp> listThirdClassAreaGrp(@PathVariable("current") Integer current, @PathVariable("size") Integer size) {
		Page<AreaGrp> pageInfo = areaGrpService.getThirdClassAreaGrpPage(PageUtils.getPageParam(new PageHelper(current, size)));
		return PageHelper.getInstance().pageData(pageInfo);
	}

	@GetMapping("/list/all")
	@ApiOperation("嵌套方式返回区域分组信息")
	public Set<AreaGrpModel> listAllAreaGrp(HttpServletRequest request){
		return areaGrpService.getAllAreaGrp(request);
	}

	@GetMapping("/list/tree/{areaGrpId}")
	@ApiOperation("通过子区域分组id获取父级，父级的父级区域分组id")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "custId", value = "区域分组id", paramType = "path", dataType = "int"),
	})
	public Set<Integer> listParentAreaGrpIds(@PathVariable("areaGrpId") Integer subAreaGrpId){
		return areaGrpService.getParentAreaGrpIds(subAreaGrpId);
	}

	@GetMapping("/list/sub/{areaGrpId}")
	@ApiOperation("通过区域分组id获取子区域分组id")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "custId", value = "区域分组id", paramType = "path", dataType = "int"),
	})
	public Set<Integer> listSubAreaGrpIds(@PathVariable("areaGrpId") Integer areaGrpId){
		return  areaGrpService.getSubAreaGrpIds(areaGrpId);
	}

}