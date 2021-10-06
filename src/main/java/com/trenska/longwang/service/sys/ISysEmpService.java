package com.trenska.longwang.service.sys;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.plugins.Page;
import com.trenska.longwang.entity.sys.SysEmp;
import com.trenska.longwang.model.sys.CommonResponse;

import java.util.Map;

public interface ISysEmpService extends IService<SysEmp> {

	Page<SysEmp> getSysEmpPage(Map<String,Object> params,Page page);

	CommonResponse saveEmp(SysEmp emp);

	CommonResponse updateEmp(SysEmp emp);
}
