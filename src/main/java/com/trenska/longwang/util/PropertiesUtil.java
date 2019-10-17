package com.trenska.longwang.util;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;

/**
 * 2019/4/10
 * 创建人:Owen
 */
public class PropertiesUtil {
	/**
	 * 测试所有属性为null
	 * @param obj
	 * @return
	 */
	public static boolean allPropertiesNull(Object obj) {

		if (obj == null) return true;

		Field[] fields = obj.getClass().getDeclaredFields();
		// 初始化 所有属性为 null
		boolean notNull = false;
		for (Field field : fields) {
			//不检查 直接取值
			field.setAccessible(true);
			try {
				if (field.get(obj) != null && StringUtils.isNotEmpty(field.get(obj).toString())) {
					// 不为空成立
					notNull = true;
					// 当有任何一个参数不为空的时候则跳出
					break;
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return !notNull;
	}
}
