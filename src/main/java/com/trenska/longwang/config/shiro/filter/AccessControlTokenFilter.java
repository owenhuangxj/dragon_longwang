package com.trenska.longwang.config.shiro.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.trenska.longwang.entity.sys.SysEmp;
import com.trenska.longwang.util.JasyptUtil;
import com.trenska.longwang.util.SysUtil;
import org.apache.commons.lang3.StringUtils;
import com.trenska.longwang.constant.DragonConstant;
import com.trenska.longwang.util.ResponseUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.web.filter.AccessControlFilter;

/**
 * 2019/4/3
 * 创建人:Owen
 * 自定义Shiro Filter，用于处理token令牌
 * 如果redis中的令牌为null 提示登陆超时，如果不为null则匹配header和Redis中token是否相同，相同才允许访问资源
 */
public class AccessControlTokenFilter extends AccessControlFilter {
	private boolean closeLoginCheck;

	public AccessControlTokenFilter(boolean closeLoginCheck) {
		this.closeLoginCheck = closeLoginCheck;
	}

	/**
	 * 表示是否允许访问；mappedValue就是[urls]配置中拦截器参数部分，如果允许访问返回true，否则false；
	 * (感觉这里应该是对白名单（不需要登录的接口）放行的)
	 * 如果isAccessAllowed返回true则onAccessDenied方法不会继续执行
	 * 这里可以用来判断一些不被通过的链接
	 * * 表示是否允许访问 ，如果允许访问返回true，否则false；
	 *
	 * @param mappedValue 表示写在拦截器中括号里面的字符串, mappedValue 就是 [urls] 配置中拦截器参数部分
	 */
	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
		fillCorsHeader((HttpServletRequest) request, (HttpServletResponse) response);
		Set<String> values = Arrays.stream(((String[]) mappedValue)).collect(Collectors.toSet());
		if (values.contains("test")) {
			return true;
		}
		return false;
	}

	/**
	 * 表示当访问拒绝时是否已经处理了；如果返回true表示需要继续处理；如果返回false表示该拦截器实例已经处理了，将直接返回即可。
	 * onAccessDenied()是否执行取决于isAccessAllowed()的值，如果返回true则onAccessDenied()不会执行；如果返回false，执行onAccessDenied()
	 * 如果onAccessDenied()也返回false，则直接返回，不会进入请求的方法（只有isAccessAllowed()和onAccessDenied()的情况下）
	 */
	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws IOException {
		// 出鬼了，获取不到Principal了...
		SysEmp sysEmp = (SysEmp) getSubject(request, response).getPrincipal();
		if (closeLoginCheck) {
			return true;
		}
		HttpServletRequest req = (HttpServletRequest) request;
		String tokenInHeader = req.getHeader(DragonConstant.TOKEN_NAME);

		// 如果无令牌
		if (StringUtils.isEmpty(tokenInHeader)) {
			ResponseUtil.accessDenied(DragonConstant.TOKEN_MISSING, DragonConstant.TOKEN_MISSING_MSG,
					DragonConstant.TOKEN_MISSING_MSG);
			return false;
		}

		String decryptTokenInHeader = JasyptUtil.decrypt("dragon-erp", tokenInHeader);
		String tokenInRedis = SysUtil.getTokenInRedis(Optional.of(decryptTokenInHeader));
		// 如果令牌超时
		if (StringUtils.isEmpty(tokenInRedis)) {
			ResponseUtil.accessDenied(DragonConstant.ACCESS_TIMEOUT, DragonConstant.ACCESS_TIMEOUT_MSG, DragonConstant.ACCESS_TIMEOUT_MSG);
		}
		// 如果Header里面的token和Redis里面的token都不为null则允许访问资源
		return true;
	}

	/**
	 * 处理跨域
	 */
	private void fillCorsHeader(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"));
		httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,OPTIONS,DELETE,HEAD");
		httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));
	}
}