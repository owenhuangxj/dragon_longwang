package com.trenska.longwang.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * 2019/4/22
 * 创建人:Owen
 */
public class NumberFormatter {
	/**
	 * @param obj 传入的小数
	 * @return
	 * @desc 1.0~1之间的BigDecimal小数，格式化后失去前面的0,则前面直接加上0。
	 * 2.传入的参数等于0，则直接返回字符串"0.00..."
	 * 3.大于1的小数，直接格式化返回字符串
	 */
	public static String format(BigDecimal obj, Integer retain) {
		String zeroNumber = "";
		if (retain != null) {
			for(int i = 0 ; i < retain ; i++)
				zeroNumber += "0";
		}
		DecimalFormat df = new DecimalFormat("#." + zeroNumber);
		if (obj.compareTo(BigDecimal.ZERO) == 0) {
			return "0.".concat(zeroNumber);
//			return BigDecimal.valueOf(Double.valueOf("0." + zeroNumber));
		} else if (obj.compareTo(BigDecimal.ZERO) > 0 && obj.compareTo(new BigDecimal(1)) < 0) {
			return "0"+df.format(obj);
//			return BigDecimal.valueOf(Double.valueOf("0".concat(df.format(obj))));
		} else {
			return df.format(obj);
//			return BigDecimal.valueOf(Double.valueOf(df.format(obj)));
		}
	}
}
