package com.trenska.longwang.validate;

import com.trenska.longwang.util.ResponseUtil;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 2019/12/14
 * 创建人:Owen
 */
public class ValidateUtil {
	public static void validateString(String str, String paramName, boolean isAllowEmpty, int bounds) throws IOException {
		if (StringUtils.isEmpty(str) && !isAllowEmpty) {
			ResponseUtil.accessDenied(HttpServletResponse.SC_BAD_REQUEST, "parameter " + paramName + " can not be empty.", "non empty");
			throw new RuntimeException("参数错误.");
		}
		if (str.length() > bounds) {
			ResponseUtil.accessDenied(HttpServletResponse.SC_BAD_REQUEST, "parameter " + paramName + " 's length can not be longer than " + bounds, "length limits");
			throw new RuntimeException("参数错误.");
		}
	}
}