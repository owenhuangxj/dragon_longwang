package com.trenska.longwang.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 2019/12/29
 * 创建人:Owen
 */
public class HttpUtil {
	public final static String getHttpMethod() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		String httpMethod = request.getMethod();
		return httpMethod;
	}
}
