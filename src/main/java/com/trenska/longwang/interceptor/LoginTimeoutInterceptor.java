package com.trenska.longwang.interceptor;

import com.alibaba.fastjson.JSON;
import com.trenska.longwang.constant.Constant;
import com.trenska.longwang.model.sys.ResponseModel;
import com.trenska.longwang.util.SysUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

/**
 * 2019/7/15
 * 创建人:Owen
 * 拦截器实现登陆超时控制；检查存入reids中的token是否已经过期来控制
 */
@Slf4j
public class LoginTimeoutInterceptor implements HandlerInterceptor {
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		String requestURI = request.getRequestURI();
		boolean isLogin = requestURI.contains("login");
		boolean isLogout = requestURI.contains("logout");

		if (isLogin || isLogout) {
			return true;
		}

		Integer empIdInRedis = SysUtil.getEmpIdInRedis(request);
		if (ObjectUtils.isEmpty(empIdInRedis)) {
			// HttpServletResponse#reset()方法会清除自身内部的所有数据 Clears any data that exists in the buffer as well as the status code and headers
			// response.reset();
			response.setStatus(Constant.ACCESS_TIMEOUT);
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/json;charset=UTF-8");
			ResponseModel responseModel = ResponseModel.getInstance().succ(false).msg(Constant.LOGIN_TIMEOUT);
			PrintWriter writer = response.getWriter();
			writer.write(JSON.toJSONString(responseModel));
			writer.flush();
			writer.close();
			return false;
		}
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView mv) throws Exception {
		if (null != mv) {
			Map<String, Object> model = mv.getModel();
			Set<String> keySet = model.keySet();
			keySet.forEach(key -> System.out.println(key));
		}
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

	}
}