package com.trenska.longwang.util;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.shiro.util.Assert;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 2019/4/16
 * 创建人:Owen
 * 单据工具类
 */
public class BillsUtil {

	/**
	 * 根据抬头和编号生成单据编号
	 * @param title 单据抬头
	 * @param num 单据编号
	 */
	public static String makeBillNo(String title, Integer num) {
		return title.concat(new SimpleDateFormat("yyyyMMdd").format(new Date())).concat(fillZero(num));
//		return title.concat("-").concat(new SimpleDateFormat("yyyyMMdd").format(new Date())).concat("-").concat(fillZero(history));
	}

	/**
	 * 对小于100000的数字填充前缀0
	 */
	private static String fillZero(Integer num) {
		String zero = "";
		for (int i = 0; i < 5 - (num + "").length(); i++) {
			zero += "0";
		}
		return zero + num;
	}


	/**
	 * 根据单据获取日期戳
	 * @param billNo 单据 TITLEyyyyMMddxxxxx
	 * @return 返回yyyyMMdd部分
	 */
	public static String getDate(String billNo) {

		Pattern pattern = Pattern.compile("\\d{8}");
		Matcher matcher = pattern.matcher(billNo);
		if (matcher.find()) {
			String matched = matcher.group();
			return matched;
		}
		return null;
	}


	/**
	 * 根据单据 获取流水号
	 */
	public static Integer getSerialNumber(String billNo) {
		Assert.notNull(billNo);
		if(billNo.length() >= 5) {
			return NumberUtils.toInt(billNo.substring(billNo.length() - 5));
		}
		return 0;
	}

}
