package com.trenska.longwang.session;

import com.trenska.longwang.constant.DragonConstant;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.Serializable;

/**
 * 2019/9/19
 * 创建人:Owen
 * 解决前后端分离无法获取Principal的和对接口做权限认证的问题
 */
public class LongWangShiroSessionManager extends DefaultWebSessionManager {

	private static final String AUTHORIZATION = DragonConstant.TOKEN_NAME;

	private static final String REFERENCED_SESSION_ID_SOURCE = "Stateless Request";

	@Override
	protected Serializable getSessionId(ServletRequest request, ServletResponse response) {

		String id = WebUtils.toHttp(request).getHeader(AUTHORIZATION);

 		if(StringUtils.isNotEmpty(id)){
			request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE,REFERENCED_SESSION_ID_SOURCE);
			request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID, id);
			request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_IS_VALID, Boolean.TRUE);
			return id;
		}else {
			return super.getSessionId(request,response);
		}
	}
}