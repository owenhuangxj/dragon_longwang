package com.trenska.longwang.service.sys;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.entity.sys.SysEmpRole;
import com.trenska.longwang.entity.sys.SysRole;
import com.trenska.longwang.model.sys.ResponseModel;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ISysRoleService extends IService<SysRole> {
    /**
     * 获取账户的角色
     * @param empId 账户id
     */
    Set<SysRole> getRolesByEmpId(Integer empId);

	ResponseModel removeRolesByIds(List<Integer> rids);

	Page<SysRole> getSysRolesSelective(Map<String, Object> params, Page page);

	ResponseModel removeRolesById(Integer rid);

	ResponseModel saveOrUpdateRolePerms(Integer rid, List<String> pvals);

	ResponseModel editEmpDataAuthority(Integer empId,boolean allData,List<Integer> areaGrpIds);

	ResponseModel editEmpRoles(Integer empId, List<Integer> roleIds);
}
