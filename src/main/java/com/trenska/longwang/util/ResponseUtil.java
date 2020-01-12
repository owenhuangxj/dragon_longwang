package com.trenska.longwang.util;

import com.alibaba.fastjson.JSON;
import com.trenska.longwang.model.sys.ResponseModel;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * 2019/5/22
 * 创建人:Owen
 */
public class ResponseUtil {

	public static void accessDenied(int statusCode, String msg, String reason) throws IOException {

		ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

		HttpServletResponse response = requestAttributes.getResponse();

		response.setStatus(statusCode);

		response.setCharacterEncoding(StandardCharsets.UTF_8.name());

		response.setContentType("application/json;charset=utf-8");

		PrintWriter writer = response.getWriter();

		writer.write(JSON.toJSONString(ResponseModel.getInstance().succ(false).code(statusCode).reason(reason).msg(msg)));

		writer.flush();

		writer.close();
	}

	public static void accessDenied(HttpServletResponse response , int statusCode , String msg) throws IOException {

		response.setStatus(statusCode);

		response.setCharacterEncoding(StandardCharsets.UTF_8.name());

		response.setContentType("application/json;charset=utf-8");

		PrintWriter writer = response.getWriter();

		writer.write(JSON.toJSONString(ResponseModel.getInstance().succ(false).data(null).msg(msg)));

		writer.flush();

		writer.close();
	}
}