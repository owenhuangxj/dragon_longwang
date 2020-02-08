package com.trenska.longwang.util;


/**
 * 人民币转大写
 */
public class RMBUtil {

	/**
	 * 小写转换大写金额
	 */
	public static String toUpper(String amount) {
		StringBuffer result = new StringBuffer("");
		String[] tmp = amount.replaceAll(",", "").split("\\.");
		String integer = tmp[0];
		final int LEN = integer.length();
		if (LEN > 12) {
			throw new RuntimeException("太大了 ，处理不了啊！");
		}
		for (int k = 12; k > LEN; k--) {
			integer = "0" + integer;
		}
		System.out.println(integer);
		int part1 = Integer.parseInt(integer.substring(0, 4));
		int part2 = Integer.parseInt(integer.substring(4, 8));
		int part3 = Integer.parseInt(integer.substring(8, 12));

		System.out.println(part1 + "|" + part2 + "|" + part3);

		if (part1 != 0) {
			result.append(parseInt3(part1) + "亿");
		}

		if (part2 != 0) {
			result.append(parseInt3(part2) + "万");
		}

		if (part3 != 0) {
			result.append(parseInt3(part3));
		} else {
			result.append("零");
		}

		result.append("元");

		if (tmp.length == 2) {
			result.append(parseFloat(tmp[1]));
		} else {
			result.append("整");
		}

		String ret = result.toString();
		System.out.println(result);

		while (ret.indexOf("零零") != -1) {
			ret = ret.replace("零零", "零");
		}

		ret = ret.replace("零亿", "亿").replace("零万", "万");

		System.out.println(ret);
		int index_y = ret.indexOf("零元");
		if (index_y == 0) {
			ret = ret.substring(2);
		} else if (index_y > 0) {
			ret = ret.replace("零元", "元");
		}

		if (ret.indexOf("零") == 0) {//金额为分的情况
			ret = ret.substring(1);
		}

		return ret;
	}

	/**
	 * 解析四位整数转换为中文金额大写
	 *
	 * @return
	 */
	public static String parseInt(int i) {
		String[] num = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
		String result = "";
		int tmp = i;
		if (tmp / 1000 != 0) {
			result += num[tmp / 1000] + "仟";
			tmp = tmp - (tmp / 1000) * 1000;
		}

		if (tmp / 100 != 0) {
			result += num[tmp / 100] + "佰";
			tmp = tmp - (tmp / 100) * 100;
		}

		if (tmp / 10 != 0) {
			result += num[tmp / 10] + "拾";
			tmp = tmp - (tmp / 10) * 10;
		}
		if (tmp != 0)
			result += num[tmp];
		return result;
	}

	/**
	 * 处理1万以下的数，小于千、百、拾需要补零
	 *
	 * @param money
	 * @return
	 */
	public static String parseInt3(int money) {
		String[] num = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
		String result = "";
		int tmp = money;
		if (tmp / 1000 != 0) {
			result += num[tmp / 1000] + "仟";
			tmp = tmp - (tmp / 1000) * 1000;
		} else {
			result += "零";
		}

		if (tmp / 100 != 0) {
			result += num[tmp / 100] + "佰";
			tmp = tmp - (tmp / 100) * 100;
		} else {
			result += "零";
		}

		if (tmp / 10 != 0) {
			result += num[tmp / 10] + "拾";
			tmp = tmp - (tmp / 10) * 10;
		} else {
			result += "零";
		}
		if (tmp != 0)
			result += num[tmp];
		return result;
	}

	/**
	 * 解析小数部分
	 */
	public static String parseFloat(String sStr) {
		if (sStr.length() == 1) {
			sStr += "0";
		}
		int k = Integer.parseInt(sStr);
		if (k == 0) {
			return "整";
		}

		String[] num = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "七", "捌", "玖"};
		String result = "";

		int jiao = k / 10;
		result += num[jiao] + (jiao == 0 ? "" : "角");

		int fen = k % 10;
		if (fen > 0)
			result += num[fen] + "分";

		return result;
	}


	public static void main(String[] args) {
		System.out.println(toUpper("100100.01"));
	}
}


