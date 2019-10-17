package com.trenska.longwang.util;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 2019/4/7
 * 创建人:Owen
 */
public class TimeUtil {
	public static String getCurrentDate(){
		return new Date(System.currentTimeMillis()).toString();
	}

	public static String getCurrentTime(String pattern) {
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
	}

}
