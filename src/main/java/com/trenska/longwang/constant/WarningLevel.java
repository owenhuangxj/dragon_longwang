package com.trenska.longwang.constant;

import io.swagger.annotations.ApiModel;

/**
 * 2019/4/27
 * 创建人:Owen
 */
@ApiModel("商品预警级别常量")
public class WarningLevel {
	public final static String NEW = "新货";
	public final static String UNSALABLING = "滞销风险商品";
	public final static String UNSALABLE = "滞销商品";
	public final static String EXPIRING = "临期商品";
	public final static String EXPIRED = "过期商品";
	public static final String INIT_STOCK = "期初入库";
}
