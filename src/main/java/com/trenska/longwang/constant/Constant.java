package com.trenska.longwang.constant;
/**
 * 2019/4/17
 * 创建人:Owen
 */
public class Constant {

	/**
	 * 登陆超时错误码
	 */
	public final static Integer ACCESS_TIMEOUT = 10401;
	public final static String ACCESS_TIMEOUT_MSG = "登陆超时";

	public final static String RKD_CHINESE = "入库单";
	public final static String RKDZF_CHINESE = "入库单(作废)";
	public final static String CKD_CHINESE = "出库单";
	public final static String XSCK_CHINESE = "销售出库";
	public final static String THCK_CHINESE = "退货出库";
	public final static String ZFRK_CHINESE = "作废入库";
	public final static String ZFRKD_CHINESE = "作废入库单";
	public final static String CKDZF_CHINESE = "出库单(作废)";
	public final static String ZFCK_CHINESE = "作废出库";
	public final static String THRK_CHINESE = "退货入库";

	public final static  String DH_TITLE = "DH";
	public final static  String TH_TITLE = "TH";

	public final static String THD_CHINESE = "退货单";



	public final static String RK_TITILE = "RK";
	public final static String QCRK_CHINESE = "期初入库";
	public final static String QCQK_CHINESE = "期初欠款";

	/**
	 * 出库前缀
	 */
	public final static String CK_TITLE = "CK";
	/**
	 * 销售出库前缀
	 */
	public final static String XSCK_TITLE = "CK";
	/**
	 * 作废入库前缀
	 */
	public final static String ZFRK_TITLE = "RK";

	/**
	 * 销售出库前缀
	 */
	public final static String BSCK_TITLE = "BS";
	/**
	 * 作废入库前缀
	 */
	public final static String BYRK_TITLE = "BY";

	public final static String LOGIN_FAILURE_MSG = "用户名或者密码错误";

	public final static String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public final static String BILL_TIME_FORMAT = "yyyyMMdd";

	public final static String DHD_CHINESE = "订货单";
	public final static String DHD_CX_CHINESE = "订货单(撤销)";
	public final static String DHD_ZF_CHINESE = "订货单(作废)";
	public final static String DHD_HG_CHINESE = "订货单(核改)";

	public final static String SK_TITLE = "SK";
	public final static String SK_CHINESE = "收款";
	public final static String SKD_CHINESE = "收款单";
	public final static String SKD_ZF_CHINESE = "收款单(作废)";
	public final static String FK_TITLE = "FK";
	public final static String FK_CHINESE = "付款";
	public final static String FKD_CHINESE = "付款单";

	/**
	 * 交易明细操作类型
	 */
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public final static String NEW = "新建";
	public final static String XSSK = "销售收款";
	public final static String THFK = "退货付款";
	public final static String CANCEL = "取消";
	public final static String INVALID = "作废";
	public final static String REPEAL_AUDIT = "撤销审核";

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	public final static String srcEncoding = "UTF-8";

	public final static String destEncoding = "iso-8859-1";

	/******************************************************************************************************************/
	public final static String ILLEGAL_DELETE_MSG = "库存记录数据被非法删除，无法作废入库单，请联系管理人员";

	/******************************************************************************************************************/
	public final static String REDIS_TEMPLATE_NAME = "redisTemplate";
	public static final String REDIS_JSON_TEMPLATE_NAME = "redisJsonTemplate";

	/******************************************************************************************************************/
	public final static String TOKEN_NAME = "token";
	/******************************************************************************************************************/
	public final static String EMP_ID_IDENTIFIER = "::empId";
	/******************************************************************************************************************/
	public final static String ACCESS_TOKEN_IDENTIFIER = "access-token::";

	/******************************************************************************************************************/
	public final static String CUSTOMER_NOT_EXISTS_MSG = "客户信息不存在";
	public final static String CUSTOMER_OUT_OF_DEBT_MSG = "客户欠款已超过额度";
	/******************************************************************************************************************/
	public final static String SYS_CONFIG_IDENTIFIER = "sysConfig";
	/******************************************************************************************************************/
	public final static String AUDIT = "未财审核";
	public final static String AUDITED = "已财审";

	public final static String XSSP = "销售商品";
	public final static String XSSP_HG = "销售商品(核改) ";
	public final static String XSSP_ZF = "销售商品(作废)";


	public static final String ZF = "(作废)";
	public static final String XSSP_CX = "销售商品(撤销)";
	public static final String INVALID_INDENT = "无效的订货单";
	public static final String INVALID_RECEIPT = "无效的收款单";
	public static final String INVALID_PAY_RECEIPT = "无效的收款单";
	public static final String INDENT_FORBIDDEN = "订单已完成，禁止此操作";
	public static final String STOCKIN_SUCC = "入库成功";
	public static final String STOCKOUT_SUCC = "出库成功";
	public static final String THDZF = "退货单(作废)";
	public static final String THTK = "退货退款";
	public static final String THTK_ZF = "退货退款(作废)";
	public static final String LOGIN_TIMEOUT = "登陆超时";

	public static final String NO_DEBT_LIMIT_LABLE = "-0.0000000001";
	public static final String COLLECTOR_LABLE = "::";
	public static final String WU = "无";
	public static final String DHD_ZF_CHINESE_CHANGE = "销售商品(核改)";
	public static final String REQUEST_REPEAT = "重复提交";
	public static final String DATE_FORMAT = "yyyyMMdd";
	public static final String TZD_PREFIX = "TZ";
	public static final String TZD_CHINESE = "调账单";
	public static final String TZ_ADD_OPER = "调账增加";
	public static final String TZ_SUBSTRACT_OPER = "调账减少";

	public static final String ON_SALE = "上架";
	public static final String OFF_SALE = "下架";
	public static final String CKZF = "出库作废";
	public static final String NO_ACCESS_PERMISSION_MSG = "无访问权限";

	public static final int ACCESS_FORBIDDEN = 10405;
	public static final int TOKEN_MISSING = 10402;
	public static final String TOKEN_MISSING_MSG = "无效的令牌";
	public static final String CHANGE_SUCC = "修改成功";
}
