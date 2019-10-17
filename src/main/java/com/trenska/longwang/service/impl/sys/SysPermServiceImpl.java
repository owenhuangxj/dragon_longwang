package com.trenska.longwang.service.impl.sys;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.trenska.longwang.dao.sys.SysPermMapper;
import com.trenska.longwang.dao.sys.SysRolePermMapper;
import com.trenska.longwang.entity.sys.SysPerm;
import com.trenska.longwang.model.sys.PermModel;
import com.trenska.longwang.service.sys.ISysPermService;
import com.trenska.longwang.util.ObjectCopier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("all")
public class SysPermServiceImpl extends ServiceImpl<SysPermMapper, SysPerm> implements ISysPermService {

	@Autowired
	private SysRolePermMapper rolePermMapper;
    @Override
    public Set<SysPerm> getPermsByEmpId(Integer empId) {
        return super.baseMapper.selectPermsByEmpId(empId);
    }

	@Override
	public List<PermModel> getPermsByRid(Integer rid) {
    	// 获取所有权限
		List<SysPerm> allPerms = new SysPerm().selectAll();
		// 获取角色权限
		List<PermModel> rolePerms = super.baseMapper.selectSubPermsByRid(rid);

		for(SysPerm sysPerm : allPerms){
			PermModel permModel = new PermModel();
			ObjectCopier.copyProperties(sysPerm,permModel);
			permModel.setChecked(false);
			// 合并权限
			if (!rolePerms.contains(permModel)){
				rolePerms.add(permModel);
			}
		}

		for(PermModel permModel : rolePerms){
			List<PermModel> subList = getSubList(permModel.getPval(), rolePerms);
			permModel.setSubPerms(subList);

		}

//		return rolePerms.stream().filter(new Predicate<PermModel>() {
//			@Override
//			public boolean test(PermModel permModel) {
//				if(permModel != null && permModel.getPtype() != null) {
//					return 0 == permModel.getPtype();
//				}else{
//					return false;
//				}
//			}
//		}).collect(Collectors.toList());
		// 只返回ptype为0的权限
		return rolePerms.stream().filter(permModel -> {
			if(permModel != null && permModel.getPtype() != null){
				return 0 == permModel.getPtype();
			}else {
				return false;
			}
		}).collect(Collectors.toList());

	}

	private List<PermModel> getSubList(String pval, List<PermModel> perms) {
		List<PermModel> permModels = new ArrayList<>();
		String parentId;

		//子集的直接子对象
		for (PermModel perm : perms) {
			parentId = perm.getParent();
			if (pval.equals(parentId)) {
				permModels.add(perm);

			}
		}

		//子集的间接子对象
		for (PermModel perm : permModels) {
			perm.setSubPerms(getSubList(perm.getPval(), perms));
		}

		//递归退出条件
		if (permModels.size() == 0) {
			// 最后一级的权限返回空集合而不是null
			return new ArrayList<>();
		}

		return permModels;
	}


}
