package com.trenska.longwang.filter;

import java.io.IOException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.commons.lang3.StringUtils;
import com.trenska.longwang.constant.Constant;
import com.trenska.longwang.util.ResponseUtil;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.springframework.data.redis.core.RedisTemplate;
import com.trenska.longwang.context.ApplicationContextHolder;

/**
 * 2019/4/3
 * 创建人:Owen
 * 自定义Shiro Filter，用于处理token令牌
 * 如果redis中的令牌为null 提示登陆超时，如果不为null则匹配header和reids中token是否相同，相同才允许访问资源
 */
public class AccessControlTokenFilter extends AccessControlFilter {
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
		return false;
	}

	/**
	 * 表示当访问拒绝时是否已经处理了；如果返回true表示需要继续处理；如果返回false表示该拦截器实例已经处理了，将直接返回即可。
	 * onAccessDenied()是否执行取决于isAccessAllowed()的值，如果返回true则onAccessDenied()不会执行；如果返回false，执行onAccessDenied()
	 * 如果onAccessDenied()也返回false，则直接返回，不会进入请求的方法（只有isAccessAllowed()和onAccessDenied()的情况下）
	 */
	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse resp) throws IOException {
		// 出鬼了，获取不到Principal了...
//		SysEmp sysEmp = (SysEmp) getSubject(request, response).getPrincipal();

		RedisTemplate<String, String> redisTemplate = ApplicationContextHolder.getBean("redisTemplate");
		HttpServletRequest req = (HttpServletRequest) request;
		String token = req.getHeader("token");
		// 如果无令牌
		if(StringUtils.isEmpty(token)){
			HttpServletResponse response = (HttpServletResponse) resp;
			ResponseUtil.accessDenied(response,Constant.TOKEN_MISSING,Constant.TOKEN_MISSING_MSG);
		}
		String tokenInRedis = redisTemplate.opsForValue().get(Constant.ACCESS_TOKEN_IDENTIFIER + token);
		// 如果令牌超时
		if (StringUtils.isEmpty(tokenInRedis)){
			HttpServletResponse response = (HttpServletResponse) resp;
			ResponseUtil.accessDenied(response,Constant.ACCESS_TIMEOUT,Constant.ACCESS_TIMEOUT_MSG);
		}
		/**
		 * 如果Header里面的token和Redis里面的token都不为null并且匹配成功则允许访问资源
		 */
		return token.equals(tokenInRedis);
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