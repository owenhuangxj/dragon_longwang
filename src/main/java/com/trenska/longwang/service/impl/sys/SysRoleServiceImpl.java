package com.trenska.longwang.service.impl.sys;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.dao.sys.EmpAreaGrpMapper;
import com.trenska.longwang.dao.sys.SysEmpRoleMapper;
import com.trenska.longwang.dao.sys.SysRoleMapper;
import com.trenska.longwang.dao.sys.SysRolePermMapper;
import com.trenska.longwang.entity.sys.*;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.sys.ISysRolePermService;
import com.trenska.longwang.service.sys.ISysRoleService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("all")
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

	@Autowired
	private SysEmpRoleMapper empRoleMapper;

	@Autowired
	private SysRolePermMapper rolePermMapper;

	@Autowired
	private ISysRolePermService rolePermService;

	@Autowired
	private EmpAreaGrpMapper empAreaGrpMapper;

    @Override
    public Set<SysRole> getRolesByEmpId(Integer empId) {
        return super.baseMapper.selectRolesByEmpId(empId);
    }

	@Override
	public ResponseModel removeRolesByIds(List<Integer> rids) {

		boolean success = removeByIds(rids);
		// 删除用户角色
    	empRoleMapper.delete(
    			new QueryWrapper<SysEmpRole>()
						.in("rid",rids)
		);
    	// 删除角色权限
		rolePermMapper.delete(
				new QueryWrapper<SysRolePerm>()
						.in("rid",rids)
		);
		return ResponseModel.getInstance().succ(success).msg(success?"删除角色成功.":"不存在该角色");

	}

	@Override
	public Page<SysRole> getSysRolesSelective(Map<String, Object> params, Page page) {
		page.setRecords(super.baseMapper.selectSysRolesSelective(params,page));
		page.setTotal(super.baseMapper.selectSysRolesCountSelective(params));
		return page;
	}

	@Override
	public ResponseModel removeRolesById(Integer rid) {
		boolean success = removeById(rid);
		// 删除用户角色
		empRoleMapper.delete(
				new QueryWrapper<SysEmpRole>()
						.eq("rid",rid)
		);
		// 删除角色权限
		rolePermMapper.delete(
				new QueryWrapper<SysRolePerm>()
						.eq("rid",rid)
		);
		return ResponseModel.getInstance().succ(success).msg(success?"删除角色成功.":"不存在该角色");
	}

	@Override
	public ResponseModel saveOrUpdateRolePerms(Integer rid, List<String> pvals) {

		// 查询角色权限
		List<SysRolePerm> dbSysRolePerms = rolePermMapper.selectList(new QueryWrapper<SysRolePerm>().eq("rid", rid));
		// 如果 传递的权限为空
		if(CollectionUtils.isEmpty(pvals)){
			// 如果角色以前拥有权限，那么就是清空权限的操作
			if(CollectionUtils.isNotEmpty(dbSysRolePerms)){
				// 清空角色权限
				rolePermMapper.delete(
						new QueryWrapper<SysRolePerm>()
								.eq("rid",rid)
				);
				return ResponseModel.getInstance().succ(true).msg("清空权限成功.");
			}else {
				return ResponseModel.getInstance().succ(false).msg("貌似您要添加/更新权限?但是你没有选择权限.");
			}
		}

		List<String> dbPvals = dbSysRolePerms.stream().map(SysRolePerm::getPval).collect(Collectors.toList());

		// 如果数据库中的原有权限包含了所有前端传递的权限，那么就是减少权限
		if(CollectionUtils.isNotEmpty(dbPvals) && dbPvals.containsAll(pvals)){
			dbPvals.removeAll(pvals);
			if(CollectionUtils.isEmpty(dbPvals)){
				return ResponseModel.getInstance().succ(true).msg("刷新成功.");
			}
			rolePermMapper.delete(new QueryWrapper<SysRolePerm>().in("pval",dbPvals));
			return ResponseModel.getInstance().succ(true).msg("更新权限成功.");
			// 如果前端传递的权限包含了所有数据库中的原有权限，那么就是增加权限
		}else if(!dbPvals.isEmpty() && pvals.containsAll(dbPvals)){
			pvals.removeAll(dbPvals);
			List<SysRolePerm> insertings = new ArrayList<>();
			pvals.forEach(pval -> {
				insertings.add(new SysRolePerm(rid,pval));
			});
			rolePermService.saveBatch(insertings);
			return ResponseModel.getInstance().succ(true).msg("权限添加成功.");
		}

		if(!dbSysRolePerms.isEmpty()){
			for(SysRolePerm rolePerm : dbSysRolePerms){
				if(pvals.contains(rolePerm.getPval())) {
					pvals.remove(rolePerm.getPval());
				}
			}
		}
		if(pvals.isEmpty()){
			return ResponseModel.getInstance().succ(false).msg("额，当前角色以前已经拥有这些角色了，你不需要再添加了.");
		}

		List<SysRolePerm> insertings = new ArrayList<>();
		for(String pval : pvals){
			insertings.add(new SysRolePerm(rid,pval));
		}
		rolePermService.saveBatch(insertings);

		return ResponseModel.getInstance().succ(true).msg("权限添加成功.");
	}

	@Override
	@Transactional
	public ResponseModel editEmpDataAuthority(Integer empId, boolean allData, List<Integer> areaGrpIds) {
		// 删除旧的客户区域分组信息
		empAreaGrpMapper.delete(
				new LambdaQueryWrapper<EmpAreaGrp>()
						.eq(EmpAreaGrp::getEmpId,empId)
		);

		if(!Objects.isNull(areaGrpIds)&&!areaGrpIds.isEmpty()){
			// 插入新的客户区域分组信息
			for(int areaGrpId : areaGrpIds){
				empAreaGrpMapper.insert(new EmpAreaGrp(empId,areaGrpId));
			}
		}

		// 如果拥有全部数据权限需要更新用户表的allData
		if(allData){
			SysEmp sysEmp = new SysEmp();
			sysEmp.setEmpId(empId);
			sysEmp.setAllData(allData);
			sysEmp.updateById();
		}
		return ResponseModel.getInstance().succ(true).msg("编辑成功");

	}

	@Override
	@Transactional
	public ResponseModel editEmpRoles(Integer empId, List<Integer> roleIds) {
		// 获取账号所有角色
//		List<SysEmpRole> dbEmpRoles = empRoleMapper.selectList(
//				new LambdaQueryWrapper<SysEmpRole>()
//						.eq(SysEmpRole::getEmpIdInToken,empId)
//		);
		// 删除旧角色
		empRoleMapper.delete(
				new LambdaQueryWrapper<SysEmpRole>()
						.eq(SysEmpRole::getEmpId,empId)
		);
		// 添加新角色
		if(!Objects.isNull(roleIds) && !roleIds.isEmpty()){
			roleIds.forEach(roleId->{
				empRoleMapper.insert(new SysEmpRole(empId,roleId));
			});
		}
		return ResponseModel.getInstance().succ(true).msg("编辑成功");
	}
}
