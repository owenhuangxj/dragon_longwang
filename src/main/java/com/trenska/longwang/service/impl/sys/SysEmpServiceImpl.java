package com.trenska.longwang.service.impl.sys;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.dao.sys.SysEmpMapper;
import com.trenska.longwang.entity.sys.EmpAreaGrp;
import com.trenska.longwang.entity.sys.SysConfig;
import com.trenska.longwang.entity.sys.SysEmp;
import com.trenska.longwang.entity.sys.SysEmpRole;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.service.sys.ISysEmpRoleService;
import com.trenska.longwang.service.sys.ISysEmpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * public class SysEmpServiceImpl extends ServiceImpl<SysEmpMapper, SysEmp> implements ISysEmpService
 * 中的SysUserMapper就是
 * public SysEmp getUserByEmpAcct(String uname) {
 * return baseMapper.getSysUserByUname(uname);
 * }
 * 中的baseMapper,因为ServiceImpl<M extends BaseMapper<T>, T>
 * public class ServiceImpl<M extends BaseMapper<T>, T> implements IService<T> {
 *
 * @Autowired protected M baseMapper;
 * ...
 * }
 */
@Service
@SuppressWarnings("all")
public class SysEmpServiceImpl extends ServiceImpl<SysEmpMapper, SysEmp> implements ISysEmpService {

	@Autowired
	private ISysEmpRoleService sysEmpRoleService;

	@Autowired
	private EmpAreaGrpServiceImpl empAreaGrpService;

	@Override
	public Page<SysEmp> getSysEmpPage(Map<String,Object> params,Page page) {
		page.setRecords(super.baseMapper.selectSysEmpPageSelective( params,page));
		page.setTotal(super.baseMapper.selectSysEmpCountSelective(params));
		return page;
	}

	@Override
	@Transactional
	public ResponseModel saveEmp(SysEmp emp) {

		this.save(emp);

		/**
		 * 保存后mybatis-plus默认的配置会返回id
		 */
		Integer empId = emp.getEmpId();

		new SysConfig(emp.getEmpId()).insert();

		List<SysEmpRole> empRoles = new ArrayList<>();

		if(!emp.getRids().isEmpty()){
			for(Integer rid : emp.getRids()){
				empRoles.add(new SysEmpRole(emp.getEmpId(),rid));
			}
		}
		sysEmpRoleService.saveBatch(empRoles);

		// 保存用户的数据权限 => 负责的区域分组
		emp.getAreaGrpIds().forEach(areaGrpId->{
			empAreaGrpService.save(new EmpAreaGrp(empId,areaGrpId));
		});

		return ResponseModel.getInstance().succ(true).msg("添加账号成功.");
	}

	@Override
	public ResponseModel updateEmp(SysEmp emp) {
		this.updateById(emp);
		List<SysEmpRole> empRoles = new ArrayList<>();
		// 获取账号的所有角色
		List<SysEmpRole> sysEmpRoles = sysEmpRoleService.list(
				new QueryWrapper<SysEmpRole>()
						.eq("emp_id", emp.getEmpId())
		);
		// 前端传递的角色
		List<Integer> rids = emp.getRids();

		// 剔除已经拥有的角色
		if(!rids.isEmpty() && !sysEmpRoles.isEmpty()){
			for(SysEmpRole empRole : sysEmpRoles){
				if(rids.contains(empRole.getRid())){
					rids.remove(empRole.getRid());
				}
			}
		}
		// 插入未拥有的角色
		if(!rids.isEmpty()){
			List<SysEmpRole> insertings = new ArrayList<>();
			rids.forEach(rid->{
				insertings.add(new SysEmpRole(emp.getEmpId(),rid));
			});
			sysEmpRoleService.saveBatch(insertings);
		}

		return ResponseModel.getInstance().succ(true).msg("更新账号信息成功");
	}
}
