package com.trenska.longwang.filter;

import javax.servlet.*;
import java.io.IOException;

import org.apache.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * 该Filter用于解决 HttpServletRequest 获取不到 axios增加的自定义header
 */
@Component
public class CorsControlFilter implements Filter {
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
            ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"); // 跨域
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "*"); // or *
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Content-Type,token,XFILENAME,XFILECATEGORY," +
                "XFILESIZE");
        // 跨域 Header or *
        // 浏览器是会先发一次Options请求，如果请求通过，则继续发送正式的Post请求
        if (request.getMethod().equals("OPTIONS")) {// 配置Options的请求直接返回
            response.setStatus(HttpStatus.SC_OK);
            response.getWriter().write("OPTIONS ARE ALLOWED TO ACCESS");
            return;
        }
        chain.doFilter(req, res);
    }

    public void init(FilterConfig filterConfig) {
    }

    public void destroy() {
    }
}