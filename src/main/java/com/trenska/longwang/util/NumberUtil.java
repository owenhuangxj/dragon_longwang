package com.trenska.longwang.util;

/**
 * 2019/5/12
 * 创建人:Owen
 */
public class NumberUtil {

	public static boolean isIntegerUsable(Integer value){
		return value != null && value > 0;
	}

	public static boolean isLongUsable(Long value){
		return value != null && value > 0;
	}
	public static boolean isIntegerNotUsable(Integer value){
		return !isIntegerUsable(value);
	}
	public static boolean isLongNotUsable(Long value){
		return !isLongUsable(value);
	}
	public static boolean isDoubleUsable(Double value){
		return value != null && value != 0;
	}
}
