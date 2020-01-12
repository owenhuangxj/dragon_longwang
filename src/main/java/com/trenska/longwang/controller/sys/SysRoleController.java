package com.trenska.longwang.controller.sys;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.annotation.CheckDuplicateSubmit;
import com.trenska.longwang.constant.Constant;
import com.trenska.longwang.entity.PageHelper;
import com.trenska.longwang.entity.sys.*;
import com.trenska.longwang.model.sys.PermModel;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.sys.*;
import com.trenska.longwang.util.NumberUtil;
import com.trenska.longwang.util.PageUtils;
import com.trenska.longwang.util.TimeUtil;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@Slf4j
@RequestMapping("/role")
@Api(value = "RoleController", description = "角色接口")
public class SysRoleController {

	@Autowired
	private ISysRoleService roleService;

	@Autowired
	private ISysPermService permService;

	@Autowired
	private ISysRolePermService rolePermService;

	@Autowired
	private ISysEmpRoleService empRoleService;

	@PostMapping("/add")
	@CheckDuplicateSubmit
	@ApiOperation(value = "添加角色")
	public ResponseModel add(@RequestBody @Valid @ApiParam(name = "role", value = "角色") SysRole role) {
		if (null == role) {
			return ResponseModel.getInstance().succ(false).msg("角色信息不能为空.");
		}
		if (role.getRid() != null) {
			SysRole sysRole = roleService.getById(role.getRid());
			if (sysRole != null) {
				return ResponseModel.getInstance().succ(false).msg("角色已经存在.");
			}
		}
		role.setRoleCreated(TimeUtil.getCurrentTime(Constant.TIME_FORMAT));
		roleService.save(role);
		return ResponseModel.getInstance().succ(true).msg("角色添加成功.");
	}

	@CheckDuplicateSubmit
	@DeleteMapping("/delete/batch")
	@ApiOperation(value = "批量删除角色")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "rids" , value = "角色id集合" , paramType = "body" , dataType = "list" , required = true)

	})
	public ResponseModel deleteBatch(@RequestBody List<Integer> rids) {
		if (rids == null || rids.isEmpty()) {
			return ResponseModel.getInstance().succ(false).msg("请选择要删除的角色.");
		}
		return roleService.removeRolesByIds(rids);

	}

	@DeleteMapping("/delete/{rid}")
	@ApiOperation(value = "删除角色")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "rid" , value = "角色id" , paramType = "path" , dataType = "int" , required = true)
	})
	public ResponseModel delete(@PathVariable("rid") Integer rid) {
		if (!NumberUtil.isIntegerUsable(rid)) {
			return ResponseModel.getInstance().succ(false).msg("请选择要删除的角色.");
		}
		return roleService.removeRolesById(rid);
	}

	@PutMapping("/update")
	@CheckDuplicateSubmit
	@ApiOperation(value = "更新角色,rid为必传参数")
	public ResponseModel update(@RequestBody SysRole role) {

		if (null == role) {
			return ResponseModel.getInstance().succ(false).msg("角色信息不能为空.");
		}
		if(!NumberUtil.isIntegerUsable(role.getRid())){
			return ResponseModel.getInstance().succ(false).msg("角色id不能为空.");
		}

		role.setRoleUpdated(TimeUtil.getCurrentTime(Constant.TIME_FORMAT));
		roleService.updateById(role);

		return ResponseModel.getInstance().succ(true).msg("更新角色成功.");
	}

	@CheckDuplicateSubmit
	@GetMapping("/list/all")
	@ApiOperation(value = "获取所有角色值")
	public List<SysRole> listAll() {
		return roleService.list();
	}

	@GetMapping("/list/{current}/{size}")
	@ApiOperation(value = "角色通用分页")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "rname", value = "角色名" , dataType = "string" , paramType = "query")
	})
	public PageHelper<SysRole> list(
			@RequestParam(name = "rname",required = false) String rname,
			@PathVariable("current") int current,
			@PathVariable("size") int size
	) {
		Map<String,Object> params = new HashMap<>();
		params.put("rname",rname);
		Page page = PageUtils.getPageParam(new PageHelper(current, size));

		Page<SysRole> sysRoles = roleService.getSysRolesSelective(params,page);

		return PageHelper.getInstance().pageData(sysRoles);
	}

	@GetMapping("/check/rname")
	@ApiOperation(value = "检查角色名是否存在")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "rname", value = "角色名,比如管理员", paramType = "query", required = true, dataType = "String")
	})
	public ResponseModel checkVExists(String rname) {
		if(StringUtils.isEmpty(rname)){
			return ResponseModel.getInstance().succ(false).msg("角色名不能为空.");
		}
		SysRole role = roleService.getOne(new QueryWrapper<SysRole>().eq("rname", rname));
		return ResponseModel.getInstance().succ(null == role).msg(null == role ? "角色名不存在" : "角色名已经存在.");
	}

	@PostMapping("/add/perm")
	@ApiOperation(value = "为角色添加/更新权限1")
	public ResponseModel addOrUpdatePerms(@RequestBody @Valid @ApiParam(name = "perms", value = "权限信息") List<SysRolePerm> rolePerms) {
		if(rolePerms == null || rolePerms.isEmpty()){
			return ResponseModel.getInstance().succ(false).msg("权限信息不能为空.");
		}
		// 获取角色id
		Integer rid = rolePerms.get(0).getRid();

		if(null == rid || rid < 1){
			return ResponseModel.getInstance().succ(false).msg("无效的角色.");
		}

		List<SysRolePerm> sysRolePerms = rolePermService.list(new QueryWrapper<SysRolePerm>().eq("rid", rid));

		// 去除数据库中重复的记录:所有字段都相同的记录
		if(!sysRolePerms.isEmpty()){
			for(SysRolePerm sysRolePerm : sysRolePerms){
				if(rolePerms.contains(sysRolePerm)){
					rolePerms.remove(sysRolePerm);
				}
			}
		}
		if(rolePerms.isEmpty()){
			return ResponseModel.getInstance().succ(false).msg("额，当前角色以前已经拥有这些角色了，你不需要再添加了.");
		}
		rolePermService.saveBatch(rolePerms);

		return ResponseModel.getInstance().succ(true).msg("权限添加成功.");
	}

	@PostMapping("/add/perm/{rid}")
	@ApiOperation(value = "为角色添加/更新权限2")
	public ResponseModel addOrUpdatePerms(@PathVariable Integer rid , @RequestBody List<String> pvals) {

		if(rid < 1){
			return ResponseModel.getInstance().succ(false).msg("无效的角色.");
		}
		return roleService.saveOrUpdateRolePerms(rid,pvals);
	}

	@GetMapping("/perm/list/role/perms/{rid}")
	@ApiOperation(value = "获取角色权限")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "rid", value = "角色id" , dataType = "int" , paramType = "path")
	})
	public List<PermModel> list(@PathVariable Integer rid) {
		return  permService.getPermsByRid(rid);
	}

	@GetMapping("/perm/list/all")
	@ApiOperation(value = "获取所有权限")
	public List<SysPerm> listAllPerms() {
		return  permService.list();
	}

	@GetMapping("/perm/list/son/{pval}")
	@ApiOperation(value = "获取所有子权限，模块级别的权限parent为0，比如 订单、库存、商品都为模块，次级权限通过传递当前权限的pval进行获取")
	public List<SysPerm> listSubPerms(@PathVariable String pval) {
		return  permService.list(
				new QueryWrapper<SysPerm>()
						.eq("parent",pval)
		);
	}

	@PostMapping("/role/add/{empId}")
	@ApiOperation("添加账号的角色")
	@ApiImplicitParams({
			@ApiImplicitParam(name="empId" , value="账号id" , paramType = "path" , required = true , dataType = "int"),
			@ApiImplicitParam(name="rids" , value="角色id数字" , paramType = "body" , required = true , dataType = "list")
	})
	public ResponseModel addRolesForEmp(@PathVariable @Min(1) @Valid Integer empId , @RequestParam List<Integer> rids){
		if(rids == null || rids.isEmpty()){
			return ResponseModel.getInstance().succ(true).msg("角色不能为空");
		}

		// 获取账号所有角色
		List<SysEmpRole> empRoles = empRoleService.list(
				new QueryWrapper<SysEmpRole>().eq("emp_id",empId)
		);
		// 筛查重复角色
		empRoles.stream().map(SysEmpRole::getRid).collect(Collectors.toList()).forEach(rid->{
			if (rids.contains(rid)){
				rids.remove(rid);
			}
		});
		// 如果选择的所有角色都已拥有，提示->
		if(rids.isEmpty()){
			return ResponseModel.getInstance().succ(true).msg("额，账号已经拥有这些角色，不需要再添加.");
		}

		List<SysEmpRole> sysEmpRoles = new ArrayList<>();
		rids.forEach(rid->{
			sysEmpRoles.add(new SysEmpRole(empId,rid));
		});
		// 添加角色
		empRoleService.saveBatch(sysEmpRoles);

		return ResponseModel.getInstance().succ(true).msg("账号添加角色成功");
	}

	@PostMapping("/role/delete/{empId}")
	@ApiOperation("删除账号的角色")
	@ApiImplicitParams({
			@ApiImplicitParam(name="empId" , value="账号id" , paramType = "path" , required = true , dataType = "int"),
			@ApiImplicitParam(name="rids" , value="角色id数字" , paramType = "body" , required = true , dataType = "list")
	})
	public ResponseModel deleteRolesForEmp(@PathVariable Integer empId , @RequestBody List<Integer> rids){

		if(rids == null || rids.isEmpty()){
			return ResponseModel.getInstance().succ(true).msg("请选择需要删除的角色");
		}

		// 获取账号所有角色
		List<SysEmpRole> empRoles = empRoleService.list(
				new QueryWrapper<SysEmpRole>().eq("emp_id",empId)
		);

		List<Integer> deletingIds = new ArrayList<>();

		// 筛查出需要删除的角色
		empRoles.stream().map(SysEmpRole::getRid).collect(Collectors.toList()).forEach(rid->{
			if (rids.contains(rid)){
				deletingIds.add(rid);
			}
		});

		// 如果选择的所有角色都已拥有，提示->
		if(deletingIds.isEmpty()){
			return ResponseModel.getInstance().succ(true).msg("额，账号不拥有这些角色，不需要删除.");
		}

		empRoleService.removeByIds(deletingIds);

		return ResponseModel.getInstance().succ(true).msg("账号添加角色成功");
	}


	@PostMapping("/role/edit/{empId}")
	@ApiOperation("编辑账号的角色-账号角色的增删改")
	public ResponseModel editEmpRoles(@PathVariable @Min(1) @Valid Integer empId , @RequestParam List<Integer> rids){

		if(NumberUtil.isIntegerNotUsable(empId)){
			return ResponseModel.getInstance().succ(false).msg("无此账号");
		}

		return roleService.editEmpRoles(empId,rids);
	}

	/**
	 * 编辑账号的数据权限==>账号负责的区域
	 * @param empId
	 * @param areaGrpIds
	 * @return
	 */
	@PostMapping("/area/edit/{empId}/{allData}")
	@ApiOperation("编辑账号的数据权限")
	public ResponseModel editEmpDataAuthority(@PathVariable("empId") Integer empId ,@PathVariable("allData") boolean allData, @RequestParam("areaGrpIds") List<Integer> areaGrpIds){
		if(NumberUtil.isIntegerNotUsable(empId)){
			return ResponseModel.getInstance().succ(false).msg("无效的账号");
		}
		return roleService.editEmpDataAuthority( empId,allData,areaGrpIds);

	}
}
