package com.trenska.longwang.service.sys;

import com.baomidou.mybatisplus.extension.service.IService;
import com.trenska.longwang.entity.sys.SysPerm;
import com.trenska.longwang.model.sys.PermModel;

import java.util.List;
import java.util.Set;

public interface ISysPermService extends IService<SysPerm> {
    /**
     * 获取账户权限
     * @param empId 账户id
     */
    Set<SysPerm> getPermsByEmpId(Integer empId);

	List<PermModel> getPermsByRid(Integer rid);

}
