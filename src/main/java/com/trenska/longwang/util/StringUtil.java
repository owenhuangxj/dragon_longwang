package com.trenska.longwang.util;

import com.trenska.longwang.constant.Constant;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 2019/4/29
 * 创建人:Owen
 */
public class StringUtil {
	/**
	 * 将大写字母转换为下横线,方便将驼峰属性转换为下横线+小写的形式
	 */
	public static String transferCamelToUnderline(@NotNull String src) {
		char[] chars = src.toCharArray();
		String dest = "";
		for (int i = 0; i < chars.length; i++) {
			if (i < chars.length - 1 && chars[i] >= 'A' && chars[i] <= 'Z' && chars[i + 1] > 'A' && chars[i + 1] < 'Z') {
				dest += '_';
				dest += ((char) (32 + chars[i]));
				i++;
			} else if (chars[i] >= 'A' && chars[i] <= 'Z') {
				dest += '_';
				dest += ((char) (32 + chars[i]));
			} else {
				dest += chars[i];
			}
		}
		return dest;
	}

	public static String makeNameNo(String name, String no) {
		return name.concat(Constant.MINUS).concat(no);
	}

	/**
	 * 检查 @param str是否为数字
	 *
	 * @param str
	 * @param allowNull
	 * @return
	 */
	public static boolean isNumeric(String str, boolean allowNull) {
		if (allowNull && StringUtils.isEmpty(str)) {
			return true;
		}else if (!allowNull && StringUtils.isEmpty(str)) {
			return false;
		}
		String regex = "^[+|-]?[1-9]?[0-9]{1,9}(\\.[0-9]{1,4})?$";
		Pattern compile = Pattern.compile(regex);
		Matcher matcher = compile.matcher(str);
		return matcher.matches();
	}

	/**
	 * 移除数字的 +/- 前缀
	 * @param amount
	 * @return
	 */
	public static String replacePrefix(Optional<String> amount) {
		return amount.get().replaceFirst("//+","").replaceFirst("//-","");
	}
}