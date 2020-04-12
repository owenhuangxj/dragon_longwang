package com.trenska.longwang.interceptor;

import com.trenska.longwang.constant.DragonConstant;
import com.trenska.longwang.util.JasyptUtil;
import com.trenska.longwang.util.ResponseUtil;
import com.trenska.longwang.util.SysUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * 2019/7/15
 * 创建人:Owen
 * 拦截器实现登陆超时控制；检查存入reids中的token是否已经过期来控制
 */
@Slf4j
public class LoginControlInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String requestURI = request.getRequestURI();
		boolean isLogin = requestURI.contains("login");
		boolean isLogout = requestURI.contains("logout");

		if (isLogin || isLogout) {
			return true;
		}

		String tokenInHeader = request.getHeader(DragonConstant.TOKEN_NAME);

		tokenInHeader = JasyptUtil.decrypt("dragon-erp",tokenInHeader);

		String tokenInRedis = SysUtil.getTokenInRedis(Optional.of(tokenInHeader));

		if (StringUtils.isEmpty(tokenInRedis)) {
			ResponseUtil.accessDenied(DragonConstant.ACCESS_TIMEOUT, DragonConstant.ACCESS_TIMEOUT_MSG, "com.trenska.longwang.timeout");
			return false;
		} else {
			if (!tokenInHeader.equals(tokenInRedis)) {
				ResponseUtil.accessDenied(DragonConstant.LOGGED_OTHER_PLACE, DragonConstant.LOGGED_OTHER_PLACE_MSG,
						"com.trenska.longwang.sso");
				return false;
			}
		}
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
						   ModelAndView mv) throws Exception {
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
								Exception ex) throws Exception {
	}
}