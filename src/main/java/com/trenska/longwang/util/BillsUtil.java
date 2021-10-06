package com.trenska.longwang.util;

import com.trenska.longwang.constant.DragonConstant;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
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
	 *
	 * @param title 单据抬头
	 * @param num   单据编号
	 */
	public static String makeBillNo(String title, int num) {
		String datePart = new SimpleDateFormat(DragonConstant.BILL_TIME_FORMAT).format(new Date());
		String serialNumber = fillZero(num);
		return title.concat(datePart).concat(serialNumber);
	}

	/**
	 * 对小于10000的数字填充前缀0
	 */
	private static String fillZero(int num) {
		StringBuilder zero = new StringBuilder();
		for (int index = 1; index <= 5 - (String.valueOf(num)).length(); index++) {
			zero.append("0");
		}
		return zero.toString() + num;
	}


	/**
	 * 根据单据获取yyyyMMdd格式的日期戳
	 *
	 * @param billNo 单据 TITLEyyyyMMddxxxxx
	 * @return 返回yyyyMMdd部分
	 */
	public static String getDateOfBillNo(Optional<String> billNo) {
		// yyyyMMdd部分 刚好是字符串中打头的8个连续数字
		Pattern pattern = Pattern.compile("\\d{8}");
		Matcher matcher = pattern.matcher(billNo.get());
		if (matcher.find()) {
			String matched = matcher.group();
			return matched;
		}
		return null;
	}

	/**
	 * 根据单据 获取流水号
	 */
	public static int getSerialNumberOfBillNo(Optional<String> billNo) {
		if (billNo.get().length() >= 5) {
			return NumberUtils.toInt(billNo.get().substring(billNo.get().length() - 5));
		}
		return 0;
	}
}