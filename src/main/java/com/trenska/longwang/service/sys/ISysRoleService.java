package com.trenska.longwang.service.sys;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.entity.sys.SysRole;
import com.trenska.longwang.model.sys.CommonResponse;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ISysRoleService extends IService<SysRole> {
    /**
     * 获取账户的角色
     * @param empId 账户id
     */
    Set<SysRole> getRolesByEmpId(Integer empId);

	CommonResponse removeRolesByIds(List<Integer> rids);

	Page<SysRole> getSysRolesSelective(Map<String, Object> params, Page page);

	CommonResponse removeRolesById(Integer rid);

	CommonResponse saveOrUpdateRolePerms(Integer rid, List<String> pvals);

	CommonResponse editEmpDataAuthority(Integer empId, boolean allData, List<Integer> areaGrpIds);

	CommonResponse editEmpRoles(Integer empId, List<Integer> roleIds);
}
