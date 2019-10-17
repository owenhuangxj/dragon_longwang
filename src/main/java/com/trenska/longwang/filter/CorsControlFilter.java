package com.trenska.longwang.filter;

import javax.servlet.*;
import java.io.IOException;
import org.apache.http.HttpStatus;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

/**
 * 该 Filter用于解决 HttpServletRequest 获取不到 axios增加的自定义header
 */
@Component
public class CorsControlFilter implements Filter {

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

        //跨域
        response.setHeader("Access-Control-Allow-Origin", "*");
        //跨域 Header
        response.setHeader("Access-Control-Allow-Methods", "*"); // or *
        response.setHeader("Access-Control-Allow-Headers", "Content-Type,token,XFILENAME,XFILECATEGORY,XFILESIZE"); // or *
        // 浏览器是会先发一次options请求，如果请求通过，则继续发送正式的post请求
        // 配置options的请求返回
        if (request.getMethod().equals("OPTIONS")) {
            response.setStatus(HttpStatus.SC_OK);
            response.getWriter().write("OPTIONS ARE ALLOWED TO ACCESS");
            return;
        }
        chain.doFilter(req, res);
    }
    public void init(FilterConfig filterConfig) {}
    public void destroy() {}
}