package com.trenska.longwang.util;

import com.trenska.longwang.context.ApplicationContextHolder;
import com.trenska.longwang.entity.sys.SysConfig;

/**
 * 2019/9/2
 * 创建人:Owen
 */
public class SysConfigUtil {
	public static SysConfig getSysConfig(){
		SysConfig sysConfig = ApplicationContextHolder.getBean(SysConfig.class);
		if(null == sysConfig){
			sysConfig = new SysConfig();
		}
		return sysConfig;
	}
}